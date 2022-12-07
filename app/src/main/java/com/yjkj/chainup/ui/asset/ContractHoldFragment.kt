package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.view.CpEmptyOrderForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.R
import kotlinx.android.synthetic.main.fragment_contract_hold.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


/**
 * 当前持仓
 */
class ContractHoldFragment : CpNBaseFragment() {

    private var adapter: CpHoldContractNewAdapter? = null
    private var mList = ArrayList<CpContractPositionBean>()




    override fun setContentView(): Int {
        return R.layout.fragment_contract_hold
    }


    var mPositionObj: JSONObject? = null
    override fun initView() {
        adapter = CpHoldContractNewAdapter(mList)
        adapter!!.setMySelf(false)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyOrderForAdapterView(context ?: return))

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        mList.clear()
        adapter?.setList(mList)
    }





    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_refresh_position_list_event -> {
                mPositionObj = event.msg_content as JSONObject
              var  lidt = ArrayList<CpContractPositionBean>()
                mPositionObj?.apply {
                    if (!isNull("positionList")) {
                        val mOrderListJson = optJSONArray("positionList")
                        if (mOrderListJson != null) {
                            for (i in 0 until mOrderListJson.length()) {
                                val obj = mOrderListJson.getString(i)
                                lidt.add(
                                    Gson().fromJson(
                                        obj,
                                        CpContractPositionBean::class.java
                                    )
                                )
                            }
                        }
                        mList=lidt

                    }
                }
                updateAdapter()
            }
            CpMessageEvent.sl_contract_logout_event -> {
                mList.clear()
                updateAdapter()
            }

        }
    }


}