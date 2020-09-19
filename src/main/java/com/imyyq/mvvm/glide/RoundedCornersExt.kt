package com.imyyq.mvvm.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest


/** A [BitmapTransformation] which rounds the corners of a bitmap.  */
class RoundedCornersExt(
    val topLeft: Int=0,
    val topRight: Int=0,
    val bottomLeft: Int=0,
    val bottomRight: Int=0
) : BitmapTransformation() {

    constructor(roundingRadius: Int) : this(
        roundingRadius,
        roundingRadius,
        roundingRadius,
        roundingRadius
    )

    override fun transform(
        pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int
    ): Bitmap {
        return TransformationUtils.roundedCorners(
            pool,
            toTransform,
            topLeft.toFloat(),
            topRight.toFloat(),
            bottomLeft.toFloat(),
            bottomRight.toFloat()
        )
    }


    override fun equals(other: Any?): Boolean {
        if (other is RoundedCornersExt) {
            return topRight == other.topRight && topLeft == other.topLeft && other.bottomLeft == bottomLeft && other.bottomRight == other.bottomLeft
        }
        return false
    }

    override fun hashCode(): Int {
        return Util.hashCode(
            ID.hashCode(),
            Util.hashCode(topLeft + topRight + bottomLeft + bottomRight)
        )
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        val radiusData =
            ByteBuffer.allocate(4).putInt(topLeft + topRight + bottomLeft + bottomRight).array()
        messageDigest.update(radiusData)
    }

    companion object {
        private const val ID = "com.imyyq.mvvm.glide.RoundedCornersExt"
        private val ID_BYTES: ByteArray =
            Companion.ID.toByteArray(Key.CHARSET)
    }

    /**
     * @param roundingRadius the corner radius (in device-specific pixels).
     * @throws IllegalArgumentException if rounding radius is 0 or less.
     */
    init {
        Preconditions.checkArgument(
            topLeft+topRight+bottomRight+bottomLeft > 0,
            "roundingRadius must be greater than 0."
        )
    }
}
