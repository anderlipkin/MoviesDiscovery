package com.example.moviesdiscovery.features.movies.data.remote

import com.example.moviesdiscovery.core.network.extension.getResult
import com.example.moviesdiscovery.features.movies.data.dto.MovieResponseDto
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

private const val PAGE_PARAMETER = "page"

class MovieApiService(private val client: HttpClient) {

    suspend fun getDiscoverMovies(
        page: Int,
        query: MovieQuery
    ): Result<MovieResponseDto> =
        client.getResult(urlString = "discover/movie") {
            parameter(PAGE_PARAMETER, page)
            url.parameters.appendAll(query.toApiParameters())
        }

}
