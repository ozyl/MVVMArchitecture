package com.imyyq.mvvm.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

object NumUtil {


    /**
     * 转为指定单位字符串
     */
    fun Number.toFormatStr(
        roundType: Int=BigDecimal.ROUND_DOWN,
        scale:Int=0,
        divide:Int=100000,
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
    fun Number.divide(double: Number):BigDecimal{
        return BigDecimal(this.toString()).divide(BigDecimal(double.toString()))
    }

}