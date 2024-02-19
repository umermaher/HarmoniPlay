package com.harmoniplay.utils.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ehsanmsz.mszprogressindicator.progressindicator.BallClipRotatePulseProgressIndicator

@Composable
fun ProgressIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BallClipRotatePulseProgressIndicator()
    }
}