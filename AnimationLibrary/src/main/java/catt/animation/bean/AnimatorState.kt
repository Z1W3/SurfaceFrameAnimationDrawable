package catt.animation.bean

import android.graphics.Bitmap
import catt.animation.enums.AnimatorType
import catt.animation.enums.AnimatorTypeClubs

internal data class AnimatorState constructor(val serial: Long, val duration: Long) : Comparable<AnimatorState> {
    @AnimatorTypeClubs
    var animatorType: Int = AnimatorType.UNKNOW

    var resId: Int = 0

    constructor(serial: Long, resId: Int, duration: Long) : this(serial, duration) {
        this.resId = resId
        animatorType = AnimatorType.RES_ID
    }

    var resName: String = ""
    var resType: String = ""
    var resPackageName: String = ""

    constructor(serial: Long, resName: String, resType: String, resPackageName: String, duration: Long) : this(
        serial,
        duration
    ) {
        this.resName = resName
        this.resType = resType
        this.resPackageName = resPackageName
        animatorType = AnimatorType.IDENTIFIER
    }

    var path: String = ""
    var isAssetResource: Boolean = true

    constructor(serial: Long, path: String, isAssetResource: Boolean, duration: Long) : this(serial, duration) {
        this.path = path
        this.isAssetResource = isAssetResource
        animatorType = AnimatorType.CACHE
    }

    lateinit var bitmap: Bitmap

    override fun compareTo(other: AnimatorState): Int = this.serial.compareTo(other.serial)
}