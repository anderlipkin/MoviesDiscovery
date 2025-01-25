package com.example.moviesdiscovery.core.data.paging

sealed class PagingLoadState(open val endReached: Boolean) {

    data object Loading : PagingLoadState(false) {
        override fun toString(): String = "Loading(endReached=$endReached)"
    }

    data class Error(val error: Throwable) : PagingLoadState(false) {
        override fun toString(): String = "Error(endReached=$endReached, error=$error)"
    }

    data class NotLoading(override val endReached: Boolean) : PagingLoadState(endReached) {
        override fun toString(): String = "NotLoading(endReached=$endReached)"

        internal companion object {
            internal val Complete = NotLoading(endReached = true)
            internal val Incomplete = NotLoading(endReached = false)
        }
    }
}

data class PagingLoadStates(
    val refresh: PagingLoadState,
    val append: PagingLoadState
) {

    val error: Throwable?
        get() = (refresh as? PagingLoadState.Error)?.error
            ?: (append as? PagingLoadState.Error)?.error

    val isIdle = refresh is PagingLoadState.NotLoading && append is PagingLoadState.NotLoading
    val hasError = refresh is PagingLoadState.Error || append is PagingLoadState.Error
    val endReached = refresh.endReached || append.endReached

    internal companion object {
        val IDLE = PagingLoadStates(
            refresh = PagingLoadState.NotLoading.Incomplete,
            append = PagingLoadState.NotLoading.Incomplete
        )
    }
}
