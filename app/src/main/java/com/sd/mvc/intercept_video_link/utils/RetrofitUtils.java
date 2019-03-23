package com.sd.mvc.intercept_video_link.utils;


import com.blankj.utilcode.util.LogUtils;
import com.sd.mvc.intercept_video_link.MyApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {
    private static Retrofit mRetrofit;

    private static Retrofit getInstance() {
        if (mRetrofit == null) {
            synchronized (Retrofit.class) {
                if (mRetrofit == null) {
                    mRetrofit = new Retrofit.Builder()
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(MyApplication.Companion.getBaseUrl())
                            .client(getOkhttpUtils()).build();
                }
            }
        }
        return mRetrofit;
    }

    private static OkHttpClient getOkhttpUtils() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(message -> LogUtils.e("RetrofitUtils", message))
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(chain -> {
                    Response proceed = chain.proceed(chain.request());
                    assert proceed.body() != null;
                    return proceed.newBuilder()
                            .body(new DownloadBody(proceed.body()))
                            .build();
                })
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
        return client;
    }

    public static <T> T client(Class<T> clazz) {
        return getInstance().create(clazz);
    }
}
