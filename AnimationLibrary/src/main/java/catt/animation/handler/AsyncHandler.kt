package catt.animation.handler

import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.Log.*
import catt.animation.enums.ThreadPriorityClubs

internal class AsyncHandler(
    @ThreadPriorityClubs override val threadPriority: Int, override val runnable: Runnable)
    : IHandlerThread, Handler(SurfaceLooper.getLooper(threadPriority)) {

    private val _TAG: String = AsyncHandler::class.java.simpleName

    private var whetherCompleted:Boolean = false

    override val isCompleted:Boolean
        get() = whetherCompleted

    private val threadId: Long
        get() = Thread.currentThread().id

    private val timetakes: Long
        get() = SystemClock.elapsedRealtime() - currentTime

    private var currentTime: Long = 0

    private var whetherPaused: Boolean = true

    override val isPaused: Boolean
        get() = whetherPaused

    override fun setPaused(pause: Boolean) {
        whetherPaused = pause
    }

    override var maxFps: Int = 16
        get() = when(field > timetakes){
            true-> field
            false-> timetakes.toInt()
        }
        set(frame) {
            field = when (frame > 60 || frame < 0) {
                true -> 1000 / 60
                false -> 1000 / frame
            }
        }


    override fun handleMessage(msg: Message?) {
        if (whetherPaused) {
            return
        }
        handlerCallback()
    }

    override fun handlerCallback() {
        whetherCompleted = false
        try {
            currentTime = SystemClock.elapsedRealtime()
            runnable.run()
            v(_TAG, " Thread.id = $threadId, time-takes -> $timetakes ms, looper=$looper")
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            whetherCompleted = true
        }
    }

    override fun release() {
        whetherPaused = true
        removeCallbacksAndMessages(null)
        if(whetherPaused){
            looper.quitSafely()
        }
    }

    override fun play(duration: Long) {
        removeCallbacksAndMessages(null)
        if(!whetherPaused){
            sendEmptyMessageDelayed(0, duration)
        }
    }

    override fun terminate() {
        removeCallbacksAndMessages(null)
    }
}