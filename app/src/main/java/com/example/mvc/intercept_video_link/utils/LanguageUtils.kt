package com.example.mvc.intercept_video_link.utils

import android.content.Context
import android.content.res.Configuration
import com.blankj.utilcode.util.SPUtils
import com.example.mvc.intercept_video_link.common.Constant.DEFAULT_LANGUAGE
import java.util.*

class LanguageUtils {
    companion object {
        //获取用户设置的Local
        fun getUserSetLocal(): String {
            return SPUtils.getInstance().getString(DEFAULT_LANGUAGE)
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

        fun changeLocale(language: String, accept_language: String, configuration: Configuration, baseContext: Context) {
            var locale = Locale(language)
            configuration.setLocale(locale)
            baseContext.createConfigurationContext(configuration)
            SPUtils.getInstance().put(DEFAULT_LANGUAGE, language)
//            SPUtils.getInstance().put(DEFAULT_ACCEPT_LANGUAGE, accept_language)
        }
    }
}