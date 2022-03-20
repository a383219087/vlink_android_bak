package com.yjkj.chainup.new_version.view.depth.btn

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.item_price_type_button.view.*

/**
 * @Author lianshangljl
 * @Date 2019/3/7-2:14 PM
 * @Email buptjinlong@163.com
 * @description
 */
class PriceTypeButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {



    var textContent = ""
        set(value) {
            field = value
            tv_trade_order_type?.text = value
            LogUtil.e("LogUtils","textContent ${tv_trade_order_type}")
        }

    init {
        attrs.let {

        }
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_price_type_button, this, true)
    }

    fun setContent(content: String) {
        tv_trade_order_type?.text = content
    }

}