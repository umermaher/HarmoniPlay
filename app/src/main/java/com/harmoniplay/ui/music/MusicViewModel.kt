package com.harmoniplay.ui.music

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harmoniplay.domain.music.MusicUseCase
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
open class MusicViewModel @Inject constructor(
    private val musicUseCase: MusicUseCase,
): ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchBarText = _searchText.asStateFlow()

    var currentSongProgress by mutableFloatStateOf(0f)

    private val _state = MutableStateFlow(MusicState(isLoading = true))
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
        _currentSongState
    ) { isPlaying, song, index, state ->
        currentSongProgress = musicUseCase.getCurrentSongPosition()

        state.copy(
            isPlaying = isPlaying,
            song = song,
            currentSongIndex = index
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), _currentSongState.value)

    private val updateSongProgressJob = viewModelScope.launch {
        while (true) {
            currentSongProgress = musicUseCase.getCurrentSongPosition()
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
            error?.let {
                resultChannel.send(
                    MusicResult.Message(it)
                )
            }
            _state.update { it.copy(
                isLoading = false,
            ) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: MusicEvent) {
        when (event) {
            is MusicEvent.OnSongClick -> {
                if(currentSongState.value.song?.id != event.id)
                    currentSongProgress = 0f
                if(_searchText.value.isNotEmpty()) {
                    musicUseCase.selectSong(id = event.id)
                } else {
                    musicUseCase.selectSong(index = event.pos)
                }
            }

            MusicEvent.OnPlayClick -> {
                if(currentSongState.value.isPlaying) {
                    musicUseCase.pause()
                } else
                    musicUseCase.play()
            }

            MusicEvent.SkipNext -> musicUseCase.skipToNextSong()

            MusicEvent.SkipPrevious -> musicUseCase.skipToPreviousSong()

            is MusicEvent.OnProgressValueChanged ->
                musicUseCase.changeProgress(event.value.toLong())

            MusicEvent.OnScrollToCurrentSongClick -> scrollToCurrentSong()

            is MusicEvent.OnSearchTextChange -> _searchText.value = event.query

            is MusicEvent.OnFavoriteIconClick -> viewModelScope.launch {
                musicUseCase.toggleFavoriteSong(event.index)
            }

            is MusicEvent.OnPlayBySettingsChanged -> viewModelScope.launch {
                musicUseCase.changePlayBy(event.playBy)
            }

            MusicEvent.ClearSearchBar -> _state.update {
                it.copy(searchBarText = "")
            }

            MusicEvent.DismissPermissionDialog -> permissionDialogQueue.removeFirst()

            MusicEvent.HideSearchBar -> _state.update {
                it.copy(
                    searchBarText = "",
                    isSearchBarShowing = false,
                )
            }

            is MusicEvent.OnPermissionResult -> {}

            MusicEvent.ShowSearchBar -> _state.update {
                it.copy(
                    searchBarText = "",
                    isSearchBarShowing = true,
                )
            }

            MusicEvent.ToggleMusicSettingsSheet -> _state.update {
                it.copy(
                    shouldShowMusicSettingsSheet = !it.shouldShowMusicSettingsSheet
                )
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

    override fun onCleared() {
        updateSongProgressJob.cancel()
        super.onCleared()
    }
}