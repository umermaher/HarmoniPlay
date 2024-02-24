package com.harmoniplay.ui.login

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harmoniplay.R
import com.harmoniplay.ui.MainActivity
import com.harmoniplay.utils.composables.InputType
import com.harmoniplay.utils.composables.MusicPermissionTextProvider
import com.harmoniplay.utils.composables.NotificationPermissionTextProvider
import com.harmoniplay.utils.composables.ObserveAsEvents
import com.harmoniplay.utils.composables.PermissionDialog
import com.harmoniplay.utils.composables.TextInput
import com.harmoniplay.utils.composables.UiText
import com.harmoniplay.utils.hasPermissions
import com.harmoniplay.utils.openAppSettings
import com.harmoniplay.utils.showToast
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginState,
    permissionDialogQueue: SnapshotStateList<String>,
    results: Flow<LoginResult>,
    onEvent: (LoginUiEvent) -> Unit,
    navigate: (route: String, currentRoute: String) -> Unit,
) {
    val activity = LocalContext.current as MainActivity

    val permissionsToRequest = remember {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else null

        if(notificationPermission != null) {
            arrayOf(storagePermission, notificationPermission)
        } else arrayOf(storagePermission)
    }

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                onEvent( LoginUiEvent.OnPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                ) )
            }
        }
    )

    ObserveAsEvents(flow = results) { res ->
        when (res) {
            is LoginResult.Message -> activity.showToast(
                msg = when(res.msg) {
                    is UiText.DynamicString -> res.msg.value
                    is UiText.StringResource -> activity.getString(res.msg.value)
                }
            )
            is LoginResult.CheckPermissionsThenNavigate -> {
                if(activity.hasPermissions(permissionsToRequest.toList())) {
                    navigate(res.route, res.currentRoute)
                } else {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                }
            }
        }
    }

    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                }
            )
        }
    ) { values ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
        ) {

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.4f))
                Image(
                    modifier = Modifier
                        .weight(1f),
                    painter = painterResource(id = R.drawable.img_onboard_music),
                    contentDescription = "App Icon in Login Screen",
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = activity.getString(R.string.add_details_to_contine),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(30.dp))
            }

            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextInput(
                    inputType = InputType.Name,
                    value = state.name,
                    isError = state.nameError
                ) { value ->
                    onEvent(LoginUiEvent.Name(value))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onEvent(LoginUiEvent.LoginEvent)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = R.string.continue_string),
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    permissionDialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.READ_EXTERNAL_STORAGE ->
                        MusicPermissionTextProvider()
                    Manifest.permission.READ_MEDIA_AUDIO ->
                        MusicPermissionTextProvider()
                    Manifest.permission.POST_NOTIFICATIONS ->
                        NotificationPermissionTextProvider()

                    else -> return@forEach
                },
                isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationale(
                    permission
                ),
                onDismiss = {
                    onEvent(LoginUiEvent.DismissPermissionDialog)
                },
                onOkClick = {
                    onEvent(LoginUiEvent.DismissPermissionDialog)
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = activity::openAppSettings
            )
        }
}