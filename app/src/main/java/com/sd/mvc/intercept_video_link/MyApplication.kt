package com.sd.mvc.intercept_video_link

import android.app.Application
import cn.jiguang.analytics.android.api.JAnalyticsInterface
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.sd.mvc.intercept_video_link.bean.AppInfo
import com.sd.mvc.intercept_video_link.common.Constant.APPINFO
import com.sd.mvc.intercept_video_link.utils.JsonHelper
import java.util.ArrayList

class MyApplication : Application() {

    private lateinit var appInfo: AppInfo
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
                    , "zh_CN"
                    , false/*默认权限不开启*/
                    , languageList
                    , themeList)
            var appContentBean = AppInfo.AppDataBeanBean(getString(R.string.data_title)
                    , getString(R.string.download_list)
                    , getString(R.string.data_history)
                    , getString(R.string.data_download))
            appInfo = AppInfo(appInfoBean, appContentBean)
            SPUtils.getInstance().put(APPINFO, JsonHelper.jsonToString(appInfo))
        } else {
            appInfo = JsonHelper.stringToJson(app, AppInfo::class.java) as AppInfo
            JsonHelper.jsonToString(appInfo)
        }
        JAnalyticsInterface.init(getAppContext())
    }

    fun getAppInfo(): AppInfo {
        return appInfo
    }

    companion object {
        private lateinit var application: Application
        fun getAppContext(): Application {
            return application
        }


        fun getBaseUrl(): String {
            return getAppContext()!!.getString(R.string.base_url)
        }
    }

}