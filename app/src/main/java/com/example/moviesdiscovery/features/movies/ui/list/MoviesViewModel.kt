package com.example.moviesdiscovery.features.movies.ui.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdiscovery.core.data.paging.PagingLoadState
import com.example.moviesdiscovery.core.data.paging.PagingLoadStates
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import com.example.moviesdiscovery.core.ui.model.LazyListScrollPosition
import com.example.moviesdiscovery.features.movies.data.FavoriteMoviesRepository
import com.example.moviesdiscovery.features.movies.data.MovieRepository
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.mergeFavorites
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.MoviesPagingUiData
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.asUiStates
import com.example.moviesdiscovery.features.movies.ui.model.insertDateSeparators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OFFLINE_CACHED_SIZE = 10

class MoviesViewModel(
    movieRepository: MovieRepository,
    connectivityNetworkMonitor: ConnectivityNetworkMonitor,
    private val favoritesRepository: FavoriteMoviesRepository
) : ViewModel() {

    private val _uiEvents = Channel<MoviesUiEvent>(capacity = Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()
    private val _uiData = MutableStateFlow(
        MoviesUiData(
            isRefreshingPull = false,
            isLoading = true,
        )
    )
    val uiData = _uiData.asStateFlow()

    val isOnlineFlow = connectivityNetworkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = connectivityNetworkMonitor.isCurrentlyConnected()
        )
    private val isOnline
        get() = isOnlineFlow.value

    private val pagingDataFetcher = movieRepository.getMoviesPagingFetcher()
    private val pagingPrefetchDistance: Int
        get() = pagingDataFetcher.pagingConfig.value.prefetchDistance

    private val pagingLoadStates: PagingLoadStates
        get() = pagingDataFetcher.loadStates.value

    private val favoriteIdsFlow = favoritesRepository.getMovieIdsFlow().map { it.toSet() }

    private val listMode =
        MutableStateFlow(if (isOnline) MoviesListUiMode.Paging else MoviesListUiMode.OfflineCached)

    private val scrollPositionFlow = MutableStateFlow(LazyListScrollPosition())

    private val cachedListState: Flow<MoviesListUiState.OfflineCached> =
        movieRepository.getMoviesByQueryFlow(OFFLINE_CACHED_SIZE)
            .map { it.map(Movie::asUiData).insertDateSeparators() }
            .distinctUntilChanged()
            .combine(scrollPositionFlow) { movies, scrollPosition ->
                updateUiData(isLoading = false)
                MoviesListUiState.OfflineCached(
                    movies = movies,
                    scrollPosition = scrollPosition
                )
            }

    private val pagingLoadStatesFlow = pagingDataFetcher.loadStates
        .onEach { processLoadStateToSideEffects(it) }
        .map { it.asUiStates(isOnline) }

    private val pagingMoviesFlow =
        combine(pagingDataFetcher.items, favoriteIdsFlow) { movies, favoriteIds ->
            movies.mergeFavorites(favoriteIds).asUiData()
        }

    private val pagingListState: Flow<MoviesListUiState.Paging> =
        combine(
            pagingLoadStatesFlow,
            pagingMoviesFlow,
            scrollPositionFlow
        ) { pagingLoadStates, movies, scrollPosition ->
            MoviesListUiState.Paging(
                data = MoviesPagingUiData(
                    items = movies,
                    loadStates = pagingLoadStates,
                    prefetchDistance = pagingPrefetchDistance
                ),
                scrollPosition = scrollPosition
            )
        }

    val listUiState: StateFlow<MoviesListUiState> = listMode.flatMapLatest { listMode ->
        when (listMode) {
            MoviesListUiMode.OfflineCached -> cachedListState
            MoviesListUiMode.Paging -> pagingListState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialListUiState())

    init {
        viewModelScope.launch {
            refreshOnOnlineOrAction {}
        }
    }

    private fun initialListUiState(): MoviesListUiState =
        when (listMode.value) {
            is MoviesListUiMode.Paging ->
                MoviesListUiState.Paging(
                    data = MoviesPagingUiData(
                        items = emptyList(),
                        loadStates = pagingLoadStates.asUiStates(isOnline),
                        prefetchDistance = pagingPrefetchDistance
                    ),
                    scrollPosition = scrollPositionFlow.value
                )

            MoviesListUiMode.OfflineCached ->
                MoviesListUiState.OfflineCached(
                    movies = emptyList(),
                    scrollPosition = scrollPositionFlow.value
                )
        }

    fun saveScrollPosition(scrollPosition: LazyListScrollPosition) {
        this.scrollPositionFlow.update { scrollPosition }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshOnOnlineOrAction {
                showNoInternetToast()
            }
        }
    }

    fun onPullToRefresh() {
        viewModelScope.launch {
            updateUiData(isRefreshingPull = true)
            refreshOnOnlineOrAction {
                showNoInternetToast()
            }
            delay(20) // Can remove after increase material3 version https://issuetracker.google.com/issues/359949836
            updateUiData(isRefreshingPull = false)
        }
    }

    fun onAppendRetryClick() {
        viewModelScope.launch {
            if (isOnline) {
                pagingDataFetcher.loadNextPage()
            } else {
                showNoInternetToast()
            }
        }
    }

    fun onLoadNextPage() {
        viewModelScope.launch {
            pagingDataFetcher.loadNextPage()
        }
    }

    fun onFavoriteChange(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.updateFavoriteMovie(id, isFavorite)
        }
    }

    fun onItemClick(movieId: Int) {

    }

    private fun processLoadStateToSideEffects(loadState: PagingLoadStates) {
        viewModelScope.launch {
            if (loadState.hasError) {
                if (isOnline) {
                    loadState.error?.message?.let {
                        sendEvent(MoviesUiEvent.ShowToast(it))
                    }
                } else {
                    sendEvent(MoviesUiEvent.NoInternetToast)
                }
            }
        }
    }

    private suspend fun refreshOnOnlineOrAction(action: suspend () -> Unit) {
        if (isOnline) {
            refreshPaging()
        } else {
            action()
        }
    }

    private suspend fun refreshPaging() {
        updateUiData(isLoading = true)
        pagingDataFetcher.refresh()
        if (listMode.value is MoviesListUiMode.OfflineCached) {
            switchToPagingListModeIfAvailable()
        }
        updateUiData(isLoading = false)
    }

    private fun switchToPagingListModeIfAvailable() {
        val isPagingAvailable = when (pagingLoadStates.refresh) {
            PagingLoadState.Loading -> false
            is PagingLoadState.NotLoading -> true
            is PagingLoadState.Error -> isOnline
        }
        if (isPagingAvailable) {
            scrollPositionFlow.update { LazyListScrollPosition() }
            listMode.update { MoviesListUiMode.Paging }
        }
    }

    private fun updateUiData(
        isRefreshingPull: Boolean? = null,
        isLoading: Boolean? = null
    ) {
        _uiData.update {
            it.copy(
                isRefreshingPull = isRefreshingPull ?: it.isRefreshingPull,
                isLoading = isLoading ?: it.isLoading
            )
        }
    }

    private suspend fun showNoInternetToast() {
        sendEvent(MoviesUiEvent.NoInternetToast)
    }

    private suspend fun sendEvent(event: MoviesUiEvent) {
        _uiEvents.send(event)
    }

}

data class MoviesUiData(
    val isRefreshingPull: Boolean,
    val isLoading: Boolean
) {
    val isFullScreenLoading: Boolean
        get() = isLoading && !isRefreshingPull
}

sealed class MoviesUiEvent {
    data object NoInternetToast : MoviesUiEvent()
    data class ShowToast(val message: String) : MoviesUiEvent()
}

sealed interface MoviesListUiMode {
    data object Paging : MoviesListUiMode
    data object OfflineCached : MoviesListUiMode
}

sealed class MoviesListUiState {
    abstract val scrollPosition: LazyListScrollPosition

    data class Paging(
        override val scrollPosition: LazyListScrollPosition,
        val data: MoviesPagingUiData
    ) : MoviesListUiState()

    @Immutable
    data class OfflineCached(
        override val scrollPosition: LazyListScrollPosition,
        val movies: List<MovieUiItem>
    ) : MoviesListUiState()

}
