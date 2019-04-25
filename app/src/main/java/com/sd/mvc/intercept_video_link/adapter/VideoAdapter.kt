package com.sd.mvc.intercept_video_link.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.bean.HistoryBean
import com.sd.mvc.intercept_video_link.bean.VideoInfo

class VideoAdapter(layoutResId: Int, data: ArrayList<HistoryBean.DataBean>?): BaseQuickAdapter<HistoryBean.DataBean, BaseViewHolder>(layoutResId, data) {
    override fun convert(helper: BaseViewHolder?, item: HistoryBean.DataBean?) {
        helper?.setText(R.id.item_title,item?.title)
        var srcImg = helper!!.getView<ImageView>(R.id.item_img)
//        val options = RequestOptions().fallback(R.drawable.default_project).placeholder(R.drawable.loading_img).error(R.drawable.default_project)
        Glide.with(mContext).load(item?.imageUrl).into(srcImg)
        helper.addOnClickListener(R.id.item_layout).addOnClickListener(R.id.item_down)
    }
}