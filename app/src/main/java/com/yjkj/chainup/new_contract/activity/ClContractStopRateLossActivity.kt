package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.chainup.contract.bean.CpContractPositionBean
import com.contract.sdk.ContractSDKAgent
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.numberFilter
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_contract.bean.ClTpslOrderBean
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.*
import com.yjkj.chainup.ws.WsContractAgentManager
import kotlinx.android.synthetic.main.cl_activity_stop_rate_loss.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 止盈止损
 */
class ClContractStopRateLossActivity : NBaseActivity(), WsContractAgentManager.WsResultCallback {
    override fun setContentView(): Int {
        return R.layout.cl_activity_stop_rate_loss
    }

    private var mContractPositionBean: CpContractPositionBean? = null
    private var isStopProfitMarket = true
    private var isStopLossMarket = true
    private var multiplier = "0"
    private var multiplierPrecision = 0
    private var mPricePrecision = 0
    private var currentSymbol = ""
    private lateinit var mTakeProfitList: JSONArray
    private lateinit var mStopLossList: JSONArray
    private lateinit var orderList: ArrayList<ClTpslOrderBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        initListener()

    }


    override fun loadData() {
        super.loadData()
        WsContractAgentManager.instance.addWsCallback(this)
        mContractPositionBean = intent.getSerializableExtra("ContractPositionBean") as CpContractPositionBean?

        var mContractJsonStr = LogicContractSetting.getContractJsonStrById(mActivity, mContractPositionBean?.contractId!!)
        multiplier = mContractJsonStr?.optString("multiplier").toString()

        multiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(mActivity, mContractPositionBean?.contractId!!)

        multiplierPrecision = if (LogicContractSetting.getContractUint(mActivity) == 0) 0 else multiplierPrecision

        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(mContext, mContractPositionBean?.contractId!!)

        currentSymbol = LogicContractSetting.getContractWsSymbolById(mContext, mContractPositionBean?.contractId!!)

        LogUtil.e(TAG, "multiplierPrecision:" + multiplierPrecision)
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

        val base = if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) getString(R.string.sl_str_contracts_unit) else mContractJsonStr?.optString("multiplierCoin")
        tv_position_key.text = getString(R.string.cl_positionlis_columns2) + "(" + base + ")"
        tv_stop_profit_volume_unit.text = base
        tv_stop_loss_volume_unit.text = base

        if (mContractPositionBean?.orderSide.equals("BUY")) {

        }

        var mPositionVolumeStr = if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) {
            mContractPositionBean?.positionVolume
        } else {
            BigDecimalUtils.mulStr(mContractPositionBean?.positionVolume, multiplier, multiplierPrecision)
        }

        var mCanCloseVolumeStr = if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) {
            mContractPositionBean?.canCloseVolume
        } else {
            BigDecimalUtils.mulStr(mContractPositionBean?.canCloseVolume, multiplier, multiplierPrecision)
        }

        tv_position_num.setText(mPositionVolumeStr + "/" + mCanCloseVolumeStr)
        tv_mark_price.setText(BigDecimalUtils.showSNormal(mContractPositionBean?.indexPrice, mPricePrecision))

        var reducePriceStr = BigDecimalUtils.showSNormal(mContractPositionBean?.reducePrice, mPricePrecision)
        if (BigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
            reducePriceStr = "--"
        }
        tv_force_close_price.setText(reducePriceStr)
    }


    override fun initView() {
        super.initView()
        initAutoTextView()
        orderList = ArrayList();
        if (isStopProfitMarket) {
            tv_market_price_hint.visibility = View.VISIBLE
            et_stop_profit_price.visibility = View.GONE
            tv_stop_profit_coin_name.visibility = View.GONE
            tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
            tv_stop_profit_price_hint?.setTextColor(ColorUtil.getColor(R.color.main_blue))
            et_stop_profit_price.requestFocus()
        } else {
            tv_market_price_hint.visibility = View.GONE
            et_stop_profit_price.visibility = View.VISIBLE
            tv_stop_profit_coin_name.visibility = View.VISIBLE
            tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
            tv_stop_profit_price_hint?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
            et_stop_profit_price.clearFocus()
        }

        if (isStopLossMarket) {
            tv_market_loss_price_hint.visibility = View.VISIBLE
            et_stop_loss_price.visibility = View.GONE
            tv_stop_loss_coin_name.visibility = View.GONE
            tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
            tv_stop_loss_price_hint?.setTextColor(ColorUtil.getColor(R.color.main_blue))
            et_stop_loss_price.requestFocus()
        } else {
            tv_market_loss_price_hint.visibility = View.GONE
            et_stop_loss_price.visibility = View.VISIBLE
            tv_stop_loss_coin_name.visibility = View.VISIBLE
            tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
            tv_stop_loss_price_hint?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
            et_stop_loss_price.clearFocus()
        }

        et_stop_profit_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_profit_price?.setBackgroundResource(if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused)
            if (hasFocus) {
                isStopProfitMarket = false
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
            }
        }

        et_stop_loss_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_loss_price?.setBackgroundResource(if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused)
            if (hasFocus) {
                isStopProfitMarket = false
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
            }
        }

        tv_stop_profit_price_hint.setOnClickListener {
            isStopProfitMarket = !isStopProfitMarket
            if (isStopProfitMarket) {
                tv_market_price_hint.visibility = View.VISIBLE
                et_stop_profit_price.visibility = View.GONE
                tv_stop_profit_coin_name.visibility = View.GONE
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
                tv_stop_profit_price_hint?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                et_stop_profit_price.clearFocus()
            } else {
                tv_market_price_hint.visibility = View.GONE
                et_stop_profit_price.visibility = View.VISIBLE
                tv_stop_profit_coin_name.visibility = View.VISIBLE
                tv_stop_profit_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
                tv_stop_profit_price_hint?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                et_stop_profit_price.requestFocus()
            }
        }

        tv_stop_loss_price_hint.setOnClickListener {
            isStopLossMarket = !isStopLossMarket
            if (isStopLossMarket) {
                tv_market_loss_price_hint.visibility = View.VISIBLE
                et_stop_loss_price.visibility = View.GONE
                tv_stop_loss_coin_name.visibility = View.GONE
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
                tv_stop_loss_price_hint?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                et_stop_loss_price.clearFocus()
            } else {
                tv_market_loss_price_hint.visibility = View.GONE
                et_stop_loss_price.visibility = View.VISIBLE
                tv_stop_loss_coin_name.visibility = View.VISIBLE
                tv_stop_loss_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
                tv_stop_loss_price_hint?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                et_stop_loss_price.requestFocus()
            }
        }

        btn_cancel_stop_profit.setOnClickListener {
            var buff = StringBuffer()
            for (i in 0..(mTakeProfitList.length() - 1)) {
                var obj: JSONObject = mTakeProfitList.get(i) as JSONObject
                buff.append(obj.optString("id"))
                buff.append(",")
            }
            val buff1 = buff.substring(0, buff.length - 1)
            cancelProfitStopLossOrder(buff1)
        }

        btn_cancel_stop_loss.setOnClickListener {
            var buff = StringBuffer()
            for (i in 0..(mStopLossList.length() - 1)) {
                var obj: JSONObject = mStopLossList.get(i) as JSONObject
                buff.append(obj.optString("id"))
                buff.append(",")
            }
            val buff1 = buff.substring(0, buff.length - 1)
            cancelProfitStopLossOrder(buff1)
        }
    }

    private fun initAutoTextView() {
        title_layout.title = getLineText("cl_stoporder_titletext")
    }

    private fun initListener() {
//        et_stop_profit_trigger_price.numberFilter(3)
//        et_stop_profit_price.numberFilter(3)
//        et_stop_profit_position.numberFilter(0)
//
//        et_stop_loss_trigger_price.numberFilter(3)
//        et_stop_loss_price.numberFilter(3)
//        et_stop_loss_position.numberFilter(0)


        ll_tab_layout_stop_profit.setOnClickListener {
            cb_tab_stop_profit.isChecked = !cb_tab_stop_profit.isChecked
        }
        ll_tab_layout_stop_loss.setOnClickListener {
            cb_tab_stop_loss.isChecked = !cb_tab_stop_loss.isChecked
        }

        tv_confirm_btn.isEnable(true)
        tv_confirm_btn.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (!cb_tab_stop_profit.isChecked && !cb_tab_stop_loss.isChecked) {
                    return
                }
                var orderType = 1;
                orderList.clear()
                if (cb_tab_stop_profit.isChecked) {
                    orderType = if (isStopProfitMarket) 2 else 1
                    val et_stop_profit_trigger_price_str = et_stop_profit_trigger_price.text.toString()
                    val et_stop_profit_price_str = et_stop_profit_price.text.toString()
                    var et_stop_profit_position_str = et_stop_profit_position.text.toString()
                    if (TextUtils.isEmpty(et_stop_profit_trigger_price_str)) {
                        return
                    }
                    if (TextUtils.isEmpty(et_stop_profit_price_str) && !isStopProfitMarket) {
                        return
                    }
                    if (TextUtils.isEmpty(et_stop_profit_position_str)) {
                        return
                    }

                    if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 1) {
                        et_stop_profit_position_str = BigDecimalUtils.getOrderLossNum(et_stop_profit_position_str, multiplier)
                    }

                    /**
                     *  止盈单--触发价校验：
                     *   多仓：止盈触发价>标记价格，否则红框提醒：止盈价需大于标记价格！
                     *   空仓：止盈触发价<标记价格，否则红框提醒：止盈价需小于标记价格！
                     */
//                    if (mContractPositionBean?.orderSide.equals("BUY")){
//                        LogUtil.e("------------------",BigDecimalUtils.compareTo(et_stop_profit_trigger_price_str,mContractPositionBean?.indexPrice).toString())
//                        if (BigDecimalUtils.compareTo(et_stop_profit_trigger_price_str,mContractPositionBean?.indexPrice)!=1){
//                            NToastUtil.showTopToast(false, "止盈价需大于标记价格！")
//                            return
//                        }
//                    }else{
//                        if (BigDecimalUtils.compareTo(et_stop_profit_trigger_price_str,mContractPositionBean?.indexPrice)==-1){
//                            NToastUtil.showTopToast(false, "止盈价需小于标记价格！")
//                            return
//                        }
//                    }

                    orderList.add(ClTpslOrderBean("2", if (isStopProfitMarket) "0" else et_stop_profit_price_str, et_stop_profit_position_str, et_stop_profit_trigger_price_str, orderType.toString(), LogicContractSetting.getStrategyEffectTimeStr(mActivity)))
                }
                if (cb_tab_stop_loss.isChecked) {
                    orderType = if (isStopLossMarket) 2 else 1
                    val et_stop_loss_trigger_price_str = et_stop_loss_trigger_price.text.toString()
                    val et_stop_loss_price_str = et_stop_loss_price.text.toString()
                    var et_stop_loss_position_str = et_stop_loss_position.text.toString()
                    if (TextUtils.isEmpty(et_stop_loss_trigger_price_str)) {
                        return
                    }
                    if (TextUtils.isEmpty(et_stop_loss_price_str) && !isStopLossMarket) {
                        return
                    }
                    if (TextUtils.isEmpty(et_stop_loss_position_str)) {
                        return
                    }
                    if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 1) {
                        et_stop_loss_position_str = BigDecimalUtils.getOrderLossNum(et_stop_loss_position_str, multiplier)
                    }
                    /**
                     *  止损单--触发价校验：
                     *  多仓：强平价格<止损触发价<标记价格，否则红框提醒：止损价需小于标记价格！或 止损价需大于强平价格！
                     *  空仓：最新价格<止损触发价<强平价格，否则红框提醒：止损价需大于标记价格！或 止损价需小于强平价格！
                     */
//                    if (mContractPositionBean?.orderSide.equals("BUY")){
//                        LogUtil.e("----------",BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.reducePrice).toString())
//                        LogUtil.e("----------",BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.indexPrice).toString())
//                        if (BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.reducePrice)!=1){
//                            NToastUtil.showTopToast(false, "止损价需大于强平价格！")
//                            return
//                        }
//                        if (BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.indexPrice)!=-1){
//                            NToastUtil.showTopToast(false, "止损价需小于标记价格！")
//                            return
//                        }
//                    }else{
//                        if (BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.indexPrice)!=1&&BigDecimalUtils.compareTo(et_stop_loss_trigger_price_str,mContractPositionBean?.reducePrice)!=-1){
//                            NToastUtil.showTopToast(false, "止损价需大于标记价格！或 止损价需小于强平价格！")
//                            return
//                        }
//                    }

                    orderList.add(ClTpslOrderBean("1", if (isStopLossMarket) "0" else et_stop_loss_price_str, et_stop_loss_position_str, et_stop_loss_trigger_price_str, orderType.toString(), LogicContractSetting.getStrategyEffectTimeStr(mActivity)))
                }
                addDisposable(getContractModel().createTpslOrder(mContractPositionBean?.contractId!!, mContractPositionBean?.positionType.toString(), mContractPositionBean?.orderSide.toString(), mContractPositionBean?.leverageLevel!!, orderList,
                        consumer = object : NDisposableObserver(mActivity, true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                val errorMsg = StringBuffer()
                                jsonObject.optJSONObject("data").run {
                                    val respList = optJSONArray("respList")
                                    for (i in 0..(respList.length() - 1)) {
                                        var obj: JSONObject = respList.get(i) as JSONObject
                                        val code = obj.optString("code")
                                        val msg = obj.optString("msg")
                                        if (!code.equals("0")) {
                                            errorMsg.append(msg)
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(errorMsg)) {
                                    NToastUtil.showTopToastNet(this@ClContractStopRateLossActivity,false, errorMsg.toString())
                                } else {
                                    finish()
                                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_modify_position_margin_event))
                                }
                            }
                        }))
            }
        }
        getTakeProfitStopLoss()
    }

    /**
     * 获取止盈止损列表
     */
    private fun getTakeProfitStopLoss() {
        addDisposable(getContractModel().getTakeProfitStopLoss(mContractPositionBean?.contractId!!.toString(), mContractPositionBean?.orderSide.toString(),
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            mTakeProfitList = optJSONArray("takeProfitList")
                            mStopLossList = optJSONArray("stopLossList")
                            btn_cancel_stop_profit.visibility = if (mTakeProfitList.length() == 0) View.GONE else View.VISIBLE
                            btn_cancel_stop_loss.visibility = if (mStopLossList.length() == 0) View.GONE else View.VISIBLE
                            btn_cancel_stop_profit.setText(getString(R.string.cl_stoporder_text2) + "：" + mTakeProfitList.length())
                            btn_cancel_stop_loss.setText(getString(R.string.cl_stoporder_text8) + "：" + mStopLossList.length())
                        }
                    }
                }))
    }

    /**
     * 撤销止盈止损单
     */
    private fun cancelProfitStopLossOrder(orderIds: String) {
        addDisposable(getContractModel().cancelOrderTpsl(mContractPositionBean?.contractId!!.toString(), orderIds,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        getTakeProfitStopLoss()
                    }
                }))
    }

    companion object {
        fun show(activity: Activity, mContractPositionBean: CpContractPositionBean) {
            val intent = Intent(activity, ClContractStopRateLossActivity::class.java)
            intent.putExtra("ContractPositionBean", mContractPositionBean)
            activity.startActivity(intent)
        }
    }

    override fun onWsMessage(json: String) {
        LogUtil.e(TAG, "止赢止损界面返回数据：" + json)
        val jsonObj = JSONObject(json)
        val channel = jsonObj.optString("channel")
        if (!jsonObj.isNull("tick")) {
            if (!jsonObj.isNull("tick")) {
                val tick = jsonObj.optJSONObject("tick")
                if (channel == WsLinkUtils.tickerFor24HLink(currentSymbol, isChannel = true)) {
                    val close = tick.optString("close")
                    this?.runOnUiThread {
                        tv_mark_price.setText(BigDecimalUtils.showSNormal(close, mPricePrecision))
                    }
                }
            }
        }
    }
}