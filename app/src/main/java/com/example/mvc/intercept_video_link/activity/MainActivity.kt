package com.example.mvc.intercept_video_link.activity

import android.content.*
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.service.UrlService
import com.example.mvc.intercept_video_link.adapter.VideoAdapter
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.utils.JsoupHelper
import com.example.mvc.intercept_video_link.utils.PatternHelper
import com.example.mvc.intercept_video_link.utils.RuleRecyclerLines
import com.gyf.barlibrary.ImmersionBar
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var urlBind: UrlService.UrlBind? = null
    private var videoInfo = ArrayList<VideoInfo>()
    private lateinit var adapter: VideoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
        var bindIntent = intent
        bindIntent.setClass(this, UrlService::class.java)
        bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
//        检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                        .setTitle("请求开启权限")
                        .setMessage("开启悬浮窗权限之后能够更方便的获取到知乎视频\n建议开启")
                        .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
                        .setPositiveButton("开启") { dialog, which ->
                            dialog.dismiss()
                            var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                            startActivityForResult(intent, 200)
                        }
                        .show()
            }
        }
    }

    override fun onDestroy() {
        if (urlBind !== null) {
            urlBind = null
            unbindService(urlConnection)
        }
        super.onDestroy()
    }

    private var urlConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            urlBind = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is UrlService.UrlBind) {
                adapter = VideoAdapter(R.layout.item_search_list, videoInfo)
                adapter.setOnItemChildClickListener { adapter, view, position ->
                    when (view.id) {
                        R.id.item_layout -> {
                            var intent = Intent(this@MainActivity, WebVideoActivity::class.java)
                            intent.putExtra("video_url", videoInfo[position].videoSrc)
                            intent.putExtra("video_title", videoInfo[position].title)
                            startActivity(intent)
                        }
                    }
                }
                video_list.addItemDecoration(RuleRecyclerLines(this@MainActivity.applicationContext, RuleRecyclerLines.HORIZONTAL_LIST, 1))
                video_list.adapter = adapter
                urlBind = service
                urlBind!!.getService().setParsingCallback(object : ParsingCallback {
                    override fun AnalysisSourceCode(primary: String) {
                        search_edit.setText(primary)
                        search_submit.performClick()
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.search_submit -> {
                var content = search_edit.text.toString()
                if (!PatternHelper.isHttpUrl(content)) {
                    ToastUtils.showShort("地址无效")
                    return
                }
                videoInfo.clear()
                video_load.visibility = View.VISIBLE
                video_null.visibility = View.INVISIBLE
                video_list.visibility = View.INVISIBLE
                Observable.just(content)
                        .subscribeOn(Schedulers.io())
                        .flatMap {
                            var httpUrl = URL(it)
                            var conn = httpUrl.openConnection() as HttpURLConnection
                            var inStream = conn.inputStream
                            var htmlSourceCode = String(inStream.readBytes())
                            var videoList = JsoupHelper.getInstance(htmlSourceCode).getAllResource()
                            LogUtils.e(videoList.size)
                            Observable.just(videoList)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ list ->
                            videoInfo.addAll(list)
                            if (videoInfo.size > 0) {
                                adapter.notifyDataSetChanged()
                                video_null.visibility = View.INVISIBLE
                                video_load.visibility = View.INVISIBLE
                                video_list.visibility = View.VISIBLE
                            } else {
                                video_null.visibility = View.VISIBLE
                                video_load.visibility = View.INVISIBLE
                                video_list.visibility = View.INVISIBLE
                            }
                        }, { thorw ->
                            LogUtils.e(thorw.message)
                            ToastUtils.showShort("链接解析错误，请重试或更换链接地址")
                        })
            }
        }
    }
}
