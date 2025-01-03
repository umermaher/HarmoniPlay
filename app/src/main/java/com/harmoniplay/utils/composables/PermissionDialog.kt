package com.harmoniplay.utils.composables

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.harmoniplay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
){
    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(true)
    }

    if(showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
            },
            confirmButton = {

                Button(
                    onClick = {
                        showDialog = false
                        if (isPermanentlyDeclined) {
                            onGoToAppSettingsClick()
                        } else onOkClick()
                    },
                ) {
                    Text(
                        text = if(isPermanentlyDeclined) {
                            stringResource(id = R.string.grant_permission)
                        } else stringResource(id = R.string.okay),
                        color = Color.White,
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                    )
                }

            },
            title = {
                Text(
                    text = stringResource(id = R.string.permission_required),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = permissionTextProvider.getDescription(
                        isPermanentlyDeclined = isPermanentlyDeclined,
                        context = context
                    ),
                )
            },
            modifier = modifier,
        )
    }
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean, context: Context): String
}

class NotificationPermissionTextProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean, context: Context): String =
        if(isPermanentlyDeclined) {
            context.getString(R.string.permanently_declined_permission_msg, "Notification")
        } else context.getString(R.string.notification_access_msg)

}

class MusicPermissionTextProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean, context: Context): String =
        if(isPermanentlyDeclined) {
            context.getString(R.string.permanently_declined_permission_msg, "Storage")
        } else context.getString(R.string.storage_access_for_music_msg)
}