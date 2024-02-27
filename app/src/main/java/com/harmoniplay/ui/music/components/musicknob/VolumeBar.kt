package com.harmoniplay.ui.music.components.musicknob

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun VolumeBar(
    modifier: Modifier = Modifier,
    activeBars: Int = 0,
    barCount: Int = 10
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val barWidth = remember {
            // if the maxWidth is 100 and barCount is 10 then width will be 5 and the we can place 10 bars
            // having width 5 and space 5 between each each bar
            // hence we will get 10 spaces and 10 bars
            constraints.maxWidth / (2f * barCount)
        }
        val activeColor = MaterialTheme.colorScheme.primary
        val unActiveColor = MaterialTheme.colorScheme.outline
        Canvas(modifier = modifier) {
            for (i in 0 until barCount) {
                drawRoundRect(
                    color = if(i in 0 until activeBars) {
                        activeColor
                    } else unActiveColor,
                    topLeft = Offset(x = i * barWidth * 2f + barWidth / 2f, y = 0f),
                    size = Size(barWidth, constraints.maxHeight.toFloat()),
                    cornerRadius = CornerRadius(5f, 5f)
                )
            }
        }
    }
}