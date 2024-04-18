package com.harmoniplay.domain.volume

import kotlinx.coroutines.flow.StateFlow

interface StreamVolumeManager {
    val volume: StateFlow<Float>
    fun changeMusicVolume(percentage: Float)
    fun unRegisterVolumeChangeListener()
}