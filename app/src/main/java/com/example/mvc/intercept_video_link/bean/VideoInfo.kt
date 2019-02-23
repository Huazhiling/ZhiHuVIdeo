package com.example.mvc.intercept_video_link.bean

import android.os.Parcel
import android.os.Parcelable

data class VideoInfo(
        var imgsrc: String,
        var title: String,
        var downLoadUrl: String,
        var videoSrc: String
):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imgsrc)
        parcel.writeString(title)
        parcel.writeString(downLoadUrl)
        parcel.writeString(videoSrc)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoInfo> {
        override fun createFromParcel(parcel: Parcel): VideoInfo {
            return VideoInfo(parcel)
        }

        override fun newArray(size: Int): Array<VideoInfo?> {
            return arrayOfNulls(size)
        }
    }
}