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
    value = ["onLongClickCommand"],
    requireAll = false
)
fun onLongClickCommand(
    view: View,
    clickCommand: View.OnLongClickListener?,
) {
    clickCommand ?: return
    view.setOnLongClickListener(clickCommand)
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
 * @param isShowStrict 该参数为true时只有在对应显示下状态的才会修改
 * 避免visibility和gone冲突
 */
@BindingAdapter(value = ["isVisible", "isGone","isShowStrict"], requireAll = false)
fun isVisible(view: View, visibility: Boolean?, gone: Boolean?, isShowStrict: Boolean?, ){
    visibility?.run {
        view.visibility = if (this) View.VISIBLE else View.INVISIBLE
    }
    gone?.run {
        if (isShowStrict == true){
            if (this){
                view.visibility = View.GONE
            }else{
                if (view.visibility==View.GONE){
                    view.visibility = View.VISIBLE
                }
            }
        }else{
            view.visibility = if (this) View.GONE else View.VISIBLE
        }
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