package catt.animation.handler

import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.util.Log.e
import catt.animation.enums.ThreadPriorityClubs

internal class AsyncHandler(
    @ThreadPriorityClubs override val threadPriority: Int, override val runnable: Runnable
) : IHandlerThread, Handler(SurfaceLooper.getLooper(threadPriority)) {

    private var isPause: Boolean = false

    override fun setPaused(pause: Boolean) {
        isPause = pause
    }

    private val _TAG: String = AsyncHandler::class.java.simpleName
    private var currentTime: Long = 0


    override fun handleMessage(msg: Message?) {
        if (isPause) {
            return
        }
        try {
            currentTime = SystemClock.elapsedRealtime()
            runnable.run()
            Log.i(
                _TAG,
                " Thread.id = ${Thread.currentThread().id}, time-takes -> ${SystemClock.elapsedRealtime() - currentTime} ms"
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun play(duration: Long) {
        removeCallbacksAndMessages(null)
        sendEmptyMessageDelayed(0, duration)
    }

    override fun release() {
        removeMessages(0)
        removeCallbacksAndMessages(null)
    }
}