package com.example.moviesdiscovery.features.movies.data.remote

import com.example.moviesdiscovery.core.data.paging.PageContext
import com.example.moviesdiscovery.features.movies.domain.MovieQuery

data class MoviePageContext(
    val page: Int,
    val query: MovieQuery
) : PageContext
