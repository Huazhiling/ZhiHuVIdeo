package com.sd.mvc.intercept_video_link.bean;

import java.util.List;

public class AppInfo {

    /**
     * title : 设置
     * AppInfo : {"default_language":"zh_CN","default_theme":"极简白","isWindow_hint":false,"language":[{"language":"简体中文"},{"language":"English"}],"theme":[{"chinese_theme_name":"极简白","english_theme_name":"Simple White"},{"chinese_theme_name":"炫酷黑","english_theme_name":"Cool Black"}]}
     * AppDataBean : {"data_title":"","current_record":"","history_record":"","download":""}
     */

    private AppInfoBean AppInfo;
    private AppDataBeanBean AppDataBean;

    public AppInfo(AppInfoBean appInfo, AppDataBeanBean appDataBean) {
        AppInfo = appInfo;
        AppDataBean = appDataBean;
    }

    public AppInfoBean getAppInfo() {
        return AppInfo;
    }

    public void setAppInfo(AppInfoBean AppInfo) {
        this.AppInfo = AppInfo;
    }

    public AppDataBeanBean getAppDataBean() {
        return AppDataBean;
    }

    public void setAppDataBean(AppDataBeanBean AppDataBean) {
        this.AppDataBean = AppDataBean;
    }

    public static class AppInfoBean {
        /**
         * default_language : zh_CN
         * default_theme : 极简白
         * isWindow_hint : false
         * language : [{"language":"简体中文"},{"language":"English"}]
         * theme : [{"chinese_theme_name":"极简白","english_theme_name":"Simple White"},{"chinese_theme_name":"炫酷黑","english_theme_name":"Cool Black"}]
         */

        private String title;
        private String default_language;
        private String default_theme;
        private boolean isWindow_hint;
        private List<LanguageBean> language;
        private List<ThemeBean> theme;

        public AppInfoBean(String title, String default_theme, String default_language, boolean isWindow_hint, List<LanguageBean> language, List<ThemeBean> theme) {
            this.title = title;
            this.default_theme = default_theme;
            this.default_language = default_language;
            this.isWindow_hint = isWindow_hint;
            this.language = language;
            this.theme = theme;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDefault_language() {
            return default_language;
        }

        public void setDefault_language(String default_language) {
            this.default_language = default_language;
        }

        public String getDefault_theme() {
            return default_theme;
        }

        public void setDefault_theme(String default_theme) {
            this.default_theme = default_theme;
        }

        public boolean isIsWindow_hint() {
            return isWindow_hint;
        }

        public void setIsWindow_hint(boolean isWindow_hint) {
            this.isWindow_hint = isWindow_hint;
        }

        public List<LanguageBean> getLanguage() {
            return language;
        }

        public void setLanguage(List<LanguageBean> language) {
            this.language = language;
        }

        public List<ThemeBean> getTheme() {
            return theme;
        }

        public void setTheme(List<ThemeBean> theme) {
            this.theme = theme;
        }

        public static class LanguageBean {
            /**
             * language : 简体中文
             */

            private String language;

            public LanguageBean(String language) {
                this.language = language;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }
        }

        public static class ThemeBean {
            /**
             * chinese_theme_name : 极简白
             * english_theme_name : Simple White
             */
            public ThemeBean(String chinese_theme_name, String english_theme_name) {
                this.chinese_theme_name = chinese_theme_name;
                this.english_theme_name = english_theme_name;
            }

            private String chinese_theme_name;
            private String english_theme_name;

            public String getChinese_theme_name() {
                return chinese_theme_name;
            }

            public void setChinese_theme_name(String chinese_theme_name) {
                this.chinese_theme_name = chinese_theme_name;
            }

            public String getEnglish_theme_name() {
                return english_theme_name;
            }

            public void setEnglish_theme_name(String english_theme_name) {
                this.english_theme_name = english_theme_name;
            }
        }
    }

    public static class AppDataBeanBean {
        /**
         * data_title :
         * current_record :
         * history_record :
         * download :
         */

        private String data_title;
        private String current_record;
        private String history_record;
        private String download;

        public AppDataBeanBean(String data_title, String current_record, String history_record, String download) {
            this.data_title = data_title;
            this.current_record = current_record;
            this.history_record = history_record;
            this.download = download;
        }

        public String getData_title() {
            return data_title;
        }

        public void setData_title(String data_title) {
            this.data_title = data_title;
        }

        public String getCurrent_record() {
            return current_record;
        }

        public void setCurrent_record(String current_record) {
            this.current_record = current_record;
        }

        public String getHistory_record() {
            return history_record;
        }

        public void setHistory_record(String history_record) {
            this.history_record = history_record;
        }

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }
    }
}
