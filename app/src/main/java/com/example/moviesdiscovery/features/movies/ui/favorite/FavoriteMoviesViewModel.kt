package com.example.moviesdiscovery.features.movies.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesdiscovery.core.ui.model.LazyListScrollPosition
import com.example.moviesdiscovery.features.movies.data.FavoriteMoviesRepository
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteMoviesViewModel(
    private val repository: FavoriteMoviesRepository
) : ViewModel() {

    private val _scrollPosition = MutableStateFlow(LazyListScrollPosition())
    val scrollPosition: StateFlow<LazyListScrollPosition> = _scrollPosition

    val uiState: StateFlow<FavoriteMoviesUiState> = repository.getMoviesFlow()
        .map { movies -> FavoriteMoviesUiState.Success(movies.asUiData()) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            FavoriteMoviesUiState.Loading
        )

    init {
        initSyncData()
    }

    fun onItemClick(movieId: Int) {

    }

    fun onFavoriteChange(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteMovie(id, isFavorite)
        }
    }

    fun saveScrollPosition(scrollPosition: LazyListScrollPosition) {
        _scrollPosition.update { scrollPosition }
    }

    private fun initSyncData() {
        viewModelScope.launch {
            repository.syncData()
        }
    }

}

sealed class FavoriteMoviesUiState {
    data object Loading : FavoriteMoviesUiState()
    data class Success(val movies: List<MovieUiItem>) : FavoriteMoviesUiState()
}
