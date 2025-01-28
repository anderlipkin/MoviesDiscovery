package com.example.moviesdiscovery.features.movies.ui.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.moviesdiscovery.core.ui.model.LazyListScrollPosition

@Composable
fun LazyListState.onPrefetchDistanceReached(distance: Int, loadNextPage: () -> Unit) {
    val currentLoadNextPage by rememberUpdatedState(loadNextPage)
    val shouldLoadMore by remember(distance) {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index + distance >= layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            currentLoadNextPage()
        }
    }
}

@Composable
fun LazyListState.saveScrollPositionOnDispose(
    onScrollPositionSave: (LazyListScrollPosition) -> Unit
) {
    val currentOnScrollPositionSave by rememberUpdatedState(onScrollPositionSave)
    DisposableEffect(this) {
        onDispose {
            currentOnScrollPositionSave(
                LazyListScrollPosition(
                    firstVisibleItemIndex = firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                )
            )
        }
    }
}

