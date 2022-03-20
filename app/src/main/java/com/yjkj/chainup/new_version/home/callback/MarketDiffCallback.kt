package com.yjkj.chainup.new_version.home.callback

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.yjkj.chainup.util.LogUtil
import org.json.JSONObject
import java.util.ArrayList
import android.os.Bundle


class MarketDiffCallback(private val mOldEmployeeList: ArrayList<JSONObject>, private val mNewEmployeeList: ArrayList<JSONObject>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldEmployeeList.size
    }

    override fun getNewListSize(): Int {
        return mNewEmployeeList.size
    }

    fun getNewData(): ArrayList<JSONObject> {
        return mNewEmployeeList
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldEmployeeList[oldItemPosition].getString("symbol") == mNewEmployeeList[newItemPosition].getString("symbol")
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEmployee = mOldEmployeeList[oldItemPosition]
        val newEmployee = mNewEmployeeList[newItemPosition]
        val rost = oldEmployee.getString("rose")
        val rostNew = oldEmployee.getString("rose")
        val price = oldEmployee.getString("close")
        val priceNew = newEmployee.getString("close")
        val index = oldEmployee.getString("homeIndex")
        val indexNew = newEmployee.getString("homeIndex")
        val isChange = rost == rostNew
                && price == priceNew && index == indexNew
//        LogUtil.e("NewHomeMarketAdapter", "== ${price}  ${priceNew}   ${rost} ${rostNew} = ${oldItemPosition} ${isChange}")
        return isChange
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
//        val oldEmployee = mOldEmployeeList[oldItemPosition]
//        val newEmployee = mNewEmployeeList[newItemPosition]
//        val payload = Bundle()
//        if (oldEmployee.getString("rose") != newEmployee.getString("rose")) {
//            payload.putString("rose", newEmployee.getString("rose"))
//        }
//        if (oldEmployee.getString("close") != newEmployee.getString("close")) {
//            payload.putString("close", newEmployee.getString("close"))
//        }
//        if (oldEmployee.getString("vol") != newEmployee.getString("vol")) {
//            payload.putString("vol", newEmployee.getString("vol"))
//        }
////        LogUtil.e("NewHomeMarketAdapter", "==NewHomeDetailFragment=payloads  ${payload.size()}= onBindViewHolder ")
//        if (payload.size() == 0) {
//            return null
//        }
//        return payload.size()
        return null
    }
}