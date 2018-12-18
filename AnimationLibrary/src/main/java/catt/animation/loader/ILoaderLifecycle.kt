package catt.animation.loader

interface ILoaderLifecycle {

    /**
     * SurfaceView执行 surfaceCreated
     * TextureView执行 onSurfaceTextureAvailable
     * 都将触发此方法
     */
    fun onSurfaceCreated()

    fun onSurfaceChanged()

    fun onSurfaceDestroyed(clean: Boolean) : Boolean

}