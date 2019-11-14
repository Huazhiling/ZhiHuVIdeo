package com.sd.mvc.intercept_video_link.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.FileUtils.getFileSize
import com.blankj.utilcode.util.ToastUtils
import com.per.rslibrary.IPermissionRequest
import com.per.rslibrary.RsPermission
import com.sd.mvc.intercept_video_link.adapter.MediaAdapter
import com.sd.mvc.intercept_video_link.bean.MediaBean
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_file_video.*
import java.io.File
import java.io.FileFilter
import java.util.ArrayList
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.sd.mvc.intercept_video_link.BuildConfig


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
        mediaAdapter = MediaAdapter(com.sd.mvc.intercept_video_link.R.layout.layout_file_video, mediaList)
        video_rv.adapter = mediaAdapter
        mediaAdapter.setOnItemChildClickListener { adapter, view, position ->
            var path = File(mediaList[position].path)
            val openVideo = Intent(Intent.ACTION_VIEW)
            val uriForFile : Uri
            //N 以后文件共享更加严格 需要fileProvider
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                uriForFile = FileProvider.getUriForFile(baseContext, BuildConfig.APPLICATION_ID + ".fileProvider", path)
            }else{
                uriForFile = Uri.fromFile(path)
            }
            openVideo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            openVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openVideo.setDataAndType(uriForFile, "video/*")
            startActivity(openVideo)
        }
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
        return com.sd.mvc.intercept_video_link.R.layout.activity_file_video
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        RsPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}