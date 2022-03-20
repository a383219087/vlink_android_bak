package com.chainup.contract.view

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.chainup.contract.R
import com.chainup.contract.utils.CpSizeUtils
import kotlinx.android.synthetic.main.cp_up_down_item_layout.view.*

/**
 * 合约通用组件封装(上下结构)
 */
class CpContractUpDownItemLayout : LinearLayout {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            var typedArray = context.obtainStyledAttributes(it, R.styleable.SlUpDownItemStyle, 0, 0)
            title = typedArray.getString(R.styleable.SlUpDownItemStyle_sl_itemTitle) ?: ""
            contentGravity = typedArray.getInt(R.styleable.SlUpDownItemStyle_contentGravity, 1)
            showExplain = typedArray.getBoolean(R.styleable.SlUpDownItemStyle_showExplain, false)
            explainText = typedArray.getString(R.styleable.SlUpDownItemStyle_explainText) ?: ""
            contentTextColor = typedArray.getInt(R.styleable.SlUpDownItemStyle_contentGravity, ContextCompat.getColor(context, R.color.text_color))
            typedArray.recycle()
            if (showExplain) {
                setExplainListener(OnClickListener { CpNewDialogUtils.showDialog(context, explainText, true, null, context.getString(R.string.cp_extra_text27), context.getString(R.string.cp_extra_text28)) })
            }
        }
    }

    /**
     * 标题
     */
    var title = ""
        set(value) {
            field = value
            tv_title?.text = title
        }
    /**
     * 内容
     */
    var content = ""
        set(value) {
            field = value
            tv_value?.text = content
        }
    /**
     * 内容颜色
     */
    var contentTextColor = ContextCompat.getColor(context, R.color.text_color)
        set(value) {
            field = value
            tv_value?.setTextColor(value)
        }
    /**
     * 内容的排版方向   1 左边 2 右边
     */
    var contentGravity = 1
        set(value) {
            field = value
            ll_warp_layout?.gravity = if(contentGravity == 1) Gravity.LEFT else Gravity.RIGHT
        }
    /**
     * 是否展示说明
     */
    var showExplain = false
        set(value) {
            field = value
            if (showExplain) {
                tv_title?.compoundDrawablePadding = CpSizeUtils.dp2px(3f)
                tv_title?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.cp_contract_prompt), null)
            } else {
                tv_title?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }
    /**
     * 说明文本
     */
    var explainText = ""


    /**
     * 设置说明点击事件
     */
    fun setExplainListener(listener: OnClickListener?) {
        tv_title?.setOnClickListener(listener)
    }

    init {
        layoutInflater.inflate(R.layout.cp_up_down_item_layout, this)
    }

}