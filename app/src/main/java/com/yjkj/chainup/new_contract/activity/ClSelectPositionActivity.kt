package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import kotlinx.android.synthetic.main.cl_activity_select_position.*
import org.json.JSONObject

/**
 * 切换保证金模式
 */
class ClSelectPositionActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_select_position
    }

    private var currTabType = 1// 1 全仓  2 逐仓
    private var contractId = 0
    private var marginModelCanSwitch = 0 //是否可以切换 1是, 0否
    private var marginModel = 1 //当前保证金模式 1全仓, 2逐仓

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        initListener()
    }


    override fun loadData() {
        super.loadData()
        contractId = intent.getIntExtra("contractId", 0)
        loadContractUserConfig()
    }

    override fun initView() {
        super.initView()
        initAutoTextView()
        switchTabUi()
    }

    private fun initAutoTextView() {
//        title_layout.title = getLineText("sl_str_switch_lever")
        title_layout.title = getLineText("cl_margin_mode_str")
        tv_tab_gradually.onLineText("sl_str_gradually_position")
        tv_tab_full.onLineText("sl_str_full_position")
    }

    private fun initListener() {
        //切换限制：
        //
        //1. 持有本合约仓位不可切换
        //2. 有本合约挂单不可切换

        /**
         * 点击逐仓tab
         *  1 全仓  2 逐仓
         */
        tv_tab_gradually.setOnClickListener {
            if (currTabType == 1) {
                currTabType = 2
                switchTabUi()
            }
        }
        /**
         * 点击全仓tab
         *  1 全仓  2 逐仓
         */
        tv_tab_full.setOnClickListener {
            if (currTabType == 2) {
                currTabType = 1
                switchTabUi()
            }
        }
        tv_confirm_btn.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                modifyMarginModel()
            }
        }
    }

    /**
     * 切换仓位Tab
     */
    private fun switchTabUi() {
        if (currTabType == 2) {//逐仓
            tv_tab_gradually.setBackgroundResource(R.drawable.sl_tab_leverage_full_select)
            tv_tab_gradually.isSelected = true
            tv_tab_full.setBackgroundResource(R.drawable.sl_tab_leverage_gradually_normal)
            tv_tab_full.isSelected = false
            tv_position_des.setText(R.string.cl_depositmode_text2)
        } else {
            tv_tab_gradually.setBackgroundResource(R.drawable.sl_tab_leverage_full_normal)
            tv_tab_gradually.isSelected = false
            tv_tab_full.setBackgroundResource(R.drawable.sl_tab_leverage_gradually_select)
            tv_tab_full.isSelected = true
            tv_position_des.setText(R.string.cl_depositmode_text1)
        }

        //是否可以切换 1是, 0否
        if (marginModelCanSwitch == 1) {
            if (marginModel == currTabType) {
                tv_confirm_btn.isEnable(false)
                tv_confirm_btn.textContent = if (marginModel == 1) getString(R.string.cl_currentcross_mode_str) else getString(R.string.cl_crurrentisolated_mode_str)
            } else {
                tv_confirm_btn.isEnable(true)
                tv_confirm_btn.textContent =if (currTabType == 1) getString(R.string.cl_switch_to_cross_mode_str) else getString(R.string.cl_switch_to_isolated_mode_str)
            }

        } else {
            tv_confirm_btn.isEnable(false)
            tv_confirm_btn.textContent = this.getString(R.string.cl_no_change_margin_mode_str)
        }
    }

    private fun loadContractUserConfig() {
        addDisposable(getContractModel().getUserConfig(contractId.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            currTabType = optInt("marginModel")
                            marginModel =currTabType
                            marginModelCanSwitch = optInt("marginModelCanSwitch")
                            switchTabUi()
                        }
                    }
                }))
    }

    private fun modifyMarginModel() {
        addDisposable(getContractModel().modifyMarginModel(contractId.toString(), currTabType.toString(),
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
//                        ToastUtils.showToast("保证金模式设置成功")
                        finish()
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_modify_margin_event))
                    }
                }))
    }


    companion object {
        fun show(activity: Activity, contractId: Int = 0, selectLeverage: Int = 10, price: String, leverageType: Int = 1) {
            val intent = Intent(activity, ClSelectPositionActivity::class.java)
            intent.putExtra("contractId", contractId)
            intent.putExtra("selectLeverage", selectLeverage)
            intent.putExtra("selectLeverageType", leverageType)
            intent.putExtra("price", price)
            activity.startActivity(intent)
        }
    }
}