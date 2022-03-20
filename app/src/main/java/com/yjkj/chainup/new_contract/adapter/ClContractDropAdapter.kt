package com.yjkj.chainup.new_contract.adapter

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.db.constant.TradeTypeEnum
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.util.*

/**
 * Created by zj on 2018/3/7.
 */
class ClContractDropAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.sl_item_contract_drop, data) {

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
            val dfRate = NumberUtil.getDecimal(2)
            helper?.setText(R.id.tv_contract_name, LogicContractSetting.getContractShowNameById(context, ticker.optInt("id")))
            if (!ticker.isNull("rose")) {
                val chg = BigDecimalUtils.mul(ticker.optString("rose"), "100", 2).toDouble()
                //比例
                val tvContractChg = helper?.getView<TextView>(R.id.tv_contract_chg)
                tvContractChg?.run {
                    text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                    textColor = ColorUtil.getMainColorType(chg >= 0)
                }
            }
            helper?.getView<RelativeLayout>(R.id.rl_content)?.setOnClickListener {
                LogicContractSetting.setContractCurrentSelectedId(context, ticker.optInt("id"))
                var msgEvent = MessageEvent(MessageEvent.sl_contract_left_coin_type)
                msgEvent.msg_content = ticker
                EventBusUtil.post(msgEvent)
            }
        }
    }


}