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
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.*
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.et_input
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.seekbar
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.tv_add
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.tv_amount_user_max
import kotlinx.android.synthetic.main.cl_fragment_contract_calculate_item.tv_sub
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


/**
 * 盈亏计算
 */
class ClPlCalculatorFragment : NBaseFragment() {

    override fun setContentView(): Int {
        return R.layout.cl_fragment_contract_calculate_item
    }


    protected val directionList = ArrayList<TabInfo>()
    protected var currDirectionInfo: TabInfo? = null
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
        initListener()
    }


    private fun initListener() {
        contractId = arguments?.getInt("contractId")!!
        mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)

        tv_open_price_symbol.setText(mContractJson.optString("quote"))
        tv_extras_symbol.setText(mContractJson.optString("quote"))
        var showUnit = ""
        if (LogicContractSetting.getContractUint(context) == 0) {
            showUnit = getLineText("sl_str_contracts_unit")
        } else {
            showUnit = mContractJson.optString("multiplierCoin")
        }

        tv_position_symbol.setText(showUnit)
        maxLeverage = mContractJson.optInt("maxLever")
        minLeverage = mContractJson.optInt("minLever")
        selectLeverage = 20

        //方向
        directionList.add(TabInfo(getString(R.string.cl_calculator_text19), 0))
        directionList.add(TabInfo(getString(R.string.cl_calculator_text20), 1))
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
                var etPositionStr = et_position.text.toString()
                var etOpenPriceStr = et_open_price.text.toString()
                var etClosePriceStr = et_extras.text.toString()
                selectLeverage = et_input.text.toString().toInt()
                if (TextUtils.isEmpty(etPositionStr) || BigDecimalUtils.compareTo(etPositionStr, "0") != 1) {
                    NToastUtil.showTopToastNet(mActivity,false, getString(R.string.cl_input_the_volume_str))
                    return
                }
                if (TextUtils.isEmpty(etOpenPriceStr) || BigDecimalUtils.compareTo(etOpenPriceStr, "0") != 1) {
                    NToastUtil.showTopToastNet(mActivity,false, getString(R.string.cl_input_the_open_price_str))
                    return
                }
                if (TextUtils.isEmpty(etClosePriceStr) || BigDecimalUtils.compareTo(etClosePriceStr, "0") != 1) {
                    NToastUtil.showTopToastNet(mActivity,false, getString(R.string.cl_input_the_close_price_str))
                    return
                }
                val multiplier = mContractJson.optString("multiplier")
                if (LogicContractSetting.getContractUint(context) == 1) {
                    etPositionStr = etPositionStr
                } else {
                    etPositionStr = BigDecimalUtils.mulStr(etPositionStr, multiplier, LogicContractSetting.getContractMultiplierPrecisionById(context,mContractJson.optInt("id")))
                }

                var marginRate = mContractJson.optString("marginRate")
                val openMargin = BigDecimalUtils.calcMarginValue(mContractJson.optString("contractSide").equals("1"), etPositionStr, etOpenPriceStr, selectLeverage.toString(), marginRate, LogicContractSetting.getContractMarginCoinPrecisionById(mActivity, contractId))
                val incomeValue = BigDecimalUtils.calcIncomeValue(mContractJson.optString("contractSide").equals("1"), currDirectionInfo?.index!!, etPositionStr, etOpenPriceStr, etClosePriceStr, marginRate, LogicContractSetting.getContractMarginCoinPrecisionById(mActivity, contractId))
                val returnRate = BigDecimalUtils.mulStr(BigDecimalUtils.divStr(incomeValue, openMargin, 4), "100", 2) + "%"

                val tabList = ArrayList<TabInfo>()

                tabList.add(TabInfo(getString(R.string.cl_open_position_margin_str), openMargin + mContractJson.optString("marginCoin")))
                tabList.add(TabInfo(getString(R.string.cl_calculator_text7), incomeValue + mContractJson.optString("marginCoin")))
                tabList.add(TabInfo(getString(R.string.cl_calculator_text8), returnRate))
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
        multiplierCoin = mContractJson.optString("multiplierCoin")
        maxLeverage = mContractJson.optInt("maxLever")
        minLeverage = mContractJson.optInt("minLever")
        multiplier = mContractJson.optString("multiplier")
        MultiplierCoinPrecision = LogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        coUnit = LogicContractSetting.getContractUint(mActivity)
        leverCeilingList = ArrayList()
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : NDisposableObserver() {
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
    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_contract_calc_switch_contract_event -> {
                et_position.setText("")
                et_open_price.setText("")
                et_extras.setText("")
                contractId = event.msg_content as Int
                mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
                tv_open_price_symbol.setText(mContractJson.optString("quote"))
                tv_extras_symbol.setText(mContractJson.optString("quote"))
                var showUnit = ""
                if (LogicContractSetting.getContractUint(context) == 0) {
                    showUnit = getLineText("sl_str_contracts_unit")
                } else {
                    showUnit = mContractJson.optString("multiplierCoin")
                }
                tv_position_symbol.setText(showUnit)
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
                if (this@ClPlCalculatorFragment::leverCeilingObject.isInitialized) {
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