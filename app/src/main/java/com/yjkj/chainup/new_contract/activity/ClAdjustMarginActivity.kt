package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.chainup.contract.bean.CpContractPositionBean
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.extra.Contract.ContractCalculate
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.extension.showMarginName
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.numberFilter
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.cl_activity_adjust_margin.*
import org.json.JSONObject
import kotlin.math.min

/**
 * 调整保证金
 *
 */
class ClAdjustMarginActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_adjust_margin
    }

    private var contract: Contract? = null
    private var contractTicker: ContractTicker? = null
    private var contractAccount: ContractAccount? = null

    private var selectLeverage = 0 // 选择杠杆
    private var mPosition = ContractPosition()
    private var mMaxMargin = "0" //最大保证金
    private var mMinMargin = "0" //最小保证金
    private var marginCoinPrecision = 0
    var mPricePrecision = 0
    private lateinit var mContractJson: JSONObject
    private var currentPositionMargin = "0"
    private var dfValue = NumberUtil.getDecimal(-1)

    private var mContractPositionBean: CpContractPositionBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContractPositionBean = intent.getSerializableExtra("ContractPositionBean") as CpContractPositionBean?

//        mPosition = intent.getParcelableExtra("position")
//        contract = ContractPublicDataAgent.getContract(mPosition.instrument_id)
//        contractTicker = ContractPublicDataAgent.getContractTicker(mPosition.instrument_id)
//        if (contract == null || contractTicker == null) {
//            finish()
//            return
//        }
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
//        contractAccount = ContractUserDataAgent.getContractAccount(contract!!.margin_coin
//                ?: "")
//        dfValue = NumberUtil.getDecimal(contract!!.value_index)
//        et_deposit_amount.numberFilter(contract?.value_index ?: 4)
//        //计算保证金范围
//        doCalculateMaxMargin()
//        doCalculateMinMargin()

        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(mActivity, mContractPositionBean?.contractId!!)
        mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, mContractPositionBean?.contractId!!)
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
        mMaxMargin = BigDecimalUtils.addStr(currentPositionMargin, canUseAmount, mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision"))
        //保证金最小范围:
        mMinMargin = BigDecimalUtils.subStr(currentPositionMargin, canSubMarginAmount, mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision"))
        tv_margin_range.text = String.format(getLineText("sl_str_margin_range"), mMinMargin, mMaxMargin, mContractJson.optString("marginCoin"))
        et_deposit_amount.setText(BigDecimalUtils.showSNormal(currentPositionMargin.toString(), mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision")))
        tv_current_position_margin_value.setText(BigDecimalUtils.showSNormal(currentPositionMargin.toString(), mContractJson.optJSONObject("coinResultVo").optInt("marginCoinPrecision")))
        tv_margin_amount_title.setText("调整后的保证金" + "(" + mContractJson.optString("marginCoin") + ")")

    }


    override fun initView() {
        super.initView()
        initAutoTextView()
//        tv_expect_price_hint.text = getLineText("sl_str_adjust_liquidation_price") + "(" + contract!!.showQuoteName() + ")"
//        tv_expect_price.text = dfValue.format(doCalculateLiqPrice(mPosition))
//        updateMarginRangeUi()
    }

    private fun initAutoTextView() {
        title_layout.title = getLineText("sl_str_adjust_margins")
        tv_adjust_lever_after.onLineText("sl_str_adjust_lever_after")
//        tv_margin_amount_title.onLineText("sl_str_margin_amount")
        et_deposit_amount.hint = getLineText("sl_str_margin_amount")
        tv_confirm_btn.textContent = getLineText("common_text_btnConfirm")
    }


    private fun initListener() {
        tv_confirm_btn.isEnable(false)
        tv_confirm_btn.listener = object : CommonlyUsedButton.OnBottonListener {
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
        if (BigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) {
            // 增加保证金
            type = "1"
            amount = BigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
        } else {
            // 减少保证金
            type = "2"
            amount = BigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
        }
        addDisposable(getContractModel().modifyPositionMargin(mContractPositionBean?.contractId.toString(), mContractPositionBean?.id.toString(), type.toString(), amount,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        finish()
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_modify_position_margin_event))
                    }
                }))

//        addDisposable(getContractModel().getPublicInfo(
//                consumer = object : NDisposableObserver(mActivity, true) {
//                    override fun onResponseSuccess(jsonObject: JSONObject) {
//                        ToastUtils.showToast("调整保证金成功")
//                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_modify_position_margin_event))
//                    }
//                }))
    }

    /**
     * 更新强平价格和按钮状态
     */
    private fun updatePriceAndBtnUi() {
        var amount = et_deposit_amount.text.toString()
        if (TextUtils.isEmpty(amount) || TextUtils.equals(amount, ".") || BigDecimalUtils.compareTo(amount, "0") == 0) {
            tv_lever.text = "--"
            tv_expect_price.text = "--"
            return
        }
        LogUtil.d("DEBUG", "updatePriceAndBtnUi2 保证金:$amount")


//        if (BigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) {
//            amount=BigDecimalUtils.subStr(amount,currentPositionMargin,marginCoinPrecision)
//        } else {
//            amount=BigDecimalUtils.subStr(amount,currentPositionMargin,marginCoinPrecision)
//        }
        //计算杠杆、强平价格
        doDealLeverage(amount)
        if (BigDecimalUtils.compareTo(amount, mMinMargin) == 1 && BigDecimalUtils.compareTo(amount, mMaxMargin) == -1) {
            if (BigDecimalUtils.compareTo(amount, currentPositionMargin) == 0) {
                tv_confirm_btn.isEnable(false)
            } else {
                tv_confirm_btn.isEnable(true)
            }
        } else {
            tv_confirm_btn.isEnable(false)
            if (mPosition.position_type == 1) {
                tv_lever.text = "--"
                tv_expect_price.text = "--"
            }
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
        var positionEquity = BigDecimalUtils.calcPositionEquity(amount, realizedAmount, unRealizedAmount, 3)
        //仓位方向
        var positionDirection = if (mContractPositionBean?.orderSide.equals("BUY")) "1" else "-1"
        //维持保证金率
        var keepRate = mContractPositionBean?.keepRate
        //手续费率
        var maxFeeRate = mContractPositionBean?.maxFeeRate
        //仓位数量
        var positionVolume = BigDecimalUtils.mulStr(mContractPositionBean?.positionVolume, multiplier, 4)
        LogUtil.e(TAG, positionVolume)
        LogUtil.e(TAG, mContractPositionBean?.positionVolume)
        LogUtil.e(TAG, multiplier)

        //仓位方向：多仓是1，空仓是-1
        var reducePriceStr = BigDecimalUtils.calcForcedPrice(contractSide.equals("1"), positionEquity, marginRate, positionVolume, positionDirection, indexPrice, keepRate, maxFeeRate, mPricePrecision)
        if (BigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
            reducePriceStr = "--"
        }
        tv_expect_price.setText(reducePriceStr)

        /**
        实际杠杆（正向合约） = 仓位数量 * 标记价格 / 调整后仓位保证金 / 保证金汇率
        实际杠杆（反向合约） = 仓位数量 / 标记价格 / 调整后仓位保证金 / 保证金汇率
         */

        var adjustingLever = "0X"
        adjustingLever = if (contractSide.equals("1")) {
            //正向
            val buff1 = BigDecimalUtils.mul(positionVolume, indexPrice)//仓位数量 * 标记价格
            val buff2 = BigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
            BigDecimalUtils.div(buff1, buff2, 1)
        } else {
            val buff1 = BigDecimalUtils.div(positionVolume, indexPrice)//仓位数量 / 标记价格
            val buff2 = BigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
            BigDecimalUtils.div(buff1, buff2, 1)
        }
        if (BigDecimalUtils.compareTo(adjustingLever, "0") != 1) {
            adjustingLever = "--"
        }
        tv_lever.setText(adjustingLever + "X")
    }


    /**
     * 计算强平价格
     */
    private fun doCalculateLiqPrice(info: ContractPosition): Double {
        var mLiqPrice = 0.0 //强平价
        info.let {
            val openType: Int = it.position_type
            if (openType == 1) {
                mLiqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                        it, null, contract!!)
            } else if (openType == 2) {
                if (contractAccount != null) {
                    mLiqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                            it, contractAccount, contract!!)
                }
            }
        }
        LogUtil.d("DEBUG", "计算强平价：$mLiqPrice")
        return mLiqPrice
    }

    /**
     * 更新保证金范围UI
     */
    private fun updateMarginRangeUi() {
        tv_margin_range.text = String.format(getLineText("sl_str_margin_range"), mMinMargin, mMaxMargin, contract!!.showMarginName())
        et_deposit_amount.setText(MathHelper.round(mPosition.im, contract?.value_index
                ?: -1).toString())

    }

    /**
     * 计算未实现盈亏
     */
    private fun doCalculateProfitAmount(): Double {
        var profitAmount = 0.0 //未实现盈亏
        mPosition.let {
            val pnlCalculate: Int = LogicContractSetting.getPnlCalculate(mContext)
            when (it.side) {
                1 -> {
                    //多仓
                    profitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                            it.cur_qty,
                            it.avg_cost_px,
                            if (pnlCalculate == 0) contractTicker?.fair_px else contractTicker?.last_px,
                            contract?.face_value,
                            contract!!.isReserve)
                }
                2 -> {
                    //空仓
                    profitAmount += ContractCalculate.CalculateCloseShortProfitAmount(
                            it.cur_qty,
                            it.avg_cost_px,
                            if (pnlCalculate == 0) contractTicker?.fair_px else contractTicker?.last_px,
                            contract?.face_value,
                            contract!!.isReserve)
                }
            }
        }
        return profitAmount
    }

    /**
     * 保证金最小值
     * 当前仓位保证金-最大可减少保证金额
     */
    private fun doCalculateMinMargin() {
//        mPosition.let {
//            var maxReduce = doCalculateCanMinMargin()
//            mMinMargin = MathHelper.round(MathHelper.sub(it.im, maxReduce.toString()), contract!!.value_index)
//            LogUtil.d("DEBUG", "最大可减少保证金:$maxReduce ;保证金最小值mMinMargin2：$mMinMargin")
//            updateMarginRangeUi()
//        }
    }

    /**
     * 计算最大可减少 = 当前仓位保证金- 合约价值*开仓保证金率 + min(未实现盈亏,0)
     */
    private fun doCalculateCanMinMargin(): Double {
        var maxReduce = 0.0 //最大可减少
        //仓位的开仓保证金率
        val IMR = ContractCalculate.CalculatePositionIMR(mPosition, contract!!)
        //合约价值
        val value = ContractCalculate.CalculateContractValue(
                mPosition.cur_qty,
                mPosition.avg_cost_px,
                contract)
        LogUtil.d("DEBUG", "合约价值:$value;仓位的开仓保证金率:$IMR")
        //未实现盈亏
        var profitAmount = 0.0 //未实现盈亏额
        when (mPosition.side) {
            1 -> {
                //多仓
                profitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                        mPosition.cur_qty,
                        mPosition.avg_cost_px,
                        contractTicker?.fair_px,
                        contract!!.face_value,
                        contract!!.isReserve)
                val p: Double = MathHelper.add(mPosition.cur_qty, mPosition.close_qty)
                val plus: Double = MathHelper.mul(
                        MathHelper.round(mPosition.tax),
                        MathHelper.div(MathHelper.round(mPosition.cur_qty), p))
            }
            2 -> {
                //空仓
                profitAmount += ContractCalculate.CalculateCloseShortProfitAmount(
                        mPosition.cur_qty,
                        mPosition.avg_cost_px,
                        contractTicker?.fair_px,
                        contract!!.face_value,
                        contract!!.isReserve)

                val p: Double = MathHelper.add(mPosition.cur_qty, mPosition.close_qty)
                val plus = MathHelper.mul(
                        MathHelper.round(mPosition.tax),
                        MathHelper.div(MathHelper.round(mPosition.cur_qty), p))
            }
            else -> {
            }
        }

        profitAmount = min(profitAmount, 0.0)
        LogUtil.d("DEBUG", "未实现盈亏:$profitAmount")
        maxReduce = MathHelper.add(MathHelper.sub(mPosition.im.toDouble(), value * IMR), profitAmount)

        return MathHelper.round(maxReduce, contract!!.value_index)
    }

    /**
     * 保证金最大值
     * 当前仓位保证金+最大可增加保证金额
     */
    private fun doCalculateMaxMargin() {
//        mPosition.let {
//            if (contractAccount != null) {
//                var availableVolReal = contractAccount?.available_vol_real
//                mMaxMargin = MathHelper.round(MathHelper.add(availableVolReal.toString(), it.im), contract!!.value_index)
//                LogUtil.d("DEBUG", "保证金最大值mMaxMargin：$mMaxMargin")
//            }
//
//        }
    }

    companion object {
        fun show(activity: Activity, mContractPositionBean: CpContractPositionBean) {
            val intent = Intent(activity, ClAdjustMarginActivity::class.java)
            intent.putExtra("ContractPositionBean", mContractPositionBean)
            activity.startActivity(intent)
        }
    }
}