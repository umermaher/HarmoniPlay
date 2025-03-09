package com.harmoniplay.ui.music.current_song

import android.graphics.Color.parseColor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.harmoniplay.ui.ARTIST_NAME_BOUND_KEY
import com.harmoniplay.ui.ARTWORK_BOUND_KEY
import com.harmoniplay.ui.CONTENT_BG_BOUND_KEY
import com.harmoniplay.ui.TITLE_BOUND_KEY
import com.harmoniplay.ui.music.CurrentSongEvent
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.ui.music.components.currentsong.drawFadedEdge
import com.harmoniplay.utils.PaletteGenerator
import com.harmoniplay.utils.composables.LoadImageWithFallback
import com.harmoniplay.utils.composables.ObserveAsEvents
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CurrentSongScreen(
    state: CurrentSongState,
    onEvent: (CurrentSongEvent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    result: Flow<CurrentSongResult>,
    navigateUp: () -> Unit
) {

    val context = LocalContext.current

    val contentColorFromTheme = MaterialTheme.colorScheme.onPrimaryContainer
    val palette = state.contentColorPalette

    val expandCurrentSongContent = state.shouldExpandCurrentSongContent

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (expandCurrentSongContent && palette.isNotEmpty()) {
            Color(parseColor(palette["darkMuted"]!!))
        } else MaterialTheme.colorScheme.primaryContainer,
        label = "darkMutedSwatchColor"
    )

    val contentColor = remember(palette, expandCurrentSongContent) {
        if (expandCurrentSongContent && palette.isNotEmpty()) {
            Color(parseColor(palette["vibrant"]!!))
        } else contentColorFromTheme
    }

    LaunchedEffect(key1 = state.song) {
        state.song?.let {
            val bitmap = PaletteGenerator.convertImageUriToBitmapUsingCoil(
                uri = it.artworkUri, context = context
            )

            if (bitmap == null) {
                Log.i("bitmap null", palette.toString())
            }

            onEvent(
                CurrentSongEvent.OnColorPaletteChange(
                    contentColorPalette = if (bitmap != null) {
                        PaletteGenerator.extractColorsFromBitmap(bitmap)
                    } else mapOf()
                )
            )
        }
    }

    ObserveAsEvents(flow = result) { res ->
        when (res) {
            CurrentSongResult.NavigateUp -> navigateUp()
        }
    }

    BackHandler {
        onEvent(CurrentSongEvent.OnBackButtonClicked)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .background(color = animatedBackgroundColor),
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onEvent(CurrentSongEvent.OnBackButtonClicked)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onEvent(CurrentSongEvent.OnShareButtonClick)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = animatedBackgroundColor
                ),
            )
        }
    ) { values ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = animatedBackgroundColor)
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = CONTENT_BG_BOUND_KEY
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .padding(values)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                LoadImageWithFallback(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.374f)
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = ARTWORK_BOUND_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    uri = state.song?.artworkUri
                )

                Spacer(modifier = Modifier.weight(0.07f))

                Row {

                    /**
                     * Song title
                     * **/
                    Text(
                        modifier = Modifier
                            // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
                            // applied only to the text, and not whatever is drawn below this composable (e.g. the
                            // window).
                            .weight(1f)
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
                            )
                            .padding(start = 5.dp),
                        text = state.song?.title.toString(),
                        color = contentColor,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = contentColor
                        ),
                    )

                    Crossfade(
                        targetState = state.song?.isFavorite,
                        label = "Fav Icon Animation",
                    ) { isFavorite ->
                        IconButton(
                            onClick = {
                                state.currentSongIndex?.let {
                                    onEvent(CurrentSongEvent.OnFavoriteIconClick(it))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite == true) {
                                    Icons.Default.Favorite
                                } else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite button",
                                tint = contentColor
                            )
                        }
                    }
                }

                /**
                 * Artist Name
                 * **/
                Text(
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = ARTIST_NAME_BOUND_KEY
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    text = state.song?.artist.toString(),
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.weight(0.4f))

                /**
                 * Slider
                 * */
                state.song?.let { song ->
                    Slider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = state.currentSongProgress,
                        onValueChange = {
                            onEvent(CurrentSongEvent.OnProgressValueChanged(it))
                        },
                        onValueChangeFinished = {
                            onEvent(CurrentSongEvent.OnProgressValueChangedFinish)
                        },
                        valueRange = 0f..song.duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = contentColor,
                            activeTrackColor = contentColor,
                            inactiveTrackColor = contentColor.copy(alpha = 0.4f)
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onEvent(CurrentSongEvent.SkipPrevious)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Play Previous",
                            tint = contentColor
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .size(75.dp),
                        onClick = {
                            onEvent(CurrentSongEvent.OnPlayClick)
                        },
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(45.dp),
                            imageVector = if (state.isPlaying) {
                                Icons.Rounded.Pause
                            } else Icons.Rounded.PlayArrow,
                            contentDescription = "Play Current",
                            tint = contentColor,

                        )
                    }

                    IconButton(
                        onClick = {
                            onEvent(CurrentSongEvent.SkipNext)
                        },
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
    }
}