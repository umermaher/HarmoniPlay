package com.harmoniplay.data.music

import com.harmoniplay.data.music.db.model.SongObject
import com.harmoniplay.utils.Resource2
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun fetchSongs(): Resource2<List<LocalSong>>
    fun getFavoriteSongs(): Flow<List<SongObject>>
    fun getFavoriteSongsIds(): Flow<List<Long>>
    suspend fun addSong(songObject: SongObject)
    suspend fun deleteSong(songObject: SongObject)

    suspend fun delete()
}