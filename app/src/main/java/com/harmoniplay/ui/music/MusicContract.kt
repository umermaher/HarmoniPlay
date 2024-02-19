package com.harmoniplay.ui.music

import com.harmoniplay.R
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.domain.music.Song
import com.harmoniplay.utils.composables.UiText

data class MusicState(
    val isLoading: Boolean = true,
    val songs: List<Song> = emptyList(),
    val selectedPlayBy: PlayBy = PlayBy.ALL,
    val playByOptions: List<PlayByOption> = listOf(
        PlayByOption(
            R.string.all,
            PlayBy.ALL
        ),
        PlayByOption(
            R.string.only_favorite,
            PlayBy.ONLY_FAVORITE
        ),
    ),
    val topBarTitle: UiText = UiText.DynamicString(""),
    val searchBarText: String = "",
    val isSearchBarShowing: Boolean = false,
    val shouldShowMusicSettingsSheet: Boolean = false,
)

data class CurrentSongState(
    val isPlaying: Boolean = false,
    val currentSongIndex: Int ?= null,
    val song: Song?= null
)

sealed class MusicResult {
    data class Message(val msg: String): MusicResult()
    data class ScrollToPosition(val pos: Int): MusicResult()
}

sealed interface MusicEvent {
    data object ToggleMusicSettingsSheet: MusicEvent
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): MusicEvent
    data object DismissPermissionDialog: MusicEvent
    data class OnSongClick(val id: Long, val pos: Int): MusicEvent
    data class OnFavoriteIconClick(val index: Int): MusicEvent
    data class OnProgressValueChanged(val value: Float): MusicEvent
    data class OnSearchTextChange(val query: String): MusicEvent
    data object ShowSearchBar: MusicEvent
    data object HideSearchBar: MusicEvent
    data object ClearSearchBar: MusicEvent
    class OnPlayBySettingsChanged(val playBy: PlayBy) : MusicEvent
    data object OnScrollToCurrentSongClick: MusicEvent
    data object OnPlayClick: MusicEvent
    data object SkipNext: MusicEvent
    data object SkipPrevious: MusicEvent
}

data class PlayByOption(
    val txtRes: Int,
    val playBy: PlayBy
)