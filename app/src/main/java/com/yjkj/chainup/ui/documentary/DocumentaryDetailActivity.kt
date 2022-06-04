package com.yjkj.chainup.ui.documentary

import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.utils.*
import com.chainup.contract.view.CpCommonlyUsedButton
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpNewDialogUtils
import com.coorchice.library.SuperTextView
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryDetailBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.ui.documentary.vm.DocumentaryDetailViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit


@Route(path = RoutePath.DocumentaryDetailActivity)
class DocumentaryDetailActivity : BaseMVActivity<DocumentaryDetailViewModel?, ActivityDocumentaryDetailBinding?>(){
    @Autowired(name = "bean")
    @JvmField
    var item : CpContractPositionBean?=null
    @Autowired(name = "type")
    @JvmField
    var type : Int?=null
    @Autowired(name = "status")
    @JvmField
    var status : Int?=null

    override fun setContentView() = R.layout.activity_documentary_detail
    override fun initData() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.bean?.value=item
        mViewModel?.type?.value=type
        mViewModel?.status?.value=status
        mViewModel?.getData()
       mBinding?.tvClose?.setOnClickListener {
           showClosePositionDialog(item!!)
       }
    }




    /**
     * 弹出平仓对话框
     */
    private var mClosePositionDialog: TDialog? = null
    private fun showClosePositionDialog(clickData: CpContractPositionBean) {
        mClosePositionDialog = CpDialogUtil.showClosePositionDialog(this, OnBindViewListener { it ->
            it.setText(
                com.chainup.contract.R.id.tv_type, if (clickData.orderSide == "BUY") getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                    com.chainup.contract.R.string.cp_order_text15))
            it.setTextColor(
                com.chainup.contract.R.id.tv_type, if (clickData.orderSide == "BUY") resources?.getColor(
                    com.chainup.contract.R.color.main_green)!! else resources?.getColor(com.chainup.contract.R.color.main_red)!!)
            it.setText(com.chainup.contract.R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(this, clickData.contractId))
            it.setText(
                com.chainup.contract.R.id.tv_level_value, (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                    com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
            it.setText(com.chainup.contract.R.id.tv_price_unit, CpClLogicContractSetting.getContractQuoteById(this, clickData.contractId))
            val volumeUnit = if (CpClLogicContractSetting.getContractUint(this) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(this, clickData.contractId)
            it.setText(com.chainup.contract.R.id.tv_volume_unit, volumeUnit)

            if (CpClLogicContractSetting.getContractUint(this) == 0) {
                it.setText(com.chainup.contract.R.id.et_volume, clickData.canCloseVolume)
            } else {
                it.setText(
                    com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(
                        clickData.canCloseVolume,
                        CpClLogicContractSetting.getContractMultiplierById(this, clickData.contractId),
                        CpClLogicContractSetting.getContractMultiplierPrecisionById(this, clickData.contractId)
                    ))
            }
            var showPrice = CpBigDecimalUtils.showSNormal(clickData.indexPrice.toString(), CpClLogicContractSetting.getContractSymbolPricePrecisionById(this, clickData.contractId))
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
            val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(this, clickData.contractId)
            etVolume.numberFilter(if (CpClLogicContractSetting.getContractUint(this) == 0) 0 else multiplierPrecision, otherFilter = object :
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
                        CpClLogicContractSetting.getContractMultiplierById(this, clickData.contractId),
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
                CpSoftKeyboardUtil.showORhideSoftKeyboard(this)
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
                    val multiplier = CpClLogicContractSetting.getContractMultiplierById(mContext, clickData.contractId)
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
                            showPrice = priceStr + " " + CpClLogicContractSetting.getContractQuoteById(mContext, clickData.contractId)
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
                            CpNToastUtil.showTopToastNet(mActivity, false, getString(com.chainup.contract.R.string.cp_extra_text33))
                            return
                        }
                    }
                    if (TextUtils.isEmpty(volStr)) {
                        mClosePositionDialog?.dismiss()
                        CpNToastUtil.showTopToastNet(mActivity, false, getString(com.chainup.contract.R.string.cp_extra_text34))
                        return
                    }
                    var dialogTitle = ""
//                                if (type==1){
//                                    dialogTitle= context?.getString(R.string.contract_action_limitPrice).toString()
//                                }else{
//                                    dialogTitle= context?.getString(R.string.cp_overview_text53).toString()
//                                }
                    val titleColor = if (clickData.orderSide.equals("BUY")) {
                        dialogTitle += getString(com.chainup.contract.R.string.cp_extra_text5)
                        resources.getColor(com.chainup.contract.R.color.main_red)
                    } else {
                        dialogTitle += getString(com.chainup.contract.R.string.cp_extra_text4)
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
                        CpDialogUtil.showCloseOrderDialog(mActivity,
                            titleColor,
                            dialogTitle,
                            CpClLogicContractSetting.getContractShowNameById(mContext, clickData.contractId),
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
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(com.chainup.contract.R.string.cp_extra_text109))
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_req_position_list_event))
                            }
                    }
                })
        )
    }

}