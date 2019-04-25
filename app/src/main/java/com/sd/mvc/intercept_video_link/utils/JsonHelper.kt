package com.sd.mvc.intercept_video_link.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.HashMap

object JsonHelper {
    private val gson = Gson()

    fun jsonToString(obj: Any): String {
        return gson.toJson(obj)
    }

    fun stringToJson(json: String, clazz: Class<*>): Any {
        return gson.fromJson<Any>(json, clazz)
    }
}
