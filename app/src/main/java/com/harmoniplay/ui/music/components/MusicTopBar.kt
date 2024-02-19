package com.harmoniplay.ui.music.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.harmoniplay.R
import com.harmoniplay.ui.music.MusicEvent
import com.harmoniplay.ui.music.MusicState
import com.harmoniplay.utils.composables.RASearchBar
import com.harmoniplay.utils.composables.UiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopBar(
    state: MusicState,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (MusicEvent) -> Unit,
    onMenuIconClick: () -> Unit,
) {

    val favIconAnimation by rememberInfiniteTransition(label = "favIconAnimation")
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

    RASearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
//            .background(color = primaryThemeColor)
        ,
        isVisible = state.isSearchBarShowing,
        value = state.searchBarText,
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
    if (!state.isSearchBarShowing) {
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
                    onClick = onMenuIconClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
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
                            scaleX = favIconAnimation
                            scaleY = favIconAnimation
                        },
                        painter = painterResource(id = R.drawable.ic_music_settings),
                        contentDescription = "Music Settings",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        onEvent(MusicEvent.ShowSearchBar)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            },
            scrollBehavior = scrollBehavior,
//            colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryThemeColor)
        )
    }
}