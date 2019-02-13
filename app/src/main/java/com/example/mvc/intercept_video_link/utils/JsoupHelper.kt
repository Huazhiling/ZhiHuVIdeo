package com.example.mvc.intercept_video_link.utils

import com.blankj.utilcode.util.LogUtils
import com.example.mvc.intercept_video_link.bean.VideoInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class JsoupHelper {
    companion object {
        private lateinit var elementBody: Element
        fun getInstance(primary: String): JsoupHelper {
            elementBody = Jsoup.parse(primary).body()
            return JsoupHelper()
        }
    }

    fun getAllResource(): ArrayList<VideoInfo> {
        var urlList = ArrayList<VideoInfo>()
        var thumbnails = elementBody.select("img.thumbnail")
        var videos = elementBody.select("span.content")
        if (thumbnails.size !== videos.size) {
            return ArrayList()
        }
        for (data in thumbnails.indices) {
            var thumbnailElement = thumbnails[data]
            var videosElement = videos[data]
            videosElement.select("span.title")
//            var src = element.attr("content")
            var title = videosElement.select("span.title").text()
            var video = VideoInfo(thumbnailElement.attr("src"), if (title == "") "暂无标题" else title, videosElement.select("span.url").text())
            urlList.add(video)
        }
        return urlList
    }

    /**
     * 获取所有Gif ---貌似这个功能不需要  本身就可以下载
     */
    fun getAllGifImage(): ArrayList<String> {
        var urlList = ArrayList<String>()
        var videos = elementBody.select("img[src]")
        LogUtils.e("src${videos.size}")
        for (data in videos.indices) {
            var element = videos[data]
            var src = element.getElementsByAttribute("src")
            LogUtils.e(src)
        }
        return urlList
    }
}