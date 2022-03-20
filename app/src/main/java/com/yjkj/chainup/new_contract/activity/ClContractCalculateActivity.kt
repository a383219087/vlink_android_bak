package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.contract.sdk.data.Contract
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_contract.fragment.ClLiquidationPriceFragment
import com.yjkj.chainup.new_contract.fragment.ClPlCalculatorFragment
import com.yjkj.chainup.new_contract.fragment.ClProfitRateFragment
import com.yjkj.chainup.new_version.adapter.PageAdapter
import kotlinx.android.synthetic.main.cl_activity_contract_calculate.*
import org.json.JSONObject

/**
 * 合约计算器
 */
class ClContractCalculateActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_contract_calculate
    }

    private var contractId = 0
    private var indexPrice = ""

    //合约类型
    private var currContractInfo: Contract? = null

    private var plCalculatorFragment = ClPlCalculatorFragment()
    private var liquidationPriceFragment = ClLiquidationPriceFragment()
    private var profitRateFragment = ClProfitRateFragment()

    private lateinit var mContractJson: JSONObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contractId = intent.getIntExtra("contractId", 0)
        indexPrice = intent.getStringExtra("indexPrice") ?: ""
        loadData()
        initView()
        initListener()
    }

    override fun loadData() {
        mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
    }

    override fun initView() {
        initAutoTextView()
        tv_contracts_type_value.text = LogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))
//        //tab
        val showTitles = java.util.ArrayList<String>()
        val fragments = java.util.ArrayList<Fragment>()
        showTitles.add(getLineText("cl_calculator_text16"))
        showTitles.add(getLineText("cl_calculator_text11"))
        showTitles.add(getLineText("cl_calculator_text13"))

        val bundle = Bundle()
        bundle.putInt("contractId", contractId)
        bundle.putString("indexPrice", indexPrice)
        plCalculatorFragment.arguments = bundle
        liquidationPriceFragment.arguments = bundle
        profitRateFragment.arguments = bundle
        fragments.add(plCalculatorFragment)
        fragments.add(liquidationPriceFragment)
        fragments.add(profitRateFragment)
        val pageAdapter = PageAdapter(supportFragmentManager, showTitles, fragments)
        vp_layout.adapter = pageAdapter
        vp_layout.offscreenPageLimit = 3
        tl_tab_layout.setViewPager(vp_layout, showTitles.toTypedArray())
        tl_tab_layout.currentTab = 1
        tl_tab_layout.currentTab = 0
//        tl_tab_layout.postDelayed(object:Runnable{
//            override fun run() {
//                doSwitchContract()
//            }
//
//        },10)

    }

    private fun initAutoTextView() {
        title_layout.setContentTitle(getLineText("sl_str_contract_calculator"))
        tv_contract_type_label.onLineText("sl_str_contract_type")
    }

    private fun initListener() {
        //合约类型
        rl_contract_type_layout.setOnClickListener {
            ClContractSearchActivity.show(mActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val contractId = data.getIntExtra("contractId", 0)
            mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
//            tv_contracts_type_value.text = mContractJson.optString("symbol") + mContractJson.optString("contractShowType")
            tv_contracts_type_value.text = LogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))
            val mMessageEvent = MessageEvent(MessageEvent.sl_contract_calc_switch_contract_event)
            mMessageEvent.msg_content = contractId
            EventBusUtil.post(mMessageEvent)
        }
    }


    private fun doSwitchContract() {
//        plCalculatorFragment.switchContract(currContractInfo)
//        liquidationPriceFragment.switchContract(currContractInfo)
//        profitRateFragment.switchContract(currContractInfo)
//        tv_contracts_type_value.text = currContractInfo?.getDisplayName(mActivity)
        tv_contracts_type_value.text = LogicContractSetting.getContractShowNameById(mActivity, mContractJson.optInt("id"))
    }


    companion object {
        fun show(activity: Activity, contractId: Int, indexPrice: String) {
            val intent = Intent(activity, ClContractCalculateActivity::class.java)
            intent.putExtra("contractId", contractId)
            intent.putExtra("indexPrice", indexPrice)
            activity.startActivity(intent)
        }
    }

}