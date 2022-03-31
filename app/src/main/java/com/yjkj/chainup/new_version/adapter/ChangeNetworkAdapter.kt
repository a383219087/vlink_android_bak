package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.widget.RadioButton
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2020-06-18-14:21
 * @Email buptjinlong@163.com
 * @description
 */
class ChangeNetworkAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_change_network) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper.setText(R.id.tv_title, LanguageUtil.getString(context, "customSetting_action_host") + (helper.adapterPosition))
//        helper.setText(R.id.tv_title, item.optString("hostName"))

        if (!TextUtils.isEmpty(item.optString("error"))) {
            helper.setText(R.id.tv_content, LanguageUtil.getString(context, "customSetting_action_unusable"))
            helper.setTextColorRes(R.id.tv_content, R.color.red)
        } else {
            var color = R.color.text_color
            if (TextUtils.isEmpty(item.optString("networkAppapi"))) {
                helper.setText(R.id.tv_content, LanguageUtil.getString(context, "customSetting_action_testing"))
            } else {
                val time = item.optString("networkAppapi", "0")
                color = if (time.toInt() <= 300) {
                    R.color.main_green
                }else{
                    R.color.deposit_tip
                }
                helper.setText(R.id.tv_content, "${item.optString("networkAppapi", "--")}ms")
            }
            helper.setTextColorRes(R.id.tv_content, color)
        }
        if (!TextUtils.isEmpty(item.optString("error_ws"))) {
            helper.setText(R.id.tv_content_ws, LanguageUtil.getString(context, "customSetting_action_unusable"))
            helper.setTextColorRes(R.id.tv_content_ws, R.color.red)
        } else {
            var color = R.color.text_color
            if (TextUtils.isEmpty(item.optString("networkWs"))) {
                helper.setText(R.id.tv_content_ws, LanguageUtil.getString(context, "customSetting_action_testing"))
            } else {
                val time = item.optString("networkWs", "0")
                color = if (time.toInt() <= 300) {
                    R.color.main_green
                }else{
                    R.color.deposit_tip
                }
                helper.setText(R.id.tv_content_ws, "${item.optString("networkWs", "--")}ms")
            }
            helper.setTextColorRes(R.id.tv_content_ws, color)
        }
        val netWorkUrl = PublicInfoDataService.getInstance().newWorkURL
        if (TextUtils.isEmpty(netWorkUrl)) {
            Log.e("我是改变地址2", "serverUrl：${Utils.getAPIInsideString(ApiConstants.BASE_URL)}")
            PublicInfoDataService.getInstance().saveNewWorkURL(Utils.getAPIInsideString(ApiConstants.BASE_URL))
        }
        val isCheck = netWorkUrl == item.optString("hostName")
        helper.getView<RadioButton>(R.id.tv_api_rb).isChecked = isCheck

        val netWorkWS = PublicInfoDataService.getInstance().newWorkWSURL
        if (TextUtils.isEmpty(netWorkWS)) {
            PublicInfoDataService.getInstance().saveNewWorkWSURL(Utils.getAPIInsideString(ApiConstants.BASE_URL))
        }
        val wsUrl = (if (netWorkWS.isNotEmpty()) netWorkWS else netWorkUrl)
        val isCheckWs = wsUrl == item.optString("hostNameWs")
        LogUtil.i(TAG, "likesWSArray ${wsUrl} []  ${item.optString("hostNameWs")}")
        helper.getView<RadioButton>(R.id.tv_ws_rb).isChecked = isCheckWs


    }
}