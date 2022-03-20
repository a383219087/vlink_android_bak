package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import android.text.TextUtils
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.BitmapUtils
import com.yjkj.chainup.util.GlideUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-09-01-12:02
 * @Email buptjinlong@163.com
 * @description
 */
class PostersRadioAdapter(var activity: Activity,data: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_posters_radio_btn, data) {

    override fun convert(helper: BaseViewHolder, item: String) {


        helper?.setText(R.id.tv_iphone, UserDataService.getInstance().userAccount)


        helper?.setText(R.id.tv_invitation_content, LanguageUtil.getString(context, "邀请您加入${LanguageUtil.getString(context, "app_name")}，长按识别二维码"))

        GlideUtils.loadImage(activity, item, helper?.getView(R.id.iv_share_image))

        /**
         * 二维码图片
         */
        if (!TextUtils.isEmpty(UserDataService.getInstance()?.inviteUrl)) {
            helper?.getView<ImageView>(R.id.tv_invitation_qr_code)?.setImageBitmap(BitmapUtils.generateBitmap(UserDataService.getInstance()?.inviteUrl, 300, 300))
        }


    }

}