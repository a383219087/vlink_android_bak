package com.yjkj.chainup.new_contract.fragment

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent

import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.contract.widget.bubble.BubbleSeekBar
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception


/**
 * 强平价格
 */
class ClLiquidationPriceFragment : NBaseFragment() {

    override fun setContentView(): Int {
        return R.layout.cl_fragment_contract_calculate_item
    }


    protected val directionList = ArrayList<TabInfo>()
    protected var currDirectionInfo: TabInfo? = null
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
        indexPrice = arguments?.getString("indexPrice")!!
        mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
        maxLeverage = mContractJson.optInt("maxLever")
        minLeverage = mContractJson.optInt("minLever")
        selectLeverage = 20

        var showUnit = ""
        if (LogicContractSetting.getContractUint(context) == 0) {
            showUnit = getLineText("sl_str_contracts_unit")
        } else {
            showUnit = mContractJson.optString("multiplierCoin")
        }


        tv_extras_title.setText(getString(R.string.cl_calculator_text21))
        et_extras.setHint(getString(R.string.cl_calculator_text21))
        tv_position_title.setText(getString(R.string.cl_calculator_text22))
        et_position.setHint(getString(R.string.cl_calculator_text22))

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
        seekbar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                leverage = progress.toString()
                if (progress == 0) leverage = "1"
                tv_leverage.setText(leverage+"X")
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                for (i in 0 until leverList.length()) {
                    try {
                        LogUtil.e(TAG, progress.toString())
                        LogUtil.e(TAG, leverList.getJSONObject(i).getInt("maxLever").toString())
                        if (progress <= leverList.getJSONObject(i).getInt("maxLever")) {
                            tv_amount_user_max.setText(getLineText("cl_holding_amount_limit_str") + leverList.getJSONObject(i).getString("maxHoldAmount") + " BTC")
                            break
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }
        }


        //方向
        directionList.add(TabInfo(getLineText("sl_str_buy_open"), 0))
        directionList.add(TabInfo(getLineText("sl_str_sell_open"), 1))
        currDirectionInfo = directionList[0]
        tv_direction_value.text = currDirectionInfo?.name

        //方向
        rl_direction_layout.setOnClickListener {
            directionDialog = NewDialogUtils.showNewBottomListDialog(mActivity!!, directionList, currDirectionInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currDirectionInfo = directionList[index]
                    directionDialog?.dismiss()
                    tv_direction_value.text = currDirectionInfo?.name
                }
            })
        }
        btn_calculate.isEnable(true)
        btn_calculate.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var etClosePriceStr = et_extras.text.toString()
                var progress = BigDecimalUtils.mul(indexPrice, etClosePriceStr, 0).toInt()
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

                if (TextUtils.isEmpty(etPositionStr)||BigDecimalUtils.compareTo(etPositionStr,"0")!=1){
                    NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_input_the_margin_balance_str))
                    return
                }
                if (TextUtils.isEmpty(etOpenPriceStr)||BigDecimalUtils.compareTo(etOpenPriceStr,"0")!=1){
                    NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_input_the_open_price_str))
                    return
                }
                if (TextUtils.isEmpty(etClosePriceStr)||BigDecimalUtils.compareTo(etClosePriceStr,"0")!=1){
                    NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_input_the_volume_str))
                    return
                }
                var marginRate = mContractJson.optString("marginRate")

                val multiplier=mContractJson.optString("multiplier")

                if (LogicContractSetting.getContractUint(context) == 1) {
                    etClosePriceStr = etClosePriceStr
                } else {
                    etClosePriceStr = BigDecimalUtils.mulStr(etClosePriceStr,multiplier,LogicContractSetting.getContractMultiplierPrecisionById(context,mContractJson.optInt("id")))
                }
                val forceClosePrice = BigDecimalUtils.calcForceClosePriceValue(
                        mContractJson.optString("contractSide").equals("1"),
                        currDirectionInfo?.index!!,
                        etPositionStr,
                        etClosePriceStr,
                        etOpenPriceStr,
                        keepMarginRate,
                        marginRate,
                        LogicContractSetting.getContractSymbolPricePrecisionById(mActivity,contractId))
                if (currDirectionInfo?.index==0){
                    //多仓：强平价格>开仓价格 时 不显示强平价格，显示：保证金不足以开仓！
                    if (BigDecimalUtils.compareTo(forceClosePrice,etOpenPriceStr)==1){
                        NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_calculator_text23))
                        return
                    }
                }else{
                    //空仓：强平价格<开仓价格 时 不显示强平价格，显示：保证金不足以开仓！
                    if (BigDecimalUtils.compareTo(etOpenPriceStr,forceClosePrice)==1){
                        NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_calculator_text23))
                        return
                    }
                }
                if (BigDecimalUtils.compareTo("0",forceClosePrice)==1){
                    NToastUtil.showTopToastNet(this@ClLiquidationPriceFragment.mActivity,false, getString(R.string.cl_calculator_text24))
                    return
                }

                val tabList = ArrayList<TabInfo>()
                tabList.add(TabInfo(getLineText("sl_str_liquidation_price"), forceClosePrice + mContractJson.optString("quote")))
                SlDialogHelper.showCalculatorResultDialog(mActivity!!, tabList)
            }
        }
        tv_add?.setOnTouchListener { _, event ->
            isPriceLongClick = true
            isStartPricePlusClick = true
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var inputLeverage = BigDecimalUtils.add(et_input.text.toString(), "1").toPlainString()
                    et_input?.setText(BigDecimalUtils.subZeroAndDot(inputLeverage))

                    doAsync {
                        while (isPriceLongClick) {
                            Thread.sleep(100L)
                            if (!isStartPricePlusClick) continue
                            inputLeverage = try {
                                BigDecimalUtils.add(et_input.text.toString(), "1").toPlainString()
                            } catch (e: NumberFormatException) {
                                ""
                            }

                            uiThread {
                                et_input?.setText(BigDecimalUtils.subZeroAndDot(inputLeverage))
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
                    var inputLeverage = BigDecimalUtils.sub(et_input.text.toString(), "1").toPlainString()
                    et_input?.setText(BigDecimalUtils.subZeroAndDot(inputLeverage))

                    doAsync {
                        while (isPriceLongClick) {
                            Thread.sleep(100L)
                            if (!isStartPriceSubClick) continue
                            inputLeverage = try {
                                BigDecimalUtils.sub(et_input.text.toString(), "1").toPlainString()
                            } catch (e: NumberFormatException) {
                                ""
                            }
                            uiThread {
                                et_input?.setText(BigDecimalUtils.subZeroAndDot(inputLeverage))
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
        MultiplierCoinPrecision = LogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        coUnit = LogicContractSetting.getContractUint(mActivity)
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : NDisposableObserver() {
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
    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_contract_calc_switch_contract_event -> {
                et_position.setText("")
                et_open_price.setText("")
                et_extras.setText("")
                contractId = event.msg_content as Int
                mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
                var showUnit = ""
                if (LogicContractSetting.getContractUint(context) == 0) {
                    showUnit = getLineText("sl_str_contracts_unit")
                } else {
                    showUnit = mContractJson.optString("multiplierCoin")
                }
                tv_open_price_symbol.setText(mContractJson.optString("quote"))
                tv_extras_symbol.setText(showUnit)//仓位数量币种
                tv_position_symbol.setText(mContractJson.optString("marginCoin"))//保证金数量币种
                val maxLeverage = mContractJson.optString("maxLever")
                val minLeverage = mContractJson.optString("minLever")
                seekbar.configBuilder
                        .min(minLeverage.toFloat())
                        .max(maxLeverage.toFloat())
                        .progress(leverage.toFloat())
                        .build()
                getLadderInfo()
            }
        }
    }
    private fun initSeekBarUi() {
        seekbar.configBuilder
                .min(minLeverage.toFloat())
                .max(maxLeverage.toFloat())
                .progress(selectLeverage.toFloat())
                .build()
        seekbar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                et_input.setText(progress.toString())
                et_input.setSelection(progress.toString().length)
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
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
                    unit = getLineText("contract_text_volumeUnit")
                }
                var max = ""
                if (this@ClLiquidationPriceFragment::leverCeilingObject.isInitialized) {
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
                    max = BigDecimalUtils.showSNormal(leverCeilingObject.optString(leverCeilingList[indexBuff].toString()), LogicContractSetting.getContractMarginCoinPrecisionById(mActivity, contractId))
                    if (coUnit == 1) {
                    } else {
                        max = BigDecimalUtils.divStr(max, multiplier, 0)
                    }
                    tv_amount_user_max.setText(getLineText("cl_holding_amount_limit_str") + max + " " + unit)
                } else {
                    max = "0"
                    tv_amount_user_max.setText(getLineText("cl_holding_amount_limit_str") + max + " " + unit)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        et_input.setText(selectLeverage.toString())
    }
}