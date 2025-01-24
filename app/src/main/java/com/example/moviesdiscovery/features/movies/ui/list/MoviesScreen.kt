package com.example.moviesdiscovery.features.movies.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.ui.ScreenPreview
import com.example.moviesdiscovery.core.ui.component.ButtonIconEnd
import com.example.moviesdiscovery.core.ui.component.ErrorState
import com.example.moviesdiscovery.core.ui.effect.collectAsEffect
import com.example.moviesdiscovery.core.ui.util.showToast
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.ui.component.DateSeparator
import com.example.moviesdiscovery.features.movies.ui.component.MovieCard
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import com.example.moviesdiscovery.features.movies.ui.model.itemContentType
import com.example.moviesdiscovery.features.movies.ui.model.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = koinViewModel()
) {
    val uiData by viewModel.uiData.collectAsStateWithLifecycle()
    val cachedMovies by viewModel.cachedMovies.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val uiState = uiData.state

    fun refresh() {
        if (isOnline) {
            pagingItems.refresh()
        } else {
            viewModel.showNoInternetToast()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiData.isRefreshingPull,
        onRefresh = {
            viewModel.onPullToRefresh()
            pagingItems.refresh()
        },
        modifier = modifier
    ) {
        when {
            uiData.isLoadingOnFullScreen -> LoadingScreen()
            uiState is MoviesUiState.OfflineCachedContent ->
                MoviesCachedStateContent(
                    cachedMovies = cachedMovies,
                    onItemClick = viewModel::onItemClick,
                    onFavoriteChange = viewModel::onFavoriteChange,
                    onRetryClick = ::refresh
                )

            uiState is MoviesUiState.PagingContent -> {
                MoviesPaginationStateContent(
                    pagingItems = pagingItems,
                    onItemClick = viewModel::onItemClick,
                    onFavoriteChange = viewModel::onFavoriteChange,
                    onRetryClick = ::refresh,
                    onAppendRetryClick = {
                        if (isOnline) {
                            pagingItems.retry()
                        } else {
                            viewModel.showNoInternetToast()
                        }
                    }
                )
            }
        }
    }
    MovieUiEffect(viewModel.uiEvents)
    LaunchedEffect(isOnline) {
        viewModel.onNetworkOnlineChanged(isOnline)
    }
    LaunchedEffect(pagingItems.loadState) {
        viewModel.onLoadStateUpdate(pagingItems.loadState)
    }
}

@Composable
private fun MoviesCachedStateContent(
    cachedMovies: List<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit
) {
    when {
        cachedMovies.isEmpty() -> NoInternetState(onRetryClick = onRetryClick)
        else -> {
            MoviesCachedContent(
                movieItems = cachedMovies,
                onItemClick = onItemClick,
                onFavoriteChange = onFavoriteChange,
            )
        }
    }
}

@Composable
private fun MoviesPaginationStateContent(
    pagingItems: LazyPagingItems<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    onAppendRetryClick: () -> Unit
) {
    when {
        pagingItems.itemCount == 0 -> {
            EmptyState(
                hasError = pagingItems.loadState.hasError,
                onRetryClick = onRetryClick,
            )
        }

        else -> {
            MoviesPaginationContent(
                pagingItems = pagingItems,
                onItemClick = onItemClick,
                onFavoriteChange = onFavoriteChange,
                onAppendRetryClick = onAppendRetryClick
            )
        }
    }
}

@Composable
private fun NoInternetState(onRetryClick: () -> Unit) {
    ErrorState(
        title = stringResource(R.string.error_no_internet_connection),
        subTitle = stringResource(R.string.error_no_internet_connection_description),
        onRetryClick = onRetryClick,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}

@Composable
private fun EmptyState(hasError: Boolean, onRetryClick: () -> Unit) {
    val modifier = Modifier
        .fillMaxSize()
        .wrapContentSize()
    if (hasError) {
        ErrorState(
            title = stringResource(R.string.error_something_went_wrong),
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    } else {
        ErrorState(
            title = stringResource(R.string.error_no_found_movies_title),
            subTitle = stringResource(R.string.try_again_later),
            onRetryClick = onRetryClick,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MoviesPaginationContent(
    pagingItems: LazyPagingItems<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onAppendRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        movieItems(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.itemKey() },
            contentType = pagingItems.itemContentType { it.itemContentType() },
            item = { index -> pagingItems[index] },
            onItemClick = onItemClick,
            onFavoriteChange = onFavoriteChange
        )
        paginationFooterItem(
            loadState = pagingItems.loadState,
            progress = { PagingProgressIndicator() },
            error = { PaginationAppendErrorState(onAppendRetryClick) }
        )
    }
}

@Composable
private fun PaginationAppendErrorState(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.error_something_went_wrong))
        RetryButton(onClick = onRetryClick)
    }
}

@Composable
fun MoviesCachedContent(
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
        movieItems(
            count = movieItems.size,
            key = { movieItems[it].itemKey() },
            contentType = { movieItems[it].itemContentType() },
            item = { movieItems[it] },
            onItemClick = onItemClick,
            onFavoriteChange = onFavoriteChange
        )
    }
}

private fun LazyListScope.movieItems(
    count: Int,
    key: ((index: Int) -> Any)? = null,
    contentType: (index: Int) -> Any? = { null },
    item: (index: Int) -> MovieUiItem? = { null },
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit
) {
    items(
        count = count,
        key = key,
        contentType = contentType
    ) { index ->
        when (val movieUiItem = item(index)) {
            is MovieUiItem.Movie ->
                MovieCard(
                    movieInitial = movieUiItem,
                    onItemClick = { onItemClick.invoke(movieUiItem.id) },
                    onFavoriteChange = onFavoriteChange,
                )

            is MovieUiItem.DateSeparatorItem ->
                DateSeparator(
                    data = movieUiItem,
                    modifier = Modifier.padding(top = if (index != 0) 8.dp else 0.dp)
                )

            null -> {}
        }
    }
}

fun LazyListScope.paginationFooterItem(
    loadState: CombinedLoadStates,
    progress: @Composable LazyItemScope.() -> Unit,
    error: @Composable LazyItemScope.() -> Unit
) {
    when (loadState.append) {
        is LoadState.Loading -> item { progress.invoke(this) }
        is LoadState.Error -> item { error.invoke(this) }
        is LoadState.NotLoading -> {}
    }
}

@Composable
fun RetryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ButtonIconEnd(
        icon = rememberVectorPainter(Icons.Default.Refresh),
        iconContentDescription = null,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(stringResource(R.string.retry))
    }
}

@Composable
private fun PagingProgressIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MovieUiEffect(uiEvents: Flow<MoviesUiEvent>) {
    val context = LocalContext.current
    val noInternetMessage = stringResource(R.string.error_no_internet_connection)
    uiEvents.collectAsEffect { event ->
        when (event) {
            is MoviesUiEvent.ShowToast -> showToast(event.message, context)
            MoviesUiEvent.NoInternetToast -> showToast(noInternetMessage, context)
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
            List(3) {
                Movie(
                    id = it,
                    title = "Sonic the Hedgehog 3",
                    overview = "Sonic, Knuckles, and Tails reunite against a powerful new adversary, Shadow, a mysterious villain with powers unlike anything they have faced before. With their abilities outmatched in every way, Team Sonic must seek out an unlikely alliance in hopes of stopping Shadow and protecting the planet.",
                    voteAverage = 4.5f,
                    posterPath = "",
                    releaseDate = releaseDates[(it / 2) % releaseDates.size],
                    favorite = false
                )
            },
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            )
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
        MoviesPaginationContent(
            pagingItems = pagingDataFlow.collectAsLazyPagingItems(),
            onFavoriteChange = { _, _ -> },
            onItemClick = {},
            onAppendRetryClick = {}
        )
    }
}
