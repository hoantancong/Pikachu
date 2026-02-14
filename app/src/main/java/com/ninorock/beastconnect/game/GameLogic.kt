package com.ninorock.beastconnect.game

import java.util.*

object GameLogic {
    const val COLS = GameConstants.COLS
    const val ROWS = GameConstants.ROWS

    fun initializeBoard(): Array<Array<Tile?>> {
        val board = Array(ROWS) { arrayOfNulls<Tile>(COLS) }
        val tileIds = mutableListOf<Int>()
        
        for (i in 0 until GameConstants.UNIQUE_BEASTS) {
            repeat(GameConstants.REPETITIONS_PER_BEAST) { tileIds.add(i) }
        }
        tileIds.shuffle()

        var index = 0
        for (y in 0 until ROWS) {
            for (x in 0 until COLS) {
                board[y][x] = Tile(id = index, bitmapIndex = tileIds[index], x = x, y = y)
                index++
            }
        }
        
        while (!hasValidMoves(board)) {
            shuffle(board)
        }
        
        return board
    }

    fun checkPath(board: Array<Array<Tile?>>, p1: Point, p2: Point): List<Point>? {
        if (p1.x !in 0 until COLS || p1.y !in 0 until ROWS || p2.x !in 0 until COLS || p2.y !in 0 until ROWS) return null
        val t1 = board[p1.y][p1.x] ?: return null
        val t2 = board[p2.y][p2.x] ?: return null
        if (t1.bitmapIndex != t2.bitmapIndex) return null
        if (p1 == p2) return null

        val queue: Queue<Node> = LinkedList()
        val visited = mutableMapOf<Triple<Int, Int, Int>, Int>() 

        for (d in 0..3) {
            queue.add(Node(p1, d, 0, listOf(p1)))
        }

        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            if (current.turns > 2) continue

            val dx = intArrayOf(1, 0, -1, 0)
            val dy = intArrayOf(0, 1, 0, -1)

            val nextX = current.p.x + dx[current.dir]
            val nextY = current.p.y + dy[current.dir]

            if (nextX !in -1..COLS || nextY !in -1..ROWS) continue

            val nextP = Point(nextX, nextY)
            val newPath = current.path + nextP

            if (nextP == p2) return newPath
            if (nextX in 0 until COLS && nextY in 0 until ROWS && board[nextY][nextX] != null) continue

            if (visited.getOrDefault(Triple(nextX, nextY, current.dir), 3) > current.turns) {
                visited[Triple(nextX, nextY, current.dir)] = current.turns
                queue.add(Node(nextP, current.dir, current.turns, newPath))
            }

            if (current.turns < 2) {
                for (newDir in 0..3) {
                    if (newDir == current.dir || newDir == (current.dir + 2) % 4) continue
                    if (visited.getOrDefault(Triple(nextX, nextY, newDir), 3) > current.turns + 1) {
                        visited[Triple(nextX, nextY, newDir)] = current.turns + 1
                        queue.add(Node(nextP, newDir, current.turns + 1, newPath))
                    }
                }
            }
        }
        return null
    }

    private data class Node(val p: Point, val dir: Int, val turns: Int, val path: List<Point>)

    fun applyGravity(board: Array<Array<Tile?>>, level: Int) {
        when (level) {
            1 -> return 
            2 -> gravityDown(board)
            3 -> gravityUp(board)
            4 -> gravityLeft(board)
            5 -> gravityRight(board)
            6 -> gravityVerticalCenter(board)
            7 -> gravityHorizontalCenter(board)
            8 -> gravityFourWayCenter(board)
        }
    }

    private fun gravityDown(board: Array<Array<Tile?>>) {
        for (x in 0 until COLS) {
            var writeY = ROWS - 1
            for (y in ROWS - 1 downTo 0) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[writeY][x] = tile?.apply { this.y = writeY }
                    writeY--
                }
            }
        }
    }

    private fun gravityUp(board: Array<Array<Tile?>>) {
        for (x in 0 until COLS) {
            var writeY = 0
            for (y in 0 until ROWS) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[writeY][x] = tile?.apply { this.y = writeY }
                    writeY++
                }
            }
        }
    }

    private fun gravityLeft(board: Array<Array<Tile?>>) {
        for (y in 0 until ROWS) {
            var writeX = 0
            for (x in 0 until COLS) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[y][writeX] = tile?.apply { this.x = writeX }
                    writeX++
                }
            }
        }
    }

    private fun gravityRight(board: Array<Array<Tile?>>) {
        for (y in 0 until ROWS) {
            var writeX = COLS - 1
            for (x in COLS - 1 downTo 0) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[y][writeX] = tile?.apply { this.x = writeX }
                    writeX--
                }
            }
        }
    }

    private fun gravityVerticalCenter(board: Array<Array<Tile?>>) {
        for (x in 0 until COLS) {
            var writeYUpper = 3
            for (y in 3 downTo 0) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[writeYUpper][x] = tile?.apply { this.y = writeYUpper }
                    writeYUpper--
                }
            }
            var writeYLower = 5
            for (y in 5 until ROWS) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[writeYLower][x] = tile?.apply { this.y = writeYLower }
                    writeYLower++
                }
            }
        }
    }

    private fun gravityHorizontalCenter(board: Array<Array<Tile?>>) {
        for (y in 0 until ROWS) {
            var writeXLeft = 6
            for (x in 6 downTo 0) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[y][writeXLeft] = tile?.apply { this.x = writeXLeft }
                    writeXLeft--
                }
            }
            var writeXRight = 9
            for (x in 9 until COLS) {
                if (board[y][x] != null) {
                    val tile = board[y][x]
                    board[y][x] = null
                    board[y][writeXRight] = tile?.apply { this.x = writeXRight }
                    writeXRight++
                }
            }
        }
    }

    private fun gravityFourWayCenter(board: Array<Array<Tile?>>) {
        gravityVerticalCenter(board)
        gravityHorizontalCenter(board)
    }

    fun shuffle(board: Array<Array<Tile?>>) {
        val tiles = mutableListOf<Tile>()
        for (y in 0 until ROWS) {
            for (x in 0 until COLS) {
                board[y][x]?.let { tiles.add(it) }
            }
        }
        tiles.shuffle()
        var index = 0
        for (y in 0 until ROWS) {
            for (x in 0 until COLS) {
                if (board[y][x] != null) {
                    val tile = tiles[index++]
                    tile.x = x
                    tile.y = y
                    board[y][x] = tile
                }
            }
        }
    }

    fun hasValidMoves(board: Array<Array<Tile?>>): Boolean {
        val points = mutableListOf<Point>()
        for (y in 0 until ROWS) {
            for (x in 0 until COLS) {
                if (board[y][x] != null) points.add(Point(x, y))
            }
        }

        for (i in 0 until points.size) {
            for (j in i + 1 until points.size) {
                if (checkPath(board, points[i], points[j]) != null) return true
            }
        }
        return false
    }

    fun isBoardEmpty(board: Array<Array<Tile?>>): Boolean {
        for (row in board) {
            for (tile in row) {
                if (tile != null) return false
            }
        }
        return true
    }
}
