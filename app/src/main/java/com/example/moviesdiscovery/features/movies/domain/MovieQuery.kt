package com.example.moviesdiscovery.features.movies.domain

import com.example.moviesdiscovery.features.movies.domain.MovieSortBy.SortOrder

data class MovieQuery(
    val voteAverageMin: Float? = null,
    val voteCountMin: Int? = null,
    val sortByList: List<MovieSortBy> = emptyList()
)

sealed class MovieSortBy {
    abstract val sortOrder: SortOrder

    data class PrimaryReleaseDate(
        override val sortOrder: SortOrder
    ) : MovieSortBy()

    data class VoteAverage(
        override val sortOrder: SortOrder
    ) : MovieSortBy()

    enum class SortOrder {
        Asc, Desc
    }
}

fun List<MovieSortBy>.asComparator() =
    map { sortBy ->
        when (sortBy) {
            is MovieSortBy.PrimaryReleaseDate -> {
                when (sortBy.sortOrder) {
                    SortOrder.Asc -> compareBy<Movie> { it.releaseDate }
                    SortOrder.Desc -> compareByDescending<Movie> { it.releaseDate }
                }
            }

            is MovieSortBy.VoteAverage -> {
                when (sortBy.sortOrder) {
                    SortOrder.Asc -> compareBy<Movie> { it.voteAverage }
                    SortOrder.Desc -> compareByDescending<Movie> { it.voteAverage }
                }
            }
        }
    }.reduce { acc, comparator ->
        acc.then(comparator)
    }
