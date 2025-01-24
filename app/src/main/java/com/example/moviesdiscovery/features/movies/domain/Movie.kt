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
