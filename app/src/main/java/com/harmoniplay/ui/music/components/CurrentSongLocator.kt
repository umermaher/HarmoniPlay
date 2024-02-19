package com.harmoniplay.ui.music.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CurrentSongLocator(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(
                expandFrom = Alignment.CenterVertically
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.CenterVertically
            ) + fadeOut()
        ) {
            SmallFloatingActionButton(
                onClick = onClick,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Filled.GpsFixed,
                    contentDescription = "Get Current Song."
                )
            }
        }
    }
}