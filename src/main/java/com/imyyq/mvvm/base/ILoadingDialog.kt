package com.imyyq.mvvm.base

import android.app.Dialog
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.apkfuns.logutils.utils.Utils
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
    var msg: String?=null,
    var voidCallback:(()->Unit)?=null,
    var time:Int?=null
)

enum class UIEventType {
    DIALOG_WAIT, DIALOG_DISMISS, DL_TIP_SUCCESS, DL_TIP_FAIL,DL_TIP_WARNING
}