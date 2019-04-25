package com.sd.mvc.intercept_video_link.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sd.mvc.intercept_video_link.bean.HistoryBean

/**
 * CREATE_VIDEO_TABLE 通过 CREATE_TABLE 的url来比对，如果相同的话那么就证明是CREATE_TABLE关联的
 * 如果CREATE_TABLE中不存在这个url，那说明没有添加过，就没必要查找了，直接添加数据即可
 */
class SQLiteHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    private val CREATE_TABLE = "CREATE TABLE $name (primary_key TEXT,title TEXT,time LONG)"
    private val CREATE_VIDEO_TABLE = "CREATE TABLE ${name}_video (primary_key TEXT,url TEXT,title TEXT,imageUrl TEXT,downloadUrl TEXT)"
    private val name = name
    private val fox_name = "${name}_video"


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
        db.execSQL(CREATE_VIDEO_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun insertNewData(primary: String, title: String, time: Long): Boolean {
        if (findVideoBasedOnUrl(primary) == null) {
            writableDatabase.execSQL("INSERT INTO $name (primary_key,title,time) VALUES ('$primary','$title','$time')")
            return false
        }
        writableDatabase.close()
        return true
    }

    fun findAllVideo(): ArrayList<HistoryBean>? {
        var cursor = writableDatabase.rawQuery("SELECT * FROM $name", null)
        var history = ArrayList<HistoryBean>()
        if (cursor.count > 0) {
            cursor.moveToFirst()
            for (i in 0 until cursor.count) {
                history.add(HistoryBean(cursor.getString(cursor.getColumnIndex("primary_key"))
                        ,cursor.getString(cursor.getColumnIndex("title"))
                        , cursor.getLong(cursor.getColumnIndex("time"))
                        , findVideoBasedOnThePrimaryKey(cursor.getString(cursor.getColumnIndex("primary_key")))))
                cursor.moveToNext()
            }
            writableDatabase.close()
            cursor.close()
            return history
        }
        writableDatabase.close()
        cursor.close()
        return null
    }

    /**
     * 只需要知道有没有就好
     */
    private fun findVideoBasedOnUrl(url: String): ArrayList<HistoryBean>? {
        var cursor = writableDatabase.rawQuery("SELECT * FROM $name WHERE primary_key='$url'", null)
        var history = ArrayList<HistoryBean>()
        if (cursor.count > 0) {
            cursor.close()
            return history
        }
        cursor.close()
        return null
    }

    fun findVideoBasedOnThePrimaryKey(url: String): ArrayList<HistoryBean.DataBean>? {
        LogUtils.e("SELECT * FROM $fox_name WHERE primary_key='$url'")
        var cursor = writableDatabase.rawQuery("SELECT * FROM $fox_name WHERE primary_key='$url'", null)
        var historyChild = ArrayList<HistoryBean.DataBean>()
        if (cursor.count > 0) {
            cursor.moveToFirst()
            for (i in 0 until cursor.count) {
                historyChild.add(HistoryBean.DataBean(
                        cursor.getString(cursor.getColumnIndex("url"))
                        , cursor.getString(cursor.getColumnIndex("title"))
                        , cursor.getString(cursor.getColumnIndex("imageUrl"))
                        , cursor.getString(cursor.getColumnIndex("downloadUrl"))))
                cursor.moveToNext()
            }
            cursor.close()
            return historyChild
        }
        cursor.close()
        return null
    }

    fun insertFoxNewData(primary_key: String, url: String, title: String, imageUrl: String, downloadUrl: String) {
        writableDatabase.execSQL("INSERT INTO $fox_name (primary_key,url,title,imageUrl,downloadUrl) VALUES ('$primary_key','$url','$title','$imageUrl','$downloadUrl')")
        writableDatabase.close()
    }
}
