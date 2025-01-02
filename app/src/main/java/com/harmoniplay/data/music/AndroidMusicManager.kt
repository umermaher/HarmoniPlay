package com.harmoniplay.data.music

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.harmoniplay.domain.utils.Result
import com.harmoniplay.domain.music.MusicRepository
import com.harmoniplay.domain.music.MusicManager
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.domain.music.PlayBy.*
import com.harmoniplay.domain.music.Song
import com.harmoniplay.domain.music.SongDomain
import com.harmoniplay.domain.utils.DataError
import com.harmoniplay.utils.TIME_UPDATED_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidMusicManager(
    private val musicRepository: MusicRepository,
    private val exoPlayer: ExoPlayer,
): MusicManager {

    override val playBy: StateFlow<PlayBy> = musicRepository.playBy
    override val songs = musicRepository.songs

    private val _currentSong = MutableStateFlow<SongDomain?>(null)
    override val currentSong: StateFlow<Song?>
        get() = _currentSong.asStateFlow()

    private val _currentSongIndex = MutableStateFlow<Int?>(null)
    override val currentSongIndex: StateFlow<Int?>
        get() = _currentSongIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    override val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<DataError.Local>()
    override val error: SharedFlow<DataError.Local>
        get() = _error.asSharedFlow()

    // Check if the ExoPlayer has media items
    private fun hasCurrentMediaItems(): Boolean = exoPlayer.currentMediaItem != null
//    private val hasMediaItems: Boolean = exoPlayer.playbackState != Player.STATE_IDLE &&
//            !exoPlayer.currentTimeline.isEmpty

    init {
        exoPlayer.addListener(object: Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                _isPlaying.update { isPlaying }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)

                resumeState()

                if (!exoPlayer.isPlaying) {
                    play()
                }
                Log.i("currentMediaItemIndex", exoPlayer.currentMediaItemIndex.toString())
            }
        })
    }

    override suspend fun getAllSongs() {
        when (val res = musicRepository.fetchSongs()) {
            is Result.Error -> {
                _isLoading.update { false }
                _error.emit(res.error)
            }
            is Result.Success -> {
                while (songs.value.isEmpty() && playBy.value != ONLY_FAVORITE) {
                    delay(TIME_UPDATED_INTERVAL)
                }
                if (hasCurrentMediaItems()) {
                    Log.i("Resuming", "resuming current song state")
                    resumeState()
                }
                delay(TIME_UPDATED_INTERVAL)
                _isLoading.update { false }
                Log.i("Resuming", "break")
            }
        }
    }

    override fun selectSong(index: Int) {
        Log.i("Song selected index", index.toString())
        Log.i("Previous song index", _currentSongIndex.value.toString())
        if (!exoPlayer.isPlaying && exoPlayer.mediaItemCount == 0) {
            exoPlayer.setMediaItems(getMediaItems(), index, 0)
        } else {
            if(_currentSongIndex.value == index)
                return
            pause()
            exoPlayer.seekTo(index, 0)
        }
        exoPlayer.prepare()
        play()
    }

    override fun selectSong(id: Long) {
        val pos = songs.value.indexOfFirst { it.id == id }
        selectSong(index = pos)
    }

    override fun getCurrentSongPosition(): Float = exoPlayer.currentPosition.toFloat()

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun skipToNextSong() {
        if(exoPlayer.hasNextMediaItem())
            exoPlayer.seekToNext()
    }

    override fun skipToPreviousSong() {
        if(exoPlayer.hasPreviousMediaItem())
            exoPlayer.seekToPrevious()
    }

    override fun changeProgress(value: Long) = exoPlayer.seekTo(value)

    override suspend fun toggleFavoriteSong(index: Int) {
        val song = songs.value[index]
        if(song.isFavorite) {
            // if play by is fav then only favorite songs will be played
            if (playBy.value == ONLY_FAVORITE) {
                exoPlayer.removeMediaItem(index)
                _isPlaying.update { false }
                _currentSong.update { null }
                _currentSongIndex.update { null }
            } else {
                _currentSong.update {
                    it?.copy(isFavorite = false)
                }
            }
            musicRepository.deleteSong(song = song)
        } else {
            _currentSong.update {
                it?.copy(isFavorite = true)
            }
            musicRepository.addSong(song)
        }
    }

    override suspend fun toggleFavoriteSong(song: SongDomain) {
        songs.value.indexOf(song).also { index ->
            if(index != -1) {
                toggleFavoriteSong(index)
            }
        }
    }

    override suspend fun changePlayBy(value: PlayBy) {
        stopState()
        musicRepository.playMusicBy(value)
        exoPlayer.clearMediaItems()
    }

    override fun shutDownPlayer() {
        _isPlaying.update { false }
        _currentSong.update { null }
        _currentSongIndex.update { null }
        exoPlayer.clearMediaItems()
    }

    private fun resumeState() {
        if(exoPlayer.currentMediaItemIndex < songs.value.size && exoPlayer.mediaItemCount != 0) {
            Log.i("Resuming","resuming current song state")
            _isPlaying.update { exoPlayer.isPlaying }
            _currentSongIndex.update { exoPlayer.currentMediaItemIndex }
            _currentSong.update {
                songs.value[exoPlayer.currentMediaItemIndex]
            }
        }
    }

    private fun stopState() {
        _isPlaying.update { false }
        _currentSong.update { null }
        _currentSongIndex.update { null }
    }

    private fun getMediaItems(): MutableList<MediaItem> {
        Log.i("Exoplayer","Initializing songs to exoplayer")
        val mediaItems = ArrayList<MediaItem>()
        songs.value.forEach { song ->
            val item = MediaItem.Builder()
                .setUri(song.uri)
                .setMediaMetadata(getMetaData(song))
                .build()
            //add the media item to list
            mediaItems.add(item)
        }
        return mediaItems
    }

    private fun getMetaData(song: Song): MediaMetadata =
        MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtworkUri(song.artworkUri)
            .build()

}