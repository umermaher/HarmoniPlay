package com.harmoniplay.domain.music

import android.net.Uri

typealias SongDomain = Song


data class Song(
    val id: Long,
    val title: String,
    val uri: Uri,
    val artist: String,
    val artworkUri: Uri,
    val duration: Int,
    val isFavorite: Boolean
)