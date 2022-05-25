package com.yjkj.chainup.new_version.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import kotlinx.android.synthetic.main.item_new_empty.view.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

/**
 * @Author lianshangljl
 * @Date 2020-03-30-12:44
 * @Email buptjinlong@163.com
 * @description
 */
class EmptyForAdapterView @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null,
                                                    defStyleAttr: Int = 0,
                                                    isblack: Boolean = false) : LinearLayout(context, attrs, defStyleAttr) {


    var black:Boolean = false
    init {
        black = isblack
        initView(context)

    }


    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_new_empty, this, true)
        tv_empty_title?.text = LanguageUtil.getString(context, "common_tip_nodata")
        if (black){
            im_nodata.imageResource = R.mipmap.quotes_norecords_night
            tv_empty_title.textColor = ColorUtil.getColor(R.color.normal_text_color_kline_night)
        }
    }


}