package com.example.mvc.intercept_video_link.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.mvc.intercept_video_link.R
import com.gyf.barlibrary.ImmersionBar

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
        initData()
    }

    abstract fun initData()

    open fun initView(){
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
    }

    abstract fun getLayoutId(): Int
}