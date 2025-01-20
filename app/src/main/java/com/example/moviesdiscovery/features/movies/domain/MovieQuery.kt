package com.example.moviesdiscovery.features.movies.domain

data class MovieQuery(
    val voteAverageMin: Float? = null,
    val voteCountMin: Int? = null,
    val sortBy: MovieSortBy? = null
)

sealed interface MovieSortBy {
    val sortOrder: SortOrder

    data class PrimaryReleaseDate(
        override val sortOrder: SortOrder
    ) : MovieSortBy

    enum class SortOrder {
        Asc, Desc
    }
}
