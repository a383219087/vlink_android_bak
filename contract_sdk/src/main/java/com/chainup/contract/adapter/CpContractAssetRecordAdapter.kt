package com.yjkj.chainup.new_contract.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils

import org.json.JSONObject

/**
 * 合约资产记录
 */
class CpContractAssetRecordAdapter(ctx: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(
    R.layout.cp_item_asset_record, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.run {
            //金额
            setText(R.id.tv_amount_value, CpBigDecimalUtils.showSNormal(item.optString("amount"), item.optInt("mMarginCoinPrecision")))
            //时间
            setText(R.id.tv_time_value, item.optString("ctime"))
            //流水类型 1 转入,2 转出,5 资金费用 ,8 分摊
            setText(R.id.tv_type_value, item.optString("type"))
            //symbol 名称
            setText(R.id.tv_symbol_name, item.optString("contractName"))
        }
    }
}