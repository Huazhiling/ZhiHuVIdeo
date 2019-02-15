package com.example.mvc.intercept_video_link.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.content.FileProvider
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mvc.intercept_video_link.MyApplication
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.listener.ApiStore
import com.example.mvc.intercept_video_link.utils.RetrofitUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.http.Url
import java.io.*

class DownloadService : IntentService("download") {
    private lateinit var nm: NotificationManager
    private lateinit var videoFile: File

    @SuppressLint("CheckResult")
    override fun onHandleIntent(intent: Intent?) {
        var title = intent?.getStringExtra("title")
        var url = intent?.getStringExtra("url")?.replace(MyApplication.getBaseUrl(), "")
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        videoFile = File("${Environment.getExternalStorageDirectory().absolutePath}/ZhiHuVideo/$title.mp4")
        ToastUtils.showShort("正在下载")
        RetrofitUtils.client(ApiStore::class.java).downloadVideo(url!!)
                .subscribeOn(Schedulers.io())
                .flatMap { t: ResponseBody ->
                    Observable.just(saveVideo(t, videoFile))
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isSave ->
                    if (isSave) {
                        ToastUtils.showShort("下载完成，视频已放在：${videoFile.path}")
                        createNotification("下载成功", videoFile.path)
                        val contentUri = Uri.fromFile(videoFile)
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri)
                        sendBroadcast(mediaScanIntent)
                    }
                }, {
                    LogUtils.e(it.message)
                    ToastUtils.showShort("下载失败")
                })

    }

    private fun saveVideo(strem: ResponseBody, videoFile: File): Boolean {
        var ips = strem.byteStream()
        var ops: OutputStream? = null
        try {
            ops = FileOutputStream(videoFile)
            var byte = ByteArray(1024)
            while (true) {
                var read = ips.read(byte)
                if (read === -1) {
                    break
                }
                ops.write(byte, 0, read)
            }
            return true
        } catch (e: FileNotFoundException) {
            LogUtils.e(e.message)
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            LogUtils.e(e.message)
            return false
        } finally {
            if (ops !== null) {
                ops.close()
            }
            if (ips !== null) {
                ips.close()
            }
        }
    }

    @Throws(JSONException::class)
    private fun createNotification(message: String, path: String) {
        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = NotificationCompat.Builder(baseContext, "price_update")
            nm.createNotificationChannel(NotificationChannel("price_update", "新消息通知", NotificationManager.IMPORTANCE_HIGH))
        } else {
            builder = NotificationCompat.Builder(baseContext)
        }
        val msgIntent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(baseContext, MyApplication.getAppContext()?.packageName + ".fileprovider", videoFile)
        msgIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        msgIntent.setDataAndType(uri, "video/*")
        val mPendingIntent = PendingIntent.getActivity(baseContext, 1, msgIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        //设置通知栏标题
        builder.setContentTitle(message)
                //设置通知栏显示内容
                .setContentText("点击播放")
                ////设置通知栏点击意图
                .setContentIntent(mPendingIntent)
                //通知首次出现在通知栏，带上升动画效果的
                .setTicker("您有新的消息")
                //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setWhen(System.currentTimeMillis())
                //设置该通知优先级
                .setPriority(Notification.PRIORITY_DEFAULT)
                //设置这个标志当用户单击面板就可以让通知将自动取消
                .setAutoCancel(true)
                //使用当前的用户默认设置
                .setDefaults(Notification.DEFAULT_VIBRATE)
                //设置通知小ICON(应用默认图标)
                .setSmallIcon(R.mipmap.ic_launcher)
        nm.notify(0, builder.build())
    }

}