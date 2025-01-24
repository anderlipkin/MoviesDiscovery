package com.example.moviesdiscovery.core.data.di

import com.example.moviesdiscovery.core.common.di.DispatchersQualifiers
import com.example.moviesdiscovery.core.common.di.coreCommonModule
import com.example.moviesdiscovery.core.ui.SnackbarEventBus
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreDataModule = module {
    includes(coreCommonModule)

    single<ConnectivityNetworkMonitor>() {
        ConnectivityNetworkMonitor(
            context = androidContext(),
            ioDispatcher = get<CoroutineDispatcher>(named(DispatchersQualifiers.IO))
        )
    }
    singleOf(::SnackbarEventBus)
}
