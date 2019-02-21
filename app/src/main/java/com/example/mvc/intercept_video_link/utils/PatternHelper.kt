package com.example.mvc.intercept_video_link.utils

import java.util.regex.Pattern

object PatternHelper {
    val httpUrlPattern = "^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+(zhihu)[/\\?\\:]?.*\$"
    val contentPattern = "^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*?(?<!js|css)\$"
    fun isHttpUrl(httpUrl: String): Boolean {
        return Pattern.compile(httpUrlPattern).matcher(httpUrl).matches()
    }

    fun excludeJsAndCss(sourceCode: String): Boolean {
        return Pattern.compile(contentPattern).matcher(sourceCode).matches()
    }
}