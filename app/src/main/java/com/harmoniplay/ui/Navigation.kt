package com.harmoniplay.ui

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.harmoniplay.ui.login.LoginScreen
import com.harmoniplay.ui.login.LoginViewModel
import com.harmoniplay.ui.music.MusicScreen
import com.harmoniplay.ui.music.MusicViewModel
import com.harmoniplay.ui.music.current_song.CurrentSongScreen
import com.harmoniplay.utils.Screen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Navigation(
    isLoggedIn: Boolean,
) {
    val rootNavController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = rootNavController,
            startDestination =
            if (isLoggedIn) Screen.Music.route
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

            navigation(
                route = Screen.Music.route,
                startDestination = Screen.MusicScreen.route
            ) {
                composable(route = Screen.MusicScreen.route) {
                    val musicViewModel: MusicViewModel = it.sharedViewModel(rootNavController)
                    val state by musicViewModel.state.collectAsStateWithLifecycle()
                    val currentSongState by musicViewModel.currentSongState.collectAsStateWithLifecycle()
                    val searchBarText by musicViewModel.searchBarText.collectAsStateWithLifecycle()
                    MusicScreen(
                        state = state,
                        searchBarText = searchBarText,
                        currentSongState = currentSongState,
                        permissionDialogQueue = musicViewModel.permissionDialogQueue,
                        result = musicViewModel.musicResult,
                        animatedVisibilityScope = this,
                        onEvent = musicViewModel::onEvent,
                        navigate = { route ->
                            rootNavController.navigate(route)
                        }
                    )
                }

                composable(route = Screen.CurrentSongScreen.route) {
                    val musicViewModel: MusicViewModel = it.sharedViewModel(rootNavController)
                    val currentSongState by musicViewModel.currentSongState.collectAsStateWithLifecycle()
                    CurrentSongScreen(
                        state = currentSongState,
                        onEvent = musicViewModel::onEvent,
                        result = musicViewModel.currentSongResult,
                        animatedVisibilityScope = this,
                        navigateUp = {
                            rootNavController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
inline fun <reified T: ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T{
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}

const val CONTENT_BG_BOUND_KEY = "CONTENT_BG_BOUND_KEY"
const val ARTWORK_BOUND_KEY = "ARTWORK_BOUND_KEY"
const val TITLE_BOUND_KEY = "TITLE_BOUND_KEY"
const val ARTIST_NAME_BOUND_KEY = "ARTIST_NAME_BOUND_KEY"
