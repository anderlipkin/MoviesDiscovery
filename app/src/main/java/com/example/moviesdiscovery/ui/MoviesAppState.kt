package com.example.moviesdiscovery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import com.example.moviesdiscovery.core.ui.SnackbarEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@Composable
fun rememberMoviesAppState(
    networkMonitor: ConnectivityNetworkMonitor,
    snackbarEventBus: SnackbarEventBus,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MoviesAppState {
    return remember(coroutineScope, networkMonitor) {
        MoviesAppState(
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            snackbarEventBus = snackbarEventBus
        )
    }
}

@Stable
class MoviesAppState(
    networkMonitor: ConnectivityNetworkMonitor,
    snackbarEventBus: SnackbarEventBus,
    coroutineScope: CoroutineScope
) {
    val isOnline = networkMonitor.isOnline
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkMonitor.isCurrentlyConnected()
        )
    val snackbarEvent = snackbarEventBus.snackbarEvent
}
