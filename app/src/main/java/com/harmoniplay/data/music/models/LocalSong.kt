package com.harmoniplay.data.music.models

import android.net.Uri

data class LocalSong(
    val id: Long,
    val title: String,
    val uri: Uri,
    val artist: String,
    val size: Int,
    val artworkUri: Uri,
    val duration: Int,
)