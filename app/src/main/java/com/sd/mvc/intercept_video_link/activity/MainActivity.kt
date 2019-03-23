package com.sd.mvc.intercept_video_link.activity

import android.Manifest
import android.content.*
import android.support.v7.app.AlertDialog
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.adapter.VideoAdapter
import com.sd.mvc.intercept_video_link.bean.VideoInfo
import com.sd.mvc.intercept_video_link.utils.DownloadUtils
import com.sd.mvc.intercept_video_link.utils.RuleRecyclerLines
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private var videoInfo = ArrayList<VideoInfo>()
    private lateinit var adapter: VideoAdapter
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }


    override fun initView() {
        super.initView()
        adapter = VideoAdapter(R.layout.item_search_list, videoInfo)
        adapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.item_layout -> {
                    var intent = Intent(this@MainActivity, WebVideoActivity::class.java)
                    intent.putExtra("video_url", videoInfo[position].videoSrc)
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
                                        LogUtils.e("成功")
                                        DownloadUtils.downloadVideo(baseContext, videoInfo[position].downLoadUrl)
                                    }
                                }).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                            .show()
                }
            }
        }
        video_list.addItemDecoration(RuleRecyclerLines(this@MainActivity.applicationContext, RuleRecyclerLines.HORIZONTAL_LIST, 1))
        video_list.adapter = adapter
        videoInfo.clear()
        videoInfo.addAll(intent.getParcelableArrayListExtra("videoList"))
        if (videoInfo.size > 0) {
            adapter.notifyDataSetChanged()
            video_load.visibility = View.INVISIBLE
            video_null.visibility = View.INVISIBLE
            video_list.visibility = View.VISIBLE
        } else {
            video_load.visibility = View.INVISIBLE
            video_list.visibility = View.INVISIBLE
            video_null.visibility = View.VISIBLE
        }
    }

    override fun initData() {

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        videoInfo.clear()
        videoInfo.addAll(intent?.getParcelableArrayListExtra("videoList")!!)
        if (videoInfo.size > 0) {
            adapter.notifyDataSetChanged()
            video_load.visibility = View.INVISIBLE
            video_null.visibility = View.INVISIBLE
            video_list.visibility = View.VISIBLE
        } else {
            video_load.visibility = View.INVISIBLE
            video_list.visibility = View.INVISIBLE
            video_null.visibility = View.VISIBLE
        }
    }
}
