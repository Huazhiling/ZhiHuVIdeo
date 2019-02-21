package com.example.mvc.intercept_video_link.activity

import android.content.Intent
import android.view.View
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.adapter.HistoryAdapter
import com.example.mvc.intercept_video_link.bean.HistoryBean
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.event.HistoryAddEvent
import com.example.mvc.intercept_video_link.utils.RuleRecyclerLines
import kotlinx.android.synthetic.main.activity_videohistory.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class VideoHistoryActivity : BaseActivity() {
    private lateinit var historyList: HashMap<String, HistoryBean>
    private lateinit var historyArray: ArrayList<HistoryBean>
    private lateinit var historyAdapter: HistoryAdapter

    override fun initData() {
        for (mutableEntry in historyList) {
            historyArray.add(mutableEntry.value)
            if (historyArray.size > 0) {
                history_rv.visibility = View.VISIBLE
                history_null.visibility = View.INVISIBLE
                historyAdapter.notifyDataSetChanged()
            } else {
                history_rv.visibility = View.INVISIBLE
                history_null.visibility = View.VISIBLE
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_videohistory
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        historyArray = ArrayList()
        historyList = intent.getSerializableExtra("historyList") as HashMap<String, HistoryBean>
        historyAdapter = HistoryAdapter(R.layout.item_history, historyArray)
        history_rv.adapter = historyAdapter
        history_rv.addItemDecoration(RuleRecyclerLines(baseContext, RuleRecyclerLines.HORIZONTAL_LIST, 1))
        historyAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.item_layout -> {
                    var videoList = ArrayList<VideoInfo>()
                    var historyData = historyList[historyArray[position].url]!!.dataBean
                    for (historyDatum in historyData) {
                        var video = VideoInfo(historyDatum.imageUrl, historyDatum.title, historyDatum.url)
                        videoList.add(video)
                    }
                    var mainIntent = Intent(this, MainActivity::class.java)
                    VideoInfo
                    mainIntent.putParcelableArrayListExtra("videoList", videoList)
                    startActivity(mainIntent)
                }
            }
        }
    }

    @Subscribe
    fun updateList(addEvent: HistoryAddEvent) {
        historyArray.add(addEvent.historyBean)
        historyList[addEvent.historyBean.url] = addEvent.historyBean
        if (historyArray.size > 0) {
            history_rv.visibility = View.VISIBLE
            history_null.visibility = View.INVISIBLE
            historyAdapter.notifyDataSetChanged()
        } else {
            history_rv.visibility = View.INVISIBLE
            history_null.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}