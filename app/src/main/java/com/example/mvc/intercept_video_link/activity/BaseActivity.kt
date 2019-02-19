package com.example.mvc.intercept_video_link.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.example.mvc.intercept_video_link.MyApplication
import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.utils.LanguageUtils
import com.gyf.barlibrary.ImmersionBar
import java.util.*

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
        initData()
    }

    abstract fun initData()

    open fun initView() {
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
    }

    override fun attachBaseContext(newBase: Context) {
        LogUtils.e(MyApplication.getAppContext())
        super.attachBaseContext(LanguageUtils.wrapLocale(newBase, Locale(LanguageUtils.getUserSetLocal(MyApplication.getAppContext()))))
    }

    abstract fun getLayoutId(): Int
}