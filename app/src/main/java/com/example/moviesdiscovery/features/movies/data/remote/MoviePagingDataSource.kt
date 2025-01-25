package com.example.moviesdiscovery.features.movies.data.remote

import androidx.room.withTransaction
import com.example.moviesdiscovery.core.data.paging.PagingDataSource
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.asEntity
import com.example.moviesdiscovery.features.movies.data.dto.asDomain
import com.example.moviesdiscovery.features.movies.domain.Movie

class MoviePagingDataSource(
    private val movieApi: MovieApiService,
    private val movieDatabase: MovieDatabase
) : PagingDataSource<MoviePageContext, Movie>() {

    private val moviesDao: MovieDao
        get() = movieDatabase.movieDao()

    override suspend fun load(params: LoadParams<MoviePageContext>): LoadResult<MoviePageContext, Movie> {
        val pageContext = params.key
        val page = pageContext.page
        val moviesApiResult = movieApi.getDiscoverMovies(page = page, query = pageContext.query)
        val result = when {
            moviesApiResult.isSuccess -> {
                val movies = moviesApiResult.getOrNull()!!.results
                movieDatabase.withTransaction {
                    if (params.key == params.initialKey) {
                        moviesDao.clearAll()
                    }
                    moviesDao.insertAll(movies.map { it.asEntity() })
                }
                LoadResult.Page(
                    data = movies.map { it.asDomain(false) },
                    nextKey = pageContext.copy(page = page + 1)
                )
            }

            else -> LoadResult.Error(moviesApiResult.exceptionOrNull()!!)
        }
        return result
    }

}
