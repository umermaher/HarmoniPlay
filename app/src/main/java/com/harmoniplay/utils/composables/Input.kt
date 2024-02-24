package com.harmoniplay.utils.composables

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null
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