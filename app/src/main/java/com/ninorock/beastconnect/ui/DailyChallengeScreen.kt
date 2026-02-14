package com.ninorock.beastconnect.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ninorock.beastconnect.game.*

@Composable
fun DailyChallengeScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state = viewModel.state
    val tileSpacing = 1.dp
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        viewModel.startGame(GameMode.DAILY_CHALLENGE)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF303F9F))
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
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("DAILY", color = Color(0xFF00E5FF), fontSize = 18.nonScalableSp(), fontWeight = FontWeight.ExtraBold)
                Text("CHALLENGE", color = Color(0xFF00E5FF), fontSize = 12.nonScalableSp(), fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                GameInfoItemModern("SCORE", "${state.score}", Color(0xFF00FF88))
                
                // No timer or shuffles in Daily Challenge
                
                Spacer(modifier = Modifier.weight(1f))
                
                ModernButton(text = "HINT: ${state.hintsLeft}", onClick = { viewModel.showHint() }, color = Color(0xFF0288D1))
                ModernButton(text = if (state.isPaused) "RESUME" else "PAUSE", onClick = { viewModel.togglePause() }, color = Color(0xFF388E3C))
                ModernButton(text = "QUIT", onClick = onBack, color = Color(0xFFD32F2F))
            }

            // Game Board
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                val boardWidth = maxWidth
                val boardHeight = maxHeight
                val tileWidth = boardWidth / 16
                val tileHeight = boardHeight / 9

                Box(modifier = Modifier.size(boardWidth, boardHeight)) {
                    viewModel.connectingPath?.let { path ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val p = androidx.compose.ui.graphics.Path()
                            p.moveTo((path.first().x + 0.5f) * tileWidth.toPx(), (path.first().y + 0.5f) * tileHeight.toPx())
                            path.drop(1).forEach { 
                                p.lineTo((it.x + 0.5f) * tileWidth.toPx(), (it.y + 0.5f) * tileHeight.toPx()) 
                            }
                            drawPath(p, Color(0xFF00E5FF), style = Stroke(width = 8f, pathEffect = PathEffect.cornerPathEffect(16f)))
                        }
                    }

                    if (!state.isLevelStarting) {
                        for (y in 0 until 9) {
                            for (x in 0 until 16) {
                                val tile = state.board[y][x]
                                if (tile != null) {
                                    DailyTile(viewModel, tile, x, y, tileWidth, tileHeight, tileSpacing)
                                }
                            }
                        }
                    }

                    state.explodingTiles.forEach { explodingTile ->
                        ExplodingTileEffectDaily(explodingTile, tileWidth, tileHeight, viewModel)
                    }
                }

                // Intro Visibility
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.isLevelStarting,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 1.2f)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DAILY CHALLENGE", color = Color(0xFFFFD700), fontSize = 36.nonScalableSp(), fontWeight = FontWeight.ExtraBold)
                            Text("SOLVE IT WITHOUT SHUFFLES!", color = Color.White, fontSize = 18.nonScalableSp())
                        }
                    }
                }
            }
        }

        // --- UI Overlays ---

        if (state.isPaused) {
            PauseDialog(onResume = { viewModel.togglePause() }, onQuit = onBack)
        }

        // In Daily Challenge, we don't have shuffles, so if no moves, it's Game Over
        if (state.isGameOver) {
            DailyGameOverDialog(score = state.score, onRetry = { viewModel.startGame(GameMode.DAILY_CHALLENGE) }, onQuit = onBack)
        }

        if (state.isVictory) {
            DailyVictoryDialog(score = state.score, onQuit = onBack)
        }
    }
}

@Composable
fun DailyGameOverDialog(score: Int, onRetry: () -> Unit, onQuit: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("NO MORE MOVES!", color = Color.Red, fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("In Daily Challenge, there are no shuffles.", color = Color.White, textAlign = TextAlign.Center, fontSize = 14.nonScalableSp())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Score: $score", color = Color.White, fontSize = 20.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = "RETRY", onClick = onRetry, color = Color(0xFF0288D1))
                Spacer(modifier = Modifier.height(8.dp))
                ModernButton(text = "QUIT", onClick = onQuit, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun DailyVictoryDialog(score: Int, onQuit: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CHALLENGE COMPLETE!", color = Color(0xFFFFD700), fontSize = 22.nonScalableSp(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Score: $score", color = Color.White, fontSize = 20.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = "BACK TO MENU", onClick = onQuit, color = Color(0xFF388E3C))
            }
        }
    }
}

@Composable
fun DailyTile(
    viewModel: GameViewModel,
    tile: Tile,
    x: Int, y: Int,
    tileWidth: androidx.compose.ui.unit.Dp, tileHeight: androidx.compose.ui.unit.Dp,
    tileSpacing: androidx.compose.ui.unit.Dp
) {
    val isSelected = viewModel.firstSelectedTile?.x == x && viewModel.firstSelectedTile?.y == y
    val isHint = viewModel.hintTiles?.first == Point(x, y) || viewModel.hintTiles?.second == Point(x, y)
    
    BeastTileUI(
        viewModel = viewModel,
        tile = tile,
        isSelected = isSelected,
        isHint = isHint,
        tileWidth = tileWidth,
        tileHeight = tileHeight,
        tileSpacing = tileSpacing,
        onClick = { viewModel.onTileClick(x, y) }
    )
}

@Composable
fun ExplodingTileEffectDaily(
    explodingTile: ExplodingTile,
    tileWidth: androidx.compose.ui.unit.Dp, tileHeight: androidx.compose.ui.unit.Dp,
    viewModel: GameViewModel
) {
    val transition = rememberInfiniteTransition(label = "explosion")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Restart), label = "scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Restart), label = "alpha"
    )

    Box(
        modifier = Modifier
            .offset(x = tileWidth * explodingTile.tile.x, y = tileHeight * explodingTile.tile.y)
            .size(tileWidth, tileHeight)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
    ) {
        BeastTileUI(
            viewModel = viewModel,
            tile = explodingTile.tile,
            isSelected = false,
            isHint = false,
            tileWidth = tileWidth,
            tileHeight = tileHeight,
            tileSpacing = 1.dp,
            onClick = {}
        )
    }
}
