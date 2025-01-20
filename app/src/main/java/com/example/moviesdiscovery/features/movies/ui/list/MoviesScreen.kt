package com.example.moviesdiscovery.features.movies.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.moviesdiscovery.core.ui.ScreenPreview
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.ui.component.DateSeparator
import com.example.moviesdiscovery.features.movies.ui.component.MovieCard
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.itemContentType
import com.example.moviesdiscovery.features.movies.ui.model.itemKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(modifier: Modifier = Modifier, viewModel: MoviesViewModel = koinViewModel()) {
    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val pullToRefreshActive by viewModel.pullToRefreshActive.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = pullToRefreshActive,
        onRefresh = {
            pagingItems.refresh()
            viewModel.refresh()
        },
        modifier = modifier
    ) {
        MoviesContent(
            pagingItems = pagingItems,
            onFavoriteChange = viewModel::onFavoriteChange
        )
    }

    LaunchedEffect(pagingItems.loadState.refresh) {
        viewModel.updatePullToRefreshState(pagingItems.loadState.refresh)
    }
}

@Composable
fun MoviesContent(
    pagingItems: LazyPagingItems<MovieUiItem>,
    onFavoriteChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.itemKey() },
            contentType = pagingItems.itemContentType { it.itemContentType() }
        ) { index ->
            when (val movieUiItem = pagingItems[index]) {
                is MovieUiItem.DateSeparatorItem ->
                    DateSeparator(
                        data = movieUiItem,
                        modifier = Modifier.padding(top = if (index != 0) 8.dp else 0.dp)
                    )

                is MovieUiItem.Movie ->
                    MovieCard(
                        movie = movieUiItem,
                        onItemClick = { /* TODO */ },
                        onFavoriteChange = onFavoriteChange,
                    )

                null -> {}
            }
        }
    }
}

@Preview
@Composable
private fun MoviesContentPreview() {
    val releaseDates = List(12) {
        LocalDate(2024, it + 1, it + 1)
    }
    val pagingDataFlow = MutableStateFlow(
        PagingData.from(
            List(10) {
                Movie(
                    id = it,
                    title = "Sonic the Hedgehog 3",
                    overview = "Sonic, Knuckles, and Tails reunite against a powerful new adversary, Shadow, a mysterious villain with powers unlike anything they have faced before. With their abilities outmatched in every way, Team Sonic must seek out an unlikely alliance in hopes of stopping Shadow and protecting the planet.",
                    voteAverage = "4.5",
                    posterPath = "",
                    releaseDate = releaseDates[(it / 2) % releaseDates.size],
                    favorite = false
                )
            }
        ).map(Movie::asUiData)
            .insertSeparators { beforeMovie, afterMovie ->
                when {
                    afterMovie == null -> null
                    beforeMovie?.monthAndYearRelease != afterMovie.monthAndYearRelease ->
                        MovieUiItem.DateSeparatorItem(afterMovie.monthAndYearRelease)

                    else -> null
                }
            }
    )
    ScreenPreview {
        MoviesContent(
            pagingItems = pagingDataFlow.collectAsLazyPagingItems(),
            onFavoriteChange = { _, _ -> }
        )
    }
}
