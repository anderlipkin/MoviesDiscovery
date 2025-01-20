package com.example.moviesdiscovery.di

import com.example.moviesdiscovery.features.movies.di.moviesFeatureModule
import org.koin.dsl.module

val appModule = module {
    includes(moviesFeatureModule)
}
