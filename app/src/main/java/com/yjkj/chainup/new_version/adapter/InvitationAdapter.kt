package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2020-08-31-16:12
 * @Email buptjinlong@163.com
 * @description
 */
class InvitationAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_my_invitation), LoadMoreModule {


    var type = ParamConstant.MY_INVITATION

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var precision = NCoinManager.getCoinShowPrecision("USDT")
        item?.run {
            helper?.run {
                when (type) {

                    ParamConstant.MY_INVITATION -> {
                        setText(R.id.tv_type_1, optString("levelZeroRegisterUid"))
                        if (TextUtils.isEmpty(optString("mobileNumber"))) {
                            setText(R.id.tv_type_2, optString("email"))
                        } else {
                            setText(R.id.tv_type_2, optString("mobileNumber"))
                        }
                        setText(R.id.tv_type_3, DateUtil.longToString("yyyy/MM/dd", optString("registerTime", "0").toLong()))

                    }

                    ParamConstant.INVITE_REWARDS -> {
                        setText(R.id.tv_type_1, DateUtil.longToString("yyyy/MM/dd", optString("sendTime").toLong()))
                        setText(R.id.tv_type_2, optString("userAccountNum"))
                        setText(R.id.tv_type_3, BigDecimalUtils.divForDown(optString("conversionAmount"), precision).toPlainString())
                    }
                    else -> {

                    }
                }

            }


        }
    }

}
