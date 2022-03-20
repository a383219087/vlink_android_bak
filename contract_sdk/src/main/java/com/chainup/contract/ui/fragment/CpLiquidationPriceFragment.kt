package com.yjkj.chainup.new_contract.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.fragment.CpCapitalRateFragment
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.ChainUpLogUtil
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
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


/**
 * 强平价格
 */
class CpLiquidationPriceFragment : CpNBaseFragment() {

    override fun setContentView(): Int {
        return R.layout.cp_fragment_contract_calculate_item
    }


    protected val directionList = ArrayList<CpTabInfo>()
    protected var currDirectionInfo: CpTabInfo? = null
    protected var directionDialog: TDialog? = null
    private var contractId = 0
    private var leverage = "0"
    private var indexPrice = "0"
    private var keepMarginRate = "0"
    private lateinit var mContractJson: JSONObject
    private lateinit var leverList: JSONArray
    private lateinit var ladderList: JSONArray

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
        initListener()
    }

    private fun initListener() {
        contractId = arguments?.getInt("contractId")!!
//        indexPrice = arguments?.getString("indexPrice")!!
        mContractJson = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
        maxLeverage = mContractJson.optInt("maxLever")
        minLeverage = mContractJson.optInt("minLever")
        selectLeverage = 20

        var showUnit = ""
        if (CpClLogicContractSetting.getContractUint(context) == 0) {
            showUnit = getString(R.string.cp_overview_text9)
        } else {
            showUnit = mContractJson.optString("multiplierCoin")
        }

        tv_extras_title.setText(getString(R.string.cp_calculator_text38))
//        et_extras.setHint(getString(R.string.cp_calculator_text38))
        tv_position_title.setText(getString(R.string.cp_calculator_text39))
//        et_position.setHint(getString(R.string.cp_calculator_text39))

        tv_open_price_symbol.setText(mContractJson.optString("quote"))
        tv_extras_symbol.setText(showUnit)//仓位数量币种
        tv_position_symbol.setText(mContractJson.optString("marginCoin"))//保证金数量币种
        val maxLeverage = mContractJson.optString("maxLever")
        val minLeverage = mContractJson.optString("minLever")
        val selectLeverage = "20"
        seekbar.configBuilder
                .min(minLeverage.toFloat())
                .max(maxLeverage.toFloat())
                .progress(selectLeverage.toFloat())
                .build()
        seekbar.onProgressChangedListener = object : CpBubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
                leverage = progress.toString()
                if (progress == 0) leverage = "1"
                tv_leverage.setText(leverage+"X")
            }

            override fun getProgressOnActionUp(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
                for (i in 0 until leverList.length()) {
                    try {
                        ChainUpLogUtil.e(TAG, progress.toString())
                        ChainUpLogUtil.e(TAG, leverList.getJSONObject(i).getInt("maxLever").toString())
                        if (progress <= leverList.getJSONObject(i).getInt("maxLever")) {
                            tv_amount_user_max.setText(getString(R.string.cp_extra_text120) + leverList.getJSONObject(i).getString("maxHoldAmount") + " BTC")
                            break
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun getProgressOnFinally(bubbleSeekBar: CpBubbleSeekBar?, progress: Int, progressFloat: Float) {
            }
        }


        //方向
        directionList.add(CpTabInfo(getString(R.string.cp_overview_text13), 0))
        directionList.add(CpTabInfo(getString(R.string.cp_overview_text14), 1))
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
                var etClosePriceStr = et_extras.text.toString()
                var progress = CpBigDecimalUtils.mul(indexPrice, etClosePriceStr, 0).toInt()
                for (i in 0 until ladderList.length()) {
                    try {
                        if (progress <= ladderList.getJSONObject(i).getInt("maxPositionValue")) {
                            keepMarginRate = ladderList.getJSONObject(i).getString("minMarginRate")
                            break
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }


                var etPositionStr = et_position.text.toString()
                var etOpenPriceStr = et_open_price.text.toString()

                if (TextUtils.isEmpty(etPositionStr)|| CpBigDecimalUtils.compareTo(etPositionStr,"0")!=1){
                    CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text36))
                    return
                }
                if (TextUtils.isEmpty(etOpenPriceStr)|| CpBigDecimalUtils.compareTo(etOpenPriceStr,"0")!=1){
                    CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text35))
                    return
                }
                if (TextUtils.isEmpty(etClosePriceStr)|| CpBigDecimalUtils.compareTo(etClosePriceStr,"0")!=1){
                    CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text37))
                    return
                }
                var marginRate = mContractJson.optString("marginRate")

                val multiplier=mContractJson.optString("multiplier")

                if (CpClLogicContractSetting.getContractUint(context) == 1) {
                    etClosePriceStr = etClosePriceStr
                } else {
                    etClosePriceStr = CpBigDecimalUtils.mulStr(etClosePriceStr,multiplier,
                        CpClLogicContractSetting.getContractMultiplierPrecisionById(context,mContractJson.optInt("id")))
                }
                val forceClosePrice = CpBigDecimalUtils.calcForceClosePriceValue(
                        mContractJson.optString("contractSide").equals("1"),
                        currDirectionInfo?.index!!,
                        etPositionStr,
                        etClosePriceStr,
                        etOpenPriceStr,
                        keepMarginRate,
                        marginRate,
                        CpClLogicContractSetting.getContractSymbolPricePrecisionById(mActivity,contractId))
                if (currDirectionInfo?.index==0){
                    //多仓：强平价格>开仓价格 时 不显示强平价格，显示：保证金不足以开仓！
                    if (CpBigDecimalUtils.compareTo(forceClosePrice,etOpenPriceStr)==1){
                        CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text38))
                        return
                    }
                }else{
                    //空仓：强平价格<开仓价格 时 不显示强平价格，显示：保证金不足以开仓！
                    if (CpBigDecimalUtils.compareTo(etOpenPriceStr,forceClosePrice)==1){
                        CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text38))
                        return
                    }
                }
                if (CpBigDecimalUtils.compareTo("0",forceClosePrice)==1){
                    CpNToastUtil.showTopToastNet(this@CpLiquidationPriceFragment.mActivity,false, getString(R.string.cp_extra_text39))
                    return
                }

                val tabList = ArrayList<CpTabInfo>()
                tabList.add(CpTabInfo(getString(R.string.cp_calculator_text4), forceClosePrice + mContractJson.optString("quote")))
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
        leverCeilingList=ArrayList()
        multiplierCoin = mContractJson.optString("multiplierCoin")
        multiplier = mContractJson.optString("multiplier")
        MultiplierCoinPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        coUnit = CpClLogicContractSetting.getContractUint(mActivity)
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : CpNDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            leverList = optJSONObject("leverList").optJSONArray("leverList")
                            ladderList = optJSONObject("ladderList").optJSONArray("ladderList")
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
                var showUnit = ""
                if (CpClLogicContractSetting.getContractUint(context) == 0) {
                    showUnit = getString(R.string.cp_overview_text9)
                } else {
                    showUnit = mContractJson.optString("multiplierCoin")
                }
                tv_open_price_symbol.setText(mContractJson.optString("quote"))
                tv_extras_symbol.setText(showUnit)//仓位数量币种
                tv_position_symbol.setText(mContractJson.optString("marginCoin"))//保证金数量币种
                 maxLeverage = mContractJson.optInt("maxLever")
                 minLeverage = mContractJson.optInt("minLever")
                seekbar.configBuilder
                        .min(1.toFloat())
                        .max(maxLeverage.toFloat())
                        .progress(leverage.toFloat())
                        .build()
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
                if (this@CpLiquidationPriceFragment::leverCeilingObject.isInitialized) {
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
                CpLiquidationPriceFragment().apply {
                    arguments = Bundle().apply {
                        putInt("contractId", param1)
                    }
                }
    }
}