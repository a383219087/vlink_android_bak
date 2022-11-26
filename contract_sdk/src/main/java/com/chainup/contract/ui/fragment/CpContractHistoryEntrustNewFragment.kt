package com.chainup.contract.ui.fragment

import android.text.TextUtils
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chainup.contract.R
import com.chainup.contract.adapter.CpContractEntrustNewAdapter
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.chainup.contract.utils.CpClickUtil
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.chainup.contract.view.CpNewDialogUtils
import com.google.gson.Gson
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.activity.CpContractEntrustDetailActivity
import com.chainup.contract.bean.CpCurrentOrderBean
import kotlinx.android.synthetic.main.cp_activity_contract_entrust.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.rv_hold_contract
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 合约 历史委托
 */
class CpContractHistoryEntrustNewFragment : CpNBaseFragment() {

    private var adapter: CpContractEntrustNewAdapter? = null
    private var mList = ArrayList<CpCurrentOrderBean>()

    override fun setContentView(): Int {
        return R.layout.cp_contract_common_entrust
    }

    var mContractId = CpContractEntrustNewActivity.mContractId
    var mOrderType = 0
    var isCommonEntrust = true
    var isCurrentEntrust = true

    var isConditionOrder = false

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
        adapter = CpContractEntrustNewAdapter(this.activity!!, mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_cancel, R.id.tv_status_go, R.id.tv_order_type)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            if (CpClickUtil.isFastDoubleClick()) return@setOnItemChildClickListener
            val item = adapter.data[position] as CpCurrentOrderBean
            if (view.id == R.id.tv_cancel) {
                cancelOrder(item.contractId, item.id, !isCommonEntrust)
            } else if (view.id == R.id.tv_status_go) {
                CpContractEntrustDetailActivity.show(mActivity!!, item)
            }else if (view.id == R.id.tv_order_type) {
                if (item.type.equals("6")){
                    var tip = item?.liqPositionMsg.toString()
                    if (TextUtils.isEmpty(tip)){
                        tip=""
                    }
                    if (tip.contains("\\n")){
                        tip = tip.replace("\\n", "<br />")
                    }
                    CpNewDialogUtils.showDialog(mActivity!!, tip, true, null, getString(R.string.cp_extra_text80), getString(R.string.cp_extra_text28))
                }
            }
        }
        adapter?.setOnItemClickListener { adapter, view, position ->
            if (CpClickUtil.isFastDoubleClick()) return@setOnItemClickListener
            val item = adapter.data[position] as CpCurrentOrderBean
            if (isCommonEntrust){
                CpContractEntrustDetailActivity.show(mActivity!!, item)
            }
        }
        getOrderList()
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

    private fun cancelOrder(mContractId: String, orderId: String, isConditionOrder: Boolean) {
        addDisposable(
                getContractModel().orderCancel(mContractId, orderId,
                        isConditionOrder,
                        consumer = object : CpNDisposableObserver(activity,true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                pageInfo.reset()
                                getOrderList()
                            }
                        })
        )
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_record_switch_contract_event -> {
                mContractId = event.msg_content as Int
                pageInfo.reset()
                getOrderList()
            }
            CpMessageEvent.sl_contract_record_switch_entrust_type_event -> {
                isCommonEntrust = event.msg_content as Boolean
                pageInfo.reset()
                getOrderList()
            }
            CpMessageEvent.sl_contract_record_switch_order_type_event -> {
                mOrderType = event.msg_content as Int
                pageInfo.reset()
                getOrderList()
            }
            CpMessageEvent.sl_contract_record_switch_tab_event -> {
              if (event.msg_content as Int ==1){
                  pageInfo.reset()
                  getOrderList()
              }
            }
        }
    }

    private fun getOrderList() {
        getHistoryOrderList()

    }



    private fun getHistoryOrderList() {
        if (isCommonEntrust) {
            getHistoryCommonOrderList()
        } else {
            getHistoryPlanOrderList()
        }
    }


    private fun getHistoryCommonOrderList() {
        addDisposable(
                getContractModel().getHistoryOrderList(mContractId.toString(),
                        mOrderType,
                        pageInfo.page,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                val mListBuffer = ArrayList<CpCurrentOrderBean>()
                                swipe_refresh?.isRefreshing = false
                                jsonObject.optJSONObject("data").run {
                                    if (!isNull("orderList")) {
                                        val mOrderListJson = optJSONArray("orderList")
                                        for (i in 0..(mOrderListJson.length() - 1)) {
                                            var obj = mOrderListJson.getString(i)
                                            val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                                    obj,
                                                    CpCurrentOrderBean::class.java
                                            )
                                            mClCurrentOrderBean.layoutType = 3
                                            mClCurrentOrderBean.isPlan = false
                                            mListBuffer.add(mClCurrentOrderBean)
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

    private fun getHistoryPlanOrderList() {
        addDisposable(
                getContractModel().getHistoryPlanOrderList(mContractId.toString(),
                        mOrderType,
                        pageInfo.page,
                        consumer = object : CpNDisposableObserver(
                                true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                val mListBuffer = ArrayList<CpCurrentOrderBean>()
                                swipe_refresh?.isRefreshing = false
                                jsonObject.optJSONObject("data").run {
                                    if (!isNull("trigOrderList")) {
                                        val mOrderListJson = optJSONArray("trigOrderList")
                                        for (i in 0..(mOrderListJson.length() - 1)) {
                                            var obj = mOrderListJson.getString(i)
                                            val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                                    obj,
                                                    CpCurrentOrderBean::class.java
                                            )
                                            mClCurrentOrderBean.layoutType = 4
                                            mClCurrentOrderBean.isPlan = true
                                            mListBuffer.add(mClCurrentOrderBean)
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




}