package com.imyyq.mvvm.utils

import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.imyyq.mvvm.app.BaseApp

fun obtainColor(resId:Int): Int {
    return ContextCompat.getColor(BaseApp.getInstance(),resId)
}

fun showToast(msg: String){
    ToastUtil.showShortToast(msg)
}

fun showToastResId(@StringRes resId:Int,vararg formatArgs:Any){
    ToastUtil.showShortToast(
        obtainString(
            resId,
            formatArgs
        )
    )
}

fun obtainString(@StringRes resId:Int,vararg formatArgs:Any):String{
    return BaseApp.getInstance().getString(resId,*formatArgs)
}

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson(json, T::class.java)

fun <E,K,V> fromObj2Map(json: E):Map<K,V> {
    return Gson().fromJson(Gson().toJson(json))
}