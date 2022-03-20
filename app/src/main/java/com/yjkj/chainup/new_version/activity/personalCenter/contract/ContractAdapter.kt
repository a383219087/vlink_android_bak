package com.yjkj.chainup.new_version.activity.personalCenter.contract

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.ContractBean
import java.util.ArrayList


/**
 * Created by Bertking on 2018/9/14.
 */

class ContractAdapter(datas: ArrayList<ContractBean>) : BaseQuickAdapter<ContractBean, BaseViewHolder>(R.layout.item_contract_item, datas) {
    var status: String = "1"
    override fun convert(helper: BaseViewHolder, item: ContractBean) {
        helper.setText(R.id.item_change_payment_name, item.title)
        helper.setVisible(R.id.item_change_payment_status, status == item.type)
    }

}