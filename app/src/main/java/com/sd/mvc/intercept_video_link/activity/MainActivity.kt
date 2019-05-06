package com.sd.mvc.intercept_video_link.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.support.v7.app.AlertDialog
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.adapter.VideoAdapter
import com.sd.mvc.intercept_video_link.utils.DownloadUtils
import com.sd.mvc.intercept_video_link.utils.RuleRecyclerLines
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import com.sd.mvc.intercept_video_link.event.CurrentVideoEvent
import com.sd.mvc.intercept_video_link.utils.RxHelper
import com.sd.mvc.intercept_video_link.utils.SQLiteHelper
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : BaseActivity() {
    private var videoInfo = ArrayList<HistoryBean.DataBean>()
    private lateinit var adapter: VideoAdapter
    private lateinit var sqLiteHelper: SQLiteHelper

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }


    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        sqLiteHelper = SQLiteHelper(baseContext, "fox", null, 1)
        adapter = VideoAdapter(R.layout.item_search_list, videoInfo)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.item_layout -> {
                    var intent = Intent(this@MainActivity, WebVideoActivity::class.java)
                    intent.putExtra("video_url", videoInfo[position].url)
                    intent.putExtra("video_title", videoInfo[position].title)
                    startActivity(intent)
                }
                R.id.item_down -> {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("请求下载")
                            .setMessage("是否下载云端视频到本地？")
                            .setNegativeButton("否") { dialog, which -> dialog.dismiss() }
                            .setPositiveButton("是") { dialog, which ->
                                dialog.dismiss()
                                RsPermission.getInstance().setiPermissionRequest(object : IPermissionRequest {
                                    override fun toSetting() {
                                        RsPermission.getInstance().toSettingPer()
                                    }

                                    override fun cancle(i: Int) {
                                        ToastUtils.showShort("未获得存储权限，无法下载")
                                    }

                                    override fun success(i: Int) {
                                        DownloadUtils.downloadVideo(baseContext, videoInfo[position].downloadUrl)
                                    }
                                }).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                            .show()
                }
            }
        }
        video_list.addItemDecoration(RuleRecyclerLines(this@MainActivity.applicationContext, RuleRecyclerLines.HORIZONTAL_LIST, 1))
        video_list.adapter = adapter
        loadVideo(intent.getStringExtra("primary_key"))
    }

    @SuppressLint("CheckResult")
    private fun loadVideo(primary: String) {
        Observable.create<ArrayList<HistoryBean.DataBean>> {
            it.onNext(sqLiteHelper.findVideoBasedOnThePrimaryKey(primary)!!)
        }.compose(RxHelper.rxSchedulerHelper())
                .flatMap {
                    Observable.just(it)
                }.subscribe({
                    loadCurrentVideo(it)
                }, { error ->
                })

    }

    @Subscribe
    fun updateList(currentVideoEvent: CurrentVideoEvent) {
        loadVideo(currentVideoEvent.url)
    }

    private fun loadCurrentVideo(it: ArrayList<HistoryBean.DataBean>?) {
        videoInfo.clear()
        videoInfo.addAll(it!!)
        if (videoInfo.size > 0) {
            video_load.visibility = View.INVISIBLE
            video_null.visibility = View.INVISIBLE
            video_list.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        } else {
            video_load.visibility = View.INVISIBLE
            video_list.visibility = View.INVISIBLE
            video_null.visibility = View.VISIBLE
        }
    }

    override fun initData() {

    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadVideo(intent.getStringExtra("primary_key"))
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}
