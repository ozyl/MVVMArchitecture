package com.imyyq.mvvm.base

import android.app.Dialog
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.apkfuns.logutils.utils.Utils
import com.fenxiangbuy.dialog.BaseDialog
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
    var autoCancel:Boolean=true,
    var confirmText:String?=null,
    var cancelText:String?=null,
    var time:Int?=null,
    var extModel: Any?=null,
    val isCancelable:Boolean=true
)


enum class UIEventType {
    DIALOG_WAIT, DIALOG_DISMISS, DL_TIP_SUCCESS, DL_TIP_FAIL,DL_TIP_WARNING,DIALOG_MSG
}