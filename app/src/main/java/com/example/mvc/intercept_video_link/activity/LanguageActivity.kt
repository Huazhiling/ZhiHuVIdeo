package com.example.mvc.intercept_video_link.activity

import com.example.mvc.intercept_video_link.R
import com.example.mvc.intercept_video_link.common.Constant.Language.CHINESE
import com.example.mvc.intercept_video_link.common.Constant.Language.ENGLISH
import com.example.mvc.intercept_video_link.event.LanguageEvent
import com.example.mvc.intercept_video_link.utils.LanguageUtils
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_language.*
import org.greenrobot.eventbus.EventBus

class LanguageActivity : BaseActivity() {
    lateinit var default_language: String
    override fun getLayoutId(): Int {
        return R.layout.activity_language
    }

    override fun initData() {
        switch_china.setOnSuperTextViewClickListener {
            LanguageUtils.changeLocale(CHINESE, resources.configuration, baseContext,application)
            switch_china.setRightIcon(R.drawable.language_selected_icon)
            switch_english.setRightIcon(R.drawable.language_unselected_icon)
            recreate()
            EventBus.getDefault().post(LanguageEvent())
        }
        switch_english.setOnSuperTextViewClickListener {
            LanguageUtils.changeLocale(ENGLISH, resources.configuration, baseContext,application)
            switch_english.setRightIcon(R.drawable.language_selected_icon)
            switch_china.setRightIcon(R.drawable.language_unselected_icon)
            recreate()
            EventBus.getDefault().post(LanguageEvent())
        }
        language_back.setOnClickListener {
            finish()
        }
//        baseContext.createConfigurationContext()
    }

    override fun initView() {
        ImmersionBar.with(this).statusBarView(R.id.status_bar).statusBarDarkFont(true).init()
        default_language = LanguageUtils.getUserSetLocal(application)
        if (default_language == CHINESE) {
            switch_china.setRightIcon(R.drawable.language_selected_icon)
            switch_english.setRightIcon(R.drawable.language_unselected_icon)
        } else if (default_language == ENGLISH) {
            switch_english.setRightIcon(R.drawable.language_selected_icon)
            switch_china.setRightIcon(R.drawable.language_unselected_icon)
        }
    }
}
