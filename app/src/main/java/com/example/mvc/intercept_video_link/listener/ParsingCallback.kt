package com.example.mvc.intercept_video_link.listener

import android.content.Context

interface ParsingCallback {
    fun AnalysisSourceCode(primary: String)
    fun startActivity(context:Context)
}