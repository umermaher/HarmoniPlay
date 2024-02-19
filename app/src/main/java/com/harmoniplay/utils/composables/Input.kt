package com.harmoniplay.utils.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

sealed class InputType (
    val label: String,
    val icon:  @Composable () -> Unit,
    val keyboardOptions: KeyboardOptions,
    val visualTransformation: VisualTransformation,
) {
    data object Name: InputType(
        label = "Name",
        icon = {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = null,
//                colorFilter = ColorFilter.tint(onSurfaceColor)
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words
        ),
        visualTransformation = VisualTransformation.None
    )

}