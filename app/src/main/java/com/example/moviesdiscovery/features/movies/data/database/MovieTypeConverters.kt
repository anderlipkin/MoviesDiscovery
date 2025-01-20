package com.example.moviesdiscovery.features.movies.data.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class MovieTypeConverters {

    @TypeConverter
    fun intToLocalDate(value: Int?): LocalDate? =
        value?.let(LocalDate::fromEpochDays)

    @TypeConverter
    fun localDateToInt(instant: LocalDate?): Int? =
        instant?.toEpochDays()
}
