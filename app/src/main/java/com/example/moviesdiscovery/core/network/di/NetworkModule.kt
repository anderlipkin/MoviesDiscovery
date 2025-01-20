package com.example.moviesdiscovery.core.network.di

import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.util.DebugLogger
import com.example.moviesdiscovery.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val API_KEY_PARAM = "api_key"
private const val SERVER_URL = "https://api.themoviedb.org/3/"

val coreNetworkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }
    single<HttpClient>(named("base")) {
        HttpClient(CIO) {
            install(Logging) {
                logger = Logger.ANDROID
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            }
        }
    }
    single<HttpClient> {
        get<HttpClient>(named("base")).config {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            defaultRequest {
                url(SERVER_URL)
                url.parameters.append(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
            }
            // TODO Add Error handling
//            HttpResponseValidator {
//                validateResponse {
//
//                }
//            }
        }
    }
    single<ImageLoader> {
        ImageLoader.Builder(context = androidContext())
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { get<HttpClient>(named("base")) }))
//                add(SvgDecoder.Factory())
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
