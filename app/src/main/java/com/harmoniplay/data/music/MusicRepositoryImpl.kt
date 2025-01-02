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
import com.harmoniplay.data.music.models.LocalSong
import com.harmoniplay.data.music.models.SongObject
import com.harmoniplay.data.music.models.toSongDomain
import com.harmoniplay.data.music.models.toSongObject
import com.harmoniplay.domain.music.MusicRepository
import com.harmoniplay.domain.music.PlayBy
import com.harmoniplay.domain.music.PlayBy.ALL
import com.harmoniplay.domain.music.PlayBy.ONLY_FAVORITE
import com.harmoniplay.domain.music.Song
import com.harmoniplay.domain.music.SongDomain
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.domain.utils.DataError
import com.harmoniplay.utils.ALBUM_ARTWORK_CONTENT_URI
import com.harmoniplay.utils.hasPermission
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okio.use
import java.util.Locale
import com.harmoniplay.domain.utils.Result

class MusicRepositoryImpl(
    private val context: Context,
    private val cursor: Cursor?,
    private val realm: Realm,
    private val userManager: UserManager,
): MusicRepository {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playBy = MutableStateFlow(getPlayMusicBy())
    override val playBy: StateFlow<PlayBy>
        get() = _playBy.asStateFlow()

    private val _songs = MutableStateFlow<List<LocalSong>>(emptyList())
    override val songs = combine(
        _songs,
        getFavoriteSongsIds(),
        playBy
    ) { songs, favIds, pb ->
        val songsDomain = when (pb) {
            PlayBy.ALL -> songs.map {
                it.toSongDomain(isFavorite = it.id in favIds)
            }
            PlayBy.ONLY_FAVORITE -> songs.filter {
                it.id in favIds
            }.map {
                it.toSongDomain(isFavorite = true)
            }
        }
        songsDomain
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())


    override suspend fun fetchSongs(): Result<Unit, DataError.Local> {
        if( !context.hasPermission(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) ) {
            return Result.Error(DataError.Local.PERMISSION_REQUIRED)
        }

        return withContext(Dispatchers.IO) {
            val songs = mutableListOf<LocalSong>()

            cursor?.use { cursor ->
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
                    var name = cursor.getString(nameColumn) ?: continue

                    val nameInLowerCase = name.lowercase()
                    // other audio files should not be included, skipping files by continue statement
                    val shouldContinueLoop =
                        nameInLowerCase.contains("aud") || nameInLowerCase.contains("slack") || !name.contains(
                            ".mp3"
                        )
                    if (shouldContinueLoop)
                        continue

                    val id = cursor.getLong(columnId)

                    val artistName = cursor.getString(artistColumn) ?: "Unknown"
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val albumId = cursor.getLong(albumColumn)

                    // Song uri
                    val uri = ContentUris.withAppendedId(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                        } else
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // album artwork uri
//                      val albumArtworkUri = ContentUris.withAppendedId(Uri.parse(ALBUM_ARTWORK_CONTENT_URI), albumId)
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
                            size = size,
                            artworkUri = albumArtworkUri,
                            duration = duration,
                        )
                    )
                    count++
                    if (count == 300)
                        break
                }
                Log.i("number of songs", songs.size.toString())
            }

            if (songs.isEmpty())
                Result.Error(DataError.Local.DISK_EMPTY)
            else {
                val list = songs.toList()
                _songs.update { list }
                Result.Success(Unit)
            }
        }
    }

    override fun getFavoriteSongs(): Flow<List<SongDomain>> = realm.query<SongObject>()
        .asFlow()
        .map { result ->
            result.list.map { it.toSongDomain() }
        }

    override fun getFavoriteSongsIds(): Flow<List<Long>> = realm.query<SongObject>()
        .asFlow()
        .map { result ->
            result.list.map {
                it.id
            }
        }

    override suspend fun addSong(song: SongDomain) {
        realm.write {
            this.copyToRealm(instance = song.toSongObject(), updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun deleteSong(song: SongDomain) {
        realm.write {
            val latestSong = copyToRealm(instance = song.toSongObject(), updatePolicy = UpdatePolicy.ALL) // Assuming songObject is unmanaged
            delete(latestSong)
        }
    }

    override suspend fun delete() = Realm.deleteRealm(
        configuration = RealmConfiguration.create(
            schema = setOf(SongObject::class)
        )
    )

    override fun playMusicBy(playBy: PlayBy) {
        _playBy.update { playBy }
        userManager.playMusicBy(playBy.name)
    }

    override fun getPlayMusicBy(): PlayBy {
        return when(userManager.getPlayMusicBy()) {
            ALL.toString() -> ALL
            else -> ONLY_FAVORITE
        }
    }

}