package catt.animation

import android.content.res.Resources
import android.graphics.*
import android.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.os.*
import android.support.annotation.FloatRange
import android.support.annotation.IntRange
import android.util.Log.*
import catt.animation.bean.AnimatorState
import catt.animation.callback.OnAnimationCallback
import catt.animation.component.IBitmapComponent
import catt.animation.component.ICanvasComponent
import catt.animation.controller.IAnimationController
import catt.animation.enums.AnimatorType
import catt.animation.enums.ThreadPriority
import catt.animation.handler.IHandlerThread
import catt.animation.handler.AsyncHandler
import catt.animation.loader.IToolView
import catt.animation.loader.ILoaderLifecycle
import catt.animation.loader.SurfaceViewTool
import catt.animation.loader.TextureViewTool
import kotlinx.coroutines.*
import java.lang.ref.SoftReference

/**
 * <h3>帧布局动画</h3>
 * <p>
 *     采用 SurfaceView 与 TextureView进行图片加载帧动画
 *
 *     优点: 1.播放帧动画不会占用主线程
 *           2.永远不会oom
 *           3.可以加载大图片进行帧动画
 *
 *     缺点: 1.耗费大量cpu进行绘制
 *           2.设备容易发热
 *           3.耗电量较高
 * </p>
 */
class FrameAnimationDrawable
private constructor(
        private val priority: Int
) : IAnimationController, ILoaderLifecycle, IBitmapComponent,
        ICanvasComponent {

    private val _TAG: String by lazy { FrameAnimationDrawable::class.java.simpleName }

    /**
     * 构造函数中进行初始化
     * @see SurfaceViewTool
     * @see TextureViewTool
     */
    private lateinit var toolView: IToolView

    private val scaleConfig: ScaleConfig by lazy { ScaleConfig() }

    override val paint: Paint by lazy { generatedBasePaint() }

    override val options: BitmapFactory.Options by lazy { generatedOptions() }

    /**
     * 异步执行线程
     */
    private val handlerThread: IHandlerThread by lazy { AsyncHandler(priority, handlerRunnable) }

    /**
     * 存储帧动画集合
     */
    private val animationList: MutableList<AnimatorState> by lazy { Collections.synchronizedList(ArrayList<AnimatorState>()) }

    override var softInBitmap: SoftReference<Bitmap?>? = null

    /**
     * 记录帧动画集合位置
     */
    private var position: Int = 0

    /**
     * 记录帧动画集合重复次数
     */
    private var repeatPosition: Int = 0

    /**
     * 动画监听
     */
    private var callback: OnAnimationCallback? = null

    /**
     * 判断是否开始动画
     */
    private var isOperationStart: Boolean = false


    /**
     * 检查是否到达重复次数
     */
    private val checkRepeatCountLoops: Boolean
        get() = when (repeatCount) {
            INFINITE -> true
            else -> repeatPosition <= repeatCount
        }

    /**
     * 图片压缩比例 0 ~ 1
     * 如果比例不满条件则无法压缩
     *
     * @see IBitmapComponent.calculateInSampleSize(reqWidth: Float, reqHeight: Float)
     */
    @FloatRange(from = 0.0, to = 1.0)
    override var compressionRatio: Float = 1F
        set(ratio) {
            field = when {
                ratio > 1F -> 1F
                ratio < 0F -> 0F
                else -> ratio
            }
        }

    /**
     * @param surfaceView:SurfaceView <p>必填项目,采用SurfaceView进行帧动画加载</p>
     *
     * @param zOrder:Boolean <p>选填项,
     * zOrder 如果是true, 那么帧动画会在所有 View hierachy 顶部进行显示，并且透明显示底部所有View以及图片;
     * zOrder 如果是false, 那么帧动画会在所有 View hierachy 底部进行显示, 底部为黑色画布
     * </p>
     * @see SurfaceView.setZOrderOnTop(onTop:Boolean)
     * @see SurfaceView.setZOrderMediaOverlay(isMediaOverlay:Boolean)
     *
     * @param priority:Int <p>选填项, 可以设置线程优先等级，可以在{@link ThreadPriority}查看具体参数</p>
     *
     * @see ThreadPriority
     */
    constructor(
            surfaceView: SurfaceView,
            zOrder: Boolean = false,
            priority: Int = ThreadPriority.PRIORITY_DEFAULT
    ) : this(priority) {
        toolView = SurfaceViewTool(surfaceView, zOrder, callback = this)
    }

    /**
     * @param textureView:TextureView <p>必填项目,采用TextureView进行帧动画加载</p>
     *
     * @param priority:Int <p>选填项, 可以设置线程优先等级，可以在{@link ThreadPriority}查看具体参数</p>
     *
     * @see ThreadPriority
     */
    constructor(
            textureView: TextureView,
            priority: Int = ThreadPriority.PRIORITY_DEFAULT
    ) : this(priority) {
        toolView = TextureViewTool(handlerThread, textureView, callback = this)
    }

    /**
     * 如果你主动进行释放，只有重新构建 {@link FrameAnimationDrawable} 对象
     *
     * 当你主动结束一个页面前，同时应该主动进行释放，达到快速释放内存的目的
     *
     *
     * example:
     * <pre>
     *       @Override
     *       public void onClick(View v) {
     *          switch (v.getId()) {
     *              case R.id.exit_btn: //退出按钮
     *              release()
     *              break;
     *          }
     *      }
     *
     *      private final FrameAnimationDrawable.SimpleOnAnimationCallback callback = new FrameAnimationDrawable.SimpleOnAnimationCallback(){
     *          @Override
     *          public void onRelease() {
     *              dismissAllowingStateLoss();
     *          }
     *      };
     *
     * </pre>
     *
     */
    override fun release() {
        i(_TAG, "release: $toolView")
        GlobalScope.launch (Dispatchers.Main){
            handlerThread.release()
            isOperationStart = false
            position = 0
            repeatPosition = 0
            animationList.clear()

            withContext(Dispatchers.Unconfined){
                while (!handlerThread.isCompleted){
                    delay(16L)
                }
            }
            toolView.onRelease()
            clearBitmap()
            System.runFinalization()
            callback?.onRelease()
        }
    }

    override fun cancel() {
        pause()
        i(_TAG, "cancel: $toolView")
        isOperationStart = false
        position = 0
        repeatPosition = 0
        toolView.lockCanvas(null)?.apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            toolView.unlockCanvasAndPost(this)
        }
        callback?.onCancel()
    }


    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceDestroyed()} 将导致触发该方法
     * @hint 如果使用TextureView进行绘制,应该在Activity.onPause/.onStop中执行此方法，
     *       否则会有概率出现严重的异常现象(android.os.DeadObjectException)
     * @see android.os.DeadObjectException
     */
    override fun pause() {
        if (!handlerThread.isPaused) {
            i(_TAG, "pause: $toolView")
            handlerThread.setPaused(true)
            handlerThread.terminate()
            callback?.onPause()
        }
    }

    /**
     * 触发 {@link SurfaceHolder.Callback.surfaceCreated()} 将导致触发该方法
     */
    override fun restore() {
        if (handlerThread.isPaused) {
            i(_TAG, "restore: $toolView")
            handlerThread.setPaused(false)
            handlerThread.terminate()
            handlerThread.play()
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun start() {
        i(_TAG, "start: $toolView")
        if (animationList.size == 0) throw IllegalArgumentException("Animation size must be > 0")
        when {
            !isOperationStart && handlerThread.isPaused -> {
                repeatPosition = 0
                isOperationStart = true
                animationList.sort()
                restore()
                callback?.onStart()
            }
            isOperationStart && handlerThread.isPaused -> {
                restore()
            }
        }
    }

    /**
     * 设置最大FPS帧数，帧数越大图片替换速度越快。
     *
     * 最高可以达到16ms替换一次
     * (设置再高的帧数没有实质的意义，Android OS 程刷新屏幕时间间隔最高为16ms释放一次VSYNC信号)
     *
     * 需要注意的是，当你的图片越来越大掉帧率将提高，原因是canvas将耗费相当长的时间
     * 所以尽量压缩你图片.
     *
     * 推荐将你的图片格式由.jpg/.png转换成google首推的.webp格式
     * @about .webp (https://www.zhihu.com/question/27201061)
     */
    fun setMaxFps(@IntRange(from = 0L, to = 60L) frame:Int){
        handlerThread.maxFps = frame
    }

    override fun onSurfaceCreated() {
        i(_TAG, "onSurfaceCreated: $toolView")
        if (isOperationStart) {
            restore()
        }
    }

    override fun onSurfaceChanged() {
        i(_TAG, "onSurfaceChanged: $toolView")
    }



    override fun onSurfaceDestroyed(clean:Boolean): Boolean {
        w(_TAG, "onSurfaceDestroyed: $toolView")
        if (isOperationStart) {
            pause()
        }
        return clean
    }

    /**
     * 设置图片缩放类型
     *
     * <p>完全拉伸，不保持原始图片比例，铺满 </p>
     * @see ScaleConfig.SC.SCALE_TYPE_FIT_XY
     * <p>
     *     保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     *     并最终依附在视图的上方或者左方
     * </p>
     * @see ScaleConfig.SC.SCALE_TYPE_FIT_START
     * <p>
     *     保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     *     并最终依附在视图的中心
     * </p>
     * @see ScaleConfig.SC.SCALE_TYPE_FIT_CENTER
     * <p>
     *     保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     *     并最终依附在视图的下方或者右方
     * </p>
     * @see ScaleConfig.SC.SCALE_TYPE_FIT_END
     *
     * <p> 将图片置于视图中央，不缩放 </p>
     * @see ScaleConfig.SC.SCALE_TYPE_CENTER
     * <p>
     *     整体缩放图片，保持原始比例，将图片置于视图中央，
     *     确保填充满整个视图，超出部分将会被裁剪
     * </p>
     * @see ScaleConfig.SC.SCALE_TYPE_CENTER_CROP
     * <p>
     *     整体缩放图片，保持原始比例，将图片置于视图中央，
     *     确保X或者Y至少有一个填充满屏幕
     * </p>
     * @see ScaleConfig.SC.SCALE_TYPE_CENTER_INSIDE
     *
     */
    fun setScaleType(@ScaleConfig.SC.ScaleType scaleType: Int) {
        scaleConfig.scaleType = scaleType
    }

    /**
     * 设置动画监听
     */
    fun setOnAnimationCallback(callback: SimpleOnAnimationCallback) {
        this.callback = callback
    }

    /**
     * 设置动画监听
     */
    fun setOnAnimationCallback(callback: OnAnimationCallback) {
        this.callback = callback
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

    /**
     * 添加帧动画资源
     * @param resId: Int 资源id
     * @param duration: Long 延时时间 默认0ms
     */
    fun addFrame(resId: Int) {
        animationList.add(AnimatorState(SystemClock.elapsedRealtime(), resId))
    }

    /**
     * 通过Resources.class 反射资源文件,添加帧动画
     *
     * @param resName: String 资源名称
     * @param resType: String 资源类型
     * @param resPackageName: String 资源所在包的包名
     * @param duration: Long 延时时间 默认0ms
     *
     * @see Resources.getIdentifier(String name, String defType, String defPackage)
     */
    fun addFrame(resName: String, resType: String, resPackageName: String) {
        animationList.add(AnimatorState(SystemClock.elapsedRealtime(), resName, resType, resPackageName))
    }

    /**
     * 从本地图片文件路径,添加帧动画
     * @param path: String 文件路径 路径
     * @param isAssetResource: Boolean 是否是资产文件
     * @param duration: Long 延时时间 默认0ms
     */
    fun addFrame(path: String, isAssetResource: Boolean) {
        animationList.add(AnimatorState(SystemClock.elapsedRealtime(), path, isAssetResource))
    }

    private fun scanAnimatorState(): AnimatorState {
        if (animationList.size == position) {
            when (repeatMode) {
                REVERSE -> animationList.reverse()
                RESTART -> animationList.sort()
            }
            position = 0
            ++repeatPosition
        }
        val o: AnimatorState = animationList[position]
        ++position
        return o
    }

    private val handlerRunnable: Runnable = Runnable {
        when {
            toolView.context == null -> {
                w(_TAG, "Context is null, terminate frame animation drawable.")
                handlerThread.terminate()
                return@Runnable
            }
            !isOperationStart -> {
                w(_TAG, "Terminate frame animation drawable.")
                handlerThread.terminate()
                return@Runnable
            }
            !checkRepeatCountLoops -> {
                w(_TAG, "repeat count finished.")
                handlerThread.terminate()
                return@Runnable
            }
            !toolView.isVisible -> {
                w(_TAG, "This '$toolView' is unvisible.")
                handlerThread.terminate()
                handlerThread.play(handlerThread.maxFps.toLong())
                return@Runnable
            }
            !toolView.isMeasured -> {
                w(_TAG, "The '$toolView' has not yet been measured.")
                handlerThread.terminate()
                handlerThread.play(handlerThread.maxFps.toLong())
                return@Runnable
            }
        }

        val bean: AnimatorState = scanAnimatorState()
        try {
            if (bean.animatorType != AnimatorType.UNKNOW) {
                findBitmap(toolView, bean)
                softInBitmap?.get()?.apply bitmap@{
                    val matrix:Matrix = scaleConfig.configureDrawMatrix(toolView.view, this@bitmap)
                    toolView.lockCanvas()?.apply {
                        if(!handlerThread.isPaused)
                            drawSurfaceAnimationBitmap(handlerThread, this@bitmap, matrix, paint)
                        toolView.unlockCanvasAndPost(this)
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            handlerThread.play(handlerThread.maxFps.toLong())
        }
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