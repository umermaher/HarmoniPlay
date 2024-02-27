package com.harmoniplay.data.music.volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log

class VolumeChangeReceiver(
    private val onVolumeChange: () -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            onVolumeChange()
        }
    }

}