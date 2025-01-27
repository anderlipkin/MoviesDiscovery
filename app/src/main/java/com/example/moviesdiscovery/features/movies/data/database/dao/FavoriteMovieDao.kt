package com.example.moviesdiscovery.features.movies.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moviesdiscovery.features.movies.data.database.entity.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movies: List<FavoriteMovieEntity>)

    @Query("SELECT * FROM favorite_movies WHERE id = :id")
    fun getMovieById(id: Int): Flow<FavoriteMovieEntity?>

    @Query("SELECT * FROM favorite_movies")
    fun getMovies(): Flow<List<FavoriteMovieEntity>>

    @Query("SELECT id FROM favorite_movies")
    suspend fun getMovieIds(): List<Int>

    @Query("SELECT id FROM favorite_movies")
    fun getMovieIdsFlow(): Flow<List<Int>>

    @Query("DELETE FROM favorite_movies WHERE id = :id")
    suspend fun deleteMovieById(id: Int)

    @Query("DELETE FROM favorite_movies WHERE id in (:ids)")
    suspend fun deleteMovieByIds(ids: Set<Int>)

    @Query("DELETE FROM favorite_movies")
    suspend fun clearAll()
}
