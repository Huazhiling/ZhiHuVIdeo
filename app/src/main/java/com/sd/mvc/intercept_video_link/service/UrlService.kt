package com.sd.mvc.intercept_video_link.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.app.ActivityManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.listener.ParsingCallback
import com.sd.mvc.intercept_video_link.utils.ConvertUtils
import com.sd.mvc.intercept_video_link.utils.LogUtils
import com.sd.mvc.intercept_video_link.utils.PatternHelper
import com.sd.mvc.intercept_video_link.utils.SQLiteHelper
import x.y.h.am


class UrlService : Service() {
    private lateinit var clipManager: ClipboardManager
    private lateinit var parCallback: ParsingCallback
    private lateinit var sqlite: SQLiteHelper
    private var isRemove = false
    private var urlBind = UrlBind()

    override fun onCreate() {
        super.onCreate()
        createClipCallback()
    }

    fun createLoadView() {
        if (isRemove) return
        //只要有解析，都判定为true，下次就忽略解析，为false的时候才进行解析
        isRemove = true
    }

    private fun isAndroidVersionAddAFloatingWindow(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Settings.canDrawOverlays(this))
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
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
        // 开启监听的时候顺便做数据库操作
        initSQLite()
        clipManager.addPrimaryClipChangedListener {
            if (isRemove) return@addPrimaryClipChangedListener
            var utlSb = StringBuffer()
            var primary = clipManager.primaryClip.getItemAt(0).text.toString()
            if (primary != "" && PatternHelper.isHttpUrl(primary)) {
                if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                    utlSb.append("http://")
                }
                utlSb.append(primary)
                parCallback.analysisSourceCode(utlSb.toString())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return urlBind
    }

    fun closeSQLite() {
        sqlite.close()
    }


    internal inner class UrlBind : Binder() {
        fun getService(): UrlService {
            return this@UrlService
        }
    }

    /**
     * =============================================================================================
     * SQLite 数据库
     * =============================================================================================
     */
    //初始化数据库
    private fun initSQLite() {
        sqlite = SQLiteHelper(baseContext, "fox", null, 1)
    }

    fun insertData(primary: String, title: String, time: Long): Boolean {
        return sqlite.insertNewData(primary, title, time)
    }

    fun insertFoxNewData(primary: String, url: String, title: String, imageUrl: String, downloadUrl: String) {
        sqlite.insertFoxNewData(primary, url, title, imageUrl, downloadUrl)
    }

    fun deleteAllData() {
        sqlite.deleteAllData()
    }
}