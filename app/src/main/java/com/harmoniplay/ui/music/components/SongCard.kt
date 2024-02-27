package com.harmoniplay.ui.music.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmoniplay.domain.music.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    song: Song,
    isCurrentSong: Boolean = false,
    currentSongProgress: Float? = null,
    onClick: () -> Unit,
    onProgressValueChanged: (Float) -> Unit = {},
    onFavoriteIconClick: () -> Unit = {  }
) {

    val cardColor by animateColorAsState(
        targetValue = if (isCurrentSong) {
            MaterialTheme.colorScheme.secondaryContainer
        } else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "Animated card Color"
    )

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
                    bottom = if(isCurrentSong) {
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
                        style = MaterialTheme.typography.titleSmall,
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
                    text = song.durationInFormat,
                    fontSize = 12.sp,
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
                                Color.Red
                            } else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if(isCurrentSong && currentSongProgress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = currentSongProgress,
                        onValueChange = onProgressValueChanged,
                        valueRange = 0f.. song.duration.toFloat(),
                    )
                }
            }
        }
    }
}
