package com.chainup.contract.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.chainup.contract.R
import com.yjkj.chainup.manager.CpLanguageUtil
import kotlinx.android.synthetic.main.cp_item_new_empty.view.*

/**
 * @Author lianshangljl
 * @Date 2020-03-30-12:44
 * @Email buptjinlong@163.com
 * @description
 */
class CpSearchCoinEmptyForAdapterView @JvmOverloads constructor(context: Context,
                                                                attrs: AttributeSet? = null,
                                                                defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {


    init {
        initView(context)
    }


    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.cp_item_new_empty, this, true)
        tv_empty_title?.text = context.getString(R.string.cp_overview_text60)
    }


}