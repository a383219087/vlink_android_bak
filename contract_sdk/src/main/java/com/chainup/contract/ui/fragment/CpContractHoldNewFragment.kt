package com.chainup.contract.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.chainup.contract.R
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.bean.CpCreateOrderBean
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.coorchice.library.SuperTextView
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_hold_new.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.*


/**
 * 当前持仓
 */
class CpContractHoldNewFragment : CpNBaseFragment() {

    private var adapter: CpHoldContractNewAdapter? = null
    private var mList = ArrayList<CpContractPositionBean>()
    private var mAllList = ArrayList<CpContractPositionBean>()


    //是否显示全部合约，1是显示当前合约持仓
    private var showAll = 0

    //合约id
    var mContractId = -1



    var subscribe: Disposable? = null

    var positionModel:Int = 1


    override fun setContentView(): Int {
        return R.layout.cp_fragment_cl_contract_hold_new
    }

    var mAdjustMarginDialog: TDialog? = null
    var mClosePositionDialog: TDialog? = null
    var mReverseOpenDialog: TDialog? = null
    var mPositionObj: JSONObject? = null
    override fun initView() {
        initOnClick()
        showSwitch()
        adapter = CpHoldContractNewAdapter(mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract?.isNestedScrollingEnabled = false
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyOrderForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_reverse_opem,
            R.id.tv_close_position,
            R.id.tv_forced_close_price_key,
            R.id.tv_adjust_margins,
            R.id.tv_profit_loss,
            R.id.iv_share,
            R.id.tv_tag_price,
            R.id.tv_settled_profit_loss_key)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            if (CpClickUtil.isFastDoubleClick()) return@setOnItemChildClickListener
            val clickData = adapter.data[position] as CpContractPositionBean
            when (view.id) {
                R.id.tv_reverse_opem -> {
                    mReverseOpenDialog = CpDialogUtil.showReverseOpeningDialog(this.activity!!) {
                        subscribe =
                            Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                .subscribe { it1 ->

                                    for (i in 0 until mAllList.size) {
                                        if (mAllList[i].contractId == clickData.contractId) { //标记价格
                                            val mPricePrecision =
                                                CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, clickData.contractId)
                                            it.setText(R.id.tv_holdings_value, CpBigDecimalUtils.showSNormal(mAllList[i].indexPrice, mPricePrecision))
                                        }
                                    }
                                }
                        it.setText(R.id.tv_type,
                            if (clickData.orderSide == "BUY") getString(R.string.cp_order_text6) else getString(R.string.cp_order_text15))
                        it.setTextColor(R.id.tv_type,
                            if (clickData.orderSide == "BUY") activity?.resources?.getColor(R.color.main_green)!! else activity?.resources?.getColor(R.color.main_red)!!)
                        it.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId))
                        it.setText(R.id.tv_level_value,
                            (if (clickData.positionType == 1) getString(R.string.cp_contract_setting_text1) else getString(R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X") //标记价格
                        val mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, clickData.contractId)
                        it.setText(R.id.tv_holdings_value, CpBigDecimalUtils.showSNormal(clickData.indexPrice, mPricePrecision)) //可平
                        var num = ""
                        var unit = ""
                        val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, clickData.contractId)

                        val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(context, clickData.contractId)
                        val mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(context, clickData.contractId)
                        if (CpClLogicContractSetting.getContractUint(context) == 0) {
                            num = CpDecimalUtil.cutValueByPrecision(clickData.positionVolume, 0)
                            unit = "(" + context?.getString(R.string.cp_overview_text9) + ")"
                        } else {
                            num = CpBigDecimalUtils.mulStr(clickData.positionVolume, mMultiplier, mMultiplierPrecision)
                            unit = mMultiplierCoin
                        }
                        it.setText(R.id.tv_gains_balance_value, num + unit) //反向开仓
                        var orderType = ""
                        if (clickData.orderSide == "BUY") { //卖出做空
                            orderType = getString(R.string.cp_order_text611)
                            it.setTextColor(R.id.tv_reverse_open_value, activity?.resources?.getColor(R.color.main_red)!!)
                        } else { //买入做多
                            orderType = getString(R.string.cp_order_text151)
                            it.setTextColor(R.id.tv_reverse_open_value, activity?.resources?.getColor(R.color.main_green)!!)
                        }
                        it.setText(R.id.tv_reverse_open_value, orderType + num + unit)
                        val btn_close_position = it.getView<CpCommonlyUsedButton>(R.id.btn_close_position)
                        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                            @SuppressLint("SuspiciousIndentation")
                            override fun bottonOnClick() {
                                if (subscribe != null) {
                                    subscribe?.dispose()
                                }
                                mReverseOpenDialog?.dismiss()
                                reverseOpen(clickData)
                            }
                        }


                    }
                }
                R.id.tv_close_position -> {
                    mClosePositionDialog = CpDialogUtil.showClosePositionDialog(this.activity!!, OnBindViewListener {
                        it.setText(R.id.tv_type,
                            if (clickData.orderSide == "BUY") getString(R.string.cp_order_text6) else getString(R.string.cp_order_text15))
                        it.setTextColor(R.id.tv_type,
                            if (clickData.orderSide == "BUY") activity?.resources?.getColor(R.color.main_green)!! else activity?.resources?.getColor(R.color.main_red)!!)
                        it.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId))
                        it.setText(R.id.tv_level_value,
                            (if (clickData.positionType == 1) getString(R.string.cp_contract_setting_text1) else getString(R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
                        it.setText(R.id.tv_price_unit, CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId))
                        val volumeUnit =
                            if (CpClLogicContractSetting.getContractUint(context) == 0) getString(R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(
                                activity,
                                clickData.contractId)
                        it.setText(R.id.tv_volume_unit, volumeUnit)

                        if (CpClLogicContractSetting.getContractUint(context) == 0) {
                            it.setText(R.id.et_volume, clickData.canCloseVolume)
                        } else {
                            it.setText(R.id.et_volume,
                                CpBigDecimalUtils.mulStr(clickData.canCloseVolume,
                                    CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
                                    CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)))
                        }
                        var showPrice = CpBigDecimalUtils.showSNormal(clickData.indexPrice.toString(),
                            CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId))
                        it.setText(R.id.et_price, showPrice)
                        var checkedIdBuff = 0
                        val rg_trade = it.getView<RadioGroup>(R.id.rg_trade)
                        val rb_1st = it.getView<RadioButton>(R.id.rb_1st)
                        val rb_2nd = it.getView<RadioButton>(R.id.rb_2nd)
                        val rb_3rd = it.getView<RadioButton>(R.id.rb_3rd)
                        val rb_4th = it.getView<RadioButton>(R.id.rb_4th)
                        val etPrice = it.getView<EditText>(R.id.et_price)
                        val etVolume = it.getView<EditText>(R.id.et_volume)
                        val llPrice = it.getView<LinearLayout>(R.id.ll_price)
                        val llVolume = it.getView<LinearLayout>(R.id.ll_volume)
                        val tvOrderTips = it.getView<SuperTextView>(R.id.tv_order_tips_layout)
                        val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)
                        etVolume.numberFilter(multiplierPrecision, otherFilter = object : CpDoListener {
                            override fun doThing(obj: Any?): Boolean {
                                return true
                            }
                        })

                        etPrice?.setOnFocusChangeListener { _, hasFocus ->
                            llPrice?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
                        }
                        tvOrderTips.setText(getString(R.string.cp_overview_text53))
                        tvOrderTips.visibility = View.VISIBLE
                        llPrice.visibility = View.GONE
                        checkedIdBuff = -1
                        etPrice.setText(showPrice)
                        tvOrderTips.visibility = View.GONE
                        llPrice.visibility = View.VISIBLE

                        rg_trade?.setOnCheckedChangeListener { group, checkedId ->
                            if (checkedId == -1) {
                                it.setText(R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedIdBuff == checkedId) {
                                it.setText(R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedId > -1) {
                                if (!CpClLogicContractSetting.isLogin()) {
                                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                                    return@setOnCheckedChangeListener
                                }
                            }
                            etVolume.clearFocus()
                            checkedIdBuff = checkedId
                            val scale = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) 0 else multiplierPrecision
                            val mCanCloseVolumeStr = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) {
                                clickData?.canCloseVolume.toString()
                            } else {
                                CpBigDecimalUtils.mulStr(clickData?.canCloseVolume,
                                    CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
                                    multiplierPrecision)
                            }
                            when (checkedId) {
                                R.id.rb_1st -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.10", scale))
                                }
                                R.id.rb_2nd -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.20", scale))
                                }
                                R.id.rb_3rd -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.50", scale))
                                }
                                R.id.rb_4th -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "1", scale))
                                }
                                else -> {
                                    it.setText(R.id.et_volume, "")
                                }
                            }
                        }
                        etVolume?.setOnFocusChangeListener { _, hasFocus ->
                            CpSoftKeyboardUtil.showORhideSoftKeyboard(activity)
                            if (hasFocus) {
                                rg_trade.clearCheck()
                                tvOrderTips.visibility = View.GONE
                                llPrice.visibility = View.VISIBLE
                            }
                            llVolume?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
                        }
                        etPrice.setOnClickListener {
                            it.setFocusable(true);
                            it.setFocusableInTouchMode(true);
                            it.requestFocus();
                            it.findFocus();
                        }

                        val btn_close_position = it.getView<CpCommonlyUsedButton>(R.id.btn_close_position)
                        btn_close_position.isEnable(true)
                        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                var priceStr = etPrice.text.toString().trim()
                                var volStr = etVolume.text.toString().trim()
                                val multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId)
                                volStr = CpBigDecimalUtils.getOrderNum(false, volStr, multiplier, 1)
                                var type = 2
                                val priceType = ""
                                type = 2
                                priceStr = ""
                                showPrice = getString(R.string.cp_overview_text53)

                                if (rb_1st.isChecked) {
                                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.10", 0)
                                }
                                if (rb_2nd.isChecked) {
                                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.20", 0)
                                }
                                if (rb_3rd.isChecked) {
                                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.50", 0)
                                }
                                if (rb_4th.isChecked) {
                                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "1", 0)
                                }
                                if (type == 1 && TextUtils.isEmpty(priceType)) {
                                    if (TextUtils.isEmpty(priceStr)) {
                                        mClosePositionDialog?.dismiss()
                                        CpNToastUtil.showTopToastNet(activity, false, getString(R.string.cp_extra_text33))
                                        return
                                    }
                                }
                                if (TextUtils.isEmpty(volStr)) {
                                    mClosePositionDialog?.dismiss()
                                    CpNToastUtil.showTopToastNet(activity, false, getString(R.string.cp_extra_text34))
                                    return
                                }
                                var dialogTitle = ""
                                val titleColor = if (clickData.orderSide == "BUY") {
                                    dialogTitle += context?.getString(R.string.cp_extra_text5)
                                    resources.getColor(R.color.main_red)
                                } else {
                                    dialogTitle += context?.getString(R.string.cp_extra_text4)
                                    resources.getColor(R.color.main_green)
                                }
                                val showTag = when (clickData.positionType) {
                                    1 -> {
                                        getString(R.string.cp_contract_setting_text1) + " " + clickData.leverageLevel.toString() + "X"
                                    }
                                    2 -> {
                                        getString(R.string.cp_contract_setting_text2) + " " + clickData.leverageLevel.toString() + "X"
                                    }
                                    else -> {
                                        ""
                                    }
                                }
                                context?.let { it1 ->
                                    CpDialogUtil.showCloseOrderDialog(it1,
                                        titleColor,
                                        dialogTitle,
                                        CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId),
                                        showPrice,
                                        "",
                                        "",
                                        etVolume.text.toString() + " " + volumeUnit,
                                        type,
                                        "",
                                        "",
                                        showTag,
                                        object : CpNewDialogUtils.DialogBottomListener {
                                            override fun sendConfirm() {
                                                closePosition(clickData, type, priceType, priceStr, volStr)
                                            }
                                        })
                                } //                                closePosition(clickData, type, priceType, priceStr, volStr)
                                mClosePositionDialog?.dismiss()
                            }
                        }
                    })
                }
                R.id.tv_adjust_margins -> {
                    mAdjustMarginDialog = CpDialogUtil.showAdjustMarginDialog(this.activity!!, OnBindViewListener {
                        val llVolume = it.getView<LinearLayout>(R.id.ll_volume)
                        val tvAdd = it.getView<TextView>(R.id.tv_title)
                        val tvSub = it.getView<TextView>(R.id.tv_title_sub)
                        val tvCanuseKey = it.getView<TextView>(R.id.tv_canuse_key)
                        val tvCanuseValue = it.getView<TextView>(R.id.tv_canuse_value)
                        val tvTips = it.getView<TextView>(R.id.tv_tips)
                        val tv_expect_price = it.getView<TextView>(R.id.tv_expect_price)
                        val tv_lever = it.getView<TextView>(R.id.tv_lever)
                        val tv_position_margin_value = it.getView<TextView>(R.id.tv_position_margin_value)
                        val imgTransfer = it.getView<ImageView>(R.id.img_transfer)
                        val etVolume = it.getView<EditText>(R.id.et_volume)
                        val rg_trade = it.getView<RadioGroup>(R.id.rg_trade)
                        val marginCoin = CpClLogicContractSetting.getContractMarginCoinById(activity, clickData.contractId)
                        val marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(activity, clickData.contractId)
                        val currentPositionMargin = clickData?.holdAmount.toString()
                        it.setText(R.id.tv_coin_name, marginCoin)
                        var canUseAmountStr = ""
                        var canSubAmountStr = clickData.canSubMarginAmount
                        mPositionObj?.apply {
                            if (!isNull("accountList")) {
                                val mOrderListJson = optJSONArray("accountList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    val obj = mOrderListJson.getJSONObject(i)
                                    var canUseAmount = obj.getString("canUseAmount")
                                    val csymbol = obj?.optString("symbol")
                                    if (marginCoin.equals(csymbol)) {
                                        canUseAmountStr = canUseAmount
                                    }
                                }
                            }
                        }

                        etVolume?.setOnFocusChangeListener { _, hasFocus ->
                            llVolume?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
                        }

                        val canUseAmountShowStr = CpBigDecimalUtils.showSNormal(canUseAmountStr, marginCoinPrecision)
                        val canSubMarginAmountShowStr = CpBigDecimalUtils.showSNormal(clickData.canSubMarginAmount, marginCoinPrecision)
                        tvCanuseValue.setText(canUseAmountShowStr + " " + marginCoin)
                        var isAdd = true
                        tvAdd.setOnClickListener {
                            tvAdd.setTextAppearance(activity, R.style.item_adjust_margin_dialog_title_check)
                            tvSub.setTextAppearance(activity, R.style.item_adjust_margin_dialog_title_no_check)
                            tvCanuseKey.setText(getString(R.string.cp_order_text22))
                            tvCanuseValue.setText(canUseAmountShowStr + " " + marginCoin)
                            tvTips.setText(getString(R.string.cp_order_text24))
                            imgTransfer.visibility = View.VISIBLE
                            isAdd = true
                            etVolume.setText("")
                            rg_trade.clearCheck()
                        }
                        tvSub.setOnClickListener {
                            tvSub.setTextAppearance(activity, R.style.item_adjust_margin_dialog_title_check)
                            tvAdd.setTextAppearance(activity, R.style.item_adjust_margin_dialog_title_no_check)
                            tvCanuseKey.setText(getString(R.string.cp_order_text23))
                            tvCanuseValue.setText(canSubMarginAmountShowStr + " " + marginCoin)
                            tvTips.setText(getString(R.string.cp_order_text25))
                            imgTransfer.visibility = View.GONE
                            isAdd = false
                            etVolume.setText("")
                            rg_trade.clearCheck()
                        }
                        imgTransfer.setOnClickListener {
                            val mMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_go_fundsTransfer_page)
                            mMessageEvent.msg_content = CpClLogicContractSetting.getContractMarginCoinById(activity, clickData.contractId)
                            CpEventBusUtil.post(mMessageEvent)
                        }

                        val volumeDecimal = CpClLogicContractSetting.getContractMarginCoinPrecisionById(activity, clickData.contractId)
                        var checkedIdBuff = 0
                        rg_trade?.setOnCheckedChangeListener { group, checkedId ->
                            if (checkedId == -1) {
                                it.setText(R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedIdBuff == checkedId) {
                                it.setText(R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedId > -1) {
                                if (!CpClLogicContractSetting.isLogin()) {
                                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                                    return@setOnCheckedChangeListener
                                }
                                etVolume.isFocusable = false
                            }
                            var buff = ""
                            if (isAdd) {
                                buff = canUseAmountStr
                            } else {
                                buff = canSubAmountStr
                            }
                            when (checkedId) {
                                R.id.rb_1st -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.10", volumeDecimal))
                                }
                                R.id.rb_2nd -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.20", volumeDecimal))
                                }
                                R.id.rb_3rd -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.50", volumeDecimal))
                                }
                                R.id.rb_4th -> {
                                    it.setText(R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "1", volumeDecimal))
                                }
                                else -> {
                                    it.setText(R.id.et_volume, "")
                                }
                            }
                        }
                        val btn_close_position = it.getView<CpCommonlyUsedButton>(R.id.btn_close_position)

                        etVolume.setOnClickListener {
                            it.setFocusable(true);
                            it.setFocusableInTouchMode(true);
                            it.requestFocus();
                            it.findFocus();
                            rg_trade.clearCheck()
                        }
                        var amount = ""

                        etVolume.numberFilter(volumeDecimal, otherFilter = object : CpDoListener {
                            override fun doThing(obj: Any?): Boolean {
                                amount = etVolume.text.toString()
                                if (isAdd) {
                                    amount = CpBigDecimalUtils.addStr(currentPositionMargin, amount, marginCoinPrecision)
                                } else {
                                    amount = CpBigDecimalUtils.subStr(currentPositionMargin, amount, marginCoinPrecision)
                                }
                                tv_position_margin_value.setText(amount + " " + marginCoin)
                                if (TextUtils.isEmpty(amount) || TextUtils.equals(amount, ".") || CpBigDecimalUtils.compareTo(amount, "0") == 0) {
                                    tv_lever.text = "--"
                                    tv_expect_price.text = "--"
                                    return true
                                } //保证金汇率:
                                var marginRate = CpClLogicContractSetting.getContractMarginRateById(activity, clickData.contractId)

                                var mPricePrecision =
                                    CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId) //面值:
                                var multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId) //标记价格
                                var indexPrice = clickData?.indexPrice //合约方向:(反向：0，正向 : 1)
                                var contractSide = CpClLogicContractSetting.getContractSideById(activity, clickData.contractId).toString() //已实现盈亏
                                val realizedAmount = clickData?.realizedAmount //未实现盈亏
                                val unRealizedAmount = clickData?.unRealizedAmount //逐仓权益
                                var positionEquity = CpBigDecimalUtils.calcPositionEquity(amount, realizedAmount, unRealizedAmount, 3) //仓位方向
                                var positionDirection = if (clickData?.orderSide.equals("BUY")) "1" else "-1" //维持保证金率
                                var keepRate = clickData?.keepRate //手续费率
                                var maxFeeRate = clickData?.maxFeeRate //仓位数量
                                var positionVolume = CpBigDecimalUtils.mulStr(clickData?.positionVolume, multiplier, 4)


                                //仓位方向：多仓是1，空仓是-1
                                var reducePriceStr = CpBigDecimalUtils.calcForcedPrice(contractSide.equals("1"),
                                    positionEquity,
                                    marginRate,
                                    positionVolume,
                                    positionDirection,
                                    indexPrice,
                                    keepRate,
                                    maxFeeRate,
                                    mPricePrecision)
                                if (CpBigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
                                    reducePriceStr = "--"
                                }
                                tv_expect_price.setText(reducePriceStr)

                                /**
                                实际杠杆（正向合约） = 仓位数量 * 标记价格 / 调整后仓位保证金 / 保证金汇率
                                实际杠杆（反向合约） = 仓位数量 / 标记价格 / 调整后仓位保证金 / 保证金汇率
                                 */
                                var adjustingLever = "0X"
                                adjustingLever = if (contractSide.equals("1")) { //正向
                                    val buff1 = CpBigDecimalUtils.mul(positionVolume, indexPrice) //仓位数量 * 标记价格
                                    val buff2 = CpBigDecimalUtils.div(amount, marginRate) //调整后仓位保证金 / 保证金汇率
                                    CpBigDecimalUtils.div(buff1, buff2, 1)
                                } else {
                                    val buff1 = CpBigDecimalUtils.div(positionVolume, indexPrice) //仓位数量 / 标记价格
                                    val buff2 = CpBigDecimalUtils.div(amount, marginRate) //调整后仓位保证金 / 保证金汇率
                                    CpBigDecimalUtils.div(buff1, buff2, 1)
                                }
                                if (CpBigDecimalUtils.compareTo(adjustingLever, "0") != 1) {
                                    adjustingLever = "--"
                                }
                                tv_lever.setText(adjustingLever + " " + "X")

                                btn_close_position.isEnable(CpBigDecimalUtils.compareTo(etVolume.text.toString(), "0") == 1)
                                return true
                            }
                        })
                        etVolume.setText("")
                        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                var type = "1"
                                if (CpBigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) { // 增加保证金
                                    type = "1"
                                    amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                } else { // 减少保证金
                                    type = "2"
                                    amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                }
                                addDisposable(getContractModel().modifyPositionMargin(clickData?.contractId.toString(),
                                    clickData?.id.toString(),
                                    type.toString(),
                                    amount,
                                    consumer = object : CpNDisposableObserver(mActivity, true) {
                                        override fun onResponseSuccess(jsonObject: JSONObject) {
                                            CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_modify_position_margin_event))
                                        }
                                    }))
                                mAdjustMarginDialog?.dismiss()
                            }
                        }
                    })
                }
                R.id.tv_profit_loss -> {
                    CpContractStopRateLossActivity.show(this.activity!!, clickData)
                }
                R.id.iv_share -> {
                    CpNewDialogUtils.showShareDialog(context!!, clickData, object : CpNewDialogUtils.DialogShareClickListener {
                        override fun clickItem(bitmap: Bitmap) {
                            doShare(bitmap)
                        }
                    })
                }
                R.id.tv_tag_price -> {
                    CpNewDialogUtils.showDialogNew(context!!,
                        getString(R.string.cp_extra_text129),
                        true,
                        null,
                        getLineText("cl_margin_rate_str"),
                        getLineText("cp_extra_text28"))
                }
                R.id.tv_forced_close_price_key -> {
                    CpNewDialogUtils.showDialogNew(context!!,
                        getString(R.string.cp_extra_text130),
                        true,
                        null,
                        getLineText("cp_calculator_text4"),
                        getLineText("cp_extra_text28"))
                }
                R.id.tv_settled_profit_loss_key -> {
                    val obj: JSONObject = JSONObject()
                    obj.put("profitRealizedAmount", clickData.profitRealizedAmount)
                    obj.put("tradeFee", clickData.tradeFee)
                    obj.put("capitalFee", clickData.capitalFee)
                    obj.put("closeProfit", clickData.closeProfit)
                    obj.put("shareAmount", clickData.shareAmount)
                    obj.put("settleProfit", clickData.settleProfit)
                    obj.put("marginCoin", CpClLogicContractSetting.getContractMarginCoinById(context, clickData.contractId))
                    obj.put("marginCoinPrecision", CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, clickData.contractId))
                    CpSlDialogHelper.showProfitLossDetailsDialog(context!!, obj, 0)
                }
            }
        }
    }


    //更新是否显示全部的是UI
    private fun showSwitch() {
        showAll = CpPreferenceManager.getInt(activity!!, CpPreferenceManager.isShowAllContract, 1)
        updateAdapter()

    }

    private fun initOnClick() { //选择

        rb_select_all.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                CpPreferenceManager.putInt(activity!!, CpPreferenceManager.isShowAllContract, 0)
            } else {
                CpPreferenceManager.putInt(activity!!, CpPreferenceManager.isShowAllContract, 1)
            }
            showSwitch()
        }

        //一键平仓
        tv_confirm_btn.setOnClickListener {
            CpDialogUtil.showNewDoubleDialog(context!!,
                context!!.getString(R.string.cp_extra_text_hold3),
                object : CpDialogUtil.DialogBottomListener {
                    override fun sendConfirm() {

                        if (mList.isEmpty()) {
                            CpNToastUtil.showTopToastNet(activity, false, context?.getString(R.string.cp_tip_text711))
                            return
                        }
                        cancelOrderAll(true)
                        saveContractIdList?.clear()
                        for (i in 0 until mList.size) {
                            val clickData = mList[i]
                             if (clickData.canCloseVolume==clickData.positionVolume){
                                 closePosition(clickData, 2, "", "", clickData.canCloseVolume)
                             }else{
                                 saveContractIdList?.add(clickData.contractId.toString())
                             }


                        }


                    }

                }

            )


        }
    }

    //更新列表
    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        if (mAllList.isEmpty()) {
            mList.clear()
            adapter?.setList(null)
            adapter?.notifyDataSetChanged()
            return
        }
        when (showAll) {
            0 -> {
                mList = mAllList
                rb_select_all.isChecked = true
            }
            else -> {
                rb_select_all.isChecked = false
                mList.clear()
                for (i in 0 until mAllList.size) {
                    if (mAllList[i].contractId == mContractId) {
                        mList.add(mAllList[i])
                    }
                }
            }
        }
        adapter?.setList(mList)
        if (saveContractId.isNotEmpty()){
            for (i in 0 until mList.size) {
                if (mList[i].contractId.toString() == saveContractId&&mList[i].positionVolume==mList[i].canCloseVolume) {
                    reverseOpen(mList[i])
                }
            }

        }
        if (!saveContractIdList.isNullOrEmpty()){
            for (i in 0 until mList.size) {
                for (j in 0 until saveContractIdList!!.size) {
                    if (mList[i].contractId.toString() == saveContractIdList!![j]&&mList[i].positionVolume==mList[i].canCloseVolume) {
                        closeOpen(mList[i])
                    }
                }

            }

        }
    }

    var  saveContractIdList :MutableList<String>?= mutableListOf()
    private fun closeOpen(clickData:CpContractPositionBean) {
        val obj = CpCreateOrderBean(
            contractId = clickData.contractId,
            positionType = clickData.positionType.toString(),
            open = "CLOSE",
            side = if (clickData.orderSide == "BUY") "SELL" else "BUY",
            leverageLevel = clickData.leverageLevel,
            price = "0",
            volume = clickData.canCloseVolume,
            type = 2,
            isConditionOrder = false,
            triggerPrice = "",
            isOto = false,
            takerProfitTrigger = "",
            stopLossTrigger = "",
            expireTime = CpClLogicContractSetting.getStrategyEffectTimeStr(context),
        )
        addDisposable(getContractModel().createOrder(obj,
            consumer = object : CpNDisposableObserver(mActivity, true) {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    if (!saveContractIdList.isNullOrEmpty()){
                        saveContractIdList!!.remove(clickData.contractId.toString())
                    }

                }
            }))
    }


      var  saveContractId=""
    private fun reverseOpen(clickData:CpContractPositionBean) {
        var mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId)
        val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, clickData.contractId)
        val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(context, clickData.contractId)
        val obj = CpCreateOrderBean(
            contractId = clickData.contractId,
            positionType = clickData.positionType.toString(),
            open = "CLOSE",
            side = if (clickData.orderSide == "BUY") "SELL" else "BUY",
            leverageLevel = clickData.leverageLevel,
            price = "0",
            volume = clickData.canCloseVolume,
            type = 2,
            isConditionOrder = false,
            triggerPrice = "",
            isOto = false,
            takerProfitTrigger = "",
            stopLossTrigger = "",
            expireTime = CpClLogicContractSetting.getStrategyEffectTimeStr(context),
        )
             //总持仓等于可平仓位)
             if (clickData.positionVolume==clickData.canCloseVolume){
                 obj.open="CLOSE"
                 obj.volume= clickData.canCloseVolume
                 addDisposable(getContractModel().createOrder(obj,
                     consumer = object : CpNDisposableObserver(mActivity, true) {
                         override fun onResponseSuccess(jsonObject: JSONObject) {
                             val num1 = CpBigDecimalUtils.mulStr( clickData.positionVolume, mMultiplier, mMultiplierPrecision)
                             val num3 = clickData.indexPrice
                             obj.open="OPEN"
                             obj.volume= CpBigDecimalUtils.mulStr(num1, num3, mPricePrecision)
                             addDisposable(getContractModel().createOrder(obj,
                                 consumer = object : CpNDisposableObserver(mActivity, true) {
                                     override fun onResponseSuccess(jsonObject: JSONObject) {
                                         CpNToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.cp_extra_text53))
                                         saveContractId=""
                                     }
                                 }))

                         }
                     }))

             }else{
                 saveContractId=clickData.contractId.toString()
                 cancelOrder(clickData.contractId.toString(),true)
             }

    }


    //单个
    private fun cancelOrder(contractId: String, isisConditionOrder: Boolean) {
        addDisposable(getContractModel().orderCancelAll("[${contractId}]",
            isisConditionOrder,
            consumer = object : CpNDisposableObserver(activity, false) {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    if (isisConditionOrder) {
                        cancelOrder(contractId,false)
                    }
                }
            }))

    }

    //全部撤销
    private fun cancelOrderAll(isConditionOrder: Boolean) {
        if (mList.isEmpty()) {
            return
        }
        var ids = "["
        for (i in 0 until mList.size) {
            if (i == 0) {
                ids += mList[i].contractId.toString()
            } else if (!ids.contains(mList[i].contractId.toString())) {
                ids = ids + "," + mList[i].contractId.toString()
            }
        }
        ids += "]"


        addDisposable(getContractModel().orderCancelAll(ids, isConditionOrder, consumer = object : CpNDisposableObserver(activity, false) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (isConditionOrder) {
                    cancelOrderAll(false)
                }
            }
        }))
    }

    private fun closePosition(data: CpContractPositionBean, type: Int, priceType: String, price: String, vol: String) {
        val contractId = data.contractId
        val positionType = data.positionType.toString()
        val open = "CLOSE"
        val side = if (data.orderSide.equals("BUY")) "SELL" else "BUY"
        val type = type
        val leverageLevel = data.leverageLevel
        val price = price
        val volume = vol
        val isConditionOrder = false
        val triggerPrice = ""
        val expireTime = CpClLogicContractSetting.getStrategyEffectTimeStr(mActivity)
        addDisposable(getContractModel().createOrder(contractId,
            positionType,
            open,
            side,
            type,
            leverageLevel,
            price,
            volume,
            isConditionOrder,
            triggerPrice,
            expireTime,
            false,
            "",
            "",
            priceType,
            consumer = object : CpNDisposableObserver(mActivity, true) {
                @SuppressLint("CheckResult")
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    CpNToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.cp_extra_text109))
                    Observable.timer(500, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_req_position_list_event))
                    }
                }
            }))
    }


    @SuppressLint("CheckResult")
    private fun doShare(shareBitmap: Bitmap) {
        val rxPermissions = activity?.let { RxPermissions(it) }
        rxPermissions?.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)?.subscribe { granted ->
            if (granted) {
                if (shareBitmap != null) {
                    CpShareToolUtil.sendLocalShare(mActivity, shareBitmap)
                } else {
                    CpDisplayUtils.showSnackBar(activity?.window?.decorView, getString(R.string.cp_extra_text128), false)
                }
            } else {
                CpDisplayUtils.showSnackBar(activity?.window?.decorView, getString(R.string.cp_extra_text128), false)
            }

        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_refresh_position_list_event -> {
                LogUtils.e("我是合约更新111111")
                mPositionObj = event.msg_content as JSONObject
                var lidt = ArrayList<CpContractPositionBean>()
                mPositionObj?.apply {
                    if (!isNull("positionList")) {
                        val mOrderListJson = optJSONArray("positionList")
                        if (mOrderListJson != null) {
                            for (i in 0 until mOrderListJson.length()) {
                                val obj = mOrderListJson.getString(i)
                                lidt.add(Gson().fromJson(obj, CpContractPositionBean::class.java))
                            }
                        }
                        mAllList = lidt

                    }
                }
                updateAdapter()
            }
            CpMessageEvent.sl_contract_logout_event -> {
                mAllList.clear()
                updateAdapter()
            }
            CpMessageEvent.sl_contract_clear_event -> {
                mAllList.clear()
                updateAdapter()
            }

            //合约id有更新要重新筛选数组列表
            CpMessageEvent.sl_contract_calc_switch_contract_id -> {
                val id = event.msg_content as Int
                if (mContractId != id) {
                    mContractId = id
                    updateAdapter()
                }

            } //合约配置
            CpMessageEvent.sl_contract_config -> {
                val jsonObject = event.msg_content as JSONObject
                jsonObject.optJSONObject("data").run {
                    positionModel = optInt("positionModel") //持仓类型 1持仓, 2双向持仓
                }

            }
        }
    }


}