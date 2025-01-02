package com.harmoniplay.utils.composables

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.harmoniplay.R
import com.harmoniplay.utils.TIME_UPDATED_INTERVAL
import kotlinx.coroutines.delay

@Composable
fun TextInput(
    inputType: InputType,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = inputType.icon,
        label = {
            Text(
                text = inputType.label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        isError = isError,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = inputType.keyboardOptions,
        visualTransformation = inputType.visualTransformation,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HPSearchBar(
    modifier: Modifier,
    isVisible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    hide: () -> Unit,
    clear: () -> Unit,
) {

    // keyboard
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(isVisible){
        if (isVisible) {
            focusRequester.requestFocus()
            delay(TIME_UPDATED_INTERVAL * 2)
            keyboard?.show()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = modifier
                        .clip(RoundedCornerShape(100))
                        .focusRequester(focusRequester)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged {
                            Log.d("TAG", "HPSearchBar: ${it.height}")
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Leading Icon
                            IconButton(
                                onClick = hide
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back Button",
                                )
                            }

                            // Placeholder
                            Box(modifier = Modifier.weight(1f)) {
                                if (value.isEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.search_dots),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        ),
                                    )
                                }
                                innerTextField()
                            }

                            // Trailing Icon
                            if (value.isNotEmpty()) {
                                IconButton(onClick = clear) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear field",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}