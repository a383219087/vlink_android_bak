package com.chainup.contract.view

import android.app.Activity
import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpContractSettingActivity
import com.chainup.contract.ui.activity.CpWebViewActivity
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpPreferenceManager
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.new_contract.activity.CpContractAssetRecordActivity
import com.yjkj.chainup.new_contract.activity.CpContractCalculateActivity
import com.yjkj.chainup.new_contract.activity.CpContractDetailActivity
import com.yjkj.chainup.new_contract.activity.CpContractHistoricalPositionActivity
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import org.json.JSONObject


object CpSlDialogHelper {


    /**
     * 展示计算结果对话框
     */
    fun showCalculatorResultDialog(context: Context, itemList: List<CpTabInfo>?
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_calculator_result_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener {
                    it.getView<TextView>(R.id.tv_title).setText(R.string.cp_calculator_text12)
                    it.getView<TextView>(R.id.tv_confirm_btn).setText(R.string.cp_extra_text28)

                    val layoutInflater = LayoutInflater.from(context)
                    val llFeeWarpLayout = it.getView<LinearLayout>(R.id.ll_fee_warp_layout)
                    for (index in itemList!!.indices) {
                        val info = itemList[index]
                        val itemView = layoutInflater.inflate(R.layout.cp_auto_relative_item, llFeeWarpLayout, false)
                        llFeeWarpLayout.addView(itemView)
                        itemView.findViewById<TextView>(R.id.tv_left).text = info.name
                        itemView.findViewById<TextView>(R.id.tv_right).text = Html.fromHtml(info.extras)
                    }
                }
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
     * 合约设置pop弹窗
     */
    fun createContractSettingNew(context: Context?, targetView: View, contractId: Int, indexPrice: String, openContract: Int) {
        val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_view_dropdown_contract_menu)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setDimValue(0.3f)
                .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setDimValue(0.3f)
                .setAnimationStyle(R.style.moreBoxAnim)
                .apply()
        cvcEasyPopup?.run {
            val llHistoryHold = findViewById<LinearLayout>(R.id.ll_history_hold)
            val llFundsTransfer = findViewById<LinearLayout>(R.id.ll_funds_transfer)
            val llCapitalFlow = findViewById<LinearLayout>(R.id.ll_capital_flow)
            val llFundsRate = findViewById<LinearLayout>(R.id.ll_funds_rate)
            val llContractSetting = findViewById<LinearLayout>(R.id.ll_contract_setting)
            val llContractCalculate = findViewById<LinearLayout>(R.id.ll_contract_calculate)
            val llContractGuide = findViewById<LinearLayout>(R.id.ll_contract_guide)

            //合约设置
            llContractSetting.setOnClickListener {
                cvcEasyPopup.dismiss()
                if (!CpClLogicContractSetting.isLogin()) {
                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                    return@setOnClickListener
                }
                if (openContract == 0) {
                    CpEventBusUtil.post(
                            CpMessageEvent(
                                    CpMessageEvent.sl_contract_create_account_event
                            )
                    )
                } else {
                    CpContractSettingActivity.show(context as Activity, contractId, openContract)
                }
            }
            //合约计算器
            llContractCalculate.setOnClickListener {
                if (contractId > 0) {
                    CpContractCalculateActivity.show(context as Activity, contractId, indexPrice)
                }
                cvcEasyPopup.dismiss()
            }
            //资金划转
            llFundsTransfer.setOnClickListener {
                if (!CpClLogicContractSetting.isLogin()) {
                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                    return@setOnClickListener
                }
                if (openContract == 0) {
                    CpEventBusUtil.post(
                            CpMessageEvent(
                                    CpMessageEvent.sl_contract_create_account_event
                            )
                    )
                } else {
                    //资金划转
                    val mMessageEvent =
                            CpMessageEvent(CpMessageEvent.sl_contract_go_fundsTransfer_page)
                    mMessageEvent.msg_content = CpClLogicContractSetting.getContractMarginCoinById(context as Activity, contractId)
                    CpEventBusUtil.post(mMessageEvent)
                }
                cvcEasyPopup.dismiss()
            }
            //合约信息
            llFundsRate.setOnClickListener {
                if (contractId > 0) {
                    CpContractDetailActivity.show(context as Activity, contractId)
                }
                cvcEasyPopup.dismiss()
            }
            //资金流水
            llCapitalFlow.setOnClickListener {
                if (contractId > 0) {
                    if (!CpClLogicContractSetting.isLogin()) {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                        return@setOnClickListener
                    }
                    if (openContract == 0) {
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_create_account_event
                                )
                        )
                    } else {
                        CpContractAssetRecordActivity.show(context as Activity)
                    }
                }
                cvcEasyPopup.dismiss()
            }
            //历史持仓列表
            llHistoryHold.setOnClickListener {
                if (contractId > 0) {
                    if (!CpClLogicContractSetting.isLogin()) {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                        return@setOnClickListener
                    }
                    if (openContract == 0) {
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_create_account_event
                                )
                        )
                    } else {
                        CpContractHistoricalPositionActivity.show(context as Activity, contractId)
                    }
                }
                cvcEasyPopup.dismiss()
            }
            llContractGuide.setOnClickListener {
                val url="https://chainupfutures.zendesk.com/hc/zh-cn/categories/900001405603-%E6%B0%B8%E7%BB%AD%E5%90%88%E7%BA%A6"
                CpWebViewActivity().loadUrl(context as Activity,url)
                cvcEasyPopup.dismiss()
            }

        }
        cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
    }


    /**
     * 展示以实现盈亏明细
     */
    fun showProfitLossDetailsDialog(context: Context, obj: JSONObject,type:Int): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_profit_loss_detail_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.tv_title).setText(R.string.cp_calculator_text12)
                    it.getView<TextView>(R.id.tv_confirm_btn).setText(
                            com.chainup.contract.R.string.cp_extra_text28)
                    val tv_value1 = it.getView<TextView>(R.id.tv_value1)//已实现盈亏
                    val tv_value2 = it.getView<TextView>(R.id.tv_value2)//手续费
                    val tv_value3 = it.getView<TextView>(R.id.tv_value3)//资金费用
                    val tv_value4 = it.getView<TextView>(R.id.tv_value4)//平仓盈亏
                    val tv_value5 = it.getView<TextView>(R.id.tv_value5)//分摊
                    val tv_value6 = it.getView<TextView>(R.id.tv_value6)//持仓结算
                    val tv_loss_desc = it.getView<TextView>(R.id.tv_loss_desc)//盈亏明细解释文案
                    tv_loss_desc.setText(if (type==0) R.string.cp_extra_text115 else R.string.cp_extra_text108)
                    val profitLossColor = if (CpBigDecimalUtils.compareTo(
                                    CpBigDecimalUtils.showSNormal(obj.optString("profitRealizedAmount"), obj.optInt("marginCoinPrecision")), "0") == 1) {
                        R.color.main_green
                    } else {
                        R.color.main_red
                    }
                    tv_value1.setTextColor(context.resources.getColor(profitLossColor))

                    tv_value1.setText(CpBigDecimalUtils.showSNormal(obj.optString("profitRealizedAmount"), obj.optInt("marginCoinPrecision")) + " "+obj.optString("marginCoin"))
                    tv_value2.setText(CpBigDecimalUtils.showSNormal(obj.optString("tradeFee"), obj.optInt("marginCoinPrecision")) + " "+obj.optString("marginCoin"))
                    tv_value3.setText(CpBigDecimalUtils.showSNormal(obj.optString("capitalFee"), obj.optInt("marginCoinPrecision")) +" "+ obj.optString("marginCoin"))
                    tv_value4.setText(CpBigDecimalUtils.showSNormal(obj.optString("closeProfit"), obj.optInt("marginCoinPrecision")) +" "+ obj.optString("marginCoin"))
                    tv_value5.setText(CpBigDecimalUtils.showSNormal(obj.optString("shareAmount"), obj.optInt("marginCoinPrecision")) +" "+ obj.optString("marginCoin"))
                    tv_value6.setText(CpBigDecimalUtils.showSNormal(obj.optString("settleProfit"), obj.optInt("marginCoinPrecision")) +" "+ obj.optString("marginCoin"))

                    var ret = 0
                    ret = CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(obj.optString("tradeFee"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_1).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(obj.optString("capitalFee"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_2).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(obj.optString("closeProfit"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_3).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(obj.optString("shareAmount"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_4).visibility = if (ret == 0) View.GONE else View.VISIBLE
                    ret = CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(obj.optString("settleProfit"), obj.optInt("marginCoinPrecision")), "0")
                    it.getView<RelativeLayout>(R.id.rl_5).visibility = if (ret == 0) View.GONE else View.VISIBLE

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

    fun showSubmitProfitLossDetailsDialog(context: Context, listener: CpNewDialogUtils.DialogBottomListener?, profit_title: String, loss_title: String, profit_info: String, loss_info: String): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_submit_profit_loss_detail_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    val tv_profit_title = it.getView<TextView>(R.id.tv_profit_title)
                    val tv_profit_info = it.getView<TextView>(R.id.tv_profit_info)
                    val tv_loss_title = it.getView<TextView>(R.id.tv_loss_title)
                    val tv_loss_info = it.getView<TextView>(R.id.tv_loss_info)

                    tv_profit_title.setText(profit_title)
                    tv_profit_info.setText(profit_info)

                    tv_loss_title.setText(loss_title)
                    tv_loss_info.setText(loss_info)

                    tv_profit_title.visibility = if (TextUtils.isEmpty(profit_title)) View.GONE else View.VISIBLE
                    tv_profit_info.visibility = if (TextUtils.isEmpty(profit_title)) View.GONE else View.VISIBLE


                    tv_loss_title.visibility = if (TextUtils.isEmpty(loss_title)) View.GONE else View.VISIBLE
                    tv_loss_info.visibility = if (TextUtils.isEmpty(loss_title)) View.GONE else View.VISIBLE

                })
                .addOnClickListener(R.id.btn_cancel, R.id.btn_ok, R.id.ll_not_again)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.btn_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.btn_ok -> {
                            tDialog.dismiss()
                            listener?.sendConfirm()
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            CpPreferenceManager.getInstance(CpMyApp.instance()).putSharedBoolean(
                                    CpPreferenceManager.PREF_LOSS_CONFIRM, !cbNotAgain.isChecked)
                        }
                        R.id.ll_not_again -> {
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            cbNotAgain.isChecked = !cbNotAgain.isChecked
                        }
                    }
                }
                .create()
                .show()

    }

    fun showSubmitProfitLossDetailsDialog(context: Context): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_submit_profit_loss_tip)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                })
                .addOnClickListener(R.id.btn_cancel, R.id.btn_ok)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.btn_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.btn_ok -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

}