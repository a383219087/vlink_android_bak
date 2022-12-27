package com.chainup.contract.ui.fragment

import android.annotation.SuppressLint
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.chainup.contract.adapter.CpContractPlanEntrustNewAdapter
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.CpNToastUtil
import com.chainup.contract.utils.CpPreferenceManager
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpNewDialogUtils
import com.timmy.tdialog.TDialog
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.rv_hold_contract
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.tv_confirm_btn
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold.tv_show_all
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class CpContractPlanEntrustNewFragment : CpNBaseFragment() {

    private var adapter: CpContractPlanEntrustNewAdapter? = null
    private var mList = ArrayList<CpCurrentOrderBean>()
    private var mAllList = ArrayList<CpCurrentOrderBean>()

    //是否显示全部合约
    private var showAll = 0

    override fun setContentView(): Int {
        return R.layout.cp_fragment_cl_contract_hold
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        showSwitch()
        initOnClick()
        adapter = CpContractPlanEntrustNewAdapter(this.activity!!,mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        rv_hold_contract?.isNestedScrollingEnabled = false
        adapter?.setEmptyView(CpEmptyForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_cancel)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val item = adapter.data[position] as CpCurrentOrderBean
            cancelOrder(item.contractId, item.id, true)
        }
    }


    //更新是否显示全部的是UI
    private fun showSwitch() {
        showAll =
            CpPreferenceManager.getInt(activity!!, CpPreferenceManager.isShowAllContractPlan, 0)

        updateAdapter()

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
        when (showAll) {
            0 -> {
                tv_show_all.text=context?.getString(R.string.cp_extra_text_hold1)
                mList = mAllList
            }
            1 -> {
                tv_show_all.text=context?.getString(R.string.cp_extra_text_hold11)
                mList.clear()
                for (i in 0 until mAllList.size) {
                    if (mAllList[i].side == "BUY") {
                        mList.add(mAllList[i])
                    }
                }
            }
            else -> {
                tv_show_all.text=context?.getString(R.string.cp_extra_text_hold12)
                mList.clear()
                for (i in 0 until mAllList.size) {
                    if (mAllList[i].side == "SELL") {
                        mList.add(mAllList[i])
                    }
                }
            }
        }
        adapter?.setList(mList)
    }
    var showTDialog:  TDialog?  =null
    private fun initOnClick() {
        //选择
        tv_show_all.setOnClickListener {
            val typeList = ArrayList<CpTabInfo>()
            typeList.add(CpTabInfo(getString(R.string.cp_extra_text_hold1), 0,extras=0))
            typeList.add(CpTabInfo(getString(R.string.cp_extra_text_hold11), 1,extras=1))
            typeList.add(CpTabInfo(getString(R.string.cp_extra_text_hold12), 2,extras=2))
            showTDialog?.dismiss()
            showTDialog=  CpDialogUtil.showNewListDialog(context!!, typeList, showAll, object : CpNewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(position: Int) {
                    showTDialog?.dismiss()
                    CpPreferenceManager.putInt(activity!!, CpPreferenceManager.isShowAllContractPlan, position)
                    showSwitch()
                }
            })

        }
        //一键撤销
        tv_confirm_btn.setOnClickListener {
            CpDialogUtil.showNewDoubleDialog(
                context!!, context!!.getString(R.string.cp_extra_text_hold4),
                object : CpDialogUtil.DialogBottomListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun sendConfirm() {
                        if (mList.isEmpty()) {
                            return
                        }
                        for (i in 0 until mList.size) {
                            val item = mList[i]
                            cancelOrder(item.contractId, item.id, false)
                        }



                    }

                }

            )


        }

    }

    private fun cancelOrder(mContractId: String, orderId: String, isConditionOrder: Boolean) {
        addDisposable(
            getContractModel().orderCancel(mContractId, orderId,
                isConditionOrder,
                consumer = object : CpNDisposableObserver(activity,true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {

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
                    mAllList=mListBuffer
                    updateAdapter()
                }
            }
            CpMessageEvent.sl_contract_clear_event -> {
                mAllList.clear()
                updateAdapter()
            }
            CpMessageEvent.sl_contract_logout_event->{
                mList.clear()
                updateAdapter()
            }
        }
    }


}
