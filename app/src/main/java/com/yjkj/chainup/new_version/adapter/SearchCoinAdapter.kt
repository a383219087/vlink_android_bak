package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.widget.Filter
import android.widget.Filterable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.setGoneV3

/**
 * @Author: Bertking
 * @Date：2019/4/3-2:48 PM
 * @Description: 用于「币币交易」选择币对
 */
class SearchCoinAdapter(var datas: ArrayList<QuotesData.Tick>, var allData: ArrayList<QuotesData.Tick>, var isNeedArea: Boolean = true) :
        BaseQuickAdapter<QuotesData.Tick, BaseViewHolder>(R.layout.item_search_coin, datas), Filterable {
    val TAG = SearchCoinAdapter::class.java.simpleName

    private var filter: MyFilter? = null
    var listener: FilterListener? = null

    override fun convert(helper: BaseViewHolder, tick: QuotesData.Tick) {
        Log.d(TAG, "======Tick======" + tick?.toString())
        tick ?: return
        /**
         * Tick(amount='39.96450966', vol='17.56774781', high='2.30000000', low='2.18970000', rose=0.0, close='2.30000000', open='2.30000000', name='BCH/BTC', symbol='bchbtc')
         */


        var name = ""
        if (tick?.anotherName?.isNotEmpty()) {
            name = tick?.anotherName
        } else {
            name = tick?.name
        }

        if (TextUtils.isEmpty(name) || helper == null) {
            return
        }
        // TODO 待解决不包含/的情况
        if (!name?.contains("/")!!) return

        val split = name.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        helper?.setText(R.id.tv_coin_name, split[0])
        helper?.setText(R.id.tv_market_name, "/" + split[1])


        val mainArea =  LanguageUtil.getString(context, "transaction_text_mainZone")
        val innovationArea =  LanguageUtil.getString(context, "market_text_innovationZone")
        val observeArea =  LanguageUtil.getString(context,"market_text_observeZone")

        if (isNeedArea) {
            if (helper?.adapterPosition == 0) {
                helper.setGoneV3(R.id.ll_main_area, true)
                helper.setGoneV3(R.id.v_line, true)
                when (tick.type) {
                    0 -> {
                        helper.setText(R.id.ll_title_content, mainArea)
                    }
                    1 -> {
                        helper.setText(R.id.ll_title_content, innovationArea)
                    }
                    2 -> {
                        helper.setText(R.id.ll_title_content, observeArea)
                    }
                }
            } else {
                Log.d(TAG, "========data:${data.size},position:${helper?.adapterPosition}====")
                if (data.size <= helper?.adapterPosition ?: 0) return
                if (data[helper?.adapterPosition!!
                                - 1].type != tick.type) {
                    helper.setGoneV3(R.id.ll_main_area, true)
                    helper.setGoneV3(R.id.v_line, true)
                    when (tick.type) {
                        0 -> {
                            helper.setText(R.id.ll_title_content, mainArea)
                        }
                        1 -> {
                            helper.setText(R.id.ll_title_content, innovationArea)
                        }
                        2 -> {
                            helper.setText(R.id.ll_title_content, observeArea)
                        }
                    }
                } else {
                    helper.setGoneV3(R.id.ll_main_area, false)
                    helper.setGoneV3(R.id.v_line, false)
                }
            }
        }


        /**
         * 收盘价
         */
        if (TextUtils.isEmpty(tick.close)) {
            helper.setText(R.id.tv_close_price, "--")
        } else {
            helper.setText(R.id.tv_close_price, DecimalUtil.cutValueByPrecision(tick.close, tick.pricePrecision))
        }


        val rose = tick.rose.toDouble()
        helper.setTextColor(R.id.tv_close_price, ColorUtil.getMainColorType(rose >= 0))

//        when {
//            rose > 0 -> {
//                helper?.getView<TextView>(R.id.tv_close_price)?.setTextColor(ColorUtil.getMainColorType())
//            }
//            rose == 0.0 -> {
//                helper?.getView<TextView>(R.id.tv_close_price)?.setTextColor(ColorUtil.getColor(R.color.main_font_color))
//            }
//            else -> {
//                helper?.getView<TextView>(R.id.tv_close_price)?.setTextColor(ColorUtil.getMainColorType(isRise = false))
//            }
//        }

    }


    interface FilterListener {
        fun getFilterData(list: List<QuotesData.Tick>) //获取过滤后的数据
    }


    /**
     * 实现了Filterable接口，重写了该方法
     */
    override fun getFilter(): Filter {
        return filter ?: MyFilter(allData)
    }

    /**
     * 创建内部类，实现数据的过滤
     */
    internal inner class MyFilter(var originalData: ArrayList<QuotesData.Tick>) : Filter() {

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

            if (TextUtils.isEmpty(constraint)) {
                results.values = originalData
                results.count = originalData.size
            } else {
                // 创建集合保存过滤后的数据
                var filteredList = ArrayList<QuotesData.Tick>()
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (s in originalData) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    val split = s.name.split("/")
                    Log.d(TAG, "=====ss:${split[0]}====")
                    if (NCoinManager.getShowMarket(split[0]).toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
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
            if (results.values != null) {
                data = results.values as ArrayList<QuotesData.Tick>
            }
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            listener?.getFilterData(data)
            Log.d(TAG, "publishResults===" + data.size)
            // 刷新数据源显示
            notifyDataSetChanged()
        }
    }

}

