package catt.animation

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log.e
import android.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.support.v4.util.ArrayMap
import android.util.SparseArray
import android.view.animation.AnimationSet
import catt.animation.bean.AnimatorState
import catt.animation.enums.AnimatorType
import catt.animation.enums.ThreadPriority
import catt.animation.handler.IHandlerThread
import catt.animation.handler.AsyncHandler
import catt.animation.handler.SurfaceLooper
import java.lang.ref.Reference


class FrameAnimationDrawable
constructor(
    reference: Reference<SurfaceView>,
    priority: Int = ThreadPriority.PRIORITY_DEFAULT,
    val callback: OnAnimationCallback? = null
) : SurfaceAnimation(reference, priority),
    Runnable {

    private val _TAG: String by lazy { FrameAnimationDrawable::class.java.simpleName }

    private val handlerThread: IHandlerThread by lazy { AsyncHandler(priority, this) }

    private var isOperationStart: Boolean = false

    private val checkRepeatCountLoops: Boolean
        get() = when (repeatCount) {
            INFINITE -> true
            else -> indexRepeat <= repeatCount
        }

    private val animationList: MutableList<AnimatorState> by lazy { Collections.synchronizedList(ArrayList<AnimatorState>()) }

    private val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            isDither = true
        }
    }

    private var index: Int = 0

    private var indexRepeat: Int = 0

    override fun run() {
        when {
            surfaceView == null -> {
                e(_TAG, "surfaceView = $surfaceView")
                handlerThread.release()
                return
            }
            !isOperationStart -> {
                e(_TAG, "isOperationStart = $isOperationStart")
                handlerThread.release()
                return
            }
            !checkRepeatCountLoops -> {
                e(_TAG, "repeat count finished.")
                handlerThread.release()
                return
            }
            !isVisible -> {
                e(_TAG, "This 'SurfaceView' is unvisible.")
                handlerThread.release()
                handlerThread.play(618L)
                return
            }
            !isMeasured -> {
                e(_TAG, "The 'SurfaceView' has not yet been measured.")
                handlerThread.release()
                handlerThread.play(618L)
                return
            }
        }
        var bean: AnimatorState = scanAnimatorState()
        if (bean.animatorType != AnimatorType.UNKNOW) {
            val bitmap: Bitmap? = getBitmap(bean)
            surfaceHolder?.lockCanvas(getDirty(bitmap))?.apply {
                drawSurfaceAnimationBitmap(bitmap)
                surfaceHolder?.unlockCanvasAndPost(this)
            }
        }
        handlerThread.play(bean.duration)
    }

    /**
     * 触发生命周期onPause() 会导致 surfaceView触发 surfaceDestroyed(holder:SurfaceHolder) -> close()
     *
     */
    override fun release() {
        cancel()
        animationList.clear()
        super.release()
        callback?.onRelease()
    }

    override fun cancel() {
        if (isOperationStart) {
            isOperationStart = false
            pause()
            index = 0
            indexRepeat = 0
            //TODO 此处睡眠应采用协程(CoroutineScope)进行挂起处理
            Thread.sleep(64L)
            cleanCanvas()
            callback?.onCancel()
        }
    }


    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceDestroyed()} 将导致触发该方法
     */
    override fun pause() {
        handlerThread.setPaused(true)
        handlerThread.release()
        callback?.onPause()
    }

    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceCreated()} 将导致触发该方法
     */
    override fun restore() {
        handlerThread.setPaused(false)
        handlerThread.release()
        handlerThread.play()
    }

    @Throws(IllegalArgumentException::class)
    override fun start() {
        if (reference.get() == null) throw IllegalArgumentException("Has been released.")
        if (animationList.size == 0) throw IllegalArgumentException("Animation size must be > 0")
        if (!isOperationStart) {
            indexRepeat = 0
            isOperationStart = true
            animationList.sort()
            restore()
            callback?.onStart()
        }
    }

    /**
     * 设置循环次数
     * @see FrameAnimationDrawable.INFINITE 无限循环
     */
    var repeatCount: Int = 0
        set(count) {
            field = when {
                count < 0 -> INFINITE
                else -> count
            }
        }

    /**
     * 设置循环模式
     *  @see FrameAnimationDrawable.RESTART 从首部循环
     *  @see FrameAnimationDrawable.REVERSE 首尾连接式循环
     */
    var repeatMode: Int = RESTART
        set(mode) {
            field = when (mode) {
                RESTART -> RESTART
                REVERSE -> REVERSE
                else -> throw IllegalArgumentException("Please use the correct parameters.")
            }
        }

    fun addFrame(resId: Int, duration: Long = 0L) {
        animationList.add(
            AnimatorState(
                SystemClock.elapsedRealtime(),
                resId,
                duration
            )
        )
    }

    /**
     * 通过Resources.class 反射资源文件
     * @see Resources.getIdentifier(String name, String defType, String defPackage)
     */
    fun addFrame(resName: String, resType: String, resPackageName: String, duration: Long = 0L) {
        animationList.add(
            AnimatorState(
                SystemClock.elapsedRealtime(),
                resName,
                resType,
                resPackageName,
                duration
            )
        )
    }

    /**
     * 对本地缓存地址进行解析
     */
    fun addFrame(imageFilePath: String, duration: Long = 0L) {
        animationList.add(
            AnimatorState(
                SystemClock.elapsedRealtime(),
                imageFilePath,
                duration
            )
        )
    }

    /**
     * 不建议使用此方法加载帧动画
     */
    fun addFrame(drawable: Drawable, duration: Long = 0L) = addFrame((drawable as BitmapDrawable).bitmap, duration)

    /**
     * 不建议使用此方法加载帧动画
     */
    fun addFrame(bitmap: Bitmap, duration: Long) {
        animationList.add(
            AnimatorState(
                SystemClock.elapsedRealtime(),
                bitmap.ownBitmapFactory(),
                duration
            )
        )
    }

    private fun Bitmap.ownBitmapFactory(): Bitmap {
        val b: Bitmap = this
        return BitmapFactory.Options().run {
            inMutable = true
            inSampleSize = 4
            inJustDecodeBounds = true
            inPreferredConfig = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Bitmap.Config.HARDWARE
                else -> Bitmap.Config.ARGB_8888
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) inDither = true
            inPremultiplied = true
            inBitmap = b
            inJustDecodeBounds = false
            return@run inBitmap
        }
    }

    private fun Canvas.drawSurfaceAnimationBitmap(bitmap: Bitmap?): Canvas {
        bitmap ?: return this
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawBitmap(bitmap, 0f, 0f, paint)
        return this
    }

    private fun scanAnimatorState(): AnimatorState {
        if (animationList.size == index) {
            when (repeatMode) {
                REVERSE -> animationList.reverse()
                RESTART -> animationList.sort()
            }
            index = 0
            ++indexRepeat
        }
        val o: AnimatorState = animationList[index]
        ++index
        return o
    }

    private fun cleanCanvas() {
        surfaceHolder?.lockCanvas(null)?.apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            surfaceHolder?.unlockCanvasAndPost(this)
        }
    }

    private fun getDirty(bitmap: Bitmap?): Rect? {
        bitmap ?: return null
        return Rect(0, 0, bitmap.width, bitmap.height)
    }

    private fun getBitmap(bean: AnimatorState) = when (bean.animatorType) {
        AnimatorType.RES_ID -> BitmapFactory.decodeResource(resources!!, bean.resId).ownBitmapFactory()
        AnimatorType.IDENTIFIER -> {
            val identifier: Int =
                resources!!.getIdentifier(bean.resName, bean.resType, bean.resPackageName)
            if (identifier > 0)
                BitmapFactory.decodeResource(resources!!, identifier).ownBitmapFactory()
            else null
        }
        AnimatorType.CACHE -> {
            //TODO 当前不支持缓存图片功能
            null
        }
        AnimatorType.BITMAP -> bean.bitmap
        else -> null
    }

    open class SimpleOnAnimationCallback : OnAnimationCallback {
        override fun restore() {

        }

        override fun onStart() {
        }

        override fun onPause() {
        }

        override fun onCancel() {
        }

        /**
         * 触发生命周期onPause() 会导致 surfaceView触发 surfaceDestroyed(holder:SurfaceHolder)
         */
        override fun onRelease() {
        }
    }

    companion object {
        /**
         * 执行次数无线循环
         */
        const val INFINITE = -1
        /**
         * 执行方式，从首部执行
         */
        const val RESTART = 1
        /**
         * 执行方式，首尾相接式执行
         */
        const val REVERSE = 2
    }
}