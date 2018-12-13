package catt.animation.loader

import android.content.Context
import android.graphics.*
import android.util.Log.e
import android.view.TextureView
import android.view.View
import kotlinx.coroutines.*
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class TextureViewTool(texture: TextureView, private val callback: ILoaderLifecycle? = null) :
    TextureView.SurfaceTextureListener, IToolView {

    private val _TAG: String by lazy { TextureViewTool::class.java.simpleName }

    override val view: View?
        get() = reference.get()

    override val context: Context?
        get() = reference.get()?.context?.applicationContext

    private val reference: Reference<TextureView> by lazy { WeakReference(texture) }

    init {
        texture.alpha = 0.99F
        texture.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
            isDither = true
            isAntiAlias = true
        })
        texture.surfaceTextureListener = this
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        callback?.onLoaderChanged()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//        if(isRelease){
//            return false
//        }
        callback ?: return false
        return callback.onLoaderDestroyed()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        callback?.onLoaderCreated()
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
        reference.get()?.visibility = View.GONE
        reference.clear()
    }
}