package com.sd.mvc.intercept_video_link.bean

import android.graphics.Bitmap

data class MediaBean(var path: String
                     , val thumbnail: Bitmap
                     , val name: String
                     , val size: String)