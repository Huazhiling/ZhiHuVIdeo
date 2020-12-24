package com.sd.mvc.intercept_video_link.utils

import com.blankj.utilcode.util.SPUtils

object SPUtils {

    fun putString(key: String, value: String) {
        SPUtils.getInstance().put(key, value)
    }

    fun putInt(key: String, value: Int) {
        SPUtils.getInstance().put(key, value)
    }

    fun putFloat(key: String, value: Float) {
        SPUtils.getInstance().put(key, value)
    }

    fun getString(key: String): String {
        return SPUtils.getInstance().getString(key)
    }

    fun getInt(key: String): Int {
        return SPUtils.getInstance().getInt(key)
    }

    fun getFloat(key: String): Float {
        return SPUtils.getInstance().getFloat(key)
    }
}