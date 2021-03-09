package com.imyyq.mvvm.base

import android.view.Gravity
import com.imyyq.mvvm.app.GlobalConfig

/**
 * 加载中对话框接口
 */
interface ILoadingDialog {
    /**
     * 详见 [com.imyyq.mvvm.app.GlobalConfig.LoadingDialog.gIsNeedLoadingDialog]
     */
    fun isNeedLoadingDialog() = GlobalConfig.LoadingDialog.gIsNeedLoadingDialog
}

data class UIEvent(
    val type: UIEventType,
    var msg: CharSequence?=null,
    var title: CharSequence?=null,
    var isExtendsMsgDialog:Boolean = false,
    var cancelVoidCallback:(()->Unit)?=null,
    var confirmVoidCallback:(()->Unit)?=null,
    var autoConfirm:Boolean=true,
    var nullOrEmptyGone:Boolean=true,
    var autoCancel:Boolean=true,
    var confirmText:String?=null,
    var cancelText:String?=null,
    var time:Int?=null,
    var extModel: Any?=null,
    val isCancelable:Boolean=true,
    val tag:String?=null,
    val gravity:Int=Gravity.CENTER
)


enum class UIEventType {
    DIALOG_WAIT, DIALOG_DISMISS,DIALOG_MSG
}