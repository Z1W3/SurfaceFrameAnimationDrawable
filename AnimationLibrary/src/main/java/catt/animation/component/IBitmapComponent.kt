package catt.animation.component

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.View
import catt.animation.bean.AnimatorState
import catt.animation.enums.AnimatorType
import catt.animation.loader.IToolView
import java.io.ByteArrayOutputStream
import java.lang.ref.SoftReference

interface IBitmapComponent {

    var compressionRatio:Float

    var softInBitmap: SoftReference<Bitmap?>?

    fun clearBitmap(){
        if(softInBitmap != null && softInBitmap!!.get() != null && !softInBitmap!!.get()!!.isRecycled){
            try{
                softInBitmap?.get()?.recycle()
            } catch (ex: Exception){
                ex.printStackTrace()
            }
        }
        softInBitmap?.clear()
        softInBitmap = null
    }

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

    fun compressBitmap(source:Bitmap): Bitmap? {
        var newBitmap: Bitmap? = null
        var baos: ByteArrayOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            source.compress(Bitmap.CompressFormat.WEBP, 50, baos)
            newBitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().size)
            source.recycle()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            baos?.close()
        }
        return newBitmap
    }

    fun generatedOptions(): BitmapFactory.Options = BitmapFactory.Options().apply {
        inMutable = true
        inPremultiplied = true
        inPreferredConfig = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Bitmap.Config.HARDWARE
            else -> Bitmap.Config.RGB_565
//            else -> Bitmap.Config.ARGB_8888
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

    fun findBitmap(toolView: IToolView, bean: AnimatorState){
        val bitmap: Bitmap? = when (bean.animatorType) {
            AnimatorType.RES_ID -> decodeBitmapReal(toolView.view, toolView.resources!!, bean.resId)
            AnimatorType.IDENTIFIER -> {
                val identifier: Int = toolView.resources!!.getIdentifier(bean.resName, bean.resType, bean.resPackageName)
                if (identifier > 0) decodeBitmapReal(toolView.view, toolView.resources!!, identifier)
                else null
            }
            AnimatorType.CACHE -> {
                if (bean.isAssetResource) decodeBitmapReal(toolView.view, toolView.context?.assets, bean.path)
                else decodeBitmapReal(toolView.view, bean.path)
            }
            else -> null
        }

        softInBitmap = when (bitmap == null) {
            true -> {
                SoftReference(null)
            }
            false -> {
                SoftReference(bitmap)
            }
        }
    }
}