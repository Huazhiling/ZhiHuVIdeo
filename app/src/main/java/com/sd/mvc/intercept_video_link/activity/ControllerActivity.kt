package com.sd.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.*
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.sd.mvc.intercept_video_link.MyApplication
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.bean.AppInfo
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import com.sd.mvc.intercept_video_link.bean.VideoInfo
import com.sd.mvc.intercept_video_link.common.Constant.HISTORY_LIST
import com.sd.mvc.intercept_video_link.event.HistoryAddEvent
import com.sd.mvc.intercept_video_link.event.LanguageEvent
import com.sd.mvc.intercept_video_link.listener.IDialogInterface
import com.sd.mvc.intercept_video_link.listener.ParsingCallback
import com.sd.mvc.intercept_video_link.service.UrlService
import com.sd.mvc.intercept_video_link.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_controller.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ControllerActivity : BaseActivity() {
    private lateinit var appInfo: AppInfo
    private lateinit var primaryKey: String
    private lateinit var videoInfo: VideoInfo
    private lateinit var historyList: ArrayList<HistoryBean>
    private var isBindService = false
    private var urlService: UrlService? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_controller
    }

    override fun initView() {
        super.initView()
        historyList = ArrayList()
        EventBus.getDefault().register(this)
        var bindIntent = Intent(this, UrlService::class.java)
        if (SPUtils.getInstance().getString(HISTORY_LIST) == "") {
            SPUtils.getInstance().put(HISTORY_LIST, JsonHelper.jsonToString(HashMap<String, HistoryBean>()))
        }
        isBindService = bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
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
//                videoIntent.putExtra("historyList", historyList)
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
                                    fileIntent.setDataAndType(FileProvider.getUriForFile(baseContext, MyApplication.getAppContext().packageName + ".fileprovider", file.parentFile), "video/mp4")
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
//            联系作者
            R.id.app_contact_author -> {
                try {
                    // 获取剪贴板管理服务
                    var cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    //将文本数据（微信号）复制到剪贴板
                    cm.primaryClip = ClipData.newPlainText(null, "Scooki_Link1004")
                    //跳转微信
                    var intent = Intent(Intent.ACTION_MAIN)
                    var cmp = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.component = cmp
                    startActivity(intent)
                    Toast.makeText(this, "微信号已复制到粘贴板，请使用", Toast.LENGTH_LONG).show()
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, "您还没有安装微信，请安装后使用", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        if (isBindService) {
            urlService?.closeSQLite()
            unbindService(urlConnection)
            isBindService = false
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private var urlConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            urlService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is UrlService.UrlBind) {
                urlService = service.getService()
                urlService?.setParsingCallback(object : ParsingCallback {
                    override fun startActivity(context: Context) {
                        startActivityCarryVideoInfo()
                    }

                    override fun analysisSourceCode(primary: String) {
                        urlService?.createLoadView()
                        resolveVideo(primary)
                    }
                })

            }
        }
    }

    //
    @Subscribe
    fun resetLanguage(language: LanguageEvent) {
        if (isBindService) {
            unbindService(urlConnection)
            isBindService = false
        }
        recreate()
    }

    /**
     * 解析网址
     */
    @SuppressLint("CheckResult")
    fun resolveVideo(url: String) {
        if (!PatternHelper.isHttpUrl(url)) {
//            ToastUtils.showShort("地址无效")
            return
        }
        Observable.create<String> {
            it.onNext(url)
        }.subscribeOn(Schedulers.io()).flatMap {
            var httpUrl = URL(it)
            var conn = httpUrl.openConnection() as HttpURLConnection
            var inStream = conn.inputStream
            var htmlSourceCode = String(inStream.readBytes())
            LogUtils.e(htmlSourceCode)
            primaryKey = it
            videoInfo = JsoupHelper.getInstance(htmlSourceCode).getAllResource()
            Observable.just(videoInfo.dataBean)
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ videoList ->
            if (videoList!!.size > 0) {
                if(!urlService?.insertData(primaryKey, videoInfo.title!!, System.currentTimeMillis())!!){
                    for (videoInfo in videoList) {
                        urlService?.insertFoxNewData(primaryKey
                                , videoInfo.videoSrc
                                , if (videoInfo.zTitle == "") SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date(System.currentTimeMillis())) else videoInfo.zTitle
                                , videoInfo.imgsrc
                                , videoInfo.downLoadUrl)
                    }
                }
                urlService?.updateView("点击查看视频列表", true)
                EventBus.getDefault().post(HistoryAddEvent(HistoryBean(null, null, null,null)))
            } else {
                urlService?.updateView("没有视频", false)
            }
        }, {
            LogUtils.e(it.message!!)
            urlService?.updateView("解析失败", false)
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
        mainIntent.putExtra("primary_key", primaryKey)
        startActivity(mainIntent)
    }
}