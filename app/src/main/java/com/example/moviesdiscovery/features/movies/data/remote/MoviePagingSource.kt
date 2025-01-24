package com.example.moviesdiscovery.features.movies.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.asEntity
import com.example.moviesdiscovery.features.movies.data.dto.asDomain
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.MovieQuery

private const val INITIAL_PAGE = 1

class MoviePagingSource(
    private val movieApi: MovieApiService,
    private val movieDatabase: MovieDatabase,
    private val query: MovieQuery
) : PagingSource<Int, Movie>() {

    private val moviesDao: MovieDao
        get() = movieDatabase.movieDao()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: INITIAL_PAGE
        val moviesApiResult = movieApi.getDiscoverMovies(page = page, query = query)
        val result = when {
            moviesApiResult.isSuccess -> {
                val movies = moviesApiResult.getOrNull()!!.results
                movieDatabase.withTransaction {
                    if (page == INITIAL_PAGE) {
                        moviesDao.clearAll()
                    }
                    moviesDao.insertAll(movies.map { it.asEntity(false) })
                }
                LoadResult.Page(
                    data = movies.map { it.asDomain(false) },
                    prevKey = if (page > INITIAL_PAGE) page - 1 else null,
                    nextKey = if (movies.isNotEmpty()) page + 1 else null
                )
            }

            else -> LoadResult.Error(moviesApiResult.exceptionOrNull()!!)
        }
        return result
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
}
