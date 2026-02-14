package com.ninorock.beastconnect.game

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    var state by mutableStateOf(GameState())
        private set

    private var timerJob: Job? = null
    val monsterBitmaps = BitmapUtils.sliceBeastBitmap(application)
    private val soundManager = SoundManager(application)
    private val vibrator = getSystemService(application, Vibrator::class.java)
    val adManager = AdManager(application)

    var firstSelectedTile by mutableStateOf<Point?>(null)
    var connectingPath by mutableStateOf<List<Point>?>(null)
    var hintTiles by mutableStateOf<Pair<Point, Point>?>(null)

    private var isInitialized = false

    fun startGame(mode: GameMode = GameMode.CLASSIC) {
        isInitialized = false
        viewModelScope.launch {
            val totalTiles = when(mode) {
                GameMode.CLASSIC -> GameConstants.TOTAL_SLOTS
                GameMode.DAILY_CHALLENGE -> GameConstants.TOTAL_SLOTS
                GameMode.CAMPAIGN -> (GameConstants.CAMPAIGN_START_TILES + (0) * GameConstants.CAMPAIGN_TILES_INCREMENT).coerceAtMost(GameConstants.TOTAL_SLOTS)
            }
            
            val initialTime = when(mode) {
                GameMode.CLASSIC -> GameConstants.LEVEL_TIME_SECONDS
                GameMode.DAILY_CHALLENGE -> Int.MAX_VALUE // Unlimited time
                GameMode.CAMPAIGN -> (totalTiles / 2) * 10
            }
            
            state = GameState(
                gameMode = mode,
                board = GameLogic.initializeBoard(mode, 1),
                level = 1,
                shufflesLeft = if (mode == GameMode.DAILY_CHALLENGE) 0 else GameConstants.INITIAL_SHUFFLES,
                hintsLeft = 3,
                timeLeftSeconds = initialTime,
                isLevelStarting = true
            )
            isInitialized = true
            delay(2000)
            state = state.copy(isLevelStarting = false)
            if (mode != GameMode.DAILY_CHALLENGE) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (state.timeLeftSeconds > 0 && !state.isPaused && !state.isGameOver && state.showAdDialog == null && !state.isLevelStarting && !state.isLevelComplete && !state.isShowingUnlock) {
                delay(1000)
                state = state.copy(timeLeftSeconds = state.timeLeftSeconds - 1)
            }
            if (state.timeLeftSeconds <= 0 && state.showAdDialog == null && !state.isLevelStarting && !state.isVictory && !state.isLevelComplete && !state.isShowingUnlock) {
                state = state.copy(showAdDialog = AdRewardType.EXTRA_TIME)
            }
        }
    }

    fun onTileClick(x: Int, y: Int) {
        soundManager.playClick()
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting || state.isLevelComplete || state.isShowingUnlock) return
        val currentTile = state.board[y][x] ?: return

        hintTiles = null

        val first = firstSelectedTile
        if (first == null) {
            firstSelectedTile = Point(x, y)
        } else {
            if (first.x == x && first.y == y) {
                firstSelectedTile = null
                return
            }

            val path = GameLogic.checkPath(state.board, first, Point(x, y))
            if (path != null) {
                handleMatch(first, Point(x, y), path)
            } else {
                triggerErrorFeedback()
                firstSelectedTile = Point(x, y)
            }
        }
    }

    private fun triggerErrorFeedback() {
        soundManager.playError()
        if (vibrator?.hasVibrator() == true) {
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
        
        viewModelScope.launch {
            connectingPath = path
            delay(150)

            val newExplodingTiles = state.explodingTiles + 
                ExplodingTile(matchedTile1, System.currentTimeMillis()) +
                ExplodingTile(matchedTile2, System.currentTimeMillis())
            
            val newBoard = state.board.map { it.copyOf() }.toTypedArray()
            newBoard[p1.y][p1.x] = null
            newBoard[p2.y][p2.x] = null
            
            state = state.copy(board = newBoard, explodingTiles = newExplodingTiles)
            
            delay(300) 

            GameLogic.applyGravity(newBoard, state.level, state.gameMode)
            
            val newScore = state.score + GameConstants.SCORE_PER_MATCH
            val maxLevels = if (state.gameMode == GameMode.CLASSIC) GameConstants.TOTAL_CLASSIC_LEVELS else GameConstants.TOTAL_CAMPAIGN_LEVELS

            if (GameLogic.isBoardEmpty(newBoard)) {
                if (state.gameMode == GameMode.DAILY_CHALLENGE) {
                    state = state.copy(board = newBoard, score = newScore, isVictory = true, explodingTiles = emptyList())
                } else if (state.level < maxLevels) {
                    state = state.copy(board = newBoard, score = newScore, isLevelComplete = true, explodingTiles = emptyList())
                } else {
                    state = state.copy(board = newBoard, score = newScore, isVictory = true, explodingTiles = emptyList())
                }
            } else {
                val nextBoard = newBoard
                if (!GameLogic.hasValidMoves(nextBoard)) {
                    if (state.shufflesLeft > 0) {
                        while (!GameLogic.hasValidMoves(nextBoard)) {
                            GameLogic.shuffle(nextBoard)
                            state = state.copy(shufflesLeft = state.shufflesLeft - 1)
                            if (state.shufflesLeft <= 0) break
                        }
                        if (!GameLogic.hasValidMoves(nextBoard) && state.shufflesLeft <= 0) {
                            state = state.copy(showAdDialog = AdRewardType.EXTRA_SHUFFLES)
                        }
                    } else {
                        if (state.gameMode == GameMode.DAILY_CHALLENGE) {
                            state = state.copy(isGameOver = true)
                        } else {
                            state = state.copy(showAdDialog = AdRewardType.EXTRA_SHUFFLES)
                        }
                    }
                }
                state = state.copy(board = nextBoard, score = newScore, explodingTiles = emptyList())
            }
            
            firstSelectedTile = null
            connectingPath = null
        }
    }

    fun showUnlockScreen() {
        state = state.copy(isLevelComplete = false, isShowingUnlock = true)
    }

    fun nextLevel() {
        viewModelScope.launch {
            val newLevel = state.level + 1
            val totalTiles = (GameConstants.CAMPAIGN_START_TILES + (newLevel - 1) * GameConstants.CAMPAIGN_TILES_INCREMENT).coerceAtMost(GameConstants.TOTAL_SLOTS)
            val nextLevelTime = if (state.gameMode == GameMode.CLASSIC) GameConstants.LEVEL_TIME_SECONDS else (totalTiles / 2) * 10
            
            state = state.copy(
                isShowingUnlock = false,
                isLevelStarting = true,
                level = newLevel,
                board = GameLogic.initializeBoard(state.gameMode, newLevel),
                timeLeftSeconds = nextLevelTime
            )
            delay(2000)
            state = state.copy(isLevelStarting = false)
            startTimer()
        }
    }

    fun togglePause() {
        soundManager.playClick()
        state = state.copy(isPaused = !state.isPaused)
        if (!state.isPaused && state.gameMode != GameMode.DAILY_CHALLENGE) startTimer()
    }

    fun showHint() {
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting || state.isLevelComplete || state.isShowingUnlock) return
        
        if (state.hintsLeft <= 0) {
            state = state.copy(showAdDialog = AdRewardType.EXTRA_HINTS)
            return
        }

        soundManager.playClick()
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
        val type = state.showAdDialog ?: return
        adManager.showRewardedAd(activity) {
            giveReward(type)
        }
    }

    private fun giveReward(type: AdRewardType) {
        when (type) {
            AdRewardType.EXTRA_TIME -> {
                state = state.copy(
                    timeLeftSeconds = state.timeLeftSeconds + 300, 
                    showAdDialog = null
                )
                startTimer()
            }
            AdRewardType.EXTRA_SHUFFLES -> {
                val nextBoard = state.board.map { it.copyOf() }.toTypedArray()
                GameLogic.shuffle(nextBoard)
                state = state.copy(
                    board = nextBoard,
                    shufflesLeft = state.shufflesLeft + 5,
                    showAdDialog = null
                )
            }
            AdRewardType.EXTRA_HINTS -> {
                state = state.copy(
                    hintsLeft = state.hintsLeft + 1,
                    showAdDialog = null
                )
            }
        }
    }

    fun skipAdReward() {
        val lastType = state.showAdDialog
        state = state.copy(showAdDialog = null)
        
        if (lastType == AdRewardType.EXTRA_TIME || lastType == AdRewardType.EXTRA_SHUFFLES) {
            state = state.copy(isGameOver = true)
        }
    }

    override fun onCleared() {
        soundManager.release()
        super.onCleared()
    }
}
