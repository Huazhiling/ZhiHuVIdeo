package com.sd.mvc.intercept_video_link.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.adapter.HistoryAdapter
import com.sd.mvc.intercept_video_link.base.BaseActivity
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import com.sd.mvc.intercept_video_link.event.HistoryAddEvent
import com.sd.mvc.intercept_video_link.utils.RuleRecyclerLines
import com.sd.mvc.intercept_video_link.utils.RxHelper
import com.sd.mvc.intercept_video_link.utils.SQLiteHelper
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_videohistory.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class VideoHistoryActivity : BaseActivity() {
    private lateinit var historyArray: ArrayList<HistoryBean>
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var sqLiteHelper: SQLiteHelper
    @SuppressLint("CheckResult")
    override fun initData() {
        Observable.create<ArrayList<HistoryBean>> {
            it.onNext(sqLiteHelper.findAllVideo()!!)
        }.compose(RxHelper.rxSchedulerHelper())
                .flatMap {
                    Observable.just(it)
                }.subscribe ({
                    loadHistory(it)
                },{
                    error->
                })
    }

    private fun loadHistory(it: ArrayList<HistoryBean>?) {
        if (it!!.size > 0) {
            historyArray.addAll(it)
            history_null.visibility = View.INVISIBLE
            history_rv.visibility = View.VISIBLE
            historyAdapter.notifyDataSetChanged()
        } else {
            history_null.visibility = View.VISIBLE
            history_rv.visibility = View.INVISIBLE
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_videohistory
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        sqLiteHelper = SQLiteHelper(baseContext, "fox", null, 1)
        historyArray = ArrayList()
        historyAdapter = HistoryAdapter(R.layout.item_history, historyArray)
        history_rv.adapter = historyAdapter
        history_rv.addItemDecoration(RuleRecyclerLines(baseContext, RuleRecyclerLines.HORIZONTAL_LIST, 1))
        historyAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.item_layout -> {
                    var mainIntent = Intent(this, MainActivity::class.java)
                    mainIntent.putExtra("primary_key", historyArray[position].url)
                    startActivity(mainIntent)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @Subscribe
    fun updateList(addEvent: HistoryAddEvent) {
        Observable.create<ArrayList<HistoryBean>> {
            it.onNext(sqLiteHelper.findAllVideo()!!)
        }.compose(RxHelper.rxSchedulerHelper())
                .flatMap {
                    Observable.just(it)
                }.subscribe ({
                    loadHistory(it)
                },{
                    error->
                })
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}