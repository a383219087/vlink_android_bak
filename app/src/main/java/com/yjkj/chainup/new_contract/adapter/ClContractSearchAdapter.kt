package com.yjkj.chainup.new_contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.Contract
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import org.json.JSONObject

/**
 * 合约搜索
 */
class ClContractSearchAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cl_item_contract_search, data) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.let {
            val symbol = LogicContractSetting.getContractShowNameById(context, item?.optInt("id"))
            it.setText(R.id.tv_name, symbol)
        }
    }

}