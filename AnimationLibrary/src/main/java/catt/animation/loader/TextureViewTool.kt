package catt.animation.loader

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.TextureView
import android.view.View
import catt.animation.handler.IHandlerThread
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class TextureViewTool(private val handler: IHandlerThread, texture: TextureView, private val callback: ILoaderLifecycle) :
    TextureView.SurfaceTextureListener, IToolView {

    private val _TAG: String by lazy { TextureViewTool::class.java.simpleName }

    private var isRelease:Boolean = false

    override val resources: Resources?
        get() = reference.get()?.resources

    override val view: View?
        get() = reference.get()

    override val context: Context?
        get() = reference.get()?.context?.applicationContext

    private val reference: Reference<TextureView> by lazy { WeakReference(texture) }

    init {
        texture.alpha = 0.99F
        texture.surfaceTextureListener = this
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        callback.onSurfaceChanged()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        reference.get()?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        handler.setPaused(true)
        while (handler.isPaused && !handler.isCompleted/*必须等待Canvas工作线程完成后才能返回bool, 否则绘制强行终止则导致JNI层崩溃*/){
            Thread.sleep(16L)
        }
        return callback.onSurfaceDestroyed(when{
            isRelease -> {
                surface?.release()
                reference.get()?.visibility = View.GONE
                reference.get()?.surfaceTextureListener = null
                reference.clear()
                true
            }
            else -> {
                false
            }
        })
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        reference.get()?.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
            isDither = true
            isAntiAlias = true
        })
        callback.onSurfaceCreated()
    }

    override fun lockCanvas(): Canvas? = reference.get()?.lockCanvas()

    override fun lockCanvas(dirty: Rect?): Canvas? = reference.get()?.lockCanvas(dirty)

    override fun unlockCanvasAndPost(canvas: Canvas) {
        reference.get()?.unlockCanvasAndPost(canvas)
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
        isRelease = true
    }
}