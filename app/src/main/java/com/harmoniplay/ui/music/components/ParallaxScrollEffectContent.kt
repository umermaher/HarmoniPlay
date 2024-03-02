package com.harmoniplay.ui.music.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.harmoniplay.R

@Composable
fun ParallaxScrollEffectContent(
    windowAndShelfOffset: Float,
    moonOffset: Float,
    musicSoundIconsOffset: Float,
) {
    // image should be crop little bit to show more parallax effect
    // means image height should be known
    // height must be assign with respect to width
    // and the aspect ratio is 2/3 i.e. width of image will be 1.5 of height
    val imageHeight = (LocalConfiguration.current.screenWidthDp * (2f/3f)).dp

    Box(
        modifier = Modifier
            .clipToBounds()
            .fillMaxWidth()
            .height(imageHeight)
    ) {

        // content will slide up so alignment should be bottom
        Image(
            painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_1),
            contentDescription = "Window and shelf",
            contentScale = ContentScale.Inside,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = windowAndShelfOffset
                }
        )

        Image(
            painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_4),
            contentDescription = "Moon",
            contentScale = ContentScale.Inside,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = moonOffset
                }
        )

        Image(
            painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_2),
            contentDescription = "Music Floating Icons",
            contentScale = ContentScale.Inside,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = musicSoundIconsOffset
                }
        )

        Image(
            painter = painterResource(id = R.drawable.ic_parallax_scroll_effect_3),
            contentDescription = "A boy and furniture",
            contentScale = ContentScale.Inside,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        )
    }
}