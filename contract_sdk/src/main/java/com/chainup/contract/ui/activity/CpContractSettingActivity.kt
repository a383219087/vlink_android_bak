package com.chainup.contract.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpPreferenceManager
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpNewDialogUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import kotlinx.android.synthetic.main.cp_activity_contract_setting.*
import org.json.JSONObject
/**
 * 合约设置
 */
class CpContractSettingActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_contract_setting
    }

    //持仓模式
    private val positionModeList = ArrayList<CpTabInfo>()
    private var positionModeDialog: TDialog? = null


    //仓位展示单位
    private val unitList = ArrayList<CpTabInfo>()
    private var currUnitInfo: CpTabInfo? = null
    private var unitDialog: TDialog? = null
    private var originUnitIndex: Int? = 0

    //未实现盈亏
    private val pnlList = ArrayList<CpTabInfo>()
    private var currPnlInfo: CpTabInfo? = null
    private var pnlDialog: TDialog? = null
    private var originPnlIndex: Int? = 0

    //有效时间
    private val timeList = ArrayList<CpTabInfo>()
    private var currTimeInfo: CpTabInfo? = null
    private var timeDialog: TDialog? = null

    //触发类型
    private val triggerList = ArrayList<CpTabInfo>()
    private var currTriggerInfo: CpTabInfo? = null
    private var triggerDialog: TDialog? = null
    private var originTriggerIndex: Int? = 0

    private var coUnit = 1
    private var positionModel = 1
    private var pcSecondConfirm = 1
    private var positionModelCanSwitch = -1
    private var expiredTime = 1

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
        positionModeList.add(CpTabInfo(getString(R.string.cp_contract_setting_text15), 0))
        positionModeList.add(CpTabInfo(getString(R.string.cp_contract_setting_text16), 1))

        unitList.add(CpTabInfo(getString(R.string.cp_overview_text9), 0))
        unitList.add(CpTabInfo(getString(R.string.cp_extra_text82), 1))
        currUnitInfo = findTabInfo(unitList, CpClLogicContractSetting.getContractUint(mActivity))
        originUnitIndex = currUnitInfo?.index

        pnlList.add(CpTabInfo(getString(R.string.cp_extra_text83), 0))
        pnlList.add(CpTabInfo(getString(R.string.cp_order_text31), 1))
        currPnlInfo = findTabInfo(pnlList, CpClLogicContractSetting.getPnlCalculate(mActivity))
        originPnlIndex = currPnlInfo?.index

        timeList.add(CpTabInfo("1" + getString(R.string.cp_extra_text84), 0, "1"))
        timeList.add(CpTabInfo("7" + getString(R.string.cp_extra_text84), 1, "7"))
        timeList.add(CpTabInfo("14" + getString(R.string.cp_extra_text84), 2, "14"))
        timeList.add(CpTabInfo("30" + getString(R.string.cp_extra_text84), 3, "30"))
        currTimeInfo = findTabInfo(timeList, CpClLogicContractSetting.getStrategyEffectTime(mActivity))

        triggerList.add(CpTabInfo(getString(R.string.cp_order_text31), 1))
        triggerList.add(CpTabInfo(getString(R.string.cp_extra_text83), 2))
        triggerList.add(CpTabInfo(getString(R.string.cp_extra_text85), 4))
        currTriggerInfo = findTabInfo(triggerList, CpClLogicContractSetting.getTriggerPriceType(mActivity))
        originTriggerIndex = currTriggerInfo?.index

        tradeConfirm = CpPreferenceManager.getInstance(mActivity).getSharedBoolean(
                CpPreferenceManager.PREF_TRADE_CONFIRM, true)

        contractId = intent.getIntExtra("contractId", 0)
        openContract = intent.getIntExtra("openContract", 0)

        positionModel = CpClLogicContractSetting.getPositionModel(mActivity)
        coUnit = CpClLogicContractSetting.getContractUint(mActivity)
        tv_position_mode_value.setText(if (positionModel == 1) getString(R.string.cp_contract_setting_text15) else getString(R.string.cp_contract_setting_text16))
        tv_contracts_unit_value.setText(if (coUnit == 1) getString(R.string.cp_extra_text82) else getString(R.string.cp_overview_text9))
    }

    override fun initView() {
        initAutoTextView()
        tv_contracts_unit_value.text = currUnitInfo?.name
        tv_pnl_calculator_value.text = currPnlInfo?.name
        tv_effective_time_value.text = currTimeInfo?.name
        tv_trigger_type_value.text = currTriggerInfo?.name
    }

    private fun initAutoTextView() {
        title_layout.setContentTitle(getString(R.string.cp_contract_setting_text13))
        tv_contracts_unit_label.setText(getString(R.string.cp_extra_text86))
        tv_pnl_calculator_label.setText(getString(R.string.cp_extra_text87))
        tv_book_confirm_label.setText(getString(R.string.cp_contract_setting_text19))
        tv_effective_time_label.setText(getString(R.string.cp_contract_setting_text21))
    }

    private fun initListener() {
        rl_position_mode_layout.setOnClickListener {

            if (positionModelCanSwitch == -1) {
                //此时通过本地配置来选择
                positionModeDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, positionModeList, if (positionModel == 1) 0 else 1, object : CpNewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currTriggerInfo = positionModeList[index]
                        positionModeDialog?.dismiss()
                        positionModel = if (index == 0) 1 else 2
                        tv_position_mode_value.text = currTriggerInfo?.name
                        CpClLogicContractSetting.setPositionModel(mActivity, positionModel)
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_change_position_model_event
                                )
                        )
                    }
                })
            } else {
                if (positionModelCanSwitch == 0) {
                    CpDialogUtil.showNewsingleDialog2(this!!, getString(R.string.cp_extra_text89), object : CpNewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {

                        }
                    }, cancelTitle = CpLanguageUtil.getString(this, "cp_extra_text28"))
                    return@setOnClickListener
                }
                positionModeDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, positionModeList, if (positionModel == 1) 0 else 1, object : CpNewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currTriggerInfo = triggerList[index]
                        positionModeDialog?.dismiss()
                        positionModel = if (index == 0) 1 else 2
                        CpClLogicContractSetting.setPositionModel(mActivity, positionModel)
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_change_position_model_event
                                )
                        )
                        modifyTransactionLike()
                    }
                })
            }


        }


        //触发类型
        rl_trigger_type_layout.setOnClickListener {
            triggerDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, triggerList, currTriggerInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currTriggerInfo = triggerList[index]
                    triggerDialog?.dismiss()
                    tv_trigger_type_value.text = currTriggerInfo?.name
                    CpClLogicContractSetting.setTriggerPriceType(mActivity, currTriggerInfo!!.index)
                }
            })
        }
        //有效时间
        rl_effective_time_layout.setOnClickListener {
            timeDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, timeList, currTimeInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currTimeInfo = timeList[index]
                    timeDialog?.dismiss()
                    tv_effective_time_value.text = currTimeInfo?.name
                    expiredTime=currTimeInfo!!.extras!!.toInt()
                    CpClLogicContractSetting.setStrategyEffectTime(mActivity, currTimeInfo!!.index)
                    CpClLogicContractSetting.setStrategyEffectTimeStr(mActivity, currTimeInfo!!.extras!!.toInt())
                    modifyTransactionLike()
                }
            })
        }
        //仓位展示单位
        rl_display_unit_layout.setOnClickListener {
            if (positionModelCanSwitch == -1) {
                //此时通过本地配置来选择
                unitDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, unitList, currUnitInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currUnitInfo = unitList[index]
                        unitDialog?.dismiss()
//                    tv_contracts_unit_value.text = currUnitInfo?.name
                        coUnit = if (index == 0) 2 else 1
                        tv_contracts_unit_value.setText(currUnitInfo!!.name)
                        CpClLogicContractSetting.setContractUint(mActivity, currUnitInfo!!.index)
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_change_unit_event
                                )
                        )
                    }
                })
            } else {
                unitDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, unitList, currUnitInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        currUnitInfo = unitList[index]
                        unitDialog?.dismiss()
//                    tv_contracts_unit_value.text = currUnitInfo?.name
                        CpClLogicContractSetting.setContractUint(mActivity, currUnitInfo!!.index)
                        coUnit = if (index == 0) 2 else 1
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_change_unit_event
                                )
                        )
                        modifyTransactionLike()
                    }
                })
            }

        }
        //未实现盈亏tv_price_hint
        rl_pnl_calculator_layout.setOnClickListener {
            pnlDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, pnlList, currPnlInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currPnlInfo = pnlList[index]
                    pnlDialog?.dismiss()
                    tv_pnl_calculator_value.text = currPnlInfo?.name
                    CpClLogicContractSetting.setPnlCalculate(mActivity, currPnlInfo!!.index)
                }
            })
        }
        //下单二次确认
        switch_book_again.isChecked = tradeConfirm
        switch_book_again.setOnCheckedChangeListener { _, isChecked ->
            CpPreferenceManager.getInstance(mActivity).putSharedBoolean(
                    CpPreferenceManager.PREF_TRADE_CONFIRM, isChecked)
            switch_book_again.isChecked = isChecked
            setViewSelect(switch_book_again, isChecked)
        }
        setViewSelect(switch_book_again, tradeConfirm)
    }

    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.cp_open)
        } else {
            view.setBackgroundResource(R.drawable.cp_shut_down)
        }
    }

    private fun loadContractUserConfig() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(getContractModel().getUserConfig(contractId.toString(),
                consumer = object : CpNDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            coUnit = optInt("coUnit")//合约单位 1标的货币, 2张
                            positionModel = optInt("positionModel")//持仓类型 1持仓, 2双向持仓
                            pcSecondConfirm = optInt("pcSecondConfirm")//下单前弹窗确认开关, 0使用，1停用
                            positionModelCanSwitch = optInt("positionModelCanSwitch")//当前持仓类型是否可以切换, 0 不可以切换, 1 可以切换
                            expiredTime = optInt("expireTime")// 单位: 天 (固定枚举) 1, 7, 14, 30
                            tv_position_mode_value.setText(if (positionModel == 1) getString(R.string.cp_contract_setting_text15) else getString(R.string.cp_contract_setting_text16))
                            tv_contracts_unit_value.setText(if (coUnit == 1) getString(R.string.cp_extra_text82) else getString(R.string.cp_overview_text9))
                            tv_effective_time_value.setText(expiredTime.toString() + getString(R.string.cp_extra_text84))
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
                expiredTime.toString(),
                consumer = object : CpNDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        loadContractUserConfig()
                    }
                }))
    }


    override fun finish() {
        super.finish()
        if (currUnitInfo?.index != originUnitIndex || originTriggerIndex != currTriggerInfo?.index || currPnlInfo?.index != originPnlIndex) {
            CpClLogicContractSetting.getInstance().refresh()
        }
    }

    companion object {
        fun show(activity: Activity, contractId: Int, openContract: Int) {
            val intent = Intent(activity, CpContractSettingActivity::class.java)
            intent.putExtra("contractId", contractId)
            intent.putExtra("openContract", openContract)
            activity.startActivity(intent)
        }
    }

    private fun findTabInfo(list: ArrayList<CpTabInfo>, index: Int = 0): CpTabInfo {
        for (i in list.indices) {
            if (list[i].index == index) {
                return list[i]
            }
        }
        return list[0]
    }

}