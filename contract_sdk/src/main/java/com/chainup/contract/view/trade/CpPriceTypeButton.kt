package com.chainup.contract.view.trade

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.chainup.contract.R
import com.chainup.contract.utils.ChainUpLogUtil
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_new.*
import kotlinx.android.synthetic.main.cp_item_price_type_button.view.*

/**
 * @Author lianshangljl
 * @Date 2019/3/7-2:14 PM
 * @Email buptjinlong@163.com
 * @description
 */
class CpPriceTypeButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {



    var textContent = ""
        set(value) {
            field = value
            tv_trade_order_type?.text = value
            ChainUpLogUtil.e("LogUtils","textContent ${tv_trade_order_type}")
        }

    init {
        attrs.let {

        }
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.cp_item_price_type_button, this, true)
    }

    fun setContent(content: String) {
        tv_trade_order_type?.text = content
    }

    fun stratAnim() {
        tv_tag.animate().setDuration(200).rotation(180f).start()
    }

    fun stopAnim() {
        tv_tag.animate().setDuration(200).rotation(0f).start()
    }

}