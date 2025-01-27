package com.example.moviesdiscovery.features.movies.ui.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem
import com.example.moviesdiscovery.features.movies.ui.model.itemContentType
import com.example.moviesdiscovery.features.movies.ui.model.itemKey

fun LazyListScope.movieItems(
    movieUiItems: List<MovieUiItem>,
    onItemClick: (Int) -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit
) {
    items(
        items = movieUiItems,
        key = { it.itemKey() },
        contentType = { it.itemContentType() }
    ) { movieUiItem ->
        when (movieUiItem) {
            is MovieUiItem.Movie ->
                MovieCard(
                    movie = movieUiItem,
                    onItemClick = { onItemClick.invoke(movieUiItem.id) },
                    onFavoriteChange = onFavoriteChange,
                    modifier = Modifier.animateItem()
                )

            is MovieUiItem.DateSeparatorItem ->
                DateSeparator(
                    data = movieUiItem,
//                        modifier = Modifier.padding(top = if (index != 0) 8.dp else 0.dp)
                )
        }
    }
}
