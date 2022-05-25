package com.yjkj.chainup.wedegit

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.BitmapUtils
import com.yjkj.chainup.util.SystemUtils
import kotlinx.android.synthetic.main.new_share_view.view.*

/**
 * @Author lianshangljl
 * @Date 2020-09-29-17:40
 * @Email buptjinlong@163.com
 * @description
 */
class ShareImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.new_share_view, this, true)

        if (!TextUtils.isEmpty(UserDataService.getInstance()?.inviteUrl)) {
            iv_new_share_qr?.setImageBitmap(BitmapUtils.generateBitmap(UserDataService.getInstance()?.inviteUrl, 300, 300))
        }
        tv_share_account?.text = UserDataService.getInstance().userAccount
        tv_share_content?.text = String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name"))

        if (SystemUtils.isZh()) {
            rl_new_share_layout.setImageResource(R.drawable.ic_share_cn_one)
        }else{
            rl_new_share_layout.setImageResource(R.drawable.ic_share_en_one)
        }
    }

    fun setShareView(index:Bitmap){
        rl_new_share_layout?.setImageBitmap(index)
    }


}