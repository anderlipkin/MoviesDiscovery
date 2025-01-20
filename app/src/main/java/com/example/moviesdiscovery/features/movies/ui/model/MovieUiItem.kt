package com.example.moviesdiscovery.features.movies.ui.model

import com.example.moviesdiscovery.features.movies.domain.Movie
import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val IMAGE_W92_URL = "https://image.tmdb.org/t/p/w92"

sealed interface MovieUiItem {
    data class Movie(
        val id: Int,
        val title: String,
        val overview: String,
        val voteAverage: String,
        val imageUrl: String,
        val isFavorite: Boolean,
        val monthAndYearRelease: String
    ) : MovieUiItem

    data class DateSeparatorItem(val date: String) : MovieUiItem
}

fun Movie.asUiData() = MovieUiItem.Movie(
    id = id,
    title = title,
    overview = overview,
    voteAverage = voteAverage,
    imageUrl = "$IMAGE_W92_URL$posterPath",
    isFavorite = favorite,
    monthAndYearRelease = releaseDate.monthAndYear()
)

fun LocalDate.monthAndYear() =
    "${month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} $year"

fun MovieUiItem.itemKey() = when (this) {
    is MovieUiItem.Movie -> id
    is MovieUiItem.DateSeparatorItem -> date
}

fun MovieUiItem.itemContentType() = when (this) {
    is MovieUiItem.Movie -> "movie"
    is MovieUiItem.DateSeparatorItem -> "dateSeparator"
}
