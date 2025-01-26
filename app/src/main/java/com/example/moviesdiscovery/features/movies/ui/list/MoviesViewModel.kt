package com.example.moviesdiscovery.features.movies.ui.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdiscovery.core.data.paging.PagingLoadState
import com.example.moviesdiscovery.core.data.paging.PagingLoadStates
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import com.example.moviesdiscovery.features.movies.data.MovieRepository
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.mergeFavorites
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiStates
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.asUiStates
import com.example.moviesdiscovery.features.movies.ui.model.insertDateSeparators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OFFLINE_CACHED_SIZE = 10

class MoviesViewModel(
    private val movieRepository: MovieRepository,
    connectivityNetworkMonitor: ConnectivityNetworkMonitor
) : ViewModel() {

    private val _uiEvents = Channel<MoviesUiEvent>(capacity = Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    private val _viewModelState = MutableStateFlow(
        MoviesViewModelState(
            isRefreshingPull = false,
            isLoading = true
        )
    )

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

    private val favoriteIdsFlow = movieRepository.getFavoriteMovieIdsFlow().map { it.toSet() }
    private val pagingMoviesFlow =
        combine(pagingDataFetcher.items, favoriteIdsFlow) { movies, favoriteIds ->
            movies.mergeFavorites(favoriteIds).asUiData()
        }

    private val contentStateFlow = pagingDataFetcher.loadStates
        .scan(MoviesPagingLoadContentState.InitialLoading as MoviesPagingLoadContentState) { action, pagingLoadStates ->
            processLoadStateToSideEffects(action, pagingLoadStates)
            getNextContentState(action, pagingLoadStates.refresh, isOnline)
        }
        .distinctUntilChanged()
        .flatMapLatest { nextUiState ->
            when (nextUiState) {
                MoviesPagingLoadContentState.InitialLoading -> flowOf(MoviesContentUiState.InitialLoading)
                MoviesPagingLoadContentState.OfflineCached ->
                    movieRepository.getMoviesByQueryFlow(OFFLINE_CACHED_SIZE)
                        .map { it.map(Movie::asUiData).insertDateSeparators() }
                        .map { MoviesContentUiState.OfflineCached(it) }

                MoviesPagingLoadContentState.Paging ->
                    combine(
                        pagingDataFetcher.loadStates.map { it.asUiStates(isOnline) },
                        pagingMoviesFlow,
                        ::buildPagingUiState
                    )
            }
        }

    val uiState = combine(_viewModelState, contentStateFlow) { viewModelState, contentState ->
        MoviesUiState(
            isRefreshingPull = viewModelState.isRefreshingPull,
            isLoading = viewModelState.isLoading,
            contentState = contentState
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MoviesUiState(
            isRefreshingPull = _viewModelState.value.isRefreshingPull,
            isLoading = _viewModelState.value.isLoading,
            contentState = MoviesContentUiState.InitialLoading
        )
    )

    init {
        viewModelScope.launch {
            refreshPaging()
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
            updateViewModelState(isRefreshingPull = true)
            refreshOnOnlineOrAction {
                showNoInternetToast()
            }
            delay(20) // Can remove after increase material3 version https://issuetracker.google.com/issues/359949836
            updateViewModelState(isRefreshingPull = false)
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
            movieRepository.updateFavoriteMovie(id, isFavorite)
        }
    }

    fun onItemClick(movieId: Int) {

    }

    private fun processLoadStateToSideEffects(
        uiState: MoviesPagingLoadContentState,
        loadState: PagingLoadStates
    ) {
        if (loadState.hasError) {
            viewModelScope.launch {
                when {
                    isOnline -> {
                        loadState.error?.message?.let {
                            sendEvent(MoviesUiEvent.ShowToast(it))
                        }
                    }

                    uiState !is MoviesPagingLoadContentState.InitialLoading -> {
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
        updateViewModelState(isLoading = true)
        pagingDataFetcher.refresh()
        updateViewModelState(isLoading = false)
    }

    private fun getNextContentState(
        uiState: MoviesPagingLoadContentState,
        refreshLoadState: PagingLoadState,
        isOnline: Boolean
    ): MoviesPagingLoadContentState {
        val nextState: MoviesPagingLoadContentState = when (uiState) {
            MoviesPagingLoadContentState.InitialLoading -> {
                when (refreshLoadState) {
                    PagingLoadState.Loading -> uiState
                    is PagingLoadState.NotLoading -> MoviesPagingLoadContentState.Paging
                    is PagingLoadState.Error -> {
                        if (isOnline) {
                            MoviesPagingLoadContentState.Paging
                        } else {
                            MoviesPagingLoadContentState.OfflineCached
                        }
                    }
                }
            }

            is MoviesPagingLoadContentState.OfflineCached -> {
                when (refreshLoadState) {
                    PagingLoadState.Loading -> uiState
                    is PagingLoadState.NotLoading -> MoviesPagingLoadContentState.Paging
                    is PagingLoadState.Error -> {
                        if (isOnline) {
                            MoviesPagingLoadContentState.Paging
                        } else {
                            uiState
                        }
                    }
                }
            }

            is MoviesPagingLoadContentState.Paging -> uiState
        }
        return nextState
    }

    private fun updateViewModelState(
        isRefreshingPull: Boolean? = null,
        isLoading: Boolean? = null
    ) {
        _viewModelState.update {
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

    private fun buildPagingUiState(
        pagingLoadStates: PagingLoadUiStates,
        movies: List<MovieUiItem>
    ): MoviesContentUiState.Paging {
        return MoviesContentUiState.Paging(
            items = movies,
            loadStates = pagingLoadStates,
            prefetchDistance = pagingPrefetchDistance
        )
    }

}

data class MoviesViewModelState(
    val isRefreshingPull: Boolean,
    val isLoading: Boolean,
)

data class MoviesUiState(
    val isRefreshingPull: Boolean,
    val isLoading: Boolean,
    val contentState: MoviesContentUiState
) {
    val isLoadingOnFullScreen: Boolean
        get() = isLoading && !isRefreshingPull
}

sealed class MoviesUiEvent {
    data object NoInternetToast : MoviesUiEvent()
    data class ShowToast(val message: String) : MoviesUiEvent()
}

sealed class MoviesPagingLoadContentState {
    data object InitialLoading : MoviesPagingLoadContentState()
    data object Paging : MoviesPagingLoadContentState()
    data object OfflineCached : MoviesPagingLoadContentState()
}

sealed class MoviesContentUiState {
    data object InitialLoading : MoviesContentUiState()

    @Immutable
    data class Paging(
        val items: List<MovieUiItem>,
        val loadStates: PagingLoadUiStates,
        val prefetchDistance: Int
    ) : MoviesContentUiState() {
        val appendPrefetchEnabled =
            !loadStates.endReached && loadStates.append.state is PagingLoadState.NotLoading
    }

    data class OfflineCached(
        val items: List<MovieUiItem>
    ) : MoviesContentUiState()
}
