package com.example.moviesdiscovery.features.movies.data.database

import androidx.room.RoomRawQuery
import com.example.moviesdiscovery.features.movies.domain.MovieQuery
import com.example.moviesdiscovery.features.movies.domain.MovieSortBy

fun MovieQuery.asRoomRawQuery(): RoomRawQuery {
    val query = StringBuilder("SELECT * FROM movies")
    if (sortBy != null) {
        query.append(" ORDER BY")
        when (sortBy) {
            is MovieSortBy.PrimaryReleaseDate -> {
                query.append(" release_date")
            }
        }
        when (sortBy.sortOrder) {
            MovieSortBy.SortOrder.Asc -> query.append(" ASC")
            MovieSortBy.SortOrder.Desc -> query.append(" DESC")
        }
    }

    return RoomRawQuery(query.toString())
}
