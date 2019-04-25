package com.sd.mvc.intercept_video_link.utils

import com.sd.mvc.intercept_video_link.MyApplication

object ConvertUtils {
    fun dp2px(dp:Int):Int{
        return (MyApplication.getAppContext().resources.displayMetrics.density * dp + 0.5f).toInt()
    }
}