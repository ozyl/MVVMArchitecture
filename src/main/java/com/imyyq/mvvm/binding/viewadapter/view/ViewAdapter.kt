package com.imyyq.mvvm.binding.viewadapter.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.http.HttpRequest
import com.imyyq.mvvm.utils.DensityUtil.dp2px
import kotlin.math.roundToInt


@BindingAdapter(
    value = ["onClickCommand", "isInterval", "intervalMilliseconds"],
    requireAll = false
)
fun onClickCommand(
    view: View,
    clickCommand: View.OnClickListener?,
    // xml中没有配置，那么使用全局的配置
    isInterval: Boolean?,
    // 没有配置时间，使用全局配置
    intervalMilliseconds: Int?
) {
    clickCommand ?: return
    if (isInterval ?: GlobalConfig.Click.gIsClickInterval) {
        view.clickWithTrigger(
            (intervalMilliseconds ?: GlobalConfig.Click.gClickIntervalMilliseconds).toLong()
        ) {
            clickCommand.onClick(it)
        }
    } else {
        view.setOnClickListener(clickCommand)
    }
}

@BindingAdapter(
    value = ["multiClickToChangeBaseUrl"]
)
fun multiClickToChangeBaseUrl(
    view: View,
    frequency: Int
) {
    HttpRequest.multiClickToChangeBaseUrl(view, frequency)
}


/**
 * view的显示隐藏
 */
@BindingAdapter(value = ["isVisible", "isGone"], requireAll = false)
fun isVisible(view: View, visibility: Boolean?, gone: Boolean?) {
    visibility?.run {
        view.visibility = if (this) View.VISIBLE else View.INVISIBLE
    }
    gone?.run {
        view.visibility = if (this) View.GONE else View.VISIBLE
    }
}

@BindingAdapter("android:layout_marginStart","android:layout_marginTop","android:layout_marginEnd","android:layout_marginBottom",requireAll = false)
fun setBottomMargin(view: View, start: Float?,top: Float?,end: Float?,bottom: Float?) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(
        start?.roundToInt()?:layoutParams.marginStart,top?.roundToInt()?:layoutParams.topMargin,
        end?.roundToInt()?:layoutParams.marginEnd,bottom?.roundToInt()?:layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}


@BindingAdapter("android:layout_width", "android:layout_height")
fun setLayoutWidth(view: View, width: Float, height: Float) {
    val params: ViewGroup.LayoutParams = view.layoutParams
    params.width = width.dp2px()
    params.height = height.dp2px()
    view.layoutParams = params
}