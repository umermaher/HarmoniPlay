package com.harmoniplay.ui.music.components.currentsong

import android.graphics.Color.parseColor
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import com.harmoniplay.R
import com.harmoniplay.ui.MainActivity
import com.harmoniplay.ui.music.CurrentSongEvent
import com.harmoniplay.ui.music.CurrentSongState
import com.harmoniplay.utils.PaletteGenerator
import com.harmoniplay.utils.composables.LoadImageWithFallback

@OptIn(ExperimentalMotionApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CurrentSongContent(
    modifier: Modifier = Modifier,
    currentSongState: CurrentSongState,
    onEvent: (CurrentSongEvent) -> Unit,
) {
    val context = LocalContext.current as MainActivity

    val motionScene = remember {
        context.resources
            .openRawResource(R.raw.current_song_motion_scene)
            .readBytes()
            .decodeToString()
    }

    val backgroundColor = MaterialTheme.colorScheme.primary
    val contentColorFromTheme = MaterialTheme.colorScheme.onPrimary
    val palette = currentSongState.contentColorPalette

    val expandCurrentSongContent = currentSongState.shouldExpandCurrentSongContent

    val songContentProgress by animateFloatAsState(
        targetValue = if (expandCurrentSongContent) 1f else 0f,
//        animationSpec = tween(durationMillis = 2300),
        label = "Current Song Progress"
    )

    val contentColor = remember(palette, expandCurrentSongContent) {
        if (expandCurrentSongContent && palette.isNotEmpty()) {
            Color(parseColor(palette["vibrant"]!!))
        } else contentColorFromTheme
    }

//    val contentColor by animateColorAsState(
//        targetValue = if (expandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["vibrant"]!!))
//        } else MaterialTheme.colorScheme.onPrimary,
//        label = "contentColor"
//    )

//    val vibrant by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["vibrant"]!!))
//        } else MaterialTheme.colorScheme.onPrimary,
//        label = "vibrantColor"
//    )
//    val darkVibrant by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["darkVibrant"]!!))
//        } else contentColor,
//        label = "darkVibrantColor"
//    )
//    val lightVibrant by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["lightVibrant"]!!))
//        } else backgroundColor,
//        label = "lightVibrantColor"
//    )
//    val domainSwatch by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["domainSwatch"]!!))
//        } else backgroundColor,
//        label = "domainSwatchColor"
//    )
//    val mutedSwatch by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["mutedSwatch"]!!))
//        } else backgroundColor,
//        label = "mutedSwatchColor"
//    )
//    val lightMuted by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["lightMuted"]!!))
//        } else backgroundColor,
//        label = "lightMutedColor"
//    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (expandCurrentSongContent && palette.isNotEmpty()) {
            Color(parseColor(palette["darkMuted"]!!))
        } else MaterialTheme.colorScheme.primary,
        label = "darkMutedSwatchColor"
    )
//    val onDarkVibrant by animateColorAsState(
//        targetValue = if(shouldExpandCurrentSongContent && palette.isNotEmpty()) {
//            Color(parseColor(palette["onDarkVibrant"]!!))
//        } else backgroundColor,
//        label = "onDarkVibrantColor"
//    )

    LaunchedEffect(key1 = currentSongState.song) {
        currentSongState.song?.let {
            val bitmap = PaletteGenerator.convertImageUriToBitmapUsingCoil(
                uri = it.artworkUri, context = context
            )

            if (bitmap == null) {
                Log.i("bitmap null", palette.toString())
            }

            onEvent(
                CurrentSongEvent.OnColorPaletteChange(
                    contentColorPalette = if (bitmap != null) {
                        PaletteGenerator.extractColorsFromBitmap(bitmap)
                    } else mapOf()
                )
            )
        }
    }

    LaunchedEffect(key1 = expandCurrentSongContent, key2 = palette) {
        val window = context.window
        window.statusBarColor = if (expandCurrentSongContent && palette.isNotEmpty()) {
            Color(parseColor(palette["darkMuted"]!!)).toArgb()
        } else backgroundColor.toArgb()
    }

    MotionLayout(
        motionScene = MotionScene(motionScene),
        progress = songContentProgress,
        modifier = modifier
    ) {

        val songTitleProperties = motionProperties(id = SONG_TITLE)
        val songArtistNameProperties = motionProperties(id = ARTIST_NAME)

        /**
         * Background
         * **/
        Box(
            modifier = Modifier
                .background(color = animatedBackgroundColor)
                .fillMaxWidth()
                .layoutId(BACKGROUND)
                .clickable {
                    onEvent(CurrentSongEvent.ToggleSongContent)
                }
        )

        /**
         * Top bar
         * **/
        TopAppBar(
            modifier = Modifier
                .layoutId(TOP_APP_BAR),
            title = { Text(text = "") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        onEvent(CurrentSongEvent.ToggleSongContent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = contentColor
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        onEvent(CurrentSongEvent.OnShareButtonClick)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = contentColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
        )

        /**
         * Artwork Image background(gradient)
         * **/
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(contentColor, Color.Transparent)
                    )
                )
                .layoutId(ARTWORK_IMAGE_BACKGROUND)
        )

        /**
         * Artwork Image
         * **/
        LoadImageWithFallback(
            modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(
                        if (expandCurrentSongContent) {
                            16.dp
                        } else 10.dp
                    )
                )
                .layoutId(ARTWORK_IMAGE),
            uri = currentSongState.song?.artworkUri
        )

        Spacer(modifier = Modifier.layoutId(FAV_ICON_AND_ARTWORK_IMAGE_SPACER))

        /**
         * Favorite Icon
         * **/
        IconButton(
            modifier = Modifier
                .layoutId(FAVORITE_ICON),
            onClick = {
                currentSongState.currentSongIndex?.let {
                    onEvent(CurrentSongEvent.OnFavoriteIconClick(it))
                }
            }
        ) {
            Icon(
                imageVector = if (currentSongState.song?.isFavorite == true) {
                    Icons.Default.Favorite
                } else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite button",
                tint = contentColor
            )
        }

        /**
         * Song title
         * **/
        Text(
            modifier = Modifier
                // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
                // applied only to the text, and not whatever is drawn below this composable (e.g. the
                // window).
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawFadedEdge(leftEdge = true)
                    drawFadedEdge(leftEdge = false)
                }
                .basicMarquee(
                    // Animate forever.
                    iterations = Int.MAX_VALUE,
                )
                .padding(start = 10.dp)
                .layoutId(SONG_TITLE),
            text = currentSongState.song?.title.toString(),
            color = contentColor,
            fontFamily = MaterialTheme.typography.labelLarge.fontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = songTitleProperties.value.fontSize("font_size")
        )

        /**
         * Artist Name
         * **/
        Text(
            modifier = Modifier
                .layoutId(ARTIST_NAME),
            text = currentSongState.song?.artist.toString(),
            color = contentColor,
//            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
//            fontWeight = FontWeight.Bold,
//            fontSize = 12.sp,
            style = MaterialTheme.typography.labelMedium,
//         songArtistNameProperties.value.fontSize("font_size")
        )

        IconButton(
            onClick = {
                onEvent(CurrentSongEvent.SkipPrevious)
            },
            modifier = Modifier
                .layoutId(PLAY_PREVIOUS)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Play Previous",
                tint = contentColor
            )
        }
        IconButton(
            onClick = {
                onEvent(CurrentSongEvent.OnPlayClick)
            },
            modifier = Modifier
                .layoutId(PLAY_CURRENT)
        ) {
            Icon(
                imageVector = if (currentSongState.isPlaying) {
                    Icons.Rounded.Pause
                } else Icons.Rounded.PlayArrow,
                contentDescription = "Play Current",
                tint = contentColor
            )
        }
        IconButton(
            onClick = {
                onEvent(CurrentSongEvent.SkipNext)
            },
            modifier = Modifier
                .layoutId(PLAY_NEXT)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Play Next",
                tint = contentColor
            )
        }
    }
}

private const val BACKGROUND = "background"
private const val TOP_APP_BAR = "top_app_bar"
private const val ARTWORK_IMAGE = "artwork_image"
private const val ARTWORK_IMAGE_BACKGROUND = "artwork_image_background"
private const val FAV_ICON_AND_ARTWORK_IMAGE_SPACER = "fav_icon_and_artwork_image_spacer"
private const val SONG_TITLE = "song_title"
private const val ARTIST_NAME = "artist_name"
private const val PLAY_NEXT = "play_next"
private const val PLAY_CURRENT = "play_current"
private const val PLAY_PREVIOUS = "play_previous"
private const val FAVORITE_ICON = "favorite_icon"