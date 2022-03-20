package com.yjkj.chainup.new_contract.adapter

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpNumberUtil
import org.json.JSONObject
import java.util.*

/**
 * Created by zj on 2018/3/7.
 */
class CpContractDropAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cp_item_contract_drop, data) {

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
        ticker?.let {
            val dfRate = CpNumberUtil().getDecimal(2)
            helper?.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, ticker.optInt("id")))
            if (!ticker.isNull("rose")) {
                val chg = CpBigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                //比例
                val tvContractChg = helper?.getView<TextView>(R.id.tv_contract_chg)
                tvContractChg?.run {
                    text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                    setTextColor( CpColorUtil.getMainColorType(chg >= 0))
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
            helper?.getView<RelativeLayout>(R.id.rl_content)?.setOnClickListener {
                CpClLogicContractSetting.setContractCurrentSelectedId(context, ticker.optInt("id"))
                var msgEvent =
                    CpMessageEvent(CpMessageEvent.sl_contract_left_coin_type)
                msgEvent.msg_content = ticker
                CpEventBusUtil.post(msgEvent)
            }
        }
    }


}