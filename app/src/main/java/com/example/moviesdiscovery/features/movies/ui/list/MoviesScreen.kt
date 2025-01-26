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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.data.paging.PagingLoadState
import com.example.moviesdiscovery.core.ui.ScreenPreview
import com.example.moviesdiscovery.core.ui.component.ErrorState
import com.example.moviesdiscovery.core.ui.component.RetryButton
import com.example.moviesdiscovery.core.ui.effect.collectAsEffect
import com.example.moviesdiscovery.core.ui.model.UiStringValue
import com.example.moviesdiscovery.core.ui.util.showToast
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.ui.component.movieItems
import com.example.moviesdiscovery.features.movies.ui.component.onPrefetchDistanceReached
import com.example.moviesdiscovery.features.movies.ui.component.scrollToBottomOnAppendVisible
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiState
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiStates
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = koinViewModel()
) {
    val uiData by viewModel.uiState.collectAsStateWithLifecycle()
    viewModel.isOnlineFlow.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = uiData.isRefreshingPull,
        onRefresh = viewModel::onPullToRefresh,
        modifier = modifier
    ) {
        if (uiData.isLoadingOnFullScreen) {
            LoadingScreen()
        } else {
            when (val pagingState = uiData.contentState) {
                MoviesContentUiState.InitialLoading -> LoadingScreen()
                is MoviesContentUiState.OfflineCached ->
                    MoviesCachedContent(
                        cachedMovies = pagingState.items,
                        onItemClick = viewModel::onItemClick,
                        onFavoriteChange = viewModel::onFavoriteChange,
                        onRetryClick = viewModel::refresh
                    )

                is MoviesContentUiState.Paging -> {
                    MoviesPaginationContent(
                        pagingState = pagingState,
                        onItemClick = viewModel::onItemClick,
                        onFavoriteChange = viewModel::onFavoriteChange,
                        onRetryClick = viewModel::refresh,
                        onAppendRetryClick = viewModel::onAppendRetryClick,
                        onLoadNextPage = viewModel::onLoadNextPage
                    )
                }
            }
        }
    }
    MoviesUiEffect(viewModel.uiEvents)
}

@Composable
fun MoviesCachedContent(
    cachedMovies: List<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit
) {
    when {
        cachedMovies.isEmpty() -> NoInternetState(onRetryClick = onRetryClick)
        else -> {
            MoviesCachedListContent(
                movieItems = cachedMovies,
                onItemClick = onItemClick,
                onFavoriteChange = onFavoriteChange,
            )
        }
    }
}

@Composable
private fun MoviesCachedListContent(
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

@Composable
private fun MoviesPaginationContent(
    pagingState: MoviesContentUiState.Paging,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    onAppendRetryClick: () -> Unit,
    onLoadNextPage: () -> Unit
) {
    when {
        pagingState.items.isEmpty() -> {
            val errorMessage = pagingState.loadStates.refresh.errorMessage
            if (errorMessage != null) {
                ErrorState(
                    title = errorMessage.asString(),
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            } else {
                EmptyState(onRetryClick = onRetryClick)
            }
        }

        else -> {
            MoviesPaginationListContent(
                pagingState = pagingState,
                onItemClick = onItemClick,
                onFavoriteChange = onFavoriteChange,
                onAppendRetryClick = onAppendRetryClick,
                onLoadNextPage = onLoadNextPage
            )
        }
    }
}

@Composable
private fun MoviesPaginationListContent(
    pagingState: MoviesContentUiState.Paging,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onAppendRetryClick: () -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appendLoadState = pagingState.loadStates.append
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        movieItems(pagingState.items, onItemClick, onFavoriteChange)
        paginationFooterItem(
            appendLoadState = appendLoadState.state,
            progress = { PagingProgressIndicator() },
            error = {
                PaginationAppendErrorState(
                    appendLoadState.errorMessage,
                    onAppendRetryClick
                )
            }
        )
    }

    lazyListState.scrollToBottomOnAppendVisible(appendLoadState.state)
    if (pagingState.appendPrefetchEnabled) {
        lazyListState.onPrefetchDistanceReached(pagingState.prefetchDistance, onLoadNextPage)
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
private fun EmptyState(onRetryClick: () -> Unit) {
    ErrorState(
        title = stringResource(R.string.error_no_found_movies_title),
        subTitle = stringResource(R.string.try_again_later),
        onRetryClick = onRetryClick,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    )
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
private fun PaginationAppendErrorState(errorMessage: UiStringValue?, onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
    ) {
        Text(text = errorMessage?.asString() ?: stringResource(R.string.error_something_went_wrong))
        RetryButton(onClick = onRetryClick)
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
private fun MoviesUiEffect(uiEvents: Flow<MoviesUiEvent>) {
    val context = LocalContext.current
    val noInternetMessage = stringResource(R.string.error_no_internet_connection)
    uiEvents.collectAsEffect { event ->
        when (event) {
            is MoviesUiEvent.ShowToast -> showToast(event.message, context)
            MoviesUiEvent.NoInternetToast -> showToast(noInternetMessage, context)
        }
    }
}

private fun LazyListScope.paginationFooterItem(
    appendLoadState: PagingLoadState,
    progress: @Composable LazyItemScope.() -> Unit,
    error: @Composable LazyItemScope.() -> Unit
) {
    item {
        when (appendLoadState) {
            is PagingLoadState.Loading -> progress.invoke(this)
            is PagingLoadState.Error -> error.invoke(this)
            is PagingLoadState.NotLoading -> {}
        }
        // TODO check after complete HomeScreen
//        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Preview
@Composable
private fun MoviesContentPreview() {
    val releaseDates = List(12) {
        LocalDate(2024, it + 1, it + 1)
    }
    val movies = List(3) {
        Movie(
            id = it,
            title = "Sonic the Hedgehog 3",
            overview = "Sonic, Knuckles, and Tails reunite against a powerful new adversary, Shadow, a mysterious villain with powers unlike anything they have faced before. With their abilities outmatched in every way, Team Sonic must seek out an unlikely alliance in hopes of stopping Shadow and protecting the planet.",
            voteAverage = 4.5f,
            posterPath = "",
            releaseDate = releaseDates[(it / 2) % releaseDates.size],
            favorite = false
        )
    }.asUiData()
    val pagingState = MoviesContentUiState.Paging(
        items = movies,
        loadStates = PagingLoadUiStates(
            refresh = PagingLoadUiState(PagingLoadState.NotLoading.Incomplete),
            append = PagingLoadUiState(PagingLoadState.NotLoading.Incomplete)
        ),
        prefetchDistance = 0
    )
    ScreenPreview {
        MoviesPaginationListContent(
            pagingState = pagingState,
            onFavoriteChange = { _, _ -> },
            onItemClick = {},
            onAppendRetryClick = {},
            onLoadNextPage = {}
        )
    }
}
