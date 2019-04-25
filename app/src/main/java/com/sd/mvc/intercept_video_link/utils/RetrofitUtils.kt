package com.sd.mvc.intercept_video_link.utils


import com.sd.mvc.intercept_video_link.MyApplication

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtils {

    private val instance: Retrofit by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MyApplication.getBaseUrl())
                .client(okhttpUtils).build()
    }

    private val okhttpUtils: OkHttpClient
        get() = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor { message -> LogUtils.e(message) }
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor { chain ->
                    val proceed = chain.proceed(chain.request())
                    assert(proceed.body() != null)
                    proceed.newBuilder()
                            .body(DownloadBody(proceed.body()!!))
                            .build()
                }
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build()

    fun <T> client(clazz: Class<T>): T {
        return instance.create(clazz)
    }
}
