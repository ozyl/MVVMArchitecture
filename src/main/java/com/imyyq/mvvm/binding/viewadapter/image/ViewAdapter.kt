package com.imyyq.mvvm.binding.viewadapter.image

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.glide.RoundedCornersExt
import com.imyyq.mvvm.utils.DensityUtil

@SuppressLint("CheckResult")
@BindingAdapter(
    value = ["url", "placeholderRes", "errorRes", "roundingRadius",
        "topLeftRoundingRadius",
        "topRightRoundingRadius",
        "bottomLeftRoundingRadius",
        "bottomRightRoundingRadius",
        "tintColor",
        "isCenterCrop"
    ],
    requireAll = false
)
fun setImageUri(
    imageView: ImageView,
    model: Any?,
    placeholder: Drawable?,
    error: Drawable?,
    roundingRadius: Int?,
    topLeftRoundingRadius: Int?,
    topRightRoundingRadius: Int?,
    bottomLeftRoundingRadius: Int?,
    bottomRightRoundingRadius: Int?,
    tintColor: Int?,
    isCenterCrop: Boolean?
) {
    if (model != null) {
        //使用Glide框架加载图片
        val request = Glide.with(imageView.context)
            .load(model)
        val options = RequestOptions().apply {
            placeholder?.let {
                placeholder(it)
            } ?: run {
                val placeholderRes = GlobalConfig.ImageView.placeholderRes
                placeholderRes?.let { placeholder(placeholderRes) }
            }
            error?.let {
                error(it)
            } ?: kotlin.run {
                val errorRes = GlobalConfig.ImageView.errorRes
                errorRes?.let { error(errorRes) }
            }
            val transformList = mutableListOf<Transformation<Bitmap>>()
            isCenterCrop?.run {
                if (this)
                    transformList.add(CenterCrop())
            }
            roundingRadius?.let {
                transformList.add(RoundedCornersExt(DensityUtil.dp2px(it.toFloat())))
            } ?: run {
                if (topLeftRoundingRadius != null || topRightRoundingRadius != null || bottomLeftRoundingRadius != null || bottomRightRoundingRadius != null) {
                    if ((topRightRoundingRadius ?: 0 + (topLeftRoundingRadius
                            ?: 0) + (bottomLeftRoundingRadius ?: 0) + (bottomRightRoundingRadius
                            ?: 0)) > 0
                    ) {
                        transformList.add(
                            RoundedCornersExt(
                                topLeft = topLeftRoundingRadius ?: 0,
                                topRight = topRightRoundingRadius ?: 0,
                                bottomLeft = bottomLeftRoundingRadius ?: 0,
                                bottomRight = bottomRightRoundingRadius ?: 0
                            )
                        )

                    }
                }
            }
            if (transformList.isNotEmpty()) {
                transform(*transformList.toTypedArray())
            }
        }
        request.apply(options).into(imageView)
    }
    tintColor?.let {
        imageView.imageTintList = ColorStateList.valueOf(tintColor)
    }
}