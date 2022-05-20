package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.*
import com.chainup.contract.view.CpCommonlyUsedButton
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.chainup.contract.bean.CpContractPositionBean
import kotlinx.android.synthetic.main.cp_activity_adjust_margin.*
import org.json.JSONObject

/**
 * 调整保证金
 */
class CpAdjustMarginActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_adjust_margin
    }
    private var mMaxMargin = "0" //最大保证金
    private var mMinMargin = "0" //最小保证金
    private var marginCoinPrecision = 0
    var mPricePrecision = 0
    private lateinit var mContractJson: JSONObject
    private var currentPositionMargin = "0"
    private var mContractPositionBean: CpContractPositionBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContractPositionBean = intent.getSerializableExtra("ContractPositionBean") as CpContractPositionBean?
        initListener()
        loadData()
        initView()
    }

    /**
     *
     *
    杠杆&强平价格
    仓位保证金 = 新键入的仓位保证金
    实际杠杆 = 仓位数量 * 标记价格 / 仓位保证金
    强平价 = （仓位保证金/保证金汇率 + 已实现盈亏/保证金汇率 - 持仓均价 * 仓位） / （仓位 - （维持保证金率+手续费率）* 仓位数量）
     *
     *
     */

    override fun loadData() {
        super.loadData()
        mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(mActivity, mContractPositionBean?.contractId!!)
        mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, mContractPositionBean?.contractId!!)
        marginCoinPrecision = mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision")
        et_deposit_amount.numberFilter(marginCoinPrecision)
        //保证金范围：[当前仓位保证金-可减，当前仓位保证金+可用]
        //当前仓位保证金：
        currentPositionMargin = mContractPositionBean?.holdAmount.toString()
        //可减保证金：
        var canSubMarginAmount = mContractPositionBean?.canSubMarginAmount
        //可用
        var canUseAmount = mContractPositionBean?.canUseAmount
        //保证金最大范围:
        mMaxMargin = CpBigDecimalUtils.addStr(currentPositionMargin, canUseAmount, mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision"))
        //保证金最小范围:
        mMinMargin = CpBigDecimalUtils.subStr(currentPositionMargin, canSubMarginAmount, mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision"))
        tv_margin_range.text = String.format(getString(R.string.cp_str_margin_range), mMinMargin, mMaxMargin, mContractJson.optString("marginCoin"))
        et_deposit_amount.setText(CpBigDecimalUtils.showSNormal(currentPositionMargin.toString(), mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision")))
        tv_current_position_margin_value.setText(CpBigDecimalUtils.showSNormal(currentPositionMargin.toString(), mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision")))
        tv_margin_amount_title.setText("调整后的保证金" + "(" + mContractJson.optString("marginCoin") + ")")

    }


    override fun initView() {
        super.initView()
        initAutoTextView()

    }

    private fun initAutoTextView() {
        title_layout.title = getString(R.string.cp_order_text16)
        tv_adjust_lever_after.setText(getString(R.string.cp_order_text27))
//        tv_margin_amount_title.onLineText("cp_calculator_text39")
        et_deposit_amount.hint = getString(R.string.cp_calculator_text39)
        tv_confirm_btn.textContent = getString(R.string.cp_calculator_text16)
    }


    private fun initListener() {
        tv_confirm_btn.isEnable(false)
        tv_confirm_btn.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                doAdjustMarginRequest()
            }
        }
        /**
         * 保证金数量
         */
        et_deposit_amount.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        updatePriceAndBtnUi()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                })
    }

    /**
     * 提交保证金修改
     */
    private fun doAdjustMarginRequest() {
        var amount = et_deposit_amount.text.toString()
        var type = "1"
        if (CpBigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) {
            // 增加保证金
            type = "1"
            amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
        } else {
            // 减少保证金
            type = "2"
            amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
        }
        addDisposable(getContractModel().modifyPositionMargin(mContractPositionBean?.contractId.toString(), mContractPositionBean?.id.toString(), type.toString(), amount,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        finish()
                        CpEventBusUtil.post(
                            CpMessageEvent(
                                CpMessageEvent.sl_contract_modify_position_margin_event
                            )
                        )
                    }
                }))
    }

    /**
     * 更新强平价格和按钮状态
     */
    private fun updatePriceAndBtnUi() {
        var amount = et_deposit_amount.text.toString()
        if (TextUtils.isEmpty(amount) || TextUtils.equals(amount, ".") || CpBigDecimalUtils.compareTo(amount, "0") == 0) {
            tv_lever.text = "--"
            tv_expect_price.text = "--"
            return
        }
        ChainUpLogUtil.d("DEBUG", "updatePriceAndBtnUi2 保证金:$amount")
        //计算杠杆、强平价格
        doDealLeverage(amount)
        if (CpBigDecimalUtils.compareTo(amount, mMinMargin) == 1 && CpBigDecimalUtils.compareTo(amount, mMaxMargin) == -1) {
            if (CpBigDecimalUtils.compareTo(amount, currentPositionMargin) == 0) {
                tv_confirm_btn.isEnable(false)
            } else {
                tv_confirm_btn.isEnable(true)
            }
        } else {
            tv_confirm_btn.isEnable(false)
        }
    }

    /**
     * 杠杆和保证金之间的联动
     * 1.逐仓：杠杆 = 仓位价值/（仓位保证金+未实现盈亏）
     * 2.全仓：杠杆 = 仓位价值/（仓位保证金+未实现盈亏+（可用余额-该币种其他全仓亏损））
     */
    private fun doDealLeverage(amount: String) {
        //保证金汇率:
        var marginRate = mContractJson.optString("marginRate")
        //面值:
        var multiplier = mContractJson.optString("multiplier")
        //标记价格
        var indexPrice = mContractPositionBean?.indexPrice
        //合约方向:(反向：0，正向 : 1)
        var contractSide = mContractJson.optString("contractSide")
        //已实现盈亏
        val realizedAmount = mContractPositionBean?.realizedAmount
        //未实现盈亏
        val unRealizedAmount = mContractPositionBean?.unRealizedAmount
        //逐仓权益
        var positionEquity = CpBigDecimalUtils.calcPositionEquity(amount, realizedAmount, unRealizedAmount, 3)
        //仓位方向
        var positionDirection = if (mContractPositionBean?.orderSide.equals("BUY")) "1" else "-1"
        //维持保证金率
        var keepRate = mContractPositionBean?.keepRate
        //手续费率
        var maxFeeRate = mContractPositionBean?.maxFeeRate
        //仓位数量
        var positionVolume = CpBigDecimalUtils.mulStr(mContractPositionBean?.positionVolume, multiplier, 4)
        ChainUpLogUtil.e(TAG, positionVolume)
        ChainUpLogUtil.e(TAG, mContractPositionBean?.positionVolume)
        ChainUpLogUtil.e(TAG, multiplier)

        //仓位方向：多仓是1，空仓是-1
        var reducePriceStr = CpBigDecimalUtils.calcForcedPrice(contractSide.equals("1"), positionEquity, marginRate, positionVolume, positionDirection, indexPrice, keepRate, maxFeeRate, mPricePrecision)
        if (CpBigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
            reducePriceStr = "--"
        }
        tv_expect_price.setText(reducePriceStr)

        /**
        实际杠杆（正向合约） = 仓位数量 * 标记价格 / 调整后仓位保证金 / 保证金汇率
        实际杠杆（反向合约） = 仓位数量 / 标记价格 / 调整后仓位保证金 / 保证金汇率
         */

        var adjustingLever = "0X"
        if (contractSide.equals("1")) {
            //正向
            val buff1 = CpBigDecimalUtils.mul(positionVolume, indexPrice)//仓位数量 * 标记价格
            val buff2 = CpBigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
            adjustingLever = CpBigDecimalUtils.div(buff1, buff2, 1)
        } else {
            val buff1 = CpBigDecimalUtils.div(positionVolume, indexPrice)//仓位数量 / 标记价格
            val buff2 = CpBigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
            adjustingLever = CpBigDecimalUtils.div(buff1, buff2, 1)
        }
        if (CpBigDecimalUtils.compareTo(adjustingLever, "0") != 1) {
            adjustingLever = "--"
        }
        tv_lever.setText(adjustingLever + "X")
    }
    companion object {
        fun show(activity: Activity, mContractPositionBean: CpContractPositionBean) {
            val intent = Intent(activity, CpAdjustMarginActivity::class.java)
            intent.putExtra("ContractPositionBean", mContractPositionBean)
            activity.startActivity(intent)
        }
    }
}