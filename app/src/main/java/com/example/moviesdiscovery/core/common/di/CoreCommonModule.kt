package com.example.moviesdiscovery.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

enum class CoroutineScopeQualifiers {
    ApplicationScope
}

enum class DispatchersQualifiers {
    Default, IO
}

val coreCommonModule = module {
    single<CoroutineDispatcher>(named(DispatchersQualifiers.IO)) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(named(DispatchersQualifiers.Default)) {
        Dispatchers.Default
    }
    single<CoroutineScope>(named(CoroutineScopeQualifiers.ApplicationScope)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(DispatchersQualifiers.Default)))
    }
}
