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

    fun startGame() {
        if (isInitialized) return
        
        viewModelScope.launch {
            state = GameState(
                board = GameLogic.initializeBoard(),
                level = 1,
                shufflesLeft = GameConstants.INITIAL_SHUFFLES,
                hintsLeft = 3,
                timeLeftSeconds = GameConstants.LEVEL_TIME_SECONDS,
                isLevelStarting = true
            )
            isInitialized = true
            delay(2000) // Hiển thị hiệu ứng level starting trong 2 giây
            state = state.copy(isLevelStarting = false)
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (state.timeLeftSeconds > 0 && !state.isPaused && !state.isGameOver && state.showAdDialog == null && !state.isLevelStarting) {
                delay(1000)
                state = state.copy(timeLeftSeconds = state.timeLeftSeconds - 1)
            }
            if (state.timeLeftSeconds <= 0 && state.showAdDialog == null && !state.isLevelStarting) {
                state = state.copy(showAdDialog = AdRewardType.EXTRA_TIME)
            }
        }
    }

    fun onTileClick(x: Int, y: Int) {
        soundManager.playClick()
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting) return
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

            GameLogic.applyGravity(newBoard, state.level)
            
            var newScore = state.score + GameConstants.SCORE_PER_MATCH
            var newLevel = state.level
            var nextBoard = newBoard
            var victory = false
            
            if (GameLogic.isBoardEmpty(newBoard)) {
                if (state.level < GameConstants.TOTAL_LEVELS) {
                    newLevel++
                    state = state.copy(isLevelStarting = true, level = newLevel)
                    nextBoard = GameLogic.initializeBoard()
                    delay(2000)
                    state = state.copy(isLevelStarting = false, timeLeftSeconds = GameConstants.LEVEL_TIME_SECONDS)
                    startTimer()
                } else {
                    victory = true
                }
            } else {
                while (!GameLogic.hasValidMoves(nextBoard)) {
                    if (state.shufflesLeft > 0) {
                        GameLogic.shuffle(nextBoard)
                        state = state.copy(shufflesLeft = state.shufflesLeft - 1)
                    } else {
                        state = state.copy(showAdDialog = AdRewardType.EXTRA_SHUFFLES)
                        break
                    }
                }
            }

            state = state.copy(
                board = nextBoard,
                score = newScore,
                level = newLevel,
                isVictory = victory,
                explodingTiles = emptyList()
            )
            
            firstSelectedTile = null
            connectingPath = null
        }
    }

    fun togglePause() {
        soundManager.playClick()
        state = state.copy(isPaused = !state.isPaused)
        if (!state.isPaused) startTimer()
    }

    fun showHint() {
        if (state.isPaused || state.isGameOver || state.isVictory || state.showAdDialog != null || state.isLevelStarting) return
        
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
                if (GameLogic.checkPath(state.board, points[i], points[j]) != null) {
                    hintTiles = Pair(points[i], points[j])
                    state = state.copy(hintsLeft = state.hintsLeft - 1)
                    return
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
                    timeLeftSeconds = state.timeLeftSeconds + 300, // + 5 minutes
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
        
        // Only game over if it was mandatory resource depletion
        if (lastType == AdRewardType.EXTRA_TIME || lastType == AdRewardType.EXTRA_SHUFFLES) {
            state = state.copy(isGameOver = true)
        }
    }

    override fun onCleared() {
        soundManager.release()
        super.onCleared()
    }
}
