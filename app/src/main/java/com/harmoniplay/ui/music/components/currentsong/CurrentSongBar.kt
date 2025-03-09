@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.harmoniplay.ui.music.components.currentsong

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.harmoniplay.ui.ARTIST_NAME_BOUND_KEY
import com.harmoniplay.ui.ARTWORK_BOUND_KEY
import com.harmoniplay.ui.CONTENT_BG_BOUND_KEY
import com.harmoniplay.ui.TITLE_BOUND_KEY
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.utils.composables.LoadImageWithFallback

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.CurrentSongBar(
    modifier: Modifier = Modifier,
    currentSongState: CurrentSongState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    skipPrevious: () -> Unit,
    onPlayClick: () -> Unit,
    skipNext: () -> Unit
) {

    val contentColor = MaterialTheme.colorScheme.onPrimary

    AnimatedVisibility(
        visible = currentSongState.song != null,
        enter = expandVertically(
            expandFrom = Alignment.Bottom,
            animationSpec = tween(
                durationMillis = 200
            )
        )
    ) {
        Row(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = CONTENT_BG_BOUND_KEY
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            LoadImageWithFallback(
                modifier = Modifier
                    .weight(0.2f)
                    .height(35.dp)
                    .sharedElement(
                        state = rememberSharedContentState(
                            key = ARTWORK_BOUND_KEY
                        ),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .clip(shape = RoundedCornerShape(10.dp)),
                uri = currentSongState.song?.artworkUri
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
                        // applied only to the text, and not whatever is drawn below this composable (e.g. the
                        // window).
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = TITLE_BOUND_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
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
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = contentColor
                )

                Text(
                    modifier = Modifier.padding(start = 10.dp)
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = ARTIST_NAME_BOUND_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    text = currentSongState.song?.artist.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
            }

            IconButton(
                onClick = skipPrevious
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Play Previous",
                    tint = contentColor
                )
            }
            IconButton(
                onClick = onPlayClick
            ) {
                Icon(
                    imageVector = if (currentSongState.isPlaying) {
                        Icons.Rounded.Pause
                    } else Icons.Rounded.PlayArrow,
                    contentDescription = "Play Current",
                    tint = contentColor
                )
            }
            IconButton(
                onClick = skipNext
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Play Next",
                    tint = contentColor
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