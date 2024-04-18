package com.harmoniplay.data.music.volume

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import com.harmoniplay.domain.volume.StreamVolumeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndroidMusicStreamVolumeManager(
    private val context: Context
): StreamVolumeManager {

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val _volume = MutableStateFlow(getCurrentMusicVolumeInPercentage())
    override val volume: StateFlow<Float>
        get() = _volume.asStateFlow()

    private val volumeChangeReceiver = VolumeChangeReceiver {
        _volume.update {
            getCurrentMusicVolumeInPercentage()
        }
    }

    init {
        context.registerReceiver(
            volumeChangeReceiver,
            IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        )
    }

    override fun changeMusicVolume(percentage: Float) {
        // Ensure percentage is within the valid range (0.0 to 1.0) so desired volume can be achieved
        val adjustedPercentage = percentage.coerceIn(0.0f, 1.0f)

        // Get the maximum volume level to calculate desired volume
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        // Calculate the volume level based on the percentage
        val volume = (adjustedPercentage * maxVolume).toInt()

        // Set the music volume using the provided function
        changeMusicVolume(volume)
    }

    private fun getCurrentMusicVolumeInPercentage(): Float {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (currentVolume / maxVolume.toFloat())
    }

    // Function to change the music volume
    private fun changeMusicVolume(volume: Int) {
        // Ensure the volume level is within the valid range
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val adjustedVolume = volume.coerceIn(0, maxVolume)

        // Set the music volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustedVolume, 0)
    }

    override fun unRegisterVolumeChangeListener() {
        context.unregisterReceiver(volumeChangeReceiver)
    }
}