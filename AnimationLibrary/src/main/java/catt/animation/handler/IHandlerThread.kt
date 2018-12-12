package catt.animation.handler

import catt.animation.enums.ThreadPriorityClubs

interface IHandlerThread {

    @ThreadPriorityClubs
    val threadPriority: Int

    val runnable: Runnable

    fun play(duration:Long = 0L)

    fun terminate()

    val isPaused:Boolean

    fun setPaused(pause:Boolean)
}