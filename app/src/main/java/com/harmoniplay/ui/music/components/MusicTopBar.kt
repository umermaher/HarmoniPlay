package com.harmoniplay.ui.music.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.harmoniplay.R
import com.harmoniplay.ui.music.MusicEvent
import com.harmoniplay.ui.music.MusicState
import com.harmoniplay.utils.composables.HPSearchBar
import com.harmoniplay.utils.composables.UiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopBar(
    state: MusicState,
    searchBarText: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (MusicEvent) -> Unit,
    onExitClick: () -> Unit,
) {

    val settingsIconScale by rememberInfiniteTransition(label = "favIconAnimation")
        .animateFloat(
            initialValue = 0.8f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "favIconAnimation"
        )

    HPSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        isVisible = state.isSearchBarShowing,
        value = searchBarText,
        onValueChange = { value ->
            onEvent(MusicEvent.OnSearchTextChange(value))
        },
        hide = {
            onEvent(MusicEvent.HideSearchBar)
        },
        clear = {
            onEvent(MusicEvent.ClearSearchBar)
        },
    )
    AnimatedVisibility(
        visible = !state.isSearchBarShowing,
        exit = fadeOut()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = when (state.topBarTitle) {
                        is UiText.DynamicString -> state.topBarTitle.value
                        is UiText.StringResource -> stringResource(id = state.topBarTitle.value)
                    }
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onExitClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Menu",
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        onEvent(MusicEvent.ToggleMusicSettingsSheet)
                    }
                ) {
                    Icon(
                        modifier = Modifier.graphicsLayer {
                            scaleX = settingsIconScale
                            scaleY = settingsIconScale
                        },
                        painter = painterResource(id = R.drawable.ic_music_settings),
                        contentDescription = "Music Settings",
                    )
                }
                IconButton(
                    onClick = {
                        onEvent(MusicEvent.ShowSearchBar)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
}