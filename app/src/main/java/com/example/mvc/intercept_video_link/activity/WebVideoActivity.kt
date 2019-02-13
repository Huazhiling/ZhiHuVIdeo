package com.example.mvc.intercept_video_link.activity

import android.Manifest
import android.app.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import com.blankj.utilcode.util.ToastUtils
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import kotlinx.android.synthetic.main.activity_web_video.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.service.DownloadService
import org.json.JSONException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


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
                    var videoFile = File("${Environment.getExternalStorageDirectory().path}/ZhiHuVideo/")
                    if (!videoFile.exists()) {
                        videoFile.mkdirs()
                    }
                    downloadVideo(url, userAgent, contentDisposition, mimetype, contentLength)
                }
            }).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        video_web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
    }

    private fun downloadVideo(url: String?, userAgent: String?, contentDisposition: String?, mimetype: String?, contentLength: Long) {
        var sb = StringBuffer()
        var now = Date()
        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        if (video_title == "") {
            sb.append("知乎${dateFormat.format(now)}")
        } else {
            sb.append("${"知乎"} $video_title ${dateFormat.format(now)}")
        }
        var download = Intent(baseContext, DownloadService::class.java)
        download.putExtra("url", url)
        download.putExtra("title", sb.toString())
        startService(download)

//        // 指定下载地址
//        val request = DownloadManager.Request(Uri.parse(url))
//        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
//        request.allowScanningByMediaScanner()
//        // 设置通知的显示类型，下载进行时和完成后显示通知
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        // 设置通知栏的标题，如果不设置，默认使用文件名
////        request.setTitle("This is title");
//        // 设置通知栏的描述
////        request.setDescription("This is description");
//        // 允许在计费流量下下载
//        request.setAllowedOverMetered(false)
//        // 允许该记录在下载管理界面可见
//        request.setVisibleInDownloadsUi(false)
//        // 允许漫游时下载
//        request.setAllowedOverRoaming(true)
//        // 允许下载的网路类型
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
//        // 设置下载文件保存的路径和文件名
//        var sb = StringBuffer()
//        var now = Date()
//        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        if (video_title == "") {
//            sb.append("知乎${dateFormat.format(now)}")
//        } else {
//            sb.append("$video_title ${dateFormat.format(now)}")
//        }
//        val fileName = URLUtil.guessFileName(url, sb.toString(), mimetype)
//        LogUtils.e("fileName:{}", fileName)
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
////        另外可选一下方法，自定义下载路径
////        request.setDestinationUri()
////        request.setDestinationInExternalFilesDir()
//        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        // 添加一个下载任务
//        val downloadId = downloadManager.enqueue(request)
//        LogUtils.e("downloadId:{}", downloadId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        RsPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        if (video_web !== null) {
            var webParent = video_web.parent as ViewGroup
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
}
