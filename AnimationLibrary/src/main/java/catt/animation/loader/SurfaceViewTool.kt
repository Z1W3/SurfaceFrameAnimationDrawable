package catt.animation.loader

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class SurfaceViewTool(surfaceView: SurfaceView, zOrder:Boolean = false, private val callback: ILoaderLifecycle) :
    SurfaceHolder.Callback2, IToolView {

    private val _TAG: String by lazy { SurfaceViewTool::class.java.simpleName }

    override val resources: Resources?
        get() = reference.get()?.resources

    override val view: View?
        get() =reference.get()

    override val context: Context?
        get() = reference.get()?.context?.applicationContext

    private val reference: Reference<SurfaceView> by lazy { WeakReference(surfaceView) }

    init {
        when (zOrder) {
            true -> surfaceView.setZOrderOnTop(true)
            false -> surfaceView.setZOrderMediaOverlay(true)
        }
        surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        callback.onSurfaceChanged()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        callback.onSurfaceDestroyed(false)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        callback.onSurfaceCreated()
    }

    override fun lockCanvas(): Canvas? = reference.get()?.holder?.lockCanvas()

    override fun lockCanvas(dirty: Rect?): Canvas? = reference.get()?.holder?.lockCanvas(dirty)

    override fun unlockCanvasAndPost(canvas: Canvas) {
        reference.get()?.holder?.unlockCanvasAndPost(canvas)
    }

    override val isVisible: Boolean
        get() {
            reference.get() ?: return false
            return reference.get()?.isVisible()!!
        }
    override val isMeasured: Boolean
        get() {
            reference.get() ?: return false
            return reference.get()?.isMeasured()!!
        }


    override fun onRelease() {
        reference.get()?.holder?.removeCallback(this@SurfaceViewTool)
        reference.get()?.holder?.surface?.release()
        reference.get()?.clearAnimation()
        reference.get()?.visibility = View.GONE
        reference.clear()
    }

}