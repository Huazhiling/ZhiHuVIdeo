package com.sd.mvc.intercept_video_link.contract

import com.sd.mvc.intercept_video_link.base.BasePresenter
import com.sd.mvc.intercept_video_link.base.IBaseActivity
import com.sd.mvc.intercept_video_link.base.IBaseModel

class ControllerContract {
    abstract class ControllerPresenter : BasePresenter<ControllerModel, ControllerView>() {
        abstract fun initAppInfo()
        abstract fun resolveClipData()

    }

    interface ControllerModel : IBaseModel {
        fun resolveVideo(url: String)
    }

    interface ControllerView : IBaseActivity {
        fun success()
        fun failed()
    }
}