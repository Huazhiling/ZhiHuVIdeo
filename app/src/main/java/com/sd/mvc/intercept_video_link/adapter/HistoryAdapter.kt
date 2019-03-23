package com.sd.mvc.intercept_video_link.adapter

import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import java.util.*

class HistoryAdapter(layoutResId: Int, data: ArrayList<HistoryBean>?) : BaseQuickAdapter<HistoryBean, BaseViewHolder>(layoutResId, data) {
    override fun convert(helper: BaseViewHolder?, item: HistoryBean?) {
        helper?.setText(R.id.item_time, TimeUtils.date2String(Date(item!!.time)))
        helper?.setText(R.id.item_title, item!!.url)
        helper?.setText(R.id.item_count, "${item!!.dataBean.size}个\n视频")
        helper?.addOnClickListener(R.id.item_layout)
    }
}