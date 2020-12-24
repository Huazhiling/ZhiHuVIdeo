package com.sd.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.blankj.utilcode.util.FileUtils.deleteDir
import com.blankj.utilcode.util.FileUtils.getDirSize
import com.blankj.utilcode.util.ToastUtils
import com.sd.mvc.intercept_video_link.MyApplication
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.base.BaseMVPActivity
import com.sd.mvc.intercept_video_link.base.BasePresenter
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import com.sd.mvc.intercept_video_link.bean.VideoInfo
import com.sd.mvc.intercept_video_link.common.Constant.HISTORY_LIST
import com.sd.mvc.intercept_video_link.common.Constant.TOKEN
import com.sd.mvc.intercept_video_link.contract.ControllerContract
import com.sd.mvc.intercept_video_link.event.HistoryAddEvent
import com.sd.mvc.intercept_video_link.event.LanguageEvent
import com.sd.mvc.intercept_video_link.listener.ParsingCallback
import com.sd.mvc.intercept_video_link.mvp.p.ControllerPresenter
import com.sd.mvc.intercept_video_link.service.UrlService
import com.sd.mvc.intercept_video_link.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_controller.*
import kotlinx.android.synthetic.main.layout_dialog.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import zlc.season.rxdownload3.core.DownloadConfig.context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ControllerActivity : BaseMVPActivity<ControllerContract.ControllerPresenter>(), ControllerContract.ControllerView {
    //插入数据库时候的键
    private var primaryKey = ""
    //保存视频信息
    private lateinit var videoInfo: VideoInfo
    private lateinit var historyList: ArrayList<HistoryBean>
    private var isBindService = false
    //service交互用
    private var urlService: UrlService? = null
    private lateinit var clipManager: ClipboardManager


    override fun getLayoutId(): Int {
        return R.layout.activity_controller
    }

    override fun initView() {
        super.initView()
        historyList = ArrayList()
        EventBus.getDefault().register(this)
        app_auto.setSwitchCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                //
                if (SPUtils.getString(TOKEN).isEmpty()) {
                    createHintDialog("请先登录").show()
                    app_auto.switchIsChecked = false
                }
            }
        }
    }

    private fun createHintDialog(content: String): Dialog {
        val dialogView = LayoutInflater.from(baseContext).inflate(R.layout.layout_dialog, null)
        var mDialog = Dialog(this, R.style.hint_dialog)
        dialogView.dialog_content.text = content
        dialogView.dialog_dismiss.setOnClickListener { mDialog.dismiss() }
        mDialog.setContentView(dialogView)
        //一定要在setContentView之后调用，否则无效
        mDialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return mDialog
    }

    override fun initData() {
        clipManager = MyApplication.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var bindIntent = Intent(this, UrlService::class.java)
        if (SPUtils.getString(HISTORY_LIST) == "") {
            SPUtils.putString(HISTORY_LIST, JsonHelper.jsonToString(HashMap<String, HistoryBean>()))
        }
        isBindService = bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
        app_clear_cache.setRightString(getDirSize(cacheDir))
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
                startActivityCurrentVideoInfo()
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
                            if (deleteDir(file)) {
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
                if (deleteDir(cacheDir)) {
                    ToastUtils.showShort(R.string.data_chear_cache_success)
                    app_clear_cache.setRightString(getDirSize(cacheDir))
                } else {
                    ToastUtils.showShort(R.string.data_chear_cache_failed)
                }
            }

            R.id.app_version_log -> {
                startActivity(Intent(baseContext, VersionLogActivity::class.java))
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
                //获取service  搭建起沟通的桥梁
                urlService = service.getService()
                urlService?.createView()
                urlService?.setParsingCallback(object : ParsingCallback {
                    override fun startActivity(context: Context) {
//                        startActivityCurrentVideoInfo()
                    }

                    override fun analysisSourceCode(primary: String) {
                        //解析数据
                        resolveVideo(primary)
                    }
                })

            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.post {
            //            mPresenter.resolveClipData()
            if (Build.VERSION.SDK_INT >= 29) {
                var utlSb = StringBuffer()
                if(clipManager.primaryClip === null){
                    return@post
                }
                var primary = clipManager.primaryClip.getItemAt(0).text.toString()
                if (primary != "" && PatternHelper.isHttpUrl(primary)) {
                    if (!primary.substring(0, 8).toLowerCase().contains("https://") && !primary.substring(0, 8).toLowerCase().contains("http://")) {
                        utlSb.append("http://")
                    }
                    utlSb.append(primary)
                    resolveVideo(utlSb.toString())
                }
            }
        }
    }

    override fun initPresenter(): BasePresenter<*, *> {
        return ControllerPresenter.newInstance()
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
                                //插入数据到数据库
                                urlService?.insertFoxNewData(primaryKey
                                        , videoInfo.videoSrc
                                        , if (videoInfo.zTitle == "!") SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date(System.currentTimeMillis())) else videoInfo.zTitle
                                        , videoInfo.imgsrc
                                        , videoInfo.downLoadUrl)
                            }
                        }
//                        ToastUtils.showShort("添加下载队列成功")
                        EventBus.getDefault().post(HistoryAddEvent(HistoryBean(null, null, null, null)))
                    } else {
//                        ToastUtils.showShort("没有视频")
                    }
                }, {
                    LogUtils.e(it.message!!)
//                    ToastUtils.showShort("解析失败")
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

    private fun startActivityCurrentVideoInfo() {
        if (primaryKey == "") {
            Toast.makeText(baseContext, "当前没有视频", Toast.LENGTH_SHORT).show()
            return
        }
        var mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.putExtra("primary_key", primaryKey)
        startActivity(mainIntent)
    }

    override fun success() {

    }

    override fun failed() {

    }
}