package com.example.moviesdiscovery.features.movies.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdiscovery.core.data.paging.PagingLoadState
import com.example.moviesdiscovery.core.data.paging.PagingLoadStates
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import com.example.moviesdiscovery.features.movies.data.FavoriteMoviesRepository
import com.example.moviesdiscovery.features.movies.data.MovieRepository
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.mergeFavorites
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.MoviesPagingUiState
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiStates
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.asUiStates
import com.example.moviesdiscovery.features.movies.ui.model.insertDateSeparators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OFFLINE_CACHED_SIZE = 10

class MoviesViewModel(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoriteMoviesRepository,
    connectivityNetworkMonitor: ConnectivityNetworkMonitor
) : ViewModel() {

    private val _uiEvents = Channel<MoviesUiEvent>(capacity = Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()
    private val _uiData = MutableStateFlow(
        MoviesUiData(
            isRefreshingPull = false,
            isLoading = true,
            state = MoviesUiState.InitialLoading
        )
    )
    val uiData = _uiData.asStateFlow()
    private val uiState: MoviesUiState
        get() = _uiData.value.state

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

    private val pagingLoadStatesFlow = pagingDataFetcher.loadStates
        .onEach { processLoadStateToSideEffects(it) }
        .map { it.asUiStates(isOnline) }

    private val pagingLoadStates: PagingLoadStates
        get() = pagingDataFetcher.loadStates.value

    private val favoriteIdsFlow = favoritesRepository.getMovieIdsFlow().map { it.toSet() }
    private val pagingMoviesFlow =
        combine(pagingDataFetcher.items, favoriteIdsFlow) { movies, favoriteIds ->
            movies.mergeFavorites(favoriteIds).asUiData()
        }

    val pagingUiState: StateFlow<MoviesPagingUiState> =
        combine(pagingLoadStatesFlow, pagingMoviesFlow, ::buildPagingUiState)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                buildPagingUiState(pagingLoadStates.asUiStates(isOnline), emptyList())
            )

    val cachedMovies: StateFlow<List<MovieUiItem>> =
        movieRepository.getMoviesByQueryFlow(OFFLINE_CACHED_SIZE)
            .takeWhile {
                if (uiState is MoviesUiState.OfflineCachedContent) { // Cached data ready to show
                    updateUiData(isLoading = false)
                }
                uiState !is MoviesUiState.PagingContent
            }
            .map { it.map(Movie::asUiData).insertDateSeparators() }
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            refreshOnOnlineOrAction {
                updateUiData(state = MoviesUiState.OfflineCachedContent)
            }
        }
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
        if (loadState.hasError) {
            viewModelScope.launch {
                when {
                    isOnline -> {
                        loadState.error?.message?.let {
                            sendEvent(MoviesUiEvent.ShowToast(it))
                        }
                    }

                    uiState !is MoviesUiState.InitialLoading -> {
                        sendEvent(MoviesUiEvent.NoInternetToast)
                    }
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
        updateUiData(
            isLoading = false,
            state = reduceNextUiState(pagingLoadStates.refresh)
        )
    }

    private fun reduceNextUiState(refreshLoadState: PagingLoadState): MoviesUiState {
        val nextState: MoviesUiState = when (val uiState = uiState) {
            MoviesUiState.InitialLoading -> {
                when (refreshLoadState) {
                    PagingLoadState.Loading -> uiState
                    is PagingLoadState.NotLoading -> MoviesUiState.PagingContent
                    is PagingLoadState.Error -> {
                        if (isOnline) {
                            MoviesUiState.PagingContent
                        } else {
                            MoviesUiState.OfflineCachedContent
                        }
                    }
                }
            }

            MoviesUiState.OfflineCachedContent -> {
                when (refreshLoadState) {
                    PagingLoadState.Loading -> uiState
                    is PagingLoadState.NotLoading -> MoviesUiState.PagingContent
                    is PagingLoadState.Error -> {
                        if (isOnline) {
                            MoviesUiState.PagingContent
                        } else {
                            uiState
                        }
                    }
                }
            }

            is MoviesUiState.PagingContent -> uiState
        }
        return nextState
    }

    private fun updateUiData(
        isRefreshingPull: Boolean? = null,
        isLoading: Boolean? = null,
        state: MoviesUiState? = null
    ) {
        _uiData.update {
            it.copy(
                isRefreshingPull = isRefreshingPull ?: it.isRefreshingPull,
                isLoading = isLoading ?: it.isLoading,
                state = state ?: it.state
            )
        }
    }

    private suspend fun showNoInternetToast() {
        sendEvent(MoviesUiEvent.NoInternetToast)
    }

    private suspend fun sendEvent(event: MoviesUiEvent) {
        _uiEvents.send(event)
    }

    private fun buildPagingUiState(
        pagingLoadStates: PagingLoadUiStates,
        movies: List<MovieUiItem>
    ): MoviesPagingUiState {
        return MoviesPagingUiState(
            items = movies,
            loadStates = pagingLoadStates,
            prefetchDistance = pagingPrefetchDistance
        )
    }

}

data class MoviesUiData(
    val isRefreshingPull: Boolean,
    val isLoading: Boolean,
    val state: MoviesUiState
) {
    val isLoadingOnFullScreen: Boolean
        get() = isLoading && !isRefreshingPull
}

sealed class MoviesUiEvent {
    data object NoInternetToast : MoviesUiEvent()
    data class ShowToast(val message: String) : MoviesUiEvent()
}

sealed class MoviesUiState {
    data object InitialLoading : MoviesUiState()
    data object PagingContent : MoviesUiState()
    data object OfflineCachedContent : MoviesUiState()
}
