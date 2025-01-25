package com.example.moviesdiscovery.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.ui.effect.collectAsEffect
import com.example.moviesdiscovery.features.movies.ui.list.MoviesScreen
import org.koin.androidx.compose.KoinAndroidContext

@Composable
fun MoviesDiscoveryApp(appState: MoviesAppState, modifier: Modifier = Modifier) {
    KoinAndroidContext {
        val isOnline by appState.isOnline.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            MoviesScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            )
        }
        appState.snackbarEvent.collectAsEffect {
            snackbarHostState.showSnackbar(it)
        }
        SnackbarNoInternetEffect(isOnline, snackbarHostState)
    }
}

@Composable
private fun SnackbarNoInternetEffect(
    isOnline: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val noInternetMessage = stringResource(R.string.error_no_internet_connection)
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            snackbarHostState.showSnackbar(message = noInternetMessage)
        }
    }
}
