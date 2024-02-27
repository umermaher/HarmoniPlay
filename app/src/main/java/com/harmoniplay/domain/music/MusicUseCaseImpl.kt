package com.harmoniplay.domain.music

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.harmoniplay.data.music.LocalSong
import com.harmoniplay.data.music.MusicRepository
import com.harmoniplay.data.music.db.model.SongObject
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.domain.music.PlayBy.*
import com.harmoniplay.utils.Resource2
import com.harmoniplay.utils.TIME_UPDATED_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MusicUseCaseImpl(
    private val musicRepository: MusicRepository,
    private val userManager: UserManager,
    private val exoPlayer: ExoPlayer,
): MusicUseCase {

    private val _localSongs = MutableStateFlow<List<LocalSong>>(emptyList())

    private val _playBy = MutableStateFlow(
        when(userManager.getPlayMusicBy()) {
            ALL.toString() -> ALL
            else -> ONLY_FAVORITE
        }
    )
    override val playBy: StateFlow<PlayBy>
        get() = _playBy.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    private val favSongsIds = musicRepository.getFavoriteSongsIds().map {
        it
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList<Long>())

    override val songs = combine(
        _localSongs,
        favSongsIds,
        playBy
    ) { songs, favIds, pb ->

        when (pb) {
            ALL -> songs.map {
                it.toSongDomain(
                    isFavorite = it.id in favIds
                )
            }
            ONLY_FAVORITE -> songs.filter {
                it.id in favIds
            }.map {
                it.toSongDomain(true)
            }
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

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

    private val _error = MutableSharedFlow<String?>()
    override val error: SharedFlow<String?>
        get() = _error.asSharedFlow()

    // Check if the ExoPlayer has media items
    private fun hasMediaItems(): Boolean = exoPlayer.currentMediaItem != null
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
            is Resource2.Error -> {
                Log.i("Message in usecase", res.message.toString())
                _error.emit(res.message)
            }
            is Resource2.Success -> {
                _localSongs.update { res.data ?: emptyList() }
                while(res.data?.isNotEmpty() == true && hasMediaItems()) {
                    if(songs.value.isNotEmpty()) {
                        resumeState()
                        break
                    }
                    delay(TIME_UPDATED_INTERVAL)
                }
                delay(TIME_UPDATED_INTERVAL * 3)
                _isLoading.update { false }
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
        val songObject = song.toSongObject()
        if(song.isFavorite) {
            if (playBy.value == ONLY_FAVORITE) {
                exoPlayer.removeMediaItem(index)
                _isPlaying.update { false }
                _currentSong.update { null }
                _currentSongIndex.update { null }
            }
            musicRepository.deleteSong(songObject = songObject)
        } else {
            musicRepository.addSong(songObject = songObject)
        }
    }

    override suspend fun toggleFavoriteSong(song: SongDomain) {
        val index = songs.value.indexOf(song)
        toggleFavoriteSong(index)
    }

    override suspend fun changePlayBy(value: PlayBy) {
        val hadCurrentSong = _currentSong.value != null
        _isPlaying.update { false }
        _currentSong.update { null }
        _currentSongIndex.update { null }
        userManager.playMusicBy(value.name)
        exoPlayer.clearMediaItems()
        if(hadCurrentSong && favSongsIds.value.isEmpty()) {
            delay(500)
        }
        _playBy.update { value }
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