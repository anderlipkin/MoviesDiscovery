package com.example.moviesdiscovery.features.movies.data

import com.example.moviesdiscovery.core.network.model.ApiErrorCodes
import com.example.moviesdiscovery.core.network.model.ApiErrorException
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.FavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.asDomain
import com.example.moviesdiscovery.features.movies.data.database.entity.asFavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.remote.MovieApiService
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy.SortOrder
import com.example.moviesdiscovery.features.movies.domain.asComparator
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val SYNC_BATCH_SIZE = 5

class FavoriteMoviesRepository(
    private val movieApi: MovieApiService,
    private val movieDao: MovieDao,
    private val favoriteMovieDao: FavoriteMovieDao
) {

    private val defaultSortBy = listOf(
        MovieSortBy.PrimaryReleaseDate(SortOrder.Desc),
        MovieSortBy.VoteAverage(SortOrder.Desc)
    )

    suspend fun syncData(): Boolean =
        coroutineScope {
            favoriteMovieDao.getMovieIds()
                .ifEmpty { return@coroutineScope true }
                .chunked(SYNC_BATCH_SIZE)
                .forEach { chunkedIds ->
                    val allFavoriteMoviesResult = chunkedIds.map { movieId ->
                        async { movieId to movieApi.getMovieById(movieId) }
                    }.awaitAll()

                    val (invalidFavoriteMovies, favoriteMoviesResult) =
                        allFavoriteMoviesResult.partition { (_, apiResult) ->
                            (apiResult.exceptionOrNull() as? ApiErrorException)?.code
                                ?.let { it == ApiErrorCodes.InvalidId.value || it == ApiErrorCodes.ResourceNotFound.value }
                                ?: false
                        }
                    if (favoriteMoviesResult.any { (_, apiResult) -> apiResult.isFailure }) return@coroutineScope false

                    val deletedFavoriteMovieIds = invalidFavoriteMovies
                        .map { (movieId, _) -> movieId }
                        .toSet()
                    favoriteMovieDao.deleteMovieByIds(deletedFavoriteMovieIds)
                    val updatedFavoriteMovies = favoriteMoviesResult
                        .map { (_, apiResult) ->
                            apiResult.getOrThrow().asFavoriteMovieEntity()
                        }
                    favoriteMovieDao.insert(updatedFavoriteMovies)
                }
            true
        }

    fun getMoviesFlow(): Flow<List<Movie>> {
        return favoriteMovieDao.getMovies()
            .distinctUntilChanged()
            .map { moviesEntity ->
                moviesEntity
                    .map(FavoriteMovieEntity::asDomain)
                    .sortedWith(defaultSortBy.asComparator())
            }
    }

    fun getMovieIdsFlow(): Flow<List<Int>> =
        favoriteMovieDao.getMovieIdsFlow().distinctUntilChanged()

    suspend fun updateFavoriteMovie(id: Int, isFavorite: Boolean) {
        if (isFavorite) {
            val movie = movieDao.getMovieById(id)
            movie?.let { favoriteMovie ->
                favoriteMovieDao.insert(listOf(favoriteMovie.asFavoriteMovieEntity()))
            }
        } else {
            favoriteMovieDao.deleteMovieById(id)
        }
    }

}
