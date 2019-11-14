package com.sd.mvc.intercept_video_link.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.FileUtils.getFileSize
import com.blankj.utilcode.util.ToastUtils
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import com.sd.mvc.intercept_video_link.R
import com.sd.mvc.intercept_video_link.adapter.MediaAdapter
import com.sd.mvc.intercept_video_link.bean.MediaBean
import com.sd.mvc.intercept_video_link.utils.DownloadUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_file_video.*
import java.io.File
import java.io.FileFilter
import java.util.ArrayList

class FileVideoActivity : BaseActivity() {
    private var mediaList = ArrayList<MediaBean>()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var progressDialog: ProgressDialog
    override fun initData() {
        isPermission()
    }

    private fun isPermission() {
        RsPermission.getInstance().setiPermissionRequest(object : IPermissionRequest {
            override fun toSetting() {
                RsPermission.getInstance().toSettingPer()
            }

            override fun cancle(i: Int) {
                ToastUtils.showShort("未获得存储权限，无法下载")
            }

            override fun success(i: Int) {
                searchFile()
            }
        }).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun initView() {
        super.initView()
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        mediaAdapter = MediaAdapter(R.layout.layout_file_video, mediaList)
        video_rv.adapter = mediaAdapter
        video_back.setOnClickListener { finish() }
        data_null.setOnClickListener { searchFile() }
    }

    @SuppressLint("CheckResult")
    private fun searchFile() {
        //需要找寻的文件列表
        progressDialog.setMessage("正在搜索视频,请稍后...")
        progressDialog.show()
        var file = File("${Environment.getExternalStorageDirectory().absolutePath}/ZhiHuVideo/")
        Observable.just(file)
                .flatMap {
                    //是视频文件夹   可以遍历
                    if (file.isDirectory) {
                        Log.e("File", "fileName:${file.name}")
                        file.listFiles(FileFilter {
                            Log.e("File", "fileName:${it.name}")
                            mediaList.add(MediaBean(it.absolutePath, getFileThumbnail(it.absolutePath), it.name, getFileSize(it)))
                            true
                        })
                    }
                    Observable.just(mediaList)
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ media ->
                    if (media.size > 0) {
                        data_null.visibility = View.INVISIBLE
                        mediaAdapter.notifyDataSetChanged()
                    }
                    progressDialog.dismiss()
                }, { error ->
                    ToastUtils.showShort("文件异常")
                    progressDialog.dismiss()
                })
    }

    private fun getFileThumbnail(absolutePath: String): Bitmap {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(absolutePath)
        return mediaMetadataRetriever.frameAtTime
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_file_video
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        RsPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}