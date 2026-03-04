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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.*

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .blur(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    val burstScale by animateFloatAsState(targetValue = if (startAnim) 2.5f else 0.5f, animationSpec = tween(duration, easing = LinearOutSlowInEasing), label = "burstScale")
    val burstAlpha by animateFloatAsState(targetValue = if (startAnim) 0f else 0.8f, animationSpec = tween(duration), label = "burstAlpha")

    Box(modifier = Modifier.offset(x = tileWidth * explodingTile.tile.x, y = tileHeight * explodingTile.tile.y).size(tileWidth, tileHeight), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(tileWidth).graphicsLayer(scaleX = burstScale, scaleY = burstScale, alpha = burstAlpha).background(Brush.radialGradient(colors = listOf(Color.White, Color(0xFF00E5FF).copy(alpha = 0.3f), Color.Transparent)), shape = CircleShape))
    }
}

@Composable
fun ConnectingPathCanvas(path: List<Point>, tileWidthPx: Float, tileHeightPx: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = androidx.compose.ui.graphics.Path()
        p.moveTo((path.first().x + 0.5f) * tileWidthPx, (path.first().y + 0.5f) * tileHeightPx)
        path.drop(1).forEach { p.lineTo((it.x + 0.5f) * tileWidthPx, (it.y + 0.5f) * tileHeightPx) }
        drawPath(p, Color(0xFF00E5FF).copy(alpha = 0.2f), style = Stroke(width = 32f, pathEffect = PathEffect.cornerPathEffect(16f)))
        drawPath(p, Color(0xFF00E5FF).copy(alpha = 0.5f), style = Stroke(width = 16f, pathEffect = PathEffect.cornerPathEffect(16f)))
        drawPath(p, Color.White, style = Stroke(width = 5f, pathEffect = PathEffect.cornerPathEffect(16f)))
    }
}

@Composable
fun SettingToggle(label: String, isEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF88), checkedTrackColor = Color(0xFF00FF88).copy(alpha = 0.5f))
        )
    }
}

@Composable
fun GameInfoItemModern(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.Gray.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ModernButton(
    text: String, 
    onClick: () -> Unit, 
    color: Color, 
    enabled: Boolean = true, 
    iconRes: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        enabled = enabled
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (iconRes != null) {
                Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(enabled) Color.White else Color.Gray)
        }
    }
}

@Composable
fun IconGameButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    badgeCount: Int? = null,
    size: Dp = 45.dp,
    iconSize: Dp = 28.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable(onClick = onClick, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize)
        )
        if (badgeCount != null) {
            Text(
                text = "$badgeCount",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center).padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ShuffleInfoRow(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.shuffle),
            contentDescription = "Shuffle",
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$count",
            color = Color(0xFFFFA500),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
