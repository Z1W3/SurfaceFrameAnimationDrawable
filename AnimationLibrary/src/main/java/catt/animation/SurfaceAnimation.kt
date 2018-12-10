package catt.animation

import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.util.Log.*
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import catt.animation.enums.ThreadPriorityClubs
import java.lang.ref.Reference
import java.io.Closeable

abstract class SurfaceAnimation(
    override val reference: Reference<SurfaceView>,
    @ThreadPriorityClubs override val priority: Int
) : ISurfaceAnimation, SurfaceHolder.Callback {
    private val _TAG: String by lazy { SurfaceAnimation::class.java.simpleName }

    override val surfaceView: SurfaceView?
        get() = reference.get()

    override val surfaceHolder: SurfaceHolder?
        get() = surfaceView?.holder

    override val surface: Surface?
        get() = surfaceView?.holder?.surface

    override val resources: Resources?
        get() = surfaceView?.resources

    override val context: Context?
        get() = surfaceView?.context

    override val isVisible: Boolean
        get() {
            surfaceView ?: return false
            return surfaceView!!.visibility == View.VISIBLE
        }

    override val isMeasured: Boolean
        get() {
            surfaceView ?: return false
            return surfaceView!!.run {
                return@run (width > 0 && measuredWidth > 0) || (height > 0 && measuredHeight > 0)
            }
        }

    init {
        surfaceView?.setZOrderOnTop(true)
        surfaceHolder?.setFormat(PixelFormat.TRANSLUCENT)
        surfaceHolder?.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        i(_TAG, "surfaceChanged: ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        w(_TAG, "surfaceDestroyed: ")
        pause()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        i(_TAG, "surfaceCreated: ")
        restore()
    }

    open fun release() {
        surfaceHolder?.removeCallback(this)
        surface?.release()
        reference.clear()
    }

    open fun cancel() {
        pause()
    }

    abstract fun pause()

    abstract fun restore()

    abstract fun start()

}