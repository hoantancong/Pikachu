package com.ninorock.beastconnect.game

data class GameState(
    val board: Array<Array<Tile?>> = Array(9) { arrayOfNulls<Tile>(16) },
    val level: Int = 1,
    val score: Int = 0,
    val shufflesLeft: Int = 7,
    val hintsLeft: Int = 3,
    val timeLeftSeconds: Int = 600,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val lastErrorTime: Long = 0L,
    val explodingTiles: List<ExplodingTile> = emptyList(),
    val showAdDialog: AdRewardType? = null,
    val isLevelStarting: Boolean = false
)

enum class AdRewardType {
    EXTRA_TIME, EXTRA_SHUFFLES, EXTRA_HINTS
}
