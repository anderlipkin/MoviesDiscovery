package com.example.moviesdiscovery.core.data.paging

import com.example.moviesdiscovery.core.data.paging.PagingLoadState.NotLoading.Companion.Complete
import com.example.moviesdiscovery.core.data.paging.PagingLoadState.NotLoading.Companion.Incomplete
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job

class PagingDataFetcher<K : PageContext, T>(
    pageConfig: PagingConfig,
    private val initialKey: K,
    private val pagingSource: PagingDataSource<K, T>
) {

    private val _pageContext = MutableStateFlow(initialKey)

    private val _loadStates = MutableStateFlow(PagingLoadStates.IDLE)
    val loadStates: StateFlow<PagingLoadStates> = _loadStates

    private val _items = MutableStateFlow(emptyList<T>())
    val items: StateFlow<List<T>> = _items

    private val _pagingConfig = MutableStateFlow(pageConfig)
    val pagingConfig: StateFlow<PagingConfig> = _pagingConfig

    private var fetchJob: Job? = null

    suspend fun refresh(): Unit = coroutineScope {
        if (loadStates.value.refresh == PagingLoadState.Loading) return@coroutineScope
        fetchJob?.cancelAndJoin()
        fetchJob = coroutineContext.job

        _pageContext.update { initialKey }
        _loadStates.update {
            it.copy(
                refresh = PagingLoadState.Loading,
                append = Incomplete
            )
        }

        val pageConfig = pagingConfig.value
        val result = pagingSource.load(
            params = PagingDataSource.LoadParams(
                key = _pageContext.value,
                initialKey = initialKey,
                loadSize = pageConfig.pageSize
            )
        )

        when (result) {
            is PagingDataSource.LoadResult.Page -> {
                val refreshLoadState =
                    if (result.data.size < pageConfig.pageSize) Complete else Incomplete
                updateStates(
                    pageContext = result.nextKey,
                    loadStates = loadStates.value.copy(refresh = refreshLoadState),
                    items = result.data,
                )
            }

            is PagingDataSource.LoadResult.Error -> {
                updateStates(
                    items = emptyList(),
                    loadStates = loadStates.value.copy(
                        refresh = PagingLoadState.Error(result.throwable)
                    )
                )
            }
        }
    }

    suspend fun loadNextPage(): Unit = coroutineScope {
        if (loadStates.value.refresh == PagingLoadState.Loading ||
            loadStates.value.append == PagingLoadState.Loading
        ) return@coroutineScope

        fetchJob?.join()
        fetchJob = coroutineContext.job

        _loadStates.update {
            it.copy(append = PagingLoadState.Loading)
        }
        val loadResult = pagingSource.load(
            params = PagingDataSource.LoadParams(
                key = _pageContext.value,
                initialKey = initialKey,
                loadSize = pagingConfig.value.pageSize
            )
        )

        when (loadResult) {
            is PagingDataSource.LoadResult.Page -> {
                val loadState =
                    if (loadResult.data.size < pagingConfig.value.pageSize) Complete else Incomplete
                updateStates(
                    items = items.value + loadResult.data,
                    pageContext = loadResult.nextKey,
                    loadStates = loadStates.value.copy(append = loadState)
                )
            }

            is PagingDataSource.LoadResult.Error -> {
                updateStates(
                    loadStates = loadStates.value.copy(
                        append = PagingLoadState.Error(loadResult.throwable)
                    )
                )
            }
        }
    }

    private fun updateStates(
        pageContext: K? = null,
        loadStates: PagingLoadStates? = null,
        items: List<T>? = null
    ) {
        items?.let { _items.value = it }
        pageContext?.let { _pageContext.value = it }
        loadStates?.let { _loadStates.value = it }
    }

}

/*data class PagingState<K : PageContext, T>(
    val items: List<T>,
    val pageContext: K,
    val pageConfig: PagingConfig,
    val loadStates: PagingLoadStates
)*/

