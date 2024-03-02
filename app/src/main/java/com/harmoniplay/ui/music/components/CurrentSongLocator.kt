package com.harmoniplay.ui.music.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harmoniplay.ui.music.CurrentSongState

@Composable
fun CurrentSongLocator(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandVertically(
            expandFrom = Alignment.CenterVertically
        ) + fadeIn(),
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