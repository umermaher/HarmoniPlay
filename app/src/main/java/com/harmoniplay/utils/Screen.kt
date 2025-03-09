package com.harmoniplay.utils

sealed class Screen(val route:String) {
    data object LoginScreen : Screen("login_screen")
    data object Music: Screen("music")
    data object MusicScreen: Screen("music_screen")
    data object CurrentSongScreen: Screen("current_song_screen")
}
