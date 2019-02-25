package com.example.mvc.intercept_video_link.service

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
import android.view.*
import com.blankj.utilcode.util.ConvertUtils
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.utils.PatternHelper
import kotlinx.android.synthetic.main.layout_window_hint.view.*


class UrlService : Service() {
    companion object {
        private val LOAD_VIEW = "load_hint"
        private val DOWNLOAD_VIEW = "download_hint"
    }

    private var urlBind = UrlBind()
    private lateinit var am: ActivityManager
    private lateinit var clipManager: ClipboardManager
    private lateinit var windowManager: WindowManager
    private lateinit var loadLayoutParams: WindowManager.LayoutParams
    private lateinit var downloadLayoutParams: WindowManager.LayoutParams
    private lateinit var loadView: View
    private lateinit var downloadView: View
    private lateinit var parCallback: ParsingCallback
    private var windowMap = HashMap<String, View>()
    private var handler = Handler()
    private var run = Runnable {
        handler.post { removeDownloadView() }
    }

    override fun onCreate() {
        super.onCreate()
        createClipCallback()
        initZhihuVideoDetect()
    }

    private fun initZhihuVideoDetect() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun createLoadView() {
        if (windowMap[LOAD_VIEW] === null) {
            loadView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_load, null)
            loadLayoutParams = WindowManager.LayoutParams()
            loadLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                loadLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            loadLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            loadLayoutParams.format = PixelFormat.RGBA_8888
            loadLayoutParams.y = ConvertUtils.dp2px(150f)
            loadLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            loadLayoutParams.height = ConvertUtils.dp2px(50f)
            loadLayoutParams.windowAnimations = android.R.style.Animation_Translucent
            windowMap[LOAD_VIEW] = loadView
        }
        windowManager.addView(windowMap[LOAD_VIEW], loadLayoutParams)
//        handler.postDelayed(run, 3000)
    }

    fun updateView(msg: String, isClick: Boolean) {
        if (windowMap[DOWNLOAD_VIEW] === null) {
            downloadView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_hint, null)
            downloadLayoutParams = WindowManager.LayoutParams()
            downloadLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                downloadLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                downloadLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            downloadLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            downloadLayoutParams.format = PixelFormat.RGBA_8888
            downloadLayoutParams.y = ConvertUtils.dp2px(150f)
            downloadLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            downloadLayoutParams.height = ConvertUtils.dp2px(50f)
            downloadLayoutParams.windowAnimations = android.R.style.Animation_Translucent
            windowMap[DOWNLOAD_VIEW] = downloadView
        }
        downloadView.dialog_content.text = msg
        downloadView.dialog_content.setOnClickListener {
            if (isClick) {
                parCallback.startActivity(baseContext)
                removeDownloadView()
                handler.removeCallbacks(run)
            }
        }
        updateViewLayout(downloadView, downloadLayoutParams)
        handler.postDelayed(run, 3000)
    }

    private fun updateViewLayout(downloadView: View, downloadLayoutParams: WindowManager.LayoutParams) {
        removeLoadView()
        windowManager.addView(downloadView, downloadLayoutParams)
    }

    private fun removeDownloadView() {
        if (windowMap[DOWNLOAD_VIEW] !== null) {
            windowManager.removeView(windowMap[DOWNLOAD_VIEW])
        }
    }

    private fun removeLoadView() {
        if (windowMap[LOAD_VIEW] !== null) {
            windowManager.removeView(windowMap[LOAD_VIEW])
        }
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
            if (primary != "" && PatternHelper.isHttpUrl(primary)) {
                if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                    utlSb.append("http://")
                }
                utlSb.append(primary)
                parCallback.analysisSourceCode(utlSb.toString())
            } else {
//                复制的空字符串  不给解析
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