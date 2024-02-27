package com.harmoniplay.utils.composables

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.verticalScrollDisabled() =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(pass = PointerEventPass.Initial).changes.forEach {
                    val offset = it.positionChange()
                    if (abs(offset.y) > 0f) {
                        it.consume()
                    }
                }
            }
        }
    }