package com.ninorock.beastconnect.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.*
import kotlinx.coroutines.delay

/**
 * Extension function to provide non-scalable SP.
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
fun ShimmerStreakEffect(color: Color = Color.White) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val offset = xOffset * width
        
        drawRect(
            brush = Brush.linearGradient(
                0f to Color.Transparent,
                0.45f to color.copy(alpha = 0.2f),
                0.5f to color.copy(alpha = 0.8f),
                0.55f to color.copy(alpha = 0.2f),
                1f to Color.Transparent,
                start = Offset(offset, 0f),
                end = Offset(offset + width * 0.5f, height)
            ),
            size = size
        )
    }
}

@Composable
fun BeastTileUI(
    tileBitmaps: List<android.graphics.Bitmap>,
    tile: Tile,
    isSelected: Boolean,
    isHint: Boolean,
    isPaused: Boolean,
    tileWidth: Dp,
    tileHeight: Dp,
    tileSpacing: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "tile_scale")
    val elementIndex = tile.elementIndex
    val bgColor = Color(0xffa7aaac)
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
                width = if (isSelected || isHint) 2.5.dp else 0.5.dp,
                color = when {
                    isSelected -> Color.Yellow
                    isHint -> Color.Cyan
                    else -> Color.Black.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isPaused) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .background(Brush.radialGradient(colors = listOf(glowColor.copy(alpha = 0.2f), Color.Transparent)))
            )

            if (tile.bitmapIndex < tileBitmaps.size) {
                Image(
                    bitmap = tileBitmaps[tile.bitmapIndex].asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(0.85f)
                )
            }

            if (isHint) {
                ShimmerStreakEffect()
            }
        } else {
            // Blurred/Hidden state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .blur(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExplodingTileEffect(
    explodingTile: ExplodingTile,
    tileWidth: Dp,
    tileHeight: Dp,
    tileBitmaps: List<android.graphics.Bitmap>
) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }
    
    val duration = 400
    
    val burstScale by animateFloatAsState(
        targetValue = if (startAnim) 2.5f else 0.5f,
        animationSpec = tween(duration, easing = LinearOutSlowInEasing),
        label = "burstScale"
    )
    val burstAlpha by animateFloatAsState(
        targetValue = if (startAnim) 0f else 0.8f,
        animationSpec = tween(duration),
        label = "burstAlpha"
    )

    Box(
        modifier = Modifier
            .offset(x = tileWidth * explodingTile.tile.x, y = tileHeight * explodingTile.tile.y)
            .size(tileWidth, tileHeight),
        contentAlignment = Alignment.Center
    ) {
        // Burst Effect only - Tiles disappear immediately as they are null in state.board
        Box(
            modifier = Modifier
                .size(tileWidth)
                .graphicsLayer(scaleX = burstScale, scaleY = burstScale, alpha = burstAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF00E5FF).copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun ConnectingPathCanvas(
    path: List<Point>,
    tileWidthPx: Float,
    tileHeightPx: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = androidx.compose.ui.graphics.Path()
        p.moveTo((path.first().x + 0.5f) * tileWidthPx, (path.first().y + 0.5f) * tileHeightPx)
        path.drop(1).forEach { 
            p.lineTo((it.x + 0.5f) * tileWidthPx, (it.y + 0.5f) * tileHeightPx) 
        }
        
        drawPath(p, Color(0xFF00E5FF).copy(alpha = 0.2f), style = Stroke(width = 32f, pathEffect = PathEffect.cornerPathEffect(16f)))
        drawPath(p, Color(0xFF00E5FF).copy(alpha = 0.5f), style = Stroke(width = 16f, pathEffect = PathEffect.cornerPathEffect(16f)))
        drawPath(p, Color.White, style = Stroke(width = 5f, pathEffect = PathEffect.cornerPathEffect(16f)))
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
                    AdRewardType.EXTRA_TIME -> stringResource(R.string.extra_time_title)
                    AdRewardType.EXTRA_SHUFFLES -> stringResource(R.string.extra_shuffles_title)
                    AdRewardType.EXTRA_HINTS -> stringResource(R.string.extra_hints_title)
                    AdRewardType.UNLOCK_FOOD -> stringResource(R.string.unlock_food_title)
                    AdRewardType.UNLOCK_GEM -> stringResource(R.string.unlock_gem_title)
                }
                val description = when (rewardType) {
                    AdRewardType.EXTRA_TIME -> stringResource(R.string.extra_time_desc)
                    AdRewardType.EXTRA_SHUFFLES -> stringResource(R.string.extra_shuffles_desc)
                    AdRewardType.EXTRA_HINTS -> stringResource(R.string.extra_hints_desc)
                    AdRewardType.UNLOCK_FOOD -> stringResource(R.string.unlock_food_desc)
                    AdRewardType.UNLOCK_GEM -> stringResource(R.string.unlock_gem_desc)
                }

                Text(text = title, color = Color.White, fontSize = 22.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = description, color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onWatchAd, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                    Text(stringResource(R.string.watch_ad_button), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.nonScalableSp())
                }
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.skip), color = Color.Gray, fontSize = 14.nonScalableSp())
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit, 
    onQuitRequest: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    isVibrationEnabled: Boolean,
    onToggleVibration: () -> Unit
) {
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
                Text(stringResource(R.string.paused), color = Color.White, fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                SettingToggle(label = stringResource(R.string.sound), isEnabled = isSoundEnabled, onToggle = onToggleSound)
                Spacer(modifier = Modifier.height(12.dp))
                SettingToggle(label = stringResource(R.string.vibration), isEnabled = isVibrationEnabled, onToggle = onToggleVibration)
                
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = stringResource(R.string.resume), onClick = onResume, color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = stringResource(R.string.quit), onClick = onQuitRequest, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun SettingToggle(label: String, isEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, fontSize = 16.nonScalableSp())
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00FF88),
                checkedTrackColor = Color(0xFF00FF88).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    isVibrationEnabled: Boolean,
    onToggleVibration: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.settings).uppercase(), color = Color.White, fontSize = 22.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                SettingToggle(label = stringResource(R.string.sound), isEnabled = isSoundEnabled, onToggle = onToggleSound)
                Spacer(modifier = Modifier.height(16.dp))
                SettingToggle(label = stringResource(R.string.vibration), isEnabled = isVibrationEnabled, onToggle = onToggleVibration)
                
                Spacer(modifier = Modifier.height(32.dp))
                ModernButton(text = stringResource(R.string.close).uppercase(), onClick = onDismiss, color = Color(0xFF455A64))
            }
        }
    }
}

@Composable
fun QuitConfirmDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.quit_confirm_title), color = Color.White, fontSize = 20.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.quit_confirm_desc), color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                        Text(stringResource(R.string.yes), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                        Text(stringResource(R.string.no), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(score: Int, onRetry: () -> Unit, onQuitRequest: () -> Unit, message: String = stringResource(R.string.game_over)) {
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
                Text(message, color = Color.Red, fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("${stringResource(R.string.score)}: $score", color = Color.White, fontSize = 16.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = stringResource(R.string.retry), onClick = onRetry, color = Color(0xFF0288D1))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = stringResource(R.string.quit), onClick = onQuitRequest, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun VictoryDialog(score: Int, bonusScore: Int = 0, onPlayAgain: () -> Unit, onQuitRequest: () -> Unit, onLeaderboardRequest: () -> Unit, title: String = stringResource(R.string.victory)) {
    var showScore by remember { mutableStateOf(false) }
    var showBonus by remember { mutableStateOf(false) }
    var showTotal by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showScore = true
        if (bonusScore > 0) {
            delay(1000)
            showBonus = true
            delay(1000)
            showTotal = true
        }
        delay(800)
        showButtons = true
    }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp),
            border = BorderStroke(2.dp, Color(0xFF00FF88))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title.uppercase(), color = Color(0xFF00FF88), fontSize = 28.nonScalableSp(), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = showScore, enter = fadeIn() + expandVertically()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.score), color = Color.LightGray, fontSize = 16.nonScalableSp())
                        Text("$score", color = Color.White, fontSize = 18.nonScalableSp(), fontWeight = FontWeight.Bold)
                    }
                }

                if (bonusScore > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedVisibility(visible = showBonus, enter = fadeIn() + expandVertically()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.time_bonus), color = Color(0xFFFFD700), fontSize = 16.nonScalableSp())
                            Text("+$bonusScore", color = Color(0xFFFFD700), fontSize = 18.nonScalableSp(), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = showTotal, enter = fadeIn() + scaleIn()) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.total), color = Color.White, fontSize = 20.nonScalableSp(), fontWeight = FontWeight.Bold)
                            Text("${score + bonusScore}", color = Color(0xFF00FF88), fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                AnimatedVisibility(visible = showButtons, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Column {
                        ModernButton(text = stringResource(R.string.leaderboard).uppercase(), onClick = onLeaderboardRequest, color = Color(0xFF455A64), iconRes = R.drawable.leaderboard_icon)
                        Spacer(modifier = Modifier.height(12.dp))
                        ModernButton(text = stringResource(R.string.play_again), onClick = onPlayAgain, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(12.dp))
                        ModernButton(text = stringResource(R.string.quit), onClick = onQuitRequest, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun LevelClearDialog(score: Int, bonusScore: Int = 0, level: Int, onContinue: () -> Unit, onLeaderboardRequest: () -> Unit) {
    var showScore by remember { mutableStateOf(false) }
    var showBonus by remember { mutableStateOf(false) }
    var showTotal by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showScore = true
        if (bonusScore > 0) {
            delay(1000)
            showBonus = true
            delay(1000)
            showTotal = true
        }
        delay(800)
        showButton = true
    }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp),
            border = BorderStroke(2.dp, Color(0xFF00E5FF))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.level_clear, level).uppercase(), color = Color(0xFF00E5FF), fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = showScore, enter = fadeIn() + expandVertically()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.score), color = Color.LightGray, fontSize = 16.nonScalableSp())
                        Text("$score", color = Color.White, fontSize = 18.nonScalableSp(), fontWeight = FontWeight.Bold)
                    }
                }

                if (bonusScore > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AnimatedVisibility(visible = showBonus, enter = fadeIn() + expandVertically()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.time_bonus), color = Color(0xFFFFD700), fontSize = 16.nonScalableSp())
                            Text("+$bonusScore", color = Color(0xFFFFD700), fontSize = 18.nonScalableSp(), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = showTotal, enter = fadeIn() + scaleIn()) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.total), color = Color.White, fontSize = 20.nonScalableSp(), fontWeight = FontWeight.Bold)
                            Text("${score + bonusScore}", color = Color(0xFF00FF88), fontSize = 24.nonScalableSp(), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                AnimatedVisibility(visible = showButton, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Column {
                        ModernButton(text = stringResource(R.string.leaderboard).uppercase(), onClick = onLeaderboardRequest, color = Color(0xFF455A64), iconRes = R.drawable.leaderboard_icon)
                        Spacer(modifier = Modifier.height(12.dp))
                        ModernButton(text = stringResource(R.string.continue_text).uppercase(), onClick = onContinue, color = Color(0xFF0288D1))
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignMenuDialog(savedLevel: Int, onContinue: () -> Unit, onNewGame: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.campaign).uppercase(), color = Color.White, fontSize = 22.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.saved_level, savedLevel), color = Color.LightGray, fontSize = 14.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                ModernButton(text = stringResource(R.string.continue_campaign).uppercase(), onClick = onContinue, color = Color(0xFF388E3C))
                Spacer(modifier = Modifier.height(12.dp))
                ModernButton(text = stringResource(R.string.new_game).uppercase(), onClick = onNewGame, color = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.skip).uppercase(), color = Color.Gray, fontSize = 12.nonScalableSp())
                }
            }
        }
    }
}

@Composable
fun NewGameConfirmDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.new_game_confirm_title), color = Color.White, fontSize = 20.nonScalableSp(), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.new_game_confirm_desc), color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.nonScalableSp())
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                        Text(stringResource(R.string.yes), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                        Text(stringResource(R.string.no), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
fun ModernButton(
    text: String, 
    onClick: () -> Unit, 
    color: Color, 
    enabled: Boolean = true, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        enabled = enabled
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (iconRes != null) {
                Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 12.nonScalableSp(), fontWeight = FontWeight.Bold, color = if(enabled) Color.White else Color.Gray)
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
