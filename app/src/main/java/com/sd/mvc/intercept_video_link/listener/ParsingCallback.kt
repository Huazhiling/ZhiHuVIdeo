package com.sd.mvc.intercept_video_link.listener

import android.content.Context

interface ParsingCallback {
    fun analysisSourceCode(primary: String)
//    fun getTheParsingResult(result: ArrayList<VideoInfo>)
    fun startActivity(context: Context)
}