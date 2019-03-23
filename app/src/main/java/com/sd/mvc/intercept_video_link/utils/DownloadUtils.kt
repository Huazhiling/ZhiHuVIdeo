package com.sd.mvc.intercept_video_link.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.sd.mvc.intercept_video_link.service.DownloadService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DownloadUtils {
    fun downloadVideo(baseContext: Context, url: String) {
        var videoFile = File("${Environment.getExternalStorageDirectory().absolutePath}/ZhiHuVideo/")
        if (!videoFile.exists()) {
            videoFile.mkdirs()
        }
        var sb = StringBuffer()
        var now = Date()
        var dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        sb.append("Zhihu_${dateFormat.format(now)}")
        var download = Intent(baseContext, DownloadService::class.java)
        download.putExtra("url", url)
        download.putExtra("title", sb.toString())
        baseContext.startService(download)
    }
}