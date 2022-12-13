package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.HeYueLikeDataService
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.SymbolWsData
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.MarketDetailAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.new_version.view.EmptyMarketForAdapterView
import com.yjkj.chainup.util.ContextUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.util.getSymbolChannel
import kotlinx.android.synthetic.main.fragment_likes.*
import kotlinx.android.synthetic.main.fragment_likes.rv_market_detail
import kotlinx.android.synthetic.main.fragment_likes.swipe_refresh
import kotlinx.android.synthetic.main.fragment_market_detail.*
import kotlinx.android.synthetic.main.include_market_sort.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.imageResource
import org.json.JSONObject
import java.util.HashMap


/**
 * @description :  合约的自选页面
 * @date 2018-1- 5
 * @author Bertking
 *
 * PS：虽然该Fragment跟MarketFragment很相似，但是单独处理
 * 1. 提高代码阅读性；
 * 2. 提高性能
 */
class LikesHeYueFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_likes_heyue

    var adapter: MarketDetailAdapter? = null
    private var curIndex = 0
    var isScrollStatus = false
    override fun loadData() {
        super.loadData()
        curIndex = arguments?.getInt(CUR_INDEX) ?: 0
    }

    override fun initView() {
        tv_name?.text = LanguageUtil.getString(context, "home_action_coinNameTitle")
        tv_new_price?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice")
        tv_limit?.text = LanguageUtil.getString(context, "common_text_priceLimit")
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        showLikeView(false)
        initRecylerView()
        setOnclick()
        initAdapter()
        setOnScrowListener()

    }

    private fun setOnScrowListener() {
        rv_market_detail?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    LogUtil.d("onScrollStateChanged", "onScrollStateChanged==初始化")
                } else {
                    LogUtil.d("onScrollStateChanged", "onScrollStateChanged==关闭")
                }
            }
        })
    }

    private var isFirstInitRecylerView = true
    private fun initRecylerView() {
        if (!isFirstInitRecylerView)
            return
        isFirstInitRecylerView = false

        rv_market_detail?.layoutManager = LinearLayoutManager(context)
        (rv_market_detail.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

    }


    /**
     * 原始
     */
    private var oriSymbols = arrayListOf<JSONObject>()

    private var normalTickList = arrayListOf<JSONObject>()

    private var serverOriDataList = arrayListOf<JSONObject>()  //只用于记录接口返回的原始自选数据

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
            when (nameIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    normalTickList.sortBy { NCoinManager.showAnoterName(it) }
                    nameIndex = 1
                    iv_name_up?.imageResource = R.drawable.quotes_up_daytime

                }
                /**
                 * 正序
                 */
                1 -> {
                    normalTickList.sortByDescending { NCoinManager.showAnoterName(it) }
                    nameIndex = 2
                    iv_name_up?.imageResource = R.drawable.quotes_under_daytime

                }
                /**
                 * 倒序
                 */
                2 -> {
                    normalTickList.clear()
                    normalTickList.addAll(oriSymbols)
                    nameIndex = 0
                    iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                }
            }
            if (normalTickList.size > 0) {
                refreshAdapter(normalTickList)
            }

        }
        /**
         * 点击最新价
         */
        ll_new_price.setOnClickListener {
            refreshTransferImageView(1)
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
                    normalTickList.clear()
                    normalTickList.addAll(oriSymbols)
                    newPriceIndex = 0
                    iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime

                }
            }
            if (normalTickList.size > 0) {
                refreshAdapter(normalTickList)
            }
        }
        /**
         * 点击24小时涨幅
         */
        ll_limit.setOnClickListener {
            refreshTransferImageView(2)
            when (limitIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    normalTickList.sortBy { it.optDouble("rose") }
                    limitIndex = 1
                    iv_new_limit?.imageResource = R.drawable.quotes_up_daytime
                }
                /**
                 * 正序
                 */
                1 -> {
                    normalTickList.sortByDescending { it.optDouble("rose") }
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
            if (normalTickList.size > 0) {
                refreshAdapter(normalTickList)
            }
        }
        /**
         * 此处刷新
         */
        swipe_refresh?.setOnRefreshListener {
            isScrollStatus = true
            /**
             * 刷新数据操作
             */
            getOptionalSymbol()
            swipe_refresh?.isRefreshing = false
        }
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


    private fun getCollecData(): ArrayList<JSONObject>? {
        return HeYueLikeDataService.getInstance().getCollecData(false)
    }

    /*
     列表展示
     */
    private fun showData() {
        var collecData = getCollecData()
        if (null != collecData && collecData.size > 0) {
            serverOriDataList.clear()
            oriSymbols.clear()
            oriSymbols.addAll(collecData)
            reloadLocalTick(collecData)
            normalTickList.clear()
            normalTickList.addAll(collecData)

            showLikeView(true)
            adapter?.setList(normalTickList)
            initSocket()

        } else {
            showLikeView(false)
        }
    }

    /*
     * 自选币对View的显示与隐藏
     */
    private fun showLikeView(isShowLikeView: Boolean) {
        if (isShowLikeView) {
            ll_item_titles?.visibility = View.VISIBLE
            rv_market_detail?.visibility = View.VISIBLE
        } else {
            normalTickList.clear()
            ll_item_titles?.visibility = View.GONE
            adapter?.setList(normalTickList)
        }
    }


    private fun initAdapter() {
        adapter = MarketDetailAdapter()
        adapter?.isMarketLike = true
        rv_market_detail?.adapter = adapter
        rv_market_detail?.setHasFixedSize(true)
        val emptyForAdapterView = EmptyMarketForAdapterView(context ?: return)
        adapter?.setEmptyView(emptyForAdapterView)
        adapter?.emptyLayout?.findViewById<LinearLayout>(R.id.layout_add_like)?.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.HeYueMapActivity, Bundle())
        }

        adapter?.setOnItemClickListener { adapter, _, position ->
            val data = adapter.data[position] as JSONObject
            ArouterUtil.forwardKLine(data.optString("symbol"))

        }
        adapter?.setOnItemLongClickListener { adapter, view, position ->
            NewDialogUtils.showNormalDialog(context!!, LanguageUtil.getString(context, "new_confrim_likes"), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {

                    var symbol = normalTickList[position].optString("symbol")
                    if (isLogined && isOptionalSymbolServerOpen) {
                        var tempList = ArrayList<String>()
                        tempList.add(symbol)
                        operationType = 2
                        addOrDeleteSymbol(tempList)
                    } else {
                        removeLocalCollecta(symbol)
                    }
                }
            })
            true
        }
    }

    private fun removeLocalCollecta(symbol: String) {

        var newArray = LikeDataService.getInstance().removeCollect(symbol)
        if (null == newArray || newArray.size <= 0) {
            normalTickList.clear()
        } else {
            normalTickList = newArray
        }
        oriSymbols.clear()
        oriSymbols.addAll(normalTickList)
        LikeDataService.getInstance().removeCollect(symbol)
        if (oriSymbols.size != 0) {
            refreshAdapter()
        } else {
            showLikeView(false)
        }
//        mMarketWsData?.closeWS()
        Handler().postDelayed({
            initSocket()
        }, 200)
        NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(context, "kline_tip_removeCollectionSuccess"))
    }

    /**
     * 刷新
     */
    fun refreshAdapter() {
        refreshAdapter(normalTickList)
    }

    /*
     * 获取服务器用户自选数据
     * sync_status
     */
    fun getOptionalSymbol() {
        addDisposable(getMainModel().getOptionalSymbol(MyNDisposableObserver(null, getUserSelfDataReqType)))
    }


    val getUserSelfDataReqType = 2 // 服务器用户自选数据
    val addCancelUserSelfDataReqType = 3

    inner class MyNDisposableObserver(symbols: ArrayList<String>?, type: Int) : NDisposableObserver() {

        var msymbols = symbols
        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            //LogUtil.d("LikesFragment","onResponseSuccess==req_type is $req_type,jsonObject is &jsonObject ")
            closeLoadingDialog()
            if (getUserSelfDataReqType == req_type) {
                LogUtil.d("LikesFragment", "onResponseSuccess==req_type is $req_type,jsonObject is $jsonObject ")
                showServerSelfSymbols(jsonObject.optJSONObject("data"))
            } else if (addCancelUserSelfDataReqType == req_type) {
                if (0 == operationType) {
                    //showLoadingDialog()
                    //getOptionalSymbol()
                } else if (2 == operationType) {
                    if (null != msymbols && msymbols!!.size > 0) {
                        removeLocalCollecta(msymbols!![0])
                    }
                }
            }
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            closeLoadingDialog()
        }
    }

    /*
     * 显示服务器用户的自选币对数据
     */
    private fun showServerSelfSymbols(data: JSONObject?) {

        if (null == data || data.length() <= 0) {
            showData()
            return
        }

        var array = data.optJSONArray("symbols")
        var sync_status = data.optString("sync_status", "")

        if ("0".equals(sync_status)) {
            var array = LikeDataService.getInstance().symbols
            if (null != array && array.length() > 0) {
                var temps = ArrayList<String>()
                for (i in 0 until array.length()) {
                    temps.add(array.optString(i))
                }
                operationType = 0
                addOrDeleteSymbol(temps)
            }
        }

        if (null == array || array.length() <= 0) {
            showData()
            return
        }

        LikeDataService.getInstance().clearAllCollect()
        var tempList = ArrayList<JSONObject>()
        for (i in 0 until array.length()) {
            var symbol = array.optString(i)
            var symbolObj = NCoinManager.getSymbolObj(symbol)
            if (null != symbolObj && symbolObj.length() > 0) {
                LikeDataService.getInstance().saveCollecData(symbol, symbolObj)
                tempList.add(symbolObj)
            }
        }

        if (!tempList.isEmpty()) {

            serverOriDataList.clear()
            serverOriDataList.addAll(tempList)

            oriSymbols.clear()
            oriSymbols.addAll(tempList)
            reloadLocalTick(tempList)
            normalTickList.clear()
            normalTickList.addAll(tempList)
        }
        showLikeView(true)
        adapter?.setList(normalTickList)
        initSocket()
    }

    private fun refreshAdapter(list: ArrayList<JSONObject>) {
        adapter?.setList(list)
    }

    private fun initSocket() {
        pageEventSymbol()
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
    }

    var isLogined = false
    var isOptionalSymbolServerOpen = false
    override fun onResume() {
        super.onResume()
        isLogined = UserDataService.getInstance().isLogined
        if (isLogined) {
            isOptionalSymbolServerOpen = PublicInfoDataService.getInstance().isOptionalSymbolServerOpen(null)
        }
        if (isLogined && isOptionalSymbolServerOpen) {
            getOptionalSymbol()
        } else {
            showData()
        }
    }

    /**
     * 添加或者删除自选数据
     * @param operationType 标识 0(批量添加)/1(单个添加)/2(单个删除)
     * @param symbol 单个币对名称
     */
    var operationType = 0

    fun addOrDeleteSymbol(symbols: ArrayList<String>?) {
        if (isLogined && isOptionalSymbolServerOpen) {
            if (null == symbols || symbols.isEmpty())
                return
            addDisposable(getMainModel().addOrDeleteSymbol(operationType, symbols, MyNDisposableObserver(symbols, addCancelUserSelfDataReqType)))
        }
    }

    fun startInit() {
        Handler().postDelayed({
            pageEventSymbol()
        }, 200)
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
        var messageEvent = MessageEvent(MessageEvent.market_event_page_symbol_type)
        messageEvent.msg_content = hashMapOf("symbols" to coin, "bind" to isBind, "curIndex" to curIndex)
        EventBusUtil.post(messageEvent)
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

    private fun showWsData(jsonObject: JSONObject) {
        if (normalTickList.isEmpty())
            return
        if (rv_market_detail?.layoutManager == null) {
            return
        }
        val obj = SymbolWsData().getNewSymbolObj(normalTickList, jsonObject)
        val layoutManager = rv_market_detail?.layoutManager as LinearLayoutManager
        val firstView = layoutManager.findFirstVisibleItemPosition()
        val lastItem = layoutManager.findLastVisibleItemPosition()
        if (obj?.length() ?: 0 > 0) {
            val pos = normalTickList.indexOf(obj)
            if (pos >= 0) {
                val isRange = (firstView..lastItem).contains(pos)
                if (!isRange) return
                activity?.runOnUiThread {
                    if (obj != null) {
                        adapter?.setData(pos, obj)
                    }
                }
            }
        }
    }

    fun getCoins(): ArrayList<Any> {
        val items = arrayListOf<Any>()
        if (adapter != null && adapter?.data != null) {
            items.addAll(adapter?.data!!)
        }
        return items
    }

    fun handleData(items: HashMap<String, JSONObject>) {
        val data = adapter?.data
        if (data?.isEmpty()!!)
            return
        if (rv_market_detail?.layoutManager == null) {
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
                normalTickList.set(tempData, model)
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

    private fun reloadLocalTick(news: ArrayList<JSONObject>) {
        for (item in news) {
            val symbolLocal = normalTickList.findLast { it.optString("symbol") == item.optString("symbol") }
            if (symbolLocal != null){
                item.put("rose", symbolLocal.optString("rose"))
                item.put("close", symbolLocal.optString("close"))
                item.put("vol", symbolLocal.optString("vol"))
            }
        }
        if (newPriceIndex != 0 || limitIndex != 0 ||  nameIndex != 0) {
            if (newPriceIndex != 0) {
                when (newPriceIndex) {
                    1 -> {
                        news.sortBy { it.optDouble("close", 0.0) }
                    }
                    2 -> {
                        news.sortByDescending { it.optDouble("close", 0.0) }
                    }
                }
            } else if (nameIndex != 0) {
                when (nameIndex) {
                    1 -> {
                        news.sortBy { NCoinManager.showAnoterName(it) }
                    }
                    2 -> {
                        news.sortByDescending { NCoinManager.showAnoterName(it) }
                    }
                }
            } else if (limitIndex != 0) {
                when (limitIndex) {
                    1 -> {
                        news.sortBy { it.optDouble("rose", 0.0) }
                    }
                    2 -> {
                        news.sortByDescending { it.optDouble("rose", 0.0) }
                    }
                }
            }
        }
    }

}
