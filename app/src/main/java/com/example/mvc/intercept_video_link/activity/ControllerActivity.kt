package com.example.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.View
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.*
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.bean.AppInfo
import com.example.mvc.intercept_video_link.bean.VideoInfo
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
import java.net.HttpURLConnection
import java.net.URL

class ControllerActivity : BaseActivity() {
    private lateinit var appInfo: AppInfo
    private lateinit var urlService: UrlService
    private var videoList = ArrayList<VideoInfo>()

    override fun getLayoutId(): Int {
        return R.layout.activity_controller
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        var bindIntent = intent
        bindIntent.setClass(this, UrlService::class.java)
        bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
        LogUtils.e(getDirSize(cacheDir))
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

            }
//            删除下载
            R.id.app_delete_download -> {

            }
//            打开下载
            R.id.app_open_download -> {

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
                        urlService.updateView("点击查看视频列表", true)
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
        LogUtils.e("video.size${videoList.size}")
        mainIntent.putParcelableArrayListExtra("videoList", videoList)
        startActivity(mainIntent)
    }
}