package com.yjkj.chainup.new_version.adapter

import android.view.View
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-10-23-11:37
 * @Description:提现账户列表
 */
class B2CWithdrawAccountAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_withdraw_account) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            helper?.run {
                setText(R.id.tv_bank, optString("bankName"))
                setText(R.id.tv_name, optString("name"))
                setText(R.id.tv_bank_account, optString("cardNo"))
                addChildClickViewIds(R.id.cl_main,R.id.btn_edit)
            }
        }

    }
}



