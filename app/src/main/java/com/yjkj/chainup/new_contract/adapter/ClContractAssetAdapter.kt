package com.yjkj.chainup.new_contract.adapter

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_contract.activity.ClCoinDetailActivity
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.Utils
import org.json.JSONObject

class ClContractAssetAdapter(context: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cl_item_contract_asset, data) {

    var accountEquity = ""
    var walletBalance = ""
    var marginBalance = ""


    init {
        //账户权益
        accountEquity = context.getLineText("contract_assets_account_equity")
        //钱包余额
        walletBalance = context.getLineText("sl_str_wallet_balance")
        //保证金余额
        marginBalance = context.getLineText("sl_str_margin_balance")
    }


    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.let { it ->
            //币种名称
            helper?.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(it.optString("symbol")))
            var isShowAssets = UserDataService.getInstance().isShowAssets

//            账户权益 可用资产
            Utils.assetsHideShow(isShowAssets, helper?.getView(R.id.tv_normal_balance),BigDecimalUtils.divForDown(it?.optString("canUseAmount"), NCoinManager.getCoinShowPrecision(it?.optString("symbol")
                    ?: "")).toPlainString())
//            总资产
            Utils.assetsHideShow(isShowAssets, helper?.getView(R.id.tv_margin_balance_value),  BigDecimalUtils.divForDown(it?.optString("totalAmount"), NCoinManager.getCoinShowPrecision(it?.optString("symbol")
                    ?: "")).toPlainString())



            //跳转到币种详情
            helper?.getView<LinearLayout>(R.id.rl_header_layout)?.setOnClickListener {
                ClCoinDetailActivity.show(context as Activity, item.optString("symbol"))
            }
        }
    }
}
