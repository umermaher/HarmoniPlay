package com.harmoniplay.ui.music.current_song


sealed interface CurrentSongResult {
    data object NavigateUp: CurrentSongResult
}