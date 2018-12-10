package catt.animation

import android.content.Context
import android.content.res.Resources
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import catt.animation.enums.ThreadPriorityClubs
import java.lang.ref.Reference

internal interface ISurfaceAnimation {

    val reference: Reference<SurfaceView>
    @ThreadPriorityClubs
    val priority: Int

    val surfaceView: SurfaceView?

    val surfaceHolder: SurfaceHolder?

    val surface: Surface?

    val resources: Resources?

    val context: Context?

    val isVisible: Boolean

    val isMeasured: Boolean
}