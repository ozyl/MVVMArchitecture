package com.imyyq.mvvm.binding.viewadapter.view

import android.view.View
import androidx.databinding.BindingAdapter
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.http.HttpRequest

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
            (intervalMilliseconds ?: GlobalConfig.Click.gClickIntervalMilliseconds).toLong(),
            clickCommand
        )
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
@BindingAdapter(value = ["isVisible"], requireAll = false)
fun isVisible(view: View, visibility: Boolean) {
    if (visibility) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}