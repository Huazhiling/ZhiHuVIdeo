package com.sd.mvc.intercept_video_link.bean

data class HistoryBean(
        var url: String?,
        var title: String?,
        var time: Long?,
        var dataBean: ArrayList<DataBean>?

) {
    data class DataBean(
            var url: String,
            var title: String,
            var imageUrl: String,
            var downloadUrl: String)
}

