package com.yjkj.chainup.new_version.adapter.trade

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.ColorUtil
import org.jetbrains.anko.textColor
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019-09-26-11:02
 * @Description:
 */
class NSearchCoinAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_coin) {

    var isSelfData = ""
    public fun setParams(symbol: String) {
        this.isSelfData = symbol
    }


    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        val market = helper.getView<TextView>(R.id.tv_coin_name)
        val layoutCoin = helper.getView<ConstraintLayout>(R.id.layout_coin)
        val name = NCoinManager.showAnoterName(item)
        market.text = name
        if (isSelfData == item.optString("symbol")) {
            market.textColor = ColorUtil.getColor(context,R.color.trade_main_blue)
        } else {
            market.textColor = ColorUtil.getColor(context,R.color.text_color)
        }
    }
}