package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.widget.Filter
import android.widget.Filterable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.CountryInfo
import com.yjkj.chainup.new_version.activity.SelectAreaActivity
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2019/5/31-9:24 AM
 * @Email buptjinlong@163.com
 * @description
 */
class AreaAdapter(var datas: ArrayList<CountryInfo>,var contextm: SelectAreaActivity) : BaseQuickAdapter<CountryInfo, BaseViewHolder>(R.layout.item_list_counrty_code, datas), Filterable {

    val selectDate: ArrayList<CountryInfo> = datas

    override fun convert(helper: BaseViewHolder, item: CountryInfo) {
        val language = Locale.getDefault().language
        if (language.contains("zh")) {
            helper?.setText(R.id.tv_area_name, item?.cnName)

        } else {
            helper?.setText(R.id.tv_area_name, item?.enName)

        }
        helper?.setText(R.id.tv_area_code, item?.dialingCode)
    }

     val TAG:String = AreaAdapter::class.java.simpleName

    private var filter: MyFilter? = null
    var listener: FilterListener? = null


    interface FilterListener {
        fun getFilterData(list: ArrayList<CountryInfo>) //获取过滤后的数据
    }


    /**
     * 实现了Filterable接口，重写了该方法
     */
    override fun getFilter(): Filter {
        return filter ?: MyFilter(datas)
    }

    /**
     * 创建内部类，实现数据的过滤
     */
    internal inner class MyFilter(var originalData: ArrayList<CountryInfo>) : Filter() {
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


            if (TextUtils.isEmpty(constraint)) {
                results.values = contextm?.countryListNormal
                results.count = contextm?.countryListNormal?.size
            } else {
                // 创建集合保存过滤后的数据
                val filteredList = ArrayList<CountryInfo>()
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (s in contextm.countryListNormal) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    if (s.cnName.toLowerCase().contains(constraint.toString().trim().toLowerCase()) || s.enName.toLowerCase().contains(constraint.toString().trim().toLowerCase()) || s.dialingCode.contains(constraint.toString().trim().toLowerCase())) {
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
               val  datas = results.values as ArrayList<CountryInfo>
                // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
                listener?.getFilterData(datas)
                Log.d(TAG, "publishResults===" + datas.size)
            }
            // 刷新数据源显示
            notifyDataSetChanged()
        }
    }
}