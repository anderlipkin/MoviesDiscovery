package com.example.moviesdiscovery.features.movies.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.asRoomRawQuery
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieRemoteKeyDao
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieRemoteKeyEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.asEntity
import com.example.moviesdiscovery.features.movies.data.dto.MovieDto
import com.example.moviesdiscovery.features.movies.data.dto.MovieResponseDto
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import io.ktor.http.formUrlEncode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

private const val INITIAL_PAGE = 1

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val movieDatabase: MovieDatabase,
    private val movieApi: MovieApiService,
    private val query: MovieQuery
) : RemoteMediator<Int, MovieEntity>() {

    private val moviesDao: MovieDao
        get() = movieDatabase.movieDao()

    private val favoriteMovieDao: FavoriteMovieDao
        get() = movieDatabase.favoriteMovieDao()

    private val remoteKeyDao: MovieRemoteKeyDao
        get() = movieDatabase.remoteKeyDao()

    private val isInitialized = MutableStateFlow(false)

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val nextPage = when (loadType) {
            LoadType.REFRESH -> INITIAL_PAGE
            LoadType.PREPEND ->
                return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val remoteKey = movieDatabase.withTransaction {
                    remoteKeyDao.getRemoteKeyByQuery(query.toApiParameters().formUrlEncode())
                }
                remoteKey.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        val moviesApiResult = movieApi.getDiscoverMovies(page = nextPage, query = query)

        movieDatabase.withTransaction {
            val queryRaw = query.toApiParameters().formUrlEncode()
            if (loadType == LoadType.REFRESH) {
                moviesApiResult
                    .onSuccess {
                        moviesDao.clearAll()
                        remoteKeyDao.deleteByQuery(queryRaw)
                    }
                    .onFailure {
                        if (isInitialized.getAndUpdate { true }) {
                            // show Error on UI
                        } else {
                            val latestMovies =
                                moviesDao.getMoviesByQuery(query.asRoomRawQuery()).take(10)
                            moviesDao.clearAll()
                            moviesDao.insertAll(latestMovies)
                        }
                    }
            }
            moviesApiResult.onSuccess { movieResponseDto ->
                val nextRemoteKey = MovieRemoteKeyEntity(
                    movieQuery = queryRaw,
                    nextPage = if (movieResponseDto.results.isEmpty()) null else nextPage + 1
                )
                remoteKeyDao.insert(nextRemoteKey)
                insertMovies(movieResponseDto.results)
            }
        }
        return moviesApiResult.asMediatorResult()
    }

    private suspend fun insertMovies(remoteMovies: List<MovieDto>) {
        val favoriteMovieIds = favoriteMovieDao.getMovieIds().toSet()
        val moviesEntity = remoteMovies.map { remoteMovie ->
            remoteMovie.asEntity(favorite = remoteMovie.id in favoriteMovieIds)
        }
        moviesDao.insertAll(moviesEntity)
    }

    private fun Result<MovieResponseDto>.asMediatorResult(): MediatorResult =
        if (isSuccess) {
            MediatorResult.Success(
                endOfPaginationReached = getOrNull()!!.results.isEmpty()
            )
        } else {
            MediatorResult.Error(exceptionOrNull()!!)
        }

}
