package com.example.mvc.intercept_video_link

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        application = this
    }

    companion object {
        private var application: Context? = null
        private fun getAppContext(): Context? {
            return application
        }

        fun getBaseUrl(): String {
            return getAppContext()!!.getString(R.string.base_url)
        }
    }

}