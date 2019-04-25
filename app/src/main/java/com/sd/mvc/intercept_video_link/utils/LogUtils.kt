package com.sd.mvc.intercept_video_link.utils

import android.util.Log

object LogUtils {
    private var isDebug = true
    private val I = 0
    private val D = 1
    private val W = 2
    private val E = 3


    fun i(msg: String) {
        log(msg, I)
    }

    fun d(msg: String) {
        log(msg, D)
    }

    fun w(msg: String) {
        log(msg, W)
    }

    fun e(msg: String) {
        log(msg, E)
    }


    private fun log(msg: String, level: Int) {
        if (isDebug) {
            when (level) {
                I -> {
                    Log.i("Fox_Log",msg)
                }
                D -> {
                    Log.d("Fox_Log",msg)
                }
                W -> {
                    Log.w("Fox_Log",msg)
                }
                E -> {
                    Log.e("Fox_Log",msg)
                }
            }
        }
    }
}