package com.yjkj.chainup.new_version.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yjkj.chainup.R
import kotlinx.android.synthetic.main.item_two_way_textview.view.*

/**
 * @Author lianshangljl
 * @Date 2020-08-27-20:32
 * @Email buptjinlong@163.com
 * @description
 */
class TextViewtwoWayVerticalView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var titleView = ""
    var contentView = ""

    init {
        attrs.let {
            var typeArray = context.obtainStyledAttributes(it, R.styleable.TextViewtwoWayView, 0, 0)
            titleView = typeArray.getString(R.styleable.TextViewtwoWayView_titleView).toString()
            contentView = typeArray.getString(R.styleable.TextViewtwoWayView_contentView).toString()
        }
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_two_way_vertical_textview, this, true)
        tv_title?.text = titleView
        tv_content?.text = contentView
    }

    fun setTitleContent(content: String) {
        titleView = content
        tv_title?.text = titleView
    }

    fun setContentText(content: String) {
        contentView = content
        tv_content?.text = contentView
    }

    fun setContentColor(colorId: Int) {
        tv_content?.setTextColor(colorId)
    }

}