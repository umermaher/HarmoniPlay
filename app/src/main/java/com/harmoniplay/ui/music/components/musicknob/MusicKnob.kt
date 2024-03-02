package com.harmoniplay.ui.music.components.musicknob

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.harmoniplay.ui.music.components.musicknob.Knob
import com.harmoniplay.ui.music.components.musicknob.VolumeBar
import com.harmoniplay.utils.MUSIC_BAR_COUNT
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MusicKnob(
    modifier: Modifier = Modifier,
    shouldShow: Boolean,
    volumeInPercentage: Float,
    onVolumeChange: (Float) -> Unit
) {

    val alpha by animateFloatAsState(
        targetValue = if (shouldShow) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300
        ),
        label = "Alpha animation"
    )

    val rotateX by animateFloatAsState(
        targetValue = if (shouldShow) 0f else -90f,
        animationSpec = tween(
            durationMillis = 300
        ),
        label = "rotation animation"
    )

    var isFingerOnKnob by remember {
        mutableStateOf(false)
    }

    val knobScale by animateFloatAsState(
        targetValue = if(isFingerOnKnob) {
            1f
        } else 0.91f,
        label = "Knob scale animation"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                //pivot is centered by default which mean when rotate value goes to 0f
                // content will rotate from center but not from top but knob should rotate from top center
                // hence we change transform origin with x = 0.5(center horizontally) and y=0f(center vertically)
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationX = rotateX
            }
            .alpha(alpha),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(start = 25.dp, end = 30.dp, top = 25.dp, bottom = 25.dp),
        ) {

            Knob(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = knobScale
                        scaleY = knobScale
                    },
                currentValue = volumeInPercentage,
                onValueChange = onVolumeChange,
                isFingerOnKnob = isFingerOnKnob,
                onUserTouchActionChange = {
                    isFingerOnKnob = it
                }
            )

            Spacer(modifier = Modifier.size(15.dp))

            VolumeBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                activeBars = (MUSIC_BAR_COUNT * volumeInPercentage).roundToInt(),
                barCount = MUSIC_BAR_COUNT
            )
        }
    }
}