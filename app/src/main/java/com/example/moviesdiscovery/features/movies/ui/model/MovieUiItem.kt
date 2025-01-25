package com.example.moviesdiscovery.features.movies.ui.model

import com.example.moviesdiscovery.features.movies.domain.Movie
import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val IMAGE_W92_URL = "https://image.tmdb.org/t/p/w92"
private const val VOTE_AVERAGE_LENGTH = 3

sealed class MovieUiItem {
    data class Movie(
        val id: Int,
        val title: String,
        val overview: String,
        val voteAverage: String,
        val imageUrl: String,
        val isFavorite: Boolean,
        val monthAndYearRelease: String,
    ) : MovieUiItem()

    data class DateSeparatorItem(val date: String) : MovieUiItem()
}

fun List<Movie>.asUiData(): List<MovieUiItem> =
    map { it.asUiData() }.insertDateSeparators()

fun Movie.asUiData() =
    MovieUiItem.Movie(
        id = id,
        title = title,
        overview = overview,
        voteAverage = voteAverage.toString().take(VOTE_AVERAGE_LENGTH),
        imageUrl = "$IMAGE_W92_URL$posterPath",
        isFavorite = favorite,
        monthAndYearRelease = releaseDate.monthAndYear()
    )

fun List<MovieUiItem.Movie>.insertDateSeparators(): List<MovieUiItem> =
    fold(mutableListOf()) { result, movie ->
        val beforeMovie = result.lastOrNull() as? MovieUiItem.Movie
        if (beforeMovie?.monthAndYearRelease != movie.monthAndYearRelease) {
            result.add(MovieUiItem.DateSeparatorItem(movie.monthAndYearRelease))
        }
        result.add(movie)
        result
    }

fun LocalDate.monthAndYear() =
    "${month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} $year"

fun MovieUiItem.itemKey(): String = when (this) {
    is MovieUiItem.Movie -> "$id"
    is MovieUiItem.DateSeparatorItem -> date
}

fun MovieUiItem.itemContentType(): String = when (this) {
    is MovieUiItem.Movie -> "movie"
    is MovieUiItem.DateSeparatorItem -> "dateSeparator"
}
