package com.example.moviesdiscovery.features.movies.data.dto

import com.example.moviesdiscovery.features.movies.domain.Movie
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesResponseDto(
    @SerialName("results")
    val results: List<MovieDto>
)

@Serializable
data class MovieDto(
    @SerialName("id")
    val id: Int,
    @SerialName("original_title")
    val title: String,
    @SerialName("overview")
    val overview: String,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("poster_path")
    val posterPath: String,
    @SerialName("release_date")
    val releaseDate: LocalDate
)

fun MovieDto.asDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        voteAverage = voteAverage,
        posterPath = posterPath,
        releaseDate = releaseDate,
        favorite = false
    )
