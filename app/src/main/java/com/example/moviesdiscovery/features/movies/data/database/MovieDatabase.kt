package com.example.moviesdiscovery.features.movies.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.database.entity.FavoriteMovieEntity
import com.example.moviesdiscovery.features.movies.data.database.entity.MovieEntity

@Database(
    entities = [
        MovieEntity::class,
        FavoriteMovieEntity::class
    ],
    version = 1
)
@TypeConverters(MovieTypeConverters::class)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}
