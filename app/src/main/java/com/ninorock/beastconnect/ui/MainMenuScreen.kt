package com.ninorock.beastconnect.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.AdRewardType
import com.ninorock.beastconnect.game.GameViewModel
import com.ninorock.beastconnect.game.TileType
import com.ninorock.beastconnect.ui.AdRewardDialog

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
    onSkipAd: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background
        Image(
            painter = painterResource(id = R.drawable.beast),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F2027).copy(alpha = 0.9f), Color(0xFF0F2027).copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: Header & Game Modes
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Header
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "BEAST",
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            shadow = Shadow(color = Color.Black, blurRadius = 10f),
                            letterSpacing = 4.sp
                        )
                    )
                    Text(
                        text = "CONNECT",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(color = Color.Black, blurRadius = 5f),
                            letterSpacing = 8.sp
                        ),
                        modifier = Modifier.offset(y = (-12).dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Mode Selection
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernMenuButton(
                        text = "CLASSIC MODE",
                        subtitle = "Relax and Match",
                        color = Color(0xFF0288D1),
                        onClick = onClassicClick
                    )
                    ModernMenuButton(
                        text = "CAMPAIGN",
                        subtitle = "Unlock Alpha Beasts",
                        color = Color(0xFF6A1B9A),
                        onClick = onCampaignClick
                    )
                    ModernMenuButton(
                        text = "DAILY QUEST",
                        subtitle = "New Puzzle Daily",
                        color = Color(0xFFE65100),
                        onClick = onDailyChallengeClick
                    )
                }
            }

            // RIGHT SIDE: Tile Selection
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "CUSTOMIZE TILES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Vertical List of Tiles for better space in landscape
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TileSetRow(
                                "BEASTS",
                                TileType.BEAST,
                                viewModel.monsterBitmaps.firstOrNull(),
                                currentTileType,
                                unlockedTileTypes,
                                onTileTypeClick
                            )
                            TileSetRow(
                                "FOOD",
                                TileType.FOOD,
                                viewModel.foodBitmaps.firstOrNull(),
                                currentTileType,
                                unlockedTileTypes,
                                onTileTypeClick
                            )
                            TileSetRow(
                                "GEMS",
                                TileType.GEM,
                                viewModel.gemBitmaps.firstOrNull(),
                                currentTileType,
                                unlockedTileTypes,
                                onTileTypeClick
                            )
                        }
                    }
                }
            }
        }

        // Ad Dialog Overlay
        showAdDialog?.let { rewardType ->
            AdRewardDialog(
                rewardType = rewardType,
                onWatchAd = onWatchAd,
                onSkip = onSkipAd
            )
        }
    }
}

@Composable
fun TileSetRow(
    name: String,
    type: TileType,
    previewBitmap: android.graphics.Bitmap?,
    currentType: TileType,
    unlockedTypes: Set<TileType>,
    onClick: (TileType) -> Unit
) {
    val isUnlocked = unlockedTypes.contains(type)
    val isSelected = currentType == type
    
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
    
    Surface(
        onClick = { onClick(type) },
        color = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.width(200.dp).height(60.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).alpha(if (isUnlocked) 1f else 0.3f),
                        contentScale = ContentScale.Fit
                    )
                }
                if (!isUnlocked) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_lock_lock),
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFFFFD700) else Color.White
                )
                if (isSelected) {
                    Text(
                        text = "SELECTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00FF88)
                    )
                } else if (!isUnlocked) {
                    Text(
                        text = "WATCH AD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                }
            }
            
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00FF88), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun ModernMenuButton(
    text: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(260.dp)
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = color),
        color = color.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
