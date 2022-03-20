package com.yjkj.chainup.new_contract.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject

/**
 * 合约资产记录
 */
class ClContractAssetRecordAdapter(ctx: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cl_item_asset_record, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.run {
            //金额
            setText(R.id.tv_amount_value, BigDecimalUtils.showSNormal(item.optString("amount"), item.optInt("mMarginCoinPrecision")))
            //时间
            setText(R.id.tv_time_value, item.optString("ctime"))
            //流水类型 1 转入,2 转出,5 资金费用 ,8 分摊
            setText(R.id.tv_type_value, item.optString("type"))
            //symbol 名称
            setText(R.id.tv_symbol_name, item.optString("contractName"))
//            setText(R.id.tv_type_value, when (item.optString("type")) {
//                "1" -> "转入"
//                "2" -> "转出"
//                "5" -> "资金费用"
//                "8" -> "分摊"
//                else -> "error"
//            })
        }
    }
}