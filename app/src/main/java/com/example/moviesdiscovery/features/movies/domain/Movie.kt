package com.example.moviesdiscovery.features.movies.domain

import kotlinx.datetime.LocalDate

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val voteAverage: Float,
    val posterPath: String,
    val releaseDate: LocalDate,
    val favorite: Boolean
)

fun List<Movie>.mergeFavorites(favoriteIds: Set<Int>): List<Movie> {
    val moviesMap = associateBy { it.id }.toMutableMap()
    favoriteIds.forEach { favoriteId ->
        moviesMap[favoriteId]?.let { movie ->
            moviesMap[favoriteId] = movie.copy(favorite = true)
        }
    }
    return moviesMap.values.toList()
}

fun Sequence<Movie>.sortMovies(sortList: List<MovieSortBy>): Sequence<Movie> {
    if (sortList.isEmpty()) return this
    return sortedWith(sortList.asComparator())
}
