package com.example.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.content.*
import android.os.IBinder
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.mvc.intercept_video_link.listener.ParsingCallback
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.service.UrlService
import com.example.mvc.intercept_video_link.adapter.VideoAdapter
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.utils.JsoupHelper
import com.example.mvc.intercept_video_link.utils.PatternHelper
import com.example.mvc.intercept_video_link.utils.RuleRecyclerLines
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : BaseActivity() {
    private var urlBind: UrlService.UrlBind? = null
    private var videoInfo = ArrayList<VideoInfo>()
    private lateinit var urlService: UrlService
    private lateinit var adapter: VideoAdapter
    private lateinit var url: String
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }


    override fun initView() {
        super.initView()
        var bindIntent = intent
        bindIntent.setClass(this, UrlService::class.java)
        bindService(bindIntent, urlConnection, Context.BIND_AUTO_CREATE)
    }

    override fun initData() {

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
                urlService = service.getService()
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
                urlService.setParsingCallback(object : ParsingCallback {
                    override fun AnalysisSourceCode(primary: String) {
                        resolveVideo(primary)
                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    @SuppressLint("CheckResult")
    fun resolveVideo(url: String) {
        if (!PatternHelper.isHttpUrl(url)) {
            ToastUtils.showShort("地址无效")
            return
        }
        videoInfo.clear()
        video_load.visibility = View.VISIBLE
        video_null.visibility = View.INVISIBLE
        video_list.visibility = View.INVISIBLE
        Observable.just(url)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    var httpUrl = URL(it)
                    var conn = httpUrl.openConnection() as HttpURLConnection
                    var inStream = conn.inputStream
                    var htmlSourceCode = String(inStream.readBytes())
                    var videoList = JsoupHelper.getInstance(htmlSourceCode).getAllResource()
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
                        urlService.updateView("点击查看视频列表", true)
                    } else {
                        video_null.visibility = View.VISIBLE
                        video_load.visibility = View.INVISIBLE
                        video_list.visibility = View.INVISIBLE
                        urlService.updateView("没有视频", false)
                    }
                }, { thorw ->
                    LogUtils.e(thorw.message)
                    urlService.updateView("解析失败", false)
                })
    }
}
