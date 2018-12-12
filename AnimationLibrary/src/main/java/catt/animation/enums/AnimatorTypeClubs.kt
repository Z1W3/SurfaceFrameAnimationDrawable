package catt.animation.enums

import android.support.annotation.IntDef


@IntDef(
    AnimatorType.UNKNOW,
    AnimatorType.RES_ID,
    AnimatorType.IDENTIFIER,
    AnimatorType.CACHE
)
@Retention(AnnotationRetention.SOURCE)
annotation class AnimatorTypeClubs