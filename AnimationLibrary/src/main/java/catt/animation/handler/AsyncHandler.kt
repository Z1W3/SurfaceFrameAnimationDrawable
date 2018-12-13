package catt.animation.handler

import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log
import catt.animation.enums.ThreadPriorityClubs

internal class AsyncHandler(
    @ThreadPriorityClubs override val threadPriority: Int, override val runnable: Runnable)
    : IHandlerThread, Handler(SurfaceLooper.getLooper(threadPriority)) {
    private val _TAG: String = AsyncHandler::class.java.simpleName

    private val threadId: Long
        get() = Thread.currentThread().id

    private val timetakes: Long
        get() = SystemClock.elapsedRealtime() - currentTime

    private var whetherPaused: Boolean = true

    override val isPaused: Boolean
        get() = whetherPaused

    override fun setPaused(pause: Boolean) {
        whetherPaused = pause
    }

    private var currentTime: Long = 0

    override fun handleMessage(msg: Message?) {
        if (whetherPaused) {
            return
        }
        try {
            currentTime = SystemClock.elapsedRealtime()
            runnable.run()
            Log.i(_TAG, " Thread.id = $threadId, time-takes -> $timetakes ms")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun release() {
        looper.quitSafely()
        if(looper.thread.isAlive) looper.thread.interrupt()
    }

    override fun play(duration: Long) {
        removeCallbacksAndMessages(null)
        sendEmptyMessageDelayed(0, duration)
    }

    override fun terminate() {
        removeCallbacksAndMessages(null)
    }
}