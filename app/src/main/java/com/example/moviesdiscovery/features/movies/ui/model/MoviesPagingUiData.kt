package com.example.moviesdiscovery.features.movies.ui.model

import androidx.compose.runtime.Immutable
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.data.paging.PagingLoadState
import com.example.moviesdiscovery.core.data.paging.PagingLoadStates
import com.example.moviesdiscovery.core.ui.model.UiStringValue

@Immutable
data class MoviesPagingUiData(
    val items: List<MovieUiItem>,
    val loadStates: PagingLoadUiStates,
    val prefetchDistance: Int
) {
    val appendPrefetchEnabled =
        !loadStates.endReached && loadStates.append.state is PagingLoadState.NotLoading
}

data class PagingLoadUiStates(
    val refresh: PagingLoadUiState,
    val append: PagingLoadUiState
) {
    val endReached = refresh.state.endReached || append.state.endReached
}

data class PagingLoadUiState(
    val state: PagingLoadState,
    val errorMessage: UiStringValue? = null
)

fun PagingLoadStates.asUiStates(isOnline: Boolean): PagingLoadUiStates {
    val refreshErrorMessage = (refresh as? PagingLoadState.Error)?.let {
        UiStringValue.StringResource(
            resId = if (isOnline) R.string.error_something_went_wrong else R.string.error_no_internet_connection
        )
    }
    val appendErrorMessage = (append as? PagingLoadState.Error)?.let {
        UiStringValue.StringResource(
            resId = if (isOnline) R.string.error_something_went_wrong else R.string.error_no_internet_connection
        )
    }
    return PagingLoadUiStates(
        refresh = PagingLoadUiState(state = refresh, errorMessage = refreshErrorMessage),
        append = PagingLoadUiState(state = append, errorMessage = appendErrorMessage)
    )
}
