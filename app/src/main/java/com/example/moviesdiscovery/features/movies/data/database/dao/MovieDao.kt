package com.example.moviesdiscovery.features.movies.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): MovieEntity?

    @RawQuery(observedEntities = [MovieEntity::class])
    fun getMoviesPagingSource(query: RoomRawQuery): PagingSource<Int, MovieEntity>

    @RawQuery
    suspend fun getMoviesByQuery(query: RoomRawQuery): List<MovieEntity>

    @Query("DELETE FROM movies")
    suspend fun clearAll()

}
