package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.cl_activity_contract_setting.*
import org.json.JSONObject

/**
 * 合约设置
 */
class ClContractSettingActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_contract_setting
    }

    //持仓模式
    private val positionModeList = ArrayList<TabInfo>()
    private var positionModeDialog: TDialog? = null


    //仓位展示单位
    private val unitList = ArrayList<TabInfo>()
    private var currUnitInfo: TabInfo? = null
    private var unitDialog: TDialog? = null
    private var originUnitIndex: Int? = 0

    //未实现盈亏
    private val pnlList = ArrayList<TabInfo>()
    private var currPnlInfo: TabInfo? = null
    private var pnlDialog: TDialog? = null
    private var originPnlIndex: Int? = 0

    //有效时间
    private val timeList = ArrayList<TabInfo>()
    private var currTimeInfo: TabInfo? = null
    private var timeDialog: TDialog? = null

    //触发类型
    private val triggerList = ArrayList<TabInfo>()
    private var currTriggerInfo: TabInfo? = null
    private var triggerDialog: TDialog? = null
    private var originTriggerIndex: Int? = 0

    private var coUnit = 1
    private var positionModel = 1
    private var pcSecondConfirm = 1
    private var positionModelCanSwitch = -1

    //下单二次确认
    private var tradeConfirm = true

    private var contractId = 0

    private var openContract = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        initListener()
        loadContractUserConfig()
    }


    override fun loadData() {
        positionModeList.add(TabInfo(getLineText("cl_netting_mode_str"), 0))
        positionModeList.add(TabInfo(getLineText("cl_hedge_mode_str"), 1))

        unitList.add(TabInfo(getLineText("sl_str_contracts_unit"), 0))
        unitList.add(TabInfo(getLineText("sl_str_coin_unit"), 1))
        currUnitInfo = findTabInfo(unitList, LogicContractSetting.getContractUint(mActivity))
        originUnitIndex = currUnitInfo?.index

        pnlList.add(TabInfo(getLineText("sl_str_fair_price"), 0))
        pnlList.add(TabInfo(getLineText("sl_str_latest_price"), 1))
        currPnlInfo = findTabInfo(pnlList, LogicContractSetting.getPnlCalculate(mActivity))
        originPnlIndex = currPnlInfo?.index

        timeList.add(TabInfo("1"+getString(R.string.cl_day_str), 0, "1"))
        timeList.add(TabInfo("7"+getString(R.string.cl_day_str), 1, "7"))
        timeList.add(TabInfo("14"+getString(R.string.cl_day_str), 2, "14"))
        timeList.add(TabInfo("30"+getString(R.string.cl_day_str), 3, "30"))
        currTimeInfo = findTabInfo(timeList, LogicContractSetting.getStrategyEffectTime(mActivity))

        triggerList.add(TabInfo(getLineText("sl_str_latest_price"), 1))
        triggerList.add(TabInfo(getLineText("sl_str_fair_price"), 2))
        triggerList.add(TabInfo(getLineText("sl_str_index_price"), 4))
        currTriggerInfo = findTabInfo(triggerList, LogicContractSetting.getTriggerPriceType(mActivity))
        originTriggerIndex = currTriggerInfo?.index

        tradeConfirm = PreferenceManager.getInstance(mActivity).getSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, true)

        contractId = intent.getIntExtra("contractId", 0)
        openContract = intent.getIntExtra("openContract", 0)

        positionModel = LogicContractSetting.getPositionModel(mActivity)
        coUnit = LogicContractSetting.getContractUint(mActivity)
        tv_position_mode_value.setText(if (positionModel == 1) getLineText("cl_netting_mode_str") else getLineText("cl_hedge_mode_str"))
        tv_contracts_unit_value.setText(if (coUnit == 1) getLineText("sl_str_coin_unit") else getLineText("sl_str_contracts_unit"))
    }

    override fun initView() {
        initAutoTextView()
        tv_contracts_unit_value.text = currUnitInfo?.name
        tv_pnl_calculator_value.text = currPnlInfo?.name
        tv_effective_time_value.text = currTimeInfo?.name
        tv_trigger_type_value.text = currTriggerInfo?.name
    }

    private fun initAutoTextView() {
        title_layout.setContentTitle(getLineText("sl_str_contract_settings"))
        tv_contracts_unit_label.onLineText("sl_str_display_unit")
        tv_pnl_calculator_label.onLineText("sl_str_pnl_calculator")
        tv_book_confirm_label.onLineText("sl_str_book_confirm")
        tv_effective_time_label.onLineText("sl_str_strategy_effective_time")
//        tv_plan_settings_label.onLineText("sl_str_plan_settings")
    }

    private fun initListener() {
        rl_position_mode_layout.setOnClickListener {

            if (positionModelCanSwitch == -1) {
                //此时通过本地配置来选择
                positionModeDialog = NewDialogUtils.showNewBottomListDialog(mActivity, positionModeList, if (positionModel == 1) 0 else 1, object : NewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currTriggerInfo = positionModeList[index]
                        positionModeDialog?.dismiss()
                        positionModel = if (index == 0) 1 else 2
                        tv_position_mode_value.setText(currTriggerInfo?.name)
                        LogicContractSetting.setPositionModel(mActivity, positionModel)
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_change_position_model_event))
                    }
                })
            } else {
                if (positionModelCanSwitch == 0) {
                    NToastUtil.showTopToastNet(mActivity,false, getLineText("cl_no_change_margin_mode_str"))
                    return@setOnClickListener
                }
                positionModeDialog = NewDialogUtils.showNewBottomListDialog(mActivity, positionModeList, if (positionModel == 1) 0 else 1, object : NewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currTriggerInfo = triggerList[index]
                        positionModeDialog?.dismiss()
                        positionModel = if (index == 0) 1 else 2
                        LogicContractSetting.setPositionModel(mActivity, positionModel)
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_change_position_model_event))
                        modifyTransactionLike()
                    }
                })
            }


        }


        //触发类型
        rl_trigger_type_layout.setOnClickListener {
            triggerDialog = NewDialogUtils.showNewBottomListDialog(mActivity, triggerList, currTriggerInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currTriggerInfo = triggerList[index]
                    triggerDialog?.dismiss()
                    tv_trigger_type_value.text = currTriggerInfo?.name
                    LogicContractSetting.setTriggerPriceType(mActivity, currTriggerInfo!!.index)
                }
            })
        }
        //有效时间
        rl_effective_time_layout.setOnClickListener {
            timeDialog = NewDialogUtils.showNewBottomListDialog(mActivity, timeList, currTimeInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currTimeInfo = timeList[index]
                    timeDialog?.dismiss()
                    tv_effective_time_value.text = currTimeInfo?.name
                    LogicContractSetting.setStrategyEffectTime(mActivity, currTimeInfo!!.index)
                    LogicContractSetting.setStrategyEffectTimeStr(mActivity, currTimeInfo!!.extras!!.toInt())
                }
            })
        }
        //仓位展示单位
        rl_display_unit_layout.setOnClickListener {
            if (positionModelCanSwitch == -1) {
                //此时通过本地配置来选择
                unitDialog = NewDialogUtils.showNewBottomListDialog(mActivity, unitList, currUnitInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currUnitInfo = unitList[index]
                        unitDialog?.dismiss()
//                    tv_contracts_unit_value.text = currUnitInfo?.name
                        coUnit = if (index == 0) 2 else 1
                        tv_contracts_unit_value.setText(currUnitInfo!!.name)
                        LogicContractSetting.setContractUint(mActivity, currUnitInfo!!.index)
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_change_unit_event))
                    }
                })
            } else {
                unitDialog = NewDialogUtils.showNewBottomListDialog(mActivity, unitList, currUnitInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currUnitInfo = unitList[index]
                        unitDialog?.dismiss()
//                    tv_contracts_unit_value.text = currUnitInfo?.name
                        LogicContractSetting.setContractUint(mActivity, currUnitInfo!!.index)
                        coUnit = if (index == 0) 2 else 1
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_change_unit_event))
                        modifyTransactionLike()
                    }
                })
            }

        }
        //未实现盈亏tv_price_hint
        rl_pnl_calculator_layout.setOnClickListener {
            pnlDialog = NewDialogUtils.showNewBottomListDialog(mActivity, pnlList, currPnlInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currPnlInfo = pnlList[index]
                    pnlDialog?.dismiss()
                    tv_pnl_calculator_value.text = currPnlInfo?.name
                    LogicContractSetting.setPnlCalculate(mActivity, currPnlInfo!!.index)
                }
            })
        }
        //下单二次确认
        switch_book_again.isChecked = tradeConfirm
        switch_book_again.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getInstance(mActivity).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, isChecked)
            switch_book_again.isChecked = isChecked
            setViewSelect(switch_book_again, isChecked)
        }
        setViewSelect(switch_book_again, tradeConfirm)
    }

    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
    }

    private fun loadContractUserConfig() {
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        addDisposable(getContractModel().getUserConfig(contractId.toString(),
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            coUnit = optInt("coUnit")//合约单位 1标的货币, 2张
                            positionModel = optInt("positionModel")//持仓类型 1持仓, 2双向持仓
                            pcSecondConfirm = optInt("pcSecondConfirm")//下单前弹窗确认开关, 0使用，1停用
                            positionModelCanSwitch = optInt("positionModelCanSwitch")//当前持仓类型是否可以切换, 0 不可以切换, 1 可以切换
                            tv_position_mode_value.setText(if (positionModel == 1) getString(R.string.cl_netting_mode_str) else getString(R.string.cl_hedge_mode_str))
                            tv_contracts_unit_value.setText(if (coUnit == 1) getString(R.string.sl_str_coin_unit) else getString(R.string.sl_str_contracts_unit))
                        }
                    }
                }))
    }

    private fun modifyTransactionLike() {
        addDisposable(getContractModel().modifyTransactionLike(
                contractId.toString(),
                positionModel.toString(),
                pcSecondConfirm.toString(),
                coUnit.toString(),
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        loadContractUserConfig()
                    }
                }))
    }


    override fun finish() {
        super.finish()
        if (currUnitInfo?.index != originUnitIndex || originTriggerIndex != currTriggerInfo?.index || currPnlInfo?.index != originPnlIndex) {
            LogicContractSetting.getInstance().refresh()
        }
    }

    companion object {
        fun show(activity: Activity, contractId: Int, openContract: Int) {
            val intent = Intent(activity, ClContractSettingActivity::class.java)
            intent.putExtra("contractId", contractId)
            intent.putExtra("openContract", openContract)
            activity.startActivity(intent)
        }
    }

    private fun findTabInfo(list: ArrayList<TabInfo>, index: Int = 0): TabInfo {
        for (i in list.indices) {
            if (list[i].index == index) {
                return list[i]
            }
        }
        return list[0]
    }

}