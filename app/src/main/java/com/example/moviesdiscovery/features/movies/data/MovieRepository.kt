package com.example.moviesdiscovery.features.movies.data

import com.example.moviesdiscovery.core.data.paging.PagingConfig
import com.example.moviesdiscovery.core.data.paging.PagingDataFetcher
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.asDomain
import com.example.moviesdiscovery.features.movies.data.database.entity.asFavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.remote.MoviePageContext
import com.example.moviesdiscovery.features.movies.data.remote.MoviePagingDataSource
import com.example.moviesdiscovery.features.movies.domain.Movie
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy.SortOrder
import com.example.moviesdiscovery.features.movies.domain.sortMovies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val INITIAL_NETWORK_PAGE = 1
private const val NETWORK_PAGE_SIZE = 20
private const val DEFAULT_VOTE_AVERAGE_MIN = 7f
private const val DEFAULT_VOTE_COUNT_MIN = 100

class MovieRepository(
    private val movieDao: MovieDao,
    private val favoriteMovieDao: FavoriteMovieDao,
    private val pagingDataSource: MoviePagingDataSource
) {

    private val defaultQuery = MovieQuery(
        voteAverageMin = DEFAULT_VOTE_AVERAGE_MIN,
        voteCountMin = DEFAULT_VOTE_COUNT_MIN,
        sortByList = listOf(
            MovieSortBy.PrimaryReleaseDate(SortOrder.Desc),
            MovieSortBy.VoteAverage(SortOrder.Desc)
        )
    )

    fun getMoviesPagingFetcher(): PagingDataFetcher<MoviePageContext, Movie> =
        PagingDataFetcher(
            pageConfig = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                prefetchDistance = NETWORK_PAGE_SIZE / 4
            ),
            initialKey = MoviePageContext(page = INITIAL_NETWORK_PAGE, query = defaultQuery),
            pagingSource = pagingDataSource
        )

    fun getMoviesByQueryFlow(limit: Int? = null): Flow<List<Movie>> {
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
        return movieDao.getMovieByIdFlow(id).map { it?.asDomain() }
            .combine(favoriteMovieDao.getMovieById(id)) { movie, favoriteMovie ->
                movie?.copy(favorite = favoriteMovie != null) ?: return@combine null
            }
    }

    fun getFavoriteMovieIdsFlow(): Flow<List<Int>> =
        favoriteMovieDao.getMovieIdsFlow().distinctUntilChanged()

    suspend fun updateFavoriteMovie(id: Int, isFavorite: Boolean) {
        if (isFavorite) {
            val movie = movieDao.getMovieById(id)
            movie?.let { favoriteMovie ->
                favoriteMovieDao.insert(favoriteMovie.asFavoriteMovieEntity())
            }
        } else {
            favoriteMovieDao.deleteMovieById(id)
        }
    }

}
