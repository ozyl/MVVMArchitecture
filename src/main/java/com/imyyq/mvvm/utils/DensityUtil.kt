package com.imyyq.mvvm.utils

import com.imyyq.mvvm.app.BaseApp
import me.jessyan.autosize.utils.AutoSizeUtils
import java.math.BigDecimal

object DensityUtil {
    fun dp2px(dp: Float): Int {
        return AutoSizeUtils.dp2px(BaseApp.getInstance(), dp)
    }

    fun sp2px(sp: Float): Int {
        return AutoSizeUtils.sp2px(BaseApp.getInstance(), sp)
    }

    inline fun <reified T> Number.dp2px(): T {
        val value = dp2px(this.toFloat())
        return conversionValue(value)
    }
    inline fun <reified T> Number.px2dp(): T {
        val value = px2dp(this.toFloat())
        return conversionValue(value)
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(pxValue: Float): Int {
        val scale = BaseApp.getInstance().resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }



    inline fun <reified T> Number.sp2px(): T {
        val value = sp2px(this.toFloat())
        return conversionValue(value)
    }
    inline fun <reified T> conversionValue(value: Int): T {
        return when (T::class.java) {
            Integer::class.java -> value
            Double::class.java -> value.toDouble()
            Long::class.java -> value.toLong()
            java.lang.Float::class.java -> value.toFloat()
            Float::class.java -> value.toFloat()
            BigDecimal::class.java -> value.toBigDecimal()
            else -> throw Exception("未处理该类型${T::class.java}")
        } as T
    }
}