package com.example.mvc.intercept_video_link.bean

import java.io.Serializable

data class HistoryBean(
        var url: String,
        var time: Long,
        var dataBean: ArrayList<DataBean>

) : Serializable {
    data class DataBean(
            var url: String,
            var title: String,
            var imageUrl: String
    ) : Serializable
}

