package com.yjkj.chainup.new_version.activity.otcTrading

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.OTCOrderBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.adapter.NewVersionOTCOrderAdapter
import com.yjkj.chainup.new_version.fragment.TRADING_TYPE_BUY
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_otc_orders.*

/**
 * @Author lianshangljl
 * @Date 2019/4/9-11:34 AM
 * @Email buptjinlong@163.com
 * @description 我的订单
 */
@Route(path = RoutePath.NewOTCOrdersActivity)
class NewOTCOrdersActivity : NewBaseActivity() {
    var status = ""
    var adapter: NewVersionOTCOrderAdapter? = null
    var payCoinNow = ""
    var coinSymbolNow = ""
    var orderStatusNow = ""
    var startTimeNow = ""
    var tradingNew = ""
    var endNow = ""

    var isScrollstatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_otc_orders)
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout.slidingShowTitle(status)
            }
        }
        initRefresh()
        setTextContent()
    }

    fun setTextContent() {
        title_layout?.setContentTitle(getStringContent("otc_text_myOrder"))
    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    override fun onResume() {
        super.onResume()
        getData(true, payCoinNow, startTimeNow, endNow, coinSymbolNow, tradingNew)

    }

    var page: Int = 1
    var pageSize: Int = 20
    var isfrist = true

    fun initRefresh() {
        spw_layout.otcOrderListener = object : ScreeningPopupWindowView.OTCOrderListener {
            override fun returnScreeningOrderStatus(trading: String, payCoin: String, coinSymbol: String, orderStatus: String, startTime: String, end: String) {
                page = 1
                payCoinNow = payCoin
                coinSymbolNow = coinSymbol
                orderStatusNow = orderStatus
                tradingNew = trading
                startTimeNow = startTime
                endNow = end
                getData(true, payCoin, startTime, end, coinSymbol, trading)
            }

        }
        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (spw_layout?.visibility == View.VISIBLE) {
                    spw_layout?.visibility = View.GONE
                } else {
                    spw_layout?.visibility = View.VISIBLE
                    if (isfrist) {
                        isfrist = false
                        spw_layout?.setMage()
                    }

                }
            }

            override fun onclickName() {
            }

        }

        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                page = 1
                isScrollstatus = true
                getData(true, payCoinNow, startTimeNow, endNow, coinSymbolNow, tradingNew)
            }
        })


        rv_order_otc?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter?.itemCount && isScrollstatus) {
                    page += 1
                    getData(false, payCoinNow, startTimeNow, endNow, coinSymbolNow, tradingNew)
                }
            }

        })
    }

    var list: ArrayList<OTCOrderBean.Order> = arrayListOf()

    fun refreshView(bean: ArrayList<OTCOrderBean.Order>) {
        list.addAll(bean)
        adapter?.notifyDataSetChanged()
    }

    fun initView(t: ArrayList<OTCOrderBean.Order>) {
        adapter = NewVersionOTCOrderAdapter(list)
        if (rv_order_otc != null) {
            rv_order_otc.layoutManager = LinearLayoutManager(this)
            adapter?.setEmptyView(EmptyForAdapterView(this))
            rv_order_otc.adapter = adapter
            adapter?.setOnItemClickListener { adapter, view, position ->

                var item = t[position]

                /**
                 * 根据订单状态
                 * status：订单状态 待支付1 已支付2 交易成功3 取消 4 申诉 5 打币中6 异常订单7 申诉处理结束8
                 */
                when (item.status) {
                    3 -> {
                        if (item.side == TRADING_TYPE_BUY) {
                            NewVersionBuyOrderActivity.enter2(this, item.sequence)
                        } else {
                            NewVersionSellOrderActivity.enter2(this, item.sequence)
                        }
                    }


                    4 -> {
                        if (item.side == TRADING_TYPE_BUY) {
                            NewVersionBuyOrderActivity.enter2(this, item.sequence)
                        } else {
                            NewVersionSellOrderActivity.enter2(this, item.sequence)
                        }
                    }

                    else -> {
                        if (item.side == TRADING_TYPE_BUY) {
                            /**
                             * 买
                             */
                            NewVersionBuyOrderActivity.enter2(this, orderId = item.sequence)
                        } else {
                            /**
                             * 卖
                             */
                            NewVersionSellOrderActivity.enter2(this, orderId = item.sequence)

                        }
                    }
                }
            }
        }

    }


    fun getData(refresh: Boolean, payCoin: String = "", startTime: String = "", endTime: String = "", coinSymbol: String = "", tradeType: String = "") {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        HttpClient.instance.byStatus4OTC(orderStatusNow, payCoin, startTime, endTime, pageSize, page, NCoinManager.setShowNameGetName(coinSymbol), tradeType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderBean>() {
                    override fun onHandleSuccess(t: OTCOrderBean?) {
                        t ?: return


                        if (t.orderList.size < 20) {
                            isScrollstatus = false
                        }
                        if (refresh) {
                            list.clear()
                            list = t.orderList
                            initView(list)
                        } else {
                            refreshView(t.orderList)

                        }

                        swipe_refresh?.isRefreshing = false
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        swipe_refresh?.isRefreshing = false
                    }

                })
    }

}