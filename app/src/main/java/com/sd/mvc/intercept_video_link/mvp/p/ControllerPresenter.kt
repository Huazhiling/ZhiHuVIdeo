package com.sd.mvc.intercept_video_link.mvp.p

import android.content.ClipboardManager
import android.content.Context
import com.sd.mvc.intercept_video_link.MyApplication
import com.sd.mvc.intercept_video_link.base.BasePresenter
import com.sd.mvc.intercept_video_link.base.IBaseModel
import com.sd.mvc.intercept_video_link.contract.ControllerContract
import com.sd.mvc.intercept_video_link.mvp.m.ControllerModel
import com.sd.mvc.intercept_video_link.utils.PatternHelper

class ControllerPresenter : ControllerContract.ControllerPresenter() {
    private lateinit var clipManager: ClipboardManager

    companion object {
        fun newInstance(): BasePresenter<*, *> {
            return ControllerPresenter()
        }
    }

    override fun initAppInfo() {

    }

    override fun resolveClipData() {
        var utlSb = StringBuffer()
        var primary = clipManager.primaryClip.getItemAt(0).text.toString()
        if (primary != "" && PatternHelper.isHttpUrl(primary)) {
            if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                utlSb.append("http://")
            }
            utlSb.append(primary)
            mModel?.resolveVideo(utlSb.toString())
        }
    }

    override fun onStar() {
        clipManager = MyApplication.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun getModel(): ControllerModel {
        return ControllerModel.instant
    }
}