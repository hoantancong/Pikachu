package com.ninorock.beastconnect.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.*

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onClassicClick: () -> Unit,
    onCampaignClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    currentTileType: TileType,
    unlockedTileTypes: Set<TileType>,
    onTileTypeClick: (TileType) -> Unit,
    showAdDialog: AdRewardType?,
    onWatchAd: () -> Unit,
    onSkipAd: () -> Unit,
    onLeaderboardClick: () -> Unit
) {
    // Tinh toán mật độ điểm ảnh tùy chỉnh để UI luôn tỷ lệ với màn hình vật lý
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    
    // Sử dụng chiều rộng thực tế (pixels) chia cho một con số chuẩn (ví dụ 800dp cho landscape)
    // Điều này giúp 1dp luôn chiếm cùng một tỷ lệ phần trăm màn hình bất kể cài đặt hệ thống.
    val designWidth = 800f 
    val customDensityValue = displayMetrics.widthPixels / designWidth
    val customDensity = Density(density = customDensityValue, fontScale = 1f)

    CompositionLocalProvider(LocalDensity provides customDensity) {
        val state = viewModel.state
        
        // Title Animation Values
        val infiniteTransition = rememberInfiniteTransition(label = "titleAnimation")
        
        val shimmerOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerOffset"
        )

        val titleScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "titleScale"
        )

        val shadowBlur by infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shadowBlur"
        )

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Dynamic Background with Crossfade for smooth transition
            Crossfade(targetState = currentTileType, animationSpec = tween(1000), label = "backgroundFade") { tileType ->
                val bgRes = when (tileType) {
                    TileType.BEAST -> R.drawable.beast_main
                    TileType.FOOD -> R.drawable.food_tile
                    TileType.GEM -> R.drawable.gem_tile
                }
                Image(
                    painter = painterResource(id = bgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F2027).copy(alpha = 0.8f), Color(0xFF203A43).copy(alpha = 0.5f), Color(0xFF0F2027).copy(alpha = 0.9f))
                        )
                    )
            )

            // Settings Button
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { viewModel.showSettings() },
                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Text Shimmer Gradient
                val textBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFF9C4), Color(0xFFFFD700)),
                    start = Offset(shimmerOffset, 0f),
                    end = Offset(shimmerOffset + 150f, 150f)
                )

                Text(
                    text = "BEAST CONNECT",
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer(scaleX = titleScale, scaleY = titleScale),
                    style = TextStyle(
                        fontSize = 52.sp, // Không cần nonScalableSp vì Density đã khóa fontScale = 1
                        fontWeight = FontWeight.Black,
                        brush = textBrush,
                        shadow = Shadow(
                            color = Color(0xFFFFD700).copy(alpha = 0.6f),
                            blurRadius = shadowBlur,
                            offset = Offset(0f, 0f)
                        ),
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Main Buttons
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MainMenuButton(
                        text = stringResource(R.string.classic),
                        color = Color(0xFF0288D1),
                        onClick = { viewModel.onClassicClick(onClassicClick) },
                        icon = Icons.Default.PlayArrow
                    )
                    
                    MainMenuButton(
                        text = stringResource(R.string.campaign),
                        color = Color(0xFF6A1B9A),
                        onClick = { viewModel.onCampaignClick(onCampaignClick) }
                    )
                    
                    if (!state.isDailyChallengeCompleted) {
                        MainMenuButton(
                            text = stringResource(R.string.daily_quest),
                            color = Color(0xFFE65100),
                            onClick = { viewModel.onDailyChallengeClick(onDailyChallengeClick) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Leaderboard Button
                    IconButton(
                        onClick = onLeaderboardClick,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.leaderboard),
                            contentDescription = "Leaderboard",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.customize_tiles).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallTileChoice(
                                type = TileType.BEAST,
                                bitmap = viewModel.monsterBitmaps.firstOrNull(),
                                isSelected = currentTileType == TileType.BEAST,
                                isUnlocked = unlockedTileTypes.contains(TileType.BEAST),
                                onClick = { onTileTypeClick(TileType.BEAST) }
                            )
                            SmallTileChoice(
                                type = TileType.FOOD,
                                bitmap = viewModel.foodBitmaps.firstOrNull(),
                                isSelected = currentTileType == TileType.FOOD,
                                isUnlocked = unlockedTileTypes.contains(TileType.FOOD),
                                onClick = { onTileTypeClick(TileType.FOOD) }
                            )
                            SmallTileChoice(
                                type = TileType.GEM,
                                bitmap = viewModel.gemBitmaps.firstOrNull(),
                                isSelected = currentTileType == TileType.GEM,
                                isUnlocked = unlockedTileTypes.contains(TileType.GEM),
                                onClick = { onTileTypeClick(TileType.GEM) }
                            )
                        }
                    }
                }
            }

            // Dialogs
            showAdDialog?.let { MessageBoxManager.AdReward(it, onWatchAd, onSkipAd) }
            if (state.showSettings) {
                MessageBoxManager.Settings(
                    onDismiss = { viewModel.hideSettings() },
                    isSoundEnabled = state.isSoundEnabled,
                    onToggleSound = { viewModel.toggleSound() },
                    isVibrationEnabled = state.isVibrationEnabled,
                    onToggleVibration = { viewModel.toggleVibration() }
                )
            }
        }
    }
}

@Composable
fun MainMenuButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text.uppercase(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun SmallTileChoice(
    type: TileType,
    bitmap: android.graphics.Bitmap?,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUnlocked) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.4f))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(32.dp).alpha(if (isUnlocked) 1f else 0.3f),
                contentScale = ContentScale.Fit
            )
        }
        
        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
