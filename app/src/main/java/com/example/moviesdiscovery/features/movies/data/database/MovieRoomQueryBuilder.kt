package com.example.moviesdiscovery.features.movies.data.database

//fun MovieQuery.asRoomRawQuery(): RoomRawQuery {
//    val query = StringBuilder("SELECT * FROM movies")
//    if (sortByList.isNotEmpty()) {
//        query.append(" ORDER BY")
//        sortByList.forEachIndexed { index, sortBy ->
//            when (sortBy) {
//                is MovieSortBy.PrimaryReleaseDate -> query.append(" release_date")
//                is MovieSortBy.VoteAverage -> query.append(" vote_average")
//            }
//            when (sortBy.sortOrder) {
//                MovieSortBy.SortOrder.Asc -> query.append(" ASC")
//                MovieSortBy.SortOrder.Desc -> query.append(" DESC")
//            }
//            if (sortByList.lastIndex != index) {
//                query.append(",")
//            }
//        }
//    }
//    return RoomRawQuery(query.toString())
//}
