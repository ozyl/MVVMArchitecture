package com.imyyq.mvvm.utils

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.apkfuns.logutils.LogUtils
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.hjq.toast.ToastUtils
import com.imyyq.mvvm.app.BaseApp
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.base.BaseViewModel
import com.imyyq.mvvm.base.DataBindingBaseActivity
import com.imyyq.mvvm.base.IBaseResponse
import com.imyyq.mvvm.http.HttpHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.reflect.Type


fun obtainColor(resId: Int): Int {
    return ContextCompat.getColor(BaseApp.getInstance(), resId)
}

fun obtainDrawable(resId: Int): Drawable? {
    return ContextCompat.getDrawable(BaseApp.getInstance(), resId)
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

inline fun <reified T> String.toBean(): T =
    commonGson.fromJson<T>(this, object : TypeToken<T>() {}.type)

inline fun <reified K, reified V> Any.toMap(): Map<K, V> {
    return commonGson.toJson(this).toBean()
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
    onComplete: (() -> Unit)? = null
): Job {
    return viewModelScope.launch {
        try {
            HttpHandler.handleResult(block(), onSuccess, onResult, onFailed)
        } catch (e: Exception) {
            if (GlobalConfig.gIsDebug) {
                e.printStackTrace()
            }
            onFailed?.let { HttpHandler.handleException(e, it) }
        } finally {
            onComplete?.invoke()
        }
    }
}


inline fun <reified T : BaseViewModel<*>> ViewModelStoreOwner.getViewModel(): T {

    return ViewModelProvider(
        this,
        ViewModelProvider.AndroidViewModelFactory(BaseApp.getInstance())
    ).get(T::class.java)
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
