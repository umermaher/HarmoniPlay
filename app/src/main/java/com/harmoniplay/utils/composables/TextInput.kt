package com.harmoniplay.utils.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
//        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
//            fontFamily = FontFamily.SansSerif,
//        ),
        leadingIcon = inputType.icon,
        label = {
            Text(text = inputType.label)
        },
        isError = isError,
//        shape = RoundedCornerShape(10.dp),
//        colors = TextFieldDefaults.colors(
//            unfocusedContainerColor = textFieldBg,
//            focusedContainerColor = textFieldBg,
//            focusedIndicatorColor = Color.Transparent,
//            unfocusedIndicatorColor = Color.Transparent,
//            disabledIndicatorColor = Color.Transparent,
//            errorIndicatorColor = Color.Transparent
//        ),
        singleLine = true,
        keyboardOptions = inputType.keyboardOptions,
        visualTransformation = inputType.visualTransformation,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RASearchBar(
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
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        enter = slideInHorizontally() + fadeIn()
    ) {
        Row(
            modifier = Modifier
//                .background(
//                    color = selectorColor,
//                    shape = RoundedCornerShape(100)
//                )
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .height(52.dp)
                    .weight(1f)
                    .background(color = Color.Transparent)
                    .defaultMinSize(minHeight = 48.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(text = stringResource(id = R.string.search_dots))
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
//                textStyle = LocalTextStyle.current.copy(color = textColor),
                leadingIcon = {
                    IconButton(
                        onClick = hide
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Button",
//                            tint = textColor
                        )
                    }
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = clear
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear field",
//                                tint = textColor
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}