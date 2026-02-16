package com.ninorock.beastconnect

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ninorock.beastconnect.game.GameViewModel
import com.ninorock.beastconnect.ui.CampaignGameScreen
import com.ninorock.beastconnect.ui.ClassicGameScreen
import com.ninorock.beastconnect.ui.DailyChallengeScreen
import com.ninorock.beastconnect.ui.MainMenuScreen
import com.ninorock.beastconnect.ui.Screen
import com.ninorock.beastconnect.ui.theme.BeastConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Bật chế độ Full Screen (Immersive Mode)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        enableEdgeToEdge()
        setContent {
            BeastConnectTheme {
                BeastConnectApp()
            }
        }
    }
}

@Composable
fun BeastConnectApp() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel()
    val activity = LocalContext.current as? Activity
    
    // Xóa Scaffold padding để đạt được Full Screen thực sự
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.MainMenu.route) {
            val state = viewModel.state
            MainMenuScreen(
                viewModel = viewModel,
                onClassicClick = { navController.navigate(Screen.ClassicGame.route) },
                onCampaignClick = { navController.navigate(Screen.CampaignGame.route) },
                onDailyChallengeClick = { navController.navigate(Screen.DailyChallenge.route) },
                currentTileType = state.currentTileType,
                unlockedTileTypes = state.unlockedTileTypes,
                onTileTypeClick = { viewModel.selectTileType(it, activity) },
                showAdDialog = state.showAdDialog,
                onWatchAd = { activity?.let { viewModel.watchAd(it) } },
                onSkipAd = { viewModel.skipAdReward() }
            )
        }
        composable(Screen.ClassicGame.route) {
            ClassicGameScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Screen.CampaignGame.route) {
            CampaignGameScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Screen.DailyChallenge.route) {
            DailyChallengeScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
    }
}
