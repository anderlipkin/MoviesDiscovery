package com.example.moviesdiscovery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moviesdiscovery.core.ui.theme.MoviesDiscoveryTheme
import com.example.moviesdiscovery.features.movies.ui.list.MoviesScreen
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoviesDiscoveryTheme {
                MoviesDiscoveryApp()
            }
        }
    }
}

@Composable
fun MoviesDiscoveryApp(modifier: Modifier = Modifier) {
    KoinAndroidContext {
        Scaffold(modifier = modifier) { innerPadding ->
            MoviesScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            )
        }
    }
}
