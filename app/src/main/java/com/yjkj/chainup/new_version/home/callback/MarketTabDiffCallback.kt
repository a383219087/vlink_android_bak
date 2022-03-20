package com.yjkj.chainup.new_version.home.callback

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import org.json.JSONObject


class MarketTabDiffCallback(private val mOldEmployeeList: List<JSONObject>, private val mNewEmployeeList: List<JSONObject>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldEmployeeList.size
    }

    override fun getNewListSize(): Int {
        return mNewEmployeeList.size
    }

    fun getNewData(): List<JSONObject> {
        return mNewEmployeeList
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldEmployeeList[oldItemPosition].getString("symbol") == mNewEmployeeList[newItemPosition].getString("symbol")
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEmployee = mOldEmployeeList[oldItemPosition]
        val newEmployee = mNewEmployeeList[newItemPosition]
        val rose = oldEmployee.optString("rose","0")
        val roseNew = newEmployee.optString("rose","0")
        val price = oldEmployee.optString("close","0")
        val priceNew = newEmployee.optString("close","0")
        val isChange = rose == roseNew
                && price == priceNew
        return isChange
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}