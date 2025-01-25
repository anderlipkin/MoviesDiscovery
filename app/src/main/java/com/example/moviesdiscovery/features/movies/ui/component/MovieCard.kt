package com.example.moviesdiscovery.features.movies.ui.component

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import com.example.moviesdiscovery.R
import com.example.moviesdiscovery.core.ui.placeholderInPreview
import com.example.moviesdiscovery.core.ui.theme.MoviesDiscoveryTheme
import com.example.moviesdiscovery.features.movies.ui.model.MovieUiItem

@Composable
internal fun MovieCard(
    movie: MovieUiItem.Movie,
    onItemClick: () -> Unit,
    onFavoriteChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    ElevatedCard(
        onClick = onItemClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(172.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
        ) {
            Row(Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ThumbnailPoster(
                        imageUrl = movie.imageUrl,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = movie.voteAverage,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                    )
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = movie.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,

                        )
                    Text(
                        text = movie.overview,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                FavoriteButton(
                    isFavorite = movie.isFavorite,
                    onCheckedChange = { favorite ->
                        onFavoriteChange(movie.id, favorite)
                    }
                )
                ShareButton(onClick = { shareMovie(movie, context) })
            }
        }
    }
}

@Composable
private fun ThumbnailPoster(imageUrl: String, modifier: Modifier = Modifier) {
    val sizeResolver = rememberConstraintsSizeResolver()
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(sizeResolver)
            .build(),
        error = placeholderInPreview {
            rememberVectorPainter(Icons.Filled.Movie)
        }
    )
    Image(
        painter = imagePainter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.then(sizeResolver)
    )
}

@Composable
private fun ShareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onCheckedChange(!isFavorite) },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = null,
        )
    }
}

fun shareMovie(movie: MovieUiItem.Movie, context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_movie_intent_title))
        putExtra(Intent.EXTRA_TEXT, movie.title)
    }
    context.startActivity(
        Intent.createChooser(intent, null)
    )
}

@Preview(showBackground = true)
@Composable
private fun MovieCardPreview() {
    MoviesDiscoveryTheme {
        MovieCard(
            movie = MovieUiItem.Movie(
                id = 100,
                title = "Sonic the Hedgehog 3",
                overview = "Sonic, Knuckles, and Tails reunite against a powerful new adversary, Shadow, a mysterious villain with powers unlike anything they have faced before. With their abilities outmatched in every way, Team Sonic must seek out an unlikely alliance in hopes of stopping Shadow and protecting the planet.",
                voteAverage = "7.6",
                imageUrl = "/sonic",
                isFavorite = false,
                monthAndYearRelease = ""
            ),
            onItemClick = {},
            onFavoriteChange = { _, _ -> },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
