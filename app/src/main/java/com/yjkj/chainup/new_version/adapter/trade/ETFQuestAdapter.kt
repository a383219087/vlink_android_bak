package com.yjkj.chainup.new_version.adapter.trade

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.NBaseAdapter
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-11-13-17:27
 * @Description:借贷记录详情
 */
class ETFQuestAdapter(data: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_levert_question_adapter, data) {
    var select = -1
    var success = -1
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.tv_symbol, item.trim())
        val isShow = select != -1 && helper.adapterPosition == select
        helper.setVisible(R.id.tv_status, isShow)
        if (isShow) {
            val isSelect = helper.adapterPosition == success
            helper.setImageResource(R.id.tv_status, when(isSelect){
                true -> R.mipmap.etf_correct
                false -> R.mipmap.etf_error
            })
            helper.setBackgroundResource(R.id.tv_symbol, when(isSelect){
                true -> R.drawable.market_etf_hot_green_bg
                false -> R.drawable.market_etf_hot_red_bg
            })
        }else{
            helper.setBackgroundResource(R.id.tv_symbol,R.drawable.market_etf_hot_bg)
        }
    }
}