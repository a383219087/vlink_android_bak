package com.yjkj.chainup.new_contract.adapter

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpMarketDetail4Activity
import com.chainup.contract.utils.*
import org.json.JSONObject
import java.util.*

/**
 * Created by zj on 2018/3/7.
 */
class CpContractDropAdapter2(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cp_item_contract_drop2, data) {

    var currentSymbol = "e_btcusdt"
    var quote = ""
    var base = ""
    var mContractId = 1
    var contractType = "0"
    var symbolPricePrecision = 2

    class SpotViewHolder(itemView: View, type: Int) : RecyclerView.ViewHolder(itemView) {
        var rlContent: RelativeLayout
        var tvContractName: TextView
        var tvContractChg: TextView

        init {
            rlContent = itemView.findViewById(R.id.rl_content)
            tvContractName = itemView.findViewById(R.id.tv_contract_name)
            tvContractChg = itemView.findViewById(R.id.tv_contract_chg)
        }
    }

    override fun convert(helper: BaseViewHolder, ticker: JSONObject) {
        ticker.let {
            val dfRate = CpNumberUtil().getDecimal(2)
            helper?.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, ticker.optInt("id")))
            if (!ticker.isNull("rose")) {
                val chg = CpBigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                //比例
                val tvContractChg = helper?.getView<TextView>(R.id.tv_contract_chg)
                tvContractChg?.run {
                    text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
//                    setTextColor( CpColorUtil.getHomeColorType(chg >= 0))
                    setBackgroundColor(CpColorUtil.getHomeColorType(chg >= 0))
                }
            }
            if (!ticker.isNull("close")) {
                val chg = CpBigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                //比例
                val tvLastPrice = helper?.getView<TextView>(R.id.tv_last_price)
                tvLastPrice?.run {
                    text = ticker.optString("close")
                    setTextColor( CpColorUtil.getMainColorType(chg >= 0))
                }
            }
            helper?.getView<LinearLayout>(R.id.rl_content)?.setOnClickListener {
                mContractId = ticker.getInt("id")
                base = ticker.getString("base")
                quote = ticker.getString("quote")
                contractType = ticker.getString("contractType")
                currentSymbol = (ticker.getString("contractType") + "_" + ticker.getString("symbol")
                .replace("-", "")).toLowerCase()
                symbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context,mContractId)
                CpClLogicContractSetting.setContractCurrentSelectedId(context, ticker.optInt("id"))
                var msgEvent =
                    CpMessageEvent(CpMessageEvent.sl_contract_left_coin_type)
                msgEvent.msg_content = ticker
                CpEventBusUtil.post(msgEvent)
                startItem()
            }
        }
    }

    fun startItem(){
        if (!CpChainUtil.isFastClick()) {
            val mIntent = Intent(context, CpMarketDetail4Activity::class.java)
            mIntent.putExtra(CpParamConstant.symbol, currentSymbol)
            mIntent.putExtra("contractId", mContractId)
            mIntent.putExtra("baseSymbol",  base)
            mIntent.putExtra("quoteSymbol",quote)
            mIntent.putExtra("pricePrecision",symbolPricePrecision)
            mIntent.putExtra(CpParamConstant.TYPE, CpParamConstant.BIBI_INDEX)
            context.startActivity(mIntent)
        }
    }


}