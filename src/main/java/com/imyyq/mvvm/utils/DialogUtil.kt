package com.imyyq.mvvm.utils

import com.fenxiangbuy.dialog.MsgDialog
import com.fenxiangbuy.dialog.WaitDialog
import com.imyyq.mvvm.base.UIEvent

object DialogUtil {

    fun initMsgDialog(msgDialog: MsgDialog, it: UIEvent) {
        msgDialog.content = it.msg ?: ""
        msgDialog.title = it.title ?: "温馨提示"
        msgDialog.msgGravity = it.gravity
        msgDialog.isCancelable = it.isCancelable

        msgDialog.confirm.apply {
            isAutoClose = it.autoConfirm
            click = it.confirmVoidCallback
            text = it.confirmText
        }

        msgDialog.cancel.apply {
            isAutoClose = it.autoCancel
            click = it.cancelVoidCallback
            text = it.cancelText
        }
    }

    fun initWaitDialog(waitDialog: WaitDialog,it: UIEvent){
        waitDialog.hintMsg = it.msg
        waitDialog.nullOrEmptyGone = it.nullOrEmptyGone
        waitDialog.isCancelable = it.isCancelable
    }
}