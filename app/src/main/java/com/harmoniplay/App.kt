package com.harmoniplay

import android.app.Application
import android.app.NotificationManager
import com.harmoniplay.utils.createNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        // Music notification channel
        createNotificationChannel(
            getString(R.string.music_notification_channel_id),
            getString(R.string.music_notification_channel_name),
            getString(R.string.music_notification_channel_desc),
            importance = NotificationManager.IMPORTANCE_LOW
        )
    }
}