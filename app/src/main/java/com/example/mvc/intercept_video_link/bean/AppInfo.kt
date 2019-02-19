package com.example.mvc.intercept_video_link.bean

import java.util.ArrayList

data class AppInfo(
        /**
         * app_info : {"title":"设置","default_theme":"white","default_language":"chinese","language":[{"language":"chinese"},{"language":"english"}],"theme":[{"chinese_theme_name":"white"},{"chinese_theme_name":"black"},{"chinese_theme_name":"customize"}],"window_hint":false}
         * app_content : {"current_record":"当前记录","histroy":"历史记录","download":"打开下载目录"}
         */
        var app_info: AppInfoBean,
        var app_content: AppContentBean
) {
    data class AppInfoBean(
            /**
             * title : 设置
             * default_theme : white
             * default_language : chinese
             * language : [{"language":"chinese"},{"language":"english"}]
             * theme : [{"chinese_theme_name":"white"},{"chinese_theme_name":"black"},{"chinese_theme_name":"customize"}]
             * window_hint : false
             */
            var title: String,
            var default_theme: String,
            var default_language: String,
            var isWindow_hint: Boolean = false,
            var language: ArrayList<LanguageBean>,
            var theme: ArrayList<ThemeBean>
    ) {
        data class LanguageBean(
                /**
                 * language : chinese
                 */
                var language: String)

        data class ThemeBean(
                /**
                 * chinese_theme_name : white
                 */
                var chinese_theme_name: String,
                var english_theme_name: String)
    }

    data class AppContentBean(
            /**
             * title : 数据中心
             * current_record : 当前记录
             * history : 历史记录
             * download : 打开下载目录
             */
            var title: String,
            var current_record: String,
            var history: String,
            var download: String
    )
}
