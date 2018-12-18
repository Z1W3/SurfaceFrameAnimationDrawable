package catt.animation.component

import android.graphics.*
import catt.animation.handler.IHandlerThread

interface ICanvasComponent {
    val paint: Paint

    fun generatedBasePaint(): Paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    fun Canvas.drawSurfaceAnimationBitmap(handler: IHandlerThread, bitmap: Bitmap?, matrix: Matrix, paint: Paint?): Canvas {
        if (!handler.isPaused && bitmap != null) drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (!handler.isPaused && bitmap != null) drawBitmap(bitmap, matrix, paint)
        return this
    }
}