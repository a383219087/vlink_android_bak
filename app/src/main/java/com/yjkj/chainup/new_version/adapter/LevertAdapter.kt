package com.yjkj.chainup.new_version.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.databinding.ItemRuleLevertBinding
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.DateUtils
import org.json.JSONObject
import java.util.*

/**
 * Created by Bertking on 2018/7/14.
 */

open class LevertAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseDataNewBindingHolder<ItemRuleLevertBinding>>(R.layout.item_rule_levert, data) {
    var isBlack: Boolean = false
    override fun convert(helper: BaseDataNewBindingHolder<ItemRuleLevertBinding>, item: JSONObject) {

        // 获取 Binding
        val binding: ItemRuleLevertBinding? = helper.dataBinding
        binding?.isblack = isBlack
        val time = DateUtils.getYearMonthDayHourMinSecond(item.optLong("adjustTime"))
        helper.setText(R.id.tv_time, "$time ${getRuleType(item.optInt("type"))}")
        helper.setText(R.id.tv_new_value, item.optString("beforeContractValue", "--") + item.optString("quote"))
        helper.setText(R.id.tv_old_value, item.optString("afterContractValue", "--") + item.optString("quote"))

        helper.setText(R.id.tv_tran_value, item.optString("beforeLever", "--") + item.optString("quote"))
        val rate = LanguageUtil.getString(context, "etf_notes_manual_lever_time").format(item.optString("afterLever", "--"))
        helper.setText(R.id.tv_tran_end_value, rate)

    }

    private fun getRuleType(type: Int): String {
        return LanguageUtil.getString(context, when (type) {
            0 -> "market_tab_etf_type_auto_no"
            1 -> "market_tab_etf_type_auto"
            else -> "tran"
        })
    }
}

open class BaseDataNewBindingHolder<BD : ViewDataBinding>(view: View) : BaseViewHolder(view) {

    val dataBinding = DataBindingUtil.bind<BD>(view)
}
