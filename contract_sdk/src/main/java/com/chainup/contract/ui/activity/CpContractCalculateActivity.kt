package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chainup.contract.R
import com.chainup.contract.adapter.CpCoinSelectLeftAdapter
import com.chainup.contract.adapter.CpCoinSelectRightAdapter
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpClLogicContractSetting
import com.yjkj.chainup.new_contract.fragment.CpLiquidationPriceFragment
import com.yjkj.chainup.new_contract.fragment.CpPlCalculatorFragment
import com.yjkj.chainup.new_contract.fragment.CpProfitRateFragment
import kotlinx.android.synthetic.main.cp_activity_contract_calculate.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 合约计算器
 */
class CpContractCalculateActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_contract_calculate
    }
    //当前选择的币对
    private var mCurrContractInfo: CpTabInfo? = null
    private var contractId = 0
    private var indexPrice = ""
    private lateinit var mContractJson: JSONObject
    var isShowContractSelect: Boolean = false;
    private var mFragments: ArrayList<Fragment>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contractId = intent.getIntExtra("contractId", 0)
        indexPrice = intent.getStringExtra("indexPrice").toString()
        loadData()
        initView()
        initListener()
    }

    override fun loadData() {
        mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
    }

    override fun initView() {
        initAutoTextView()
        tv_order_type.text = CpClLogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))

        val bundle = Bundle()
        bundle.putInt("contractId", contractId)
        bundle.putString("indexPrice", indexPrice)

        initTabInfo()
        ic_close.setOnClickListener { finish() }
        ll_order_type.setOnClickListener {
            setCoinData()
            if (!isShowContractSelect) {
                ll_coin_select.visibility = View.VISIBLE
                img_order_type.animate().setDuration(200).rotation(180f).start()
            } else {
                ll_coin_select.visibility = View.GONE
                img_order_type.animate().setDuration(200).rotation(0f).start()
            }
            isShowContractSelect = !isShowContractSelect
        }
        dissmiss.setOnClickListener {
            isShowContractSelect = false
            ll_coin_select.visibility = View.GONE
            img_order_type.animate().setDuration(200).rotation(0f).start()
        }
    }

    private fun initTabInfo() {
        mFragments = ArrayList()
        mFragments?.add(CpPlCalculatorFragment.newInstance(contractId))
        mFragments?.add(CpLiquidationPriceFragment.newInstance(contractId))
        mFragments?.add(CpProfitRateFragment.newInstance(contractId))
        sub_tab_layout.setViewPager(vp_order, arrayOf(getString(R.string.cp_calculator_text2), getString(R.string.cp_calculator_text4),getString(R.string.cp_calculator_text9) ), this, mFragments)
    }

    private fun setCoinData() {
        var sideList = ArrayList<CpTabInfo>()
        var sideListBuff = ArrayList<CpTabInfo>()
        var sideListU = ArrayList<CpTabInfo>()
        var sideListB = ArrayList<CpTabInfo>()
        var sideListH = ArrayList<CpTabInfo>()
        var sideListM = ArrayList<CpTabInfo>()

        var isHasU = false //正向合约
        var isHasB = false //币本位
        var isHasH = false //混合合约
        var isHasM = false //模拟合约
        val mContractList = JSONArray(CpClLogicContractSetting.getContractJsonListStr(this))
        var positionLeft = 0
        for (i in 0 until mContractList.length()) {
            val obj = mContractList.getJSONObject(i)
            val contractSide = obj.getInt("contractSide")
            val contractType = obj.getString("contractType")
            val id = obj.getInt("id")

            //E,USDT合约 2,币本位合约 H,混合合约 S,模拟合约
            //classification 1,USDT合约 2,币本位合约 3,混合合约 4,模拟合约
            when (contractType) {
                "E" -> {
                    isHasU = true
                    sideListU.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                "S" -> {
                    isHasM = true
                    sideListM.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                else -> {
                    isHasH = true
                    sideListH.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
            }
            if (CpContractEntrustNewActivity.mContractId == id) {
                positionLeft = when (contractType) {
                    "E" -> {
                        0
                    }
                    "S" -> {
                        3
                    }
                    else -> {
                        2
                    }
                }
            }
        }
        if (isHasU) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text13), 0,if (positionLeft == 0) 0 else 1))
        }
        if (isHasB) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text10), 1,if (positionLeft == 1) 0 else 1))
        }
        if (isHasH) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text12), 2,if (positionLeft == 2) 0 else 1))
        }
        if (isHasM) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text11), 3,if (positionLeft == 3) 0 else 1))
        }
        if (positionLeft == 0) {
            sideListBuff.addAll(sideListU)
        } else if (positionLeft == 1) {
            sideListBuff.addAll(sideListB)
        } else if (positionLeft == 3) {
            sideListBuff.addAll(sideListM)
        } else {
            sideListBuff.addAll(sideListH)
        }
        var mRightAdapter = CpCoinSelectRightAdapter(sideListBuff, CpContractEntrustNewActivity.mContractId)
        rv_right?.layoutManager = LinearLayoutManager(this)
        rv_right?.adapter = mRightAdapter
        rv_right?.setHasFixedSize(true)
        mRightAdapter.setOnItemClickListener { adapter, view, position ->
            mCurrContractInfo = CpTabInfo(sideListBuff[position].name, position)
            CpContractEntrustNewActivity.mContractId = sideListBuff[position].index
            tv_order_type.setText(CpClLogicContractSetting.getContractShowNameById(this@CpContractCalculateActivity, CpContractEntrustNewActivity.mContractId))
            val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_event)
            event.msg_content = CpContractEntrustNewActivity.mContractId
            CpEventBusUtil.post(event)
            img_order_type.animate().setDuration(200).rotation(0f).start()
            isShowContractSelect = false
            ll_coin_select.visibility = View.GONE
        }
        var adapter = CpCoinSelectLeftAdapter(sideList, positionLeft)
        rv_left?.layoutManager = LinearLayoutManager(this)
        rv_left?.adapter = adapter
        rv_left?.setHasFixedSize(true)
        adapter.setOnItemClickListener { adapter, view, position ->
            sideListBuff.clear()
            if (sideList[position].index == 0) {
                sideListBuff.addAll(sideListU)
            } else if (sideList[position].index == 1) {
                sideListBuff.addAll(sideListB)
            } else if (sideList[position].index == 2) {
                sideListBuff.addAll(sideListH)
            } else {
                sideListBuff.addAll(sideListM)
            }
            mRightAdapter.notifyDataSetChanged()

            for (buff in sideList){
                buff.extrasNum=if (buff.index==sideList[position].index) 0 else 1
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun initAutoTextView() {
//        title_layout.setContentTitle(getString(R.string.sl_str_contract_calculator))
//        tv_contract_type_label.setText(getString(R.string.sl_str_contract_type))
    }

    private fun initListener() {
//        //合约类型
//        rl_contract_type_layout.setOnClickListener {
//            CpContractSearchActivity.show(mActivity)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            val contractId = data.getIntExtra("contractId", 0)
//            mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
////            tv_contracts_type_value.text = mContractJson.optString("symbol") + mContractJson.optString("contractShowType")
//            tv_contracts_type_value.text = CpClLogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))
//            val mMessageEvent =
//                CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_event)
//            mMessageEvent.msg_content = contractId
//            CpEventBusUtil.post(mMessageEvent)
//        }
    }


    private fun doSwitchContract() {
//        plCalculatorFragment.switchContract(currContractInfo)
//        liquidationPriceFragment.switchContract(currContractInfo)
//        profitRateFragment.switchContract(currContractInfo)
//        tv_contracts_type_value.text = currContractInfo?.getDisplayName(mActivity)
//        tv_contracts_type_value.text = CpClLogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))
    }


    companion object {
        fun show(activity: Activity, contractId: Int, indexPrice: String) {
            val intent = Intent(activity, CpContractCalculateActivity::class.java)
            intent.putExtra("contractId", contractId)
            intent.putExtra("indexPrice", indexPrice)
            activity.startActivity(intent)
        }
    }

}