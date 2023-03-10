package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.ClCoinDetailActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.treaty.dialog.ContractDialog
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LanguageUtil
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
        item.let {
            //币种名称
            helper.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(it.optString("symbol")))
            val isShowAssets = UserDataService.getInstance().isShowAssets


            val coin = PublicInfoDataService.getInstance().getCoinByName( NCoinManager.getShowMarket(it?.optString("symbol")))
            //币种logo
            Glide.with(context).load(coin?.getString("icon")).into(helper.getView(R.id.img))

      //            账户权益 可用资产
            Utils.assetsHideShow(isShowAssets,
                helper.getView(R.id.tv_normal_balance),BigDecimalUtils.divForDown(
                    it.optString("canUseAmount"), NCoinManager.getCoinShowPrecision(
                        it.optString("symbol")
                ?: "")).toPlainString())
      //            未实现盈亏
            Utils.assetsHideShow(isShowAssets,
                helper.getView(R.id.tv_margin_balance_value),  BigDecimalUtils.divForDown(
                    it.optString("openRealizedAmount"), NCoinManager.getCoinShowPrecision(it?.optString("symbol")
                ?: "")).toPlainString())
      //            钱包余额
            Utils.assetsHideShow(isShowAssets,
                helper.getView(R.id.tv_available_value),  BigDecimalUtils.divForDown(
                    it.optString("totalAmount"), NCoinManager.getCoinShowPrecision(it?.optString("symbol")
                ?: "")).toPlainString())
      //            保证金余额
            val  sumHoldAmount =  it.optString("sumHoldAmount").toString().toDouble()
//            val  totalMargin =  it.optString("totalMargin").toString().toDouble()
//            val  lockAmount =  it.optString("lockAmount").toString().toDouble()
            Utils.assetsHideShow(isShowAssets,
                helper.getView(R.id.tv_normal_balance1),  BigDecimalUtils.divForDown(
                    (sumHoldAmount).toString(), NCoinManager.getCoinShowPrecision(it?.optString("symbol")
                ?: "")).toPlainString())


            //跳转到币种详情
            helper.getView<LinearLayout>(R.id.rl_header_layout).setOnClickListener {
                ClCoinDetailActivity.show(context as Activity, item.optString("symbol"))
            }
            helper.getView<ImageView>(R.id.img_small_assets_tip).setOnClickListener {
                ContractDialog.showDialog4AvailableBalance(context)


            }
        }
    }
}
