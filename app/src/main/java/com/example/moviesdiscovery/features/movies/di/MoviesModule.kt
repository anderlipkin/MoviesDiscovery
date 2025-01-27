package com.example.moviesdiscovery.features.movies.di

import androidx.room.Room
import com.example.moviesdiscovery.core.data.di.coreDataModule
import com.example.moviesdiscovery.core.network.di.coreNetworkModule
import com.example.moviesdiscovery.features.movies.data.FavoriteMoviesRepository
import com.example.moviesdiscovery.features.movies.data.MovieRepository
import com.example.moviesdiscovery.features.movies.data.database.MovieDatabase
import com.example.moviesdiscovery.features.movies.data.database.dao.FavoriteMovieDao
import com.example.moviesdiscovery.features.movies.data.database.dao.MovieDao
import com.example.moviesdiscovery.features.movies.data.remote.MovieApiService
import com.example.moviesdiscovery.features.movies.data.remote.MoviePagingDataSource
import com.example.moviesdiscovery.features.movies.ui.favorite.FavoriteMoviesViewModel
import com.example.moviesdiscovery.features.movies.ui.list.MoviesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val databaseModule = module {
    single<MovieDatabase> {
        Room.databaseBuilder(
            context = androidContext(),
            klass = MovieDatabase::class.java,
            name = "movie-database",
        ).fallbackToDestructiveMigration().build()
    }
    single<MovieDao> {
        get<MovieDatabase>().movieDao()
    }
    single<FavoriteMovieDao> {
        get<MovieDatabase>().favoriteMovieDao()
    }
}

private val dataModule = module {
    includes(databaseModule, coreNetworkModule, coreDataModule)
    singleOf(::MovieApiService)
    singleOf(::MovieRepository)
    singleOf(::FavoriteMoviesRepository)
    singleOf(::MoviePagingDataSource)
}

val moviesFeatureModule = module {
    includes(dataModule)
    viewModelOf(::MoviesViewModel)
    viewModelOf(::FavoriteMoviesViewModel)
}
