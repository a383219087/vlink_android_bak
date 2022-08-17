package com.yjkj.chainup.new_version.fragment

import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.MarketDetailAdapter
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.ContextUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.util.getSymbolChannel
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_market_detail.rv_market_detail
import kotlinx.android.synthetic.main.fragment_market_detail.swipe_refresh
import kotlinx.android.synthetic.main.include_market_sort.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.imageResource
import org.json.JSONObject


/**
 * @Author: Bertking
 * @Date：2019-06-15-15:26
 * @Description:
 */

class MarketTrendFragment : NBaseFragment() {


    override fun setContentView() = R.layout.fragment_market_detail

    override fun initView() {
        tv_name?.text = LanguageUtil.getString(context, "home_action_coinNameTitle")
        tv_new_price?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice")
        tv_limit?.text = LanguageUtil.getString(context, "common_text_priceLimit")
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        initAdapter()
        setOnclick()
        setOnScrowListener()
    }


    override fun loadData() {
        super.loadData()

        marketName = arguments?.getString(MARKET_NAME) ?: ""
        curIndex = arguments?.getInt(CUR_INDEX) ?: 1

        if (null == marketName || marketName.isEmpty())
            return

        symbols = NCoinManager.getMarketByName(marketName)

        if (null == symbols || symbols.isEmpty())
            return

        oriSymbols.addAll(symbols)

        normalTickList.clear()
        normalTickList.addAll(oriSymbols)

        normalTickList?.sortBy { it?.optInt("sort") }
        normalTickList.sortBy { it.optInt("newcoinFlag") }

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
            adapter = MarketDetailAdapter()

            rv_market_detail?.layoutManager = LinearLayoutManager(mActivity)
            rv_market_detail?.adapter = adapter
            adapter?.notifyDataSetChanged()
            rv_market_detail?.isNestedScrollingEnabled = false
            rv_market_detail?.setHasFixedSize(true)
            ll_item_titles?.visibility = View.VISIBLE
            adapter?.setEmptyView(EmptyForAdapterView(context ?: return))
        }

        adapter?.setList(normalTickList)
        initReq()
        adapter?.setOnItemClickListener { adapter, _, position ->
            adapter?.apply {
                val model = (data.get(position) as JSONObject)
                ArouterUtil.forwardKLine(model.optString("symbol"))
            }
        }

    }

    /**
     * 原始
     */
    private var oriSymbols = arrayListOf<JSONObject>()

    private var normalTickList = arrayListOf<JSONObject>()

    var adapter: MarketDetailAdapter? = null

    private var marketName = ""
    private var curIndex = 1
    var selectIndex = 1
        set(value) {
            field = value
        }
    private var symbols = arrayListOf<JSONObject>()


    var nameIndex = 0
    var newPriceIndex = 0
    var limitIndex = 0

    var isScrollStatus = false
    /**
     * 点击事件
     */
    fun setOnclick() {
        /**
         * 点击名称
         */
        ll_name.setOnClickListener {
            refreshTransferImageView(0)
            adapter?.isMarketSort = false
            val normalTickList = adapter?.data
            if (normalTickList.isNullOrEmpty()) return@setOnClickListener
            when (nameIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    normalTickList.sortBy { NCoinManager.showAnoterName(it) }
                    normalTickList.sortBy { it.optInt("newcoinFlag") }
                    nameIndex = 1
                    iv_name_up?.imageResource = R.drawable.quotes_up_daytime
                }
                /**
                 * 正序
                 */
                1 -> {
                    normalTickList.sortByDescending { NCoinManager.showAnoterName(it) }
                    normalTickList.sortBy { it.optInt("newcoinFlag") }
                    nameIndex = 2
                    iv_name_up?.imageResource = R.drawable.quotes_under_daytime
                }
                /**
                 * 倒序
                 */
                2 -> {
                    normalTickList.sortBy { it.optInt("sort") }
                    normalTickList.sortBy { it.optInt("newcoinFlag") }
                    nameIndex = 0
                    iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                }
            }

            refreshAdapter(normalTickList as ArrayList<JSONObject>)
        }
        /**
         * 点击最新价
         */
        ll_new_price.setOnClickListener {
            refreshTransferImageView(1)
            val normalTickList = adapter?.data
            if (normalTickList.isNullOrEmpty()) return@setOnClickListener
            when (newPriceIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    normalTickList.sortBy { it.optDouble("close") }
                    newPriceIndex = 1
                    iv_new_price?.imageResource = R.drawable.quotes_up_daytime

                }
                /**
                 * 正序
                 */
                1 -> {
                    normalTickList.sortByDescending { it.optDouble("close") }
                    newPriceIndex = 2
                    iv_new_price?.imageResource = R.drawable.quotes_under_daytime
                }
                /**
                 * 倒序
                 */
                2 -> {
                    normalTickList.sortBy { it.optInt("sort") }
                    normalTickList.sortBy { it.optInt("newcoinFlag") }
                    newPriceIndex = 0
                    iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime

                }
            }
            adapter?.isMarketSort = newPriceIndex != 0
            refreshAdapter(normalTickList as ArrayList<JSONObject>)
        }
        /**
         * 点击24小时涨幅
         */
        ll_limit.setOnClickListener {
            refreshTransferImageView(2)
            val normalTickList = adapter?.data
            if (normalTickList.isNullOrEmpty()) return@setOnClickListener
            when (limitIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    normalTickList.sortBy { it.optDouble("rose", 0.0) }
                    limitIndex = 1
                    iv_new_limit?.imageResource = R.drawable.quotes_up_daytime
                }
                /**
                 * 正序
                 */
                1 -> {
                    normalTickList.sortByDescending { it.optDouble("rose", 0.0) }
                    limitIndex = 2
                    iv_new_limit?.imageResource = R.drawable.quotes_under_daytime
                }
                /**
                 * 倒序
                 */
                2 -> {
                    normalTickList.sortBy { it.optInt("sort") }
                    normalTickList.sortBy { it.optInt("newcoinFlag") }
                    limitIndex = 0
                    iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                }
            }
            adapter?.isMarketSort = limitIndex != 0
            refreshAdapter(normalTickList as ArrayList<JSONObject>)
        }

        /**
         * 此处刷新
         */
        swipe_refresh?.setOnRefreshListener {
            isScrollStatus = true
            /**
             * 刷新数据操作
             */
             loadData()
            swipe_refresh?.isRefreshing =false
        }
    }


    fun handleData(data: String) {
        try {
            val json = JSONObject(data)
            if (!json.isNull("tick")) {
                doAsync {
                    val quotesData = json
                    showWsData(quotesData)
                }
            } else {
                if (!json.isNull("data")) {
                    doAsync {
                        val array = json.optJSONObject("data")
                        if (null != array && array.length() > 0) {
                            val it = array.keys()
                            val wsArrayMap = hashMapOf<String, JSONObject>()
                            normalTickList.forEach {
                                val key = it.getString("symbol")
                                val tick = array.optJSONObject(key)
                                if (tick != null) {
                                    val itemObj = JSONObject()
                                    itemObj.put("tick", tick)
                                    itemObj.put("channel", "market_${key}_ticker")
                                    wsArrayMap.put(itemObj.optString("channel", ""), itemObj)
                                }
                            }
                            LogUtil.e(TAG, "dropListsAdapter req ${marketName} list ${normalTickList.size} ${wsArrayMap.size}")
                            dropListsAdapter(wsArrayMap)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun showWsData(jsonObject: JSONObject) {
        if (normalTickList.isEmpty())
            return
        val dataDiff = callDataDiff(jsonObject)
        if (dataDiff != null) {
            val items = dataDiff.second
            dropListsAdapter(items)
            wsArrayTempList.clear()
            wsArrayMap.clear()
        }
    }


    fun startInit() {
        Handler().postDelayed({
            pageEventSymbol()
        }, 200)
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

    private fun refreshAdapter(list: ArrayList<JSONObject>) {
        adapter?.replaceData(list)
    }

    private fun pageEventSymbol() {
        if (normalTickList.size == 0) {
            return
        }
        val arrays = arrayOfNulls<String>(normalTickList.size)
        for ((index, item) in normalTickList.withIndex()) {
            arrays.set(index, item.getString("symbol"))
        }
        forwardMarketTab(arrays)
    }

    private fun forwardMarketTab(coin: Array<String?>, isBind: Boolean = true) {
        val messageEvent = MessageEvent(MessageEvent.market_event_page_symbol_type)
        messageEvent.msg_content = hashMapOf("symbols" to coin, "bind" to isBind, "curIndex" to 1)
        EventBusUtil.post(messageEvent)
    }


    private val wsArrayTempList: ArrayList<JSONObject> = arrayListOf()
    private val wsArrayMap = hashMapOf<String, JSONObject>()
    private var wsTimeFirst: Long = 0L

    @Synchronized
    private fun callDataDiff(jsonObject: JSONObject): Pair<ArrayList<JSONObject>, HashMap<String, JSONObject>>? {
        if (System.currentTimeMillis() - wsTimeFirst >= 2000L && wsTimeFirst != 0L) {
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
            wsArrayMap.put(jsonObject.optString("channel", ""), jsonObject)
        }
        return null
    }

    @Synchronized
    fun dropListsAdapter(items: HashMap<String, JSONObject>) {
        val data = adapter?.data
        if (data?.isEmpty()!!) {
            return
        }
        val message = Gson().toJson(data)
        val jsonCopy = Utils.jsonToArrayList(message, JSONObject::class.java)
        val tempNew = jsonCopy
        for ((index, item) in items.entries) {
            val jsonObject = item
            val channel = jsonObject.optString("channel")
            var tempData = -1
            for ((coinIndex, coinItem) in data.withIndex()) {
                if (channel == coinItem.optString("symbol").getSymbolChannel()) {
                    tempData = coinIndex
                    break
                }
            }
            if (tempData != -1) {
                val tick = jsonObject.optJSONObject("tick")
                val model = tempNew.get(tempData)
                model.put("rose", tick?.optString("rose"))
                model.put("close", tick?.optString("close"))
                model.put("vol", tick?.optString("vol"))
                tempNew.set(tempData, model)
            }
        }
        if (newPriceIndex != 0 || limitIndex != 0) {
            if (newPriceIndex != 0) {
                when (newPriceIndex) {
                    1 -> {
                        tempNew.sortBy { it.optDouble("close", 0.0) }
                    }
                    2 -> {
                        tempNew.sortByDescending { it.optDouble("close", 0.0) }
                    }
                }
            } else if (limitIndex != 0) {
                when (limitIndex) {
                    1 -> {
                        tempNew.sortBy { it.optDouble("rose", 0.0) }
                    }
                    2 -> {
                        tempNew.sortByDescending { it.optDouble("rose", 0.0) }
                    }
                }
            }
        }
        val diffCallback = MarketTabDiffCallback(data, tempNew)
        activity?.runOnUiThread {
            adapter?.setDiffData(diffCallback)
        }

    }

    private fun initReq() {
        val data = WsAgentManager.instance.reqJson
        if (data != null) {
            doAsync {
                normalTickList.forEach {
                    val key = it.getString("symbol")
                    val tick = data.get(key)
                    if (tick != null) {
                        it.put("rose", tick.get("rose"))
                        it.put("close", tick.get("close"))
                        it.put("vol", tick.get("vol"))
                    }
                }
                activity?.runOnUiThread {
                    adapter?.setList(normalTickList)
                }
            }
        }
    }
}