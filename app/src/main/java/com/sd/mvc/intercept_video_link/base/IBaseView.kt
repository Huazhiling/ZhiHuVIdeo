package com.sd.mvc.intercept_video_link.base

interface IBaseView {
    fun initPresenter() : BasePresenter<*, *>
}