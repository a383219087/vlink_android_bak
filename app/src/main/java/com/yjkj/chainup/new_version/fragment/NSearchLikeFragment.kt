package com.yjkj.chainup.new_version.fragment


import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.databinding.FragmentSearchCoinBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.TradeTypeEnum
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NSearchCoinAdapter
import com.yjkj.chainup.new_version.home.MarketWsData
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.*
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_search_coin.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject

/**
 * @date 2019-9-26
 * @author Bertking
 * @description : 侧边栏自选(改版)
 *
 */
class NSearchLikeFragment : NBaseFragment() {

    private var marketName = ""
    private var curIndex = 0
    private var type = 0
    private var isblack = false

    var adapter: NSearchCoinAdapter? = null
    private var normalTickList = arrayListOf<JSONObject>()
    override fun setContentView() = R.layout.fragment_search_coin

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var binding:FragmentSearchCoinBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_coin, container, false);
        layoutView = binding.root
        loadData()
        super.onCreateView(inflater, container, savedInstanceState)
        binding.isblack = isblack
        return layoutView
    }

    override fun initView() {
        initAdapter()
        initSocket()
        showSearch()
    }




    private fun filterLeverData(dataList: ArrayList<JSONObject>): ArrayList<JSONObject> {
        val newList = java.util.ArrayList<JSONObject>()
        if (dataList.size > 0) {
            for (i in dataList.indices) {
                val jsonObject = dataList[i]
                val isOpenLever = jsonObject.has("isOpenLever") && jsonObject.optString("isOpenLever") != "0"
                val multiple = jsonObject.has("multiple") && jsonObject.optString("multiple") != "0"
                if (multiple && isOpenLever) {
                    newList.add(jsonObject)
                }
            }
        }
        return newList
    }


    override fun loadData() {
        super.loadData()
        arguments?.let {
            marketName = it.getString(MARKET_NAME) ?: ""
            curIndex = it.getInt(CUR_INDEX)
            type = it.getInt(ParamConstant.TYPE)
            isblack = it.getBoolean(ParamConstant.ISBLACK)
        }


    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            initSocket()
        } else {
            mMarketWsData?.closeWS()
        }
    }


    private fun initAdapter() {
        rv_search_coin?.layoutManager = LinearLayoutManager(activity)
        (rv_search_coin?.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        adapter = NSearchCoinAdapter()
        adapter?.isShowLever = (type == TradeTypeEnum.LEVER_TRADE.value)
        adapter?.isblack = isblack
        adapter?.setParams(0 == curIndex)
        rv_search_coin?.adapter = adapter
        rv_search_coin?.setHasFixedSize(true)
        adapter?.setEmptyView(EmptyForAdapterView(context ?: return , isblack = isblack))
        adapter?.emptyLayout?.findViewById<TextView>(R.id.tv_add_like)?.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.CoinMapActivity, Bundle().apply {
                putString(ParamConstant.TYPE, ParamConstant.ADD_COIN_MAP)
            })
        }

        initReq()

        adapter?.setOnItemClickListener { adapter, _, position ->

            val dataList = adapter.data
            if (null == dataList || dataList.size <= 0) {
                return@setOnItemClickListener
            }

            var data = dataList[position] as JSONObject

            var symbol = data.optString("symbol")

            var messageEvent = MessageEvent(MessageEvent.symbol_switch_type)
            messageEvent.msg_content = symbol
            messageEvent.isLever = TradeTypeEnum.LEVER_TRADE.value == type
            messageEvent.isGrid = TradeTypeEnum.GRID_TRADE.value == type
            NLiveDataUtil.postValue(messageEvent)
            closeDialog()
        }

        if (0 == curIndex) {
            var localData = LikeDataService.getInstance().getCollecData(TradeTypeEnum.LEVER_TRADE.value == type)

            if (null != localData) {
                if (TradeTypeEnum.GRID_TRADE.value == type) {
                    var listGrid = arrayListOf<JSONObject>()
                    for (temp in localData) {
                        if (temp.optInt("is_grid_open") == 1) {
                            listGrid.add(temp)
                        }
                    }
                    localData.clear()
                    localData.addAll(listGrid)
                }
                normalTickList = localData
            } else {
                normalTickList.clear()
            }
        } else {
            val oriSymbols : ArrayList<JSONObject>? =  if (TradeTypeEnum.LEVER_TRADE.value == type) {
                NCoinManager.getLeverGroupList(marketName)
            } else if (TradeTypeEnum.COIN_TRADE.value == type) {
                NCoinManager.getMarketByName(marketName)
            } else {
                NCoinManager.getGridCroupList(marketName)
            }

            LogUtil.d(TAG, "type is $type,oriSymbols is $oriSymbols")
            if (null != oriSymbols && oriSymbols.size > 0) {
                normalTickList.clear()
                normalTickList.addAll(oriSymbols)
            }
        }
        normalTickList.sortBy { it.optInt("sort") }
        normalTickList.sortBy { it.optInt("newcoinFlag") }
        refreshAdapter(normalTickList)
    }

    private fun closeDialog() {
        Handler().postDelayed({
            var msgEvent = MessageEvent(MessageEvent.closeLeftCoinSearchType)
            NLiveDataUtil.postValue(msgEvent)
        }, 200)

    }

    private var mMarketWsData: MarketWsData? = null
    private fun initSocket() {

    }


    override fun onMessageStickyEvent(event: MessageEvent) {
        super.onMessageStickyEvent(event)
        if (MessageEvent.collect_data_type == event.msg_type) {
            if (0 == curIndex) {
                var isLogined = UserDataService.getInstance().isLogined
                var isOptionalSymbolServerOpen = PublicInfoDataService.getInstance().isOptionalSymbolServerOpen(null)

                if (isLogined && isOptionalSymbolServerOpen) {
                    getOptionalSymbol()
                } else {
                    showLocalCollectData()
                }
                EventBusUtil.removeStickyEvent(event)

            }
        }
    }

    private fun showLocalCollectData() {
        var localData = LikeDataService.getInstance().getCollecData(TradeTypeEnum.LEVER_TRADE.value == type)
        if (null != localData) {
            if (TradeTypeEnum.GRID_TRADE.value == type) {
                var listGrid = arrayListOf<JSONObject>()
                for (temp in localData) {
                    if (temp.optInt("is_grid_open") == 1) {
                        listGrid.add(temp)
                    }
                }
                localData.clear()
                localData.addAll(listGrid)
            }
            normalTickList = localData
        } else {
            normalTickList.clear()
        }
        refreshAdapter(normalTickList)
    }

    private fun refreshAdapter(dataList: ArrayList<JSONObject>?) {

        if (null == dataList || dataList.size <= 0) {
            adapter?.setEmptyView(EmptyForAdapterView(context ?: return))
            adapter?.setList(null)
        } else {
            adapter?.setList(dataList)
        }
    }

    /*
     * 获取服务器用户自选数据
     * var req_type = type
     */
    fun getOptionalSymbol() {
        addDisposable(getMainModel().getOptionalSymbol(MyNDisposableObserver(getUserSelfDataReqType), ""))
    }

    val getUserSelfDataReqType = 2 // 服务器用户自选数据
    val addCancelUserSelfDataReqType = 3

    inner class MyNDisposableObserver(type: Int) : NDisposableObserver() {
        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            closeLoadingDialog()
            if (getUserSelfDataReqType == req_type) {
                showServerSelfSymbols(jsonObject.optJSONObject("data"))
            } else if (addCancelUserSelfDataReqType == req_type) {
                if (0 == operationType) {
                    getOptionalSymbol()
                }
            }
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            closeLoadingDialog()
            if (getUserSelfDataReqType == req_type) {
                showLocalCollectData()
            }

        }

    }

    /*
     * 显示服务器用户的自选币对数据
     */
    private fun showServerSelfSymbols(data: JSONObject?) {

        if (null == data || data.length() <= 0) {
            showLocalCollectData()
            return
        }

        var array = data.optJSONArray("symbols")
        var sync_status = data.optString("sync_status")

        if ("0".equals(sync_status)) {
            var array = LikeDataService.getInstance().symbols
            if (null != array && array.length() > 0) {
                var temps = ArrayList<String>()
                for (i in 0 until array.length()) {
                    temps.add(array.optString(i))
                }
                operationType = 0
                addOrDeleteSymbol(temps)
                return
            }
        }

        if (null == array || array.length() <= 0) {
            showLocalCollectData()
            return
        }
        LikeDataService.getInstance().clearAllCollect()
        var tempList = ArrayList<JSONObject>()
        for (i in 0 until array.length()) {

            var symbol = array.optString(i)
            var symbolObj = NCoinManager.getSymbolObj(symbol)
            if (null != symbolObj && symbolObj.length() > 0) {
                tempList.add(symbolObj)
                LikeDataService.getInstance().saveCollecData(symbol, symbolObj)
            }
        }
        if (!tempList.isEmpty()) {
            tempList.sortBy { it.optInt("newcoinFlag") }
            mMarketWsData?.closeWS()

            if (TradeTypeEnum.GRID_TRADE.value == type) {
                var listGrid = arrayListOf<JSONObject>()
                for (temp in tempList) {
                    if (temp.optInt("is_grid_open") == 1) {
                        listGrid.add(temp)
                    }
                }
                tempList.clear()
                tempList.addAll(listGrid)
            }

            normalTickList.clear()
            normalTickList.addAll(tempList)
        }
        normalTickList.sortBy { it.optInt("sort") }
        normalTickList.sortBy { it.optInt("newcoinFlag") }

        if (PublicInfoDataService.getInstance().isLeverOpen(null) && (type == TradeTypeEnum.LEVER_TRADE.value)) run {
            val newList = java.util.ArrayList<JSONObject>()
            if (normalTickList.size > 0) {
                for (i in normalTickList.indices) {
                    val jsonObject = normalTickList[i]
                    val isOpenLever = jsonObject.has("isOpenLever") && jsonObject.optString("isOpenLever") != "0"
                    val multiple = jsonObject.has("multiple") && jsonObject.optString("multiple") != "0"
                    if (multiple && isOpenLever) {
                        newList.add(jsonObject)
                    }
                }
            }
            adapter?.setList(newList)
        } else {
            adapter?.setList(normalTickList)
        }


    }

    /**
     * 添加或者删除自选数据
     * @param operationType 标识 0(批量添加)/1(单个添加)/2(单个删除)
     * @param symbol 单个币对名称
     */
    var operationType = 0

    fun addOrDeleteSymbol(symbols: ArrayList<String>?) {
        if (null == symbols || symbols.isEmpty())
            return
        showLoadingDialog()
        addDisposable(getMainModel().addOrDeleteSymbol(operationType, symbols,"", MyNDisposableObserver(addCancelUserSelfDataReqType)))
    }

    fun handleData(data: String) {
        try {
            val json = JSONObject(data)
            if (!json.isNull("tick")) {
                doAsync {
                    val quotesData = json
                    showWsData(quotesData)
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

    private val wsArrayTempList: ArrayList<JSONObject> = arrayListOf()
    private val wsArrayMap = hashMapOf<String, JSONObject>()
    private var wsTimeFirst: Long = 0L

    @Synchronized
    private fun callDataDiff(jsonObject: JSONObject): Pair<ArrayList<JSONObject>, HashMap<String, JSONObject>>? {
        if (System.currentTimeMillis() - wsTimeFirst >= 1000L && wsTimeFirst != 0L) {
            // 大于一秒
            wsTimeFirst = 0L
            if (wsArrayMap.size != 0) {
                return Pair(wsArrayTempList, wsArrayMap)
            }
        } else {
            if (wsTimeFirst == 0L) {
                wsTimeFirst = System.currentTimeMillis()
            }
            val tempCoin = normalTickList.filter { it.optString("symbol") == jsonObject.byMarketCoinList() }
            if (tempCoin.isNotEmpty()) {
                wsArrayTempList.add(jsonObject)
                wsArrayMap.put(jsonObject.optString("channel", ""), jsonObject)
            } else {
                LogUtil.d(TAG, "callDataDiff==content is $jsonObject")
            }
        }
        return null
    }

    @Synchronized
    private fun dropListsAdapter(items: HashMap<String, JSONObject>) {
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
        val diffCallback = MarketTabDiffCallback(data, tempNew)
        activity?.runOnUiThread {
            adapter?.setDiffData(diffCallback)
        }

    }

    private fun searchList(content: String) {
        if (StringUtil.checkStr(content)) {
            val list = if (TradeTypeEnum.LEVER_TRADE.value == type) {
                filterLeverData(normalTickList)
            } else {
                normalTickList
            }
            val searchData = NCoinManager.getSearchData(list, content)
            refreshAdapter(searchData)
        } else {
            if (TradeTypeEnum.LEVER_TRADE.value == type) {
                refreshAdapter(filterLeverData(normalTickList))
            } else {
                  refreshAdapter(normalTickList)
            }
        }

    }

    private fun showSearch() {
        et_search?.hint = LanguageUtil.getString(context, "assets_action_search")
        et_search?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val content = s.toString()
                LogUtil.d("et_search", "afterTextChanged==content is $content")
                searchList(content)
            }
        })
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
