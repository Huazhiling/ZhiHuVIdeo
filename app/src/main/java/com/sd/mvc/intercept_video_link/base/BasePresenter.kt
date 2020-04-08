package com.sd.mvc.intercept_video_link.base

abstract class BasePresenter<M, V> {
    protected var mModel: M? = null
    protected var mView: V? = null

    fun attachMVP(mView: V) {
        this.mModel = getModel()
        this.mView = mView
        onStar()
    }

    fun detachMVP() {
        this.mModel = null
        this.mView = null
    }

    /**
     * 可以再这里做一些数据初始化
     */
    abstract fun onStar()

    /**
     * 返回当前P想持有的M引用
     */
    abstract fun getModel(): M
}