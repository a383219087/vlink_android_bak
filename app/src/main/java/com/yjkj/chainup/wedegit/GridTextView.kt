package com.yjkj.chainup.wedegit

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.util.ColorUtil
import kotlinx.android.synthetic.main.item_two_way_textview.view.*

/**
 * @Author lianshangljl
 * @Date 2021/1/29-10:35 AM
 * @Email buptjinlong@163.com
 * @description
 */
class GridTextView @JvmOverloads constructor(
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
        LayoutInflater.from(context).inflate(R.layout.item_two_way_textview, this, true)
        tv_title?.text = titleView
        tv_content?.text = contentView
        tv_title?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        tv_content?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    }


    fun setTitleContent(content: String) {
        titleView = content
        tv_title?.text = titleView
    }

    fun setContentSize() {
        tv_content?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    }

    fun setContentText(content: String, isFormat: Boolean = false, isSplit: Boolean = false) {
        val show = content
        tv_content?.setTextColor(ColorUtil.getMainGridResType(show))
        contentView = show
        val corner = ColorUtil.getMainGridResTypeCorner(show)
        val line = if (isSplit) " " else ""
        if (isFormat) {
            contentView = "$contentView$line%"
        }
        tv_content?.text = "${corner}${contentView}"
    }

    fun setContentText(content: String) {
        if (content.contains("-")) {
            tv_content?.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            tv_content?.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
        contentView = content
        tv_content?.text = contentView
    }

    fun setContentTextInterval(content: String) {
        tv_content?.text = content
    }

}