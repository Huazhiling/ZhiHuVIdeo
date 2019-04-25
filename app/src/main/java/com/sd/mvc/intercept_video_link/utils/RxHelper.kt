package com.sd.mvc.intercept_video_link.utils

import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RxHelper {
    /**
     * 统一线程处理
     *
     *
     * 发布事件io线程，接收事件主线程
     */
    fun <T> rxSchedulerHelper(): ObservableTransformer<T, T> {//compose处理线程 并且处理token过期问题
        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
