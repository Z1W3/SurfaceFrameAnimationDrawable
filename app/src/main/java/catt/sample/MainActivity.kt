package catt.sample

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log.e
import catt.animation.FrameAnimationDrawable
import catt.animation.enums.ThreadPriority
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private val _TAG: String by lazy { MainActivity::class.java.simpleName }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val frameAnimator1: FrameAnimationDrawable = FrameAnimationDrawable(
            WeakReference(surface_view1),
            priority = ThreadPriority.PRIORITY_VIDEO,
            callback = object : FrameAnimationDrawable.SimpleOnAnimationCallback() {
                override fun onRelease() {
                    e(_TAG, "onRelease")
                }

            }).apply {
            repeatCount = FrameAnimationDrawable.INFINITE
            repeatMode = FrameAnimationDrawable.RESTART
            for (index in 1..30) {
                addFrame("sparklers_$index", "drawable", packageName)
            }
        }

        val frameAnimator2: FrameAnimationDrawable = FrameAnimationDrawable(WeakReference(surface_view2),  priority = ThreadPriority.PRIORITY_BACKGROUND).apply {
            repeatCount = FrameAnimationDrawable.INFINITE
            repeatMode = FrameAnimationDrawable.RESTART
            for (index in 1..26) {
                addFrame("loading_$index", "drawable", packageName)
            }
        }

        val frameAnimator3: FrameAnimationDrawable = FrameAnimationDrawable(WeakReference(surface_view3), priority = ThreadPriority.PRIORITY_BACKGROUND).apply {
            repeatCount = FrameAnimationDrawable.INFINITE
            repeatMode = FrameAnimationDrawable.RESTART
            for (index in 1..26) {
                addFrame("loading_$index", "drawable", packageName)
            }
        }

        val frameAnimator4: FrameAnimationDrawable = FrameAnimationDrawable(WeakReference(surface_view4), priority = ThreadPriority.PRIORITY_BACKGROUND).apply {
            repeatCount = FrameAnimationDrawable.INFINITE
            repeatMode = FrameAnimationDrawable.RESTART
            for (index in 1..26) {
                addFrame("loading_$index", "drawable", packageName)
            }
        }

        start_btn.setOnClickListener {
            frameAnimator1.start()
            frameAnimator2.start()
            frameAnimator3.start()
            frameAnimator4.start()
        }

        cancel_btn.setOnClickListener {
            frameAnimator1.cancel()
            frameAnimator2.cancel()
            frameAnimator3.pause()
            frameAnimator4.pause()
        }

    }
}
