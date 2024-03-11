package com.harmoniplay.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.palette.graphics.Palette
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object PaletteGenerator {

    suspend fun convertImageUrlToBitmap(
        imageUrl: String,
        context: Context
    ): Bitmap? {
        return try {
            val loader = ImageLoader(context = context)
            val request = ImageRequest.Builder(context = context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val imageResult = loader.execute(request = request)
            if (imageResult is SuccessResult) {
                (imageResult.drawable as BitmapDrawable).bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("convertImageUrlToBitmap", e.message.toString())
            null
        }
    }

    suspend fun convertImageUriToBitmapUsingCoil(uri: Uri, context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false) //IMPORTANT!
                    .build()

                val result = Coil.imageLoader(context).execute(request)

                if (result is SuccessResult) {
                    (result.drawable as BitmapDrawable).bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("convertImageUriToBitmap", e.message.toString())
                null
            }
        }

    }

    suspend fun convertImageUriToBitmap(uri: Uri, context: Context): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                Log.e("convertImageUriToBitmap", e.message.toString())
                null
            }
        }
    }

    fun extractColorsFromBitmap(bitmap: Bitmap): Map<String, String> = mapOf(
        "vibrant" to parseColorSwatch(
            color = Palette.from(bitmap).generate().vibrantSwatch
        ),
        "darkVibrant" to parseColorSwatch(
            color = Palette.from(bitmap).generate().darkVibrantSwatch
        ),
        "onDarkVibrant" to parseBodyColor(
            color = Palette.from(bitmap).generate().darkVibrantSwatch?.bodyTextColor
        ),
        "lightVibrant" to parseColorSwatch(
            color = Palette.from(bitmap).generate().lightVibrantSwatch
        ),
        "domainSwatch" to parseColorSwatch(
            color = Palette.from(bitmap).generate().dominantSwatch
        ),
        "mutedSwatch" to parseColorSwatch(
            color = Palette.from(bitmap).generate().mutedSwatch
        ),
        "lightMuted" to parseColorSwatch(
            color = Palette.from(bitmap).generate().lightMutedSwatch
        ),
        "darkMuted" to parseColorSwatch(
            color = Palette.from(bitmap).generate().darkMutedSwatch
        ),
    )


    private fun parseColorSwatch(color: Palette.Swatch?): String {
        return if (color != null) {
            val parsedColor = Integer.toHexString(color.rgb)
            return "#$parsedColor"
        } else {
            "#000000"
        }
    }

    private fun parseBodyColor(color: Int?): String {
        return if (color != null) {
            val parsedColor = Integer.toHexString(color)
            "#$parsedColor"
        } else {
            "#FFFFFF"
        }
    }

}