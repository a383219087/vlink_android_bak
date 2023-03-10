package com.chainup.contract.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.bean.CpTpslOrderBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.chainup.contract.ws.CpWsContractAgentManager
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import kotlinx.android.synthetic.main.cp_activity_stop_rate_loss.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 止盈止损
 */
class CpContractStopRateLossActivity : CpNBaseActivity(), CpWsContractAgentManager.WsResultCallback, TextWatcher {
    override fun setContentView(): Int {
        return R.layout.cp_activity_stop_rate_loss
    }

    private var mContractPositionBean: CpContractPositionBean? = null
    private var isStopProfitMarket = true
    private var isStopLossMarket = true
    private var multiplier = "0"
    private var multiplierPrecision = 0
    private var marginCoinPrecision = 0
    private var mPricePrecision = 0
    private var currentSymbol = ""
    private var marginRate = ""
    private var mMarginCoin = ""
    private var mCanCloseVolumeStr = "0"
    private lateinit var mTakeProfitList: JSONArray
    private lateinit var mStopLossList: JSONArray
    private lateinit var orderList: ArrayList<CpTpslOrderBean>
    private lateinit var mContractJsonStr: JSONObject
    var orderType=1
    private var isLimit = false
    private var base = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        initListener()

    }


    @SuppressLint("SetTextI18n")
    override fun loadData() {
        super.loadData()
        CpWsContractAgentManager.instance.addWsCallback(this)
        mContractPositionBean = intent.getSerializableExtra("ContractPositionBean") as CpContractPositionBean?

        mContractJsonStr = CpClLogicContractSetting.getContractJsonStrById(mActivity, mContractPositionBean?.contractId!!)
        multiplier = mContractJsonStr?.optString("multiplier").toString()

        multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, mContractPositionBean?.contractId!!)

        marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(mActivity, mContractPositionBean?.contractId!!)

        multiplierPrecision =  multiplierPrecision

        mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(mContext, mContractPositionBean?.contractId!!)

        currentSymbol = CpClLogicContractSetting.getContractWsSymbolById(mContext, mContractPositionBean?.contractId!!)

        marginRate = CpClLogicContractSetting.getContractMarginRateById(mContext, mContractPositionBean?.contractId!!)

        mMarginCoin = CpClLogicContractSetting.getContractMarginCoinById(mContext, mContractPositionBean?.contractId!!)

        et_stop_profit_position.numberFilter(multiplierPrecision)
        et_stop_loss_position.numberFilter(multiplierPrecision)
        et_stop_profit_trigger_price.numberFilter(mPricePrecision)
        et_stop_loss_trigger_price.numberFilter(mPricePrecision)
        et_stop_loss_price.numberFilter(mPricePrecision)
        et_stop_profit_price.numberFilter(mPricePrecision)

        tv_stop_profit_trigger_coin_name.text = mContractJsonStr?.optString("quote")
        tv_stop_loss_trigger_coin_name.text = mContractJsonStr?.optString("quote")
        tv_stop_profit_coin_name.text = mContractJsonStr?.optString("quote")
        tv_stop_loss_coin_name.text = mContractJsonStr?.optString("quote")

        base = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) getString(R.string.cp_overview_text9) else mContractJsonStr?.optString("multiplierCoin")
        tv_position_key.setText(getString(R.string.cp_extra_text90) + "(" + base + ")")
        tv_stop_profit_volume_unit.setText(base)
        tv_stop_loss_volume_unit.setText(base)
        if (mContractPositionBean?.orderSide.equals("BUY")) {
            tv_type.text = getString(R.string.cp_order_text6)
        }else{
            tv_type.text = getString(R.string.cp_order_text15)
        }

        var mPositionVolumeStr = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) {
            mContractPositionBean?.positionVolume
        } else {
            CpBigDecimalUtils.mulStr(mContractPositionBean?.positionVolume, multiplier, multiplierPrecision)
        }

        mCanCloseVolumeStr = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) {
            mContractPositionBean?.canCloseVolume.toString()
        } else {
            CpBigDecimalUtils.mulStr(mContractPositionBean?.canCloseVolume, multiplier, multiplierPrecision)
        }

        tv_can_close_volume.text = "$mCanCloseVolumeStr $base"
        et_stop_profit_position.setText(mCanCloseVolumeStr)
        tv_position_num.text = "$mPositionVolumeStr/$mCanCloseVolumeStr"
        tv_mark_price.text = CpBigDecimalUtils.showSNormal(mContractPositionBean?.indexPrice.toString(), mPricePrecision)

        var reducePriceStr = CpBigDecimalUtils.showSNormal(mContractPositionBean?.reducePrice, mPricePrecision)
        if (CpBigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
            reducePriceStr = "--"
        }
        tv_force_close_price.text = reducePriceStr

        var openAvgPriceStr = CpBigDecimalUtils.showSNormal(mContractPositionBean?.openAvgPrice, mPricePrecision)
        tv_open_price_value.text = openAvgPriceStr

        tv_contract_name.text = CpClLogicContractSetting.getContractShowNameById(this, mContractPositionBean?.contractId!!)


        when (mContractPositionBean?.positionType) {
            1 -> {
                tv_contract_name_lable.text = getString(R.string.cp_contract_setting_text1)+" " + mContractPositionBean?.leverageLevel + "X"
            }
            2 -> {
                tv_contract_name_lable.text = getString(R.string.cp_contract_setting_text2)+" " + mContractPositionBean?.leverageLevel + "X"
            }
            else -> {
            }
        }

//        tv_contract_name_lable.setText(orderSideStr+" " + mContractPositionBean?.leverageLevel + "X")

//        tv_contract_name_lable.setBackgroundResource(if (mContractPositionBean?.orderSide.equals("BUY")) R.drawable.cp_border_green_fill else R.drawable.cp_border_red_fill)
        tv_type.setTextColor(CpColorUtil.getMainColorType(mContractPositionBean?.orderSide.equals("BUY")))
    }


    override fun initView() {
        super.initView()
        initAutoTextView()



        et_stop_profit_trigger_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_trigger_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }


        et_stop_profit_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_profit_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }


        et_stop_loss_trigger_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_loss_trigger_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }


        et_stop_loss_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_loss_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }


        orderList = ArrayList();
        if (isStopProfitMarket) {

            tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_focused)
            tv_stop_profit_price_hint?.setTextColor(CpColorUtil.getColor(R.color.main_blue))
        } else {

            tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
            tv_stop_profit_price_hint?.setTextColor(CpColorUtil.getColor(R.color.normal_text_color))
        }

        if (isStopLossMarket) {

            tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_focused)
            tv_stop_loss_price_hint?.setTextColor(CpColorUtil.getColor(R.color.main_blue))
//            et_stop_loss_price.requestFocus()
        } else {
//            tv_market_loss_price_hint.visibility = View.GONE
//            et_stop_loss_price.visibility = View.VISIBLE
//            tv_stop_loss_coin_name.visibility = View.VISIBLE
            tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
            tv_stop_loss_price_hint?.setTextColor(CpColorUtil.getColor(R.color.normal_text_color))
//            et_stop_loss_price.clearFocus()
        }

        et_stop_profit_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_profit_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
            if (hasFocus) {
                isStopProfitMarket = false
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
            }
        }

        et_stop_loss_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_loss_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
            if (hasFocus) {
                isStopProfitMarket = false
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
            }
        }

        tv_stop_profit_price_hint.setSafeListener {
            isStopProfitMarket = !isStopProfitMarket
            if (isStopProfitMarket) {
                tv_market_price_hint.visibility = View.VISIBLE
//                et_stop_profit_price.visibility = View.GONE
//                tv_stop_profit_coin_name.visibility = View.GONE
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_focused)
                tv_stop_profit_price_hint?.setTextColor(CpColorUtil.getColor(R.color.main_blue))
//                et_stop_profit_price.clearFocus()
            } else {
                tv_market_price_hint.visibility = View.GONE
//                et_stop_profit_price.visibility = View.VISIBLE
//                tv_stop_profit_coin_name.visibility = View.VISIBLE
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
                tv_stop_profit_price_hint?.setTextColor(CpColorUtil.getColor(R.color.normal_text_color))
//                et_stop_profit_price.requestFocus()
            }
        }

        tv_stop_loss_price_hint.setSafeListener {
            isStopLossMarket = !isStopLossMarket
            if (isStopLossMarket) {
//                tv_market_loss_price_hint.visibility = View.VISIBLE
//                et_stop_loss_price.visibility = View.GONE
//                tv_stop_loss_coin_name.visibility = View.GONE
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_focused)
                tv_stop_loss_price_hint?.setTextColor(CpColorUtil.getColor(R.color.main_blue))
//                et_stop_loss_price.clearFocus()
            } else {
//                tv_market_loss_price_hint.visibility = View.GONE
//                et_stop_loss_price.visibility = View.VISIBLE
//                tv_stop_loss_coin_name.visibility = View.VISIBLE
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.cp_bg_trade_et_unfocused)
                tv_stop_loss_price_hint?.setTextColor(CpColorUtil.getColor(R.color.normal_text_color))
//                et_stop_loss_price.requestFocus()
            }
        }

        btn_cancel_stop_profit.setSafeListener {
            var buff = StringBuffer()
            for (i in 0 until mTakeProfitList.length()) {
                var obj: JSONObject = mTakeProfitList.get(i) as JSONObject
                buff.append(obj.optString("id"))
                buff.append(",")
            }
            val buff1 = buff.substring(0, buff.length - 1)
            cancelProfitStopLossOrder(buff1)
        }

        btn_cancel_stop_loss.setSafeListener {
            var buff = StringBuffer()
            for (i in 0..(mStopLossList.length() - 1)) {
                var obj: JSONObject = mStopLossList.get(i) as JSONObject
                buff.append(obj.optString("id"))
                buff.append(",")
            }
            val buff1 = buff.substring(0, buff.length - 1)
            cancelProfitStopLossOrder(buff1)
        }

        tv_stop_loss_tip.setSafeListener {
            CpSlDialogHelper.showSubmitProfitLossDetailsDialog(this@CpContractStopRateLossActivity)
        }

        ll_order_type.setOnClickListener {
            img_order_type.animate().setDuration(300).rotation(180f).start()
            CpDialogUtil.createOrderTypePop(this, orderType, it, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
                override fun clickItem(position: Int, text: String) {
                    tv_order_type.setText(text)
                    orderType=position
                    when (position) {
                        1 -> {
                            isLimit = false
                        }
                        2 -> {
                            isLimit = true
                        }
                    }
                    ll_stop_profit_price.visibility = if (isLimit) View.VISIBLE else View.GONE
                    ll_stop_loss_price.visibility = if (isLimit) View.VISIBLE else View.GONE
                    et_stop_profit_trigger_price.setText("")
                    et_stop_profit_price.setText("")
                    et_stop_loss_trigger_price.setText("")
                    et_stop_loss_price.setText("")
                }
            }, object : CpNewDialogUtils.DialogOnDismissClickListener {
                override fun clickItem() {
                    img_order_type.animate().setDuration(300).rotation(0f).start()
                }
            })
        }

        et_stop_profit_trigger_price.addTextChangedListener(this)
        et_stop_profit_price.addTextChangedListener(this)
        et_stop_loss_trigger_price.addTextChangedListener(this)
        et_stop_loss_price.addTextChangedListener(this)
        et_stop_profit_position.addTextChangedListener(this)

        ll_stop_profit_price.visibility = if (isLimit) View.VISIBLE else View.GONE
        ll_stop_loss_price.visibility = if (isLimit) View.VISIBLE else View.GONE
    }

    private fun initAutoTextView() {
        title_layout.title = getLineText("cl_stoporder_titletext")
    }

    private fun initListener() {
        ll_stop_profit_price.visibility = if (isLimit) View.VISIBLE else View.GONE
        ll_stop_loss_price.visibility = if (isLimit) View.VISIBLE else View.GONE
        et_stop_profit_trigger_price.setText("")
        et_stop_profit_price.setText("")
        et_stop_loss_trigger_price.setText("")
        et_stop_loss_price.setText("")

        cb_tab_stop_profit.isChecked = true
        cb_tab_stop_loss.isChecked = true
        img_back.setSafeListener {
            finish()
        }
        ll_tab_layout_stop_profit.setSafeListener {
            if (cb_tab_stop_loss.isChecked){
            cb_tab_stop_profit.isChecked = !cb_tab_stop_profit.isChecked
            ll_stop_profit.visibility = if (cb_tab_stop_profit.isChecked) View.VISIBLE else View.GONE
            }
        }
        ll_tab_layout_stop_loss.setSafeListener {
            if(cb_tab_stop_profit.isChecked){
                cb_tab_stop_loss.isChecked = !cb_tab_stop_loss.isChecked
                ll_stop_loss.visibility = if (cb_tab_stop_loss.isChecked) View.VISIBLE else View.GONE
            }
        }

        tv_confirm_btn.isEnable(true)
        tv_confirm_btn.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (!cb_tab_stop_profit.isChecked && !cb_tab_stop_loss.isChecked) {
                    return
                }
                var orderType = 1;
                orderList.clear()
                var profit_title = ""
                var loss_title = ""
                var profit_info = ""
                var loss_info = ""
                /**
                请输入止盈触发价！
                请输入止损触发价！
                请输入止盈委托价！
                请输入止损委托价！
                请输入止盈止损数量！
                 */
                if (cb_tab_stop_profit.isChecked) {
                    orderType = if (isLimit) 1 else 2
                    //止盈触发价
                    val et_stop_profit_trigger_price_str = et_stop_profit_trigger_price.text.toString()
                    //止盈委托价
                    val et_stop_profit_price_str = et_stop_profit_price.text.toString()
                    //交易量
                    var et_stop_profit_position_str = et_stop_profit_position.text.toString()
                    //开仓均价
                    val  openAvgPrice =tv_open_price_value.text.toString().toDouble()

                    if (TextUtils.isEmpty(et_stop_profit_trigger_price_str)) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text104))
                        return
                    }
                    if (TextUtils.isEmpty(et_stop_profit_price_str) && isLimit) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text105))
                        return
                    }


                    if (mContractPositionBean?.orderSide.equals("BUY")) {
                        //止盈触发价必须大于开仓均价
                        if (et_stop_profit_trigger_price_str.toDouble()<openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1041))
                            return
                        }
                        //止盈委托价必须大于开仓均价
                        if (isLimit && et_stop_profit_price_str.toDouble()<openAvgPrice ) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1042))
                            return
                        }
                    }else{
                        //止盈触发价必须小于开仓均价
                        if (et_stop_profit_trigger_price_str.toDouble()>openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1046))
                            return
                        }
                        //止盈委托价必须小于开仓均价
                        if (isLimit && et_stop_profit_price_str.toDouble()>openAvgPrice ) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1047))
                            return
                        }
                    }

                    if (TextUtils.isEmpty(et_stop_profit_position_str)) {
                        CpNToastUtil.showTopToastNet(
                            this@CpContractStopRateLossActivity,
                            false,
                            getString(R.string.cp_extra_text106)
                        )
                        return
                    }
                    //交易量必须小于可平仓位
                    if (et_stop_profit_position_str.toDouble()>mCanCloseVolumeStr.toDouble()) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1043))
                        return
                    }

                    if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 1) {
                        et_stop_profit_position_str = CpBigDecimalUtils.getOrderLossNum(et_stop_profit_position_str, multiplier)
                    }
                    orderList.add(CpTpslOrderBean("2", if (!isLimit) "0" else et_stop_profit_price_str, et_stop_profit_position_str, et_stop_profit_trigger_price_str, orderType.toString(), CpClLogicContractSetting.getStrategyEffectTimeStr(mActivity)))
                    if (isLimit) {
                        profit_title = getString(R.string.cp_extra_text99)
                        profit_info = String.format(getString(R.string.cp_extra_text92), et_stop_profit_trigger_price_str, et_stop_profit_position.text.toString(), base, et_stop_profit_price_str)
                    } else {
                        profit_title = getString(R.string.cp_extra_text97)
                        profit_info = String.format(getString(R.string.cp_extra_text91), et_stop_profit_trigger_price_str, et_stop_profit_position.text.toString(), base)
                    }
                }
                if (cb_tab_stop_loss.isChecked) {
                    orderType = if (isLimit) 1 else 2
                    //止损触发价
                    val et_stop_loss_trigger_price_str = et_stop_loss_trigger_price.text.toString()
                    //止损委托价
                    val et_stop_loss_price_str = et_stop_loss_price.text.toString()
                    var et_stop_loss_position_str = et_stop_profit_position.text.toString()
                    //开仓均价
                    val  openAvgPrice =tv_open_price_value.text.toString().toDouble()
                    if (TextUtils.isEmpty(et_stop_loss_trigger_price_str)) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text101))
                        return
                    }

                    if (TextUtils.isEmpty(et_stop_loss_price_str) && isLimit) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text102))
                        return
                    }

                    if (TextUtils.isEmpty(et_stop_loss_position_str)) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text103))
                        return
                    }

                    if (mContractPositionBean?.orderSide.equals("BUY")) {
                        //止损触发价必须小于开仓均价
                        if (et_stop_loss_trigger_price_str.toDouble()>openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1044))
                            return
                        }
                        //止损触发价必须小于开仓均价
                        if (isLimit&& et_stop_loss_price_str.toDouble()>openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1045))
                            return
                        }
                    }else{
                        //止损触发价必须大于开仓均价
                        if (et_stop_loss_trigger_price_str.toDouble()<openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1048))
                            return
                        }
                        //止损触发价必须大于开仓均价
                        if (isLimit&& et_stop_loss_price_str.toDouble()<openAvgPrice) {
                            CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1049))
                            return
                        }
                    }

                    //交易量必须小于可平仓位
                    if (et_stop_loss_position_str.toDouble()>mCanCloseVolumeStr.toDouble()) {
                        CpNToastUtil.showTopToastNet(this@CpContractStopRateLossActivity, false,  getString(R.string.cp_extra_text1043))
                        return
                    }

                    if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 1) {
                        et_stop_loss_position_str = CpBigDecimalUtils.getOrderLossNum(et_stop_loss_position_str, multiplier)
                    }
                    orderList.add(CpTpslOrderBean("1", if (!isLimit) "0" else et_stop_loss_price_str, et_stop_loss_position_str, et_stop_loss_trigger_price_str, orderType.toString(), CpClLogicContractSetting.getStrategyEffectTimeStr(mActivity)))
                    if (isLimit) {
                        loss_title = getString(R.string.cp_extra_text100)
                        loss_info = String.format(getString(R.string.cp_extra_text92), et_stop_loss_trigger_price_str, et_stop_profit_position.text.toString(), base, et_stop_loss_price_str)
                    } else {
                        loss_title = getString(R.string.cp_extra_text98)
                        loss_info = String.format(getString(R.string.cp_extra_text93), et_stop_loss_trigger_price_str, et_stop_profit_position.text.toString(), base)
                    }
                }
                val tradeConfirm = CpPreferenceManager.getInstance(CpMyApp.instance())
                        .getSharedBoolean(CpPreferenceManager.PREF_LOSS_CONFIRM, true)
                if (tradeConfirm){
                    CpSlDialogHelper.showSubmitProfitLossDetailsDialog(
                            this@CpContractStopRateLossActivity,
                            object : CpNewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    createTpslOrder()
                                }
                            },
                            if (cb_tab_stop_profit.isChecked) profit_title else "",
                            if (cb_tab_stop_loss.isChecked) loss_title else "",
                            profit_info,
                            loss_info)
                }else{
                    createTpslOrder()
                }
            }
        }
        cp_loss_trade.setOnSelectionChangedListener(object : CpUISegmentedView.OnSelectionChangedListener {
            override fun onSelectionChanged(position: Int, value: String?) {

            }

            override fun onSelectionChanged(position: Int) {
                val scale = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) 0 else multiplierPrecision
                when (position) {
                    0 -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStr(mCanCloseVolumeStr, "0.1", scale))
                    }
                    1 -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStr(mCanCloseVolumeStr, "0.2", scale))
                    }
                    2 -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStr(mCanCloseVolumeStr, "0.5", scale))
                    }
                    3 -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStr(mCanCloseVolumeStr, "1", scale))
                    }
                }

            }
        })

        for (buff in 0 until rg_trade?.childCount!!){
            rg_trade.getChildAt(buff).setOnClickListener {
                rg_trade.check(it.id)
                et_stop_profit_position.clearFocus()
                val scale = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) 0 else multiplierPrecision
                when(it.id){
                    R.id.rb_1st -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.1", scale))
                    }
                    R.id.rb_2nd -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.2", scale))
                    }
                    R.id.rb_3rd -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.5", scale))
                    }
                    R.id.rb_4th -> {
                        et_stop_profit_position.setText(CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "1", scale))
                    }
                }
            }
        }

        et_stop_profit_position.onFocusChangeListener = object :View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1){
                    rg_trade.clearCheck()
                }
            }
        }


        getTakeProfitStopLoss()
    }

    private fun createTpslOrder() {
        addDisposable(getContractModel().createTpslOrder(mContractPositionBean?.contractId!!, mContractPositionBean?.id!!,mContractPositionBean?.positionType.toString(), mContractPositionBean?.orderSide.toString(), mContractPositionBean?.leverageLevel!!, orderList,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.cp_extra_text109))
                        CpEventBusUtil.post(
                                CpMessageEvent(
                                        CpMessageEvent.sl_contract_modify_position_margin_event
                                )
                        )
                        finish()

                    }
                }))
    }

    /**
     * 获取止盈止损列表
     * triggerPrice -> {Double@11600} 34.0
     * contractId -> {Integer@11606} 10
     */
    private fun getTakeProfitStopLoss() {
        addDisposable(getContractModel().getTakeProfitStopLoss(mContractPositionBean?.contractId!!.toString(), mContractPositionBean?.orderSide.toString(),
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            mTakeProfitList = optJSONArray("takeProfitList")
                            mStopLossList = optJSONArray("stopLossList")
                            if (mTakeProfitList.length()>0){
                                for (i in 0 until mTakeProfitList.length()) {
                                    val obj: JSONObject = mTakeProfitList.get(i) as JSONObject
                                   if (obj.optString("contractId")==mContractPositionBean?.contractId.toString()){
                                       et_stop_profit_trigger_price.setText(obj.optString("triggerPrice"))
                                       et_stop_profit_price.setText(obj.optString("triggerPrice"))
                                   }

                                }
                            }
                            if (mStopLossList.length()>0){
                                for (i in 0 until mStopLossList.length()) {
                                    val obj: JSONObject = mStopLossList.get(i) as JSONObject
                                   if (obj.optString("contractId")==mContractPositionBean?.contractId.toString()){
                                       et_stop_loss_trigger_price.setText(obj.optString("triggerPrice"))
                                       et_stop_loss_price.setText(obj.optString("triggerPrice"))
                                   }

                                }
                            }



                        }
                    }
                }))
    }

    /**
     * 撤销止盈止损单
     */
    private fun cancelProfitStopLossOrder(orderIds: String) {
        addDisposable(getContractModel().cancelOrderTpsl(mContractPositionBean?.contractId!!.toString(), orderIds,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        getTakeProfitStopLoss()
                    }
                }))
    }

    companion object {
        fun show(activity: Activity, mContractPositionBean: CpContractPositionBean) {
            val intent = Intent(activity, CpContractStopRateLossActivity::class.java)
            intent.putExtra("ContractPositionBean", mContractPositionBean)
            activity.startActivity(intent)
        }
    }

    override fun onWsMessage(json: String) {
        val jsonObj = JSONObject(json)
        val channel = jsonObj.optString("channel")
        if (!jsonObj.isNull("tick")) {
            if (!jsonObj.isNull("tick")) {
                val tick = jsonObj.optJSONObject("tick")
                if (channel == WsLinkUtils.tickerFor24HLink(currentSymbol, isChannel = true)) {
                    val close = tick.optString("close")
                    this.runOnUiThread {
                        tv_mark_price.text = CpBigDecimalUtils.showSNormal(close, mPricePrecision)
                    }
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        //et_stop_profit_trigger_price  //止盈触发价
        //et_stop_profit_price  //止盈委托价
        //et_stop_loss_trigger_price  //止损触发价
        //et_stop_loss_price  //止损委托价
        //et_stop_profit_position  //委托数量

        var stop_profit_trigger_price = et_stop_profit_trigger_price.text.toString().trim()
        var stop_profit_price = et_stop_profit_price.text.toString().trim()//委托价
        var stop_profit_position = et_stop_profit_position.text.toString().trim()

        var stop_loss_trigger_price = et_stop_loss_trigger_price.text.toString().trim()
        var stop_loss_price = et_stop_loss_price.text.toString().trim()//委托价

        val direction = if (mContractPositionBean?.orderSide.equals("BUY")) 0 else 1

        val isForward = CpClLogicContractSetting.getContractSideById(this, mContractPositionBean?.contractId!!) == 1

        val multiplier = CpClLogicContractSetting.getContractMultiplierById(this, mContractPositionBean?.contractId!!)


        var buff = "0"
        if (cb_tab_stop_profit.isChecked) {
            if (TextUtils.isEmpty(stop_profit_trigger_price) || TextUtils.isEmpty(stop_profit_position)) {
                tv_estimate_stop_profit.visibility = View.GONE
            } else {
                if (TextUtils.isEmpty(stop_profit_price) && isLimit){
                    tv_estimate_stop_profit.visibility = View.GONE
                }else{
                    tv_estimate_stop_profit.visibility = View.VISIBLE
                }
            }

            if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 1) {
                stop_profit_position = CpBigDecimalUtils.getOrderLossNum(stop_profit_position, multiplier)
            }
            buff = CpBigDecimalUtils.calcEstimatedProfitLoss(
                    isForward,
                    direction, isLimit,
                    mContractPositionBean?.openAvgPrice,
                    stop_profit_trigger_price,
                    stop_profit_price,
                    stop_profit_position,
                    multiplier,
                    marginRate,
                    marginCoinPrecision
            )

            if (CpBigDecimalUtils.compareTo(buff, "0") == 1) {
                tv_estimate_stop_profit.setText(getString(R.string.cp_order_text38)+"：" + buff + " " + mMarginCoin)
                tv_estimate_stop_profit.setTextColor(CpColorUtil.getMainColorType(true))
            } else {
                tv_estimate_stop_profit.setText(getString(R.string.cp_extra_text95) +"："+ buff + " " + mMarginCoin)
                tv_estimate_stop_profit.setTextColor(CpColorUtil.getMainColorType(false))
            }
        }
        stop_profit_position= et_stop_profit_position.text.toString().trim()
        if (cb_tab_stop_loss.isChecked) {
            if (TextUtils.isEmpty(stop_loss_trigger_price) || TextUtils.isEmpty(stop_profit_position)) {
                tv_estimate_loss_profit.visibility = View.GONE
            } else {
                if (TextUtils.isEmpty(stop_loss_price) && isLimit){
                    tv_estimate_loss_profit.visibility = View.GONE
                }else{
                    tv_estimate_loss_profit.visibility = View.VISIBLE
                }
            }
            if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 1) {
                stop_profit_position = CpBigDecimalUtils.getOrderLossNum(stop_profit_position, multiplier)
            }
            buff = CpBigDecimalUtils.calcEstimatedProfitLoss(
                    isForward,
                    direction, isLimit,
                    mContractPositionBean?.openAvgPrice,
                    stop_loss_trigger_price,
                    stop_loss_price,
                    stop_profit_position,
                    multiplier,
                    marginRate,
                    marginCoinPrecision
            )

            if (CpBigDecimalUtils.compareTo(buff, "0") == 1) {
                tv_estimate_loss_profit.setText(getString(R.string.cp_order_text38) +"："+ buff + " " + mMarginCoin)
                tv_estimate_loss_profit.setTextColor(CpColorUtil.getMainColorType(true))
            } else {
                tv_estimate_loss_profit.setText(getString(R.string.cp_extra_text95)  +"："+ buff + " " + mMarginCoin)
                tv_estimate_loss_profit.setTextColor(CpColorUtil.getMainColorType(false))
            }
        }

    }
}