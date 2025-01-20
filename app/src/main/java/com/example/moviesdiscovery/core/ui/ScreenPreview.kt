package com.example.moviesdiscovery.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.moviesdiscovery.core.ui.theme.MoviesDiscoveryTheme
import com.example.moviesdiscovery.di.appModule
import org.koin.compose.KoinApplication

@Suppress("ModifierMissing")
@Composable
fun ScreenPreview(
    screen: @Composable () -> Unit
) {
    KoinApplication(application = { modules(appModule) }) {
        MoviesDiscoveryTheme {
            Surface(Modifier.fillMaxSize()) {
                screen()
            }
        }
    }
}

@Composable
fun placeholderInPreview(placeholder: @Composable () -> Painter): Painter? =
    if (LocalInspectionMode.current) placeholder() else null
