package com.yjkj.chainup.new_version.activity.like.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.getVisible
import kotlinx.android.synthetic.main.include_search_hot.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * @Author lianshangljl
 * @Date 2020-03-30-12:44
 * @Email buptjinlong@163.com
 * @description
 */
class SearchTopView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var searchViewListener: SearchViewListener? = null
    private lateinit var mHotCoinAdapter: HotCoinAdapter
    var dataList: ArrayList<String> = arrayListOf()

    init {
        initView(context)
    }


    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.include_search_hot, this, true)
        iv_delete_history.onClick {
            searchViewListener?.apply {
                clearSearch()
                rl_history_title.visibility = View.GONE
            }
        }
        mHotCoinAdapter = HotCoinAdapter(R.layout.item_hot_coin_search, dataList)
        mHotCoinAdapter.setOnItemClickListener { adapter, view, position ->
            searchViewListener?.apply {
                val symbol = NCoinManager.getShowMarket(adapter.getItem(position) as String)
                hotItemClick(symbol)
            }
        }
    }

    fun initItems(isShow: Boolean) {
        rl_history_title.visibility = isShow.getVisible()
    }

    fun initTopView(symbols: String) {
        layout_hot.visibility = symbols.isNotEmpty().getVisible()
        if (symbols.isNotEmpty()) {
            val dataList = symbols.split(",")
            mHotCoinAdapter.setList(dataList)
            rv_hot_coin.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = mHotCoinAdapter
            }
        }
    }

    fun getItemView(): View {
        return layout_hot
    }

    class HotCoinAdapter(layoutResId: Int, data: ArrayList<String>) :
            BaseQuickAdapter<String, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: String) {
            val name = NCoinManager.getShowMarket(item)
            helper.setText(R.id.tv_title, name)
        }
    }

    interface SearchViewListener {
        fun clearSearch()
        fun hotItemClick(name: String)
    }


}