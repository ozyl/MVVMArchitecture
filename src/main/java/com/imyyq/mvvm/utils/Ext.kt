package com.imyyq.mvvm.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.apkfuns.logutils.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.hjq.toast.ToastUtils
import com.imyyq.mvvm.app.BaseApp
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.base.BaseViewModel
import com.imyyq.mvvm.base.IBaseResponse
import com.imyyq.mvvm.http.HttpHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun obtainColor(resId: Int): Int {
    return ContextCompat.getColor(BaseApp.getInstance(), resId)
}

fun obtainDrawable(resId: Int?): Drawable? {
    return resId?.let {
        ContextCompat.getDrawable(BaseApp.getInstance(), resId)
    }
}

fun obtainDimens(resId: Int): Float {
    return BaseApp.getInstance().resources?.getDimension(resId) ?: 0f
}

fun showToast(msg: String?) {
    if (!msg.isNullOrBlank()) {
        ToastUtils.show(msg)
    }
}

fun showToastResId(@StringRes resId: Int, vararg formatArgs: Any) {
    ToastUtils.show(
        obtainString(
            resId,
            formatArgs
        )
    )
}

fun obtainString(@StringRes resId: Int, vararg formatArgs: Any): String {
    return BaseApp.getInstance().getString(resId, *formatArgs)
}

inline fun <reified T> String.toBeanOrNull(gson:Gson=commonGson): T? =
    try {
        this.toBean<T>()
    } catch (e: Exception) {
        e.printStackTrace()
        LogUtils.e("Json转换实体失败，转换内容：$this，错误信息：$e")
        null
    }

inline fun <reified T> String.toBean(gson:Gson=commonGson): T? =
        gson.fromJson<T>(this, object : TypeToken<T>() {}.type)

val String?.isJson: Boolean
    get() {
        this?:return false
        return try {
            val jsonElement = this.toBeanOrNull<JsonElement>()
            jsonElement?.run {
                jsonElement.isJsonObject || jsonElement.isJsonArray
            }?:false
        } catch (ex: JsonSyntaxException) {
            false
        }
    }


fun String?.nullOrBlankReplace(replaceStr:String):String{
    return  if (this.isNullOrBlank()) replaceStr else this
}

inline fun <reified K, reified V> Any.toMap(): Map<K, V> {
    return commonGson.toJson(this).toBeanOrNull()?: mutableMapOf<K,V>()
}

val commonGson: Gson = GsonBuilder().apply {
    val adapter = CustomizedObjectTypeAdapter()
    registerTypeAdapter(
        object : TypeToken<Map<String, Any>>() {}.type,
        adapter
    )
    registerTypeAdapter(
        object : TypeToken<MutableMap<String, Any>>() {}.type,
        adapter
    )
    registerTypeAdapter(
        object : TypeToken<Map<String, String>>() {}.type,
        adapter
    )
    registerTypeAdapter(
        object : TypeToken<MutableMap<String, String>>() {}.type,
        adapter
    )
    registerTypeAdapter(
        object : TypeToken<Map<Any, Any>>() {}.type,
        adapter
    )
    registerTypeAdapter(
        object : TypeToken<MutableMap<Any, Any>>() {}.type,
        adapter
    )
}.create()


/**
 * 所有网络请求都在 mCoroutineScope 域中启动协程，当页面销毁时会自动取消
 */
fun <T> launch(
    viewModelScope: CoroutineScope,
    block: suspend CoroutineScope.() -> IBaseResponse<T?>?,
    onSuccess: (() -> Unit)? = null,
    onResult: ((t: T) -> Unit)? = null,
    onFailed: ((code: Int, msg: String?, data: T?) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onResultOrNull: ((t: T?) -> Unit)?=null,
    ): Job {
    return viewModelScope.launch {
        try {
            HttpHandler.handleResult(block(), onSuccess, onResult, onFailed,onResultOrNull)
        } catch (e: Exception) {
            if (GlobalConfig.gIsDebug) {
                e.printStackTrace()
            }
            HttpHandler.handleException(e, onFailed, onCancel)
        } finally {
            onComplete?.invoke()
        }
    }
}


inline fun <reified T : BaseViewModel<*>> Fragment.getViewModel(): T {

    return ViewModelProvider(
        this,
        ViewModelProvider.AndroidViewModelFactory(BaseApp.getInstance())
    ).get(T::class.java).apply {
        lifecycle.addObserver(this)
    }
}

inline fun <reified T : BaseViewModel<*>> FragmentActivity.getViewModel(): T {
    return ViewModelProvider(
        this,
        ViewModelProvider.AndroidViewModelFactory(BaseApp.getInstance())
    ).get(T::class.java).apply {
        lifecycle.addObserver(this)
    }

}

inline fun <reified T : BaseViewModel<*>> AppCompatActivity.getViewModel(): T {
    return ViewModelProvider(
        this,
        ViewModelProvider.AndroidViewModelFactory(BaseApp.getInstance())
    ).get(T::class.java).apply {
        lifecycle.addObserver(this)
    }

}


val Context.isValidGlideContext get() = this !is Activity || (!this.isDestroyed && !this.isFinishing)