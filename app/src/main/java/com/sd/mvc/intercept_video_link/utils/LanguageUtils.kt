package com.sd.mvc.intercept_video_link.utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.blankj.utilcode.util.SPUtils
import com.sd.mvc.intercept_video_link.MyApplication
import com.sd.mvc.intercept_video_link.common.Constant.APPINFO
import com.sd.mvc.intercept_video_link.event.LanguageEvent
import org.greenrobot.eventbus.EventBus
import java.util.*

class LanguageUtils {
    companion object {
        //获取用户设置的Local
        fun getUserSetLocal(application: Application): String {
            return (application as MyApplication).getAppInfo().appInfo.default_language
        }

        fun wrapConfiguration(context: Context, config: Configuration): Context {
            return context.createConfigurationContext(config)

        }

        fun wrapLocale(context: Context, locale: Locale): Context {
            var res = context.resources
            var config = res.configuration
            config.setLocale(locale)
            return wrapConfiguration(context, config)
        }

        fun changeLocale(language: String, configuration: Configuration, baseContext: Context, application: Application) {
            var locale = Locale(language)
            configuration.setLocale(locale)
            baseContext.createConfigurationContext(configuration)
            var appInfo = (application as MyApplication).getAppInfo()
            appInfo.appInfo.default_language = language
            SPUtils.getInstance().put(APPINFO, JsonHelper.jsonToString(appInfo))
            EventBus.getDefault().post(LanguageEvent())
//            var cotIntent = Intent(baseContext,ControllerActivity::class.java)
//            cotIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            application.startActivity(cotIntent)
        }
    }
}