package com.yjkj.chainup.new_version.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NHistoryEntrustAdapter
import com.yjkj.chainup.new_version.view.HistoryScreeningControl
import com.yjkj.chainup.util.BigDecimalUtils
import kotlinx.android.synthetic.main.activity_history_entrust.*
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/8-10:26 AM
 * @Description: 历史委托
 */
class HistoryEntrustFragment : NBaseFragment(), HistoryScreeningListener {
    override fun ConfirmationScreen(status: Boolean, statusType: Int, symbolCoin: String, symbolAndUnit: String, tradingType: Int, priceType: Int, begin: String, end: String) {
        var activity = activity

        if (activity != null && !activity.isFinishing) {
            if (activity is EntrustActivity) {

                if (!TextUtils.isEmpty(symbolCoin) || !TextUtils.isEmpty(symbolAndUnit)) {
                    ll_tip_layout?.visibility = View.GONE
                } else {
                    ll_tip_layout?.visibility = View.VISIBLE
                }
                var trading = ""
                page = 1
                if (status) {
                    isShowCanceled = "1"
                } else {
                    isShowCanceled = "0"
                }
                isScrollStatus = true
                when (tradingType) {
                    1 -> {
                        trading = "BUY"
                    }
                    2 -> {
                        trading = "SELL"

                    }
                }
                this.statusType = statusType
                side = trading
                if (symbolCoin.isNotEmpty()){
                    symbol = NCoinManager.setShowNameGetName(symbolCoin) + NCoinManager.setShowNameGetName(symbolAndUnit)
                }
                if (priceType == 0) {
                    type = ""
                } else {
                    type = priceType.toString()
                }
                startTime = begin
                endTime = end
                if (activity.currentItem == 1) {
                    if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                        getNewHistoryEntrust(true)
                    } else {
                        getHistoryEntrust(true)
                    }
                }
            }
        }
    }


    fun initData() {
        page = 1
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            ll_tip_layout?.visibility = View.VISIBLE
            getNewHistoryEntrust(true)
        } else {
            symbol = if (orderType == ParamConstant.LEVER_INDEX) {
                PublicInfoDataService.getInstance().currentSymbol4Lever
            } else {
                PublicInfoDataService.getInstance().currentSymbol
            }
            ll_tip_layout?.visibility = View.GONE
            getHistoryEntrust(true)
        }
    }


    override fun setContentView() = R.layout.activity_history_entrust
    var orderList = ArrayList<JSONObject>()
    var coinList = arrayListOf<String>()

    companion object {

        @JvmStatic
        fun newInstance(orderType: String) =
                HistoryEntrustFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.TYPE, orderType)

                    }
                }
    }

    override fun loadData() {
        arguments.let {
            orderType = it?.getString(ParamConstant.TYPE, ParamConstant.BIBI_INDEX)
                    ?: ParamConstant.BIBI_INDEX
        }
    }

    var orderType = ParamConstant.BIBI_INDEX


    var isShowCanceled = "1"
    var side = ""
    var type = ""
    var startTime = ""
    var endTime = ""
    var symbol = ""
    val pageSize = 20
    var page = 1
    var statusType = 0

    var isScrollStatus = true

    override fun initView() {
        HistoryScreeningControl.getInstance().addListener(this)
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            ll_tip_layout?.visibility = View.VISIBLE
        } else {
            ll_tip_layout?.visibility = View.GONE
        }
        initAdapter()
        tv_tip_title?.text = LanguageUtil.getString(mActivity, "common_text_tip")
        tv_tip_content?.text = LanguageUtil.getString(mActivity, "common_text_entrustListLimit")
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                getNewHistoryEntrust(true)
            } else {
                getHistoryEntrust(true)
            }
        }


        /**
         * 这里是滑动加载
         */
        rv_history_entrust?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_SETTLING && lastVisibleItem + 1 == historyAdapter?.itemCount && isScrollStatus) {
                    page += 1
                    if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                        getNewHistoryEntrust(false)
                    } else {
                        getHistoryEntrust(false)
                    }
                }
            }
        })
        initData()
    }


    var historyAdapter = NHistoryEntrustAdapter(orderList)

    fun initAdapter() {

        rv_history_entrust?.layoutManager = LinearLayoutManager(mActivity)
        rv_history_entrust?.adapter = historyAdapter
        historyAdapter.setOnItemClickListener { adapter, view, position ->

            if (adapter?.data?.isNotEmpty() == true) {
                try {
                    (adapter.data[position] as JSONObject?)?.run {
                        if (optString("status") == "2" || optString("status") == "3" || (optString("status") == "4" && BigDecimalUtils.compareTo(optString("deal_volume"), "0") == 1)) {
                            ArouterUtil.greenChannel(RoutePath.EntrustDetialsActivity, Bundle().apply {
                                putString(ParamConstant.DEAL_VOLUME, optString("deal_volume", ""))

                                putString(ParamConstant.HISTORY_SIDE, optString("side", ""))

                                putString(ParamConstant.AVG_PRICE, optString("avg_price", ""))

                                putString(ParamConstant.DEAL_MONEY, optString("deal_money", ""))

                                putString(ParamConstant.ENTRUST_ID, optString("id", ""))

                                putString(ParamConstant.TYPE, orderType)
                                putString(ParamConstant.BASE_COIN, optString("baseCoin"))
                                putString(ParamConstant.COUNT_COIN, optString("countCoin"))
                                putString(ParamConstant.symbol, symbol)

                            })
                        }
                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }


        }
    }


    /**
     * 获取历史委托
     * @param symbol
     * @param pageSize default 10
     * @param page  default 1
     * @param isShowCanceled 是否展示已取消的订单，0表示不展示，1表示展示，默认1
     * @param side 订单买卖方向，BUY买入 SELL卖出 ,不传全部
     * @param type 委托类型：1 limit，2 market ,不传全部
     * @param startTime 年月日，禁止输入时分秒：2019-04-22
     * @param endTime 年月日，禁止输入时分秒：2019-04-22
     */
    private fun getHistoryEntrust(refresh: Boolean) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        var statusTypeStr=""
        if (statusType == 0){
            statusTypeStr=""
        }else if (statusType == 1){
            statusTypeStr="2"
        }else if (statusType == 2){
            statusTypeStr="4"
        }else{
            statusTypeStr=""
        }
        addDisposable(MainModel().getHistoryEntrust4(symbol = symbol,
                page = page.toString(),
                pageSize = "20",
                isShowCanceled = isShowCanceled,
                side = side,
                type = type,
                statusType = statusTypeStr,
                isLever = (orderType == ParamConstant.LEVER_INDEX),
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        try {

                            var activity = activity

                            if (activity != null && !activity.isFinishing) {
                                if (activity is EntrustActivity) {
                                    if (activity.currentItem == 1) {
                                        swipe_refresh?.isRefreshing = false
                                        jsonObject.optJSONObject("data")?.run {
                                            val orderJsonArray = optJSONArray("orderList")
                                            var entrustActivity = activity
                                            if (orderJsonArray != null && orderJsonArray.length() != 0) {
                                                if (refresh) {
                                                    orderList.clear()
                                                    var list = JSONUtil.arrayToList(orderJsonArray)
                                                    if (null != list) {
                                                        orderList.addAll(list)
                                                    }
                                                } else {
                                                    if (orderJsonArray?.length() ?: 0 < 20) {
                                                        isScrollStatus = false
                                                    }
                                                    var list = JSONUtil.arrayToList(orderJsonArray)
                                                    if (null != list) {
                                                        orderList.addAll(list)
                                                    }
                                                }
                                                historyAdapter?.setList(orderList)
                                            }
                                            if ((orderJsonArray == null || orderJsonArray.length() == 0) && refresh) {
                                                orderList.clear()
                                                historyAdapter?.setList(orderList)
                                            }

                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        try {

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        swipe_refresh?.isRefreshing = false
                        orderList.clear()
                        historyAdapter?.setList(orderList)
                    }
                }))
    }


    /**
     * 新版本历史委托
     * @param entrust 1:当前委托、2：历史委托
     * @param side  BUY：买、SELL：卖fFfFUuPp
     * @param symbol 币对
     * @param orderType 订单类型1:常规订单，2 杠杆订单
     * @param status  订单状态：1 新订单，2 已完成，3 部分成交，4 已取消，5 待撤销，6 异常单
     * @param isShowCanceled 0:不展示已撤单、其余默认展示已撤单
     * @param quote 所在交易区（usdt。。。）
     * @param page 分页
     * @param pageSize 页面大小
     */
    private fun getNewHistoryEntrust(refresh: Boolean) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }

        addDisposable(getMainModel().getNewEntrustSearch(side, symbol, isShowCanceled, statusType, type, page.toString(), pageSize.toString(), (orderType == ParamConstant.LEVER_INDEX), ParamConstant.HISTORY_ENTURST, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                try {

                    var activity = activity

                    if (activity != null && !activity.isFinishing) {
                        if (activity is EntrustActivity) {
                            if (activity.currentItem == 1) {
                                var t = jsonObject.optJSONObject("data")
                                Log.d(TAG, "======获新取历史委托：=====" + t.toString())
                                swipe_refresh?.isRefreshing = false

                                var ordersList = t?.optJSONArray("orders")
                                var entrustActivity = activity
                                if (ordersList != null && ordersList.length() > 0) {
                                    if (refresh) {
                                        orderList?.clear()
                                        var list = JSONUtil.arrayToList(ordersList)
                                        if (list != null) {
                                            orderList?.addAll(list)
                                        }
                                    } else {
                                        if (ordersList.length() < 20) {
                                            isScrollStatus = false
                                        }
                                        var list = JSONUtil.arrayToList(ordersList)
                                        if (list != null) {
                                            orderList?.addAll(list)
                                        }
                                    }
                                    historyAdapter?.setList(orderList)
                                } else {
                                    if (refresh) {
                                        orderList.clear()
                                        historyAdapter.setList(orderList)
                                        historyAdapter.setEmptyView(R.layout.item_new_empty)
                                    }
                                }
                            }
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                try {
                    if (isResumed) {
                        swipe_refresh?.isRefreshing = false
                        orderList.clear()
                        historyAdapter?.setList(orderList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }))
    }


}
