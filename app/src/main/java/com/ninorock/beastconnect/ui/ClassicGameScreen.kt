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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ninorock.beastconnect.game.*

/**
 * Extension function to provide non-scalable SP.
 * It calculates the SP value based on the current density but ignores the fontScale.
 */
@Composable
fun Int.nonScalableSp(): TextUnit {
    return (this.toFloat() / LocalDensity.current.fontScale).sp
}

@Composable
fun Float.nonScalableSp(): TextUnit {
    return (this / LocalDensity.current.fontScale).sp
}

@Composable
fun ClassicGameScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state = viewModel.state
    val tileSpacing = 1.dp
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        viewModel.startGame()
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

            // Menu Dọc
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("BEAST", color = Color(0xFF00E5FF), fontSize = 20.nonScalableSp(), fontWeight = FontWeight.Bold)
                GameInfoItemModern("LV", "${state.level}/8", Color(0xFFFFD700))
                GameInfoItemModern("SCORE", "${state.score}", Color(0xFF00FF88))
                GameInfoItemModern("TIME", formatTime(state.timeLeftSeconds), if(state.timeLeftSeconds < 60) Color.Red else Color.White)
                GameInfoItemModern("SHUFFLE", "${state.shufflesLeft}", Color(0xFFFFA500))
                Spacer(modifier = Modifier.weight(1f))
                ModernButton(text = "HINT: ${state.hintsLeft}", onClick = { viewModel.showHint() }, color = Color(0xFF0288D1))
                ModernButton(text = if (state.isPaused) "RESUME" else "PAUSE", onClick = { viewModel.togglePause() }, color = Color(0xFF388E3C))
            }

            // Game Board
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                val boardWidth = maxWidth
                val boardHeight = maxHeight
                val tileWidth = boardWidth / 16
                val tileHeight = boardHeight / 9

                Box(modifier = Modifier.size(boardWidth, boardHeight)) {
                    // Vẽ đường nối gấp khúc
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

                    // Vẽ các Tile đang tồn tại trên board (Chỉ hiện khi không ở trạng thái Intro)
                    if (!state.isLevelStarting) {
                        for (y in 0 until 9) {
                            for (x in 0 until 16) {
                                val tile = state.board[y][x]
                                if (tile != null) {
                                    ClassicTile(viewModel, tile, x, y, tileWidth, tileHeight, tileSpacing)
                                }
                            }
                        }
                    }

                    // Vẽ các Tile đang trong hiệu ứng nổ
                    state.explodingTiles.forEach { explodingTile ->
                        ExplodingTileEffect(explodingTile, tileWidth, tileHeight, viewModel)
                    }
                }

                // Hiệu ứng Intro Level
                androidx.compose.animation.AnimatedVisibility(
                    visible = state.isLevelStarting,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LEVEL ${state.level}",
                                color = Color(0xFFFFD700),
                                fontSize = 48.nonScalableSp(),
                                fontWeight = FontWeight.ExtraBold,
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "READY?",
                                color = Color.White,
                                fontSize = 24.nonScalableSp(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Ad Reward Dialog
        state.showAdDialog?.let { rewardType ->
            AdRewardDialog(
                rewardType = rewardType,
                onWatchAd = { activity?.let { viewModel.watchAd(it) } },
                onSkip = { viewModel.skipAdReward() }
            )
        }

        // Pause Dialog
        if (state.isPaused) {
            PauseDialog(
                onResume = { viewModel.togglePause() },
                onQuit = onBack
            )
        }

        // Game Over Dialog
        if (state.isGameOver && state.showAdDialog == null) {
             GameOverDialog(
                 score = state.score,
                 onRetry = { viewModel.startGame() },
                 onQuit = onBack
             )
        }

        // Victory Dialog
        if (state.isVictory) {
            VictoryDialog(
                score = state.score,
                onPlayAgain = { viewModel.startGame() },
                onQuit = onBack
            )
        }
    }
}

@Composable
fun AdRewardDialog(rewardType: AdRewardType, onWatchAd: () -> Unit, onSkip: () -> Unit) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val title = when (rewardType) {
                    AdRewardType.EXTRA_TIME -> "Hết thời gian!"
                    AdRewardType.EXTRA_SHUFFLES -> "Hết lượt trộn!"
                    AdRewardType.EXTRA_HINTS -> "Hết lượt gợi ý!"
                }
                val description = when (rewardType) {
                    AdRewardType.EXTRA_TIME -> "Xem quảng cáo để nhận thêm 5 phút chơi tiếp?"
                    AdRewardType.EXTRA_SHUFFLES -> "Xem quảng cáo để nhận thêm 5 lần trộn?"
                    AdRewardType.EXTRA_HINTS -> "Xem quảng cáo để nhận thêm 1 lượt gợi ý?"
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.nonScalableSp(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.nonScalableSp()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onWatchAd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("XEM QUẢNG CÁO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.nonScalableSp())
                }
                TextButton(onClick = onSkip) {
                    Text("Bỏ qua", color = Color.Gray, fontSize = 14.nonScalableSp())
                }
            }
        }
    }
}

@Composable
fun PauseDialog(onResume: () -> Unit, onQuit: () -> Unit) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(250.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PAUSED", color = Color.White, fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = "RESUME", onClick = onResume, color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = "QUIT", onClick = onQuit, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun GameOverDialog(score: Int, onRetry: () -> Unit, onQuit: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(250.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("GAME OVER", color = Color.Red, fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold)
                Text("Score: $score", color = Color.White, fontSize = 16.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = "RETRY", onClick = onRetry, color = Color(0xFF0288D1))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = "QUIT", onClick = onQuit, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun VictoryDialog(score: Int, onPlayAgain: () -> Unit, onQuit: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(250.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("VICTORY!", color = Color(0xFF00FF88), fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold)
                Text("Score: $score", color = Color.White, fontSize = 16.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = "PLAY AGAIN", onClick = onPlayAgain, color = Color(0xFF0288D1))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = "QUIT", onClick = onQuit, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun BeastTileUI(
    viewModel: GameViewModel,
    tile: Tile,
    isSelected: Boolean,
    isHint: Boolean,
    tileWidth: androidx.compose.ui.unit.Dp,
    tileHeight: androidx.compose.ui.unit.Dp,
    tileSpacing: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "tile_scale")
    val elementIndex = tile.elementIndex
    val bgColor = GameConstants.ELEMENT_BACKGROUNDS.getOrElse(elementIndex) { Color(0xFF1A1A1A) }
    val glowColor = GameConstants.ELEMENT_GLOW_COLORS.getOrElse(elementIndex) { Color.White }

    Box(
        modifier = Modifier
            .offset(x = tileWidth * tile.x, y = tileHeight * tile.y)
            .size(tileWidth, tileHeight)
            .padding(tileSpacing)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (isSelected || isHint) 2.dp else 0.5.dp,
                color = when {
                    isSelected -> Color.Yellow
                    isHint -> Color.Cyan
                    else -> Color.White.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Light Glow for foreground
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Image(
            bitmap = viewModel.monsterBitmaps[tile.bitmapIndex].asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(0.85f)
        )
    }
}

@Composable
private fun ClassicTile(
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
private fun ExplodingTileEffect(
    explodingTile: ExplodingTile,
    tileWidth: androidx.compose.ui.unit.Dp, tileHeight: androidx.compose.ui.unit.Dp,
    viewModel: GameViewModel
) {
    val transition = rememberInfiniteTransition(label = "explosion")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Restart),
        label = "scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Restart),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .offset(x = tileWidth * explodingTile.tile.x, y = tileHeight * explodingTile.tile.y)
            .size(tileWidth, tileHeight)
            .padding(1.dp)
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

@Composable
fun GameInfoItemModern(label: String, value: String, valueColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Color.Gray.copy(alpha = 0.7f), fontSize = 10.nonScalableSp(), fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 16.nonScalableSp(), fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ModernButton(text: String, onClick: () -> Unit, color: Color, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        enabled = enabled
    ) {
        Text(text, fontSize = 12.nonScalableSp(), fontWeight = FontWeight.Bold, color = if(enabled) Color.White else Color.Gray)
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
