[![](https://jitpack.io/v/LuckyCattZW/SurfaceFrameAnimationDrawable.svg)](https://jitpack.io/#LuckyCattZW/SurfaceFrameAnimationDrawable)

# SurfaceFrameAnimationDrawable
使用TextureView 与 SurfaceView 实现帧动画

#### Usage
##### 1.Add Repository
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```groovy
dependencies {
    implementation 'com.github.LuckyCattZW:SurfaceFrameAnimationDrawable:x.y.z'
}
```

##### 2.kotlin version
```groovy
buildscript {
    kotlin_version = '1.3.11'
}
```

##### 3.Code

可加载引用的View
```kotlin
   /**
     * 使用SurfaceView加载帧动画
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
FrameAnimationDrawable constructor(surfaceView: SurfaceView, zOrder:Boolean = false,
                priority: Int = ThreadPriority.PRIORITY_DEFAULT) : this(priority)
```

可加载引用的View
```kotlin
   /**
     * 使用TextureView加载帧动画
     * @param textureView:TextureView <p>必填项目,采用TextureView进行帧动画加载</p>
     *
     * @param priority:Int <p>选填项, 可以设置线程优先等级，可以在{@link ThreadPriority}查看具体参数</p>
     *
     * @see ThreadPriority
     */
FrameAnimationDrawable constructor(textureView: TextureView, 
        priority: Int = ThreadPriority.PRIORITY_DEFAULT) : this(priority)
```

使用方法
```kotlin
val frameAnimator1: FrameAnimationDrawable = FrameAnimationDrawable(
            surfaceView, zOrder = true,  priority = ThreadPriority.PRIORITY_VIDEO)
            .apply {
                /**
                 * 设置循环次数
                 * @see FrameAnimationDrawable.INFINITE 无限循环
                 */
                repeatCount = FrameAnimationDrawable.INFINITE
                
                /**
                 * 设置循环模式
                 *  @see FrameAnimationDrawable.RESTART 从首部循环
                 *  @see FrameAnimationDrawable.REVERSE 首尾连接式循环
                 */
                repeatMode = FrameAnimationDrawable.RESTART
                
                /**
                 * 图片压缩比例 0 ~ 1
                 * 如果比例不满条件则无法压缩
                 *
                 * @see IBitmapComponent.calculateInSampleSize(reqWidth: Float, reqHeight: Float)
                 */
                compressionRatio = 0.5F
                
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
                setMaxFps(60)
                
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
                setScaleType(ScaleConfig.SCALE_TYPE_FIT_XY)
                
                
                for (index in 1..30) {
                    /**
                     *  添加帧动画资源
                     *  {@link addFrame(resId: Int, duration: Long = 0L)}
                     *  
                     *  通过Resources.class 反射资源文件,添加帧动画
                     *  {@link addFrame(resName: String, resType: String, resPackageName: String, duration: Long = 0L)}
                     *  
                     *  从本地图片文件路径,添加帧动画
                     *  {@link addFrame(path: String, isAssetResource: Boolean, duration: Long = 0L)}
                     */
                    addFrame("sparklers_$index", "drawable", packageName)
                }
                
                /*
                 * 设置动画监听
                 */    
                setOnAnimationCallback(object : FrameAnimationDrawable.SimpleOnAnimationCallback(){
                // callback method
                })
            }
```

控制方法
```kotlin
    /**
     * 开启
     */
    frameAnimationDrawable.start()
    
    /**
     * 恢复
     */
    frameAnimationDrawable.restore()
    
    /**
     * 暂停
     */        
    frameAnimationDrawable.pause()
    
    /**
     * 取消
     */        
    frameAnimationDrawable.cancel()   
    
    /**
     * 释放
     */        
    frameAnimationDrawable.release()
```

需要注意的地方
```kotlin
    override fun onPause(){
        super.onPause()
        /**
        * 暂停后你应该主动停止
        * 如果你回到当前页面则会主动恢复动画(自动执行restore())
        */
        frameAnimator.pause()
    }
```

```kotlin
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
     *      //frameAnimator.setOnAnimationCallback(SimpleOnAnimationCallback callback)
     *      private final FrameAnimationDrawable.SimpleOnAnimationCallback callback = 
     *                new FrameAnimationDrawable.SimpleOnAnimationCallback(){
     *          @Override
     *          public void onRelease() {
     *              dismissAllowingStateLoss();
     *          }
     *      };
     *
     * </pre>
     *
     */
    frameAnimator.release()
```


编译问题,如果你的AS在编译的时候出现`AAPT2 error: check logs for details`，请按照此方法操作
```groovy
    implementation('com.github.LuckyCattZW:SurfaceFrameAnimationDrawable:x.y.z') {
        exclude group: "com.android.support"
    }
```