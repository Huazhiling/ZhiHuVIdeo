package com.example.mvc.intercept_video_link.listener

import com.example.mvc.intercept_video_link.utils.DownloadBody
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiStore {
    @Streaming
    @GET()
    fun downloadVideo(@Url url: String): Observable<ResponseBody>
}