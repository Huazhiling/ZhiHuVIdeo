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
    private lateinit var windowManager: WindowManager
    private lateinit var loadLayoutParams: WindowManager.LayoutParams
    private lateinit var loadView: View
    private var isRemove = false
    private var urlBind = UrlBind()
    override fun onCreate() {
        super.onCreate()
        createClipCallback()
    }

    //创建一个1像素的window放置前台当悬浮窗
    fun createView() {
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        loadView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_load, null)
//        loadLayoutParams = WindowManager.LayoutParams()
//        loadLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            loadLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//        } else {
//            loadLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
//        }
//        loadLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//        loadLayoutParams.format = PixelFormat.RGBA_8888
//        loadLayoutParams.y = ConvertUtils.dp2px(150)
//        loadLayoutParams.y = ConvertUtils.dp2px(150)
//        loadLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
//        loadLayoutParams.height = ConvertUtils.dp2px(50)
//        loadLayoutParams.height = ConvertUtils.dp2px(50)
//        loadLayoutParams.windowAnimations = android.R.style.Animation_Translucent
//        windowManager.addView(loadView, loadLayoutParams)
//        loadView.let {
        clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 开启监听的时候顺便做数据库操作
        clipManager.addPrimaryClipChangedListener {
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
//        }
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
        initSQLite()
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