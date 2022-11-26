package com.chainup.contract.ui.fragment

import android.annotation.SuppressLint
import android.view.View
import com.chainup.contract.R
import com.chainup.contract.adapter.CpContractCurrentEntrustNewAdapter
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.view.CpEmptyOrderForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.utils.CpPreferenceManager
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_entruset.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 合约 当前委托
 */
class CpContractCurrentEntrustNewFragment : CpNBaseFragment() {

    private var adapter: CpContractCurrentEntrustNewAdapter? = null
    private var mList = ArrayList<CpCurrentOrderBean>()
    private var mAllList = ArrayList<CpCurrentOrderBean>()

    //是否显示全部合约
    private var showAll = true

    //合约id
    var mContractId = "-1"

    override fun setContentView(): Int {
        return R.layout.cp_fragment_cl_contract_entruset
    }

    override fun initView() {
        showSwitch()
        initOnClick()
        adapter = CpContractCurrentEntrustNewAdapter(this.activity!!, mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyOrderForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_cancel)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as CpCurrentOrderBean
            cancelOrder(item.contractId, item.id, false)
        }

    }

    //更新是否显示全部的是UI
    private fun showSwitch() {
        showAll =
            CpPreferenceManager.getBoolean(activity!!, CpPreferenceManager.isShowAllContractEntrust, true)
        if (showAll) {
            img_switch.visibility = View.VISIBLE
            img_not_switch.visibility = View.GONE
        } else {
            img_switch.visibility = View.GONE
            img_not_switch.visibility = View.VISIBLE
        }
        updateAdapter()

    }

    private fun initOnClick() {
        //选中切换成未选中
        img_switch.setOnClickListener {
            CpPreferenceManager.putBoolean(activity!!, CpPreferenceManager.isShowAllContractEntrust, false)
            showSwitch()
        }
        //未选中切换成选中
        img_not_switch.setOnClickListener {
            CpPreferenceManager.putBoolean(activity!!, CpPreferenceManager.isShowAllContractEntrust, true)
            showSwitch()
        }

    }

    //更新列表
    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        if (mAllList.isEmpty()) {
            mList.clear()
            adapter?.setList(null)
            adapter?.notifyDataSetChanged()
            return
        }
        if (showAll) {
            mList = mAllList
        } else {
            mList.clear()
            for (i in 0 until mAllList.size) {
                if (mAllList[i].contractId == mContractId) {
                    mList.add(mAllList[i])
                }
            }
        }
        adapter?.setList(mList)
    }


    private fun cancelOrder(mContractId: String, orderId: String, isConditionOrder: Boolean) {
        addDisposable(
                getContractModel().orderCancel(mContractId, orderId,
                        isConditionOrder,
                        consumer = object : CpNDisposableObserver(activity,true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_req_current_entrust_list_event))
                            }
                        })
        )
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_refresh_current_entrust_list_event -> {
                val mPositionObj = event.msg_content as JSONObject
                val mListBuffer = ArrayList<CpCurrentOrderBean>()
                mPositionObj.apply {
                    if (!isNull("orderList")) {
                        val mOrderListJson = optJSONArray("orderList")
                        for (i in 0..(mOrderListJson.length() - 1)) {
                            var obj = mOrderListJson.getString(i)
                            val mClCurrentOrderBean =
                                    Gson().fromJson<CpCurrentOrderBean>(
                                            obj,
                                            CpCurrentOrderBean::class.java
                                    )
                            mClCurrentOrderBean.isPlan = false
                            mListBuffer.add(mClCurrentOrderBean)
                        }
                    }
                    mAllList=mListBuffer
                    updateAdapter()
                }
            }
            CpMessageEvent.sl_contract_logout_event -> {
                mAllList.clear()
                updateAdapter()
            }

            //合约id有更新要重新筛选数组列表
            CpMessageEvent.sl_contract_calc_switch_contract_id -> {
                val id = event.msg_content as Int
                if (mContractId != id.toString()) {
                    mContractId = id.toString()
                    updateAdapter()
                }

            }
        }
    }


}