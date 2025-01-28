package com.example.moviesdiscovery.features.movies.ui.favorite

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.ui.component.ErrorWithActionButton
import com.example.moviesdiscovery.core.ui.component.LoadingScreen
import com.example.moviesdiscovery.features.movies.ui.component.MoviesContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoriteMoviesScreen(
    onGoToLibraryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteMoviesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollPosition by viewModel.scrollPosition.collectAsStateWithLifecycle()

    when (val uiState = uiState) {
        FavoriteMoviesUiState.Loading -> LoadingScreen()
        is FavoriteMoviesUiState.Success -> {
            if (uiState.movies.isEmpty()) {
                EmptyState(onGoToLibraryClick = onGoToLibraryClick)
            } else {
                MoviesContent(
                    movieItems = uiState.movies,
                    scrollPosition = scrollPosition,
                    onItemClick = viewModel::onItemClick,
                    onFavoriteChange = viewModel::onFavoriteChange,
                    onScrollPositionSave = viewModel::saveScrollPosition,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onGoToLibraryClick: () -> Unit) {
    ErrorWithActionButton(
        title = stringResource(R.string.error_no_favorites_yet),
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Button(
            onClick = onGoToLibraryClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.go_to_library))
        }
    }
}
