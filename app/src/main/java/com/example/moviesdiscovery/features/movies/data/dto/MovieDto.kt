package com.example.moviesdiscovery.features.movies.data.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponseDto(
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
    val voteAverage: String,
    @SerialName("poster_path")
    val posterPath: String,
    @SerialName("release_date")
    val releaseDate: LocalDate
)
