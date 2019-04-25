package com.sd.mvc.intercept_video_link.bean


data class VideoInfo(
        var title: String?,
        var dataBean: ArrayList<DataBean>?
) {
    data class DataBean(
            var downLoadUrl: String,
            var videoSrc: String,
            var imgsrc: String,
            var zTitle: String)
}
