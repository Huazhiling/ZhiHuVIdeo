package com.example.mvc.intercept_video_link.utils

import com.blankj.utilcode.util.LogUtils
import com.example.mvc.intercept_video_link.bean.VideoInfo
import com.example.mvc.intercept_video_link.listener.ApiStore
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.HttpURLConnection
import java.net.URL

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
            var title = videosElement.select("span.title").text()
            var videoUrl = videosElement.select("span.url").text()
            var video_id = videoUrl.substring(videoUrl.lastIndexOf("/") + 1, videoUrl.length)
            var zhihuVideoBean = RetrofitUtils.client(ApiStore::class.java).getVideoInfo(video_id).execute()
            var video = VideoInfo(thumbnailElement.attr("src"), if (title == "") "暂无标题" else title, zhihuVideoBean.body()!!.playlist.ld.play_url, videoUrl)
            urlList.add(video)
        }
        return urlList
    }

    fun getDownLoadUrl(url: String): String {
        var httpUrl = URL(url)
        var conn = httpUrl.openConnection() as HttpURLConnection
        var inStream = conn.inputStream
        var htmlSourceCode = String(inStream.readBytes())
        var sb = StringBuffer()
        var thumbnails = Jsoup.parse(htmlSourceCode).body().select("div#player")
        LogUtils.e("$url  ${thumbnails.size}")
        LogUtils.e(htmlSourceCode)
        for (thumbnail in thumbnails) {
            LogUtils.e(thumbnails.html())
        }
        return sb.toString()
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