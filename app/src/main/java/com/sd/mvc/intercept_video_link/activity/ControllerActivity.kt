package com.sd.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cdc.sed.yff.nm.sp.SpotManager
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.FileUtils.*
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
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
import kotlinx.android.synthetic.main.layout_dialog.view.*
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
    //插入数据库时候的键
    private var primaryKey = ""
    //保存视频信息
    private lateinit var videoInfo: VideoInfo
    private lateinit var historyList: ArrayList<HistoryBean>
    private var isBindService = false
    //service交互用
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
        createHintDialog(this, getString(R.string.app_help), getString(R.string.app_dialog_title)).show()
    }

    private fun createHintDialog(context: Context, msg: String, title: String): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null)
        var mDialog = Dialog(context, R.style.hint_dialog)
        dialogView.dialog_title.text = title
        dialogView.dialog_content.text = msg
        dialogView.dialog_dismiss.setOnClickListener { mDialog.dismiss() }
        mDialog.setContentView(dialogView)
        //一定要在setContentView之后调用，否则无效
        mDialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return mDialog
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
                                urlService!!.deleteAllData()
                                ToastUtils.showShort("删除成功")
                            } else {
                                ToastUtils.showShort("删除失败")
                            }
                        }
                        .show()
            }
//            打开下载
            R.id.app_open_download -> {
                startActivity(Intent(baseContext, FileVideoActivity::class.java))
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
            R.id.app_update_log -> {
                startActivity(Intent(baseContext, UpdateLogActivity::class.java))
            }

            R.id.app_support -> {
                startActivity(Intent(baseContext, SupportActivity::class.java))
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
//                        startActivityCarryVideoInfo()
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
                        if (!urlService?.insertData(primaryKey, videoInfo.title!!, System.currentTimeMillis())!!) {
                            for (videoInfo in videoList) {
                                urlService?.insertFoxNewData(primaryKey
                                        , videoInfo.videoSrc
                                        , if (videoInfo.zTitle == "!") SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date(System.currentTimeMillis())) else videoInfo.zTitle
                                        , videoInfo.imgsrc
                                        , videoInfo.downLoadUrl)
                            }
                        }
                        ToastUtils.showShort("添加下载队列成功")
                        EventBus.getDefault().post(HistoryAddEvent(HistoryBean(null, null, null, null)))
                    } else {
                        ToastUtils.showShort("没有视频")
                    }
                }, {
                    LogUtils.e(it.message!!)
                    ToastUtils.showShort("解析失败")
                })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    fun startActivityCarryVideoInfo() {
        if (primaryKey == "") {
            Toast.makeText(baseContext, "当前没有视频", Toast.LENGTH_SHORT).show()
            return
        }
        var mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.putExtra("primary_key", primaryKey)
        startActivity(mainIntent)
    }
}