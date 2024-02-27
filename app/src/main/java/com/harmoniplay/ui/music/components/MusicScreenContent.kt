package com.harmoniplay.ui.music.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.harmoniplay.R
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.ui.music.MusicEvent
import com.harmoniplay.ui.music.MusicState
import com.harmoniplay.ui.music.components.musicknob.MusicKnob
import com.harmoniplay.utils.toDp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicScreenContent(
    modifier: Modifier = Modifier,
    state: MusicState,
    currentSongState: CurrentSongState,
    lazyListState: LazyListState,
    onEvent: (MusicEvent) -> Unit,
) {

    val isScrolling by remember {
        derivedStateOf {
            lazyListState.isScrollInProgress
        }
    }

    val moonScrollSpeed = 0.06f
    val musicSoundIconsSpeed = 0.08f
    val windowAndShelfSpeed = 0.03f

    // image should be crop little bit to show more parallax effect
    // means image height should be known
    // height must be assign with respect to width
    // and the aspect ratio is 2/3 i.e. width of image will be 1.5 of height
    val imageHeight = (LocalConfiguration.current.screenWidthDp * (2f/3f)).dp

    var moonOffset by remember {
        mutableFloatStateOf(0f)
    }
    var musicSoundIconsOffset by remember {
        mutableFloatStateOf(0f)
    }
    var windowAndShelfOffset by remember {
        mutableFloatStateOf(0f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                Log.i("scroll delta offset", delta.toString())
                val layoutInfo = lazyListState.layoutInfo
                if (lazyListState.firstVisibleItemIndex == 0
                    || layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    == layoutInfo.totalItemsCount - 1
                ) {
                    return Offset.Zero
                }
                moonOffset += delta * moonScrollSpeed
                musicSoundIconsOffset += delta * musicSoundIconsSpeed
                windowAndShelfOffset += delta * windowAndShelfSpeed
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
    ) {

        Box(
            modifier = Modifier.weight(1f)
        ) {

            MusicKnob(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(1f)
                ,
                volumeInPercentage = currentSongState.volume,
                shouldShow = state.shouldShowMusicKnob,
            ) {
                onEvent(MusicEvent.OnVolumeChange(it))
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(nestedScrollConnection)
            ) {

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(
                    items = state.songs,
                    key = { index, song ->
                        if (index == 0) {
                            index
                        } else song.id
                    }
                ) { index, song ->

                    val isCurrentSong = currentSongState.song?.id == song.id

                    SongCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .animateItemPlacement(
                                animationSpec = tween(
                                    durationMillis = 300
                                )
                            ),
                        song = song,
                        isCurrentSong = isCurrentSong,
                        currentSongProgress = if (isCurrentSong) {
                            currentSongState.currentSongProgress
                        } else null,
                        onFavoriteIconClick = {
                            onEvent(MusicEvent.OnFavoriteIconClick(index))
                        },
                        onProgressValueChanged = {
                            onEvent(MusicEvent.OnProgressValueChanged(it))
                        },
                        onClick = {
                            onEvent(
                                MusicEvent.OnSongClick(song.id, index)
                            )
                        }
                    )
                    
                    // Add additional content after 10 items
                    if (index == 8 && !isSystemInDarkTheme()) {
                        Box(
                            modifier = Modifier
                                .clipToBounds()
                                .fillMaxWidth()
                                .height(imageHeight + windowAndShelfOffset.toDp())
                                .align(Alignment.BottomCenter)
//                                .background(
//                                    Brush.verticalGradient(
//                                        listOf(
//                                            MaterialTheme.colorScheme.primary,
//                                            MaterialTheme.colorScheme.secondary
//                                        )
//                                    )
//                                )
                        ) {

                            // content will slide up so alignment should be bottom
                            Image(
                                painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_1),
                                contentDescription = "Window and shelf",
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.BottomCenter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = windowAndShelfOffset
                                    }
                            )

                            Image(
                                painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_4),
                                contentDescription = "Moon",
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.BottomCenter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = moonOffset
                                    }
                            )

                            Image(
                                painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_2),
                                contentDescription = "Music Floating Icons",
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.BottomCenter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = musicSoundIconsOffset
                                    }
                            )

                            Image(
                                painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_3),
                                contentDescription = "A boy and furniture",
                                contentScale = ContentScale.Inside,
                                alignment = Alignment.BottomCenter,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            CurrentSongLocator(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-20).dp),
                visible = isScrolling && currentSongState.song != null
            ) {
                onEvent(
                    MusicEvent.OnScrollToCurrentSongClick
                )
            }
        }

        CurrentSongBar(
            currentSongState = currentSongState,
            skipPrevious = {
                onEvent(MusicEvent.SkipPrevious)
            },
            onPlayClick = {
                onEvent(MusicEvent.OnPlayClick)
            },
            skipNext = {
                onEvent(MusicEvent.SkipNext)
            }
        )
    }
}