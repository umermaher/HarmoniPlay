package com.harmoniplay.ui.music.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.utils.composables.LoadImageWithFallback

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CurrentSongBar(
    currentSongState: CurrentSongState,
    skipPrevious: () -> Unit,
    onPlayClick: () -> Unit,
    skipNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .background(color = primaryThemeColor)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            LoadImageWithFallback(
                modifier = Modifier
                    .weight(0.2f)
                    .height(35.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                uri = currentSongState.song?.artworkUri
            )

            Column(
                modifier = Modifier.weight(1f),
            ){
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
                        // applied only to the text, and not whatever is drawn below this composable (e.g. the
                        // window).
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawFadedEdge(leftEdge = true)
                            drawFadedEdge(leftEdge = false)
                        }
                        .basicMarquee(
                            // Animate forever.
                            iterations = Int.MAX_VALUE,
                            spacing = MarqueeSpacing(0.dp)
                        )
                        .padding(start = 10.dp),
                    text = currentSongState.song?.title.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                )

                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = currentSongState.song?.artist.toString(),
                    fontSize = 11.sp,
                    color = Color.White
                )
            }

            IconButton(
                onClick = skipPrevious
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Play Previous",
                )
            }
            IconButton(
                onClick = onPlayClick
            ) {
                Icon(
                    imageVector = if(currentSongState.isPlaying) {
                        Icons.Default.Pause
                    } else Icons.Default.PlayArrow,
                    contentDescription = "Play Current",
                )
            }
            IconButton(
                onClick = skipNext
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Play Next",
                )
            }
        }
    }
}

fun ContentDrawScope.drawFadedEdge(leftEdge: Boolean) {
    val edgeWidthPx = 12.dp.toPx()
    drawRect(
        topLeft = Offset(if (leftEdge) 0f else size.width - edgeWidthPx, 0f),
        size = Size(edgeWidthPx, size.height),
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = if (leftEdge) 0f else size.width,
            endX = if (leftEdge) edgeWidthPx else size.width - edgeWidthPx
        ),
        blendMode = BlendMode.DstIn
    )
}