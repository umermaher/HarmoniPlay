package com.harmoniplay.utils.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.harmoniplay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    text: String,
    title: String? = null,
    confirmText: String = stringResource(id = R.string.yes),
    cancelText: String = stringResource(id = R.string.no),
    onDismiss: () -> Unit = {},
    onOkClick: () -> Unit = {},
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onOkClick) {
                Text(
                    text = confirmText,
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = cancelText,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        },
        title = {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        text = {
            Text(text = text)
        },
        modifier = modifier,
    )
}