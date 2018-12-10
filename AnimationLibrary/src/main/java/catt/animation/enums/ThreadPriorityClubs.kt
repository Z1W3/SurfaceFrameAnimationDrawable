package catt.animation.enums

import android.support.annotation.IntDef


@IntDef(
    ThreadPriority.PRIORITY_DEFAULT,
    ThreadPriority.PRIORITY_BACKGROUND,
    ThreadPriority.PRIORITY_UI,
    ThreadPriority.PRIORITY_VIDEO
)
@Retention(AnnotationRetention.SOURCE)
annotation class ThreadPriorityClubs