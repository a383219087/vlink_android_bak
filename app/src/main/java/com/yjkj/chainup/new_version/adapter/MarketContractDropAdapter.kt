package com.yjkj.chainup.new_version.adapter

import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.app.CpParamConstant
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.chainup.contract.ui.activity.CpMarketDetail4Activity
import com.chainup.contract.utils.*
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.util.ColorUtil
import org.json.JSONObject

/**
 * Created by zj on 2018/3/7.
 */
open class MarketContractDropAdapter(layoutRes: Int, data: ArrayList<JSONObject>) :
    BaseQuickAdapter<JSONObject, BaseViewHolder>(layoutRes, data) {

    constructor(data: ArrayList<JSONObject>) : this(R.layout.item_contract_drop, data)

    class SpotViewHolder(itemView: View, type: Int) : RecyclerView.ViewHolder(itemView) {
        var rlContent: RelativeLayout
        var tvContractName: TextView
        var tvContractChg: SuperTextView

        init {
            rlContent = itemView.findViewById(R.id.rl_content)
            tvContractName = itemView.findViewById(R.id.tv_contract_name)
            tvContractChg = itemView.findViewById(R.id.tv_contract_chg)
        }
    }

    /**
    //    {"id":1,"contractName":"E-BTC-USDT","symbol":"BTC-USDT","contractType":"E","coType":"E",
    //        "contractShowType":"永续合约","deliveryKind":"0","contractSide":1,"multiplier":1.0E-4,
    //        "multiplierCoin":"BTC","marginCoin":"USDT","marginRate":1,"capitalStartTime":0,
    //        "capitalFrequency":1,"settlementFrequency":1,"brokerId":1,"base":"BTC","quote":"USDT",
    //        "coinResultVo":{"symbolPricePrecision":2,"depth":["2","1","0"],"minOrderVolume":1,
    //        "minOrderMoney":1,"maxMarketVolume":5000000,"maxMarketMoney":10000000,"maxLimitVolume":
    //        10000000,"maxLimitMoney":10000000,"priceRange":1,"marginCoinPrecision":6,"fundsInStatus":1,
    //        "fundsOutStatus":1},"sort":1,"maxLever":125,"minLever":1,"amount":"20446099371635.43","close":
    //        "20089.6","high":"20176.37","low":"19336.2","open":"19952.8","rose":"0.0068561806","vol":"1032478256"}
     */
    override fun convert(helper: BaseViewHolder, ticker: JSONObject) {
        ticker.let {
            val dfRate = CpNumberUtil().getDecimal(2)
            helper.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, ticker.optInt("id")).replace("-","")+context.getString(R.string.contract_type))
            if (!ticker.isNull("rose")) {
                val chg = CpBigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                //比例
                val tvContractChg = helper.getView<TextView>(R.id.tv_contract_chg)
                tvContractChg.run {
                    text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                    helper.getView<SuperTextView>(R.id.tv_contract_chg).solid =  getMainColorV2Type( ColorUtil.getColorType(),chg)
                }
            }
            if (!ticker.isNull("close")) {
                val chg = CpBigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                val tvLastPrice = helper.getView<TextView>(R.id.tv_last_price)
                tvLastPrice.run {
                    text = ticker.optString("close")
                    setTextColor(getMainColorV2Type( ColorUtil.getColorType(),chg))
//                    helper.getView<SuperTextView>(R.id.tv_contract_chg).solid =  getMainColorV2Type( ColorUtil.getColorType(),chg)
                }
            }
            helper.getView<RelativeLayout>(R.id.rl_content).setOnClickListener {
                if (!CpChainUtil.isFastClick()) {
                    val  mContractId = ticker.optInt("id")
                    val   currentSymbol = (ticker.getString("contractType") + "_" + ticker.getString("symbol")
                        .replace("-", "")).toLowerCase()
                    val mIntent = Intent(context, CpMarketDetail4Activity::class.java)
                    mIntent.putExtra(CpParamConstant.symbol, currentSymbol)
                    mIntent.putExtra("contractId", mContractId)
                    mIntent.putExtra("baseSymbol",  ticker.optString("base"))
                    mIntent.putExtra("quoteSymbol",ticker.optString("quote") )
                    mIntent.putExtra("pricePrecision",  CpClLogicContractSetting.getContractSymbolPricePrecisionById(context,mContractId))
                    mIntent.putExtra(CpParamConstant.TYPE, CpParamConstant.BIBI_INDEX)
                    context.startActivity(mIntent)
                }
            }
        }
    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorV2Type(colorSelect: Int, isRise: Double = 0.0): Int {
        val mainGreen =
            ColorUtil.getColor(R.color.main_green)
        val mainRed = ColorUtil.getColor(R.color.main_red)
        val mainZero = ColorUtil.getColorByMode(R.color.main_zero_day)
        return if (colorSelect == ColorUtil.GREEN_RISE) {
            if (isRise > 0) {
                mainGreen
            } else if (isRise < 0) {
                mainRed
            } else {
                mainZero
            }
        } else {
            if (isRise > 0) {
                mainRed
            } else if (isRise < 0) {
                mainGreen
            } else {
                mainZero
            }
        }
    }

    fun setDiffData(diffCallback: MarketTabDiffCallback) {
        if (emptyLayout!=null &&  emptyLayout?.childCount == 1) {
            setList(diffCallback.getNewData())
            return
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)
        data = diffCallback.getNewData() as java.util.ArrayList<JSONObject>
        diffResult.dispatchUpdatesTo(this)
    }
}