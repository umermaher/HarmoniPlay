package com.harmoniplay.ui.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.harmoniplay.R
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.ui.MainActivity
import com.harmoniplay.ui.music.components.MusicScreenContent
import com.harmoniplay.ui.music.components.MusicTopBar
import com.harmoniplay.ui.music.components.currentsong.CurrentSongContent
import com.harmoniplay.ui.music.components.currentsong.CurrentSongLocator
import com.harmoniplay.utils.composables.ConfirmDialog
import com.harmoniplay.utils.composables.GeneralBottomSheet
import com.harmoniplay.utils.composables.MusicPermissionTextProvider
import com.harmoniplay.utils.composables.NotificationPermissionTextProvider
import com.harmoniplay.utils.composables.ObserveAsEvents
import com.harmoniplay.utils.composables.OnBoardMessage
import com.harmoniplay.utils.composables.OptionSheetCard
import com.harmoniplay.utils.composables.PermissionDialog
import com.harmoniplay.utils.composables.ProgressIndicator
import com.harmoniplay.utils.composables.UiText
import com.harmoniplay.utils.hasPermissions
import com.harmoniplay.utils.openAppSettings
import com.harmoniplay.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MusicScreen(
    state: MusicState,
    searchBarText: String,
    currentSongState: CurrentSongState,
    permissionDialogQueue: SnapshotStateList<String>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    result: Flow<MusicResult>,
    onEvent: (MusicEvent) -> Unit,
    navigate: (route: String) -> Unit
) {

    val activity = LocalContext.current as MainActivity

    val musicSettingSheetState = rememberModalBottomSheetState()
    val lazyListState = rememberLazyListState()

    val scope = rememberCoroutineScope()

    val isScrolling by remember {
        derivedStateOf {
            lazyListState.isScrollInProgress
        }
    }

    var isLocatorVisible by remember { mutableStateOf(false) }

    val permissionsToRequest = remember {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else null

        if(notificationPermission != null) {
            arrayOf(storagePermission, notificationPermission)
        } else arrayOf(storagePermission)
    }

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                onEvent( MusicEvent.OnPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                ) )
            }
        }
    )

    LaunchedEffect(key1 = true) {
        // Checking Permission in Login screen is not enough
        // User can remove it from the settings too
        // To handle this working with permission is necessary
        if(!activity.hasPermissions(permissionsToRequest.toList())) {
            multiplePermissionResultLauncher.launch(permissionsToRequest)
        }
    }

    var scrollJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            isLocatorVisible = true
            scrollJob?.cancel() // Cancel any existing coroutine
            scrollJob = launch {
                delay(2000)
                isLocatorVisible = false
            }
        } else {
            scrollJob?.cancel() // Cancel any existing coroutine when scrolling stops
            scrollJob = launch {
                delay(2000)
                isLocatorVisible = false
            }
        }
    }

    BackHandler(state.isSearchBarShowing || currentSongState.shouldExpandCurrentSongContent) {
        if (state.isSearchBarShowing) {
            onEvent(MusicEvent.HideSearchBar)
        }
        if (currentSongState.shouldExpandCurrentSongContent) {
            onEvent(CurrentSongEvent.ToggleSongContent)
        }
    }

    ObserveAsEvents(flow = result) { res ->
        when (res) {
            is MusicResult.Message -> activity.showToast(
                when (res.msg) {
                    is UiText.DynamicString -> res.msg.value
                    is UiText.StringResource -> activity.getString(
                        res.msg.value
                    )
                }
            )

            is MusicResult.ScrollToPosition -> {
                scope.launch {
                    lazyListState.scrollToItem(index = res.pos)
                }
            }

            is MusicResult.StartPlayerService -> {
                activity.startPlayerService(res.action)
                if (res.shouldExitFromApplication) {
                    activity.finishAndRemoveTask()
                }
            }

            is MusicResult.Share -> scope.launch {
                // Create an intent to send the audio
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, res.uri)
                    type = "audio/*" // Set the MIME type to audio/*
                }
                Log.i("MusicResult.Share", "Sharing Audio")
                val chooser = Intent.createChooser(sendIntent, "Share Song")
                if (sendIntent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(chooser)
                }
            }

            is MusicResult.Navigate -> navigate(res.route)
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MusicTopBar(
                    state = state,
                    searchBarText = searchBarText,
                    onEvent = onEvent,
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                CurrentSongLocator(
                    visible = isLocatorVisible && currentSongState.song != null
                ) {
                    onEvent(
                        MusicEvent.OnScrollToCurrentSongClick
                    )
                }
            }
        ) { values ->
            when {
                state.songs.isNotEmpty() -> {
                    MusicScreenContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values),
                        state = state,
                        currentSongState = currentSongState,
                        animatedVisibilityScope = animatedVisibilityScope,
                        lazyListState = lazyListState,
                        onEvent = onEvent
                    )
                }
                state.isLoading -> ProgressIndicator()
                else -> {
                    val modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
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
            }
        }

//        if(currentSongState.song != null) {
//            CurrentSongContent(
//                modifier = Modifier
//                    .fillMaxSize(),
//                currentSongState = currentSongState,
//                onEvent = onEvent,
//            )
//        }

    }

    if (state.shouldShowMusicSettingsSheet) {
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
                            onEvent(MusicEvent.OnPlayBySettingsChanged(option.playBy))
                        }
                    }
                }
            }
        }
    }

    if (state.shouldShowQuitDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.quit),
            text = stringResource(id = R.string.quit_msg),
            onOkClick = {
                onEvent(MusicEvent.OnQuitButtonClick)
            },
            onDismiss = {
                onEvent(MusicEvent.OnCancelToQuitClick)
            }
        )
    }

    permissionDialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.READ_EXTERNAL_STORAGE ->
                        MusicPermissionTextProvider()
                    Manifest.permission.READ_MEDIA_AUDIO ->
                        MusicPermissionTextProvider()
                    Manifest.permission.POST_NOTIFICATIONS ->
                        NotificationPermissionTextProvider()

                    else -> return@forEach
                },
                isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationale(
                    permission
                ),
                onDismiss = {
                    onEvent(MusicEvent.DismissPermissionDialog)
                },
                onOkClick = {
                    onEvent(MusicEvent.DismissPermissionDialog)
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = activity::openAppSettings
            )
        }
}
