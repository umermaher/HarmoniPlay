package com.harmoniplay.di

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.media3.exoplayer.ExoPlayer
import com.harmoniplay.data.music.repository.MusicRepository
import com.harmoniplay.data.music.repository.MusicRepositoryImpl
import com.harmoniplay.data.music.volume.AndroidMusicStreamVolumeManager
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.domain.music.MusicManager
import com.harmoniplay.data.music.AndroidMusicManager
import com.harmoniplay.domain.volume.StreamVolumeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.realm.kotlin.Realm

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelScopedModule {

    @Provides
    @ViewModelScoped
    fun provideMusicRepository(
        @ApplicationContext context: Context,
        cursor: Cursor?,
        realm: Realm
    ): MusicRepository = MusicRepositoryImpl(context, cursor, realm)

    @Provides
    @ViewModelScoped
    fun provideMusicUseCase(
        musicRepository: MusicRepository,
        settingsRepository: UserManager,
        exoPlayer: ExoPlayer
    ): MusicManager = AndroidMusicManager(
        musicRepository = musicRepository,
        userManager = settingsRepository,
        exoPlayer = exoPlayer
    )

    @Provides
    @ViewModelScoped
    fun provideAudioCursor(
        @ApplicationContext context: Context,
    ): Cursor? {
        val mediaStoreUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        //Define projection
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID,
        )
        //Order
        val order = MediaStore.Audio.Media.DATE_ADDED + " DESC"
        return context.contentResolver.query(
            mediaStoreUri, projection, null, null, order
        )
    }

    @Provides
    @ViewModelScoped
    fun provideVolumeStreamManager(
        @ApplicationContext context: Context,
    ): StreamVolumeManager = AndroidMusicStreamVolumeManager(context)

}