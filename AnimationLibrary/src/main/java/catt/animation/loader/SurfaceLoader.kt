package catt.animation.loader

import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class SurfaceLoader(surfaceView: SurfaceView, zOrder:Boolean = false, private val callback: ILoaderLifecycle? = null) :
    SurfaceHolder.Callback2, ILoader {

    private val _TAG: String by lazy { SurfaceLoader::class.java.simpleName }

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
        callback?.onLoaderChanged()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        callback?.onLoaderDestroyed()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        callback?.onLoaderCreated()
    }


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
        reference.get()?.holder?.removeCallback(this)
        reference.get()?.holder?.surface?.release()
    }

}