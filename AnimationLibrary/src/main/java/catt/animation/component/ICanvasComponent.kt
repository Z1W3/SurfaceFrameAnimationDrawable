package catt.animation.component

import android.graphics.*

interface ICanvasComponent {
    val paint: Paint

    fun generatedBasePaint(): Paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    fun Canvas.drawSurfaceAnimationBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint?): Canvas {
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawBitmap(bitmap, matrix, paint)
        return this
    }
}