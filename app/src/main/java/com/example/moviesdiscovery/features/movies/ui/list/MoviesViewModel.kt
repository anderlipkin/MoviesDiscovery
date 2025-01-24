package com.example.moviesdiscovery.features.movies.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.moviesdiscovery.features.movies.data.MovieRepository
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.insertDateSeparators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OFFLINE_CACHED_SIZE = 10

class MoviesViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiEvents = Channel<MoviesUiEvent>(capacity = Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()
    private val _uiData = MutableStateFlow<MoviesUiData>(
        MoviesUiData(
            isOnline = true,
            isRefreshingPull = false,
            isLoading = true,
            state = MoviesUiState.InitialLoading
        )
    )
    val uiData = _uiData.asStateFlow()
    private val uiState: MoviesUiState
        get() = _uiData.value.state

    val cachedMovies = uiData
        .map { it.state !is MoviesUiState.PagingContent }
        .distinctUntilChanged()
        .flatMapLatest { shouldCachedData ->
            if (shouldCachedData) {
                movieRepository.getMoviePagingFlow(OFFLINE_CACHED_SIZE)
                    .map { it.map(Movie::asUiData).insertDateSeparators() }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagingDataFlow =
        movieRepository.getMovieNetworkPagingFlow()
            .map { it.asUiData() }
            .cachedIn(viewModelScope)

    fun getMovieById(initialMovie: MovieUiItem.Movie): StateFlow<MovieUiItem.Movie> =
        movieRepository.getMovieByIdFlow(initialMovie.id)
            .map { it!!.asUiData() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialMovie)

    fun onPullToRefresh() {
        updateUiData(isRefreshing = true)
    }

    fun onFavoriteChange(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            movieRepository.updateFavoriteMovie(id, isFavorite)
        }
    }

    fun onLoadStateUpdate(loadState: CombinedLoadStates) {
        val uiData = uiData.value
        val currentUiState = uiState
        val refreshLoadState = loadState.refresh
        val isOnline = uiData.isOnline
        val isLoading = refreshLoadState is LoadState.Loading
        val nextState: MoviesUiState = getNextUiState(currentUiState, refreshLoadState, isOnline)
        processLoadStateToSideEffects(currentUiState, loadState, isOnline)
        updateUiData(
            isRefreshing = isLoading.takeIf { uiData.isRefreshingPull },
            isLoading = isLoading,
            state = nextState
        )
    }

    private fun processLoadStateToSideEffects(
        currentUiState: MoviesUiState,
        loadState: CombinedLoadStates,
        isOnline: Boolean
    ) {
        if (loadState.hasError && currentUiState !is MoviesUiState.InitialLoading) {
            val errorMessage = (loadState.refresh as? LoadState.Error)?.error?.message
                ?: (loadState.append as? LoadState.Error)?.error?.message
            viewModelScope.launch {
                if (isOnline) {
                    errorMessage?.let {
                        sendEvent(MoviesUiEvent.ShowToast(it))
                    }
                } else {
                    sendEvent(MoviesUiEvent.NoInternetToast)
                }
            }
        }
    }

    private fun getNextUiState(
        currentUiState: MoviesUiState,
        refreshLoadState: LoadState,
        isOnline: Boolean
    ): MoviesUiState {
        val nextState: MoviesUiState = when (currentUiState) {
            MoviesUiState.InitialLoading -> {
                when (refreshLoadState) {
                    LoadState.Loading -> currentUiState
                    is LoadState.NotLoading -> MoviesUiState.PagingContent
                    is LoadState.Error -> {
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
                    LoadState.Loading -> currentUiState
                    is LoadState.NotLoading -> MoviesUiState.PagingContent
                    is LoadState.Error -> {
                        if (isOnline) {
                            MoviesUiState.PagingContent
                        } else {
                            currentUiState
                        }
                    }
                }
            }

            is MoviesUiState.PagingContent -> currentUiState
        }
        return nextState
    }

    fun onNetworkOnlineChanged(isOnline: Boolean) {
        updateUiData(isOnline = isOnline)
    }

    fun onItemClick(movieId: Int) {

    }

    private fun updateUiData(
        isOnline: Boolean? = null,
        isRefreshing: Boolean? = null,
        isLoading: Boolean? = null,
        state: MoviesUiState? = null
    ) {
        _uiData.update {
            it.copy(
                isOnline = isOnline ?: it.isOnline,
                isRefreshingPull = isRefreshing ?: it.isRefreshingPull,
                isLoading = isLoading ?: it.isLoading,
                state = state ?: it.state
            )
        }
    }

    fun showNoInternetToast() {
        viewModelScope.launch {
            sendEvent(MoviesUiEvent.NoInternetToast)
        }
    }

    private suspend fun sendEvent(event: MoviesUiEvent) {
        _uiEvents.send(event)
    }

}

data class MoviesUiData(
    val isOnline: Boolean,
    val isRefreshingPull: Boolean,
    val isLoading: Boolean,
    val state: MoviesUiState
) {
    val isLoadingOnFullScreen: Boolean
        get() = (isLoading && !isRefreshingPull) || state is MoviesUiState.InitialLoading
}

sealed interface MoviesUiEvent {
    data object NoInternetToast : MoviesUiEvent
    data class ShowToast(val message: String) : MoviesUiEvent
}

sealed interface MoviesUiState {
    data object InitialLoading : MoviesUiState
    data object PagingContent : MoviesUiState
    data object OfflineCachedContent : MoviesUiState
}

private fun PagingData<Movie>.asUiData(): PagingData<MovieUiItem> =
    map(Movie::asUiData)
        .insertSeparators(TerminalSeparatorType.SOURCE_COMPLETE) { beforeMovie, afterMovie ->
            when {
                afterMovie == null -> null
                beforeMovie?.monthAndYearRelease != afterMovie.monthAndYearRelease ->
                    MovieUiItem.DateSeparatorItem(afterMovie.monthAndYearRelease)

                else -> null
            }
        }
