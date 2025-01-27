package com.example.moviesdiscovery.features.movies.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem

@Composable
fun MoviesContent(
    movieItems: List<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // TODO maybe need to adding in tabRow
//        item {
//            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
//        }
        movieItems(movieItems, onItemClick, onFavoriteChange)
        // TODO check after complete HomeScreen
//        item {
//            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
//        }
    }
}
