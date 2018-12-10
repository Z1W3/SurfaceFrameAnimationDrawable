package catt.animation.handler

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import catt.animation.enums.ThreadPriority
import catt.animation.enums.ThreadPriorityClubs
import java.util.*

object SurfaceLooper {

    @JvmStatic
    fun getLooper(@ThreadPriorityClubs priority:Int) : Looper{
        val thread = HandlerThread("Looper:${UUID.randomUUID()}", convertPriority(priority))
        thread.start()
        return thread.looper
    }


    @JvmStatic
    fun convertPriority(@ThreadPriorityClubs priority:Int):Int = when (priority) {
        ThreadPriority.PRIORITY_BACKGROUND -> Process.THREAD_PRIORITY_BACKGROUND
        ThreadPriority.PRIORITY_UI -> Process.THREAD_PRIORITY_FOREGROUND
        ThreadPriority.PRIORITY_VIDEO -> Process.THREAD_PRIORITY_VIDEO
        ThreadPriority.PRIORITY_MAX -> Process.THREAD_PRIORITY_URGENT_AUDIO
        else -> Process.THREAD_PRIORITY_DEFAULT
    }
}