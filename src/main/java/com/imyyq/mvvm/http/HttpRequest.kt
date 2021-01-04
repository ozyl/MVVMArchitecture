package com.imyyq.mvvm.http

import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import com.apkfuns.logutils.LogUtils
import com.imyyq.mvvm.R
import com.imyyq.mvvm.app.AppActivityManager
import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.base.IBaseResponse
import com.imyyq.mvvm.http.interceptor.HeaderInterceptor
import com.imyyq.mvvm.utils.AppUtil
import com.imyyq.mvvm.utils.Utils
import com.safframework.http.interceptor.LoggingInterceptor
import mmkv
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.*
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import string
import java.io.IOException
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Created by 杨永青 on 16/9/11.
 *
 *
 *
 *
 * 目的1：没网的时候，尝试读取缓存，避免界面空白，只需要addInterceptor和cache即可（已实现）
 * 目的2：有网的时候，总是读取网络上最新的，或者设置一定的超时时间，比如10秒内有多个同一请求，则都从缓存中获取（没实现）
 * 目的3：不同的接口，不同的缓存策略（？）
 */
object HttpRequest {

    const val MODIFY_BASE_URL_KEY = "modifyBaseUrl"

    // 缓存 service
    private val mServiceMap = ArrayMap<String, Any>()

    // 默认的 baseUrl
    lateinit var mDefaultBaseUrl: String

    // 默认的请求头
    private lateinit var mDefaultHeader: ArrayMap<String, String>

    /**
     * 存储 baseUrl，以便可以动态更改
     */
    var spModifyBaseUrl by mmkv.string(MODIFY_BASE_URL_KEY)
    lateinit var modifyBaseUrl: String

    /**
     * 请求超时时间，秒为单位
     */
    var mDefaultTimeout = 10

    /**
     * 添加默认的请求头
     */
    @JvmStatic
    fun addDefaultHeader(name: String, value: String) {
        if (!this::mDefaultHeader.isInitialized) {
            mDefaultHeader = ArrayMap()
        }
        mDefaultHeader[name] = value
    }

    /**
     * 获取
     */
    private fun getRxJavaAdapter(): Any? {
        try {
            return RxJava2CallAdapterFactory.create()
        } catch (e: Throwable) {
        }
        return null
    }

    /**
     * 如果有不同的 baseURL，那么可以相同 baseURL 的接口都放在一个 Service 钟，通过此方法来获取
     */
    @JvmStatic
    fun <T> getService(cls: Class<T>, host: String, vararg interceptors: Interceptor?): T {
        val name = cls.name

        var obj: Any? = mServiceMap[name]
        if (obj == null) {
            val httpClientBuilder = OkHttpClient.Builder()
            httpClientBuilder.proxy(Proxy.NO_PROXY)
            // 超时时间
            httpClientBuilder.connectTimeout(mDefaultTimeout.toLong(), TimeUnit.SECONDS)

            // 拦截器
            interceptors.forEach { interceptor ->
                interceptor?.let {
                    httpClientBuilder.addInterceptor(it)
                }
            }

            httpClientBuilder
                .addInterceptor(
                    LoggingInterceptor.Builder()
                        .loggable(AppUtil.isDebug())
                        .request()
                        .response()
                        .hideVerticalLine()
                        .requestTag("Request")
                        .responseTag("Response")
                        .build()
                )
            val client = httpClientBuilder.build()
            val builder = Retrofit.Builder().client(client)
                // 基础url
                .baseUrl(host)
                // JSON解析
                .addConverterFactory(GsonConverterFactory.create())
            if (GlobalConfig.gIsNeedChangeBaseUrl) {
                if (!this::modifyBaseUrl.isInitialized) {
                    modifyBaseUrl = spModifyBaseUrl ?: ""
                }
                builder.callFactory(object : okhttp3.Call.Factory {
                    override fun newCall(request: Request): okhttp3.Call {
                        return if (modifyBaseUrl.isNotBlank()) {
                            val url = request.url.toString()
                            // 防止尾缀有问题
                            if (mDefaultBaseUrl.endsWith("/") && !modifyBaseUrl.endsWith("/")) {
                                modifyBaseUrl += "/"
                            } else if (!mDefaultBaseUrl.endsWith("/") && modifyBaseUrl.endsWith("/")) {
                                modifyBaseUrl = modifyBaseUrl.substring(0, modifyBaseUrl.length - 1)
                            }
                            if (!url.startsWith(modifyBaseUrl)) {
                                // 替换 url 并创建新的 call
                                val newRequest: Request =
                                    request.newBuilder()
                                        .url(
                                            url.replace(
                                                mDefaultBaseUrl,
                                                modifyBaseUrl
                                            )
                                                .toHttpUrl()
                                        )
                                        .build()
                                LogUtils.i("HttpRequest getService: old ${request.url} new ${newRequest.url}")
                                client.newCall(newRequest)
                            } else client.newCall(request)
                        } else {
                            client.newCall(request)
                        }
                    }

                })
            }
            // Kotlin 使用协程，Java 使用 rx
            val adapter = getRxJavaAdapter()
            adapter?.let {
                builder.addCallAdapterFactory(it as CallAdapter.Factory) // 回调处理，可以设置Rx作为回调的处理
            }
            obj = builder.build().create(cls)
            mServiceMap[name] = obj
        }
        @Suppress("UNCHECKED_CAST")
        return obj as T
    }

    /**
     * 设置了 [mDefaultBaseUrl] 后，可通过此方法获取 Service
     */
    @JvmStatic
    fun <T> getService(cls: Class<T>): T {
        if (!this::mDefaultBaseUrl.isInitialized) {
            throw RuntimeException("必须初始化 mBaseUrl")
        }
        if (this::mDefaultHeader.isInitialized) {
            val headers = HeaderInterceptor(mDefaultHeader)
            return getService(cls, mDefaultBaseUrl, headers)
        }
        return getService(cls, mDefaultBaseUrl, null)
    }


    @JvmStatic
    fun <T> getService(cls: Class<T>, vararg interceptors: Interceptor?): T {
        if (!this::mDefaultBaseUrl.isInitialized) {
            throw RuntimeException("必须初始化 mBaseUrl")
        }
        if (this::mDefaultHeader.isInitialized) {
            val headers = HeaderInterceptor(mDefaultHeader)
            return getService(cls, mDefaultBaseUrl, headers, *interceptors)
        }
        return getService(cls, mDefaultBaseUrl, *interceptors)
    }

    /**
     * 同步的请求，当一个界面需要调用多个接口才能呈现出来时，可以在子线程中或者Observable.zip操作多个接口
     */
    @JvmStatic
    fun <T> execute(call: Call<T>): T? {
        try {
            return call.execute().body()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Retrofit 的原生异步请求，如果你不想使用 Rx，那么可以使用这个
     */
    @JvmStatic
    fun <T, R> request(
        call: Call<T>,
        callback: CommonObserver<R>
    ): Call<T> {
        callback.onStart()

        call.enqueue(object : Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                val baseResponse = response.body()

                @Suppress("UNCHECKED_CAST")
                val resp = baseResponse as? IBaseResponse<R>
                if (resp == null) {
                    callback.onFailed(entityNullable, msgEntityNullable)
                } else {
                    callback.onNext(resp)
                }
                callback.onComplete()
            }

            override fun onFailure(
                call: Call<T>,
                t: Throwable
            ) {
                callback.onError(t)
                callback.onComplete()
            }
        })
        return call
    }

    /**
     * 在合适的位置调用此方法，多次连击后将弹出修改 baseUrl 的对话框。
     * 前提是必须开启了 [GlobalConfig.gIsNeedChangeBaseUrl] 属性，同时获取了 service 实例
     */
    fun multiClickToChangeBaseUrl(view: View, frequency: Int) {
        if (!GlobalConfig.gIsNeedChangeBaseUrl) {
            return
        }
        Utils.multiClickListener(view, frequency) {
            showChangeBaseUrlDialog()
        }
    }

    fun showChangeBaseUrlDialog() {
        AppActivityManager.currentAppCompatActivity()?.let { activity ->

            val layout = LinearLayout(activity)
            layout.orientation = LinearLayout.VERTICAL
            val textView = TextView(activity).apply {
                text = mDefaultBaseUrl
            }
            val editView = EditText(activity).apply {
                setText(if (modifyBaseUrl.isBlank()) mDefaultBaseUrl else modifyBaseUrl)
            }

            layout.addView(textView)
            layout.addView(editView)

            val btn = Button(activity)
            btn.text = activity.getString(R.string.restore)
            btn.setOnClickListener {
                editView.setText(mDefaultBaseUrl)
            }
            layout.addView(btn)

            val checkBox = CheckBox(activity)
            checkBox.text = activity.getString(R.string.effective_next_time)
            checkBox.isChecked = !spModifyBaseUrl.isNullOrBlank()
            layout.addView(checkBox)

            val editDialog = AlertDialog.Builder(activity)
            editDialog.setView(layout)

            editDialog.setPositiveButton(R.string.confirm) { dialog, _ ->
                modifyBaseUrl = editView.text.toString()
                checkBox.isChecked.apply {
                    spModifyBaseUrl = if (this) {
                        modifyBaseUrl
                    } else {
                        null
                    }
                }
                dialog.dismiss()
            }

            editDialog.create().show()
        }
    }
}
