package catt.animation.handler

import catt.animation.enums.ThreadPriorityClubs

interface IHandlerThread {

    @ThreadPriorityClubs
    val threadPriority: Int

    val runnable: Runnable

    fun play(duration:Long = 0L)

    fun terminate()

    val isPaused:Boolean

    val isCompleted:Boolean

    fun setPaused(pause:Boolean)

    fun release()

    fun handlerCallback()

    var maxFps:Int
}