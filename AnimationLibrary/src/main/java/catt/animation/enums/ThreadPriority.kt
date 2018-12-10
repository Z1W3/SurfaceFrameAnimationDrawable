package catt.animation.enums

object ThreadPriority {

    /**
     * 执行线程效率等同用于android UI线程
     * CPU处理优先级一般
     *
     * @see android.os.Process.THREAD_PRIORITY_DEFAULT
     */
    const val PRIORITY_DEFAULT: Int = 0

    /**
     * 执行线程效率等同用于android UI线程
     * CPU处理优先级低
     *
     * @see android.os.Process.THREAD_PRIORITY_BACKGROUND
     */
    const val PRIORITY_BACKGROUND: Int = 1


    /**
     * 执行线程效率等同用于android UI线程
     * CPU处理优先级高
     *
     * @see android.os.Process.THREAD_PRIORITY_FOREGROUND
     */
    const val  PRIORITY_UI: Int = 2

    /**
     * 执行线程效率等同于视频播放级别
     * CPU处理优先级最高
     *
     * @see android.os.Process.THREAD_PRIORITY_VIDEO
     */
    const val PRIORITY_VIDEO: Int = 3

    /**
     * 执行线程效率等同于视频音频播放级别
     * CPU处理优先级最高
     *
     * @see android.os.Process.THREAD_PRIORITY_URGENT_AUDIO
     * @hide 不建议使用
     */
    const val PRIORITY_MAX:Int = 4
}