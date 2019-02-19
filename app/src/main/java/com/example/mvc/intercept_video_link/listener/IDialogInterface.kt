package com.example.mvc.intercept_video_link.listener

import android.view.View

interface IDialogInterface {
    fun dismissCallback()
    fun clickCallback(view: View)
}