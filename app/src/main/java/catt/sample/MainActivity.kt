package catt.sample

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Log.e
import catt.animation.FrameAnimationDrawable
import catt.animation.ScaleConfig
import catt.animation.enums.ThreadPriority
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val _TAG: String by lazy { MainActivity::class.java.simpleName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.drawable.wallpaper)
        setContentView(R.layout.activity_main)

//        val frameAnimator1: FrameAnimationDrawable = FrameAnimationDrawable(
//            surface_view1,
//            zOrder = true,
//            priority = ThreadPriority.PRIORITY_VIDEO
//           ).apply {
//            repeatCount = FrameAnimationDrawable.INFINITE
//            repeatMode = FrameAnimationDrawable.RESTART
//            for (index in 1..30) {
//                addFrame("sparklers_$index", "drawable", packageName)
//            }
//
//            setOnAnimationCallback(object : FrameAnimationDrawable.SimpleOnAnimationCallback() {
//                override fun restore() {
//                    e(_TAG, "restore")
//                }
//
//                override fun onStart() {
//                    e(_TAG, "onStart")
//                }
//
//                override fun onPause() {
//                    e(_TAG, "onPause")
//                }
//
//                override fun onCancel() {
//                    e(_TAG, "onCancel")
//                }
//
//                override fun onRelease() {
//                    e(_TAG, "onRelease")
//                }
//
//            })
//        }




//        val frameAnimator2: FrameAnimationDrawable = FrameAnimationDrawable(texture_view).apply {
//            repeatCount = FrameAnimationDrawable.INFINITE
//            repeatMode = FrameAnimationDrawable.RESTART
//            for (index in 1..26) {
//                addFrame("loading_$index", "drawable", packageName)
//            }
//        }

        val frameAnimator5: FrameAnimationDrawable = FrameAnimationDrawable(texture_view5).apply {
            repeatCount = FrameAnimationDrawable.INFINITE
            repeatMode = FrameAnimationDrawable.RESTART
            setScaleType(ScaleConfig.SCALE_TYPE_FIT_XY)


            val assetPath = "flash"
            val assetFiles = assets.list(assetPath)
            e("aaa", "assetFiles = $assetFiles, ${assetFiles != null && assetFiles.isEmpty()}")
            if (assetFiles != null && !assetFiles.isEmpty()) {
                for (index in assetFiles.indices) {
                    e(_TAG, "path = ${assetPath + File.separator + assetFiles[index]}")
                    addFrame(assetPath + File.separator + assetFiles[index], true)
                }
            }

        }

//        val frameAnimator3: FrameAnimationDrawable = FrameAnimationDrawable(texture_view1, priority = ThreadPriority.PRIORITY_BACKGROUND).apply {
//            repeatCount = FrameAnimationDrawable.INFINITE
//            repeatMode = FrameAnimationDrawable.RESTART
//            for (index in 1..30) {
//                addFrame("sparklers_$index", "drawable", packageName)
//            }
//        }
////
//        val frameAnimator4: FrameAnimationDrawable = FrameAnimationDrawable(WeakReference(surface_view4), priority = ThreadPriority.PRIORITY_BACKGROUND).apply {
//            repeatCount = FrameAnimationDrawable.INFINITE
//            repeatMode = FrameAnimationDrawable.RESTART
//            for (index in 1..26) {
//                addFrame("loading_$index", "drawable", packageName)
//            }
//        }

        start_btn.setOnClickListener {
            e(_TAG, "onStartClick")
//            try {
//                frameAnimator1.start()
//            } catch (ex: IllegalArgumentException) {
//                ex.printStackTrace()
//            }

//            try {
//                frameAnimator2.start()
//            } catch (ex: IllegalArgumentException) {
//                ex.printStackTrace()
//            }

            try {
                frameAnimator5.start()
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            }

//            try {
//                frameAnimator3.start()
//            } catch (ex: IllegalArgumentException) {
//                ex.printStackTrace()
//            }
//            frameAnimator4.start()
        }

        pause_btn.setOnClickListener {
            e(_TAG, "onPauseClick")
//            frameAnimator1.pause()
//            frameAnimator2.pause()
            frameAnimator5.pause()
//            frameAnimator3.pause()
        }

        cancel_btn.setOnClickListener {
            e(_TAG, "onCancelClick")
//            frameAnimator1.cancel()
//            frameAnimator2.cancel()
            frameAnimator5.cancel()
//            frameAnimator3.cancel()
//            frameAnimator4.cancel()
        }

        release_btn.setOnClickListener {
            e(_TAG, "onReleaseClick")
//            frameAnimator1.release()
//            frameAnimator2.release()
            frameAnimator5.release()
//            frameAnimator3.release()
//            frameAnimator4.release()
        }

    }
}
