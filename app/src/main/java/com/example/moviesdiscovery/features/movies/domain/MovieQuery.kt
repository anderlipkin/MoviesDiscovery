package com.example.moviesdiscovery.features.movies.domain

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
