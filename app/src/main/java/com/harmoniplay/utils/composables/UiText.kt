package com.harmoniplay.utils.composables

import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(
        val value: String
    ): UiText()

    data class StringResource(
        @StringRes val value: Int,
        val args: List<Any>
    ): UiText()
}