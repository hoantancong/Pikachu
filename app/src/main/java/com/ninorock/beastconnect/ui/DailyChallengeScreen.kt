package com.ninorock.beastconnect.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.*

@Composable
fun DailyChallengeScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    
    // Virtual Density setup
    val designWidth = 800f 
    val customDensityValue = displayMetrics.widthPixels / designWidth
    val customDensity = Density(density = customDensityValue, fontScale = 1f)

    CompositionLocalProvider(LocalDensity provides customDensity) {
        val state = viewModel.state
        val tileSpacing = 1.dp
        val activity = context as? Activity
        val density = LocalDensity.current

        DisposableEffect(Unit) {
            val window = activity?.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        val quitAction = {
            viewModel.quitGame(activity, onBack)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    )
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {

                // Sidebar
                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    //Text(stringResource(R.string.daily_challenge), color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    GameInfoItemModern(stringResource(R.string.daily_challenge).uppercase(), "", Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    GameInfoItemModern(stringResource(R.string.score).uppercase(), "${state.score}", Color(0xFF00FF88))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    ShuffleInfoRow(count = state.shufflesLeft)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconGameButton(
                            iconRes = R.drawable.searchbtn,
                            onClick = { viewModel.showHint() },
                            contentDescription = "Hint",
                            badgeCount = state.hintsLeft,
                            size = 42.dp,
                            iconSize = 30.dp
                        )

                        IconGameButton(
                            iconRes = R.drawable.pausebtn,
                            onClick = { viewModel.togglePause() },
                            contentDescription = "Pause",
                            size = 42.dp,
                            iconSize = 24.dp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Game Board
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp), // Tạo biên trái và phải (tối thiểu 10 pixel ~ 16dp)
                    contentAlignment = Alignment.Center
                ) {
                    val boardWidth = maxWidth
                    val boardHeight = maxHeight
                    val tileWidth = boardWidth / 16
                    val tileHeight = boardHeight / 9
                    
                    val tileWidthPx = with(density) { tileWidth.toPx() }
                    val tileHeightPx = with(density) { tileHeight.toPx() }

                    Box(modifier = Modifier.size(boardWidth, boardHeight)) {
                        viewModel.connectingPath?.let { path ->
                            ConnectingPathCanvas(path, tileWidthPx, tileHeightPx)
                        }

                        if (!state.isLevelStarting) {
                            for (y in 0 until 9) {
                                for (x in 0 until 16) {
                                    val tile = state.board[y][x]
                                    if (tile != null) {
                                        key(tile.id) {
                                            val isSelected = viewModel.firstSelectedTile?.x == x && viewModel.firstSelectedTile?.y == y
                                            val isHint = viewModel.hintTiles?.first == Point(x, y) || viewModel.hintTiles?.second == Point(x, y)
                                            
                                            BeastTileUI(
                                                tileBitmaps = viewModel.tileBitmaps,
                                                tile = tile,
                                                isSelected = isSelected,
                                                isHint = isHint,
                                                isPaused = state.isPaused,
                                                tileWidth = tileWidth,
                                                tileHeight = tileHeight,
                                                tileSpacing = tileSpacing,
                                                onClick = { viewModel.onTileClick(x, y) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        state.explodingTiles.forEach { explodingTile ->
                            key(explodingTile.tile.id, explodingTile.startTime) {
                                ExplodingTileEffect(explodingTile, tileWidth, tileHeight, viewModel.tileBitmaps)
                            }
                        }
                    }

                    // Level Intro
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.isLevelStarting,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 1.2f)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.daily_challenge).uppercase(), color = Color(0xFFFFD700), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                Text(stringResource(R.string.get_ready), color = Color.White, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            // --- UI Overlays ---

            if (state.isPaused) {
                MessageBoxManager.Pause(
                    onResume = { viewModel.togglePause() }, 
                    onQuitRequest = { viewModel.requestQuit() },
                    isSoundEnabled = state.isSoundEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    isVibrationEnabled = state.isVibrationEnabled,
                    onToggleVibration = { viewModel.toggleVibration() }
                )
            }

            if (state.showQuitConfirmDialog) {
                MessageBoxManager.QuitConfirm(onConfirm = quitAction, onCancel = { viewModel.cancelQuit() })
            }

            state.showAdDialog?.let { rewardType ->
                MessageBoxManager.AdReward(
                    rewardType = rewardType,
                    onWatchAd = { activity?.let { viewModel.watchAd(it) } },
                    onSkip = { viewModel.skipAdReward() }
                )
            }

            if (state.isGameOver && state.showAdDialog == null) {
                MessageBoxManager.GameOver(
                    score = state.score, 
                    onRetry = { viewModel.startGame(GameMode.DAILY_CHALLENGE) }, 
                    onQuitRequest = { quitAction() }
                )
            }

            if (state.isVictory) {
                MessageBoxManager.Victory(
                    score = state.score, 
                    onPlayAgain = { viewModel.startGame(GameMode.DAILY_CHALLENGE) }, 
                    onQuitRequest = { quitAction() },
                    onLeaderboardRequest = { activity?.let { viewModel.showLeaderboards(it) } },
                    title = stringResource(R.string.victory)
                )
            }
        }
    }
}
