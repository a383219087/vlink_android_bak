package com.yjkj.chainup.new_version.activity.oldgrid.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.adapter.NBaseAdapter
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2021/2/3-5:02 PM
 * @Email buptjinlong@163.com
 * @description
 */
class OldBeingPerformedAdapter (data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_being_performed_adapter) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item.run {


        }
    }
}