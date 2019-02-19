package com.example.mvc.intercept_video_link

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.example.mvc.intercept_video_link.bean.AppInfo
import com.example.mvc.intercept_video_link.common.Constant.APPINFO
import com.example.mvc.intercept_video_link.utils.JsonHelper
import java.util.ArrayList

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        application = this
        initAppInfo()
    }

    /**
     * 初始化App信息
     */
    private fun initAppInfo() {
        var app = SPUtils.getInstance().getString(APPINFO)
        if (app == "") {
            //语言
            var languageList = ArrayList<AppInfo.AppInfoBean.LanguageBean>()
            languageList.add(AppInfo.AppInfoBean.LanguageBean("简体中文"))
            languageList.add(AppInfo.AppInfoBean.LanguageBean("English"))
            //主题
            var themeList = ArrayList<AppInfo.AppInfoBean.ThemeBean>()
            themeList.add(AppInfo.AppInfoBean.ThemeBean("极简白", "Simple White"))
            themeList.add(AppInfo.AppInfoBean.ThemeBean("炫酷黑", "Cool Black"))
            var appInfoBean = AppInfo.AppInfoBean(getString(R.string.main_title)
                    , getString(R.string.main_default_theme)
                    , getString(R.string.main_default_language)
                    , false/*默认权限不开启*/
                    , languageList
                    , themeList)
            var appContentBean = AppInfo.AppContentBean(getString(R.string.data_title)
                    , getString(R.string.data_current_record)
                    , getString(R.string.data_history)
                    , getString(R.string.data_download))
            appInfo = AppInfo(appInfoBean, appContentBean)
            SPUtils.getInstance().put(APPINFO, JsonHelper.jsonToString(appInfoBean))
        } else {
            appInfo = JsonHelper.stringToJson(app, AppInfo::class.java) as AppInfo
        }
    }

    companion object {
        private var application: Context? = null
        private lateinit var appInfo: AppInfo
        fun getAppContext(): Context? {
            return application
        }

        fun getAppInfo(): AppInfo? {
            return appInfo
        }

        fun getBaseUrl(): String {
            return getAppContext()!!.getString(R.string.base_url)
        }
    }

}