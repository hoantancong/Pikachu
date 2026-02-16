package com.ninorock.beastconnect.game

import androidx.compose.ui.graphics.Color

object GameConstants {
    const val ROWS = 9
    const val COLS = 16
    const val TOTAL_SLOTS = ROWS * COLS
    
    const val UNIQUE_BEASTS = 36 // 6x6 grid
    const val BEASTS_PER_ROW = 6
    const val TOTAL_ROWS = 6
    
    const val INITIAL_SHUFFLES = 3
    const val CLASSIC_LEVEL_TIME_SECONDS = 600 // 10 minutes
    const val LEVEL_TIME_SECONDS = CLASSIC_LEVEL_TIME_SECONDS
    const val SCORE_PER_MATCH = 100
    
    const val TOTAL_CLASSIC_LEVELS = 8
    const val TOTAL_CAMPAIGN_LEVELS = 30
    
    const val CAMPAIGN_START_TILES = 48
    const val CAMPAIGN_TILES_INCREMENT = 4
    const val CAMPAIGN_START_BEASTS = 12

    // Element Colors
    val ELEMENT_BACKGROUNDS = listOf(
        Color(0xFF1A1F16), // Green
        Color(0xFF050B1E), // Water Blue
        Color(0xFF0B1B2B), // Ice
        Color(0xFF0B1020), // Fire
        Color(0xFF0F1724), // Rock
        Color(0xFF07040F)  // Dark Purple
    )
    
    val ELEMENT_GLOW_COLORS = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF03A9F4), // Water Blue
        Color(0xFF81D4FA), // Ice
        Color(0xFFFF5722), // Fire
        Color(0xFF9E9E9E), // Rock
        Color(0xFF9C27B0)  // Dark Purple
    )
}
