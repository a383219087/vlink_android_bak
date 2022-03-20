package com.yjkj.chainup.new_version.adapter

import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2020/11/3-3:54 PM
 * @Email buptjinlong@163.com
 * @description
 */
class HotSelectCoinAdapter (data: ArrayList<String>?) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_new_screening_label, data) {


    override fun convert(helper: BaseViewHolder, item: String) {
        helper?.setText(R.id.tv_parent_content, item)
    }

}