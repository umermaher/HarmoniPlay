package com.harmoniplay.ui.login

import com.harmoniplay.ui.music.MusicEvent
import com.harmoniplay.utils.composables.UiText

data class LoginState(
    val name: String = "",
    val nameError: Boolean = false,
)

sealed interface LoginUiEvent{
    data class Name(val value: String): LoginUiEvent
    data object LoginEvent: LoginUiEvent
    data class OnPermissionResult(val permission: String, val isGranted: Boolean): LoginUiEvent
    data object DismissPermissionDialog: LoginUiEvent
}

sealed interface LoginResult {
    class CheckPermissionsThenNavigate(val route: String, val currentRoute: String): LoginResult
    class Message(val msg: UiText): LoginResult
}