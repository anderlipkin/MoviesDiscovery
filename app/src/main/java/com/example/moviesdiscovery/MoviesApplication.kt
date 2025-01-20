package com.example.moviesdiscovery

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.example.moviesdiscovery.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MoviesApplication : Application(), SingletonImageLoader.Factory {

    private val imageLoader: ImageLoader by inject<ImageLoader>(mode = LazyThreadSafetyMode.NONE)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MoviesApplication)
            modules(appModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader

}
