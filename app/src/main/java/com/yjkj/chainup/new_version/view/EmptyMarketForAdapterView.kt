package com.yjkj.chainup.new_version.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import kotlinx.android.synthetic.main.layout_market_destail_empty_view.view.*

/**
 * @Author lianshangljl
 * @Date 2020-03-30-12:44
 * @Email buptjinlong@163.com
 * @description
 */
class EmptyMarketForAdapterView @JvmOverloads constructor(context: Context,
                                                          attrs: AttributeSet? = null,
                                                          defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {


    init {
        initView(context)
    }


    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_market_destail_empty_view, this, true)
        tv_add_like?.text = LanguageUtil.getString(context, "market_text_customZoneAdd")
    }


}