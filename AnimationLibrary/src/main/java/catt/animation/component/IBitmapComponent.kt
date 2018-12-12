package catt.animation.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build

interface IBitmapComponent {


    fun Bitmap.ownBitmapFactory(): Bitmap {
        val b: Bitmap = this
        return BitmapFactory.Options().run {
            inMutable = true
            inSampleSize = 4
            inJustDecodeBounds = true
            inPreferredConfig = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Bitmap.Config.HARDWARE
                else -> Bitmap.Config.ARGB_8888
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) inDither = true
            inPremultiplied = true
            inBitmap = b
            inJustDecodeBounds = false
            return@run inBitmap
        }
    }
}