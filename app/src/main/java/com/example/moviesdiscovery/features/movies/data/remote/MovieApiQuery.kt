package com.example.moviesdiscovery.features.movies.data.remote

import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy
import io.ktor.http.parameters

private const val VOTE_AVERAGE_GTE_PARAMETER = "vote_average.gte"
private const val VOTE_COUNT_GTE_PARAMETER = "vote_count.gte"
private const val SORT_BY_PARAMETER = "sort_by"
private const val SORT_ORDER_ASC = "asc"
private const val SORT_ORDER_DESC = "desc"
private const val PRIMARY_RELEASE_DATE_SORT_BY = "primary_release_date"
private const val VOTE_AVERAGE_SORT_BY = "vote_average"

fun MovieSortBy.toApiQueryValue(): String {
    val apiSortOrder = when (sortOrder) {
        MovieSortBy.SortOrder.Asc -> SORT_ORDER_ASC
        MovieSortBy.SortOrder.Desc -> SORT_ORDER_DESC
    }
    return when (this) {
        is MovieSortBy.PrimaryReleaseDate ->
            "$PRIMARY_RELEASE_DATE_SORT_BY.$apiSortOrder"

        is MovieSortBy.VoteAverage ->
            "$VOTE_AVERAGE_SORT_BY.$apiSortOrder"
    }
}

fun MovieQuery.toApiParameters() =
    parameters {
        voteAverageMin?.let {
            append(VOTE_AVERAGE_GTE_PARAMETER, it.toString())
        }
        voteCountMin?.let {
            append(VOTE_COUNT_GTE_PARAMETER, it.toString())
        }
        sortByList.firstOrNull()?.let {
            append(SORT_BY_PARAMETER, it.toApiQueryValue())
        }
    }
