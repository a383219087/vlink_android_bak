package com.yjkj.chainup.new_contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpClLogicContractSetting
import org.json.JSONObject

/**
 * 合约搜索
 */
class CpContractSearchAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cp_item_contract_search, data) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.let {
            val symbol = CpClLogicContractSetting.getContractShowNameById(context, item?.optInt("id"))
            it.setText(R.id.tv_name, symbol)
        }
    }

}