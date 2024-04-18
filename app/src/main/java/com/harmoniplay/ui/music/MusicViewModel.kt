package com.harmoniplay.ui.music

import android.Manifest
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harmoniplay.R
import com.harmoniplay.domain.music.MusicManager
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.domain.volume.StreamVolumeManager
import com.harmoniplay.service.ServiceActions
import com.harmoniplay.utils.composables.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicUseCase: MusicManager,
    private val userManager: UserManager,
    private val musicStreamVolumeManager: StreamVolumeManager
): ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchBarText = _searchText.asStateFlow()

    private val _state = MutableStateFlow(MusicState(
        isLoading = true,
        topBarTitle = UiText.DynamicString(
            value = userManager.getGreetings()
        )
    ))

    @OptIn(FlowPreview::class)
    val state = combine(
        _searchText.debounce(500),
        musicUseCase.songs,
        musicUseCase.playBy,
        musicUseCase.isLoading,
        _state,
    ) { searchText, songs, playBy, isLoading, state ->

        val filteredSongs = if(searchText.isNotBlank()) {
            songs.filter { song ->
                song.title.replace(" ","").contains(searchText, ignoreCase = true)
            }
        } else songs

        state.copy(
            songs = filteredSongs,
            selectedPlayBy = playBy,
            isLoading = isLoading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _state.value)

    private val _currentSongState = MutableStateFlow(CurrentSongState())
    val currentSongState = combine(
        musicUseCase.isPlaying,
        musicUseCase.currentSong,
        musicUseCase.currentSongIndex,
        musicStreamVolumeManager.volume,
        _currentSongState
    ) { isPlaying, song, index, volume, state ->
        state.copy(
            isPlaying = isPlaying,
            song = song,
            currentSongIndex = index,
            currentSongProgress = musicUseCase.getCurrentSongPosition(),
            volume = volume
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _currentSongState.value)

    private val updateSongProgressJob = viewModelScope.launch {
        while (true) {
            _currentSongState.update {
                it.copy(currentSongProgress = musicUseCase.getCurrentSongPosition())
            }
            delay(1000) // Delay for 1 second
        }
    }

    val permissionDialogQueue = mutableStateListOf<String>()

    private val resultChannel = Channel<MusicResult>()
    val musicResult = resultChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            musicUseCase.getAllSongs()
        }

        musicUseCase.error.onEach { error ->
            _state.update { it.copy(
                isLoading = false,
            ) }
            error.let {
                resultChannel.send(
                    MusicResult.Message(
                        UiText.DynamicString(it)
                    )
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: MusicEvent) {
        when (event) {
            is MusicEvent.OnSongClick -> playSong(id = event.id, pos = event.pos)

            is MusicEvent.OnProgressValueChanged -> musicUseCase.changeProgress(event.value.toLong())

            MusicEvent.OnScrollToCurrentSongClick -> scrollToCurrentSong()

            is MusicEvent.OnSearchTextChange -> _searchText.value = event.query

            is MusicEvent.OnFavoriteIconClick -> viewModelScope.launch {
                toggleFavorite(event.index)
            }

            is MusicEvent.OnPlayBySettingsChanged -> viewModelScope.launch {
                onEvent(MusicEvent.ToggleMusicSettingsSheet)
                if(event.playBy != state.value.selectedPlayBy) {
                    resultChannel.send(
                        MusicResult.StartPlayerService(action = ServiceActions.STOP)
                    )
                    musicUseCase.changePlayBy(event.playBy)
                }
            }

            MusicEvent.ClearSearchBar -> _searchText.update { "" }

            MusicEvent.HideSearchBar -> {
                _searchText.update { "" }
                _state.update {
                    it.copy(isSearchBarShowing = false)
                }
            }

            MusicEvent.ShowSearchBar -> {
                _searchText.update { "" }
                _state.update {
                    it.copy(isSearchBarShowing = true,)
                }
            }

            is MusicEvent.OnVolumeChange -> musicStreamVolumeManager.changeMusicVolume(event.volume)

            MusicEvent.DismissPermissionDialog -> permissionDialogQueue.removeFirst()

            is MusicEvent.OnPermissionResult -> onPermissionResult(
                permission = event.permission, isGranted = event.isGranted
            )

            MusicEvent.ToggleMusicSettingsSheet -> _state.update {
                it.copy(
                    shouldShowMusicSettingsSheet = !it.shouldShowMusicSettingsSheet
                )
            }

            MusicEvent.ToggleMusicKnob -> _state.update {
                it.copy(
                    shouldShowMusicKnob = !it.shouldShowMusicKnob
                )
            }

            MusicEvent.OnExitButtonClick -> _state.update {
                it.copy(shouldShowQuitDialog = true)
            }

            MusicEvent.OnCancelToQuitClick -> _state.update {
                it.copy(shouldShowQuitDialog = false)
            }

            MusicEvent.OnQuitButtonClick -> viewModelScope.launch {
                _state.update {
                    it.copy(shouldShowQuitDialog = false)
                }
                musicUseCase.shutDownPlayer()
                resultChannel.send(
                    MusicResult.StartPlayerService(
                        action = ServiceActions.STOP,
                        shouldExitFromApplication = true
                    )
                )
            }

            /**
             * Current Song Events
             * **/

            CurrentSongEvent.OnPlayClick -> {
                if(currentSongState.value.isPlaying) {
                    musicUseCase.pause()
                } else
                    musicUseCase.play()
            }

            CurrentSongEvent.SkipNext -> musicUseCase.skipToNextSong()

            CurrentSongEvent.SkipPrevious -> musicUseCase.skipToPreviousSong()

            is CurrentSongEvent.OnColorPaletteChange -> _currentSongState.update {
                it.copy(contentColorPalette = event.contentColorPalette)
            }

            is CurrentSongEvent.OnFavoriteIconClick -> viewModelScope.launch {
                if(state.value.selectedPlayBy == PlayBy.ONLY_FAVORITE) {
                    onEvent(CurrentSongEvent.ToggleSongContent)
                }
                toggleFavorite(event.index)
            }

            CurrentSongEvent.ToggleSongContent -> _currentSongState.update {
                it.copy(shouldExpandCurrentSongContent = !it.shouldExpandCurrentSongContent)
            }

            CurrentSongEvent.OnShareButtonClick -> viewModelScope.launch {
                currentSongState.value.song?.let {
                    resultChannel.send(
                        MusicResult.Share(uri = it.uri, title = it.title)
                    )
                }
            }

            is CurrentSongEvent.OnProgressValueChanged -> musicUseCase.changeProgress(event.value.toLong())
        }
    }

    private suspend fun toggleFavorite(index: Int) {
        if(state.value.selectedPlayBy == PlayBy.ONLY_FAVORITE) {
            resultChannel.send(
                MusicResult.StartPlayerService(action = ServiceActions.STOP)
            )
            resultChannel.send(
                MusicResult.Message(
                    UiText.StringResource(R.string.only_favorite_can_be_play, emptyList())
                )
            )
        }
        musicUseCase.toggleFavoriteSong(index)
    }

    private fun playSong(id: Long, pos: Int) {
        viewModelScope.launch {
            if (currentSongState.value.song?.id != id) {
                _currentSongState.update {
                    it.copy(currentSongProgress = 0f)
                }
                resultChannel.send(
                    MusicResult.StartPlayerService(ServiceActions.START)
                )
            }
            if (_searchText.value.isNotEmpty()) {
                musicUseCase.selectSong(id = id)
            } else {
                musicUseCase.selectSong(index = pos)
            }
        }
    }

    private fun scrollToCurrentSong() {
        viewModelScope.launch {
            if(_searchText.value.isEmpty() && currentSongState.value.currentSongIndex != null) {
                resultChannel.send(
                    MusicResult.ScrollToPosition(currentSongState.value.currentSongIndex!!)
                )
            } else {
                for(index in 0 until state.value.songs.size) {
                    if(state.value.songs[index].id == currentSongState.value.song?.id) {
                        resultChannel.send(
                            MusicResult.ScrollToPosition(index)
                        )
                        break
                    }
                }
            }
        }
    }

    private fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !permissionDialogQueue.contains(permission)) {
            permissionDialogQueue.add(permission)
        }
        if(isGranted) {
            val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else null

            if(permission == storagePermission || permission == notificationPermission) {
                viewModelScope.launch {
                    musicUseCase.getAllSongs()
                }
            }
        }
    }

    override fun onCleared() {
        updateSongProgressJob.cancel()
        musicStreamVolumeManager.unRegisterVolumeChangeListener()
        super.onCleared()
    }

}