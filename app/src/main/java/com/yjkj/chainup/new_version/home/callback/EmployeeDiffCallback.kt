package com.yjkj.chainup.new_version.home.callback

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import org.json.JSONObject


class EmployeeDiffCallback(private val mOldEmployeeList: List<JSONObject>, private val mNewEmployeeList: List<JSONObject>) : DiffUtil.Callback() {

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
        val rose = oldEmployee.getString("rose")
        val roseNew = oldEmployee.getString("rose")
        val price = oldEmployee.getString("close")
        val priceNew = newEmployee.getString("close")
        val index = oldEmployee.getString("homeIndex")
        val indexNew = newEmployee.getString("homeIndex")
        val vol = oldEmployee.getString("vol")
        val volNew = newEmployee.getString("vol")
        val isChange = rose == roseNew
                && price == priceNew && index == indexNew && vol == volNew
        return isChange
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}