package com.yjkj.chainup.contract.widget

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.extra.Contract.ContractCalculate
import com.common.sdk.utlis.MathHelper
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.activity.SlContractCalculateActivity
import com.yjkj.chainup.contract.activity.SlContractDetailActivity
import com.yjkj.chainup.contract.activity.SlContractHistoryHoldActivity
import com.yjkj.chainup.contract.activity.SlContractSettingActivity
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.ContractUtils
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.new_contract.activity.*
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.BigDecimalUtils
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import org.jetbrains.anko.imageResource
import org.json.JSONObject


object SlDialogHelper {
    /**
     * 资金费率对话框
     */
    fun showFundsRateDialog(context: Context,
                            listener: OnBindViewListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_funds_rate_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(listener)
                .addOnClickListener(R.id.tv_confirm_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 开通合约/购买提示对话框
     */
    fun showSimpleCreateContractDialog(context: Context, viewListener: OnBindViewListener?,
                                       listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_simple_create_contract_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(viewListener)
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 暂停充提对话框
     */
    fun showSuspensionChargingDialog(context: Context, viewListener: OnBindViewListener?,
                                     listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_suspension_charging)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(viewListener)
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }


    /**
     * 资产页面--安全建议弹窗
     */
    fun showSimpleSafetyAdviceDialog(context: Context, viewListener: OnBindViewListener?,
                                     listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        var isSelected = false
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_simple_safety_advice_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setScreenWidthAspect(context, 0.89f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder ->
                    val imageView = viewHolder?.getView<ImageView>(R.id.iv_state)
                    imageView?.setOnClickListener {
                        isSelected = !isSelected
                        if (isSelected) {
                            imageView.imageResource = R.drawable.selected
                        } else {
                            imageView.imageResource = R.drawable.unchecked
                        }
                    }
                }
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            PreferenceManager.putBoolean(context, "isShowSafetyAdviceDialog", !isSelected)
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 展示强平明细对话框
     */
    fun showWipedOutIntroduceDialog(context: Context, title: String, intro1: String? = "", intro2: String, confirmText: String? = "",
                                    listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_wiped_out_introduce_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder ->
                    viewHolder?.let {
                        it.getView<TextView>(R.id.btn_cancel).onLineText("common_text_btnCancel")

                        it.setText(R.id.tv_title, title)
                        it.setText(R.id.tv_intro1, intro1)
                        it.setText(R.id.tv_intro2, intro2)
                        if (!TextUtils.isEmpty(confirmText)) {
                            it.setText(R.id.btn_ok, confirmText)
                        } else {
                            it.getView<TextView>(R.id.btn_ok).onLineText("sl_str_force_close_mechanism")
                        }
                        if (TextUtils.isEmpty(intro2)) {
                            it.setGone(R.id.ll_warp_layout2, false)
                        } else {
                            it.setGone(R.id.ll_warp_layout2, true)
                        }
                    }
                }
                .addOnClickListener(R.id.btn_ok, R.id.btn_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.btn_ok -> {
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.btn_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }


    /**
     * 仓位平仓对话框
     */
    fun showClosePositionDialog(context: Context,
                                listener: OnBindViewListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_close_position_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(listener)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 展示市价全平前，撤销订单对话框
     */
    fun showCancelOpenOrdersDialog(context: Context, sureLisener: NewDialogUtils.DialogBottomListener,
                                   info: ContractPosition, orderList: List<ContractOrder>?
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_cancel_open_orders_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    val contract: Contract = ContractPublicDataAgent.getContract(info.instrument_id)
                            ?: return@OnBindViewListener

                    it.getView<TextView>(R.id.tv_title).onLineText("cl_tradeform_text21")
                    it.getView<TextView>(R.id.tv_content).onLineText("sl_str_close_position_tips")
                    it.getView<TextView>(R.id.tv_cancel).onLineText("common_text_btnCancel")
                    it.getView<TextView>(R.id.tv_confirm_btn).onLineText("sl_str_cancel_orders")

                    //方向
                    val tvType = it.getView<TextView>(R.id.tv_type)
                    if (info.side === ContractPosition.POSITION_TYPE_LONG) {
                        tvType.onLineText("sl_str_sell_close")
                        tvType.setTextColor(context.getResources().getColor(R.color.main_red))
                    } else if (info.side === ContractPosition.POSITION_TYPE_SHORT) {
                        tvType.onLineText("sl_str_buy_close")
                        tvType.setTextColor(context.getResources().getColor(R.color.main_green))
                    }
                    //合约名称
                    it.setText(R.id.tv_contract_name, contract.symbol)
                    //委托量
                    var vol = 0.0
                    var amount = 0.0
                    if (orderList != null) {
                        for (i in orderList.indices) {
                            val order: ContractOrder = orderList[i]
                            vol += MathHelper.sub(order.qty, order.cum_qty)
                            amount += MathHelper.mul(vol, MathHelper.round(order.px))
                        }
                    }
                    val price: Double = MathHelper.div(amount, vol)
                    val text = context.getLineText("sl_str_entrust_vol") + " <font color='black'>" + ContractCalculate.getVolUnitNoSuffix(contract, vol, price, LogicContractSetting.getContractUint(ContractSDKAgent.context)) + "</font> " + ContractUtils.getHoldVolUnit(contract)
                    it.setText(R.id.tv_volume_value, Html.fromHtml(text))
                })
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                            sureLisener.sendConfirm()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 合约交易确认对话框
     */
    fun showOrderCreateConfirmDialog(context: Context, titleColor: Int, title: String, contractName: String, price: String, triggerPrice: String, costPrice: String, amountValue: String, orderType: Int,
                                     sureLisener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cl_item_order_create_confirm_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.btn_cancel).onLineText("common_text_btnCancel")
                    it.getView<TextView>(R.id.btn_ok).onLineText("common_text_btnConfirm")
                    //标题
                    it.setText(R.id.tv_title, title)
                    it.setTextColor(R.id.tv_title, titleColor)
                    //合约
                    it.setText(R.id.tv_contract, contractName)
                    //价格
                    it.setText(R.id.tv_price_value, price)
                    //委托价格
                    it.setText(R.id.tv_commission_price_value, price)
                    //触发价格
                    it.setText(R.id.tv_trigger_price_value, triggerPrice)
                    //成本
                    it.setText(R.id.tv_cost_value, costPrice)
                    //数量
                    it.setText(R.id.tv_number_value, amountValue)
                    when (orderType) {
                        1, 2, 4, 5, 6 -> {
                            it.setVisibility(R.id.ll_price, View.VISIBLE)
                            it.setVisibility(R.id.ll_cost, View.VISIBLE)

                            it.setVisibility(R.id.ll_trigger_price, View.GONE)
                            it.setVisibility(R.id.ll_commission_price, View.GONE)
                        }
                        else -> {
                            it.setVisibility(R.id.ll_price, View.GONE)
                            it.setVisibility(R.id.ll_cost, View.GONE)

                            it.setVisibility(R.id.ll_trigger_price, View.VISIBLE)
                            it.setVisibility(R.id.ll_commission_price, View.VISIBLE)
                        }
                    }
                })
                .addOnClickListener(R.id.btn_cancel, R.id.btn_ok, R.id.rl_not_remind)
                .setOnViewClickListener { it, view, tDialog ->
                    //是否提示
                    val cbNotRemind = it.getView<CheckBox>(R.id.cb_not_remind)
                    when (view.id) {
                        R.id.btn_cancel -> {
                            tDialog.dismiss()
                            PreferenceManager.getInstance(ContractSDKAgent.context).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, !cbNotRemind.isChecked)
                        }
                        R.id.btn_ok -> {
                            tDialog.dismiss()
                            sureLisener.sendConfirm()
                            PreferenceManager.getInstance(ContractSDKAgent.context).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, !cbNotRemind.isChecked)
                        }
                        R.id.rl_not_remind -> {
                            cbNotRemind.isChecked = !cbNotRemind.isChecked
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 展示计划交易确认对话框
     */
    fun showPlanTreadConfirmDialog(context: Context, titleColor: Int, title: String, contract: String, triggerPrice: String, executionPrice: String, amountValue: String,
                                   leverage: String, triggerType: String, triggerTime: String,
                                   sureLisener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_plan_tread_confirm_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.btn_cancel).onLineText("common_text_btnCancel")
                    it.getView<TextView>(R.id.btn_ok).onLineText("common_text_btnConfirm")
                    it.getView<TextView>(R.id.tv_not_remind).onLineText("sl_str_no_longer_remind")
                    it.getView<TextView>(R.id.tv_leverage).onLineText("contract_action_lever")
                    it.getView<TextView>(R.id.tv_trigger_price).onLineText("sl_str_trigger_price")
                    it.getView<TextView>(R.id.tv_execution_price).onLineText("sl_str_execution_price")
                    it.getView<TextView>(R.id.tv_trigger_price_type_label).onLineText("sl_str_trigger_price_type")
                    it.getView<TextView>(R.id.tv_trigger_time_label).onLineText("sl_str_strategy_effective_time")
                    //标题
                    it.setText(R.id.tv_title, title)
                    it.setTextColor(R.id.tv_title, titleColor)
                    //合约
                    it.setText(R.id.tv_contract, contract)
                    //触发价格
                    it.setText(R.id.tv_trigger_price_value, triggerPrice)
                    //执行价格
                    it.setText(R.id.tv_execution_price_value, executionPrice)
                    //数量
                    it.setText(R.id.tv_amount_value, amountValue)
                    it.setText(R.id.tv_amount, context.getLineText("sl_str_amount") + "(" + context.getLineText("sl_str_contracts_unit") + ")")
                    //杠杆倍数
                    it.setText(R.id.tv_leverage_value, leverage)
                    //触发类型
                    it.setText(R.id.tv_trigger_price_type, triggerType)
                    //有效时间
                    it.setText(R.id.tv_str_trigger_time_value, triggerTime)

                })
                .addOnClickListener(R.id.btn_cancel, R.id.btn_ok, R.id.tv_not_remind)
                .setOnViewClickListener { it, view, tDialog ->
                    //是否提示
                    val cbNotRemind = it.getView<CheckBox>(R.id.cb_not_remind)
                    when (view.id) {
                        R.id.btn_cancel -> {
                            tDialog.dismiss()
                            PreferenceManager.getInstance(ContractSDKAgent.context).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, !cbNotRemind.isChecked)
                        }
                        R.id.btn_ok -> {
                            tDialog.dismiss()
                            sureLisener.sendConfirm()
                            PreferenceManager.getInstance(ContractSDKAgent.context).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, !cbNotRemind.isChecked)
                        }
                        R.id.tv_not_remind -> {
                            cbNotRemind.isChecked = !cbNotRemind.isChecked
                        }
                    }
                }
                .create()
                .show()

    }


    /**
     * 展示计算结果对话框
     */
    fun showCalculatorResultDialog(context: Context, itemList: List<TabInfo>?
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_calculator_result_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.tv_title).onLineText("sl_str_calculate_result")
                    it.getView<TextView>(R.id.tv_confirm_btn).onLineText("alert_common_i_understand")

                    val layoutInflater = LayoutInflater.from(context)
                    val llFeeWarpLayout = it.getView<LinearLayout>(R.id.ll_fee_warp_layout)
                    for (index in itemList!!.indices) {
                        val info = itemList[index]
                        val itemView = layoutInflater.inflate(R.layout.sl_auto_relative_item, llFeeWarpLayout, false)
                        llFeeWarpLayout.addView(itemView)
                        itemView.findViewById<TextView>(R.id.tv_left).text = info.name
                        itemView.findViewById<TextView>(R.id.tv_right).text = Html.fromHtml(info.extras)
                    }
                })
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 开通合约账号弹窗
     */
    fun showCreateContractAccountDialog(context: Context,
                                        listener: OnBindViewListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_create_contract_account_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setScreenHeightAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .create()
                .show()

    }

    /**
     * 展示选择杠杆对话框
     */
    fun showNewSelectLeverDialog(context: Context, bindListener: OnBindViewListener

    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_select_new_lever_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(bindListener)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 合约设置pop弹窗
     */
    fun createContractSetting(context: Context?, targetView: View, contractId: Int) {
        val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.sl_view_dropdown_contract_menu)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
        cvcEasyPopup?.run {
            findViewById<TextView>(R.id.tv_contract_setting).onLineText("sl_str_contract_settings")
            findViewById<TextView>(R.id.tv_contract_calculate).onLineText("sl_str_contract_calculator")
            findViewById<TextView>(R.id.tv_funds_transfer).onLineText("sl_str_funds_transfer")
            findViewById<TextView>(R.id.tv_funds_rate).onLineText("sl_str_tab_contract_info")
            findViewById<TextView>(R.id.tv_history_hold).onLineText("sl_str_history_hold")

            //合约设置
            findViewById<View>(R.id.ll_contract_setting).setOnClickListener {
                cvcEasyPopup.dismiss()
                SlContractSettingActivity.show(context as Activity)
            }
            //合约指南
//            findViewById<View>(R.id.ll_contract_guide).setOnClickListener {
//                UtilSystem.openHtmlActivity(BTConstants.BTURL_CONTRACT_GUIDE, context?.getString(R.string.sl_str_contract_guide))
//                cvcEasyPopup.dismiss()
//            }
            //合约计算器
            findViewById<View>(R.id.ll_contract_calculate).setOnClickListener {
                if (contractId > 0) {
                    SlContractCalculateActivity.show(context as Activity, contractId)
                }
                cvcEasyPopup.dismiss()
            }
            //资金划转
            findViewById<View>(R.id.ll_funds_transfer).setOnClickListener {
                if (LoginManager.checkLogin(context, true)) {
                    ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                        putString(ParamConstant.TRANSFERSTATUS, ParamConstant.TRANSFER_CONTRACT)
                        putString(ParamConstant.TRANSFERSYMBOL, "USDT")
                    })
                }
                cvcEasyPopup.dismiss()
            }
            //资金费率
            findViewById<View>(R.id.ll_funds_rate).setOnClickListener {
                if (contractId > 0) {
                    SlContractDetailActivity.show(context as Activity, contractId, 0)
                }
                cvcEasyPopup.dismiss()
            }
            //历史持仓
            findViewById<View>(R.id.ll_history_hold).setOnClickListener {
                if (contractId > 0) {
                    if (LoginManager.checkLogin(context, true)) {
                        SlContractHistoryHoldActivity.show(context as Activity, contractId)
                    }
                }
                cvcEasyPopup.dismiss()
            }
        }
        cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
    }

    /**
     * 合约设置pop弹窗
     */
    fun createContractSettingNew(context: Context?, targetView: View, contractId: Int, indexPrice: String, openContract: Int) {
        val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.sl_view_dropdown_contract_menu)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
        cvcEasyPopup?.run {
//            findViewById<TextView>(R.id.tv_contract_setting).onLineText("cl_preference_setting_str")
//            findViewById<TextView>(R.id.tv_contract_calculate).onLineText("sl_str_contract_calculator")
//            findViewById<TextView>(R.id.tv_funds_transfer).onLineText("sl_str_funds_transfer")
//            findViewById<TextView>(R.id.tv_funds_rate).onLineText("sl_str_tab_contract_info")
//            findViewById<TextView>(R.id.tv_history_hold).onLineText("assets_action_journalaccount")

            //合约设置
            findViewById<View>(R.id.ll_contract_setting).setOnClickListener {
                cvcEasyPopup.dismiss()
                if (LoginManager.checkLogin(context, true)) {
                    if (openContract == 0) {
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    } else {
                        ClContractSettingActivity.show(context as Activity, contractId, openContract)
                    }
                }
            }
            //合约指南
//            findViewById<View>(R.id.ll_contract_guide).setOnClickListener {
//                UtilSystem.openHtmlActivity(BTConstants.BTURL_CONTRACT_GUIDE, context?.getString(R.string.sl_str_contract_guide))
//                cvcEasyPopup.dismiss()
//            }
            //合约计算器
            findViewById<View>(R.id.ll_contract_calculate).setOnClickListener {
                if (contractId > 0) {
                    ClContractCalculateActivity.show(context as Activity, contractId, indexPrice)
                }
                cvcEasyPopup.dismiss()
            }
            //资金划转
            findViewById<View>(R.id.ll_funds_transfer).setOnClickListener {
                if (LoginManager.checkLogin(context, true)) {
                    if (openContract == 0) {
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    } else {
                        ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                            putString(ParamConstant.TRANSFERSTATUS, ParamConstant.TRANSFER_CONTRACT)
                            putString(ParamConstant.TRANSFERSYMBOL, "USDT")
                        })
                    }
                }
                cvcEasyPopup.dismiss()
            }
            //合约信息
            findViewById<View>(R.id.ll_funds_rate).setOnClickListener {
                if (contractId > 0) {
                    ClContractDetailActivity.show(context as Activity, contractId)
                }
                cvcEasyPopup.dismiss()
            }
            //资金流水
            findViewById<View>(R.id.ll_capital_flow).setOnClickListener {
                if (contractId > 0) {
                    if (LoginManager.checkLogin(context, true)) {
                        if (openContract == 0) {
                            EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                        } else {
                            ClContractAssetRecordActivity.show(context as Activity,LogicContractSetting.getContractMarginCoinById(context,contractId))
                        }
                    }
                }
                cvcEasyPopup.dismiss()
            }
            //历史持仓列表
            findViewById<View>(R.id.ll_history_hold).setOnClickListener {
                if (contractId > 0) {
                    if (LoginManager.checkLogin(context, true)) {
                        if (openContract == 0) {
                            EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                        } else {
                            ClContractHistoricalPositionActivity.show(context as Activity, contractId)
                        }
                    }
                }
                cvcEasyPopup.dismiss()
            }
        }
        cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
    }


    /**
     * 展示以实现盈亏明细
     */
    fun showProfitLossDetailsDialog(context: Context, obj: JSONObject): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cl_item_profit_loss_detail_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.tv_title).setText("盈亏明细")
                    it.getView<TextView>(R.id.tv_confirm_btn).onLineText("alert_common_i_understand")
                    val tv_value1 = it.getView<TextView>(R.id.tv_value1)//已实现盈亏
                    val tv_value2 = it.getView<TextView>(R.id.tv_value2)//手续费
                    val tv_value3 = it.getView<TextView>(R.id.tv_value3)//资金费用
                    val tv_value4 = it.getView<TextView>(R.id.tv_value4)//平仓盈亏
                    val tv_value5 = it.getView<TextView>(R.id.tv_value5)//分摊

                    val profitLossColor = if (BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(obj.optString("profitRealizedAmount"), obj.optInt("marginCoinPrecision")), "0") == 1) {
                        R.color.main_green
                    } else {
                        R.color.main_red
                    }
                    tv_value1.setTextColor(context.resources.getColor(profitLossColor))

                    tv_value1.setText(BigDecimalUtils.showSNormal(obj.optString("profitRealizedAmount"), obj.optInt("marginCoinPrecision"))+" "+obj.optString("marginCoin"))
                    tv_value2.setText(BigDecimalUtils.showSNormal(obj.optString("tradeFee"), obj.optInt("marginCoinPrecision"))+" "+obj.optString("marginCoin"))
                    tv_value3.setText(BigDecimalUtils.showSNormal(obj.optString("capitalFee"), obj.optInt("marginCoinPrecision"))+" "+obj.optString("marginCoin"))
                    tv_value4.setText(BigDecimalUtils.showSNormal(obj.optString("closeProfit"), obj.optInt("marginCoinPrecision"))+" "+obj.optString("marginCoin"))
                    tv_value5.setText(BigDecimalUtils.showSNormal(obj.optString("shareAmount"), obj.optInt("marginCoinPrecision"))+" "+obj.optString("marginCoin"))

                    var ret = 0
                    ret = BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(obj.optString("tradeFee"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_1).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(obj.optString("capitalFee"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_2).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(obj.optString("closeProfit"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_3).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(obj.optString("shareAmount"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_4).visibility = if (ret == 0) View.GONE else View.VISIBLE

                })
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

}