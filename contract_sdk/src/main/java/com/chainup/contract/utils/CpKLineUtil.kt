package com.chainup.contract.utils

import android.content.Context
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.kline.view.MainKlineViewStatus
import com.chainup.contract.kline.view.vice.CpViceViewStatus
import java.lang.IndexOutOfBoundsException

/**
 * @Author: Bertking
 * @Date：2019/3/19-11:52 AM
 * @Description: K线相关的配置项
 */
object CpKLineUtil {

    /**
     * KLine的当前刻度
     */
    private const val CURRENT_TIME = "cur_time"


    private const val CURRENT_TIME_CONTENT = "cur_time_content"

    /**
     * 副图指标
     */
    private const val VICE_INDEX = "vice_index"

    /**
     * 主图指标
     */
    private const val MAIN_INDEX = "main_index"


    /**
     * 获取Kline的刻度
     */
    fun getKLineScale(): ArrayList<String> {
        var list = arrayListOf<String>()
        list.add("1min")
        list.add("5min")
        list.add("15min")
        list.add("30min")
        list.add("60min")
        list.add("4h")
        list.add("1day")
        list.add("1week")
        list.add("1month")


        /**
         * 添加分时
         */
        list.add(0, "line")
        return list
    }

    fun getShowKLineScaleName(name:String,context:Context): String {
        return when (name) {
            "1min" -> context.getString(R.string.cp_extra_text41)
            "5min" -> context.getString(R.string.cp_extra_text42)
            "15min" -> context.getString(R.string.cp_extra_text43)
            "30min" -> context.getString(R.string.cp_extra_text44)
            "60min" -> context.getString(R.string.cp_extra_text45)
            "1h" -> context.getString(R.string.cp_extra_text45)
            "4h" -> context.getString(R.string.cp_extra_text46)
            "1day" -> context.getString(R.string.cp_extra_text47)
            "1week" -> context.getString(R.string.cp_extra_text48)
            "1month" -> context.getString(R.string.cp_extra_text49)
            "line" -> context.getString(R.string.cp_extra_text40)
            else -> name
        }
    }

    /**
     *@return 获取KLine的当前刻度
     * TODO 数组越界异常
     */
    fun getCurTime4KLine(): HashMap<Int, String> {
        return try {
            hashMapOf<Int, String>(getCurTime4Index() to getKLineScale()[if (getCurTime4Index() < 0) 0 else getCurTime4Index()])
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            hashMapOf((getCurTime4Index() to getKLineScale()[0]))
        }
    }

    fun setCurTime(curTime: String) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .putSharedString(CURRENT_TIME_CONTENT, curTime);
    }

    /**
     * 设置副图指标
     * @param status 副图指标
     */
    fun setViceIndex(status: Int) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .putSharedInt(VICE_INDEX, status);
    }

    fun getCurTime(): String {
        return  CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .getSharedString(CURRENT_TIME_CONTENT, "15min");
    }


    /**
     * @return 获取KLine的当前刻度的下标
     */
    fun getCurTime4Index(): Int {
        return CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .getSharedInt(CURRENT_TIME, getKLineScale().indexOf("15min"));
    }

    /**
     * 保存KLine的当前刻度
     * @param curPosition KLine的当前刻度下标
     */
    fun setCurTime4KLine(curPosition: Int) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .putSharedInt(CURRENT_TIME, curPosition);
    }

    /**
     * 获取副图指标
     * @return 副图指标
     */
    fun getViceIndex(): Int{
        return CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .getSharedInt(VICE_INDEX, CpViceViewStatus.NONE.status);
    }



    /**
     * 设置主图指标
     * @param status 主图指标
     */
    fun setMainIndex(status: Int) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .putSharedInt(MAIN_INDEX, status);
    }

    /**
     * 获取主图指标
     * @return 主图指标
     */
    fun getMainIndex(): Int {
        return CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .getSharedInt(MAIN_INDEX, MainKlineViewStatus.MA.status);
    }


    fun getKLineDefaultScale(): ArrayList<String> {
        var list = arrayListOf<String>()
        list.add("15min")
        list.add("60min")
        list.add("4h")
        list.add("1day")
        list.add("1week")
        return list
    }
}