package com.harmoniplay.domain.music

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MusicUseCase {
    val songs: StateFlow<List<Song>>
    val currentSongIndex: StateFlow<Int?>
    val currentSong: StateFlow<Song?>
    val isPlaying: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val playBy: StateFlow<PlayBy>
    val error: SharedFlow<String?>
    suspend fun getAllSongs()
//    suspend fun getAllFavSongsIds(): Flow<List<Long>>
    fun selectSong(index: Int)
    fun selectSong(id: Long)
    suspend fun toggleFavoriteSong(index: Int)
    suspend fun toggleFavoriteSong(song: SongDomain)
    fun getCurrentSongPosition(): Float
    fun play()
    fun pause()
    fun skipToNextSong()
    fun skipToPreviousSong()
    fun changeProgress(value: Long)
    suspend fun changePlayBy(value: PlayBy)
    fun shutDownPlayer()
}