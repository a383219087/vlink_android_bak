package com.yjkj.chainup.contract.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.InsuranceFund
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.extension.showMarginName
import com.yjkj.chainup.util.DateUtils

/**
 * 保险基金
 */
class InsuranceFundAdapter(data: ArrayList<InsuranceFund>) : BaseQuickAdapter<InsuranceFund, BaseViewHolder>(R.layout.sl_item_insturance_funding_layout, data) {
    override fun convert(helper: BaseViewHolder, item: InsuranceFund) {
        helper?.run {
            val contract: Contract = ContractPublicDataAgent.getContract(item.instrument_id)
                    ?: return

            val vol: Double = MathHelper.round(item.vol, contract.value_index)
            getView<TextView>(R.id.tv_remain_value).text = NumberUtil.getDecimal(-1).format(vol) +" " + contract.showMarginName()

            getView<TextView>(R.id.tv_time_value).text = DateUtils.longToString(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND,item.timestamp)
        }
    }


}