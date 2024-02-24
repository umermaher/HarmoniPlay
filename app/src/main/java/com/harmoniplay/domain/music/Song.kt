package com.harmoniplay.domain.music

import android.net.Uri
import com.harmoniplay.data.music.LocalSong
import com.harmoniplay.data.music.db.model.SongObject

typealias SongDomain = Song


data class Song(
    val id: Long,
    val title: String,
    val uri: Uri,
    val artist: String,
    val artworkUri: Uri,
    val duration: Int,
    val durationInFormat: String,
    val isFavorite: Boolean
)

internal fun SongDomain.toSongObject() =
    SongObject().apply {
        id = this@toSongObject.id
        title = this@toSongObject.title
        uri = this@toSongObject.uri.toString()
        artist = this@toSongObject.artist
        artworkUri = this@toSongObject.artworkUri.toString()
        duration = this@toSongObject.duration
        durationInFormat = this@toSongObject.durationInFormat
    }

internal fun LocalSong.toSongDomain(isFavorite: Boolean = false) = Song(
    id = id,
    title = title,
    uri = uri,
    artist = artist,
    artworkUri = artworkUri,
    duration = duration,
    durationInFormat = durationInFormat,
    isFavorite = isFavorite
)