package catt.animation.component

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build

interface IBitmapComponent {

    val options: BitmapFactory.Options

    fun decodeBitmapReal(resources: Resources, resId: Int, options: BitmapFactory.Options): Bitmap =
        BitmapFactory.decodeResource(resources, resId, options)


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


    fun generatedOptions(): BitmapFactory.Options = BitmapFactory.Options()

    fun Bitmap.simOptions(): BitmapFactory.Options {
        val b: Bitmap = this
        return BitmapFactory.Options().apply {
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
        }
    }
}