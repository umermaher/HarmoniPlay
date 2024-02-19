package com.harmoniplay.data.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.harmoniplay.R
import com.harmoniplay.data.music.db.model.SongObject
import com.harmoniplay.utils.ALBUM_ARTWORK_CONTENT_URI
import com.harmoniplay.utils.Resource2
import com.harmoniplay.utils.hasPermission
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MusicRepositoryImpl(
    private val context: Context,
    private val cursor: Cursor?,
    private val realm: Realm
): MusicRepository {

    @SuppressLint("Recycle")
    override suspend fun fetchSongs(): Resource2<List<LocalSong>> {

        if(context.hasPermission(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
        )) {
            return Resource2.Error(context.getString(R.string.permission_required))
        }

        return withContext(Dispatchers.IO) {
            try {

                if (cursor != null) {
                    val songs = ArrayList<LocalSong>()

                    //cache cursor indices
                    val columnId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val artistColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val albumColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                    var count = 0
                    //clear the previous loaded before added loading again
                    while (cursor.moveToNext()) {
                        //get values of a column for a given audio file
                        var name = cursor.getString(nameColumn)
                        // other audio files should not be included
                        if (name.lowercase().contains("aud") || name.lowercase().contains("slack"))
                            continue

                        val id = cursor.getLong(columnId)

                        val artistName = cursor.getString(artistColumn)
                        val duration = cursor.getInt(durationColumn)
                        val size = cursor.getInt(sizeColumn)
                        val albumId = cursor.getLong(albumColumn)

                        // Song uri
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        //album artwork uri
//                            val albumArtworkUri = ContentUris.withAppendedId(Uri.parse(ALBUM_ARTWORK_CONTENT_URI), albumId)
                        val albumArtUri = Uri.parse(ALBUM_ARTWORK_CONTENT_URI)
                        val albumArtworkUri =
                            Uri.withAppendedPath(albumArtUri, albumId.toString())

                        //remove .mp3 extension from the song's name
                        name = name.substring(0, name.lastIndexOf("."))

                        songs.add(
                            LocalSong(
                                id = id,
                                title = name,
                                uri = uri,
                                artist = artistName,
                                artworkUri = albumArtworkUri,
                                size = size,
                                duration = duration,
                                durationInFormat = duration.formatMillisecondsToTime()
                            )
                        )
                        count++
                        if (count == 100)
                            break
                    }
                    Log.i("number of songs", songs.size.toString())

                    if (songs.isEmpty())
                        Resource2.Error(
                            context.getString(R.string.no_songs_msg)
                        )
                    else
                        Resource2.Success(songs)
                } else
                    Resource2.Error(context.getString(R.string.cant_fetch_songs_msg))

            } catch (e: Exception) {
                Log.e("fetch songs error", e.message.toString())
                Resource2.Error(e.localizedMessage)
            }
        }
    }

    override fun getFavoriteSongs(): Flow<List<SongObject>> = realm.query<SongObject>()
        .asFlow()
        .map { result ->
            result.list.toList()
        }

    override fun getFavoriteSongsIds(): Flow<List<Long>> = realm.query<SongObject>()
        .asFlow()
        .map { result ->
            result.list.toList().map {
                it.id
            }
        }

    override suspend fun addSong(songObject: SongObject) {
        realm.write {
            copyToRealm(instance = songObject, updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun deleteSong(songObject: SongObject) {
        realm.write {
            val latestSong = findLatest(songObject) ?: return@write
            delete(latestSong)
        }
    }

    override suspend fun delete() = Realm.deleteRealm(
        configuration = RealmConfiguration.create(
            schema = setOf(SongObject::class)
        )
    )


    private fun Int.formatMillisecondsToTime(): String {
        val totalSeconds = this / 1000

        val hours = if(this >= 3600000){
            totalSeconds / 3600
        } else null

        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if(hours != null){
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else String.format("%02d:%02d", minutes, seconds)
    }

}