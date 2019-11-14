package com.sd.mvc.intercept_video_link.adapter

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.bean.MediaBean

class MediaAdapter(layoutResId: Int, data: ArrayList<MediaBean>): BaseQuickAdapter<MediaBean, BaseViewHolder>(layoutResId, data)  {
    override fun convert(helper: BaseViewHolder, item: MediaBean) {
        var srcImg = helper.getView<ImageView>(R.id.file_thumbnail)
        Glide.with(mContext).load(item.thumbnail).into(srcImg)
        helper.setText(R.id.file_name,item.name)
        helper.setText(R.id.file_size,item.size)
    }
}