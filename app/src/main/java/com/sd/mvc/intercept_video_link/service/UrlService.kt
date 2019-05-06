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
import com.sd.mvc.intercept_video_link.utils.PatternHelper
import com.sd.mvc.intercept_video_link.utils.SQLiteHelper


class UrlService : Service() {
    companion object {
        private val LOAD_VIEW = "load_hint"
        private val DOWNLOAD_VIEW = "download_hint"
    }

    private lateinit var am: ActivityManager
    private lateinit var clipManager: ClipboardManager
    private lateinit var windowManager: WindowManager
    private lateinit var wmLayoutParams: WindowManager.LayoutParams
    private lateinit var loadView: View
    private lateinit var downloadView: View
    private lateinit var parCallback: ParsingCallback
    private lateinit var sqlite: SQLiteHelper
    private lateinit var frameLayout: FrameLayout
    private var isRemove = false
    private var urlBind = UrlBind()
    private var windowMap = HashMap<String, View>()
    private var handler = Handler()
    private var run = Runnable {
        handler.post {
            removeDownloadView()
        }
    }

    override fun onCreate() {
        super.onCreate()
        createClipCallback()
        initWMLayoutParams()
        initZhihuVideoDetect()
    }

    private fun initWMLayoutParams() {
        frameLayout = FrameLayout(baseContext)
        wmLayoutParams = WindowManager.LayoutParams()
        wmLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
        }
        wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        wmLayoutParams.format = PixelFormat.RGBA_8888
        wmLayoutParams.y = ConvertUtils.dp2px(150)
        wmLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        wmLayoutParams.height = ConvertUtils.dp2px(50)
        wmLayoutParams.windowAnimations = android.R.style.Animation_Translucent
    }

    private fun initZhihuVideoDetect() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun createLoadView() {
        if (isRemove) return
        //只要有解析，都判定为true，下次就忽略解析，为false的时候才进行解析
        isRemove = true
        if (windowMap[LOAD_VIEW] == null) {
            loadView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_load, null)
            windowMap[LOAD_VIEW] = loadView
        }
        removeAllViews(windowMap[LOAD_VIEW]!!)
        frameLayout.addView(windowMap[LOAD_VIEW]!!)
        if (isAndroidVersionAddAFloatingWindow()) {
            windowManager.addView(frameLayout, wmLayoutParams)
        }
    }

    //现将view从原父容器中移除
    private fun removeAllViews(view: View) {
        (view.parent as ViewGroup?)?.removeView(view)
        frameLayout.removeAllViews()
    }

    private fun isAndroidVersionAddAFloatingWindow(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Settings.canDrawOverlays(this))
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    }

    fun updateView(msg: String, isClick: Boolean) {
        if (windowMap[DOWNLOAD_VIEW] == null) {
            downloadView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_hint, null)
            windowMap[DOWNLOAD_VIEW] = downloadView
        }
        downloadView.findViewById<TextView>(R.id.dialog_content).text = msg
        downloadView.findViewById<TextView>(R.id.dialog_content).setOnClickListener {
            if (isClick) {
                parCallback.startActivity(baseContext)
                removeDownloadView()
                handler.removeCallbacks(run)
            }
        }
        removeAllViews(windowMap[DOWNLOAD_VIEW]!!)
        frameLayout.addView(windowMap[DOWNLOAD_VIEW]!!)
        if (isAndroidVersionAddAFloatingWindow()) {
            updateViewLayout(frameLayout, wmLayoutParams)
            handler.postDelayed(run, 2500)
        }
    }

    private fun updateViewLayout(downloadView: View, downloadLayoutParams: WindowManager.LayoutParams) {
        windowManager.updateViewLayout(downloadView, downloadLayoutParams)
    }

    private fun removeDownloadView() {
        windowManager.removeView(frameLayout)
        isRemove = false
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
//        开启监听的时候顺便做数据库操作
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
}