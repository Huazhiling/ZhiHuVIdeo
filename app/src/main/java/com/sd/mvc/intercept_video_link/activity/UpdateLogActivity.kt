package com.sd.mvc.intercept_video_link.activity

import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.base.BaseActivity
import kotlinx.android.synthetic.main.activity_update_log.*

class UpdateLogActivity : BaseActivity() {
    override fun initData() {

    }

    override fun initView() {
        super.initView()
        up_back.setOnClickListener { finish() }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_update_log
    }
}