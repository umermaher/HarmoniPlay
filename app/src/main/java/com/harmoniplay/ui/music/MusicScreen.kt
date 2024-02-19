package com.harmoniplay.ui.music

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.harmoniplay.R
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.service.ServiceActions
import com.harmoniplay.ui.MainActivity
import com.harmoniplay.ui.music.components.CurrentSongBar
import com.harmoniplay.ui.music.components.CurrentSongLocator
import com.harmoniplay.ui.music.components.MusicTopBar
import com.harmoniplay.ui.music.components.SongCard
import com.harmoniplay.utils.composables.GeneralBottomSheet
import com.harmoniplay.utils.composables.ObserveAsEvents
import com.harmoniplay.utils.composables.OnBoardMessage
import com.harmoniplay.utils.composables.OptionSheetCard
import com.harmoniplay.utils.composables.ProgressIndicator
import com.harmoniplay.utils.showToast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MusicScreen(
    state: MusicState,
    currentSongState: CurrentSongState,
    currentSongProgress: Float,
    result: Flow<MusicResult>,
    onEvent: (MusicEvent) -> Unit,
) {
    val activity = LocalContext.current as MainActivity

    val musicSettingSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var isScrolling by remember {
        mutableStateOf(false)
    }

    BackHandler(state.isSearchBarShowing) {
        onEvent(MusicEvent.HideSearchBar)
    }

    ObserveAsEvents(flow = result) { res ->
        when (res) {
            is MusicResult.Message -> activity.showToast(res.msg)
            is MusicResult.ScrollToPosition -> {
                scope.launch {
                    lazyListState.scrollToItem(index = res.pos)
                }
            }
        }
    }

    val scrollBehavior = enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopBar(
                state = state,
                onEvent = onEvent,
                scrollBehavior = scrollBehavior,
                onMenuIconClick = {

                })
        }
    ) { values ->
        when {
            state.songs.isEmpty() && !state.isLoading -> {
                val modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .background(Color.White)
                when (state.selectedPlayBy) {
                    PlayBy.ONLY_FAVORITE -> OnBoardMessage(
                        modifier = modifier,
                        imgRes = R.drawable.img_onboard_music,
                        titleRes = R.string.no_fav_songs_msg,
                        msgRes = R.string.add_fav_songs_msg
                    )
                    PlayBy.ALL -> OnBoardMessage(
                        modifier = modifier,
                        imgRes = R.drawable.img_onboard_music,
                        titleRes = R.string.no_songs_msg,
                        msgRes = R.string.empty_string
                    )
                }
            }

            state.isLoading -> ProgressIndicator()

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(values)
                ) {

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .onGloballyPositioned {
                                    // This callback will be called whenever the scroll position changes
                                    isScrolling = lazyListState.isScrollInProgress
                                }
                        ) {

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            itemsIndexed(
                                items = state.songs,
                                key = { _, song ->
                                    song.id
                                }
                            ) { index, song ->
                                val isCurrentSong = currentSongState.song?.id == song.id
                                SongCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .animateItemPlacement(
                                            animationSpec = tween(
                                                durationMillis = 400
                                            )
                                        ),
                                    song = song,
                                    isCurrentSong = isCurrentSong,
                                    currentSongProgress = if (isCurrentSong) {
                                        currentSongProgress
                                    } else null,
                                    onFavoriteIconClick = {
                                        if(state.selectedPlayBy == PlayBy.ONLY_FAVORITE) {
                                            activity.startPlayerService(ServiceActions.STOP)
                                        }
                                        onEvent(MusicEvent.OnFavoriteIconClick(index))
                                    },
                                    onProgressValueChanged = {
                                        onEvent(MusicEvent.OnProgressValueChanged(it))
                                    },
                                    onClick = {
                                        activity.startPlayerService(ServiceActions.START)
                                        onEvent(
                                            MusicEvent.OnSongClick(song.id, index)
                                        )
                                    }
                                )
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

                    if(currentSongState.song != null) {
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
            }
        }
    }



    if(state.shouldShowMusicSettingsSheet) {
        GeneralBottomSheet(
            sheetState = musicSettingSheetState,
            titleRes = R.string.play,
            onDismissRequest = {
                onEvent(MusicEvent.ToggleMusicSettingsSheet)
            }
        ) {
            LazyColumn {
                items(state.playByOptions) { option ->
                    OptionSheetCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        txtRes = option.txtRes,
                        isSelected = option.playBy == state.selectedPlayBy
                    ) {
                        scope.launch {
                            musicSettingSheetState.hide()
                            onEvent(MusicEvent.ToggleMusicSettingsSheet)

                            if(option.playBy != state.selectedPlayBy) {
                                activity.startPlayerService(ServiceActions.STOP)
                                onEvent(MusicEvent.OnPlayBySettingsChanged(option.playBy))
                            }
                        }
                    }
                }
            }
        }
    }
}
