package com.example.moviesdiscovery.features.movies.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem

@Composable
fun DateSeparator(data: MovieUiItem.DateSeparatorItem, modifier: Modifier = Modifier) {
    Text(
        text = data.date,
        modifier = modifier
            .height(32.dp)
            .wrapContentHeight()
    )
}

@Preview(showBackground = true)
@Composable
private fun DateSeparatorPreview() {
    DateSeparator(MovieUiItem.DateSeparatorItem("Jun 2024"), modifier = Modifier.fillMaxWidth())
}
