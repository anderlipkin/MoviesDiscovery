package com.example.moviesdiscovery.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.ui.theme.MoviesDiscoveryTheme

@Composable
fun ErrorWithActionButton(
    title: String,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    button: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        if (subTitle != null) {
            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        button()
    }
}

@Composable
fun ErrorWithRetryButton(
    title: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
) {
    ErrorWithActionButton(
        title = title,
        modifier = modifier,
        subTitle = subTitle,
    ) {
        RetryButton(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorWithRetryButtonPreview() {
    MoviesDiscoveryTheme {
        ErrorWithRetryButton(
            title = stringResource(R.string.error_something_went_wrong),
            subTitle = stringResource(R.string.try_again_later),
            onRetryClick = {},
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )
    }
}
