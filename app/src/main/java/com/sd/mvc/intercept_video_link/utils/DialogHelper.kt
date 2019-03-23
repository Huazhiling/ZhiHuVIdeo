package com.sd.mvc.intercept_video_link.utils

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.listener.IDialogInterface
import kotlinx.android.synthetic.main.layout_dialog.view.*

class DialogHelper {
    private var dismissCount = 3
    private var DISMISS_CODE = 0
    private var mHandler = Handler(Handler.Callback { msg ->
        if (msg?.what == DISMISS_CODE) {
            dismiss()
            true
        } else {
            dismissCount--
            false
        }
    })
    private var runnable = Runnable {
        message.what = dismissCount
        mHandler.sendMessage(message)
    }
    companion object {
        private lateinit var mDialog: Dialog
        private lateinit var message: Message
        fun getInstance(): DialogHelper {
            message = Message.obtain()
            return DialogHelper()
        }
    }

    fun createHintDialog(context: Context, msg: String, title: String,dialogDialog: IDialogInterface?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null)
        mDialog = Dialog(context, R.style.hint_dialog)
        dialogView.dialog_title.text = title
        dialogView.dialog_content.text = msg
        dialogView.dialog_dismiss.setOnClickListener { dismiss() }
        mDialog.setContentView(dialogView)
        //一定要在setContentView之后调用，否则无效
        mDialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        message.what = dismissCount
        mHandler.sendMessage(message)
        mDialog.setOnDismissListener {
            if (dialogDialog != null) {
                dialogDialog!!.dismissCallback()
            } }
        return mDialog
    }

    fun dismissDelayed(dialogDialog: IDialogInterface?) {
        Handler().postDelayed({
            dismiss()
            if (dialogDialog != null) {
                dialogDialog!!.dismissCallback()
            }
        }, 2000)
    }

    fun dismissDelayed(dialogDialog: IDialogInterface?, delayMillis: Long) {
        Handler().postDelayed({
            dismiss()
            if (dialogDialog != null) {
                dialogDialog!!.dismissCallback()
            }
        }, delayMillis)
    }

    fun dismiss() {
        if (mDialog != null) {
            mDialog.dismiss()
        }
    }
}