package com.sd.mvc.intercept_video_link.base;

import android.os.Bundle;

import org.jetbrains.annotations.Nullable;

public abstract class BaseMVPActivity<P extends BasePresenter> extends BaseActivity implements IBaseView {
    protected P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = (P) initPresenter();
        if (mPresenter != null) {
            mPresenter.attachMVP(this);
        }
    }
}
