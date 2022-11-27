package com.chainup.contract.ui.fragment

import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.CpContractPlanEntrustNewAdapter
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class CpContractPlanEntrustNewFragment : CpNBaseFragment() {

    private var adapter: CpContractPlanEntrustNewAdapter? = null
    private var mList = ArrayList<CpCurrentOrderBean>()

    override fun setContentView(): Int {
        return R.layout.cp_fragment_cl_contract_hold
    }

    override fun initView() {
        adapter = CpContractPlanEntrustNewAdapter(this.activity!!,mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_cancel)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as CpCurrentOrderBean
            cancelOrder(item.contractId, item.id, true)
        }
    }

    private fun cancelOrder(mContractId: String, orderId: String, isConditionOrder: Boolean) {
        addDisposable(
            getContractModel().orderCancel(mContractId, orderId,
                isConditionOrder,
                consumer = object : CpNDisposableObserver(activity,true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_req_plan_entrust_list_event))
                    }
                })
        )
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_refresh_plan_entrust_list_event -> {
                val mPositionObj = event.msg_content as JSONObject
                val mListBuffer = ArrayList<CpCurrentOrderBean>()
                mPositionObj.apply {
                    if (!isNull("trigOrderList")) {
                        val mOrderListJson = optJSONArray("trigOrderList")
                        for (i in 0..(mOrderListJson.length() - 1)) {
                            var obj = mOrderListJson.getString(i)
                            val mClCurrentOrderBean =
                                Gson().fromJson<CpCurrentOrderBean>(
                                    obj,
                                    CpCurrentOrderBean::class.java
                                )
                            mClCurrentOrderBean.isPlan = true
                            mListBuffer.add(mClCurrentOrderBean)
                        }
                    }
                    adapter?.setList(mListBuffer)
                }
            }
            CpMessageEvent.sl_contract_logout_event->{
                mList.clear()
                adapter?.notifyDataSetChanged()
            }
        }
    }


}
