package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import com.contract.sdk.data.Contract
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.bubble.BubbleSeekBar
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.ToastUtils
import kotlinx.android.synthetic.main.cl_activity_select_leverage.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.ArrayList

/**
 * 选择合约杠杆
 */
class ClSelectLeverageActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_select_leverage
    }

    private var mContract: Contract? = null
    private var currTabType = 0// 0 逐仓  1 全仓
    private var levelCanSwitch = 0//是否可以切换 1是, 0否

    private lateinit var leverCeilingObject: JSONObject
    private lateinit var leverCeilingList: ArrayList<Int>

    private var nowLevel = "0"//当前

    //    private var minLevel = "0"//最小
//    private var maxLevel = "0"//最大
    private var userMaxLevel = "0"//当前持仓最高支持杠杆
    private var multiplierCoin = ""//标的货币单位
    private var coUnit = 1//合约单位 1标的货币, 2张

    private var multiplier = "0.0"
    private var indexPrice = "0.0"
    private var selectLeverage = 0 // 选择杠杆
    private var selectLeverageType = 1 // 选择杠杆
    private var minLeverage = 1
    private var maxLeverage = 100
    private var contractId = 0

    private var isPriceLongClick: Boolean = false
    private var isStartPriceSubClick = false
    private var isStartPricePlusClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        initListener()
    }


    override fun loadData() {
        super.loadData()
        contractId = intent.getIntExtra("contractId", 0)
        multiplier = intent.getStringExtra("multiplier") ?: "0.0"
        indexPrice = intent.getStringExtra("indexPrice") ?: "0.0"
        selectLeverage = intent.getIntExtra("selectLeverage", 10)
        selectLeverageType = intent.getIntExtra("selectLeverageType", 1)
        LogUtil.d("DEBUG", "当前选择杠杆:${selectLeverage}选择杠杆类型:${selectLeverageType}")
    }

    override fun initView() {
        super.initView()
        initAutoTextView()
        switchTabUi()
        loadContractUserConfig()
    }

    private fun initAutoTextView() {
        title_layout.title = getLineText("sl_str_switch_lever")
        tv_tab_gradually.onLineText("sl_str_gradually_position")
        tv_tab_full.onLineText("sl_str_full_position")
        tv_leverage_warn.onLineText("sl_select_lever_warn")
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

                if (leverage >= 50) {
                    tv_leverage_warn.visibility = View.VISIBLE
                } else {
                    tv_leverage_warn.visibility = View.INVISIBLE
                }

                if (leverage >= userMaxLevel.toInt()) {
                    tv_confirm_btn.isEnable(false)
                    tv_confirm_btn.textContent = getString(R.string.cl_exceed_the_current_max_leverage_level_str)
                } else {
                    tv_confirm_btn.isEnable(true)
                    tv_confirm_btn.textContent = getLineText("common_text_btnConfirm")
                }

                if (levelCanSwitch == 1) {
                    tv_confirm_btn.isEnable(true)
                    tv_confirm_btn.textContent = getLineText("common_text_btnConfirm")
                } else {
                    tv_confirm_btn.isEnable(false)
                    tv_confirm_btn.textContent = getString(R.string.cl_can_not_change_the_leverage_level_while_exists_orders_str)
                }
                leverCeilingList.clear()
                var unit = ""
                if (coUnit == 1) {
                    unit = multiplierCoin
                } else {
                    unit = getLineText("contract_text_volumeUnit")
                }
                var max = ""
                if (this@ClSelectLeverageActivity::leverCeilingObject.isInitialized) {
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

    private fun initListener() {
        tv_confirm_btn.isEnable(true)
        tv_confirm_btn.textContent = getLineText("common_text_btnConfirm")
        tv_confirm_btn.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
//                val inputLeverage = et_input.text.toString()
                modifyLevel()
//                if (!TextUtils.isEmpty(inputLeverage)) {
//                    selectLeverage = if (!GlobalLeverageUtils.isOpenGlobalLeverage && currTabType == 1) {
//                        maxLeverage
//                    } else {
//                        inputLeverage.toInt()
//                    }
//                    val map = hashMapOf<String, Int>("leverage" to selectLeverage, "leverageType" to selectLeverageType)
//                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_select_leverage_event, map))
//                    finish()
//                }
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
    }

    /**
     * 切换仓位Tab
     */
    private fun switchTabUi() {
        tv_tab_gradually.setBackgroundResource(R.drawable.sl_tab_leverage_gradually_select)
        tv_tab_gradually.isSelected = true
        tv_tab_full.setBackgroundResource(R.drawable.sl_tab_leverage_full_normal)
        tv_tab_full.isSelected = false
        et_input.setText(selectLeverage.toString())
        seekbar.visibility = View.VISIBLE
        if (selectLeverage >= 50) {
            tv_leverage_warn.visibility = View.VISIBLE
        } else {
            tv_leverage_warn.visibility = View.INVISIBLE
        }
    }

    private fun loadContractUserConfig() {
        leverCeilingList = ArrayList()
        addDisposable(getContractModel().getUserConfig(contractId.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            levelCanSwitch = optInt("levelCanSwitch")
                            nowLevel = optString("nowLevel")

                            selectLeverage = nowLevel.toInt()
                            minLeverage = optString("minLevel").toInt()
                            maxLeverage = optString("maxLevel").toInt()

                            userMaxLevel = optString("userMaxLevel")
                            multiplierCoin = optString("multiplierCoin")
                            coUnit = optInt("coUnit")
                            try {
                                leverCeilingObject = optJSONObject("leverCeiling")
                                val iteratorKeys = leverCeilingObject.keys()
                                while (iteratorKeys.hasNext()) {
                                    val key = iteratorKeys.next().toString()
                                    leverCeilingList.add(key.toInt())
                                }
                            } catch (e: Exception) {
                            } finally {
                                tv_level_user_max.setText(getLineText("cl_max_leverage_level_str") + " " + userMaxLevel + " X")
                                initSeekBarUi()
                            }
                        }
                    }
                }))
    }

    private fun modifyLevel() {
        if (TextUtils.isEmpty(et_input.text.toString())) {
            return
        }
        addDisposable(getContractModel().modifyLevel(contractId.toString(), et_input.text.toString(),
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
//                        ToastUtils.showToast("杠杆设置成功")
                        finish()
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_modify_leverage_event))
                    }
                }))
    }


    companion object {
        fun show(activity: Activity, contractId: Int, multiplier: String, indexPrice: String) {
            val intent = Intent(activity, ClSelectLeverageActivity::class.java)
            intent.putExtra("multiplier", multiplier)
            intent.putExtra("contractId", contractId)
            intent.putExtra("indexPrice", indexPrice)
            activity.startActivity(intent)
        }
    }

}
