package com.example.moviesdiscovery.features.movies.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.asRoomRawQuery
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.asDomain
import com.example.moviesdiscovery.features.movies.data.database.entity.asFavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.remote.MovieApiService
import com.example.moviesdiscovery.features.movies.data.remote.MovieRemoteMediator
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val NETWORK_PAGE_SIZE = 20
private const val DEFAULT_VOTE_AVERAGE_MIN = 7f
private const val DEFAULT_VOTE_COUNT_MIN = 100

@OptIn(ExperimentalPagingApi::class)
class MovieRepository(
    private val movieDatabase: MovieDatabase,
    private val movieApi: MovieApiService,
    private val movieDao: MovieDao,
    private val favoriteMovieDao: FavoriteMovieDao
) {

    private val defaultQuery = MovieQuery(
        voteAverageMin = DEFAULT_VOTE_AVERAGE_MIN,
        voteCountMin = DEFAULT_VOTE_COUNT_MIN,
        sortBy = MovieSortBy.PrimaryReleaseDate(SortOrder.Desc)
    )

    fun getMoviePagingFlow(): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                prefetchDistance = NETWORK_PAGE_SIZE / 4,
                initialLoadSize = NETWORK_PAGE_SIZE / 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                movieDao.getMoviesPagingSource(query = defaultQuery.asRoomRawQuery())
            },
            remoteMediator = MovieRemoteMediator(
                movieDatabase = movieDatabase,
                movieApi = movieApi,
                query = defaultQuery
            )
        ).flow.map { pagingData -> pagingData.map(MovieEntity::asDomain) }

    suspend fun updateFavoriteMovie(id: Int, isFavorite: Boolean) {
        movieDatabase.withTransaction {
            val movie = movieDao.getMovieById(id)
            if (isFavorite) {
                movie?.let { favoriteMovie ->
                    favoriteMovieDao.insert(favoriteMovie.asFavoriteMovieEntity())
                }
            } else {
                favoriteMovieDao.deleteMovieById(id)
            }
            if (movie != null) {
                movieDao.insert(movie.copy(favorite = isFavorite))
            }
        }
    }

}
