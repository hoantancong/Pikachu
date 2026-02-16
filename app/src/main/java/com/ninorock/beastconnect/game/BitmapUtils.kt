package com.ninorock.beastconnect.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ninorock.beastconnect.R

object BitmapUtils {
    fun sliceBeastBitmap(context: Context): List<Bitmap> {
        return sliceBitmap(context, R.drawable.beast_main)
    }

    fun sliceFoodBitmap(context: Context): List<Bitmap> {
        return sliceBitmap(context, R.drawable.food_tile)
    }

    fun sliceGemBitmap(context: Context): List<Bitmap> {
        return sliceBitmap(context, R.drawable.gem_tile)
    }

    private fun sliceBitmap(context: Context, resourceId: Int): List<Bitmap> {
        val options = BitmapFactory.Options().apply { inScaled = false }
        val source = BitmapFactory.decodeResource(context.resources, resourceId, options) ?: return emptyList()
        
        val tiles = mutableListOf<Bitmap>()
        val tileWidth = source.width / 6
        val tileHeight = source.height / 6
        
        for (row in 0 until 6) {
            for (col in 0 until 6) {
                val bitmap = Bitmap.createBitmap(source, col * tileWidth, row * tileHeight, tileWidth, tileHeight)
                tiles.add(bitmap)
            }
        }
        return tiles
    }
}
