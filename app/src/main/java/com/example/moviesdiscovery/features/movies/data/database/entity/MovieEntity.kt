package com.example.moviesdiscovery.features.movies.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moviesdiscovery.features.movies.data.dto.MovieDto
import com.example.moviesdiscovery.features.movies.domain.Movie
import kotlinx.datetime.LocalDate

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String,
    @ColumnInfo("vote_average")
    val voteAverage: String,
    @ColumnInfo("poster_path")
    val posterPath: String,
    @ColumnInfo("release_date")
    val releaseDate: LocalDate,
    val favorite: Boolean
)

fun MovieDto.asEntity(favorite: Boolean) =
    MovieEntity(
        id = id,
        title = title,
        overview = overview,
        voteAverage = voteAverage,
        posterPath = posterPath,
        releaseDate = releaseDate,
        favorite = favorite
    )

fun MovieEntity.asDomain() =
    Movie(
        id = id,
        title = title,
        overview = overview,
        voteAverage = voteAverage,
        posterPath = posterPath,
        releaseDate = releaseDate,
        favorite = favorite
    )
