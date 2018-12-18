package catt.animation.loader

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View

interface IToolView {

    val context: Context?

    val resources: Resources?

    val view: View?

    /**
     * SurfaceView 释放 surface
     * TextureView 释放 surfaceTexture
     */
    fun onRelease()

    fun lockCanvas(): Canvas?

    fun lockCanvas(dirty: Rect?): Canvas?

    fun unlockCanvasAndPost(canvas: Canvas)

    val isVisible:Boolean

    val isMeasured:Boolean

    /**
     * 判断是否被隐藏
     */
    fun View.isVisible(): Boolean = visibility == View.VISIBLE

    /**
     * 判断是否被绘制
     */
    fun View.isMeasured(): Boolean = (width > 0 && measuredWidth > 0) || (height > 0 && measuredHeight > 0)
}