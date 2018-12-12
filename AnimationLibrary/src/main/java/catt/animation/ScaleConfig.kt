package catt.animation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.support.annotation.IntDef
import android.util.Log.e
import android.view.View


class ScaleConfig {
    private val _TAG:String by lazy { ScaleConfig::class.java.simpleName }

    private val matrixScaleArray: Array<Matrix.ScaleToFit> by lazy {
        arrayOf(Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END)
    }

    private val matrix: Matrix by lazy { Matrix() }

    private var lastFrameWidth: Int = -1
    private var lastFrameHeight: Int = -1
    private var lastFrameScaleType: Int = -1
    private var lastSurfaceWidth: Int = 0
    private var lastSurfaceHeight: Int = 0

    var scaleType: Int = SCALE_TYPE_FIT_CENTER
        set(@ScaleType type) {
            field = when (type < SCALE_TYPE_FIT_XY || type > SCALE_TYPE_CENTER_INSIDE) {
                true -> SCALE_TYPE_FIT_CENTER
                else -> type
            }
        }

    /**
     * 根据ScaleType配置绘制bitmap的Matrix
     */
    fun configureDrawMatrix(view: View, bitmap: Bitmap) : Matrix {
        val dstWidth: Int = view.measuredWidth
        val dstHeight: Int = view.measuredHeight

        val srcWidth: Int = bitmap.width
        val srcHeight: Int = bitmap.height

        val nothingChanged: Boolean =
            srcWidth == lastFrameWidth
                    && srcHeight == lastFrameHeight
                    && lastFrameScaleType == scaleType
                    && lastSurfaceWidth == dstWidth
                    && lastSurfaceHeight == dstHeight

        e(_TAG, "nothingChanged=$nothingChanged")
        if (nothingChanged) {
            return matrix
        }

        lastFrameScaleType = scaleType
        lastFrameHeight = bitmap.height
        lastFrameWidth = bitmap.width
        lastSurfaceHeight = view.measuredHeight
        lastSurfaceWidth = view.measuredWidth

        when (scaleType) {
            SCALE_TYPE_MATRIX -> return matrix
            SCALE_TYPE_CENTER -> matrix.setTranslate(
                Math.round((dstWidth - srcWidth) * 0.5f).toFloat(),
                Math.round((dstHeight - srcHeight) * 0.5f).toFloat()
            )
            SCALE_TYPE_CENTER_CROP -> {
                val scale: Float
                var dx = 0f
                var dy = 0f
                //按照高缩放
                when (dstHeight * srcWidth > dstWidth * srcHeight) {
                    true -> {
                        scale = dstHeight.toFloat() / srcHeight.toFloat()
                        dx = (dstWidth - srcWidth * scale) * 0.5f
                    }
                    false -> {
                        scale = dstWidth.toFloat() / srcWidth.toFloat()
                        dy = (dstHeight - srcHeight * scale) * 0.5f
                    }
                }
                matrix.setScale(scale, scale)
                matrix.postTranslate(dx, dy)
            }
            SCALE_TYPE_CENTER_INSIDE -> {
                //小于dst时不缩放
                val scale: Float = when (srcWidth <= dstWidth && srcHeight <= dstHeight) {
                    true -> 1.0f
                    false -> Math.min(
                        dstWidth.toFloat() / srcWidth.toFloat(),
                        dstHeight.toFloat() / srcHeight.toFloat()
                    )
                }
                val dx: Float = Math.round((dstWidth - srcWidth * scale) * 0.5f).toFloat()
                val dy: Float = Math.round((dstHeight - srcHeight * scale) * 0.5f).toFloat()
                matrix.setScale(scale, scale)
                matrix.postTranslate(dx, dy)
            }
            else -> {
                val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val dstRect = RectF(0f, 0f, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
                matrix.setRectToRect(srcRect, dstRect, matrixScaleArray[scaleType - 1])
            }
        }
        return matrix
    }


    companion object SC{
        @IntDef(
            SCALE_TYPE_FIT_XY,
            SCALE_TYPE_FIT_START,
            SCALE_TYPE_FIT_CENTER,
            SCALE_TYPE_FIT_END,
            SCALE_TYPE_CENTER,
            SCALE_TYPE_CENTER_CROP,
            SCALE_TYPE_CENTER_INSIDE
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class ScaleType

        /**
         * 给定的matrix
         */
        private const val SCALE_TYPE_MATRIX = 0
        /**
         * 完全拉伸，不保持原始图片比例，铺满
         */
        const val SCALE_TYPE_FIT_XY = 1

        /**
         * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
         * 并最终依附在视图的上方或者左方
         */
        const val SCALE_TYPE_FIT_START = 2

        /**
         * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
         * 并最终依附在视图的中心
         */
        const val SCALE_TYPE_FIT_CENTER = 3

        /**
         * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
         * 并最终依附在视图的下方或者右方
         */
        const val SCALE_TYPE_FIT_END = 4

        /**
         * 将图片置于视图中央，不缩放
         */
        const val SCALE_TYPE_CENTER = 5

        /**
         * 整体缩放图片，保持原始比例，将图片置于视图中央，
         * 确保填充满整个视图，超出部分将会被裁剪
         */
        const val SCALE_TYPE_CENTER_CROP = 6

        /**
         * 整体缩放图片，保持原始比例，将图片置于视图中央，
         * 确保X或者Y至少有一个填充满屏幕
         */
        const val SCALE_TYPE_CENTER_INSIDE = 7

    }
}