package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/16-4:19 PM
 * @Description:
 */
class SelectCoinAdapter(val datas: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_search_coin, datas), Filterable {

    val TAG = SelectCoinAdapter::class.java.simpleName

    var beanData: ArrayList<JSONObject> = arrayListOf()


    fun setBean(data: ArrayList<JSONObject>) {
        beanData = data
        notifyDataSetChanged()
    }

    private var filter: MyFilter? = null
    var listener: FilterListener? = null

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(item?.optString("coinName")))
        helper?.getView<TextView>(R.id.tv_coin_name)?.textSize = 14f

        helper?.getView<View>(R.id.tv_market_name)?.visibility = View.GONE

        if (item?.has("depositOpen")!!) {
            //depositOpen  0 暂停充币  1 允许充币
            helper?.setGone(R.id.tv_close_price, item?.optInt("depositOpen") != 1)
            helper?.setText(R.id.tv_close_price, if (item?.optInt("depositOpen") != 1) LanguageUtil.getString(context,"assets_suspend_deposit") else "")
        } else if (item?.has("withdrawOpen")!!) {
            //withdrawOpen  0 暂停提币  1 允许提币
            helper?.setGone(R.id.tv_close_price, item?.optInt("withdrawOpen") != 1)
            helper?.setText(R.id.tv_close_price, if (item?.optInt("withdrawOpen") != 1) LanguageUtil.getString(context,"assets_suspend_withdraw") else "")
        }else if (item?.has("withdrawOpen")!! && item?.has("depositOpen")!!){
            helper?.setText(R.id.tv_close_price, if (item?.optInt("withdrawOpen") != 1) LanguageUtil.getString(context,"assets_suspend_deposit") else "")
        }

        helper?.getView<View>(R.id.tv_close_price)?.visibility = View.GONE
        helper.setGoneV3(R.id.ctv_content, false)
        helper.setGoneV3(R.id.item_view_market_line, false)

    }


    interface FilterListener {
        fun getFilterData(list: ArrayList<JSONObject>) //获取过滤后的数据
    }


    /**
     * 实现了Filterable接口，重写了该方法
     */
    override fun getFilter(): Filter {
        return filter ?: MyFilter(beanData)
    }

    /**
     * 创建内部类，实现数据的过滤
     */
    internal inner class MyFilter(var originalData: ArrayList<JSONObject>) : Filter() {
        /**
         * 该方法返回搜索过滤后的数据
         *
         * @param constraint
         * @return
         */
        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            val results = Filter.FilterResults()
            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             */

            Log.d(TAG, "=======s:$constraint")
            Log.d(TAG, "=======s1:${originalData.size}")
            Log.d(TAG, "=======s2:${beanData?.size}")
            Log.d(TAG, "=======s3:${data?.size}")

            if (TextUtils.isEmpty(constraint)) {
                results.values = data
                results.count = data?.size
            } else {
                // 创建集合保存过滤后的数据
                val filteredList = ArrayList<JSONObject>()
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (s in originalData) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    if (null != s.optString("coinName") && NCoinManager.getShowMarket(s.optString("coinName")).toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
                        // 规则匹配的话就往集合中添加该数据
                        filteredList.add(s)
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
            }

            // 返回FilterResults对象
            return results
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {

            // 获取过滤后的数据
            try {
                val beanDatas = results.values as ArrayList<JSONObject>
                // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
                listener?.getFilterData(beanDatas)
                Log.d(TAG, "publishResults===" + beanData.size)
                // 刷新数据源显示
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}