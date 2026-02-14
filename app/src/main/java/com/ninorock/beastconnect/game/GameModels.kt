package com.ninorock.beastconnect.game

import android.graphics.Bitmap

data class GameState(
    val gameMode: GameMode = GameMode.CLASSIC,
    val board: Array<Array<Tile?>> = Array(GameLogic.ROWS) { arrayOfNulls<Tile>(GameLogic.COLS) },
    val level: Int = 1,
    val score: Int = 0,
    val timeLeftSeconds: Int = 300,
    val shufflesLeft: Int = 3,
    val hintsLeft: Int = 3,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isLevelComplete: Boolean = false,
    val isShowingUnlock: Boolean = false,
    val isLevelStarting: Boolean = false,
    val showAdDialog: AdRewardType? = null,
    val explodingTiles: List<ExplodingTile> = emptyList()
)

data class Tile(val id: Int, var x: Int, var y: Int, val bitmapIndex: Int) {
    val elementIndex: Int get() = bitmapIndex / 6
}

data class ExplodingTile(val tile: Tile, val startTime: Long)

data class Point(val x: Int, val y: Int)

enum class GameMode {
    CLASSIC, CAMPAIGN, DAILY_CHALLENGE
}

enum class AdRewardType {
    EXTRA_TIME, EXTRA_SHUFFLES, EXTRA_HINTS
}
