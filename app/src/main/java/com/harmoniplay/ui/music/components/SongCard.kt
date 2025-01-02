package com.harmoniplay.ui.music.components

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmoniplay.domain.music.Song
import com.harmoniplay.ui.theme.HarmoniPlayTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    song: Song,
    isCurrentSong: Boolean = false,
    currentSongProgress: Float? = null,
    onClick: () -> Unit,
    onProgressValueChanged: (Float) -> Unit = {},
    onProgressValueChangedFinished: (() -> Unit)? = null,
    onFavoriteIconClick: () -> Unit = {  }
) {

    val cardColor by animateColorAsState(
        targetValue = if (isCurrentSong) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "Animated card Color"
    )

    val formattedDuration = remember {
        song.duration.formatMillisecondsToTime()
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp), // Set the corner radius here
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(
                    start = 16.dp, end = 16.dp, top = 16.dp,
                    bottom =
                    if(isCurrentSong) {
                        0.dp
                    } else 16.dp
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                /**
                 * Muted for temp
                LoadImageWithFallback(
                modifier = Modifier
                .weight(0.3f)
                .height(56.dp)
                .clip(shape = RoundedCornerShape(12.dp)),
                uri = song.artworkUri
                )
                Spacer(modifier = Modifier.width(10.dp))

                 */
                Column(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formattedDuration,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                )

                Crossfade(
                    targetState = song.isFavorite,
                    label = "Favorite Button Animation"
                ) { isFavorite ->
                    IconButton(
                        onClick = onFavoriteIconClick
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Default.Favorite
                            } else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite button",
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.errorContainer
                            } else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if(isCurrentSong && currentSongProgress != null) {
                Slider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = currentSongProgress,
                    onValueChange = onProgressValueChanged,
                    onValueChangeFinished = onProgressValueChangedFinished,
                    valueRange = 0f..song.duration.toFloat(),
                    thumb = {
                        Box(
                            Modifier
                                .size(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    },
                    track = { sliderState ->
                        val fraction by remember {
                            derivedStateOf {
                                (sliderState.value - sliderState.valueRange.start) /
                                        (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                            }
                        }

                        Box(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction)
                                    .height(6.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .height(6.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(1f - fraction)
                                        .height(3.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondary,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun Int.formatMillisecondsToTime(): String {
    val totalSeconds = this / 1000

    val hours = if(this >= 3600000){
        totalSeconds / 3600
    } else null

    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if(hours != null){
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Preview
@Composable
fun SongCardPreview(modifier: Modifier = Modifier) {
    HarmoniPlayTheme {
        SongCard(
            modifier = Modifier
                .fillMaxWidth(),
            song = Song(
                id = 1000L,
                title = "Song Title",
                uri = Uri.EMPTY,
                artist = "Artist",
                artworkUri = Uri.EMPTY,
                duration = 100000,
                isFavorite = true
            ),
            isCurrentSong = true,
            currentSongProgress = 1f,
            onClick = {  },
            onProgressValueChanged = {  },
            onProgressValueChangedFinished = {  },
        )
    }
}