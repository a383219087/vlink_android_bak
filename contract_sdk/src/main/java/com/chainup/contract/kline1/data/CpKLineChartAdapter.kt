package com.chainup.contract.kline1.data

import com.chainup.contract.kline1.data.CpKLineChartAdapter
import com.chainup.contract.utils.CpDateUtils
import com.chainup.contract.kline1.bean.CpKLineBean
import java.lang.Exception


/**
 * @Author: Bertking
 * @Date：2019/3/14-2:13 PM
 * @Description:
 */
class CpKLineChartAdapter : CpBaseKLineChartAdapter() {

    val TAG = CpKLineChartAdapter::class.java.simpleName

    private val data = arrayListOf<CpKLineBean>()

    private var dateFormat = CpDateUtils.FORMAT_MONTH_DAY_HOUR_MIN


    fun setDateFormat(dateFormat: String) {
        this.dateFormat = dateFormat
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        try {

        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        } finally {
            if (data.size <= position) {
                var size = data.size - 1
                if (size >= 0) {
                    return data[size]
                }else{
                    return 0
                }
            } else {
                return data[position]
            }
        }
    }

    override fun getDate(position: Int): String {
        if (position >= data.size) return ""
        val date = data[position].id
        when (dateFormat) {
            CpDateUtils.FORMAT_YEAR_MONTH -> return CpDateUtils.getYearMonthDayHourMin(date)
            CpDateUtils.FORMAT_YEAR_MONTH_DAY -> return CpDateUtils.getYearMonthDay(date)
            else -> return CpDateUtils.getYearMonthDayHourMin(date)
        }

    }

    /**
     * 向头部添加数据
     */
    fun addHeaderData(data: List<CpKLineBean>?) {
        if (data != null && !data.isEmpty()) {
            this.data.clear()
            this.data.addAll(data)
        } else {
            clearData()
        }
    }

    /**
     * 向尾部添加数据
     */
    fun addFooterData(data: List<CpKLineBean>?) {
        if (data != null && !data.isEmpty()) {
            this.data.clear()
            this.data.addAll(0, data)
            notifyDataSetChanged()
        } else {
            clearData()
        }
    }

    /**
     * 改变某个点的值
     *
     * @param position 索引值
     */
    fun changeItem(position: Int, data: CpKLineBean) {
        try {
            this.data[position] = data
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 改变某个点的值
     */
    fun addItem(data: CpKLineBean) {
        if (this.data.isEmpty()) return
        var lastId = this.data.size - 1
        if (this.data[lastId].id == data.id) {
            changeItem(lastId, data)
        } else {
            this.data.add(data)
            notifyDataSetChanged()
        }
    }

    fun addItems(data: List<CpKLineBean>?) {
        if (data != null && data.isNotEmpty()) {
            this.data.addAll(data)
            notifyDataSetChanged()
        }
    }

    fun addItems(position: Int,data: List<CpKLineBean>?) {
        if (data != null && data.isNotEmpty()) {
            this.data.addAll(position,data)
            notifyDataSetChanged()
        }
    }

    /**
     * 数据清除
     */
    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }
}
