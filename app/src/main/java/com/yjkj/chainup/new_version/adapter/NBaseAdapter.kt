package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-09-09-10:52
 * @Description:
 */
open class NBaseAdapter(data: ArrayList<JSONObject>, layoutId:Int): BaseQuickAdapter<JSONObject, BaseViewHolder>(layoutId,data){
    val TAG = this::class.java.simpleName
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
    }
}
