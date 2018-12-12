package catt.animation.controller

interface IAnimationController{

    @Throws(IllegalArgumentException::class)
    fun start()

    fun restore()

    fun pause()

    fun cancel()

    fun release()
}