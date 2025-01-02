package com.harmoniplay.data.music.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class SongObject: RealmObject {
    @PrimaryKey var id: Long = 0L
    var title: String = ""
    var uri: String = ""
    var artist: String = ""
    var artworkUri: String = ""
    var duration: Int = 0
}