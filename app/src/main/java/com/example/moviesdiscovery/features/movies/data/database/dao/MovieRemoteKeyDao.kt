package com.example.moviesdiscovery.features.movies.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieRemoteKeyEntity

@Dao
interface MovieRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKey: MovieRemoteKeyEntity)

    @Query("SELECT * FROM movie_remote_keys WHERE movie_query = :query")
    suspend fun getRemoteKeyByQuery(query: String): MovieRemoteKeyEntity

    @Query("DELETE FROM movie_remote_keys WHERE movie_query = :query")
    suspend fun deleteByQuery(query: String)
}
