package com.imyyq.mvvm.utils

import android.util.Log
import com.apkfuns.logutils.LogUtils
import java.math.BigDecimal

object NumUtil {


    /**
     * 转为指定单位字符串
     */
    fun Number.toFormatStr(
        roundType: Int=BigDecimal.ROUND_DOWN,
        scale:Int=0,
        divide:Int=10000,
        unit:String="万"
    ): String {
        val originValue = BigDecimal(this.toDouble())
        if (this.toDouble()>=divide){
            val resultValue = originValue.divide(BigDecimal(divide))
            return resultValue.setScale(scale,roundType).toString()+unit
        }
        return originValue.setScale(scale,roundType).toString()
    }

    fun Number.multiply(double: Number):BigDecimal{
        return BigDecimal(this.toString()).multiply(BigDecimal(double.toString()))
    }
    fun Number.divide(double: Number,scale: Int=2,roundingMode:Int=BigDecimal.ROUND_HALF_UP):BigDecimal{
        return BigDecimal(this.toString()).divide(BigDecimal(double.toString()),2,roundingMode)
    }

    fun Number.roundDecimalPlaces(scale:Int=2, roundingMode:Int=BigDecimal.ROUND_HALF_UP) =
        BigDecimal(this.toString()).setScale(scale,roundingMode).toDouble()

    /**
     * 转换到目标类型
     */
    inline fun <reified T:Number> Number.asValue(enableWarnLog:Boolean=true): T {

        val result = when (T::class.java) {
            Integer::class.java -> this.toInt()
            Double::class.java -> this.toDouble()
            Long::class.java -> this.toLong()
            java.lang.Float::class.java -> this.toFloat()
            Float::class.java -> this.toFloat()
            BigDecimal::class.java -> this.toDouble().toBigDecimal()
            else -> throw Exception("未处理该类型${T::class.java}")
        } as T
        if (enableWarnLog && this.toDouble() != result.toDouble()){
            repeat(5) {
                LogUtils.e("NumUtil->请注意，精度出现丢失,原始值：$this 转换后：$result;")
            }
            LogUtils.e(Log.getStackTraceString(Throwable()))
        }
        return result
    }
}