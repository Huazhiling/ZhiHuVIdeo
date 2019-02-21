package com.example.mvc.intercept_video_link.activity

import android.content.*
import android.view.View
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.adapter.VideoAdapter
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.utils.RuleRecyclerLines
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
