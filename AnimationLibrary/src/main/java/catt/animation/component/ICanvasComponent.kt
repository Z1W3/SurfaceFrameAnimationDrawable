package catt.animation.component

import android.graphics.*

interface ICanvasComponent{


    fun Canvas.drawSurfaceAnimationBitmap(bitmap: Bitmap?, paint: Paint?): Canvas {
        bitmap ?: return this
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawBitmap(bitmap, 0f, 0f, paint)
        return this
    }
}