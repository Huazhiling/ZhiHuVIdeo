package com.sd.mvc.intercept_video_link.activity

import android.util.Log
import cdc.sed.yff.nm.cm.ErrorCode
import cdc.sed.yff.nm.sp.SpotListener
import cdc.sed.yff.nm.sp.SpotManager
import com.sd.mvc.intercept_video_link.MyApplication
import com.sd.mvc.intercept_video_link.R
import kotlinx.android.synthetic.main.activity_support.*
import com.blankj.utilcode.util.ToastUtils


class SupportActivity : BaseActivity() {
    override fun initData() {

    }

    override fun initView() {
        super.initView()
        SpotManager.getInstance(MyApplication.getAppContext()).setImageType(SpotManager.IMAGE_TYPE_VERTICAL)
        var nativeView = SpotManager.getInstance(MyApplication.getAppContext()).getNativeSpot(this, object : SpotListener {
            override fun onSpotClicked(p0: Boolean) {
                Log.e("SupportActivity", "onSpotClicked")
            }

            override fun onShowSuccess() {
                Log.e("SupportActivity", "onShowSuccess")
            }

            override fun onShowFailed(errorCode: Int) {
                when (errorCode) {
                    ErrorCode.NON_NETWORK -> ToastUtils.showShort("网络异常")
                    ErrorCode.NON_AD -> ToastUtils.showShort("还没有准备好哦")
                    ErrorCode.RESOURCE_NOT_READY -> ToastUtils.showShort("还没有准备好哦")
                    ErrorCode.SHOW_INTERVAL_LIMITED -> ToastUtils.showShort("请勿频繁点击")
                    else -> ToastUtils.showShort("请稍后再试")
                }
                finish()
            }

            override fun onSpotClosed() {
                Log.e("SupportActivity", "onSpotClosed")
            }
        })
        if (nativeView != null) {
            support_parent.addView(nativeView)
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_support
    }
}