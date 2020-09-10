package com.imyyq.mvvm.binding.viewadapter.image

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.utils.DensityUtil

@SuppressLint("CheckResult")
@BindingAdapter(value = ["url", "placeholderRes", "errorRes","roundingRadius"], requireAll = false)
fun setImageUri(
    imageView: ImageView,
    url: String?,
    placeholder: Drawable?,
    error: Drawable?,
    roundingRadius:Int?
) {
    if (!url.isNullOrBlank()) {
        val newUrl = url.toIntOrNull()?:url
        //使用Glide框架加载图片
        val request = Glide.with(imageView.context)
            .load(newUrl)
        val options = RequestOptions().apply {
            placeholder?.let {
                placeholder(it)
            }?: run {
                val placeholderRes = GlobalConfig.ImageView.placeholderRes
                placeholderRes?.let { placeholder(placeholderRes) }
            }
            error?.let {
                error(it)
            }?: kotlin.run {
                val errorRes = GlobalConfig.ImageView.errorRes
                errorRes?.let { error(errorRes) }
            }
            roundingRadius?.let {
                apply(RequestOptions.bitmapTransform(RoundedCorners(DensityUtil.dp2px(it.toFloat()))))
            }
        }
        request.apply(options).into(imageView)
    } else {
        imageView.setImageResource(0)
    }
}