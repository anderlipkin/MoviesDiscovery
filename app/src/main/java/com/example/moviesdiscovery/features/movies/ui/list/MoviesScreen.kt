package com.example.moviesdiscovery.features.movies.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.moviesdiscovery.core.ui.component.ErrorWithRetryButton
import com.example.moviesdiscovery.core.ui.component.LoadingScreen
import com.example.moviesdiscovery.core.ui.component.RetryButton
import com.example.moviesdiscovery.core.ui.effect.collectAsEffect
import com.example.moviesdiscovery.core.ui.model.LazyListScrollPosition
import com.example.moviesdiscovery.core.ui.model.UiStringValue
import com.example.moviesdiscovery.core.ui.util.showToast
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.ui.component.MoviesContent
import com.example.moviesdiscovery.features.movies.ui.component.movieItems
import com.example.moviesdiscovery.features.movies.ui.component.onPrefetchDistanceReached
import com.example.moviesdiscovery.features.movies.ui.component.saveScrollPositionOnDispose
import com.example.moviesdiscovery.features.movies.ui.model.MoviesPagingUiData
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiState
import com.example.moviesdiscovery.features.movies.ui.model.PagingLoadUiStates
import com.example.moviesdiscovery.features.movies.ui.model.asUiData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = koinViewModel()
) {
    val uiData by viewModel.uiData.collectAsStateWithLifecycle()
    val listUiState by viewModel.listUiState.collectAsStateWithLifecycle()
    viewModel.isOnlineFlow.collectAsStateWithLifecycle()

    if (uiData.isFullScreenLoading) {
        LoadingScreen()
    } else {
        PullToRefreshBox(
            isRefreshing = uiData.isRefreshingPull,
            onRefresh = viewModel::onPullToRefresh,
            modifier = modifier
        ) {
            when (val listUiState = listUiState) {
                is MoviesListUiState.OfflineCached ->
                    MoviesCachedContent(
                        listUiState = listUiState,
                        onItemClick = viewModel::onItemClick,
                        onFavoriteChange = viewModel::onFavoriteChange,
                        onRetryClick = viewModel::refresh,
                        onScrollPositionSave = viewModel::saveScrollPosition
                    )

                is MoviesListUiState.Paging ->
                    MoviesPaginationContent(
                        pagingState = listUiState,
                        onItemClick = viewModel::onItemClick,
                        onFavoriteChange = viewModel::onFavoriteChange,
                        onRetryClick = viewModel::refresh,
                        onAppendRetryClick = viewModel::onAppendRetryClick,
                        onLoadNextPage = viewModel::onLoadNextPage,
                        onScrollPositionSave = viewModel::saveScrollPosition
                    )
            }
        }
    }
    MoviesUiEffect(viewModel.uiEvents)
}

@Composable
private fun MoviesCachedContent(
    listUiState: MoviesListUiState.OfflineCached,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    onScrollPositionSave: (LazyListScrollPosition) -> Unit
) {
    if (listUiState.movies.isEmpty()) {
        NoInternetState(onRetryClick = onRetryClick)
    } else {
        MoviesContent(
            movieItems = listUiState.movies,
            scrollPosition = listUiState.scrollPosition,
            onItemClick = onItemClick,
            onFavoriteChange = onFavoriteChange,
            onScrollPositionSave = onScrollPositionSave
        )
    }
}

@Composable
private fun MoviesPaginationContent(
    pagingState: MoviesListUiState.Paging,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onRetryClick: () -> Unit,
    onAppendRetryClick: () -> Unit,
    onLoadNextPage: () -> Unit,
    onScrollPositionSave: (LazyListScrollPosition) -> Unit
) {
    if (pagingState.data.items.isEmpty()) {
        val errorMessage = pagingState.data.loadStates.refresh.errorMessage
        if (errorMessage != null) {
            ErrorState(errorMessage, onRetryClick)
        } else {
            EmptyState(onRetryClick = onRetryClick)
        }
    } else {
        MoviesPaginationListContent(
            pagingState = pagingState,
            onItemClick = onItemClick,
            onFavoriteChange = onFavoriteChange,
            onAppendRetryClick = onAppendRetryClick,
            onLoadNextPage = onLoadNextPage,
            onScrollPositionSave = onScrollPositionSave
        )
    }
}

@Composable
private fun ErrorState(
    errorMessage: UiStringValue,
    onRetryClick: () -> Unit
) {
    ErrorWithRetryButton(
        title = errorMessage.asString(),
        onRetryClick = onRetryClick,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}

@Composable
private fun MoviesPaginationListContent(
    pagingState: MoviesListUiState.Paging,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    onAppendRetryClick: () -> Unit,
    onLoadNextPage: () -> Unit,
    onScrollPositionSave: (LazyListScrollPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagingData = pagingState.data
    val appendLoadState = pagingData.loadStates.append
    val scrollPosition = pagingState.scrollPosition
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollPosition.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = scrollPosition.firstVisibleItemScrollOffset
    )
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        movieItems(pagingData.items, onItemClick, onFavoriteChange)
        paginationFooterItem(
            appendLoadState = appendLoadState.state,
            progress = { CircularProgressIndicator() },
            error = {
                PaginationAppendErrorState(
                    appendLoadState.errorMessage,
                    onAppendRetryClick
                )
            }
        )
    }

    lazyListState.saveScrollPositionOnDispose(onScrollPositionSave)
    if (pagingData.appendPrefetchEnabled) {
        lazyListState.onPrefetchDistanceReached(pagingData.prefetchDistance, onLoadNextPage)
    }
}

@Composable
private fun NoInternetState(onRetryClick: () -> Unit) {
    ErrorWithRetryButton(
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
    ErrorWithRetryButton(
        title = stringResource(R.string.error_no_found_movies_title),
        subTitle = stringResource(R.string.try_again_later),
        onRetryClick = onRetryClick,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}

@Composable
private fun ColumnScope.PaginationAppendErrorState(
    errorMessage: UiStringValue?,
    onRetryClick: () -> Unit
) {
    Text(text = errorMessage?.asString() ?: stringResource(R.string.error_something_went_wrong))
    RetryButton(onClick = onRetryClick)
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
    progress: @Composable (ColumnScope.() -> Unit),
    error: @Composable (ColumnScope.() -> Unit)
) {
    item(contentType = "paginationFooterItem") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
        ) {
            when (appendLoadState) {
                is PagingLoadState.Loading -> progress.invoke(this)
                is PagingLoadState.Error -> error.invoke(this)
                is PagingLoadState.NotLoading -> {}
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Preview
@Composable
private fun MoviesContentPreview() {
    val releaseDates = List(12) { LocalDate(2024, it + 1, it + 1) }
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
    val pagingState = MoviesListUiState.Paging(
        data = MoviesPagingUiData(
            items = movies,
            loadStates = PagingLoadUiStates(
                refresh = PagingLoadUiState(PagingLoadState.NotLoading.Incomplete),
                append = PagingLoadUiState(
                    state = PagingLoadState.Error(IOException()),
                    errorMessage = UiStringValue.StringResource(R.string.error_something_went_wrong)
                )
            ),
            prefetchDistance = 0
        ),
        scrollPosition = LazyListScrollPosition()
    )
    ScreenPreview {
        MoviesPaginationListContent(
            pagingState = pagingState,
            onFavoriteChange = { _, _ -> },
            onItemClick = {},
            onAppendRetryClick = {},
            onLoadNextPage = {},
            onScrollPositionSave = {}
        )
    }
}
