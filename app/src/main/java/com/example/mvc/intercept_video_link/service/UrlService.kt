package com.example.mvc.intercept_video_link.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.app.ActivityManager
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.utils.PatternHelper


class UrlService : Service() {
    private var urlBind = UrlBind()
    private lateinit var clipManager: ClipboardManager
    private lateinit var am: ActivityManager
    private lateinit var parCallback: ParsingCallback

    override fun onCreate() {
        super.onCreate()
        createClipCallback()
    }

    fun setParsingCallback(parCallback: ParsingCallback) {
        this.parCallback = parCallback
    }

    /**
     * 监听剪贴板记录  如果是url链接  直接搜索
     */
    @SuppressLint("NewApi")
    private fun createClipCallback() {
        clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        clipManager.addPrimaryClipChangedListener {
            var utlSb = StringBuffer()
            var primary = clipManager.primaryClip.getItemAt(0).text.toString()
            if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                utlSb.append("http://")
            }
            utlSb.append(primary)
            if (PatternHelper.isHttpUrl(utlSb.toString())) {
                parCallback.AnalysisSourceCode(utlSb.toString())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return urlBind
    }


    internal inner class UrlBind : Binder() {
        fun getService(): UrlService {
            return this@UrlService
        }
    }
}