package com.harmoniplay.utils

sealed class Screen(val route:String) {
    object LoginScreen : Screen("login_screen")
    object MusicScreen: Screen("music_screen")
}