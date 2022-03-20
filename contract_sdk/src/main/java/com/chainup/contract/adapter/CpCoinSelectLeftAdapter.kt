package com.chainup.contract.adapter

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpTabInfo

open class CpCoinSelectLeftAdapter(data: ArrayList<CpTabInfo>, var position: Int) : BaseQuickAdapter<CpTabInfo, BaseViewHolder>(R.layout.item_select_coins_left, data) {

    override fun convert(helper: BaseViewHolder, item: CpTabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (0 == item?.extrasNum) {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
            helper?.setBackgroundColor(R.id.tv_content,  ContextCompat.getColor(context, R.color.bg_card_color))
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
            helper?.setBackgroundColor(R.id.tv_content, ContextCompat.getColor(context, R.color.trade_search_radius_color))
        }
    }

}