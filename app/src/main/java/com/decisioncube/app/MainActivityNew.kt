package com.decisioncube.app

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decisioncube.app.data.model.Screen
import com.decisioncube.app.ui.theme.FitnessCubeTheme
import com.decisioncube.app.util.ShakeDetector
import com.decisioncube.app.viewmodel.FitnessViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessCubeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitnessCubeApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessCubeApp(viewModel: FitnessViewModel = viewModel(androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
    androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
))) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vibro = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            val shakeDetector = ShakeDetector(context) {
                viewModel.rollCube()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibro.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibro.vibrate(200)
                }
            }
            shakeDetector.start()
        }
    }

    // Используем старые экраны из MainActivity.kt пока не перенесем их
    when {
        !uiState.isLoggedIn -> {
            when (uiState.currentScreen) {
                Screen.LOGIN -> {
                    LoginScreen(
                        onLogin = viewModel::login,
                        onNavigateToRegister = { viewModel.navigateToScreen(Screen.REGISTER) }
                    )
                }
                Screen.REGISTER -> {
                    RegisterScreen(
                        onRegister = viewModel::register,
                        onNavigateToLogin = { viewModel.navigateToScreen(Screen.LOGIN) }
                    )
                }
                else -> {
                    LoginScreen(
                        onLogin = viewModel::login,
                        onNavigateToRegister = { viewModel.navigateToScreen(Screen.REGISTER) }
                    )
                }
            }
        }
        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (uiState.currentScreen) {
                                    Screen.MAIN -> "Фитнес-кубик"
                                    Screen.STATISTICS -> "Статистика"
                                    Screen.FRIENDS -> "Друзья"
                                    Screen.LEADERBOARD -> "Рейтинг"
                                    else -> "Фитнес-кубик"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        actions = {
                            TextButton(onClick = viewModel::logout) {
                                Text("Выйти")
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomNavigationBar(
                        currentScreen = uiState.currentScreen,
                        onScreenSelected = viewModel::navigateToScreen,
                        todayCount = uiState.todayCount
                    )
                }
            ) { paddingValues ->
                when (uiState.currentScreen) {
                    Screen.MAIN -> MainScreenContent(uiState, viewModel, vibro, paddingValues)
                    Screen.STATISTICS -> StatisticsScreenContent(uiState, viewModel, paddingValues)
                    Screen.FRIENDS -> FriendsScreenContent(uiState, viewModel)
                    Screen.LEADERBOARD -> LeaderboardScreenContent(uiState)
                    else -> {}
                }
            }
        }
    }
}

