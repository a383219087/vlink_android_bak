package com.yjkj.chainup.new_version.view

import android.app.Activity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import kotlinx.android.synthetic.main.item_select_coin_header.view.*

/**
 * @Author lianshangljl
 * @Date 2020/11/3-11:31 AM
 * @Email buptjinlong@163.com
 * @description
 */
class NewSelectCoinHeaderView @JvmOverloads constructor(
        context: Activity,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        initView(context)
    }

    fun initView(context: Activity) {
        LayoutInflater.from(context).inflate(R.layout.item_select_coin_header, this, true)
        mcv_layout?.setTitleContent(LanguageUtil.getString(context, "assets_popular_crypto"))
    }


    fun setHotCoinData(coins: ArrayList<String>) {
        mcv_layout?.setHotCoinView(coins)
    }

}