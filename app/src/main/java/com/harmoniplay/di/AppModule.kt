package com.harmoniplay.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import com.harmoniplay.data.music.models.SongObject
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.data.user.UserManagerImpl
import com.harmoniplay.utils.HARMONI_PLAY_PREFS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(HARMONI_PLAY_PREFS, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        preferences: SharedPreferences,
    ): UserManager = UserManagerImpl(
        context = context,
        sp = preferences
    )

    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context,
    ): Realm {
        return Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    SongObject::class
                )
            )
        )
    }

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
    ): ExoPlayer {
        Log.i("Initializing","Initializing expoPlayer")
        return ExoPlayer.Builder(context).build()
    }

}