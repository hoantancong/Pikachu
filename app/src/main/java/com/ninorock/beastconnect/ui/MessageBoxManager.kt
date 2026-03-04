package com.ninorock.beastconnect.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ninorock.beastconnect.R
import com.ninorock.beastconnect.game.*
import kotlinx.coroutines.delay

@Composable
fun MessageBoxContainer(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    
    // Virtual Density setup (đồng nhất với các màn hình chính)
    // Cố định kích thước bằng cách sử dụng tỷ lệ màn hình và khóa fontScale = 1f
    val designWidth = 800f 
    val customDensityValue = displayMetrics.widthPixels / designWidth
    val customDensity = Density(density = customDensityValue, fontScale = 1f)

    CompositionLocalProvider(LocalDensity provides customDensity) {
        content()
    }
}

object MessageBoxManager {

    @Composable
    fun AdReward(rewardType: AdRewardType, onWatchAd: () -> Unit, onSkip: () -> Unit) {
        MessageBoxContainer {
            Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp).heightIn(max = 350.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
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

                        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = description, color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onWatchAd, 
                            modifier = Modifier.fillMaxWidth().height(48.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.watch_ad_button), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onSkip, modifier = Modifier.height(40.dp)) {
                            Text(stringResource(R.string.skip).uppercase(), color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun UnlockTileDialog(
        tileType: TileType,
        bitmaps: List<android.graphics.Bitmap>,
        onUnlock: () -> Unit,
        onCancel: () -> Unit
    ) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onCancel, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(300.dp),
                    border = BorderStroke(2.dp, Color(0xFFFFD700))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val title = when (tileType) {
                            TileType.FOOD -> stringResource(R.string.unlock_food_title)
                            TileType.GEM -> stringResource(R.string.unlock_gem_title)
                            else -> ""
                        }
                        Text(
                            text = title.uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show samples
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            bitmaps.take(4).forEach { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp).padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel button
                            Button(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                            ) {
                                Text(stringResource(R.string.skip).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            // Unlock button with icon
                            Button(
                                onClick = onUnlock,
                                modifier = Modifier.weight(1.5f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.videoad),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.watch_ad_button).uppercase(), color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Pause(
        onResume: () -> Unit, 
        onQuitRequest: () -> Unit,
        isSoundEnabled: Boolean,
        onToggleSound: () -> Unit,
        isVibrationEnabled: Boolean,
        onToggleVibration: () -> Unit
    ) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onResume) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.paused).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(modifier = Modifier.width(200.dp)) {
                            SettingToggle(label = stringResource(R.string.sound), isEnabled = isSoundEnabled, onToggle = onToggleSound)
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingToggle(label = stringResource(R.string.vibration), isEnabled = isVibrationEnabled, onToggle = onToggleVibration)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ModernButton(text = stringResource(R.string.resume).uppercase(), onClick = onResume, color = Color(0xFF388E3C))
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ModernButton(text = stringResource(R.string.quit).uppercase(), onClick = onQuitRequest, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Settings(
        onDismiss: () -> Unit,
        isSoundEnabled: Boolean,
        onToggleSound: () -> Unit,
        isVibrationEnabled: Boolean,
        onToggleVibration: () -> Unit
    ) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onDismiss) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.settings).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(modifier = Modifier.width(200.dp)) {
                            SettingToggle(label = stringResource(R.string.sound), isEnabled = isSoundEnabled, onToggle = onToggleSound)
                            Spacer(modifier = Modifier.height(4.dp))
                            SettingToggle(label = stringResource(R.string.vibration), isEnabled = isVibrationEnabled, onToggle = onToggleVibration)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        ModernButton(text = stringResource(R.string.close).uppercase(), onClick = onDismiss, color = Color(0xFF455A64))
                    }
                }
            }
        }
    }

    @Composable
    fun QuitConfirm(onConfirm: () -> Unit, onCancel: () -> Unit) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onCancel) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.quit_confirm_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.quit_confirm_desc), color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onConfirm, 
                                modifier = Modifier.weight(1f).height(44.dp), 
                                shape = RoundedCornerShape(8.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(stringResource(R.string.yes).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onCancel, 
                                modifier = Modifier.weight(1f).height(44.dp), 
                                shape = RoundedCornerShape(8.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(stringResource(R.string.no).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun GameOver(score: Int, onRetry: () -> Unit, onQuitRequest: () -> Unit, message: String = stringResource(R.string.game_over)) {
        MessageBoxContainer {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(260.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(message.uppercase(), color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("${stringResource(R.string.score)}: $score", color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        ModernButton(text = stringResource(R.string.retry).uppercase(), onClick = onRetry, color = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.height(10.dp))
                        ModernButton(text = stringResource(R.string.quit).uppercase(), onClick = onQuitRequest, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }

    @Composable
    fun Victory(score: Int, bonusScore: Int = 0, onPlayAgain: () -> Unit, onQuitRequest: () -> Unit, onLeaderboardRequest: () -> Unit, title: String = stringResource(R.string.victory)) {
        MessageBoxContainer {
            var showScore by remember { mutableStateOf(false) }
            var showBonus by remember { mutableStateOf(false) }
            var showTotal by remember { mutableStateOf(false) }
            var showButtons by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(500)
                showScore = true
                if (bonusScore > 0) {
                    delay(800)
                    showBonus = true
                    delay(800)
                    showTotal = true
                }
                delay(600)
                showButtons = true
            }

            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp),
                    border = BorderStroke(2.dp, Color(0xFF00FF88))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(title.uppercase(), color = Color(0xFF00FF88), fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(visible = showScore, enter = fadeIn() + expandVertically()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.score), color = Color.LightGray, fontSize = 14.sp)
                                Text("$score", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bonusScore > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedVisibility(visible = showBonus, enter = fadeIn() + expandVertically()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(R.string.time_bonus), color = Color(0xFFFFD700), fontSize = 14.sp)
                                    Text("+$bonusScore", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedVisibility(visible = showTotal, enter = fadeIn() + scaleIn()) {
                                Column {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.total), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("${score + bonusScore}", color = Color(0xFF00FF88), fontSize = 22.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AnimatedVisibility(visible = showButtons, enter = fadeIn() + slideInVertically { it / 2 }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = onLeaderboardRequest,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color(0xFF455A64), RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.leaderboard),
                                            contentDescription = "Leaderboard",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    IconButton(
                                        onClick = onPlayAgain,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color(0xFF0288D1), RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.play),
                                            contentDescription = "Play Again",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ModernButton(text = stringResource(R.string.quit).uppercase(), onClick = onQuitRequest, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LevelClear(score: Int, bonusScore: Int = 0, level: Int, onContinue: () -> Unit, onLeaderboardRequest: () -> Unit) {
        MessageBoxContainer {
            var showScore by remember { mutableStateOf(false) }
            var showBonus by remember { mutableStateOf(false) }
            var showTotal by remember { mutableStateOf(false) }
            var showButton by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(500)
                showScore = true
                if (bonusScore > 0) {
                    delay(800)
                    showBonus = true
                    delay(800)
                    showTotal = true
                }
                delay(600)
                showButton = true
            }

            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp),
                    border = BorderStroke(2.dp, Color(0xFF00E5FF))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.level_clear, level).uppercase(), color = Color(0xFF00E5FF), fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(visible = showScore, enter = fadeIn() + expandVertically()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.score), color = Color.LightGray, fontSize = 14.sp)
                                Text("$score", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bonusScore > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedVisibility(visible = showBonus, enter = fadeIn() + expandVertically()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(stringResource(R.string.time_bonus), color = Color(0xFFFFD700), fontSize = 14.sp)
                                    Text("+$bonusScore", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedVisibility(visible = showTotal, enter = fadeIn() + scaleIn()) {
                                Column {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(stringResource(R.string.total), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("${score + bonusScore}", color = Color(0xFF00FF88), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AnimatedVisibility(visible = showButton, enter = fadeIn() + slideInVertically { it / 2 }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onLeaderboardRequest,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF455A64), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.leaderboard),
                                        contentDescription = "Leaderboard",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                IconButton(
                                    onClick = onContinue,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF0288D1), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.play),
                                        contentDescription = "Continue",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CampaignMenu(savedLevel: Int, onContinue: () -> Unit, onNewGame: () -> Unit, onDismiss: () -> Unit) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onDismiss) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.campaign).uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.saved_level, savedLevel), color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ModernButton(text = stringResource(R.string.continue_campaign).uppercase(), onClick = onContinue, color = Color(0xFF388E3C))
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ModernButton(text = stringResource(R.string.new_game).uppercase(), onClick = onNewGame, color = Color(0xFFD32F2F))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismiss, modifier = Modifier.height(40.dp)) {
                            Text(stringResource(R.string.skip).uppercase(), color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun NewGameConfirm(onConfirm: () -> Unit, onCancel: () -> Unit) {
        MessageBoxContainer {
            Dialog(onDismissRequest = onCancel) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    modifier = Modifier.padding(16.dp).width(320.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.new_game_confirm_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.new_game_confirm_desc), color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onConfirm, 
                                modifier = Modifier.weight(1f).height(44.dp), 
                                shape = RoundedCornerShape(8.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(stringResource(R.string.yes).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onCancel, 
                                modifier = Modifier.weight(1f).height(44.dp), 
                                shape = RoundedCornerShape(8.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(stringResource(R.string.no).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
