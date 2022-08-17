package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contract.sdk.data.ContractTicker
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.activity.SlContractKlineActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.adapter.MarketSLDetailAdapter
import com.yjkj.chainup.new_version.home.callback.ContractDiffCallback
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_market_detail.*
import kotlinx.android.synthetic.main.include_market_sort.*
import org.jetbrains.anko.imageResource


/**
 * @Author: Bertking
 * @Date：2019-06-15-15:26
 * @Description:
 */

class MarketSLTrendFragment : NBaseFragment() {
    companion object {
        @JvmStatic
        fun newInstance(index: Int): MarketSLTrendFragment {
            val fg = MarketSLTrendFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, index)
            fg.arguments = bundle
            return fg
        }
    }


    override fun setContentView() = R.layout.fragment_market_detail

    override fun initView() {
        tv_name?.text = LanguageUtil.getString(context, "home_action_coinNameTitle")
        tv_new_price?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice")
        tv_limit?.text = LanguageUtil.getString(context, "common_text_priceLimit")

        initAdapter()
        setOnclick()
        setOnScrowListener()

    }


    override fun loadData() {
        super.loadData()
        curIndex = arguments!!.getInt(ParamConstant.CUR_INDEX)

        val ticks = curIndex.byContractGetList();
        symbols = ticks.first

        if (symbols.isEmpty())
            return

        oriSymbols.addAll(symbols)

        normalTickList.clear()
        normalTickList.addAll(oriSymbols)


    }

    var adapterScroll = true
    private fun setOnScrowListener() {
        rv_market_detail?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (ParamConstant.ONSCROLLSTATECHANGED == newState) {
                    adapterScroll = true
                } else {
                    adapterScroll = false
                }
            }
        })
    }

    private fun initAdapter() {
        if (null == adapter) {
            adapter = MarketSLDetailAdapter()

            rv_market_detail?.layoutManager = LinearLayoutManager(mActivity)
            rv_market_detail?.adapter = adapter
            adapter?.notifyDataSetChanged()
            rv_market_detail?.isNestedScrollingEnabled = false
            rv_market_detail?.setHasFixedSize(true)
            ll_item_titles?.visibility = View.VISIBLE
            adapter?.setEmptyView(EmptyForAdapterView(context ?: return))
        }

        adapter?.setNewData(normalTickList)

        adapter?.setOnItemClickListener { adapter, _, position ->
            adapter?.apply {
                val model = (data.get(position) as ContractTicker)
                SlContractKlineActivity.show(activity!!, model.instrument_id)
            }
        }

    }

    /**
     * 原始
     */
    private var oriSymbols = arrayListOf<ContractTicker>()

    private var normalTickList = arrayListOf<ContractTicker>()

    var adapter: MarketSLDetailAdapter? = null

    private var marketName = ""
    private var curIndex = 1
    var selectIndex = 1
        set(value) {
            field = value
        }
    private var symbols = arrayListOf<ContractTicker>()


    var nameIndex = 0
    var newPriceIndex = 0
    var limitIndex = 0

    /**
     * 点击事件
     */
    fun setOnclick() {
        /**
         * 点击名称
         */
        ll_name.setOnClickListener {
            refreshTransferImageView(0)
            refreshSortMarket(0)

        }
        /**
         * 点击最新价
         */
        ll_new_price.setOnClickListener {
            refreshTransferImageView(1)
            refreshSortMarket(1)
        }
        /**
         * 点击24小时涨幅
         */
        ll_limit.setOnClickListener {
            refreshTransferImageView(2)
            refreshSortMarket(2)
        }
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
    }


    private fun refreshTransferImageView(status: Int) {
        when (status) {
            0 -> {
                iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                newPriceIndex = 0
                limitIndex = 0
            }
            1 -> {
                iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                nameIndex = 0
                limitIndex = 0
            }
            2 -> {
                iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime
                nameIndex = 0
                newPriceIndex = 0
            }
        }

    }

    private fun refreshAdapter(list: ArrayList<ContractTicker>) {
        adapter?.replaceData(list)
        pageEventSymbol()
    }

    private fun pageEventSymbol() {
        if (normalTickList.size == 0) {
            return
        }

    }

    private fun refreshSortMarket(status: Int) {
        when (status) {
            0 -> {
                when (nameIndex) {
                    0 -> {
                        normalTickList.sortBy {
                            it.symbolFormat
                        }
                        nameIndex = 1
                        iv_name_up?.imageResource = R.drawable.quotes_up_daytime
                    }
                    1 -> {
                        normalTickList.sortByDescending {
                            it.symbolFormat
                        }
                        nameIndex = 2
                        iv_name_up?.imageResource = R.drawable.quotes_under_daytime
                    }
                    2 -> {
                        normalTickList.clear()
                        normalTickList.addAll(oriSymbols)
                        nameIndex = 0
                        iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                    }
                }
            }
            1 -> {
                when (newPriceIndex) {
                    /**
                     * 正常
                     */
                    0 -> {
                        normalTickList.sortBy { it.getClosePriceSort() }
                        newPriceIndex = 1
                        iv_new_price?.imageResource = R.drawable.quotes_up_daytime

                    }
                    /**
                     * 正序
                     */
                    1 -> {
                        normalTickList.sortByDescending { it.getClosePriceSort() }
                        newPriceIndex = 2
                        iv_new_price?.imageResource = R.drawable.quotes_under_daytime
                    }
                    /**
                     * 倒序
                     */
                    2 -> {
                        normalTickList.clear()
                        normalTickList.addAll(oriSymbols)
                        newPriceIndex = 0
                        iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime

                    }
                }
            }
            2 -> {
                when (limitIndex) {
                    /**
                     * 正常
                     */
                    0 -> {
                        normalTickList.sortBy { it.getChangeRate() }
                        limitIndex = 1
                        iv_new_limit?.imageResource = R.drawable.quotes_up_daytime
                    }
                    /**
                     * 正序
                     */
                    1 -> {
                        normalTickList.sortByDescending { it.getChangeRate() }
                        limitIndex = 2
                        iv_new_limit?.imageResource = R.drawable.quotes_under_daytime
                    }
                    /**
                     * 倒序
                     */
                    2 -> {
                        normalTickList.clear()
                        normalTickList.addAll(oriSymbols)
                        limitIndex = 0
                        iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                    }
                }
            }
        }
        refreshAdapter(normalTickList)
    }


    fun handleData(ticker: ContractTicker) {
        val items = normalTickList.filter { ticker.instrument_id == it.instrument_id }
        if (items.isNotEmpty()) {
            val diff = callDataDiff(ticker)
            if (diff != null) {
                dropListsAdapter(diff.second)
                wsArrayTempList.clear()
                wsArrayMap.clear()
            }
        }
    }

    private val wsArrayTempList: ArrayList<ContractTicker> = arrayListOf()
    private val wsArrayMap = hashMapOf<String, ContractTicker>()
    private var wsTimeFirst: Long = 0L

    @Synchronized
    private fun callDataDiff(jsonObject: ContractTicker): Pair<ArrayList<ContractTicker>, HashMap<String, ContractTicker>>? {
        if (System.currentTimeMillis() - wsTimeFirst >= 1800L && wsTimeFirst != 0L) {
            // 大于一秒
            wsTimeFirst = 0L
            if (wsArrayMap.size != 0) {
                return Pair(wsArrayTempList, wsArrayMap)
            }
        } else {
            if (wsTimeFirst == 0L) {
                wsTimeFirst = System.currentTimeMillis()
            }
            wsArrayTempList.add(jsonObject)
            wsArrayMap.put(jsonObject.instrument_id.toString(), jsonObject)
        }
        return null
    }

    @Synchronized
    private fun dropListsAdapter(items: HashMap<String, ContractTicker>) {
        val data = adapter?.data
        if (data?.isEmpty()!!) {
            return
        }
        val message = Gson().toJson(data)
        val jsonCopy = Utils.jsonToArrayList(message, ContractTicker::class.java)
        val tempNew = jsonCopy
        for ((index, item) in items.entries) {
            val jsonObject = item
            val channel = jsonObject.instrument_id
            var tempData = 0
            for ((index, it) in data.withIndex()) {
                if (channel == it.instrument_id) {
                    tempData = index
                    break
                }
            }
            val model = tempNew.get(tempData)
            model.change_rate = jsonObject.change_rate
            model.qty24 = jsonObject.qty24
            model.last_px = jsonObject.last_px
            tempNew.set(tempData, model)
        }
        if (newPriceIndex != 0 || limitIndex != 0) {
            if (newPriceIndex != 0) {
                when (newPriceIndex) {
                    1 -> {
                        tempNew.sortBy { it.getClosePriceSort() }
                    }
                    2 -> {
                        tempNew.sortByDescending { it.getClosePriceSort() }
                    }
                }
            } else if (limitIndex != 0) {
                when (limitIndex) {
                    1 -> {
                        tempNew.sortBy { it.getChangeRate() }
                    }
                    2 -> {
                        tempNew.sortByDescending { it.getChangeRate() }
                    }
                }
            }
        }
        val diffCallback = ContractDiffCallback(data, tempNew)
        activity?.runOnUiThread {
            adapter?.setDiffData(diffCallback)
        }

    }

}