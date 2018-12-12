package catt.animation.component

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build

interface IBitmapComponent {

    var ownInBitmap: Bitmap?

    val options: BitmapFactory.Options

    fun decodeBitmapReal(resources: Resources, resId: Int): Bitmap {
        if (ownInBitmap != null) options.inBitmap = ownInBitmap
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    fun decodeBitmapReal(asset: AssetManager?, path:String): Bitmap? {
        if (ownInBitmap != null) options.inBitmap = ownInBitmap
        return BitmapFactory.decodeStream(asset?.open(path), null, options)
    }

    fun decodeBitmapReal(path:String): Bitmap {
        if (ownInBitmap != null) options.inBitmap = ownInBitmap
        return BitmapFactory.decodeFile(path, options)
    }

    fun generatedOptions(): BitmapFactory.Options = BitmapFactory.Options().apply {
        inMutable = true
        inSampleSize = 1
        inPremultiplied = true
        inPreferredConfig = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Bitmap.Config.HARDWARE
            else -> Bitmap.Config.ARGB_8888
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) inDither = true
    }
}