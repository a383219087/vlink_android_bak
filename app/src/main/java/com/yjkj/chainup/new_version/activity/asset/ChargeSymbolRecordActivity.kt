package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.activity.CoinActivity.Companion.SELECTED_TYPE
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.adapter.CashFlowAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_charge_symbol_record.*

/**
 * @Author lianshangljl
 * @Date 2019/5/15-7:26 PM
 * @Email buptjinlong@163.com
 * @description  充值记录
 */
const val RECHARGE_INDEX = 0
const val WITHDRAW_INDEX = 1
const val OTHER_INDEX = 2
const val OTC_TRANSFER = 3

class ChargeSymbolRecordActivity : NewBaseActivity() {


    var selectedCoin: String = ""
    var transactionScene: String = ""
    var startTime: String = ""
    var endTime: String = ""
    val adapter: CashFlowAdapter? = null
    var page: Int = 1
    var isScrollstatus = true

    companion object {
        fun enter2(context: Context) {
            context.startActivity(Intent(context, ChargeSymbolRecordActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_symbol_record)
        getData()
        initRefresh()
    }

    fun getData() {
        if (intent != null) {
            selectedCoin = intent.getStringExtra(CoinActivity.SELECTED_COIN) ?: ""
            transactionScene = intent.getStringExtra(SELECTED_TYPE) ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        if (UserDataService.getInstance().isLogined) {
            getOtherRecord(selectedCoin, transactionScene, startTime, endTime, true)
        }

    }

    var isfrist = true

    fun initRefresh() {
        spw_layout?.assetTopUpListener = object : ScreeningPopupWindowView.AssetTopUpListener {

            override fun returnAssetTopUpTime(startTime: String, end: String) {
                endTime = end
                this@ChargeSymbolRecordActivity.startTime = startTime
                page = 1
                getOtherRecord(selectedCoin, transactionScene, startTime, end, true)
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
                getOtherRecord(selectedCoin, transactionScene, startTime, endTime, true)
            }
        })


        recycler_view?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter?.itemCount && isScrollstatus) {
                    page += 1
                    getOtherRecord(selectedCoin, transactionScene, startTime, endTime, false)
                }
            }

        })
    }

    var list: ArrayList<CashFlowBean.Finance> = arrayListOf()

    fun refreshView(bean: ArrayList<CashFlowBean.Finance>) {
        list.addAll(bean)
        adapter?.notifyDataSetChanged()
    }

    fun initView() {
        if (recycler_view != null) {
            recycler_view.layoutManager = LinearLayoutManager(this@ChargeSymbolRecordActivity)
            val adapter = CashFlowAdapter(list)
            adapter.setEmptyView(EmptyForAdapterView(context ?: return))
            adapter.index = OTHER_INDEX
            recycler_view.adapter = adapter
        }
    }

    /**
     * 获取其它记录
     */
    fun getOtherRecord(symbol: String, transactionScene: String, startTime: String, endTime: String, refresh: Boolean) {
        showProgressDialog()
        HttpClient.instance.otherTransList4V2(symbol, transactionScene, startTime, endTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CashFlowBean>() {
                    override fun onHandleSuccess(t: CashFlowBean?) {
                        cancelProgressDialog()
                        if (t?.financeList != null) {
                            if (t.financeList.size < 20) {
                                isScrollstatus = false
                            }
                            if (refresh) {
                                list.clear()
                                list.addAll(t.financeList as ArrayList<CashFlowBean.Finance>)
                                initView()
                            } else {
                                refreshView(t.financeList as ArrayList<CashFlowBean.Finance>)
                            }
                        }
                        swipe_refresh?.isRefreshing = false
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        swipe_refresh?.isRefreshing = false
                        NToastUtil.showTopToastNet(this@ChargeSymbolRecordActivity, false, msg)
                    }
                })
    }
}