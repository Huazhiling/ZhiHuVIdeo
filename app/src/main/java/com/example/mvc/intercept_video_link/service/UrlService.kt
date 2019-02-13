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
import com.blankj.utilcode.util.LogUtils
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.activity.MainActivity
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.utils.PatternHelper


class UrlService : Service() {
    companion object {
        private val ZHIHU_VIEW = "zhihu_hint"
    }

    private var urlBind = UrlBind()
    private lateinit var am: ActivityManager
    private lateinit var clipManager: ClipboardManager
    private lateinit var windowManager: WindowManager
    private lateinit var toastLayoutParams: WindowManager.LayoutParams
    private lateinit var zhihuView: View
    private lateinit var parCallback: ParsingCallback
    private var windowMap = HashMap<String, View>()
    private var handler = Handler()
    private var run = Runnable {
        handler.post { removeZhihuView() }
    }

    override fun onCreate() {
        super.onCreate()
        createClipCallback()
        initZhihuVideoDetect()
    }

    private fun initZhihuVideoDetect() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun createZhihuVideoHint() {
        if (windowMap[ZHIHU_VIEW] === null) {
            zhihuView = LayoutInflater.from(applicationContext).inflate(R.layout.layout_window_hint, null)
            toastLayoutParams = WindowManager.LayoutParams()
            toastLayoutParams.gravity = Gravity.RIGHT or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                toastLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                toastLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
            }
            toastLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
            toastLayoutParams.format = PixelFormat.RGBA_8888
            toastLayoutParams.y = ConvertUtils.dp2px(150f)
            toastLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            toastLayoutParams.height = ConvertUtils.dp2px(50f)
            toastLayoutParams.windowAnimations = android.R.style.Animation_Toast
            windowMap[ZHIHU_VIEW] = zhihuView
            zhihuView.setOnClickListener {
                startActivity(Intent(baseContext, MainActivity::class.java))
                removeZhihuView()
                handler.removeCallbacks(run)
            }
        }
        LogUtils.e(windowMap[ZHIHU_VIEW])
        windowManager.addView(windowMap[ZHIHU_VIEW], toastLayoutParams)
        handler.postDelayed(run, 3000)
    }

    private fun removeZhihuView() {
        if (windowMap[ZHIHU_VIEW] !== null) {
            windowManager.removeView(windowMap[ZHIHU_VIEW])
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
            if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                utlSb.append("http://")
            }
            utlSb.append(primary)
            if (PatternHelper.isHttpUrl(utlSb.toString())) {
                parCallback.AnalysisSourceCode(utlSb.toString())
                createZhihuVideoHint()
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