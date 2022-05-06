package com.yjkj.chainup.new_version.activity.oldgrid

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.activity.oldgrid.adapter.OldAiGridAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DecimalDigitsInputFilter
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.item_custom_grid_adapter_old.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2021/2/1-5:23 PM
 * @Email buptjinlong@163.com
 * @description
 */
class OldNCustomGridFragment : NBaseFragment() {

    var symbol = ""

    var coin = ""
    var base = ""

    /*七日收益率*/
    var sevenAnnualizedYield = ""

    /*价格区间 高*/
    var highestPrice = ""

    /*价格区间 低*/
    var lowestPrice = ""

    /*预计最小利润率*/
    var everyProfitMin = ""

    /*预计最大利润率*/
    var everyProfitMax = ""

    /*网格数量 */
    var gridNumber = ""

    /*总投入金额 */
    var totalQuoteAmount = ""

    /*网格类型 1:等差 2:等比 */
    var gridLineType = "1"

    /*是否使用原有资产 0:不使用 1:使用 */
    var useOwnBase = "0"

    /*止损价格 */
    var stopPrice = ""

    /*止盈价格 */
    var profitTakingPrice = ""

    /*最小下单数量 */
    var minimumOrderQuantity = ""

    /*手续费 */
    var makerFee = "0"

    /*当前价格 */
    var currentPrice = ""

    /*网格数量 */
    var gridAmount = ""

    var list: ArrayList<JSONObject> = arrayListOf()

    override fun initView() {
        getAIStrategyInfo(symbol)
        setOnclick()
        observeData()
        initAdapter()
        btn_begin_grid_custom?.isEnable(true)
        tv_check_full_stop?.text = LanguageUtil.getString(context, "quant_stop_hign_and_low") + LanguageUtil.getString(context, "common_text_optionalinput")
    }

    private fun observeData() {
        NLiveDataUtil.observeForeverData {
            if (null == it || !it.isGrid) {
                return@observeForeverData
            }
            when (it.msg_type) {
                //切换币种事件
                MessageEvent.symbol_switch_type -> {
                    if (null != it.msg_content) {
                        var coinSymbol = it.msg_content as String
                        symbol = NCoinManager.getNameForSymbol(coinSymbol)
                        getAIStrategyInfo(symbol)
                        getStrategyList(symbol)
                        clearView()
                    }
                }
            }
        }
    }

    fun setEditFocusable() {
        if (!LoginManager.isLogin(context)) {
            rd_check_surplus?.isFocusableInTouchMode = false
            ed_stop_loss?.isFocusableInTouchMode = false
            ed_custom_grid_num?.isFocusableInTouchMode = false
            et_volume_investment?.isFocusableInTouchMode = false
            rd_check_surplus_check_full?.isFocusableInTouchMode = false
            ed_stop_loss_check_full?.isFocusableInTouchMode = false
        } else {
            if (rd_check_surplus?.isFocusableInTouchMode?.not() == true) {
                rd_check_surplus?.isFocusable = true
                rd_check_surplus?.isFocusableInTouchMode = true
                rd_check_surplus?.requestFocus()
                rd_check_surplus?.findFocus()
            }
            if (ed_stop_loss?.isFocusableInTouchMode?.not() == true) {
                ed_stop_loss?.isFocusable = true
                ed_stop_loss?.isFocusableInTouchMode = true
                ed_stop_loss?.requestFocus()
                ed_stop_loss?.findFocus()
            }
            if (ed_custom_grid_num?.isFocusableInTouchMode?.not() == true) {
                ed_custom_grid_num?.isFocusable = true
                ed_custom_grid_num?.isFocusableInTouchMode = true
                ed_custom_grid_num?.requestFocus()
                ed_custom_grid_num?.findFocus()
            }
            if (et_volume_investment?.isFocusableInTouchMode?.not() == true) {
                et_volume_investment?.isFocusable = true
                et_volume_investment?.isFocusableInTouchMode = true
                et_volume_investment?.requestFocus()
                et_volume_investment?.findFocus()
            }
            if (rd_check_surplus_check_full?.isFocusableInTouchMode?.not() == true) {
                rd_check_surplus_check_full?.isFocusable = true
                rd_check_surplus_check_full?.isFocusableInTouchMode = true
                rd_check_surplus_check_full?.requestFocus()
                rd_check_surplus_check_full?.findFocus()
            }
            if (ed_stop_loss_check_full?.isFocusableInTouchMode?.not() == true) {
                ed_stop_loss_check_full?.isFocusable = true
                ed_stop_loss_check_full?.isFocusableInTouchMode = true
                ed_stop_loss_check_full?.requestFocus()
                ed_stop_loss_check_full?.findFocus()
            }
        }
    }

    fun setOnclick() {

        /**
         * 等差网络
         */
        tv_arithmetic_grid?.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                if (lowestPrice == "0" || lowestPrice.isEmpty() || highestPrice == "0" || highestPrice.isEmpty() || gridNumber == "0" || gridNumber.isEmpty()) {
                    gt_division_profits?.setContentTextInterval("--% ~ --%(${LanguageUtil.getString(context, "trading_fee_deducted")})")
                }
                gridLineType = "1"
                calculateMaxOrMin()
                tv_arithmetic_grid?.run {
                    textColor = ContextCompat.getColor(context, R.color.main_color)
                    setBackgroundResource(R.drawable.bg_grid)
                }
                tv_geometric_grid?.run {
                    textColor = ContextCompat.getColor(context, R.color.normal_text_color)
                    setBackgroundResource(R.drawable.bg_grid_gray)
                }
            }
        }
        /**
         * 等比网络
         */
        tv_geometric_grid?.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {

                if (lowestPrice == "0" || lowestPrice.isEmpty() || highestPrice == "0" || highestPrice.isEmpty() || gridNumber == "0" || gridNumber.isEmpty()) {
                    gt_division_profits?.setContentTextInterval("--%(${LanguageUtil.getString(context, "trading_fee_deducted")})")
                }


                gridLineType = "2"
                calculateMaxOrMin()
                tv_arithmetic_grid?.run {
                    textColor = ContextCompat.getColor(context, R.color.normal_text_color)
                    setBackgroundResource(R.drawable.bg_grid_gray)
                }
                tv_geometric_grid?.run {
                    textColor = ContextCompat.getColor(context, R.color.main_color)
                    setBackgroundResource(R.drawable.bg_grid)
                }
            }
        }
        /**
         * 价格区间 低
         */
        rd_check_surplus?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        rd_check_surplus?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                lowestPrice = p0.toString()
                if (lowestPrice.isNotEmpty()) {
                    ll_check_surplus_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                    ll_stop_loss_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
                calculateMaxOrMin()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        /**
         * 价格区间 高
         */
        ed_stop_loss?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        ed_stop_loss?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                highestPrice = p0.toString()
                if (highestPrice.isNotEmpty()) {
                    ll_stop_loss_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                    ll_check_surplus_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
                calculateMaxOrMin()


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        /**
         * 网格数量
         */
        ed_custom_grid_num?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        ed_custom_grid_num?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.e("jinlong", "afterTextChanged")
                gridNumber = p0.toString()
                if (gridNumber.isNotEmpty()) {
                    ed_custom_grid_num?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
                calculateMaxOrMin()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        /**
         * 投入资产
         */
        et_volume_investment?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        et_volume_investment?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                totalQuoteAmount = p0.toString()
                if (totalQuoteAmount.isNotEmpty()) {
                    ll_volum_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
                changeTotalQuoteAmount()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        /**
         * 止损价格
         */
        rd_check_surplus_check_full?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        rd_check_surplus_check_full?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                stopPrice = s.toString()
                if (stopPrice.isNotEmpty()) {
                    ll_stop_price_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
        /**
         * 止盈价格
         */
        ed_stop_loss_check_full?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        ed_stop_loss_check_full?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                profitTakingPrice = p0.toString()
                if (profitTakingPrice.isNotEmpty()) {
                    ll_profit_taking_price_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        tv_history_grid_title?.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                ArouterUtil.navigation(RoutePath.HistoryGridActivity, Bundle().apply {
                    putString(ParamConstant.COIN_SYMBOL, symbol)
                })
            }
        }
        /**
         * 保存策略
         */
        btn_begin_grid_custom?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (LoginManager.checkLogin(context, true)) {

                    if (lowestPrice.isEmpty()) {
                        ll_check_surplus_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.otc_mustWrite_tex))
                        return
                    }
                    if (highestPrice.isEmpty()) {
                        ll_stop_loss_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.otc_mustWrite_tex))
                        return
                    }
                    if (gridNumber.isEmpty()) {
                        ed_custom_grid_num?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.otc_mustWrite_tex))
                        return
                    }
                    if (totalQuoteAmount.isEmpty()) {
                        ll_volum_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.otc_mustWrite_tex))
                        return
                    }


                    if (BigDecimalUtils.compareTo(highestPrice, BigDecimalUtils.mul(lowestPrice, "1.02").toPlainString()) < 0) {
                        NToastUtil.showTopToast(false, getString(R.string.price_range_smal))
                        ll_stop_loss_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        ll_check_surplus_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }

                    if (BigDecimalUtils.compareTo(gridNumber, "2") < 0 || BigDecimalUtils.compareTo(gridNumber, "100") > 0) {
                        ed_custom_grid_num?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.grid_range_number))
                        return
                    }

                    if (BigDecimalUtils.compareTo(everyProfitMin, "0") <= 0 || BigDecimalUtils.compareTo(everyProfitMax, "0") <= 0) {
                        NToastUtil.showTopToast(false, getString(R.string.profit_per_grid_small))
                        return
                    }


                    if (BigDecimalUtils.compareTo(totalCoinAmount, totalQuoteAmount) < 0) {
                        NToastUtil.showTopToast(false, "${NCoinManager.getShowMarket(totalBaseCoin)} ${LanguageUtil.getString(context, "common_tip_balanceNotEnough")}")
                        ll_volum_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }
                    if (BigDecimalUtils.compareTo(gridAmount, minimumOrderQuantity) < 0) {
                        ll_volum_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        NToastUtil.showTopToast(false, getString(R.string.minimum_current_currency_pair))
                        return
                    }

                    if (stopPrice.isNotEmpty() && BigDecimalUtils.compareTo(stopPrice, lowestPrice) >= 0) {
                        NToastUtil.showTopToast(false, getString(R.string.quant_stop_low_error))
                        ll_stop_price_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }

                    if (stopPrice.isNotEmpty() && BigDecimalUtils.compareTo(stopPrice, currentPrice) >= 0) {
                        NToastUtil.showTopToast(false, getString(R.string.stop_loss_price_price))
                        ll_stop_price_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }

                    if (profitTakingPrice.isNotEmpty() && BigDecimalUtils.compareTo(profitTakingPrice, highestPrice) <= 0) {
                        NToastUtil.showTopToast(false, getString(R.string.quant_stop_high_error))
                        ll_profit_taking_price_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }

                    if (profitTakingPrice.isNotEmpty() && BigDecimalUtils.compareTo(profitTakingPrice, currentPrice) <= 0) {
                        NToastUtil.showTopToast(false, getString(R.string.loss_price_higher_current_price))
                        ll_profit_taking_price_layout?.setBackgroundResource(R.drawable.bg_grid_red)
                        return
                    }

                    if (useOwnBase == "1") {
                        calBaseAmount(symbol, lowestPrice, highestPrice, gridNumber, gridLineType, totalQuoteAmount, currentPrice, makerFee)
                    } else {
                        saveStrategy(symbol, "1", gridLineType, gridNumber, lowestPrice, highestPrice, profitTakingPrice, stopPrice, totalQuoteAmount, useOwnBase, makerFee, "0")
                    }
                }
            }
        }
        /**
         * 使用原有baseCoin
         */
        switch_fingerprint_pwd?.setOnCheckedChangeListener { compoundButton, b ->
            Log.e("jinlong", "b:$b")
            if (LoginManager.checkLogin(context, true)) {
                useOwnBase = if (b) {
                    "1"
                } else {
                    "0"
                }
                setViewSelect(switch_fingerprint_pwd, useOwnBase == "1")
            }

        }
    }


    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
    }

    fun setEditPrice() {
        var name = NCoinManager.getMarket4Name((base + coin).toLowerCase())
        var prico = name.optInt("price")
        rd_check_surplus?.filters = arrayOf(DecimalDigitsInputFilter(prico))
        ed_stop_loss?.filters = arrayOf(DecimalDigitsInputFilter(prico))
        et_volume_investment?.filters = arrayOf(DecimalDigitsInputFilter(prico))
        rd_check_surplus_check_full?.filters = arrayOf(DecimalDigitsInputFilter(prico))
        ed_stop_loss_check_full?.filters = arrayOf(DecimalDigitsInputFilter(prico))
    }

    fun getAIStrategyInfo(symbol: String = "") {
        runOnUiThread {
            if (symbol.isNotEmpty()) {
                base = symbol.split("/")[0]
                coin = symbol.split("/")[1]
                tv_used_btc?.text = "${LanguageUtil.getString(context, "quant_use_own_base")}${NCoinManager.getShowMarket(base)}"
                setEditPrice()
                st_check_surplus?.text = NCoinManager.getShowMarket(coin)
                st_stop_loss?.text = NCoinManager.getShowMarket(coin)
                tv_coin_name_investment?.text = NCoinManager.getShowMarket(coin)
                st_check_surplus_check_full?.text = NCoinManager.getShowMarket(coin)
                stv_stop_loss_check_full?.text = NCoinManager.getShowMarket(coin)
                if (!UserDataService.getInstance().isLogined) {
                    tv_balance_str?.text = "-- ${NCoinManager.getShowMarket(coin)} -- ${NCoinManager.getShowMarket(base)}"
                }
                setDivisionProfits()
            }
        }
        addDisposable(getMainModel().getAIStrategyInfo(symbol, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data") ?: return
                /*手续费 */
                makerFee = json.optString("makerFee")
                /* 	最小下单数量 */
                minimumOrderQuantity = json.optString("minimumOrderQuantity")
            }
        }))
    }

    fun setDivisionProfits() {
        when (gridLineType) {
            "1" -> {
                gt_division_profits?.setContentTextInterval("--% ~ --%(${LanguageUtil.getString(context, "trading_fee_deducted")})")
            }
            "2" -> {
                gt_division_profits?.setContentTextInterval("--%(${LanguageUtil.getString(context, "trading_fee_deducted")})")
            }
        }
    }


    fun calculateMaxOrMin() {
        if (updateIsEmpty(highestPrice) || updateIsEmpty(lowestPrice) || updateIsEmpty(gridNumber)) {
            setDivisionProfits()
            return
        }
        when (gridLineType) {
            "1" -> {
                var poor = BigDecimalUtils.sub(highestPrice, lowestPrice).toPlainString()
                var division = BigDecimalUtils.div(poor, BigDecimalUtils.sub(gridNumber, "1").toPlainString()).toPlainString()
                var geometricDivMax = BigDecimalUtils.div(division, lowestPrice).toPlainString()
                var geometricDivMin = BigDecimalUtils.div(division, BigDecimalUtils.sub(highestPrice, division).toPlainString()).toPlainString()
                var max = BigDecimalUtils.sub(geometricDivMax, BigDecimalUtils.mul(makerFee, "2").toPlainString()).toPlainString()
                everyProfitMax = BigDecimalUtils.mulStr(max, "100", 2)
                var min = BigDecimalUtils.sub(geometricDivMin, BigDecimalUtils.mul(makerFee, "2").toPlainString()).toPlainString()
                everyProfitMin = BigDecimalUtils.mulStr(min, "100", 2)
                gt_division_profits?.setContentTextInterval("$everyProfitMin% ~ $everyProfitMax%(${LanguageUtil.getString(context, "trading_fee_deducted")})")

            }
            "2" -> {
                var division = BigDecimalUtils.div(highestPrice, lowestPrice).toPlainString()
                var gridNum = BigDecimalUtils.sub(gridNumber, "1").toDouble()
                var prescribing = Math.pow(division.toDouble(), BigDecimalUtils.div("1", gridNum.toString()).toDouble()).toString()
                var max = BigDecimalUtils.sub(prescribing, BigDecimalUtils.mul(makerFee, "2").toPlainString()).toPlainString()
                var max2 = BigDecimalUtils.sub(max, "1").toPlainString()
                everyProfitMax = BigDecimalUtils.mulStr(max2, "100", 2)
                everyProfitMin = everyProfitMax
                gt_division_profits?.setContentTextInterval("$everyProfitMax%(${LanguageUtil.getString(context, "trading_fee_deducted")})")
            }
        }
    }


    fun updateIsEmpty(temp: String): Boolean {
        if (temp.isEmpty() || temp == "0") {
            return true
        }
        return false
    }

    /**
     * 保存策略
     * @param symbol 币对 BTC/USDT
     * @param quantType 量化交易类型 1:网格
     * @param gridLineType  网格类型 1:等差 2:等比
     * @param gridNumber 网格数量
     * @param lowestPrice 网格下限
     * @param highestPrice 网格上限
     * @param stopHighPrice 停止网格上限
     * @param stopLowPrice 停止网格下限
     * @param totalQuoteAmount 用户投入资产
     * @param useOwnBase 是否使用Base资产 0:不使用 1:使用
     * @return
     */
    fun saveStrategy(symbol: String, quantType: String, gridLineType: String, gridNumber: String,
                     lowestPrice: String, highestPrice: String, stopHighPrice: String, stopLowPrice: String,
                     totalQuoteAmount: String, useOwnBase: String, fee: String, totalBaseAmount: String) {
        addDisposable(getMainModel().saveStrategy(symbol, quantType, gridLineType, gridNumber,
                lowestPrice, highestPrice, stopHighPrice, stopLowPrice, totalQuoteAmount, useOwnBase, fee, totalBaseAmount, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                clearView()
                NToastUtil.showTopToastNet(activity, true, LanguageUtil.getString(context, "grid_check_execution"))
                getStrategyList(symbol)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(activity, false, msg)
            }

        }))
    }

    fun clearView() {
        rd_check_surplus?.setText("")
        ed_stop_loss?.setText("")
        ed_custom_grid_num?.setText("")
        et_volume_investment?.setText("")
        rd_check_surplus_check_full?.setText("")
        ed_stop_loss_check_full?.setText("")

        ll_check_surplus_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
        ll_stop_loss_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
        ll_profit_taking_price_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
        ll_stop_price_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
        ll_volum_layout?.setBackgroundResource(R.drawable.bg_grid_gray)
        ed_custom_grid_num?.setBackgroundResource(R.drawable.bg_grid_gray)
    }

    /**
     * 计算使用base投入总资产
     * @param symbol 币对 BTC/USDT
     * @param gridLineType  网格类型 1:等差 2:等比
     * @param gridNumber 网格数量
     * @param lowestPrice 网格下限
     * @param highestPrice 网格上限
     * @param totalQuoteAmount 用户投入资产
     * @param currentPrice 当前价格
     * @return
     */
    fun calBaseAmount(symbol: String, lowestPrice: String, highestPrice: String, gridNumber: String, gridLineType: String,
                      totalQuoteAmount: String, currentPrice: String, fee: String) {
        addDisposable(getMainModel().calBaseAmount(symbol, lowestPrice, highestPrice, gridNumber, gridLineType, totalQuoteAmount, currentPrice, fee, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data") ?: return
                var baseAmount = json.optString("baseAmount")
                if (BigDecimalUtils.compareTo(baseAmount4Newwork, baseAmount) >= 0) {
                    totalBaseAmount = baseAmount
                    saveStrategy(symbol, "1", gridLineType, gridNumber, lowestPrice, highestPrice, profitTakingPrice, stopPrice, totalQuoteAmount, useOwnBase, makerFee, totalBaseAmount)
                } else {
                    NToastUtil.showTopToast(false, getString(R.string.grid_need_least) + " " + baseAmount + NCoinManager.getShowMarket(base))
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(activity, false, msg)
            }
        }))
    }

    override fun setContentView() = R.layout.item_custom_grid_adapter_old

    var totalBaseAmount = "0"
    var baseAmount4Newwork = "0"
    var totalCoinAmount = "0"
    var totalBaseCoin = ""

    /**
     * 这里获取 总资产
     */
    fun setAccountBalance(base: String, baseBalance: String, coin: String, coinBalance: String) {
        totalBaseAmount = baseBalance
        baseAmount4Newwork = baseBalance
        totalCoinAmount = coinBalance
        totalBaseCoin = coin


        var name = NCoinManager.getMarket4Name((base + coin).toLowerCase())
        var pricePrecision = name.optInt("price")
        var volumePrecision = name.optInt("volume")

        tv_balance_str?.text = "${BigDecimalUtils.divForDown(coinBalance, pricePrecision).toPlainString()}${NCoinManager.getShowMarket(coin)} ${BigDecimalUtils.divForDown(baseBalance, volumePrecision).toPlainString()}${NCoinManager.getShowMarket(base)}"
    }

    fun initAdapter() {
        oldAiGridAdapter = OldAiGridAdapter(list, object : OldGridStopStrategyListener {
            override fun stopStrategy(id: String) {
                NewDialogUtils.showNormalDialog(context!!, getString(R.string.quant_alert_stopGrid), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        stopStrategyForNetwork(id)
                    }
                }, LanguageUtil.getString(context, "common_text_tip"))
            }

        }, false)
        recycler_view?.layoutManager = LinearLayoutManager(activity)
        oldAiGridAdapter?.setEmptyView(EmptyForAdapterView(activity ?: return))
        recycler_view?.adapter = oldAiGridAdapter
    }


    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        val mainActivity = activity
        if (mainActivity != null) {
            if (mainActivity is NewMainActivity) {
                if (isVisibleToUser && mainActivity.curPosition == 2) {
                    getAIStrategyInfo(symbol)
                    setButtonStatus()
                    clearView()
                    setEditFocusable()
                    if (!UserDataService.getInstance().isLogined) {
                        list.clear()
                        oldAiGridAdapter?.setList(list)
                    }
                    loopData()
                } else {
                    subscribeCoin?.dispose()
                }

            }
        }
    }

    override fun onStop() {
        super.onStop()
        subscribeCoin?.dispose()
    }


    fun setButtonStatus() {
        if (UserDataService.getInstance().isLogined) {
            btn_begin_grid_custom?.setContent(getString(R.string.quant_start_trade))
        } else {
            btn_begin_grid_custom?.setContent(getString(R.string.login_action_login))

        }
    }

    var oldAiGridAdapter: OldAiGridAdapter? = null

    /**
     * 获取正在执行列表
     */
    fun getStrategyList(symbol: String = "") {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getMainModel().getStrategyList(symbol, "1", "1", "100", object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    var json = jsonObject.optJSONObject("data")
                    var strategyVoList = json.optJSONArray("strategyVoList") ?: return
                    var listNew = JSONUtil.arrayToList(strategyVoList) ?: arrayListOf()
                    list.clear()
                    list.addAll(listNew)
                    oldAiGridAdapter?.setList(list)
                }
            }))
        }
    }

    private var cvcFragment = true
    var subscribeCoin: Disposable? = null//保存订阅者

    override fun background() {
        super.background()
        cvcFragment = false
    }

    override fun foreground() {
        super.foreground()
        cvcFragment = true
    }

    private fun loopData(status: Boolean = true) {
        LogUtil.e(TAG, "ETF value loopData  $mIsVisibleToUser $cvcFragment")

        if (!mIsVisibleToUser || !cvcFragment)
            return
        if (subscribeCoin == null || (subscribeCoin != null && subscribeCoin?.isDisposed != null && subscribeCoin?.isDisposed!!)) {
            subscribeCoin = Observable.interval(0L, CommonConstant.etfLoopTime, TimeUnit.SECONDS)//按时间间隔发送整数的Observable
                    .observeOn(AndroidSchedulers.mainThread())//切换到主线程修改UI
                    .subscribe {
                        if (!UserDataService.getInstance().isLogined) return@subscribe
                        getStrategyList(symbol)
                    }
        }
    }

    /**
     * 停止策略
     */
    fun stopStrategyForNetwork(id: String) {
        addDisposable(getMainModel().stopStrategy(id, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                getStrategyList(symbol)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(activity, false, msg)
            }
        }))
    }

    fun changeTotalQuoteAmount() {
        gridAmount = BigDecimalUtils.div(totalQuoteAmount, gridNumber).toPlainString()
    }

}