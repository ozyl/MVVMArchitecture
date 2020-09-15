package com.imyyq.mvvm.utils

import com.imyyq.mvvm.app.BaseApp
import me.jessyan.autosize.utils.AutoSizeUtils
import java.math.BigDecimal

object DensityUtil {
    fun dp2px(dp:Float): Int {
        return AutoSizeUtils.dp2px(BaseApp.getInstance(),dp)
    }

    fun sp2px(sp:Float): Int {
        return AutoSizeUtils.sp2px(BaseApp.getInstance(),sp)
    }

    inline fun <reified T> Number.dp2px(): T {
        val value = dp2px(this.toFloat())
        return conversionValue(value)
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
            BigDecimal::class.java -> value.toBigDecimal()
            else -> throw Exception("未处理该类型")
        } as T
    }
}