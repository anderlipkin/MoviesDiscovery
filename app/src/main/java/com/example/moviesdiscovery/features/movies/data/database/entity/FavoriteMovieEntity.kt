package com.example.moviesdiscovery.features.movies.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String,
    @ColumnInfo("vote_average")
    val voteAverage: Float,
    @ColumnInfo("poster_path")
    val posterPath: String,
    @ColumnInfo("release_date")
    val releaseDate: LocalDate
)

fun MovieEntity.asFavoriteMovieEntity() =
    FavoriteMovieEntity(
        id = id,
        title = title,
        overview = overview,
        voteAverage = voteAverage,
        posterPath = posterPath,
        releaseDate = releaseDate
    )
