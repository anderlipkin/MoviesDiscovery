package com.example.moviesdiscovery.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ButtonIconEnd(
    icon: Painter,
    iconContentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
    text: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 8.dp
        ),
        modifier = modifier
    ) {
        text()
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Icon(
            painter = icon,
            contentDescription = iconContentDescription,
            tint = iconTint,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
    }
}
