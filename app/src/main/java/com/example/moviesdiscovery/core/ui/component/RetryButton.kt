package com.example.moviesdiscovery.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.example.moviesdiscovery.R

@Composable
fun RetryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ButtonIconEnd(
        icon = rememberVectorPainter(Icons.Default.Refresh),
        iconContentDescription = null,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(stringResource(R.string.retry))
    }
}
