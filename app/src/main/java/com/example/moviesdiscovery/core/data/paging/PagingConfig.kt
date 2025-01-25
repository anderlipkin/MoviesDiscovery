package com.example.moviesdiscovery.core.data.paging

data class PagingConfig(
    val pageSize: Int,
    val prefetchDistance: Int = pageSize
)
