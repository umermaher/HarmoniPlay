package com.harmoniplay.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

fun Application.createNotificationChannel(
    channelId:String,
    channelName:String,
    channelDescription:String,
    importance: Int = NotificationManager.IMPORTANCE_HIGH
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        )

        channel.description = channelDescription
        channel.enableLights(true)
        channel.lightColor = Color.RED



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}