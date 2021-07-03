package com.imyyq.mvvm.utils

import com.imyyq.mvvm.app.BaseApp
import com.imyyq.mvvm.utils.NumUtil.asValue
import me.jessyan.autosize.utils.AutoSizeUtils

object DensityUtil {
    fun dp2px(dp: Float): Int {
        return AutoSizeUtils.dp2px(BaseApp.getInstance(), dp)
    }

    fun sp2px(sp: Float): Int {
        return AutoSizeUtils.sp2px(BaseApp.getInstance(), sp)
    }

    inline fun <reified T:Number> Number.dp2px(): T {
        val value = dp2px(this.toFloat())
        return value.asValue()
    }
    inline fun <reified T:Number> Number.px2dp(): T {
        val value = px2dp(this.toFloat())
        return value.asValue()
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(pxValue: Float): Int {
        val scale = BaseApp.getInstance().resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }



    inline fun <reified T:Number> Number.sp2px(): T {
        val value = sp2px(this.toFloat())
        return value.asValue()
    }
}