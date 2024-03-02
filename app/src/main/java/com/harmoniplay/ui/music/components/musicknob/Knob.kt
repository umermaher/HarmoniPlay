package com.harmoniplay.ui.music.components.musicknob

import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import com.harmoniplay.R
import kotlin.math.PI
import kotlin.math.atan2

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Knob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    maximumAngle: Float = 335f,
    currentValue: Float = limitingAngle,
    onValueChange: (Float) -> Unit,
    isFingerOnKnob: Boolean,
    onUserTouchActionChange: (isFingerOnKnob: Boolean) -> Unit
) {

    /**
     * [rotation] can directly be updated by assigning it [fixedAngle] in [pointerInteropFilter] lambda
     * if its not changing the system volume because system volume can also change by the volume buttons
     * [KeyEvent.KEYCODE_VOLUME_UP] and [KeyEvent.KEYCODE_VOLUME_DOWN] and hence the state of rotation should depend
     * on volume and volume value should based on volume button as well as user's touch on knob
     * if user try to move the knob, then volume will be changed and then based on volume value knob position will be changed
     * the above theory is just for understanding as [fixedAngle] variable is the boy that can change rotation of knob and
     * value of volume to.
     * */
    val rotationByVolume by remember(currentValue) {
        mutableFloatStateOf(
            limitingAngle + currentValue * (maximumAngle - limitingAngle)
        )
    }
    var rotationByTouch by remember {
        mutableFloatStateOf(limitingAngle)
    }

    // touch
    var touchX by remember {
        mutableFloatStateOf(0f)
    }
    var touchY by remember {
        mutableFloatStateOf(0f)
    }
    // center of image
    var centerX by remember {
        mutableFloatStateOf(0f)
    }
    var centerY by remember {
        mutableFloatStateOf(0f)
    }

    Image(
        painter = painterResource(id = R.drawable.music_knob),
        contentDescription = "Music Knob",
        modifier = modifier
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow()
                // to get the center we need to divide window bounds by two
                // if size is 100 then at fifty we get the center
                centerX = windowBounds.size.width / 2f
                centerY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event ->
                onUserTouchActionChange(true)
                touchX = event.x
                touchY = event.y
                val angle = -atan2(centerX - touchX, centerY - touchY) * (180 / PI).toFloat()
                /**
                 * [angle] value will be calculated as 0 to 180 when half passed [angle]
                 * will be from -180 to 0 degree
                 * */
                /**
                 * [angle] value will be calculated as 0 to 180 when half passed [angle]
                 * will be from -180 to 0 degree
                 * */
                when (event.action) {
                    // Only these are necessary
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        /**
                         * [angle] between 25 to -25 is need as real knob works
                         * */
                        /**
                         * [angle] between 25 to -25 is need as real knob works
                         * */
                        if (angle !in -limitingAngle..limitingAngle) {
                            /**
                             * Now we need angle from 25 to 335
                             * as we know after 180 we get -180 and to -25 we need to add 360 angle
                             * like if the angle is -150 then then we will get 210
                             */
                            /**
                             * Now we need angle from 25 to 335
                             * as we know after 180 we get -180 and to -25 we need to add 360 angle
                             * like if the angle is -150 then then we will get 210
                             */
                            val fixedAngle = if (angle in -180f..-limitingAngle) {
                                360 + angle
                            } else angle

                            rotationByTouch = fixedAngle
                            // so we go from 25 to 335
                            // lets say angle is 100 then the angle we get from the below formula is 24 percent
                            val percent = (fixedAngle - limitingAngle) / (360 - 2 * limitingAngle)
                            onValueChange(percent)
                            true
                        } else false
                    }

                    else -> {
                        onUserTouchActionChange(false)
                        false
                    }
                }
            }
            .rotate(
                degrees = if(isFingerOnKnob) {
                    rotationByTouch
                } else rotationByVolume
            ),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primaryContainer)
    )
}