package com.harmoniplay.utils.composables

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.harmoniplay.R

@Composable
fun LoadImageWithFallback(
    modifier: Modifier = Modifier,
    uri: Uri?,
    onSuccess: () -> Unit =  {},
    onError: () -> Unit =  {},
) {
    var isError by remember {
        mutableStateOf(true)
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = uri,
            contentDescription = "Music Artwork",
            contentScale = if(isError) {
                ContentScale.Inside
            } else ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_music),
            onError = {
                isError = true
                Log.e("Image Error",it.result.throwable.message.toString())
                onError()
            },
            onSuccess = {
                isError = false
                onSuccess()
            },
        )
    }
}