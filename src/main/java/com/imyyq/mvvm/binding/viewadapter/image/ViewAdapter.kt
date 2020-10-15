package com.imyyq.mvvm.binding.viewadapter.image

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.imyyq.mvvm.R
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.glide.RoundedCornersExt
import com.imyyq.mvvm.utils.DensityUtil
import com.imyyq.mvvm.utils.obtainColor

@SuppressLint("CheckResult")
@BindingAdapter(
    value = ["url", "placeholderRes", "errorRes", "roundingRadius",
        "topLeftRoundingRadius",
        "topRightRoundingRadius",
        "bottomLeftRoundingRadius",
        "bottomRightRoundingRadius",
        "tintColor"
    ],
    requireAll = false
)
fun setImageUri(
    imageView: ImageView,
    url: String?,
    placeholder: Drawable?,
    error: Drawable?,
    roundingRadius: Int?,
    topLeftRoundingRadius: Int?,
    topRightRoundingRadius: Int?,
    bottomLeftRoundingRadius: Int?,
    bottomRightRoundingRadius: Int?,
    tintColor:Int?
) {
    if (!url.isNullOrBlank()) {
        val newUrl = url.toIntOrNull() ?: url
        //使用Glide框架加载图片
        val request = Glide.with(imageView.context)
            .load(newUrl)
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
            roundingRadius?.let {
                apply(RequestOptions.bitmapTransform(RoundedCornersExt(DensityUtil.dp2px(it.toFloat()))))
            } ?: run {
                if (topLeftRoundingRadius != null || topRightRoundingRadius != null || bottomLeftRoundingRadius != null || bottomRightRoundingRadius != null) {
                    if ((topRightRoundingRadius?:0 + (topLeftRoundingRadius?:0) + (bottomLeftRoundingRadius?:0) + (bottomRightRoundingRadius?:0)) > 0) {
                        apply(
                            RequestOptions.bitmapTransform(
                                RoundedCornersExt(
                                    topLeft = topLeftRoundingRadius ?: 0,
                                    topRight = topRightRoundingRadius ?: 0,
                                    bottomLeft = bottomLeftRoundingRadius ?: 0,
                                    bottomRight = bottomRightRoundingRadius ?: 0
                                )
                            )
                        )
                    }
                }
            }
        }
        request.apply(options).into(imageView)
    }
    tintColor?.let {
        imageView.imageTintList = ColorStateList.valueOf(tintColor)
    }
}