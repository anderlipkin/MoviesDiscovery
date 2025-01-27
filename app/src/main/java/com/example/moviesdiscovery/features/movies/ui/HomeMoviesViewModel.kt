package com.example.moviesdiscovery.features.movies.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.features.movies.ui.favorite.FavoriteMoviesScreen
import com.example.moviesdiscovery.features.movies.ui.list.MoviesScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeMoviesViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow(HomeMoviesTab.Movies)
    val selectedTab: StateFlow<HomeMoviesTab> = _selectedTab

    fun updateSelectedTab(tab: HomeMoviesTab) {
        _selectedTab.value = tab
    }
}

enum class HomeMoviesTab(@StringRes val titleResId: Int) {
    Movies(R.string.home_tab_movies),
    Favorites(R.string.home_tab_favorites)
}

fun HomeMoviesTab.moviesScreen() =
    HomeMoviesTabContent(this) {
        MoviesScreen()
    }

fun HomeMoviesTab.favoriteScreen(onGoToLibraryClick: () -> Unit) =
    HomeMoviesTabContent(this) {
        FavoriteMoviesScreen(
            onGoToLibraryClick = onGoToLibraryClick
        )
    }

data class HomeMoviesTabContent(val tab: HomeMoviesTab, val content: @Composable () -> Unit)
