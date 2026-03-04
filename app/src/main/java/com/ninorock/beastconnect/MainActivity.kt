package com.ninorock.beastconnect

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
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
        
        // Initialize Play Games SDK
        PlayGamesSdk.initialize(this)
        
        // 1. Cấu hình hiển thị tràn Notch/Cutout (cho Android 9+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 2. Thiết lập chế độ Full Screen Immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()
        
        enableEdgeToEdge()
        setContent {
            BeastConnectTheme {
                BeastConnectApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE giúp vuốt để hiện thanh hệ thống tạm thời và tự ẩn sau đó
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Ẩn cả Status Bar và Navigation Bar
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun BeastConnectApp() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel()
    val activity = LocalContext.current as? Activity
    
    // Tự động đăng nhập Google Play Games khi ứng dụng khởi chạy
    activity?.let { viewModel.initializeLeaderboard(it) }
    
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
                onSkipAd = { viewModel.skipAdReward() },
                onLeaderboardClick = { activity?.let { viewModel.showLeaderboards(it) } }
            )
        }
        composable(Screen.ClassicGame.route) {
            BackHandler(enabled = true) {
                // User must use Pause Menu to Quit
            }
            ClassicGameScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Screen.CampaignGame.route) {
            BackHandler(enabled = true) {
                // Disable back button
            }
            CampaignGameScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
        composable(Screen.DailyChallenge.route) {
            BackHandler(enabled = true) {
                // Disable back button
            }
            DailyChallengeScreen(onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
    }
}
