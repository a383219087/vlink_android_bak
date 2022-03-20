package com.yjkj.chainup.new_version.home.callback

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.contract.sdk.data.ContractTicker


class ContractDiffCallback(private val mOldEmployeeList: List<ContractTicker>, private val mNewEmployeeList: List<ContractTicker>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldEmployeeList.size
    }

    override fun getNewListSize(): Int {
        return mNewEmployeeList.size
    }

    fun getNewData(): List<ContractTicker> {
        return mNewEmployeeList
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldEmployeeList[oldItemPosition].instrument_id == mNewEmployeeList[newItemPosition].instrument_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEmployee = mOldEmployeeList[oldItemPosition]
        val newEmployee = mNewEmployeeList[newItemPosition]
        val rose = oldEmployee.change_rate
        val roseNew = oldEmployee.change_rate
        val price = oldEmployee.last_px
        val priceNew = newEmployee.last_px
        val vol = oldEmployee.qty24
        val volNew = newEmployee.qty24
        val isChange = rose == roseNew
                && price == priceNew  && vol == volNew
        return isChange
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}