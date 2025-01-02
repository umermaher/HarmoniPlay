package com.harmoniplay.domain.music

import com.harmoniplay.domain.utils.DataError
import com.harmoniplay.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    val playBy: StateFlow<PlayBy>
    val songs: StateFlow<List<Song>>
    suspend fun fetchSongs(): Result<Unit, DataError.Local>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getFavoriteSongsIds(): Flow<List<Long>>
    suspend fun addSong(song: Song)
    suspend fun deleteSong(song: Song)
    suspend fun delete()

    // Music Settings
    fun playMusicBy(playBy: PlayBy)
    fun getPlayMusicBy(): PlayBy
}