package com.example.moviesdiscovery.core.data.paging

abstract class PagingDataSource<K : PageContext, T> {

    data class LoadParams<K : PageContext>(
        val key: K,
        val initialKey: K,
        val loadSize: Int
    )

    abstract suspend fun load(params: LoadParams<K>): LoadResult<K, T>

    sealed class LoadResult<K : PageContext, T> {
        data class Page<K : PageContext, T>(
            val data: List<T>,
            val nextKey: K
        ) : LoadResult<K, T>()

        data class Error<K : PageContext, T>(
            val throwable: Throwable
        ) : LoadResult<K, T>()
    }
}
