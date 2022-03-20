package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.NumberUtil
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.home.callback.ContractDiffCallback
import com.yjkj.chainup.new_version.home.callback.EmployeeDiffCallback
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.new_version.view.CustomTagView
import com.yjkj.chainup.util.*
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2018/12/10-2:55 PM
 * @Description:
 */

class MarketSLDetailAdapter : BaseQuickAdapter<ContractTicker, BaseViewHolder>(R.layout.item_market_detail_new) {


    val TAG = MarketSLDetailAdapter::class.java.simpleName
    var isMarketLike = false
    var isMarketSort = false
    override fun convert(helper: BaseViewHolder, item: ContractTicker) {


        helper.setGoneV3(R.id.ll_main_area, false)
        item.let {
            val contract = ContractPublicDataAgent.getContract(it.instrument_id) ?: return

            val dfRate = NumberUtil.getDecimal(2)
            //合约名
            val name = item.symbolFormat
            helper.setText(R.id.tv_coin_name, name)
            val chg = it.getChangeRate()
            //比例
            val tvContractChg = helper.getView<SuperTextView>(R.id.tv_rose)
            tvContractChg?.run {
                text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                solid = ColorUtil.getMainColorType(chg >= 0)
            }
            helper.setGoneV3(R.id.ctv_content, false)

            helper.setText(R.id.tv_volume, it.get24Vol(contract))
            helper.setText(R.id.tv_close_price, it.getClosePrice(contract))
            helper.setText(R.id.tv_close_price_rmb, it.getUsdPrice(contract))
        }
    }

    fun setDiffData(diffCallback: ContractDiffCallback) {
        if (emptyLayout != null && emptyLayout?.childCount == 1) {
            setList(diffCallback.getNewData())
            return
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)
        diffResult.dispatchUpdatesTo(this)
        data = diffCallback.getNewData() as ArrayList<ContractTicker>
    }
}

