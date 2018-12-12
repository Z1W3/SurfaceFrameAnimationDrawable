package catt.animation.callback

interface OnAnimationCallback{
    fun onStart()

    fun onPause()

    fun onCancel()

    fun restore()

    fun onRelease()
}