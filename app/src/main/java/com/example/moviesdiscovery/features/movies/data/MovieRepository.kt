package com.example.moviesdiscovery.features.movies.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.asDomain
import com.example.moviesdiscovery.features.movies.data.database.entity.asFavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.remote.MovieApiService
import com.example.moviesdiscovery.features.movies.data.remote.MoviePagingSource
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val NETWORK_PAGE_SIZE = 20
private const val DEFAULT_VOTE_AVERAGE_MIN = 7f
private const val DEFAULT_VOTE_COUNT_MIN = 100

class MovieRepository(
    private val movieDatabase: MovieDatabase,
    private val movieApi: MovieApiService,
    private val movieDao: MovieDao,
    private val favoriteMovieDao: FavoriteMovieDao
) {

    private val defaultQuery = MovieQuery(
        voteAverageMin = DEFAULT_VOTE_AVERAGE_MIN,
        voteCountMin = DEFAULT_VOTE_COUNT_MIN,
        sortByList = listOf(
            MovieSortBy.PrimaryReleaseDate(SortOrder.Desc),
            MovieSortBy.VoteAverage(SortOrder.Desc)
        )
    )

    fun getMovieNetworkPagingFlow(): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                prefetchDistance = NETWORK_PAGE_SIZE / 4,
                initialLoadSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApi = movieApi,
                    movieDatabase = movieDatabase,
                    query = defaultQuery
                )
            }
        ).flow.map { pagingData -> pagingData }

    fun getMoviePagingFlow(limit: Int? = null): Flow<List<Movie>> {
        val favoriteIdsFlow = getFavoriteMovieIdsFlow().map { it.toSet() }
        return movieDao.getMoviesByQueryFlow()
            .map { moviesEntity ->
                moviesEntity
                    .asSequence()
                    .map(MovieEntity::asDomain)
                    .sortMovies(defaultQuery.sortByList)
                    .let { if (limit != null) it.take(limit) else it }
                    .toList()
            }
            .distinctUntilChanged()
            .combine(favoriteIdsFlow) { movies, favoriteIds ->
                val moviesMap = movies.associateBy { it.id }.toMutableMap()
                favoriteIds.forEach { favoriteId ->
                    moviesMap[favoriteId]?.let { movie ->
                        moviesMap[favoriteId] = movie.copy(favorite = true)
                    }
                }
                moviesMap.values.toList()
            }
    }

    fun getMovieByIdFlow(id: Int): Flow<Movie?> {
        val favoriteIdsFlow = getFavoriteMovieIdsFlow().map { it.toSet() }
        return movieDao.getMovieByIdFlow(id).map { it?.asDomain() }
            .combine(favoriteIdsFlow) { movie, favoriteIds ->
                movie ?: return@combine null
                if (movie.id in favoriteIds) {
                    movie.copy(favorite = true)
                } else {
                    movie
                }
            }
    }

    private fun getFavoriteMovieIdsFlow(): Flow<List<Int>> =
        favoriteMovieDao.getMovieIdsFlow().distinctUntilChanged()

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

    private fun Sequence<Movie>.sortMovies(sortList: List<MovieSortBy>): Sequence<Movie> {
        if (sortList.isEmpty()) return this

        val comparator = sortList.map { sortBy ->
            when (sortBy) {
                is MovieSortBy.PrimaryReleaseDate -> {
                    when (sortBy.sortOrder) {
                        SortOrder.Asc -> compareBy<Movie> { it.releaseDate }
                        SortOrder.Desc -> compareByDescending<Movie> { it.releaseDate }
                    }
                }

                is MovieSortBy.VoteAverage -> {
                    when (sortBy.sortOrder) {
                        SortOrder.Asc -> compareBy<Movie> { it.voteAverage }
                        SortOrder.Desc -> compareByDescending<Movie> { it.voteAverage }
                    }
                }
            }
        }.reduce { acc, comparator ->
            acc.then(comparator)
        }
        return sortedWith(comparator)
    }

}
