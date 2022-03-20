package com.chainup.contract.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.chainup.contract.R
import kotlinx.android.synthetic.main.cp_view_contract_entrust_tab_layout.view.*


/**
 * 合约委托tab切换widget
 */
class CpContractEntrustTabWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var tabListener: ContractEntrustTabListener? = null

    init {
        val view = layoutInflater.inflate(R.layout.cp_view_contract_entrust_tab_layout, this)

        tv_current_entrust.setText(R.string.cp_order_text2)
        tv_history_entrust.setText(R.string.cp_order_text72)

        //当前委托
        tv_current_entrust.setOnClickListener {
            doSwitchTab(0)
        }
        //历史委托
        tv_history_entrust.setOnClickListener {
            doSwitchTab(1)
        }

        doSwitchTab(0)
    }

    private fun doSwitchTab(index : Int = 0){
        tabListener?.onTab(index)
        if(index == 0){
//            tv_current_entrust.animate().scaleX(1.5f).scaleY(1.5f).setDuration(300).start()
//            tv_history_entrust.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start()
            tv_current_entrust.setTextColor(resources.getColor(R.color.text_color))
            tv_history_entrust.setTextColor(resources.getColor(R.color.normal_text_color))
            tv_current_entrust.setTextSize(TypedValue.COMPLEX_UNIT_SP,28f)
            tv_history_entrust.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
            tv_current_entrust.paint.isFakeBoldText = true
            tv_history_entrust.paint.isFakeBoldText = false
        }else{
//            tv_history_entrust.animate().scaleX(1.5f).scaleY(1.5f).setDuration(300).start()
//            tv_current_entrust.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start()
            tv_history_entrust.setTextColor(resources.getColor(R.color.text_color))
            tv_current_entrust.setTextColor(resources.getColor(R.color.normal_text_color))
            tv_current_entrust.paint.isFakeBoldText = false
            tv_history_entrust.paint.isFakeBoldText = true
            tv_current_entrust.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
            tv_history_entrust.setTextSize(TypedValue.COMPLEX_UNIT_SP,28f)
        }
    }

    fun bindTabListener(listener: ContractEntrustTabListener) {
        this.tabListener = listener
    }


    interface ContractEntrustTabListener {
        fun onTab(index: Int)
    }
}