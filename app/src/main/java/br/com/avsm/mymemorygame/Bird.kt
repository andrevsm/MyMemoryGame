package br.com.avsm.mymemorygame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.ArrayList

class Bird(context: Context) {
    private val birdImages: MutableList<Bitmap> = ArrayList()
    private var currentIndex = 0

    init {
        // Carregando o spritesheet
        val spritesheet = BitmapFactory.decodeResource(context.resources, R.drawable.bird)

        // Calculando a largura e a altura de cada sprite na spritesheet
        val sheetWidth = spritesheet.width
        val sheetHeight = spritesheet.height
        val numColumns = 3 // 3 sprites na horizontal
        val numRows = 2    // 2 sprites na vertical
        val spriteWidth = sheetWidth / numColumns
        val spriteHeight = sheetHeight / numRows

        for (row in 0 until numRows) {
            // Loop para percorrer as colunas
            for (col in 0 until numColumns) {
                // Calcular as coordenadas de recorte para o sprite atual
                val left = col * spriteWidth
                val top = row * spriteHeight
                val right = left + spriteWidth
                val bottom = top + spriteHeight

                // Criar um Bitmap para o sprite atual e adicionar à lista
                val spriteBitmap = Bitmap.createBitmap(spritesheet, left, top, right - left, bottom - top)
                birdImages.add(spriteBitmap)
            }
        }
    }

    fun getBirds(): MutableList<Bitmap> {
        return birdImages
    }
}
