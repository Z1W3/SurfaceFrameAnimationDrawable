package catt.animation

interface OnAnimationCallback{
    fun onStart()

    fun onPause()

    fun onCancel()

    fun restore()

    fun onRelease()
}