package com.ninorock.beastconnect.game

data class Point(val x: Int, val y: Int)

data class Tile(
    val id: Int,
    val bitmapIndex: Int,
    var x: Int,
    var y: Int
)

data class ExplodingTile(
    val tile: Tile,
    val startTime: Long
)

enum class GameMode {
    CLASSIC, CAMPAIGN
}
