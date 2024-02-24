package com.harmoniplay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harmoniplay.ui.login.LoginScreen
import com.harmoniplay.ui.login.LoginViewModel
import com.harmoniplay.ui.music.MusicScreen
import com.harmoniplay.ui.music.MusicViewModel
import com.harmoniplay.utils.Screen

@Composable
fun Navigation(
    isLoggedIn: Boolean,
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination =
        if (isLoggedIn) Screen.MusicScreen.route
        else Screen.LoginScreen.route
    ) {

        composable(route = Screen.LoginScreen.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.loginState.collectAsStateWithLifecycle()
            LoginScreen(
                state = state,
                permissionDialogQueue = viewModel.permissionDialogQueue,
                results = viewModel.loginResults,
                onEvent = viewModel::onEvent,
                navigate = { route, currentRoute ->
                    rootNavController.navigate(route) {
                        popUpTo(currentRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = Screen.MusicScreen.route) {

            val musicViewModel: MusicViewModel = hiltViewModel()
            val state by musicViewModel.state.collectAsStateWithLifecycle()
            val currentSongState by musicViewModel.currentSongState.collectAsStateWithLifecycle()
            val currentSongProgress = musicViewModel.currentSongProgress
            val searchBarText by musicViewModel.searchBarText.collectAsStateWithLifecycle()
            MusicScreen(
                state = state,
                searchBarText = searchBarText,
                currentSongState = currentSongState,
                currentSongProgress = currentSongProgress,
                result = musicViewModel.musicResult,
                onEvent = musicViewModel::onEvent,
            )
        }

    }
}