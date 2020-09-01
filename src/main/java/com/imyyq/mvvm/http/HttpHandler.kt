package com.imyyq.mvvm.http

import com.imyyq.mvvm.app.GlobalConfig
import com.imyyq.mvvm.base.IBaseResponse
import retrofit2.HttpException
import java.net.UnknownHostException


object HttpHandler {


    /**
     * 处理请求结果
     *
     * [entity] 实体
     * [onSuccess] 状态码对了就回调
     * [onResult] 状态码对了，且实体不是 null 才回调
     * [onFailed] 有错误发生，可能是服务端错误，可能是数据错误，详见 code 错误码和 msg 错误信息
     */
    fun <T> handleResult(
        entity: IBaseResponse<T?>?,
        onSuccess: (() -> Unit)? = null,
        onResult: ((t: T) -> Unit)?,
        onFailed: ((code: Int, msg: String?,data:T?)->Unit)? = null
    ) {
        // 防止实体为 null
        if (entity == null) {
            onFailed?.invoke(entityNullable, msgEntityNullable,null)
            return
        }
        val code = entity.code()
        val msg = entity.msg()
        // 防止状态码为 null
        if (code == null) {
            onFailed?.invoke(entityCodeNullable, msgEntityCodeNullable,null)
            return
        }
        // 请求成功
        if (entity.isSuccess()) {
            // 回调成功
            onSuccess?.invoke()
            // 实体不为 null 才有价值
            entity.data()?.let { onResult?.invoke(it) }
        } else {
            // 失败了
            onFailed?.invoke(code, msg,entity.data())
        }
    }

    /**
     * 处理异常
     */
    fun handleException(
        e: Exception,
        onFailed: (code: Int, msg: String?,data:Nothing?) -> Unit
    ) {
        if (GlobalConfig.gIsDebug) {
            e.printStackTrace()
        }
        return when (e) {
            is HttpException -> {
                onFailed(e.code(), e.message(),null)
            }
            is UnknownHostException -> {
                onFailed(netException, msgNotHttpException,null)
            }
            else -> {
                onFailed(
                    notHttpException,
                    msgException,
                    null
                )
            }
        }
    }
}