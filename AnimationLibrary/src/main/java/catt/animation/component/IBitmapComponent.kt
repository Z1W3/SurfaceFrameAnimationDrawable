package catt.animation.component

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import java.lang.ref.SoftReference

interface IBitmapComponent {

    var compressionRatio:Float

    var softInBitmap: SoftReference<Bitmap?>?
//    var oInBitmap: Bitmap?

    val options: BitmapFactory.Options

    fun decodeBitmapReal(view:View?, resources: Resources, resId: Int): Bitmap {
        if(softInBitmap != null && softInBitmap!!.get() != null) options.inBitmap = softInBitmap?.get()
//        if (oInBitmap != null) options.inBitmap = oInBitmap
        return BitmapFactory.decodeResource(resources, resId, options.apply {
            view?:return@apply
            inJustDecodeBounds = true
            inSampleSize = calculateInSampleSize(view.measuredWidth * compressionRatio, view.measuredHeight * compressionRatio)
            inJustDecodeBounds = false
        })
    }

    fun decodeBitmapReal(view:View?, asset: AssetManager?, path:String): Bitmap? {
        if(softInBitmap != null && softInBitmap!!.get() != null) options.inBitmap = softInBitmap?.get()
//                if (oInBitmap != null) options.inBitmap = oInBitmap
        return BitmapFactory.decodeStream(asset?.open(path), null, options.apply {
            view?:return@apply
            inJustDecodeBounds = true
            inSampleSize = calculateInSampleSize(view.measuredWidth * compressionRatio, view.measuredHeight * compressionRatio)
            inJustDecodeBounds = false
        })
    }

    fun decodeBitmapReal(view:View?, path:String): Bitmap {
        if(softInBitmap != null && softInBitmap!!.get() != null) options.inBitmap = softInBitmap?.get()
//                if (oInBitmap != null) options.inBitmap = oInBitmap
        return BitmapFactory.decodeFile(path, options.apply {
            view?:return@apply
            inJustDecodeBounds = true
            inSampleSize = calculateInSampleSize(view.measuredWidth * compressionRatio, view.measuredHeight * compressionRatio)
            inJustDecodeBounds = false
        })
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


    fun calculateInSampleSize(reqWidth: Float, reqHeight: Float): Int {
        // 原始图片的宽高
        if (reqHeight == 0F || reqWidth == 0F) {
            return 1
        }
        val width: Float = options.outWidth.toFloat()
        val height: Float = options.outHeight.toFloat()
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2F
            val halfWidth = width / 2F
            // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
            // The maximum value of the inSampleSize can be obtained on the premise of ensuring that the
            // width and height of the bitmap are larger than the target size
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}