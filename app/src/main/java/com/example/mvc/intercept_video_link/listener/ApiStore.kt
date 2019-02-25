package com.example.mvc.intercept_video_link.listener

import com.example.mvc.intercept_video_link.bean.ZhihuVideoBean
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiStore {
    @Streaming
    @GET()
    fun downloadVideo(@Url url: String): Observable<ResponseBody>


    @GET("api/v4/videos/{video_id}")
    fun getVideoInfo(@Path("video_id") url: String): Call<ZhihuVideoBean>

}