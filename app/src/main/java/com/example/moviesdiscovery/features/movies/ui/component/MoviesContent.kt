package com.example.moviesdiscovery.features.movies.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moviesdiscovery.core.ui.model.LazyListScrollPosition
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem

@Composable
fun MoviesContent(
    movieItems: List<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scrollPosition: LazyListScrollPosition = LazyListScrollPosition(),
    onScrollPositionSave: (LazyListScrollPosition) -> Unit = {}
) {
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollPosition.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = scrollPosition.firstVisibleItemScrollOffset
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        movieItems(movieItems, onItemClick, onFavoriteChange)
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing)) }
    }
    lazyListState.saveScrollPositionOnDispose(onScrollPositionSave)
}
