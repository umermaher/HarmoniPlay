package com.harmoniplay.ui.music

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.harmoniplay.R
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.domain.music.Song
import com.harmoniplay.service.ServiceActions
import com.harmoniplay.utils.composables.UiText

@Immutable
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
    val shouldAnimateItemPlacements: Boolean = false,
    val isSearchBarShowing: Boolean = false,
    val shouldShowMusicSettingsSheet: Boolean = false,
    val shouldShowMusicKnob: Boolean = false,
    val shouldShowQuitDialog: Boolean = false,
)

@Immutable
data class CurrentSongState(
    val isPlaying: Boolean = false,
    val currentSongIndex: Int ?= null,
    val song: Song?= null,
    val currentSongProgress: Float = 0f,
    val contentColorPalette: Map<String,String> = mapOf(),
    val volume: Float = 0f,
    val shouldExpandCurrentSongContent: Boolean = false,
    val isUserChangingProgress: Boolean = false
)

sealed class MusicResult {
    data class Message(val msg: UiText): MusicResult()
    data class ScrollToPosition(val pos: Int): MusicResult()
    data class StartPlayerService(val action: ServiceActions, val shouldExitFromApplication: Boolean = false): MusicResult()
    data class Share(val uri: Uri, val title: String): MusicResult()
}

sealed class MusicEvent {
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): MusicEvent()
    data object DismissPermissionDialog: MusicEvent()
    data object OnExitButtonClick: MusicEvent()
    data class OnSearchTextChange(val query: String): MusicEvent()
    data object ShowSearchBar: MusicEvent()
    data object HideSearchBar: MusicEvent()
    data object ClearSearchBar: MusicEvent()
    data object ToggleMusicSettingsSheet: MusicEvent()
    data object ToggleMusicKnob: MusicEvent()
    data class OnVolumeChange(val volume: Float): MusicEvent()
    data class OnSongClick(val id: Long, val pos: Int): MusicEvent()
    data class OnFavoriteIconClick(val index: Int): MusicEvent()
    class OnPlayBySettingsChanged(val playBy: PlayBy) : MusicEvent()
    data object OnScrollToCurrentSongClick: MusicEvent()
    data object OnQuitButtonClick: MusicEvent()
    data object OnCancelToQuitClick: MusicEvent()
}

sealed class CurrentSongEvent: MusicEvent() {
    data class OnColorPaletteChange(val contentColorPalette: Map<String,String>): CurrentSongEvent()
    data object OnPlayClick: CurrentSongEvent()
    data object SkipNext: CurrentSongEvent()
    data object SkipPrevious: CurrentSongEvent()
    data object ToggleSongContent: CurrentSongEvent()
    data class OnFavoriteIconClick(val index: Int): CurrentSongEvent()
    data object OnShareButtonClick: CurrentSongEvent()
    data class OnProgressValueChanged(val value: Float): CurrentSongEvent()
    data object OnProgressValueChangedFinish: CurrentSongEvent()
}

data class PlayByOption(
    val txtRes: Int,
    val playBy: PlayBy
)