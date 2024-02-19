package com.harmoniplay.ui.login

import android.Manifest
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harmoniplay.R
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.utils.Screen
import com.harmoniplay.utils.composables.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userManager: UserManager
): ViewModel() {
    var loginState by mutableStateOf(LoginState())

    private val resultChannel = Channel<LoginResult>()
    val loginResults = resultChannel.receiveAsFlow()

    val permissionDialogQueue = mutableStateListOf<String>()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.LoginEvent -> {
                loginState = loginState.copy(nameError = false)
                login()
            }

            is LoginUiEvent.Name -> loginState = loginState.copy(name = event.value)
            LoginUiEvent.DismissPermissionDialog -> permissionDialogQueue.removeFirst()
            is LoginUiEvent.OnPermissionResult -> onPermissionResult(
                event.permission, event.isGranted
            )
        }
    }

    private fun login() {
        viewModelScope.launch {
            if(areInputsValidated()) {
                userManager.saveUser(
                    loginState.name
                )
                resultChannel.send(
                    LoginResult.CheckPermissionsThenNavigate(Screen.MusicScreen.route, Screen.LoginScreen.route)
                )
            }
        }
    }

    private fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if(!isGranted && !permissionDialogQueue.contains(permission)) {
            permissionDialogQueue.add(permission)
        }
        if(isGranted) {
            val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else null

            if(permission == storagePermission || permission == notificationPermission) {
                viewModelScope.launch {
                    resultChannel.send(
                        LoginResult.CheckPermissionsThenNavigate(Screen.MusicScreen.route, Screen.LoginScreen.route)
                    )
                }
            }
        }
    }

    private suspend fun areInputsValidated(): Boolean {
        return if(loginState.name.isEmpty()) {
            loginState = loginState.copy(nameError = true)
            resultChannel.send(LoginResult.Message(
                UiText.StringResource(R.string.fields_should_not_be_empty, emptyList())
            ))
            false
        } else if(loginState.name.isEmpty()) {
            loginState = loginState.copy(nameError = true)
            resultChannel.send(LoginResult.Message(
                UiText.StringResource(R.string.name_required, emptyList())
            ))
            false
        } else true
    }
}

