package com.ninorock.beastconnect.game

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ninorock.beastconnect.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class GameViewModel(application: Application) : AndroidViewModel(application) {
    var state by mutableStateOf(GameState())
        private set

    private var timerJob: Job? = null
    private var gameStartJob: Job? = null
    private var matchProcessJob: Job? = null
    
    val monsterBitmaps = BitmapUtils.sliceBeastBitmap(application)
    val foodBitmaps = BitmapUtils.sliceFoodBitmap(application)
    val gemBitmaps = BitmapUtils.sliceGemBitmap(application)
    
    val currentBitmaps: List<android.graphics.Bitmap>
        get() = when (state.currentTileType) {
            TileType.BEAST -> monsterBitmaps
            TileType.FOOD -> foodBitmaps
            TileType.GEM -> gemBitmaps
        }

    val tileBitmaps: List<android.graphics.Bitmap> get() = currentBitmaps

    private val soundManager = SoundManager(application)
    private val vibrator = getSystemService(application, Vibrator::class.java)
    val adManager = AdManager(application)
    private var leaderboardManager: LeaderboardManager? = null
    
    private val prefs = application.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    var firstSelectedTile by mutableStateOf<Point?>(null)
    var connectingPath by mutableStateOf<List<Point>?>(null)
    var hintTiles by mutableStateOf<Pair<Point, Point>?>(null)

    private var isInitialized = false

    init {
        loadSettings()
        checkDailyChallengeStatus()
    }

    private fun loadSettings() {
        val unlocked = prefs.getStringSet("unlocked_tiles", setOf(TileType.BEAST.name)) ?: setOf(TileType.BEAST.name)
        val current = prefs.getString("current_tile_type", TileType.BEAST.name) ?: TileType.BEAST.name
        val savedCampaign = prefs.getInt("campaign_level", 0)
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        val vibrationEnabled = prefs.getBoolean("vibration_enabled", true)
        
        state = state.copy(
            unlockedTileTypes = unlocked.map { TileType.valueOf(it) }.toSet(),
            currentTileType = TileType.valueOf(current),
            savedCampaignLevel = savedCampaign,
            isSoundEnabled = soundEnabled,
            isVibrationEnabled = vibrationEnabled
        )
    }

    private fun saveSettings() {
        prefs.edit().apply {
            putStringSet("unlocked_tiles", state.unlockedTileTypes.map { it.name }.toSet())
            putString("current_tile_type", state.currentTileType.name)
            putBoolean("sound_enabled", state.isSoundEnabled)
            putBoolean("vibration_enabled", state.isVibrationEnabled)
            apply()
        }
    }

    private fun checkDailyChallengeStatus() {
        val lastDate = prefs.getString("last_daily_challenge_date", "")
        val today = getTodayString()
        val isCompleted = lastDate == today
        state = state.copy(isDailyChallengeCompleted = isCompleted)
    }

    private fun getTodayString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun markDailyChallengeCompleted() {
        val today = getTodayString()
        prefs.edit().putString("last_daily_challenge_date", today).apply()
        state = state.copy(isDailyChallengeCompleted = true)
    }

    fun initializeLeaderboard(activity: Activity) {
        if (leaderboardManager == null) {
            Log.d("GameViewModel", "Initializing Leaderboard and attempting sign-in")
            leaderboardManager = LeaderboardManager(activity)
            leaderboardManager?.signIn()
        }
    }

    fun selectTileType(type: TileType, activity: Activity? = null) {
        if (state.unlockedTileTypes.contains(type)) {
            // If already unlocked, show interstitial if loaded, otherwise just select
            if (activity != null && adManager.isInterstitialAdLoaded()) {
                adManager.showInterstitialAd(activity) {
                    performTileSelection(type)
                }
            } else {
                performTileSelection(type)
            }
        } else {
            // Locked tile: requires rewarded ad
            val adType = when (type) {
                TileType.FOOD -> AdRewardType.UNLOCK_FOOD
                TileType.GEM -> AdRewardType.UNLOCK_GEM
                else -> null
            }
            if (adType != null && activity != null) {
                if (adManager.isAdLoaded()) {
                    state = state.copy(showAdDialog = adType)
                } else {
                    // Ad not ready: inform user and try to load in background
                    Toast.makeText(getApplication(), "Ad is not ready yet. Please try again in a moment.", Toast.LENGTH_SHORT).show()
                    adManager.loadRewardedAd()
                }
            }
        }
    }

    private fun performTileSelection(type: TileType) {
        state = state.copy(currentTileType = type)
        saveSettings()
        soundManager.playClick()
    }

    fun onClassicClick(onNavigate: () -> Unit) {
        soundManager.playClick()
        startGame(GameMode.CLASSIC)
        onNavigate()
    }

    fun onCampaignClick(onNavigate: () -> Unit) {
        soundManager.playClick()
        if (state.savedCampaignLevel > 0) {
            state = state.copy(showCampaignMenu = true)
        } else {
            startGame(GameMode.CAMPAIGN)
            onNavigate()
        }
    }

    fun onDailyChallengeClick(onNavigate: () -> Unit) {
        soundManager.playClick()
        startGame(GameMode.DAILY_CHALLENGE)
        onNavigate()
    }

    fun hideCampaignMenu() {
        soundManager.playClick()
        state = state.copy(showCampaignMenu = false)
    }

    fun requestNewCampaign() {
        soundManager.playClick()
        state = state.copy(showNewGameConfirm = true)
    }

    fun cancelNewCampaign() {
        soundManager.playClick()
        state = state.copy(showNewGameConfirm = false)
    }

    fun startNewCampaign() {
        soundManager.playClick()
        prefs.edit().putInt("campaign_level", 0).apply()
        state = state.copy(savedCampaignLevel = 0, showNewGameConfirm = false, showCampaignMenu = false)
        startGame(GameMode.CAMPAIGN)
    }

    fun continueCampaign() {
        soundManager.playClick()
        state = state.copy(showCampaignMenu = false)
        startGame(GameMode.CAMPAIGN, resumeSaved = true)
    }

    fun startGame(mode: GameMode = GameMode.CLASSIC, resumeSaved: Boolean = false) {
        isInitialized = false
        stopAllJobs()
        
        // Reset interactive states
        firstSelectedTile = null
        connectingPath = null
        hintTiles = null
        
        val effectiveTileType = if (mode == GameMode.CAMPAIGN) TileType.BEAST else state.currentTileType

        gameStartJob = viewModelScope.launch {
            val startLevel = if (mode == GameMode.CAMPAIGN && resumeSaved) {
                state.savedCampaignLevel.coerceAtLeast(1)
            } else 1

            // Set initial time only for modes that use it
            val initialTime = if (mode == GameMode.DAILY_CHALLENGE) 0 else GameConstants.INITIAL_TIME_CLASSIC
            
            state = state.copy(
                gameMode = mode,
                board = GameLogic.initializeBoard(mode, startLevel),
                level = startLevel,
                shufflesLeft = if (mode == GameMode.DAILY_CHALLENGE) 0 else GameConstants.INITIAL_SHUFFLES,
                hintsLeft = 3,
                timeLeftSeconds = initialTime,
                isLevelStarting = true,
                isGameOver = false,
                isVictory = false,
                isLevelComplete = false,
                isShowingUnlock = false,
                isPaused = false,
                score = 0,
                bonusScore = 0,
                explodingTiles = emptyList(),
                showAdDialog = null,
                hasUsedTimeRewardInLevel = false,
                currentTileType = effectiveTileType,
                showQuitConfirmDialog = false
            )
            isInitialized = true
            soundManager.playBeginning()
            delay(2000)
            state = state.copy(isLevelStarting = false)
            if (mode != GameMode.DAILY_CHALLENGE) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        if (state.gameMode == GameMode.DAILY_CHALLENGE) return
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (state.timeLeftSeconds > 0 && !state.isPaused && !state.isGameOver && state.showAdDialog == null && !state.isLevelStarting && !state.isLevelComplete && !state.isShowingUnlock) {
                delay(1000)
                if (!state.isPaused && !state.isGameOver && state.showAdDialog == null && !state.isLevelStarting && !state.isLevelComplete && !state.isShowingUnlock) {
                    state = state.copy(timeLeftSeconds = state.timeLeftSeconds - 1)
                }
            }
            if (state.timeLeftSeconds <= 0 && state.showAdDialog == null && !state.isLevelStarting && !state.isVictory && !state.isLevelComplete && !state.isShowingUnlock) {
                handleTimeOut()
            }
        }
    }

    private fun handleTimeOut() {
        if (state.gameMode == GameMode.DAILY_CHALLENGE) return
        
        soundManager.playLose()
        if (!state.hasUsedTimeRewardInLevel && adManager.isAdLoaded()) {
            state = state.copy(showAdDialog = AdRewardType.EXTRA_TIME)
        } else {
            state = state.copy(isGameOver = true)
        }
    }

    fun onTileClick(x: Int, y: Int) {
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting || state.isLevelComplete || state.isShowingUnlock) return
        
        val currentTile = state.board[y][x] ?: return
        soundManager.playSelect()
        hintTiles = null

        val first = firstSelectedTile
        if (first == null) {
            firstSelectedTile = Point(x, y)
        } else {
            if (first.x == x && first.y == y) {
                firstSelectedTile = null
                return
            }

            val tile1 = state.board[first.y][first.x]
            val tile2 = state.board[y][x]
            
            if (tile1?.bitmapIndex == tile2?.bitmapIndex) {
                val path = GameLogic.checkPath(state.board, first, Point(x, y))
                if (path != null) {
                    handleMatch(first, Point(x, y), path)
                } else {
                    triggerErrorFeedback()
                    firstSelectedTile = Point(x, y)
                }
            } else {
                firstSelectedTile = Point(x, y)
            }
        }
    }

    private fun triggerErrorFeedback() {
        soundManager.playWrong()
        if (state.isVibrationEnabled && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    private fun handleMatch(p1: Point, p2: Point, path: List<Point>) {
        soundManager.playMatch()
        val matchedTile1 = state.board[p1.y][p1.x]!!
        val matchedTile2 = state.board[p2.y][p2.x]!!
        
        matchProcessJob = viewModelScope.launch {
            connectingPath = path
            delay(150)

            val myExplodingTiles = listOf(
                ExplodingTile(matchedTile1.copy(), System.currentTimeMillis()),
                ExplodingTile(matchedTile2.copy(), System.currentTimeMillis())
            )
            
            val newExplodingTiles = state.explodingTiles + myExplodingTiles
            
            val newBoard = state.board.map { it.copyOf() }.toTypedArray()
            newBoard[p1.y][p1.x] = null
            newBoard[p2.y][p2.x] = null
            
            var newTime = state.timeLeftSeconds
            if (state.gameMode == GameMode.CLASSIC || state.gameMode == GameMode.CAMPAIGN) {
                newTime += GameConstants.TIME_ADD_PER_MATCH
            }
            
            state = state.copy(board = newBoard, explodingTiles = newExplodingTiles, timeLeftSeconds = newTime)
            
            delay(300) 

            GameLogic.applyGravity(newBoard, state.level, state.gameMode)
            
            val newScore = state.score + GameConstants.SCORE_PER_MATCH
            val maxLevels = if (state.gameMode == GameMode.CLASSIC) GameConstants.TOTAL_CLASSIC_LEVELS else GameConstants.TOTAL_CAMPAIGN_LEVELS

            if (GameLogic.isBoardEmpty(newBoard)) {
                val bonus = if (state.gameMode != GameMode.DAILY_CHALLENGE) state.timeLeftSeconds else 0
                val totalWithBonus = newScore + bonus

                if (state.gameMode == GameMode.CLASSIC || state.gameMode == GameMode.CAMPAIGN) {
                    leaderboardManager?.submitScore(state.gameMode, totalWithBonus)
                }

                if (state.gameMode == GameMode.DAILY_CHALLENGE) {
                    soundManager.playWin()
                    markDailyChallengeCompleted()
                    state = state.copy(board = newBoard, score = newScore, bonusScore = 0, isVictory = true, explodingTiles = state.explodingTiles.filter { it !in myExplodingTiles })
                } else if (state.level < maxLevels) {
                    soundManager.playWin()
                    if (state.gameMode == GameMode.CAMPAIGN) {
                        val nextLvl = state.level + 1
                        prefs.edit().putInt("campaign_level", nextLvl).apply()
                        state = state.copy(savedCampaignLevel = nextLvl)
                    }
                    state = state.copy(board = newBoard, score = newScore, bonusScore = bonus, isLevelComplete = true, explodingTiles = state.explodingTiles.filter { it !in myExplodingTiles })
                } else {
                    soundManager.playWin()
                    if (state.gameMode == GameMode.CAMPAIGN) {
                        prefs.edit().putInt("campaign_level", 0).apply() 
                        state = state.copy(savedCampaignLevel = 0)
                    }
                    state = state.copy(board = newBoard, score = newScore, bonusScore = bonus, isVictory = true, explodingTiles = state.explodingTiles.filter { it !in myExplodingTiles })
                }
            } else {
                val nextBoard = newBoard
                if (!GameLogic.hasValidMoves(nextBoard)) {
                    if (state.shufflesLeft > 0) {
                        while (!GameLogic.hasValidMoves(nextBoard)) {
                            soundManager.playShuffle()
                            GameLogic.shuffle(nextBoard)
                            state = state.copy(shufflesLeft = state.shufflesLeft - 1)
                            if (state.shufflesLeft <= 0) break
                        }
                        if (!GameLogic.hasValidMoves(nextBoard) && state.shufflesLeft <= 0) {
                            handleNoMoves()
                        }
                    } else {
                        if (state.gameMode == GameMode.DAILY_CHALLENGE) {
                            soundManager.playLose()
                            state = state.copy(isGameOver = true)
                        } else {
                            handleNoMoves()
                        }
                    }
                }
                state = state.copy(board = nextBoard, score = newScore, explodingTiles = state.explodingTiles.filter { it !in myExplodingTiles })
            }
            
            firstSelectedTile = null
            connectingPath = null
        }
    }

    private fun handleNoMoves() {
        if (state.gameMode == GameMode.DAILY_CHALLENGE) {
            soundManager.playLose()
            state = state.copy(isGameOver = true)
            return
        }
        
        soundManager.playLose()
        if (adManager.isAdLoaded()) {
            state = state.copy(showAdDialog = AdRewardType.EXTRA_SHUFFLES)
        } else {
            state = state.copy(isGameOver = true)
        }
    }

    fun showUnlockScreen() {
        soundManager.playClick()
        if (state.gameMode == GameMode.CAMPAIGN) {
            state = state.copy(isLevelComplete = false, isShowingUnlock = true)
        } else {
            nextLevel()
        }
    }

    fun nextLevel(activity: Activity? = null) {
        soundManager.playClick()
        
        val proceed = {
            stopAllJobs()
            viewModelScope.launch {
                val totalScore = state.score + state.bonusScore
                val newLevel = state.level + 1
                val nextLevelTime = if (state.gameMode == GameMode.DAILY_CHALLENGE) 0 else GameConstants.INITIAL_TIME_CLASSIC 
                
                state = state.copy(
                    isLevelComplete = false,
                    isShowingUnlock = false,
                    isLevelStarting = true,
                    isPaused = false,
                    level = newLevel,
                    board = GameLogic.initializeBoard(state.gameMode, newLevel),
                    score = totalScore,
                    bonusScore = 0,
                    timeLeftSeconds = nextLevelTime,
                    explodingTiles = emptyList(),
                    hasUsedTimeRewardInLevel = false
                )
                soundManager.playBeginning()
                delay(2000)
                state = state.copy(isLevelStarting = false)
                if (state.gameMode != GameMode.DAILY_CHALLENGE) {
                    startTimer()
                }
            }
        }

        if (activity != null && adManager.isInterstitialAdLoaded()) {
            adManager.showInterstitialAd(activity) { proceed() }
        } else {
            proceed()
        }
    }

    fun togglePause() {
        soundManager.playClick()
        state = state.copy(isPaused = !state.isPaused)
        if (!state.isPaused && state.gameMode != GameMode.DAILY_CHALLENGE) startTimer()
        else if (state.isPaused) timerJob?.cancel()
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (!state.isPaused && !state.isGameOver && !state.isVictory && !state.isLevelComplete && state.gameMode != GameMode.DAILY_CHALLENGE) {
            startTimer()
        }
    }

    fun requestQuit() {
        soundManager.playClick()
        if (state.isGameOver || state.isVictory || state.isLevelComplete) {
            state = state.copy(isPaused = false, showAdDialog = null, isGameOver = false, isVictory = false, isLevelComplete = false, isShowingUnlock = false, showQuitConfirmDialog = false)
        } else {
            state = state.copy(showQuitConfirmDialog = true)
        }
    }

    fun cancelQuit() {
        soundManager.playClick()
        state = state.copy(showQuitConfirmDialog = false)
    }

    fun quitGame(activity: Activity? = null, onBack: () -> Unit) {
        soundManager.playClick()
        
        val proceed = {
            stopAllJobs()
            state = state.copy(isPaused = false, showAdDialog = null, isGameOver = false, isVictory = false, isLevelComplete = false, isShowingUnlock = false, showQuitConfirmDialog = false)
            firstSelectedTile = null
            connectingPath = null
            hintTiles = null
            onBack()
        }

        if (activity != null && adManager.isInterstitialAdLoaded()) {
            adManager.showInterstitialAd(activity) { proceed() }
        } else {
            proceed()
        }
    }

    private fun stopAllJobs() {
        timerJob?.cancel()
        gameStartJob?.cancel()
        matchProcessJob?.cancel()
    }

    fun showHint() {
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting || state.isLevelComplete || state.isShowingUnlock) return
        
        if (state.hintsLeft <= 0) {
            if (adManager.isAdLoaded()) {
                state = state.copy(showAdDialog = AdRewardType.EXTRA_HINTS)
            } else {
                Toast.makeText(getApplication(), "Ad is not ready. Try again later for more hints.", Toast.LENGTH_SHORT).show()
                adManager.loadRewardedAd()
            }
            return
        }

        soundManager.playHint()
        val points = mutableListOf<Point>()
        for (y in 0 until GameLogic.ROWS) {
            for (x in 0 until GameLogic.COLS) {
                if (state.board[y][x] != null) points.add(Point(x, y))
            }
        }

        for (i in 0 until points.size) {
            for (j in i + 1 until points.size) {
                val t1 = state.board[points[i].y][points[i].x]
                val t2 = state.board[points[j].y][points[j].x]
                if (t1?.bitmapIndex == t2?.bitmapIndex) {
                    if (GameLogic.checkPath(state.board, points[i], points[j]) != null) {
                        hintTiles = Pair(points[i], points[j])
                        state = state.copy(hintsLeft = state.hintsLeft - 1)
                        return
                    }
                }
            }
        }
    }

    fun watchAd(activity: Activity) {
        soundManager.playClick()
        val type = state.showAdDialog ?: return
        adManager.showRewardedAd(
            activity = activity,
            onAdDismissed = {
                // If ad dismissed without reward and it was a critical ad, skip it
                if (state.showAdDialog != null) {
                    skipAdReward()
                }
            },
            onRewardEarned = {
                giveReward(type)
            }
        )
    }

    private fun giveReward(type: AdRewardType) {
        soundManager.playBonus()
        when (type) {
            AdRewardType.EXTRA_TIME -> {
                state = state.copy(
                    timeLeftSeconds = state.timeLeftSeconds + GameConstants.REWARD_AD_TIME_SECONDS, 
                    showAdDialog = null,
                    hasUsedTimeRewardInLevel = true
                )
                if (state.gameMode != GameMode.DAILY_CHALLENGE) startTimer()
            }
            AdRewardType.EXTRA_SHUFFLES -> {
                val nextBoard = state.board.map { it.copyOf() }.toTypedArray()
                GameLogic.shuffle(nextBoard)
                state = state.copy(
                    board = nextBoard,
                    shufflesLeft = state.shufflesLeft + 5,
                    showAdDialog = null
                )
                if (state.gameMode != GameMode.DAILY_CHALLENGE) startTimer()
            }
            AdRewardType.EXTRA_HINTS -> {
                state = state.copy(
                    hintsLeft = state.hintsLeft + 1,
                    showAdDialog = null
                )
                if (state.gameMode != GameMode.DAILY_CHALLENGE) startTimer()
            }
            AdRewardType.UNLOCK_FOOD -> {
                state = state.copy(
                    unlockedTileTypes = state.unlockedTileTypes + TileType.FOOD,
                    currentTileType = TileType.FOOD,
                    showAdDialog = null
                )
                saveSettings()
            }
            AdRewardType.UNLOCK_GEM -> {
                state = state.copy(
                    unlockedTileTypes = state.unlockedTileTypes + TileType.GEM,
                    currentTileType = TileType.GEM,
                    showAdDialog = null
                )
                saveSettings()
            }
        }
    }

    fun skipAdReward() {
        soundManager.playSkip()
        val lastType = state.showAdDialog
        state = state.copy(showAdDialog = null)
        
        // If critical rewards are skipped, trigger game over
        if (lastType == AdRewardType.EXTRA_TIME || lastType == AdRewardType.EXTRA_SHUFFLES) {
            state = state.copy(isGameOver = true)
        } else if (lastType == AdRewardType.UNLOCK_FOOD || lastType == AdRewardType.UNLOCK_GEM) {
            // No additional action needed, just closing dialog is enough
        } else {
            // For optional rewards like Hints, resume timer if not daily
            if (state.gameMode != GameMode.DAILY_CHALLENGE) {
                startTimer()
            }
        }
    }

    fun toggleSound() {
        state = state.copy(isSoundEnabled = !state.isSoundEnabled)
        saveSettings()
        if (state.isSoundEnabled) soundManager.playClick()
    }

    fun toggleVibration() {
        state = state.copy(isVibrationEnabled = !state.isVibrationEnabled)
        saveSettings()
        if (state.isSoundEnabled) soundManager.playClick()
    }

    fun showSettings() {
        soundManager.playClick()
        state = state.copy(showSettings = true)
    }

    fun hideSettings() {
        soundManager.playClick()
        state = state.copy(showSettings = false)
    }

    fun showLeaderboards(activity: Activity) {
        soundManager.playClick()
        if (leaderboardManager == null) {
            leaderboardManager = LeaderboardManager(activity)
        }
        leaderboardManager?.showLeaderboard()
    }

    fun showSpecificLeaderboard(activity: Activity, mode: GameMode) {
        soundManager.playClick()
        if (leaderboardManager == null) {
            leaderboardManager = LeaderboardManager(activity)
        }
        val id = when (mode) {
            GameMode.CLASSIC -> LeaderboardManager.LEADERBOARD_CLASSIC
            GameMode.CAMPAIGN -> LeaderboardManager.LEADERBOARD_CAMPAIGN
            else -> null
        }
        leaderboardManager?.showLeaderboard(id)
    }

    override fun onCleared() {
        stopAllJobs()
        soundManager.release()
        super.onCleared()
    }
}
