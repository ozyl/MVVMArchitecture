package com.imyyq.mvvm.http

import com.imyyq.mvvm.base.IBaseResponse
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLHandshakeException


object HttpHandler {


    interface HttpFailedCall{
        fun <T> handle(entity:IBaseResponse<T>):Boolean
    }

    var failedCall:HttpFailedCall?=null

    /**
     * 处理请求结果
     *
     * [entity] 实体
     * [onSuccess] 状态码对了就回调
     * [onResult] 状态码对了，且实体不是 null 才回调
     * [onFailed] 有错误发生，可能是服务端错误，可能是数据错误，详见 code 错误码和 msg 错误信息
     * [onGlobalFailed] 有全局错误发生，可能是服务端错误，详见 code 错误码和 msg 错误信息
     * [onFailedPreHandle] 失败预处理，可拦截不经过onFailed,默认值为全局失败处理
     */
    fun <T> handleResult(
        entity: IBaseResponse<out T?>?,
        onSuccess: (() -> Unit)? = null,
        onResult: ((t: T) -> Unit)?,
        onFailed: ((code: Int, msg: String?, data: T?) -> Unit)? = null,
        onResultOrNull: ((t: T?) -> Unit)?=null,
        onGlobalFailed: ((code: Int, msg: String?, data: T?) -> Unit)? = null,
        onFailedPreHandle: HttpFailedCall?=null,
        ) {
        // 防止实体为 null
        if (entity == null) {
            onFailed?.invoke(entityNullable, msgEntityNullable, null)
            return
        }
        val code = entity.code()
        val msg = entity.msg()
        // 防止状态码为 null
        if (code == null) {
            onFailed?.invoke(entityCodeNullable, msgEntityCodeNullable, null)
            return
        }
        // 请求成功
        when {
            entity.isSuccess() -> {
                // 回调成功
                onSuccess?.invoke()
                onResultOrNull?.invoke(entity.data())
                // 实体不为 null 才有价值
                entity.data()?.let { onResult?.invoke(it) }
            }
            else -> {
                if ((onFailedPreHandle?:failedCall)?.handle(entity) != true){
                    onFailed?.invoke(code, msg, entity.data())
                }else{
                    onGlobalFailed?.invoke(code,msg,entity.data())
                }
            }
        }
    }

    /**
     * 处理异常
     */
    fun handleException(
        e: Exception,
        onFailed: ((code: Int, msg: String?, data: Nothing?) -> Unit)?,
        onCancel: (() -> Unit)?
    ) {
        when (e) {
            is HttpException -> {
                onFailed?.invoke(netException, msgNotHttpException, null)
            }
            is UnknownHostException, is CertPathValidatorException, is SSLHandshakeException, is SocketTimeoutException -> {
                onFailed?.invoke(netException, msgNotHttpException, null)
            }
            is CancellationException ->{
                onCancel?.invoke()
            }
            else -> {
                onFailed?.invoke(
                    netException,
                    msgNotHttpException,
                    null
                )
            }
        }
    }
}