package com.chainup.contract.view

import com.chainup.contract.bean.CpDepthBean
import com.chainup.contract.utils.CpKlineDepth
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-03-31-11:45
 * @Email buptjinlong@163.com
 * @description
 */
class CpBBKlineDataDepthHelper {


    private val listeners = ArrayList<DepthKlineDataUpdateListener>()


    /**
     * 初始化深度数据源
     * @param data
     */
    @Synchronized
    fun initSourceDepth(depth: CpKlineDepth?) {
        if (depth != null) {
            mSourceBuys.clear()
            mSourceSells.clear()
            mSourceBuys.addAll(depth.bids)
            mSourceSells.addAll(depth.asks)
        }
    }


    /**
     * 刷新间隔频率
     */
    val intervalFrequency = 100
    var lastTime = 0L


    fun clearData() {
        mSourceBuys.clear()
        mSourceSells.clear()
    }


    fun bindDepthBeanUpdateListener(listener: DepthKlineDataUpdateListener) {
        if (listener != null) {
            listeners.add(listener)
        }
    }

    fun unBindDepthBeanUpdateListener(listener: DepthKlineDataUpdateListener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }

    /**
     * 深度原数据
     */
    var mSourceBuys = ArrayList<CpDepthBean>()
        @Synchronized
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }
    var mSourceSells = ArrayList<CpDepthBean>()
        @Synchronized
        get() {
            if (field == null) {
                field = ArrayList()
            }
            return field
        }


    /**
     * 更新深度数据
     */
    @Synchronized
    fun updateDepthByType(jsonObject: JSONObject) {
        val dataObj = jsonObject.optJSONObject("tick") ?: return
        try {
            val depth = CpKlineDepth()
            depth.fromJson(dataObj)
            var bindList: ArrayList<CpDepthBean> = depth.bids as ArrayList<CpDepthBean>
            var sellList: ArrayList<CpDepthBean> = depth.asks as ArrayList<CpDepthBean>

            mSourceSells.clear()
            mSourceBuys.clear()
            mSourceSells.addAll(sellList)
            mSourceBuys.addAll(bindList)
            //需要排序
            doSourceSort()
            //加上UI刷新频率限制
            val nowTime = System.currentTimeMillis()
            if (nowTime - lastTime < intervalFrequency) {
                return
            }
            lastTime = nowTime
            listeners.forEach {
                it.onUpdateComplete()
            }

        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
    /**
     * 原数据排序
     */
    private fun doSourceSort() {
        //买 降序排列
        mSourceBuys.sortByDescending { it.price.toDouble() }
        //卖 升序排列
        mSourceSells.sortBy { it.price.toDouble() }
    }



    interface DepthKlineDataUpdateListener {
        fun onUpdateComplete()
    }

    companion object {
        @Volatile
        private var mSingleton: CpBBKlineDataDepthHelper? = null

        val instance: CpBBKlineDataDepthHelper?
            get() {
                if (mSingleton == null) {
                    synchronized(CpBBKlineDataDepthHelper::class.java) {
                        if (mSingleton == null) {
                            mSingleton = CpBBKlineDataDepthHelper()
                        }
                    }
                }
                return mSingleton
            }
    }

}