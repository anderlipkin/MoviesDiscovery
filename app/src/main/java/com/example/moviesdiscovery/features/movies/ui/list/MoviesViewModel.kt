package com.example.moviesdiscovery.features.movies.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _pullToRefreshActive = MutableStateFlow(false)
    val pullToRefreshActive: StateFlow<Boolean> = _pullToRefreshActive

    val pagingDataFlow =
        movieRepository.getMoviePagingFlow()
            .map { pagingData ->
                pagingData.map(Movie::asUiData)
                    .insertSeparators(TerminalSeparatorType.SOURCE_COMPLETE) { beforeMovie, afterMovie ->
                        when {
                            afterMovie == null -> null
                            beforeMovie?.monthAndYearRelease != afterMovie.monthAndYearRelease ->
                                MovieUiItem.DateSeparatorItem(afterMovie.monthAndYearRelease)

                            else -> null
                        }
                    }
            }
            .cachedIn(viewModelScope)

    fun refresh() {
        _pullToRefreshActive.update { true }
    }

    fun onFavoriteChange(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            movieRepository.updateFavoriteMovie(id, isFavorite)
        }
    }

    fun updatePullToRefreshState(loadState: LoadState) {
        if (pullToRefreshActive.value) {
            _pullToRefreshActive.update { loadState is LoadState.Loading }
        }
    }

}

data class MoviesUiState(
    val pagingData: PagingData<MovieUiItem>
)
