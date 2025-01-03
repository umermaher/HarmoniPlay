package com.harmoniplay.ui.music.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.harmoniplay.ui.music.CurrentSongEvent
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.ui.music.MusicEvent
import com.harmoniplay.ui.music.MusicState
import com.harmoniplay.ui.music.components.currentsong.CurrentSongBar
import com.harmoniplay.ui.music.components.currentsong.CurrentSongLocator
import com.harmoniplay.ui.music.components.musicknob.MusicKnob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicScreenContent(
    modifier: Modifier = Modifier,
    state: MusicState,
    currentSongState: CurrentSongState,
    lazyListState: LazyListState,
    onEvent: (MusicEvent) -> Unit,
) {

    val moonScrollSpeed = 0.05f
    val musicSoundIconsSpeed = 0.08f
    val windowAndShelfSpeed = 0.03f

    var moonOffset by remember {
        mutableFloatStateOf(0f)
    }
    var musicSoundIconsOffset by remember {
        mutableFloatStateOf(0f)
    }

    /**
     * [windowAndShelfOffset] was also added to image height to decrease the height of image
     * but changing the height can make list laggy due to slightly decrement in the size
     * scrolling list looks laggy due to decrement
     */
    var windowAndShelfOffset by remember {
        mutableFloatStateOf(0f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
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

//    Column(
//        modifier = modifier
//    ) {
//

    var zIndex by remember {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(state.shouldShowMusicKnob) {
        zIndex = if (state.shouldShowMusicKnob) {
            1f
        } else {
            delay(500)
            0f
        }
    }
    Box(
        modifier = modifier
    ) {

        MusicKnob(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(zIndex),
            volumeInPercentage = currentSongState.volume,
            shouldShow = state.shouldShowMusicKnob,
        ) {
            onEvent(MusicEvent.OnVolumeChange(it))
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
        ) {

            item { Spacer(modifier = Modifier.height(8.dp)) }

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
                        .animateItem()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 6.dp
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
                        onEvent(CurrentSongEvent.OnProgressValueChanged(it))
                    },
                    onProgressValueChangedFinished = {
                        onEvent(CurrentSongEvent.OnProgressValueChangedFinish)
                    },
                    onClick = {
                        onEvent(
                            MusicEvent.OnSongClick(song.id, index)
                        )
                    }
                )

                // Add additional content after 10 items
                if (index == 8 && !isSystemInDarkTheme()) {
                    ParallaxScrollEffectContent(
                        windowAndShelfOffset = windowAndShelfOffset,
                        moonOffset = moonOffset,
                        musicSoundIconsOffset = musicSoundIconsOffset
                    )
                }
            }
        }
    }

//        CurrentSongBar(
//            currentSongState = currentSongState,
//            skipPrevious = {
//                onEvent(CurrentSongEvent.SkipPrevious)
//            },
//            onPlayClick = {
//                onEvent(CurrentSongEvent.OnPlayClick)
//            },
//            skipNext = {
//                onEvent(CurrentSongEvent.SkipNext)
//            }
//        )

//        if(currentSongState.song != null) {
//            Spacer(modifier = Modifier.size(50.dp))
//        }
//    }
}