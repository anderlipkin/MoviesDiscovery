package com.example.moviesdiscovery.features.movies.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_remote_keys")
data class MovieRemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo("movie_query")
    val movieQuery: String,
    @ColumnInfo("next_page")
    val nextPage: Int?
)
