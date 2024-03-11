package com.harmoniplay.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import android.widget.ImageView
import androidx.compose.ui.text.toLowerCase
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.harmoniplay.R
import com.harmoniplay.ui.MainActivity
import com.harmoniplay.utils.ACTION_SHOW_MUSIC_SCREEN
import com.harmoniplay.utils.MUSIC_NOTIFICATION_ID
import com.harmoniplay.utils.MUSIC_SCREEN_RC
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@androidx.annotation.OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlayerService : Service() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private lateinit var notificationManager: PlayerNotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    private var isStartedAlready: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ServiceActions.START.toString() -> if(!isStartedAlready) {
                start()
            }
            ServiceActions.STOP.toString() -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start() {
        //audio focus attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer.setAudioAttributes(audioAttributes, true) // set audio attributes to player

        notificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            MUSIC_NOTIFICATION_ID,
            applicationContext.getString(R.string.music_notification_channel_id)
        ).setMediaDescriptionAdapter(mediaDescriptionAdapter)
            .setNotificationListener(notificationListener)
            .setChannelImportance(NotificationManagerCompat.IMPORTANCE_HIGH)
            .setSmallIconResourceId(R.drawable.ic_notification)
            .setChannelDescriptionResourceId(R.string.music_notification_channel_desc)
            .setChannelNameResourceId(R.string.music_notification_channel_name)
            .setNextActionIconResourceId(R.drawable.ic_skip_next)
            .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .build()

        // set player to notification manager
        notificationManager.apply {
            setPlayer(exoPlayer)
            setPriority(NotificationCompat.PRIORITY_MAX)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
        }

        isStartedAlready = true
    }

    private fun stop() {
        if(isStartedAlready) {
            isStartedAlready = false
            notificationManager.setPlayer(null)
            exoPlayer.stop()
        }
        stopSelf()
    }

    private val notificationListener = object: PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stop()
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    notificationId, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(notificationId, notification)
            }
        }
    }

    // Notification description adapter
    private val mediaDescriptionAdapter = object: PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return player.currentMediaItem?.mediaMetadata?.title ?: "NaN"
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            // intent to open app when clicked
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.action = ACTION_SHOW_MUSIC_SCREEN

            return PendingIntent.getActivity(
                applicationContext,
                MUSIC_SCREEN_RC,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        override fun getCurrentContentText(player: Player): CharSequence? = null

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            // try creating an Image View on the fly then get its drawable
            val iv = ImageView(applicationContext)
            iv.setImageURI(player.currentMediaItem?.mediaMetadata?.artworkUri ?: return null)

            return try {
                // get view drawable
                val bitmapDrawable = iv.drawable as BitmapDrawable
                bitmapDrawable.bitmap
            } catch (e: Exception) {
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_music)?.let {
                    // Convert the drawable to a Bitmap
                    val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)
                    return bitmap
                }
                return null
            }
        }
    }
}