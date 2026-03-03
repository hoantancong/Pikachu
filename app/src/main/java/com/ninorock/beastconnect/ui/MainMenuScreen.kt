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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            Spacer(modifier = Modifier.height(60.dp))

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
                    fontSize = 52.nonScalableSp(),
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
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMenuButton(
                    text = stringResource(R.string.leaderboard),
                    color = Color(0xFF455A64),
                    onClick = onLeaderboardClick,
                    iconRes = R.drawable.leaderboard_icon
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.customize_tiles).uppercase(),
                        fontSize = 10.nonScalableSp(),
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
        showAdDialog?.let { AdRewardDialog(it, onWatchAd, onSkipAd) }
        if (state.showSettings) {
            SettingsDialog(
                onDismiss = { viewModel.hideSettings() },
                isSoundEnabled = state.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                isVibrationEnabled = state.isVibrationEnabled,
                onToggleVibration = { viewModel.toggleVibration() }
            )
        }
        if (state.showCampaignMenu) {
            CampaignMenuDialog(state.savedCampaignLevel, { viewModel.continueCampaign(); onCampaignClick() }, { viewModel.requestNewCampaign() }, { viewModel.hideCampaignMenu() })
        }
        if (state.showNewGameConfirm) {
            NewGameConfirmDialog({ viewModel.startNewCampaign(); onCampaignClick() }, { viewModel.cancelNewCampaign() })
        }
    }
}

@Composable
fun MainMenuButton(text: String, color: Color, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(200.dp).height(54.dp).shadow(12.dp, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text.uppercase(), fontSize = 15.nonScalableSp(), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MiniMenuButton(text: String, color: Color, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, iconRes: Int? = null) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(130.dp).height(40.dp),
        color = color.copy(alpha = 0.8f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (iconRes != null) {
                Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text.uppercase(), fontSize = 10.nonScalableSp(), fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun SmallTileChoice(type: TileType, bitmap: android.graphics.Bitmap?, isSelected: Boolean, isUnlocked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFFFD700).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(30.dp).alpha(if (isUnlocked) 1f else 0.4f), contentScale = ContentScale.Fit)
        }
        if (!isUnlocked) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
        }
    }
}
