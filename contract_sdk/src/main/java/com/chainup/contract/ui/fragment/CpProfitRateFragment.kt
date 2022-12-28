package com.yjkj.chainup.new_contract.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.fragment.CpCapitalRateFragment
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpNToastUtil
import com.chainup.contract.view.CpCommonlyUsedButton
import com.chainup.contract.view.CpNewDialogUtils
import com.chainup.contract.view.CpSlDialogHelper
import com.chainup.contract.view.bubble.CpBubbleSeekBar
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import kotlinx.android.synthetic.main.cp_fragment_contract_calculate_item.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


/**
 * 平仓价格
 */
class CpProfitRateFragment : CpNBaseFragment() {

    override fun setContentView(): Int {
        return R.layout.cp_fragment_contract_calculate_item
    }


    protected val directionList = ArrayList<CpTabInfo>()
    protected var currDirectionInfo: CpTabInfo? = null
    protected var directionDialog: TDialog? = null
    private var contractId = 0
    private lateinit var mLadderList: JSONArray
    private lateinit var mContractJson: JSONObject



    private var isPriceLongClick: Boolean = false
    private var isStartPriceSubClick = false
    private var isStartPricePlusClick = false
    private var coUnit = 0
    private var multiplierCoin = ""
    private var multiplier = "0"
    private var MultiplierCoinPrecision = 0
    private lateinit var leverCeilingObject: JSONObject
    private lateinit var leverCeilingList: ArrayList<Int>
    private var minLeverage = 1
    private var maxLeverage = 100
    private var selectLeverage = 0 // 选择杠杆

    override fun initView() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initListener()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        contractId = arguments?.getInt("contractId")!!
        mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
        maxLeverage = mContractJson.optInt("maxLever")
        minLeverage = mContractJson.optInt("minLever")
        selectLeverage = 20

        ll_extras_layout.visibility = View.GONE
        tv_position_title.setText(getString(R.string.cp_calculator_text15))
        et_position.setHint(getString(R.string.cp_calculator_text15))
        tv_open_price_symbol.setText(mContractJson.optString("quote"))
        tv_position_symbol.setText("%")
        tv_extras_symbol.setText(mContractJson.optString("marginCoin"))
        tv_open_price_symbol.setText(mContractJson.optString("quote"))

        //方向
        directionList.add(CpTabInfo(getString(R.string.cp_calculator_text21), 0))
        directionList.add(CpTabInfo(getString(R.string.cp_calculator_text22), 1))
        currDirectionInfo = directionList[0]
        tv_direction_value.text = currDirectionInfo?.name

        rb_buy.setOnClickListener {
            changeBuyOrSellUI("buy")
            currDirectionInfo = directionList[0]
        }

        rb_sell.setOnClickListener {
            changeBuyOrSellUI("sell")
            currDirectionInfo = directionList[1]
        }

        //方向
        rl_direction_layout.setOnClickListener {
            directionDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity!!, directionList, currDirectionInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currDirectionInfo = directionList[index]
                    directionDialog?.dismiss()
                    tv_direction_value.text = currDirectionInfo?.name
                }
            })
        }
        btn_calculate.isEnable(true)
        btn_calculate.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var etPositionStr = et_position.text.toString()
                var etOpenPriceStr = et_open_price.text.toString()
                selectLeverage = et_input.text.toString().toInt()
                if (TextUtils.isEmpty(etPositionStr) || CpBigDecimalUtils.compareTo(etPositionStr, "0") != 1) {
                    CpNToastUtil.showTopToastNet(this@CpProfitRateFragment.mActivity,false, getString(R.string.cp_extra_text119))
                    return
                }
                if (TextUtils.isEmpty(etOpenPriceStr) || CpBigDecimalUtils.compareTo(etOpenPriceStr, "0") != 1) {
                    CpNToastUtil.showTopToastNet(this@CpProfitRateFragment.mActivity,false, getString(R.string.cp_extra_text35))
                    return
                }
                val closePrice = CpBigDecimalUtils.calcClosePriceValue(mContractJson.optString("contractSide").equals("1"), currDirectionInfo?.index!!, etPositionStr, etOpenPriceStr, selectLeverage.toString(), 4)
                //平仓价格计算出来为负数时显示：无法达到该收益率
                if (CpBigDecimalUtils.compareTo(closePrice,"0") != 1) {
                    CpNToastUtil.showTopToastNet(this@CpProfitRateFragment.mActivity,false, getString(R.string.cp_extra_text118))
                    return
                }

                val tabList = ArrayList<CpTabInfo>()
                tabList.add(CpTabInfo(getString(R.string.cp_calculator_text3), closePrice + mContractJson.optString("quote")))
                CpSlDialogHelper.showCalculatorResultDialog(mActivity!!, tabList)
            }
        }
        tv_add?.setOnTouchListener { _, event ->
            isPriceLongClick = true
            isStartPricePlusClick = true
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var inputLeverage = CpBigDecimalUtils.add(et_input.text.toString(), "1").toPlainString()
                    et_input?.setText(CpBigDecimalUtils.subZeroAndDot(inputLeverage))

                    doAsync {
                        while (isPriceLongClick) {
                            Thread.sleep(100L)
                            if (!isStartPricePlusClick) continue
                            inputLeverage = try {
                                CpBigDecimalUtils.add(et_input.text.toString(), "1").toPlainString()
                            } catch (e: NumberFormatException) {
                                ""
                            }

                            uiThread {
                                et_input?.setText(CpBigDecimalUtils.subZeroAndDot(inputLeverage))
                            }

                        }
                    }

                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPriceLongClick = false
                    isStartPricePlusClick = false
                }
            }
            true
        }
        tv_sub?.setOnTouchListener { _, event ->
            isPriceLongClick = true
            isStartPriceSubClick = true
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var inputLeverage = CpBigDecimalUtils.sub(et_input.text.toString(), "1").toPlainString()
                    et_input?.setText(CpBigDecimalUtils.subZeroAndDot(inputLeverage))

                    doAsync {
                        while (isPriceLongClick) {
                            Thread.sleep(100L)
                            if (!isStartPriceSubClick) continue
                            inputLeverage = try {
                                CpBigDecimalUtils.sub(et_input.text.toString(), "1").toPlainString()
                            } catch (e: NumberFormatException) {
                                ""
                            }

                            uiThread {
                                et_input?.setText(CpBigDecimalUtils.subZeroAndDot(inputLeverage))
                            }

                        }
                    }

                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPriceLongClick = false
                    isStartPriceSubClick = false
                }
            }
            true
        }
        getLadderInfo()
    }

    private fun getLadderInfo() {
        multiplierCoin = mContractJson.optString("multiplierCoin")
        multiplier = mContractJson.optString("multiplier")
        MultiplierCoinPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        coUnit = CpClLogicContractSetting.getContractUint(mActivity)
        leverCeilingList=ArrayList()
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : CpNDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            mLadderList = optJSONObject("leverList").optJSONArray("leverList")
                            try {
                                leverCeilingObject = optJSONObject("leverCeiling")
                                val iteratorKeys = leverCeilingObject.keys()
                                while (iteratorKeys.hasNext()) {
                                    val key = iteratorKeys.next().toString()
                                    leverCeilingList.add(key.toInt())
                                }
                            } catch (e: Exception) {
                            } finally {
                                initSeekBarUi()
                            }
                        }
                    }
                }))
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_calc_switch_contract_event -> {
                et_position.setText("")
                et_open_price.setText("")
                et_extras.setText("")


                contractId = event.msg_content as Int
                mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
                tv_open_price_symbol.setText(mContractJson.optString("quote"))
                tv_extras_symbol.setText(mContractJson.optString("quote"))
                tv_position_symbol.setText("%")
                maxLeverage = mContractJson.optInt("maxLever")
                minLeverage = mContractJson.optInt("minLever")
                getLadderInfo()
            }
        }
    }


    private fun initSeekBarUi() {
        seekbar.configBuilder
                .min(1.toFloat())
                .max(maxLeverage.toFloat())
                .progress(selectLeverage.toFloat())
                .build()
        seekbar.onProgressChangedListener = object : CpBubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
                et_input.setText(progress.toString())
                et_input.setSelection(progress.toString().length)
            }

            override fun getProgressOnActionUp(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
            }
        }

        et_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (TextUtils.isEmpty(s.toString())) {
                    seekbar.setProgress(minLeverage.toFloat())
                    return
                }
                var leverage = et_input.text.toString().toInt()
                if (leverage > maxLeverage) {
                    et_input.setText("$maxLeverage")
                    et_input.setSelection(maxLeverage.toString().length)
                    leverage = maxLeverage
                }
                if (leverage < minLeverage) {
                    et_input.setText("$minLeverage")
                    et_input.setSelection(minLeverage.toString().length)
                    leverage = minLeverage
                }
                seekbar.setProgress(et_input.text.toString().toFloat())

                leverCeilingList.clear()
                var unit = ""
                if (coUnit == 1) {
                    unit = multiplierCoin
                } else {
                    unit = getString(R.string.cp_overview_text9)
                }
                var max = ""
                if (this@CpProfitRateFragment::leverCeilingObject.isInitialized) {
                    val iteratorKeys = leverCeilingObject.keys()
                    while (iteratorKeys.hasNext()) {
                        val key = iteratorKeys.next().toString()
                        leverCeilingList.add(key.toInt())
                    }

                    var isExist = false
                    for (buff in leverCeilingList) {
                        if (leverage == buff) {
                            isExist = true
                        }
                    }
                    if (!isExist) {
                        leverCeilingList.add(leverage)
                    }
                    leverCeilingList.sort()
                    var indexBuff = 0
                    for (index in leverCeilingList.indices) {
                        if (leverCeilingList[index] == leverage) {
                            indexBuff = index
                        }
                    }
                    if (!isExist) {
                        indexBuff++
                    }
                    max = CpBigDecimalUtils.showSNormal(leverCeilingObject.optString(leverCeilingList[indexBuff].toString()), CpClLogicContractSetting.getContractMarginCoinPrecisionById(mActivity, contractId))
                    if (coUnit == 1) {
                    } else {
                        max = CpBigDecimalUtils.divStr(max, multiplier, 0)
                    }
                    tv_amount_user_max.setText(getString(R.string.cp_calculator_text7) + max + " " + unit)
                } else {
                    max = "0"
                    tv_amount_user_max.setText(getString(R.string.cp_calculator_text7) + max + " " + unit)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        et_input.setText(selectLeverage.toString())
    }
    private fun changeBuyOrSellUI(type: String) {
        when (type) {
            // 买
            "buy" -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = R.drawable.coins_exchange_buy_green
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.drawable.coins_exchange_sell_grey
                }
            }
            // 卖
            "sell" -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.drawable.coins_exchange_buy_grey
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = R.drawable.coins_exchange_sell_green
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                CpProfitRateFragment().apply {
                    arguments = Bundle().apply {
                        putInt("contractId", param1)
                    }
                }
    }
}