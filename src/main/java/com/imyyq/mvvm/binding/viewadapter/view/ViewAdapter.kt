package com.imyyq.mvvm.binding.viewadapter.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.http.HttpRequest
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


@BindingAdapter("android:layout_width")
fun setLayoutWidth(view: View, width: Float) {
    val params: ViewGroup.LayoutParams = view.layoutParams
    params.width = width.toInt()
    view.layoutParams = params
}
@BindingAdapter("android:layout_height")
fun setLayoutHeight(view: View, height: Float) {
    val params: ViewGroup.LayoutParams = view.layoutParams
    params.height = height.toInt()
    view.layoutParams = params
}