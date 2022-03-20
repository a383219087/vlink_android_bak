package com.yjkj.chainup.new_version.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019-10-24-17:03
 * @Description:
 */
class B2CBankListAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_bank_list) {
    var id:String = ""

    fun setSelectedID(bankId:String){
        id = bankId
    }

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            helper?.run {
                setText(R.id.tv_bank, optString("accountName"))
                helper.getView<TextView>(R.id.tv_bank).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (id == optString("bankNo")) R.drawable.ic_coin_selected else 0, 0)
            }

        }
    }
}