package com.example.moviesdiscovery.features.movies.data.remote

import com.example.moviesdiscovery.core.network.extension.getResult
import com.example.moviesdiscovery.features.movies.data.dto.MovieDto
import com.example.moviesdiscovery.features.movies.data.dto.MoviesResponseDto
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter

private const val PAGE_PARAMETER = "page"

class MovieApiService(private val client: HttpClient) {

    suspend fun getDiscoverMovies(
        page: Int,
        query: MovieQuery
    ): Result<MoviesResponseDto> =
        client.getResult(urlString = "discover/movie") {
            parameter(PAGE_PARAMETER, page)
            url.parameters.appendAll(query.toApiParameters())
        }

    suspend fun getMovieById(movieId: Int): Result<MovieDto> =
        client.getResult(urlString = "movie/$movieId")

}
