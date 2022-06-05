package com.chainup.contract.ui.fragment

import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.chainup.contract.view.CpSlDialogHelper
import com.chainup.talkingdata.AppAnalyticsExt
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.yjkj.chainup.new_contract.adapter.CpContractHistoricalPositionAdapter
import kotlinx.android.synthetic.main.cp_activity_contract_entrust.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.rv_hold_contract
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 合约 当前委托
 */
class CpContractPLRecordFragment : CpNBaseFragment() {

    private var adapter: CpContractHistoricalPositionAdapter? = null
    private var mList = ArrayList<JSONObject>()

    override fun setContentView(): Int {
        return R.layout.cp_contract_common_entrust
    }

    var mContractId = CpContractEntrustNewActivity.mContractId
    var mOrderType = 0
    var mOrderSide = ""
    var isCommonEntrust = true
    var isCurrentEntrust = true

    internal class PageInfo {
        var page = 1
        fun nextPage() {
            page++
        }

        fun reset() {
            page = 1
        }

        val isFirstPage: Boolean
            get() = page == 1
    }

    private val pageInfo = PageInfo()


    override fun initView() {
        mContractId = CpContractEntrustNewActivity.mContractId
        LogUtils.e("--------------------++++"+ CpContractEntrustNewActivity.mContractId)
        adapter = CpContractHistoricalPositionAdapter(this.activity!!, mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_settled_profit_loss_key)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val obj: JSONObject = adapter?.getItem(position) as JSONObject
            obj.put("marginCoin", CpClLogicContractSetting.getContractMarginCoinById(activity, mContractId))
            obj.put("marginCoinPrecision", CpClLogicContractSetting.getContractMarginCoinPrecisionById(activity, mContractId))
            obj?.let { activity?.let { it1 -> CpSlDialogHelper.showProfitLossDetailsDialog(it1, it,1) } }

        }
        swipe_refresh?.setOnRefreshListener {
            pageInfo.reset()
            getOrderList()
        }
        adapter?.loadMoreModule?.apply {
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    getOrderList()
                }
            })
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = false
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_record_switch_contract_event -> {
                mContractId = event.msg_content as Int
                LogUtils.e("--------------------"+mContractId)
                pageInfo.reset()
                getOrderList()
            }
            CpMessageEvent.sl_contract_record_switch_contract_side_event -> {
                mOrderSide = event.msg_content as String
                pageInfo.reset()
                getOrderList()
            }
            CpMessageEvent.sl_contract_record_switch_tab_event -> {
                if (event.msg_content as Int ==2){
                    pageInfo.reset()
                    getOrderList()
                }
            }

        }
    }

    fun getOrderList() {
        LogUtils.e("--------------------"+mContractId)
        var symbolName = CpClLogicContractSetting.getContractShowNameById(this.activity, mContractId)
        var mMarginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(this.activity, mContractId)
        addDisposable(
                getContractModel().getHistoryPositionList(mContractId.toString(),
                        pageInfo.page.toString(),
                        mOrderSide,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                val mListBuffer = ArrayList<JSONObject>()
                                swipe_refresh?.isRefreshing = false
                                jsonObject.optJSONObject("data").run {
                                    if (!isNull("positionList")) {
                                        val mOrderListJson = optJSONArray("positionList")
                                        for (i in 0..(mOrderListJson.length() - 1)) {
                                            var obj: JSONObject = mOrderListJson.get(i) as JSONObject
                                            obj.put("symbol", symbolName)
                                            obj.put("marginCoinPrecision", mMarginCoinPrecision)
                                            mListBuffer.add(obj)
                                        }
                                    }
                                }
                                if (pageInfo.isFirstPage) {
                                    adapter?.setList(mListBuffer)
                                } else {
                                    adapter?.addData(mListBuffer)
                                }
                                if (mListBuffer.size < 20) {
                                    adapter?.loadMoreModule?.loadMoreEnd()
                                } else {
                                    adapter?.loadMoreModule?.loadMoreComplete()
                                }
                                pageInfo.nextPage()
                                closeLoadingDialog()
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                swipe_refresh?.isRefreshing = false
                                closeLoadingDialog()
                            }
                        })
        )
    }
    override fun onResume() {
        super.onResume()
        AppAnalyticsExt.instance.activityStart(AppAnalyticsExt.CONTRACT_APP_PAGE_2)
    }

    override fun onPause() {
        super.onPause()
        if(!this.isHidden()){
            AppAnalyticsExt.instance.activityStop(AppAnalyticsExt.CONTRACT_APP_PAGE_2)
        }
    }


}