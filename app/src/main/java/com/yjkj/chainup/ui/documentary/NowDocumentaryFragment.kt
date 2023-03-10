package com.yjkj.chainup.ui.documentary

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.bean.CpCreateOrderBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.model.CpNewContractModel
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.coorchice.library.SuperTextView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.ui.documentary.vm.NowDocumentViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_now_documentary.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class NowDocumentaryFragment : BaseMVFragment<NowDocumentViewModel?, FragmentNowDocumentaryBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(status: Int,uid:String): NowDocumentaryFragment {
            val fg = NowDocumentaryFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            bundle.putString(ParamConstant.MARKET_NAME, uid)
            fg.arguments = bundle
            return fg
        }

    }
    private var adapter: CpHoldContractNewAdapter? = null
    private var mList = ArrayList<CpContractPositionBean>()
    var mAdjustMarginDialog: TDialog? = null
    var mQuickClosePositionDialog: TDialog? = null
    var mClosePositionDialog: TDialog? = null
    var mReverseOpenDialog: TDialog? = null

    var subscribe: Disposable? = null


    override fun setContentView(): Int = R.layout.fragment_now_documentary
    @SuppressLint("SetTextI18n")
    override fun initView() {
        subscribe = Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {it1->
                getList()
            }
        mViewModel?.activity?.value=mActivity
        mViewModel?.status?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.uid?.value=arguments?.getString(ParamConstant.MARKET_NAME)


        adapter = CpHoldContractNewAdapter(mList)
        adapter!!.setMySelf( mViewModel?.uid?.value.isNullOrEmpty())
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.addChildClickViewIds(
            com.chainup.contract.R.id.tv_close_position,
            com.chainup.contract.R.id.tv_reverse_opem,
            com.chainup.contract.R.id.tv_forced_close_price_key,
            com.chainup.contract.R.id.tv_adjust_margins,
            com.chainup.contract.R.id.tv_profit_loss,
            com.chainup.contract.R.id.iv_share,
            com.chainup.contract.R.id.tv_tag_price,
            com.chainup.contract.R.id.tv_settled_profit_loss_key)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            if (CpClickUtil.isFastDoubleClick()) return@setOnItemChildClickListener
            val clickData = adapter.data[position] as CpContractPositionBean
            when (view.id) {
                com.chainup.contract.R.id.tv_reverse_opem-> {
                    mReverseOpenDialog = CpDialogUtil.showReverseOpeningDialog(this.activity!!) {
                        it.setText(
                            com.chainup.contract.R.id.tv_type,
                            if (clickData.orderSide == "BUY") getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                                com.chainup.contract.R.string.cp_order_text15
                            )
                        )
                        it.setTextColor(
                            com.chainup.contract.R.id.tv_type,
                            if (clickData.orderSide == "BUY") activity?.resources?.getColor(com.chainup.contract.R.color.main_green)!! else activity?.resources?.getColor(
                                com.chainup.contract.R.color.main_red
                            )!!
                        )
                        it.setText(
                            com.chainup.contract.R.id.tv_contract_name,
                            CpClLogicContractSetting.getContractShowNameById(
                                context,
                                clickData.contractId
                            )
                        )
                        it.setText(
                            com.chainup.contract.R.id.tv_level_value,
                            (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                                com.chainup.contract.R.string.cp_contract_setting_text2
                            )) + " " + clickData.leverageLevel + "X"
                        )
                        //????????????
                        val mPricePrecision =
                            CpClLogicContractSetting.getContractSymbolPricePrecisionById(
                                context,
                                clickData.contractId
                            )
                        it.setText(
                            com.chainup.contract.R.id.tv_holdings_value,
                            CpBigDecimalUtils.showSNormal(clickData.indexPrice, mPricePrecision)
                        )
                        //??????
                        var num = ""
                        var unit = ""
                        val mMultiplierPrecision =
                            CpClLogicContractSetting.getContractMultiplierPrecisionById(
                                context,
                                clickData.contractId
                            )
                        val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(
                            context,
                            clickData.contractId
                        )
                        val mMultiplierCoin =
                            CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(
                                context,
                                clickData.contractId
                            )
                        if (CpClLogicContractSetting.getContractUint(context) == 0) {
                            num = CpDecimalUtil.cutValueByPrecision(clickData.canCloseVolume, 0)
                            unit = "(" + context?.getString(com.chainup.contract.R.string.cp_overview_text9) + ")"
                        } else {
                            num = CpBigDecimalUtils.mulStr(
                                clickData.canCloseVolume,
                                mMultiplier,
                                mMultiplierPrecision
                            )
                            unit = mMultiplierCoin
                        }
                        it.setText(com.chainup.contract.R.id.tv_gains_balance_value, num + unit)
                        //????????????
                        var orderType = ""
                        if (clickData.orderSide == "BUY") {
                            //????????????
                            orderType = getString(com.chainup.contract.R.string.cp_order_text611)
                            it.setTextColor(
                                com.chainup.contract.R.id.tv_reverse_open_value,
                                activity?.resources?.getColor(com.chainup.contract.R.color.main_red)!!
                            )
                        } else {
                            //????????????
                            orderType = getString(com.chainup.contract.R.string.cp_order_text151)
                            it.setTextColor(
                                com.chainup.contract.R.id.tv_reverse_open_value,
                                activity?.resources?.getColor(com.chainup.contract.R.color.main_green)!!
                            )
                        }
                        it.setText(com.chainup.contract.R.id.tv_reverse_open_value, orderType + num + unit)
                        val btn_close_position =
                            it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)
                        btn_close_position.listener =
                            object : CpCommonlyUsedButton.OnBottonListener {
                                override fun bottonOnClick() {

                                    LogUtils.e("??????????????????1${clickData.toString()}")
                                    val side = if (clickData.orderSide == "BUY") "SELL" else "BUY"
                                    val obj = CpCreateOrderBean(
                                        contractId = clickData.contractId,
                                        positionType = clickData.positionType.toString(),
                                        open = "OPEN",
                                        side = side,
                                        leverageLevel = clickData.leverageLevel,
                                        price = "0",
                                        volume = CpBigDecimalUtils.getOrderNum(true, clickData.canCloseVolume, mMultiplier, 2),
                                        type = 2,
                                        isConditionOrder = false,
                                        triggerPrice = "",
                                        isOto = false,
                                        takerProfitTrigger = "",
                                        stopLossTrigger = "",
                                        expireTime = CpClLogicContractSetting.getStrategyEffectTimeStr(context),
                                    )
                                    //????????????
                                    addDisposable(
                                        CpNewContractModel().lightClose(clickData.contractId.toString(),
                                            "CLOSE",
                                            side,
                                            clickData.positionType.toString(),
                                            consumer = object : CpNDisposableObserver(true) {
                                                override fun onResponseSuccess(jsonObject: JSONObject) {
                                                    mReverseOpenDialog?.dismiss()
                                                    ///?????????????????????????????????????????????
                                                    val mContractList = JSONArray(CpClLogicContractSetting.getContractJsonListStr(context))
                                                    var objj:JSONObject?=null
                                                    if (clickData.contractId == -1 && mContractList.length() != 0) {
                                                        objj = (mContractList[0] as JSONObject)
                                                    } else {
                                                        for (i in 0 until mContractList.length()) {
                                                            val o: JSONObject = mContractList.get(i) as JSONObject
                                                            if (clickData.contractId == o.optInt("id")) {
                                                                objj = o
                                                            }
                                                        }
                                                    }
                                                    //??????????????????
                                                    val coinResultVo = objj?.optString("coinResultVo").let { JSONObject(it) }
                                                    val minOrderVolume = coinResultVo.optString("minOrderVolume")//???????????????
                                                    val minOrderMoney = coinResultVo.optString("minOrderMoney")//??????????????????
                                                    val maxMarketVolume = coinResultVo.optString("maxMarketVolume")//???????????????????????????
                                                    val maxMarketMoney = coinResultVo.optString("maxMarketMoney")//????????????????????????
                                                    val maxLimitVolume = coinResultVo.optString("maxLimitVolume")//???????????????????????????

                                                    //??????????????????  < x < ????????????????????????
                                                    if (CpBigDecimalUtils.orderMoneyMinCheck(
                                                            clickData.canCloseVolume,
                                                            minOrderMoney,
                                                            mMultiplier
                                                        )
                                                    ) {
                                                        CpNToastUtil.showTopToastNet(
                                                            activity,
                                                            false,
                                                            context?.getString(com.chainup.contract.R.string.cp_tip_text7) + minOrderMoney + objj?.optString(
                                                                "quote"
                                                            )
                                                        )
                                                        return
                                                    }
                                                    if (CpBigDecimalUtils.orderMoneyMaxCheck(
                                                            clickData.canCloseVolume,
                                                            maxMarketMoney,
                                                            mMultiplier
                                                        )
                                                    ) {
                                                        CpNToastUtil.showTopToastNet(
                                                            activity,
                                                            false,
                                                            context?.getString(com.chainup.contract.R.string.cp_tip_text8) + maxMarketMoney + objj?.optString(
                                                                "quote"
                                                            )
                                                        )
                                                        return
                                                    }

                                                    //????????????
                                                    addDisposable(
                                                        CpNewContractModel().createOrder(obj,
                                                            consumer = object :
                                                                CpNDisposableObserver(
                                                                    mActivity,
                                                                    true
                                                                ) {
                                                                override fun onResponseSuccess(
                                                                    jsonObject: JSONObject
                                                                ) {
                                                                    CpNToastUtil.showTopToastNet(
                                                                        this.mActivity,
                                                                        true,
                                                                        getString(com.chainup.contract.R.string.cp_extra_text53)
                                                                    )

                                                                }
                                                            })
                                                    )
                                                }
                                            })
                                    )

                                }
                            }


                    }
                }
                com.chainup.contract.R.id.tv_close_position -> {
                    mClosePositionDialog = CpDialogUtil.showClosePositionDialog(this.activity!!, OnBindViewListener {
                        it.setText(
                            com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                                com.chainup.contract.R.string.cp_order_text15))
                        it.setTextColor(
                            com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) activity?.resources?.getColor(
                                com.chainup.contract.R.color.main_green)!! else activity?.resources?.getColor(com.chainup.contract.R.color.main_red)!!)
                        it.setText(com.chainup.contract.R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId))
                        it.setText(
                            com.chainup.contract.R.id.tv_level_value, (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                                com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
                        it.setText(com.chainup.contract.R.id.tv_price_unit, CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId))
                        val volumeUnit = if (CpClLogicContractSetting.getContractUint(context) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(activity, clickData.contractId)
                        it.setText(com.chainup.contract.R.id.tv_volume_unit, volumeUnit)

                        if (CpClLogicContractSetting.getContractUint(context) == 0) {
                            it.setText(com.chainup.contract.R.id.et_volume, clickData.canCloseVolume)
                        } else {
                            it.setText(
                                com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(
                                clickData.canCloseVolume,
                                CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
                                CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)
                            ))
                        }
                        var showPrice = CpBigDecimalUtils.showSNormal(clickData.indexPrice.toString(), CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId))
                        it.setText(com.chainup.contract.R.id.et_price, showPrice)
                        var checkedIdBuff = 0
                        val rg_order_type = it.getView<RadioGroup>(com.chainup.contract.R.id.rg_order_type)
                        val rg_trade = it.getView<RadioGroup>(com.chainup.contract.R.id.rg_trade)
                        val rb_1 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_1)
                        val rb_2 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_2)
                        val rb_3 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_3)
                        val rb_1st = it.getView<RadioButton>(com.chainup.contract.R.id.rb_1st)
                        val rb_2nd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_2nd)
                        val rb_3rd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_3rd)
                        val rb_4th = it.getView<RadioButton>(com.chainup.contract.R.id.rb_4th)
                        val etPrice = it.getView<EditText>(com.chainup.contract.R.id.et_price)
                        val etVolume = it.getView<EditText>(com.chainup.contract.R.id.et_volume)
                        val llPrice = it.getView<LinearLayout>(com.chainup.contract.R.id.ll_price)
                        val llVolume = it.getView<LinearLayout>(com.chainup.contract.R.id.ll_volume)
                        val tvOrderTips = it.getView<SuperTextView>(com.chainup.contract.R.id.tv_order_tips_layout)
                        val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)
                        etVolume.numberFilter(if (CpClLogicContractSetting.getContractUint(context) == 0) 0 else multiplierPrecision, otherFilter = object : CpDoListener {
                            override fun doThing(obj: Any?): Boolean {
                                return true
                            }
                        })

                        etPrice?.setOnFocusChangeListener { _, hasFocus ->
                            llPrice?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
                        }

                        for (buff in 0 until rg_order_type?.childCount!!) {
                            rg_order_type.getChildAt(buff).setOnClickListener {
                                when (it.id) {
                                    com.chainup.contract.R.id.rb_1 -> {
                                        tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_overview_text53))
                                        tvOrderTips.visibility = View.VISIBLE
                                        llPrice.visibility = View.GONE
                                        if (checkedIdBuff == it.id) {
                                            checkedIdBuff = -1
                                            etPrice.setText(showPrice)
                                            rg_order_type.clearCheck()
                                            tvOrderTips.visibility = View.GONE
                                            llPrice.visibility = View.VISIBLE
                                        } else {
                                            checkedIdBuff = it.id
                                            rg_order_type.check(it.id)
                                        }
                                    }
                                    com.chainup.contract.R.id.rb_2 -> {
                                        tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text44))
                                        tvOrderTips.visibility = View.VISIBLE
                                        llPrice.visibility = View.GONE
                                        if (checkedIdBuff == it.id) {
                                            checkedIdBuff = -1
                                            etPrice.setText(showPrice)
                                            rg_order_type.clearCheck()
                                            tvOrderTips.visibility = View.GONE
                                            llPrice.visibility = View.VISIBLE
                                        } else {
                                            checkedIdBuff = it.id
                                            rg_order_type.check(it.id)
                                        }
                                    }
                                    com.chainup.contract.R.id.rb_3 -> {
                                        tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text45))
                                        tvOrderTips.visibility = View.VISIBLE
                                        llPrice.visibility = View.GONE
                                        if (checkedIdBuff == it.id) {
                                            checkedIdBuff = -1
                                            etPrice.setText(showPrice)
                                            rg_order_type.clearCheck()
                                            tvOrderTips.visibility = View.GONE
                                            llPrice.visibility = View.VISIBLE
                                        } else {
                                            checkedIdBuff = it.id
                                            rg_order_type.check(it.id)
                                        }
                                    }
                                }
                            }
                        }
                        rg_trade?.setOnCheckedChangeListener { group, checkedId ->
                            if (checkedId == -1) {
                                it.setText(com.chainup.contract.R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedIdBuff == checkedId) {
                                it.setText(com.chainup.contract.R.id.et_volume, "")
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
                            LogUtils.e("checkedId" + checkedId)

                            val scale = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) 0 else multiplierPrecision
                            val mCanCloseVolumeStr = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) {
                                clickData?.canCloseVolume.toString()
                            } else {
                                CpBigDecimalUtils.mulStr(clickData?.canCloseVolume, CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId), multiplierPrecision)
                            }
                            when (checkedId) {
                                com.chainup.contract.R.id.rb_1st -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.10", scale))
                                }
                                com.chainup.contract.R.id.rb_2nd -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.20", scale))
                                }
                                com.chainup.contract.R.id.rb_3rd -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.50", scale))
                                }
                                com.chainup.contract.R.id.rb_4th -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "1", scale))
                                }
                                else -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, "")
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
                            llVolume?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
                        }
                        etPrice.setOnClickListener {
                            it.setFocusable(true);
                            it.setFocusableInTouchMode(true);
                            it.requestFocus();
                            it.findFocus();
                            rg_order_type.clearCheck()
                        }
                        val btn_close_position = it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)
                        btn_close_position.isEnable(true)
                        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                var priceStr = etPrice.text.toString().trim()
                                var volStr = etVolume.text.toString().trim()
                                val multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId)
                                volStr = CpBigDecimalUtils.getOrderNum(false, volStr, multiplier, 1)
                                var type = 1
                                var priceType = ""
                                when {
                                    rb_1.isChecked -> {
                                        type = 2
                                        priceStr = ""
                                        showPrice = getString(com.chainup.contract.R.string.cp_overview_text53)
                                    }
                                    rb_2.isChecked -> {
                                        priceType = "1"
                                        priceStr = "0"
                                        showPrice = getString(com.chainup.contract.R.string.cp_order_text44)
                                    }
                                    rb_3.isChecked -> {
                                        priceType = "0"
                                        priceStr = "0"
                                        showPrice = getString(com.chainup.contract.R.string.cp_order_text45)
                                    }
                                    else -> {
                                        showPrice = priceStr + " " + CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId)
                                    }
                                }
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
                                        CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text33))
                                        return
                                    }
                                }
                                if (TextUtils.isEmpty(volStr)) {
                                    mClosePositionDialog?.dismiss()
                                    CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text34))
                                    return
                                }
                                var dialogTitle = ""
//                                if (type==1){
//                                    dialogTitle= context?.getString(R.string.contract_action_limitPrice).toString()
//                                }else{
//                                    dialogTitle= context?.getString(R.string.cp_overview_text53).toString()
//                                }
                                val titleColor = if (clickData.orderSide.equals("BUY")) {
                                    dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text5)
                                    resources.getColor(com.chainup.contract.R.color.main_red)
                                } else {
                                    dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text4)
                                    resources.getColor(com.chainup.contract.R.color.main_green)
                                }
                                val showTag = when (clickData.positionType) {
                                    1 -> {
                                        getString(com.chainup.contract.R.string.cp_contract_setting_text1) + " " + clickData.leverageLevel.toString() + "X"
                                    }
                                    2 -> {
                                        getString(com.chainup.contract.R.string.cp_contract_setting_text2) + " " + clickData.leverageLevel.toString() + "X"
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
                                }
//                                closePosition(clickData, type, priceType, priceStr, volStr)
                                mClosePositionDialog?.dismiss()
                            }
                        }
                    })
                }
                com.chainup.contract.R.id.tv_adjust_margins -> {
                    mAdjustMarginDialog = CpDialogUtil.showAdjustMarginDialog(this.activity!!, OnBindViewListener {
                        val llVolume = it.getView<LinearLayout>(com.chainup.contract.R.id.ll_volume)
                        val tvAdd = it.getView<TextView>(com.chainup.contract.R.id.tv_title)
                        val tvSub = it.getView<TextView>(com.chainup.contract.R.id.tv_title_sub)
                        val tvCanuseKey = it.getView<TextView>(com.chainup.contract.R.id.tv_canuse_key)
                        val tvCanuseValue = it.getView<TextView>(com.chainup.contract.R.id.tv_canuse_value)
                        val tvTips = it.getView<TextView>(com.chainup.contract.R.id.tv_tips)
                        val tv_expect_price = it.getView<TextView>(com.chainup.contract.R.id.tv_expect_price)
                        val tv_lever = it.getView<TextView>(com.chainup.contract.R.id.tv_lever)
                        val tv_position_margin_value = it.getView<TextView>(com.chainup.contract.R.id.tv_position_margin_value)
                        val imgTransfer = it.getView<ImageView>(com.chainup.contract.R.id.img_transfer)
                        val etVolume = it.getView<EditText>(com.chainup.contract.R.id.et_volume)
                        val rg_trade = it.getView<RadioGroup>(com.chainup.contract.R.id.rg_trade)
                        val marginCoin = CpClLogicContractSetting.getContractMarginCoinById(activity, clickData.contractId)
                        val marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(activity, clickData.contractId)
                        val currentPositionMargin = clickData?.holdAmount.toString()
                        it.setText(com.chainup.contract.R.id.tv_coin_name, marginCoin)
                        val canUseAmountStr = clickData.canUseAmount
                        val canSubAmountStr = clickData.canSubMarginAmount
                        etVolume?.setOnFocusChangeListener { _, hasFocus ->
                            llVolume?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
                        }

                        var canUseAmountShowStr = CpBigDecimalUtils.showSNormal(canUseAmountStr, marginCoinPrecision)
                        val canSubMarginAmountShowStr = CpBigDecimalUtils.showSNormal(clickData.canSubMarginAmount, marginCoinPrecision)
                        if (canUseAmountShowStr.isNullOrEmpty()){
                            canUseAmountShowStr="0.00"
                        }
                        tvCanuseValue.text = "$canUseAmountShowStr $marginCoin"
                        var isAdd = true
                        tvAdd.setOnClickListener {
                            tvAdd.setTextAppearance(activity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_check)
                            tvSub.setTextAppearance(activity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_no_check)
                            tvCanuseKey.text = getString(com.chainup.contract.R.string.cp_order_text22)
                            tvCanuseValue.text = "$canUseAmountShowStr $marginCoin"
                            tvTips.text = getString(com.chainup.contract.R.string.cp_order_text24)
                            imgTransfer.visibility = View.VISIBLE
                            isAdd = true
                            etVolume.setText("")
                            rg_trade.clearCheck()
                        }
                        tvSub.setOnClickListener {
                            tvSub.setTextAppearance(activity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_check)
                            tvAdd.setTextAppearance(activity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_no_check)
                            tvCanuseKey.text = getString(com.chainup.contract.R.string.cp_order_text23)
                            tvCanuseValue.text = "$canSubMarginAmountShowStr $marginCoin"
                            tvTips.text = getString(com.chainup.contract.R.string.cp_order_text25)
                            imgTransfer.visibility = View.GONE
                            isAdd = false
                            etVolume.setText("")
                            rg_trade.clearCheck()
                        }
                        imgTransfer.setOnClickListener {
                            val mMessageEvent =
                                CpMessageEvent(CpMessageEvent.sl_contract_go_fundsTransfer_page)
                            mMessageEvent.msg_content = CpClLogicContractSetting.getContractMarginCoinById(activity, clickData.contractId)
                            CpEventBusUtil.post(mMessageEvent)
                        }

                        val volumeDecimal = CpClLogicContractSetting.getContractMarginCoinPrecisionById(activity, clickData.contractId)
                        var checkedIdBuff = 0
                        rg_trade?.setOnCheckedChangeListener { group, checkedId ->
                            if (checkedId == -1) {
                                it.setText(com.chainup.contract.R.id.et_volume, "")
                                return@setOnCheckedChangeListener
                            }
                            if (checkedIdBuff == checkedId) {
                                it.setText(com.chainup.contract.R.id.et_volume, "")
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
                            buff = if (isAdd) {
                                canUseAmountStr
                            } else {
                                canSubAmountStr
                            }
                            when (checkedId) {
                                com.chainup.contract.R.id.rb_1st -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.10", volumeDecimal))
                                }
                                com.chainup.contract.R.id.rb_2nd -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.20", volumeDecimal))
                                }
                                com.chainup.contract.R.id.rb_3rd -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "0.50", volumeDecimal))
                                }
                                com.chainup.contract.R.id.rb_4th -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(buff, "1", volumeDecimal))
                                }
                                else -> {
                                    it.setText(com.chainup.contract.R.id.et_volume, "")
                                }
                            }
                        }
                        val btn_close_position = it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)

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
                                amount = if (isAdd) {
                                    CpBigDecimalUtils.addStr(currentPositionMargin, amount, marginCoinPrecision)
                                } else {
                                    CpBigDecimalUtils.subStr(currentPositionMargin, amount, marginCoinPrecision)
                                }
                                tv_position_margin_value.text = amount + " " + marginCoin
                                if (TextUtils.isEmpty(amount) || TextUtils.equals(amount, ".") || CpBigDecimalUtils.compareTo(amount, "0") == 0) {
                                    tv_lever.text = "--"
                                    tv_expect_price.text = "--"
                                    return true
                                }
                                //???????????????:
                                var marginRate = CpClLogicContractSetting.getContractMarginRateById(activity, clickData.contractId)

                                var mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId)
                                //??????:
                                var multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId)
                                //????????????
                                var indexPrice = clickData?.indexPrice
                                //????????????:(?????????0????????? : 1)
                                var contractSide = CpClLogicContractSetting.getContractSideById(activity, clickData.contractId).toString()
                                //???????????????
                                val realizedAmount = clickData?.realizedAmount
                                //???????????????
                                val unRealizedAmount = clickData?.unRealizedAmount
                                //????????????
                                var positionEquity = CpBigDecimalUtils.calcPositionEquity(amount, realizedAmount, unRealizedAmount, 3)
                                //????????????
                                var positionDirection = if (clickData?.orderSide.equals("BUY")) "1" else "-1"
                                //??????????????????
                                var keepRate = clickData?.keepRate
                                //????????????
                                var maxFeeRate = clickData?.maxFeeRate
                                //????????????
                                var positionVolume = CpBigDecimalUtils.mulStr(clickData?.positionVolume, multiplier, 4)


                                //????????????????????????1????????????-1
                                var reducePriceStr = CpBigDecimalUtils.calcForcedPrice(contractSide == "1", positionEquity, marginRate, positionVolume, positionDirection, indexPrice, keepRate, maxFeeRate, mPricePrecision)
                                if (CpBigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
                                    reducePriceStr = "--"
                                }
                                tv_expect_price.setText(reducePriceStr)

                                /**
                                ?????????????????????????????? = ???????????? * ???????????? / ???????????????????????? / ???????????????
                                ?????????????????????????????? = ???????????? / ???????????? / ???????????????????????? / ???????????????
                                 */
                                var adjustingLever = "0X"
                                adjustingLever = if (contractSide == "1") {
                                    //??????
                                    val buff1 = CpBigDecimalUtils.mul(positionVolume, indexPrice)//???????????? * ????????????
                                    val buff2 = CpBigDecimalUtils.div(amount, marginRate)//???????????????????????? / ???????????????
                                    CpBigDecimalUtils.div(buff1, buff2, 1)
                                } else {
                                    val buff1 = CpBigDecimalUtils.div(positionVolume, indexPrice)//???????????? / ????????????
                                    val buff2 = CpBigDecimalUtils.div(amount, marginRate)//???????????????????????? / ???????????????
                                    CpBigDecimalUtils.div(buff1, buff2, 1)
                                }
                                if (CpBigDecimalUtils.compareTo(adjustingLever, "0") != 1) {
                                    adjustingLever = "--"
                                }
                                tv_lever.text = "$adjustingLever X"

                                btn_close_position.isEnable(CpBigDecimalUtils.compareTo(etVolume.text.toString(), "0") == 1)
                                return true
                            }
                        })
                        etVolume.setText("")
                        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                var type = "1"
                                if (CpBigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) {
                                    // ???????????????
                                    type = "1"
                                    amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                } else {
                                    // ???????????????
                                    type = "2"
                                    amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                }
                                addDisposable(getContractModel().modifyPositionMargin(
                                    clickData.contractId.toString(), clickData?.id.toString(), type, amount,
                                    consumer = object : CpNDisposableObserver(mActivity, true) {
                                        override fun onResponseSuccess(jsonObject: JSONObject) {
                                            getList()
                                        }
                                    }))
                                mAdjustMarginDialog?.dismiss()
                            }
                        }
                    })
                }
                com.chainup.contract.R.id.tv_profit_loss -> {
                    CpContractStopRateLossActivity.show(this.activity!!, clickData)
                }
                com.chainup.contract.R.id.iv_share -> {
                    CpNewDialogUtils.showShareDialog(
                        context!!,
                        clickData,
                        object : CpNewDialogUtils.DialogShareClickListener {
                            override fun clickItem(bitmap: Bitmap) {
                                doShare(bitmap)
                            }
                        })
                }
                com.chainup.contract.R.id.tv_tag_price -> {
                    CpNewDialogUtils.showDialogNew(
                        context!!,
                        getString(com.chainup.contract.R.string.cp_extra_text129),
                        true,
                        null,
                        getLineText("cl_margin_rate_str"),
                        getLineText("cp_extra_text28")
                    )
                }
                com.chainup.contract.R.id.tv_forced_close_price_key -> {
                    CpNewDialogUtils.showDialogNew(
                        context!!,
                        getString(com.chainup.contract.R.string.cp_extra_text130),
                        true,
                        null,
                        getLineText("cp_calculator_text4"),
                        getLineText("cp_extra_text28")
                    )
                }
                com.chainup.contract.R.id.tv_settled_profit_loss_key -> {
                    val obj = JSONObject()
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
        adapter?.setOnItemClickListener{adapter, view, position ->
            if (mViewModel?.uid?.value.isNullOrEmpty()){
                val item = adapter.data[position] as CpContractPositionBean
                ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity)
                    .withString("id", item.id.toString())
                    .withInt("contractId", item.contractId )
                    .navigation()
            }
        }


    }



    override fun onDestroy() {
        subscribe?.dispose()
        super.onDestroy()
    }


    @SuppressLint("NotifyDataSetChanged")
    private  fun getList() {
       if(mViewModel?.status?.value==1){
           mViewModel?.startTask(mViewModel?.contractApiService!!.findCurrentSingleList(), Consumer {
               mList.clear()
               it.data?.positionList?.let { it1 -> mList.addAll(it1) }
               adapter?.notifyDataSetChanged()
               if (it.data?.positionList.isNullOrEmpty()){
                   tv_em.visibility=View.VISIBLE
               }else{
                   tv_em.visibility=View.GONE
               }

           })
       }else{
           val map = HashMap<String, Any>()
           map["status"] =  "1"
           if ( mViewModel?.uid?.value.isNullOrEmpty()) {
               map["traderUid"] = UserDataService.getInstance().userInfo4UserId
           } else {
               map["traderUid"] =  mViewModel?.uid?.value.toString()
           }
           mViewModel?.startTask(mViewModel?.contractApiService!!.traderPositionList(map)) {
               mList.clear()
               it.data?.positionList?.let { it1 -> mList.addAll(it1) }
               adapter?.notifyDataSetChanged()
               if (it.data?.positionList.isNullOrEmpty()) {
                   tv_em.visibility = View.VISIBLE
               } else {
                   tv_em.visibility = View.GONE
               }

           }

       }


    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.NowDocumentViewModel_close -> {
                val cp: CpContractPositionBean = event.msg_content as CpContractPositionBean
                showClosePositionDialog(cp)
            }
            MessageEvent.refresh_MyInviteCodesActivity -> {
               getList()
            }
        }
    }

    private fun quickClosePosition(contractId: String, open: String, side: String, positionType: String) {
        val side = if (side == "BUY") "SELL" else "BUY"
        addDisposable(
            CpNewContractModel().lightClose(contractId, open, side, positionType,
                consumer = object : CpNDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(com.chainup.contract.R.string.cp_extra_text109))
                        LogUtils.e("quickClosePosition :success")
                        getList()
                    }
                })
        )
    }

    @SuppressLint("CheckResult")
    private fun doShare(shareBitmap: Bitmap) {
        val rxPermissions = activity?.let { RxPermissions(it) }
        rxPermissions?.request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            ?.subscribe { granted ->
                if (granted) {
                    if (shareBitmap != null) {
                        CpShareToolUtil.sendLocalShare(mActivity, shareBitmap)
                    } else {
                        CpDisplayUtils.showSnackBar(
                            activity?.window?.decorView,
                            getString(com.chainup.contract.R.string.cp_extra_text128),
                            false
                        )
                    }
                } else {
                    CpDisplayUtils.showSnackBar(
                        activity?.window?.decorView,
                        getString(com.chainup.contract.R.string.cp_extra_text128),
                        false
                    )
                }

            }
    }

    /**
     * ?????????????????????
     */

    private fun showClosePositionDialog(clickData: CpContractPositionBean) {
        mClosePositionDialog = CpDialogUtil.showClosePositionDialog(this.activity!!, OnBindViewListener { it ->
            it.setText(
                com.chainup.contract.R.id.tv_type, if (clickData.orderSide == "BUY") getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                    com.chainup.contract.R.string.cp_order_text15))
            it.setTextColor(
                com.chainup.contract.R.id.tv_type, if (clickData.orderSide == "BUY") activity?.resources?.getColor(
                    com.chainup.contract.R.color.main_green)!! else activity?.resources?.getColor(com.chainup.contract.R.color.main_red)!!)
            it.setText(R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId))
            it.setText(
                com.chainup.contract.R.id.tv_level_value, (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                    com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
            it.setText(com.chainup.contract.R.id.tv_price_unit, CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId))
            val volumeUnit = if (CpClLogicContractSetting.getContractUint(context) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(activity, clickData.contractId)
            it.setText(com.chainup.contract.R.id.tv_volume_unit, volumeUnit)

            if (CpClLogicContractSetting.getContractUint(context) == 0) {
                it.setText(com.chainup.contract.R.id.et_volume, clickData.canCloseVolume)
            } else {
                it.setText(
                    com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(
                    clickData.canCloseVolume,
                    CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
                    CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)
                ))
            }
            var showPrice = CpBigDecimalUtils.showSNormal(clickData.indexPrice.toString(), CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData.contractId))
            it.setText(com.chainup.contract.R.id.et_price, showPrice)
            var checkedIdBuff = 0
            val rg_order_type = it.getView<RadioGroup>(com.chainup.contract.R.id.rg_order_type)
            val rg_trade = it.getView<RadioGroup>(com.chainup.contract.R.id.rg_trade)
            val rb_1 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_1)
            val rb_2 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_2)
            val rb_3 = it.getView<RadioButton>(com.chainup.contract.R.id.rb_3)
            val rb_1st = it.getView<RadioButton>(com.chainup.contract.R.id.rb_1st)
            val rb_2nd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_2nd)
            val rb_3rd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_3rd)
            val rb_4th = it.getView<RadioButton>(com.chainup.contract.R.id.rb_4th)
            val etPrice = it.getView<EditText>(com.chainup.contract.R.id.et_price)
            val etVolume = it.getView<EditText>(com.chainup.contract.R.id.et_volume)
            val llPrice = it.getView<LinearLayout>(com.chainup.contract.R.id.ll_price)
            val llVolume = it.getView<LinearLayout>(com.chainup.contract.R.id.ll_volume)
            val tvOrderTips = it.getView<SuperTextView>(com.chainup.contract.R.id.tv_order_tips_layout)
            val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData.contractId)
            etVolume.numberFilter(if (CpClLogicContractSetting.getContractUint(context) == 0) 0 else multiplierPrecision, otherFilter = object :
                CpDoListener {
                override fun doThing(obj: Any?): Boolean {
                    return true
                }
            })

            etPrice?.setOnFocusChangeListener { _, hasFocus ->
                llPrice?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
            }

            for (buff in 0 until rg_order_type?.childCount!!) {
                rg_order_type.getChildAt(buff).setOnClickListener {
                    when (it.id) {
                        com.chainup.contract.R.id.rb_1 -> {
                            tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_overview_text53))
                            tvOrderTips.visibility = View.VISIBLE
                            llPrice.visibility = View.GONE
                            if (checkedIdBuff == it.id) {
                                checkedIdBuff = -1
                                etPrice.setText(showPrice)
                                rg_order_type.clearCheck()
                                tvOrderTips.visibility = View.GONE
                                llPrice.visibility = View.VISIBLE
                            } else {
                                checkedIdBuff = it.id
                                rg_order_type.check(it.id)
                            }
                        }
                        com.chainup.contract.R.id.rb_2 -> {
                            tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text44))
                            tvOrderTips.visibility = View.VISIBLE
                            llPrice.visibility = View.GONE
                            if (checkedIdBuff == it.id) {
                                checkedIdBuff = -1
                                etPrice.setText(showPrice)
                                rg_order_type.clearCheck()
                                tvOrderTips.visibility = View.GONE
                                llPrice.visibility = View.VISIBLE
                            } else {
                                checkedIdBuff = it.id
                                rg_order_type.check(it.id)
                            }
                        }
                        com.chainup.contract.R.id.rb_3 -> {
                            tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text45))
                            tvOrderTips.visibility = View.VISIBLE
                            llPrice.visibility = View.GONE
                            if (checkedIdBuff == it.id) {
                                checkedIdBuff = -1
                                etPrice.setText(showPrice)
                                rg_order_type.clearCheck()
                                tvOrderTips.visibility = View.GONE
                                llPrice.visibility = View.VISIBLE
                            } else {
                                checkedIdBuff = it.id
                                rg_order_type.check(it.id)
                            }
                        }
                    }
                }
            }
            rg_trade?.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId == -1) {
                    it.setText(com.chainup.contract.R.id.et_volume, "")
                    return@setOnCheckedChangeListener
                }
                if (checkedIdBuff == checkedId) {
                    it.setText(com.chainup.contract.R.id.et_volume, "")
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
                    clickData.canCloseVolume
                } else {
                    val mulStr = CpBigDecimalUtils.mulStr(
                        clickData?.canCloseVolume,
                        CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
                        multiplierPrecision
                    )
                    mulStr
                }
                when (checkedId) {
                    com.chainup.contract.R.id.rb_1st -> {
                        it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.10", scale))
                    }
                    com.chainup.contract.R.id.rb_2nd -> {
                        it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.20", scale))
                    }
                    com.chainup.contract.R.id.rb_3rd -> {
                        it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.50", scale))
                    }
                    com.chainup.contract.R.id.rb_4th -> {
                        it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "1", scale))
                    }
                    else -> {
                        it.setText(com.chainup.contract.R.id.et_volume, "")
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
                llVolume?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
            }
            etPrice.setOnClickListener {
                it.setFocusable(true);
                it.setFocusableInTouchMode(true);
                it.requestFocus();
                it.findFocus();
                rg_order_type.clearCheck()
            }


            val btn_close_position = it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)
            btn_close_position.isEnable(true)
            btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                override fun bottonOnClick() {
                    var priceStr = etPrice.text.toString().trim()
                    var volStr = etVolume.text.toString().trim()
                    val multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId)
                    volStr = CpBigDecimalUtils.getOrderNum(false, volStr, multiplier, 1)
                    var type = 1
                    var priceType = ""
                    when {
                        rb_1.isChecked -> {
                            type = 2
                            priceStr = ""
                            showPrice = getString(com.chainup.contract.R.string.cp_overview_text53)
                        }
                        rb_2.isChecked -> {
                            priceType = "1"
                            priceStr = "0"
                            showPrice = getString(com.chainup.contract.R.string.cp_order_text44)
                        }
                        rb_3.isChecked -> {
                            priceType = "0"
                            priceStr = "0"
                            showPrice = getString(com.chainup.contract.R.string.cp_order_text45)
                        }
                        else -> {
                            showPrice = priceStr + " " + CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId)
                        }
                    }
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
                            CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text33))
                            return
                        }
                    }
                    if (TextUtils.isEmpty(volStr)) {
                        mClosePositionDialog?.dismiss()
                        CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text34))
                        return
                    }
                    var dialogTitle = ""
//                                if (type==1){
//                                    dialogTitle= context?.getString(R.string.contract_action_limitPrice).toString()
//                                }else{
//                                    dialogTitle= context?.getString(R.string.cp_overview_text53).toString()
//                                }
                    val titleColor = if (clickData.orderSide.equals("BUY")) {
                        dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text5)
                        resources.getColor(com.chainup.contract.R.color.main_red)
                    } else {
                        dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text4)
                        resources.getColor(com.chainup.contract.R.color.main_green)
                    }
                    val showTag = when (clickData.positionType) {
                        1 -> {
                            getString(com.chainup.contract.R.string.cp_contract_setting_text1) + " " + clickData.leverageLevel.toString() + "X"
                        }
                        2 -> {
                            getString(com.chainup.contract.R.string.cp_contract_setting_text2) + " " + clickData.leverageLevel.toString() + "X"
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
                    }
//                                closePosition(clickData, type, priceType, priceStr, volStr)
                    mClosePositionDialog?.dismiss()
                }
            }
        })
    }


    private fun closePosition(data: CpContractPositionBean, type: Int, priceType: String, price: String, vol: String) {
        var contractId = data.contractId
        var positionType = data.positionType.toString()
        var open = "CLOSE"
        var side = if (data.orderSide.equals("BUY")) "SELL" else "BUY"
        var type = type
        var leverageLevel = data.leverageLevel
        var price = price
        var volume = vol
        var isConditionOrder = false
        var triggerPrice = ""

        var expireTime =
            CpClLogicContractSetting.getStrategyEffectTimeStr(mActivity)
        addDisposable(
            getContractModel().createOrder1(contractId,
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
                consumer = object :
                    CpNDisposableObserver(mActivity, true) {
                    @SuppressLint("CheckResult")
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(com.chainup.contract.R.string.cp_extra_text109))
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                getList()
                            }
                    }
                })
        )
    }

}




