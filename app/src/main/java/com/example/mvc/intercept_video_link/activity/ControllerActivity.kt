package com.example.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.View
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.*
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mvc.intercept_video_link.MyApplication
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.bean.AppInfo
import com.example.mvc.intercept_video_link.bean.HistoryBean
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.event.HistoryAddEvent
import com.example.mvc.intercept_video_link.event.LanguageEvent
import com.example.mvc.intercept_video_link.listener.IDialogInterface
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.service.UrlService
import com.example.mvc.intercept_video_link.utils.DialogHelper
import com.example.mvc.intercept_video_link.utils.JsoupHelper
import com.example.mvc.intercept_video_link.utils.PatternHelper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_controller.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ControllerActivity : BaseActivity() {
    private lateinit var appInfo: AppInfo
    private lateinit var urlService: UrlService
    private lateinit var historyList: HashMap<String, HistoryBean>
    private var videoList = ArrayList<VideoInfo>()

    override fun getLayoutId(): Int {
        return R.layout.activity_controller
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        historyList = HashMap()
        var bindIntent = intent
        bindIntent.setClass(this, UrlService::class.java)
        bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
        app_clear_cache.setRightString(getDirSize(cacheDir))
    }

    override fun initData() {
        DialogHelper.getInstance().createHintDialog(this, getString(R.string.app_help), getString(R.string.app_dialog_title), object : IDialogInterface {
            override fun clickCallback(view: View) {

            }

            override fun dismissCallback() {
                //检查悬浮窗权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this@ControllerActivity)) {
                        AlertDialog.Builder(this@ControllerActivity)
                                .setTitle("请求开启权限")
                                .setMessage("开启悬浮窗权限之后能够更方便的获取到知乎视频\n建议开启")
                                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
                                .setPositiveButton("开启") { dialog, which ->
                                    dialog.dismiss()
                                    var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                                    startActivityForResult(intent, 300)
                                }
                                .show()
                    } else {
                        app_backstage.setRightString("已开启")
                    }
                } else {
                    app_backstage.setRightString("已开启")
                }
            }
        }).show()
    }

    fun onClick(view: View) {
        when (view.id) {
//            设置语言
            R.id.app_language -> {
                startActivityForResult(Intent(this@ControllerActivity, LanguageActivity::class.java), 200)
            }
//            设置主题
            R.id.app_theme -> {
                ToastUtils.showShort("暂未开放")
            }
//            设置悬浮窗
            R.id.app_backstage -> {
                var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 300)
            }
//            查看当前
            R.id.app_current_record -> {
                startActivityCarryVideoInfo()
            }
//            查看历史
            R.id.app_history_record -> {
                var videoIntent = Intent(this, VideoHistoryActivity::class.java)
                videoIntent.putExtra("historyList", historyList)
                startActivity(videoIntent)
            }
//            删除下载
            R.id.app_delete_download -> {
                AlertDialog.Builder(this@ControllerActivity)
                        .setTitle("删除视频")
                        .setMessage("即将删除所有视频，删除后可能无法恢复，是否允许？")
                        .setNegativeButton("否") { dialog, which -> dialog.dismiss() }
                        .setPositiveButton("是") { dialog, which ->
                            dialog.dismiss()
                            var file = File("${Environment.getExternalStorageDirectory().absolutePath}/ZhiHuVideo/")
                            if (FileUtils.deleteDir(file)) {
                                ToastUtils.showShort("删除成功")
                            } else {
                                ToastUtils.showShort("删除失败")
                            }
                        }
                        .show()
            }
//            打开下载
            R.id.app_open_download -> {
                AlertDialog.Builder(this@ControllerActivity)
                        .setTitle("打开文件夹")
                        .setMessage("即将离开App，前往下载目录，是否前往？")
                        .setNegativeButton("否") { dialog, which -> dialog.dismiss() }
                        .setPositiveButton("是") { dialog, which ->
                            dialog.dismiss()
                            var file = File("${Environment.getExternalStorageDirectory().absolutePath}/ZhiHuVideo/")
                            if (file.exists()) {
                                var fileIntent = Intent(Intent.ACTION_GET_CONTENT)
                                fileIntent.addCategory(Intent.CATEGORY_DEFAULT)
                                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    //data是file类型,忘了复制过来
                                    fileIntent.setDataAndType(FileProvider.getUriForFile(baseContext, MyApplication.getAppContext()?.packageName + ".fileprovider", file.parentFile), "video/mp4")
                                } else {
                                    fileIntent.setDataAndType(Uri.fromFile(file), "video/mp4")
                                }
                                startActivity(fileIntent)
                            } else {
                                ToastUtils.showShort("还没有下载文件")
                            }
                        }
                        .show()
            }
//            清除缓存
            R.id.app_clear_cache -> {
                if (FileUtils.deleteDir(cacheDir)) {
                    ToastUtils.showShort(R.string.data_chear_cache_success)
                    app_clear_cache.setRightString(getDirSize(cacheDir))
                } else {
                    ToastUtils.showShort(R.string.data_chear_cache_failed)
                }
            }
        }
    }

    override fun onDestroy() {
        if (urlService !== null) {
            unbindService(urlConnection)
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private var urlConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is UrlService.UrlBind) {
                urlService = service.getService()
                urlService.setParsingCallback(object : ParsingCallback {
                    override fun startActivity(context: Context) {
                        startActivityCarryVideoInfo()
                    }

                    override fun AnalysisSourceCode(primary: String) {
                        //检查悬浮窗权限
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this@ControllerActivity)) {
                                urlService.createLoadView()
                                resolveVideo(primary)
                            }
                        } else {
                            urlService.createLoadView()
                            resolveVideo(primary)
                        }
                    }
                })
            }
        }
    }

    @Subscribe
    fun resetLanguage(language: LanguageEvent) {
        if (urlService !== null) {
            unbindService(urlConnection)
        }
        recreate()
    }

    /**
     * 解析网址
     */
    @SuppressLint("CheckResult")
    fun resolveVideo(url: String) {
        if (!PatternHelper.isHttpUrl(url)) {
            ToastUtils.showShort("地址无效")
            return
        }
        Observable.just(url)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    var httpUrl = URL(it)
                    var conn = httpUrl.openConnection() as HttpURLConnection
                    var inStream = conn.inputStream
                    var htmlSourceCode = String(inStream.readBytes())
                    videoList.clear()
                    this.videoList.addAll(JsoupHelper.getInstance(htmlSourceCode).getAllResource())
                    Observable.just(videoList)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    if (list.size > 0) {
                        var childHistoryList = ArrayList<HistoryBean.DataBean>()
                        for (videoInfo in list) {
                            var childHistory = HistoryBean.DataBean(videoInfo.videoSrc, videoInfo.title, videoInfo.imgsrc)
                            childHistoryList.add(childHistory)
                        }
                        var history = HistoryBean(url, System.currentTimeMillis(), childHistoryList)
                        historyList[url] = history
                        urlService.updateView("点击查看视频列表", true)
                        EventBus.getDefault().post(HistoryAddEvent(history))
                    } else {
                        urlService.updateView("没有视频", false)
                    }
                }, { thorw ->
                    LogUtils.e(thorw.message)
                    urlService.updateView("解析失败", false)
                })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            //申请悬浮窗权限
            300 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this@ControllerActivity)) {
                        app_backstage.setRightString("已关闭")
                    } else {
                        app_backstage.setRightString("已开启")
                    }
                }
            }
        }
    }

    fun startActivityCarryVideoInfo() {
        var mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.putParcelableArrayListExtra("videoList", videoList)
        startActivity(mainIntent)
    }
}