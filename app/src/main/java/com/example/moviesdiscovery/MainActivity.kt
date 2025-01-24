package com.example.moviesdiscovery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moviesdiscovery.core.ui.SnackbarEventBus
import com.example.moviesdiscovery.core.data.util.ConnectivityNetworkMonitor
import com.example.moviesdiscovery.core.ui.theme.MoviesDiscoveryTheme
import com.example.moviesdiscovery.ui.MoviesDiscoveryApp
import com.example.moviesdiscovery.ui.rememberMoviesAppState
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val networkMonitor: ConnectivityNetworkMonitor by inject(mode = LazyThreadSafetyMode.NONE)
    private val snackbarEventBus: SnackbarEventBus by inject(mode = LazyThreadSafetyMode.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberMoviesAppState(networkMonitor, snackbarEventBus)
            MoviesDiscoveryTheme {
                MoviesDiscoveryApp(appState)
            }
        }
    }
}
