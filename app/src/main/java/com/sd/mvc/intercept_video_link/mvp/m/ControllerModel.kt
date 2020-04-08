package com.sd.mvc.intercept_video_link.mvp.m

import com.sd.mvc.intercept_video_link.base.BaseModel
import com.sd.mvc.intercept_video_link.contract.ControllerContract

class ControllerModel : BaseModel(), ControllerContract.ControllerModel {
    companion object {
        val instant: ControllerModel
            get() = ControllerModel()
    }

    override fun resolveVideo(url: String) {

    }
}