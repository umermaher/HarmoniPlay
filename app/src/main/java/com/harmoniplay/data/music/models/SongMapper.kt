package com.harmoniplay.data.music.models

import androidx.core.net.toUri
import com.harmoniplay.domain.music.Song
import com.harmoniplay.domain.music.SongDomain

internal fun SongDomain.toSongObject() =
    SongObject().apply {
        id = this@toSongObject.id
        title = this@toSongObject.title
        uri = this@toSongObject.uri.toString()
        artist = this@toSongObject.artist
        artworkUri = this@toSongObject.artworkUri.toString()
        duration = this@toSongObject.duration
    }

internal fun SongObject.toSongDomain() = SongDomain(
    id = id,
    title = title,
    uri = uri.toUri(),
    artist = artist,
    artworkUri = artworkUri.toUri(),
    duration = duration,
    isFavorite = true,
)

internal fun LocalSong.toSongDomain(isFavorite: Boolean = false) = Song(
    id = id,
    title = title,
    uri = uri,
    artist = artist,
    artworkUri = artworkUri,
    duration = duration,
    isFavorite = isFavorite
)