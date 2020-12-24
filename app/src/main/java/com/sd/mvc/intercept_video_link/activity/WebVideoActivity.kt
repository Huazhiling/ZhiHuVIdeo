package com.sd.mvc.intercept_video_link.activity

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import com.blankj.utilcode.util.ToastUtils
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import kotlinx.android.synthetic.main.activity_web_video.*
import com.blankj.utilcode.util.LogUtils
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.utils.DownloadUtils


class WebVideoActivity : AppCompatActivity() {
    private lateinit var url: String
    private lateinit var video_title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_video)
        var webSetting = video_web.settings
        webSetting.javaScriptEnabled = true
        url = intent.getStringExtra("video_url")
        video_title = intent.getStringExtra("video_title")
        video_web.loadUrl(url)
        video_web.addJavascriptInterface(JavaScriptInterface(), "html_source")
        video_web.webChromeClient = object : WebChromeClient() {
        }
        video_web.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            RsPermission.getInstance().setiPermissionRequest(object : IPermissionRequest {
                override fun toSetting() {
                    RsPermission.getInstance().toSettingPer()
                }

                override fun cancle(i: Int) {
                    ToastUtils.showShort("未获得存储权限，无法下载")
                }

                override fun success(i: Int) {
                    DownloadUtils.downloadVideo(baseContext, url)
                }
            }).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        video_web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                LogUtils.e("onLoadResource  $url")
                super.onLoadResource(view, url)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        RsPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        if (video_web !== null) {
            var webParent = video_web.parent as ViewGroup?
            if (webParent !== null) {
                webParent.removeView(video_web)
            }
            video_web.stopLoading()
            video_web.settings.javaScriptEnabled = false
            video_web.clearHistory()
            video_web.removeAllViews()
            video_web.destroy()
        }
        super.onDestroy()
    }

    class JavaScriptInterface {
        @JavascriptInterface
        fun getHtml(source: String) {
            LogUtils.e(source)
        }
    }
}
