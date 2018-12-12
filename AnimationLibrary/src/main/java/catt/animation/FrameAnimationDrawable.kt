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
import catt.animation.bean.AnimatorState
import catt.animation.callback.OnAnimationCallback
import catt.animation.component.IBitmapComponent
import catt.animation.component.ICanvasComponent
import catt.animation.controller.IAnimationController
import catt.animation.enums.AnimatorType
import catt.animation.enums.ThreadPriority
import catt.animation.handler.IHandlerThread
import catt.animation.handler.AsyncHandler
import catt.animation.loader.ILoader
import catt.animation.loader.ILoaderLifecycle
import catt.animation.loader.SurfaceLoader
import catt.animation.loader.TextureLoader

/**
 * <h3>帧布局动画</h3>
 * <p>
 *     采用 SurfaceView 与 TextureView进行图片加载帧动画
 * </p>
 */
class FrameAnimationDrawable
private constructor(
    private val priority: Int
) : IAnimationController, ILoaderLifecycle, IBitmapComponent,
    ICanvasComponent {

    private val _TAG: String by lazy { FrameAnimationDrawable::class.java.simpleName }

    private lateinit var loader: ILoader

    /**
     * @param surfaceView:SurfaceView <p>必填项目,采用SurfaceView进行帧动画加载</p>
     *
     * @param zOrder:Boolean <p>选填项,
     * zOrder 如果是true, 那么帧动画会在所有 View hierachy 顶部进行显示，并且透明显示底部所有View以及图片;
     * zOrder 如果是false, 那么帧动画会在所有 View hierachy 底部进行显示, 底部为黑色画布
     * </p>
     *
     * @param priority:Int <p>选填项, 可以设置线程优先等级，可以在{@link ThreadPriority}查看具体参数</p>
     *
     * @see ThreadPriority
     */
    constructor(surfaceView: SurfaceView,
                zOrder:Boolean = false,
                priority: Int = ThreadPriority.PRIORITY_DEFAULT) : this(priority) {
        loader = SurfaceLoader(surfaceView, zOrder, callback = this)
    }

    /**
     * @param textureView:TextureView <p>必填项目,采用TextureView进行帧动画加载</p>
     *
     * @param priority:Int <p>选填项, 可以设置线程优先等级，可以在{@link ThreadPriority}查看具体参数</p>
     *
     * @see ThreadPriority
     */
    constructor(textureView: TextureView,
                priority: Int = ThreadPriority.PRIORITY_DEFAULT) : this(priority) {
        loader = TextureLoader(textureView, callback = this)
    }

    private var callback: OnAnimationCallback? = null

    fun setOnAnimationCallback(callback: SimpleOnAnimationCallback) {
        this.callback = callback
    }

    fun setOnAnimationCallback(callback: OnAnimationCallback) {
        this.callback = callback
    }

    private val handlerThread: IHandlerThread by lazy { AsyncHandler(priority, handlerRunnable) }

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

    /**
     * 触发生命周期onPause() 会导致 surfaceView触发 surfaceDestroyed(holder:SurfaceHolder) -> close()
     *
     */
    override fun release() {
        cancel()
        animationList.clear()
        loader.onRelease()
        callback?.onRelease()
    }

    override fun cancel() {
        if (isOperationStart) {
            pause()
            isOperationStart = false
            index = 0
            indexRepeat = 0
            //TODO 此处睡眠应采用协程(CoroutineScope)进行挂起处理
            Thread.sleep(64L)
            loader.cleanCanvas()
            callback?.onCancel()
        }
    }


    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceDestroyed()} 将导致触发该方法
     */
    override fun pause() {
        handlerThread.setPaused(true)
        handlerThread.terminate()
        callback?.onPause()
    }

    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceCreated()} 将导致触发该方法
     */
    override fun restore() {
        handlerThread.setPaused(false)
        handlerThread.terminate()
        handlerThread.play()
    }

    @Throws(IllegalArgumentException::class)
    override fun start() {
        if (animationList.size == 0) throw IllegalArgumentException("Animation size must be > 0")
        when{
            !isOperationStart && handlerThread.isPaused -> {
                indexRepeat = 0
                isOperationStart = true
                animationList.sort()
                restore()
                callback?.onStart()
            }
            isOperationStart && handlerThread.isPaused ->{
                restore()
            }
        }
    }

    override fun onLoaderCreated() {
        e(_TAG, "onLoaderCreated")
        if(isOperationStart) {
            restore()
        }
    }

    override fun onLoaderChanged() {
        e(_TAG, "onLoaderChanged")
    }

    override fun onLoaderDestroyed(): Boolean {
        e(_TAG, "onLoaderDestroyed")
        if(isOperationStart) {
            pause()
        }
        return true
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

    private fun getDirty(bitmap: Bitmap?): Rect? {
        bitmap ?: return null
        return Rect(0, 0, bitmap.width, bitmap.height)
    }

    private fun getBitmap(resources: Resources, bean: AnimatorState) = when (bean.animatorType) {
        AnimatorType.RES_ID -> BitmapFactory.decodeResource(resources, bean.resId).ownBitmapFactory()
        AnimatorType.IDENTIFIER -> {
            val identifier: Int =
                resources.getIdentifier(bean.resName, bean.resType, bean.resPackageName)
            if (identifier > 0)
                BitmapFactory.decodeResource(resources, identifier).ownBitmapFactory()
            else null
        }
        AnimatorType.CACHE -> {
            //TODO 当前不支持缓存图片功能
            null
        }
        AnimatorType.BITMAP -> bean.bitmap
        else -> null
    }

    private val handlerRunnable: Runnable = Runnable {
        when {
            loader.context == null -> {
                e(_TAG, "Context is null, terminate frame animation drawable.")
                handlerThread.terminate()
                return@Runnable
            }
            !isOperationStart -> {
                e(_TAG, "Terminate frame animation drawable.")
                handlerThread.terminate()
                return@Runnable
            }
            !checkRepeatCountLoops -> {
                e(_TAG, "repeat count finished.")
                handlerThread.terminate()
                return@Runnable
            }
            !loader.isVisible -> {
                e(_TAG, "This 'SurfaceView' is unvisible.")
                handlerThread.terminate()
                handlerThread.play(618L)
                return@Runnable
            }
            !loader.isMeasured -> {
                e(_TAG, "The 'SurfaceView' has not yet been measured.")
                handlerThread.terminate()
                handlerThread.play(618L)
                return@Runnable
            }
        }
        var bean: AnimatorState = scanAnimatorState()
        if (bean.animatorType != AnimatorType.UNKNOW) {
            val bitmap: Bitmap? = getBitmap(loader.context!!.resources!!, bean)
            loader.lockCanvas(getDirty(bitmap))?.apply {
                drawSurfaceAnimationBitmap(bitmap, paint)
                loader.unlockCanvasAndPost(this)
            }
        }
        handlerThread.play(bean.duration)
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