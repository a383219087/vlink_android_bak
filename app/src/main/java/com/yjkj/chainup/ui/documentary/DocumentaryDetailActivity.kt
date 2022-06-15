package com.yjkj.chainup.ui.documentary

import android.Manifest
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.chainup.talkingdata.AppAnalyticsExt
import com.coorchice.library.SuperTextView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryDetailBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.ClContractHistoricalPositionAdapter
import com.yjkj.chainup.ui.documentary.vm.DocumentaryDetailViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_documentary_detail.*
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
    @Autowired(name = "json")
    @JvmField
    var jsonStr : String?=null


    private var adapter: CpHoldContractNewAdapter? = null
    private var adapter1: ClContractHistoricalPositionAdapter? = null
    private var mList = ArrayList<CpContractPositionBean>()
    private var mList1 = ArrayList<JSONObject>()
    var mAdjustMarginDialog: TDialog? = null
    var mQuickClosePositionDialog: TDialog? = null
    var mClosePositionDialog: TDialog? = null
    override fun setContentView() = R.layout.activity_documentary_detail
    override fun initData() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.type?.value=type
        mViewModel?.status?.value=status
        mViewModel?.getData()
        if (status==1){
            mViewModel?.bean?.value=item
            item?.let { mList.add(it) }
            adapter = CpHoldContractNewAdapter(mList)
            adapter!!.setMySelf(true)
            adapter!!.setMyTrader(true)
            rv_hold_contract.layoutManager = CpMyLinearLayoutManager(mContext)
            rv_hold_contract.adapter = adapter
            adapter?.addChildClickViewIds(com.chainup.contract.R.id.tv_quick_close_position, com.chainup.contract.R.id.tv_close_position, com.chainup.contract.R.id.tv_forced_close_price_key, com.chainup.contract.R.id.tv_adjust_margins, com.chainup.contract.R.id.tv_profit_loss, com.chainup.contract.R.id.iv_share, com.chainup.contract.R.id.tv_tag_price, com.chainup.contract.R.id.tv_settled_profit_loss_key)
            adapter?.setOnItemChildClickListener { adapter, view, position ->
                if (CpClickUtil.isFastDoubleClick()) return@setOnItemChildClickListener
                val clickData = adapter.data[position] as CpContractPositionBean
                when (view.id) {
                    com.chainup.contract.R.id.tv_close_position -> {
                        AppAnalyticsExt.instance.clickAction(AppAnalyticsExt.CONTRACT_APP_ACTION_11)
                        mClosePositionDialog = CpDialogUtil.showClosePositionDialog(mActivity, OnBindViewListener {
                            it.setText(
                                com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                                    com.chainup.contract.R.string.cp_order_text15))
                            it.setTextColor(
                                com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) mActivity?.resources?.getColor(
                                    com.chainup.contract.R.color.main_green)!! else mActivity?.resources?.getColor(com.chainup.contract.R.color.main_red)!!)
                            it.setText(com.chainup.contract.R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(mActivity, clickData.contractId))
                            it.setText(
                                com.chainup.contract.R.id.tv_level_value, (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                                    com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
                            it.setText(com.chainup.contract.R.id.tv_price_unit, CpClLogicContractSetting.getContractQuoteById(mActivity, clickData.contractId))
                            val volumeUnit = if (CpClLogicContractSetting.getContractUint(mActivity) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(mActivity, clickData.contractId)
                            it.setText(com.chainup.contract.R.id.tv_volume_unit, volumeUnit)

                            if (CpClLogicContractSetting.getContractUint(mActivity) == 0) {
                                it.setText(com.chainup.contract.R.id.et_volume, clickData.canCloseVolume)
                            } else {
                                it.setText(
                                    com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStr(
                                        clickData.canCloseVolume,
                                        CpClLogicContractSetting.getContractMultiplierById(mActivity, clickData.contractId),
                                        CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, clickData.contractId)
                                    ))
                            }
                            var showPrice = CpBigDecimalUtils.showSNormal(clickData.indexPrice.toString(), CpClLogicContractSetting.getContractSymbolPricePrecisionById(mActivity, clickData.contractId))
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
                            val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, clickData.contractId)
                            etVolume.numberFilter(if (CpClLogicContractSetting.getContractUint(mActivity) == 0) 0 else multiplierPrecision, otherFilter = object : CpDoListener {
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
                                            AppAnalyticsExt.instance.clickAction(AppAnalyticsExt.CONTRACT_APP_ACTION_12)
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
                                            AppAnalyticsExt.instance.clickAction(AppAnalyticsExt.CONTRACT_APP_ACTION_14)
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
                                            AppAnalyticsExt.instance.clickAction(AppAnalyticsExt.CONTRACT_APP_ACTION_13)
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
                                    CpBigDecimalUtils.mulStr(clickData?.canCloseVolume, CpClLogicContractSetting.getContractMultiplierById(mActivity, clickData.contractId), multiplierPrecision)
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
                                CpSoftKeyboardUtil.showORhideSoftKeyboard(mActivity)
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
                                    val multiplier = CpClLogicContractSetting.getContractMultiplierById(mActivity, clickData.contractId)
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
                                            showPrice = priceStr + " " + CpClLogicContractSetting.getContractQuoteById(mActivity, clickData.contractId)
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
                                        dialogTitle = dialogTitle + mActivity?.getString(com.chainup.contract.R.string.cp_extra_text5)
                                        resources.getColor(com.chainup.contract.R.color.main_red)
                                    } else {
                                        dialogTitle = dialogTitle + mActivity?.getString(com.chainup.contract.R.string.cp_extra_text4)
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
                                    mActivity?.let { it1 ->
                                        CpDialogUtil.showCloseOrderDialog(it1,
                                            titleColor,
                                            dialogTitle,
                                            CpClLogicContractSetting.getContractShowNameById(mActivity, clickData.contractId),
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
                    com.chainup.contract.R.id.tv_quick_close_position -> {
                        mQuickClosePositionDialog = CpDialogUtil.showQuickClosePositionDialog(this.mActivity!!, OnBindViewListener {
                            it.setText(
                                com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) getString(com.chainup.contract.R.string.cp_order_text6) else getString(
                                    com.chainup.contract.R.string.cp_order_text15))
                            it.setTextColor(
                                com.chainup.contract.R.id.tv_type, if (clickData.orderSide.equals("BUY")) mActivity?.resources?.getColor(
                                    com.chainup.contract.R.color.main_green)!! else mActivity?.resources?.getColor(com.chainup.contract.R.color.main_red)!!)
                            it.setText(com.chainup.contract.R.id.tv_contract_name, CpClLogicContractSetting.getContractShowNameById(mActivity, clickData.contractId))
                            it.setText(
                                com.chainup.contract.R.id.tv_level_value, (if (clickData.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                                    com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData.leverageLevel + "X")
                            val unit = if (CpClLogicContractSetting.getContractUint(mActivity) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(mActivity, clickData.contractId)
                            if (CpClLogicContractSetting.getContractUint(mActivity) == 0) {
                                it.setText(com.chainup.contract.R.id.tv_position_amount, getString(com.chainup.contract.R.string.cp_order_text50) + "：" + clickData.positionVolume + unit)
                            } else {
                                it.setText(
                                    com.chainup.contract.R.id.tv_position_amount, getString(com.chainup.contract.R.string.cp_order_text50) + "：" + CpBigDecimalUtils.mulStr(
                                        clickData.positionVolume,
                                        CpClLogicContractSetting.getContractMultiplierById(mActivity, clickData.contractId),
                                        CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, clickData.contractId)
                                    ) + unit)
                            }
                            val btn_close_position = it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)
                            btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
                                override fun bottonOnClick() {
                                    quickClosePosition(clickData.contractId.toString(), "CLOSE", clickData.orderSide, clickData.positionType.toString())
                                    mQuickClosePositionDialog?.dismiss()
                                }
                            }
                        })
                    }
                    com.chainup.contract.R.id.tv_adjust_margins -> {
                        mAdjustMarginDialog = CpDialogUtil.showAdjustMarginDialog(this.mActivity!!, OnBindViewListener {
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
                            val rb_1st = it.getView<RadioButton>(com.chainup.contract.R.id.rb_1st)
                            val rb_2nd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_2nd)
                            val rb_3rd = it.getView<RadioButton>(com.chainup.contract.R.id.rb_3rd)
                            val rb_4th = it.getView<RadioButton>(com.chainup.contract.R.id.rb_4th)
                            val marginCoin = CpClLogicContractSetting.getContractMarginCoinById(mActivity, clickData.contractId)
                            val marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(mActivity, clickData.contractId)
                            val currentPositionMargin = clickData?.holdAmount.toString()
                            it.setText(com.chainup.contract.R.id.tv_coin_name, marginCoin)
                            var canUseAmountStr = ""
                            var canSubAmountStr = clickData.canSubMarginAmount
                            etVolume?.setOnFocusChangeListener { _, hasFocus ->
                                llVolume?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
                            }

                            val canUseAmountShowStr = CpBigDecimalUtils.showSNormal(canUseAmountStr, marginCoinPrecision)
                            val canSubMarginAmountShowStr = CpBigDecimalUtils.showSNormal(clickData.canSubMarginAmount, marginCoinPrecision)
                            tvCanuseValue.setText(canUseAmountShowStr + " " + marginCoin)
                            var isAdd = true
                            tvAdd.setOnClickListener {
                                tvAdd.setTextAppearance(mActivity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_check)
                                tvSub.setTextAppearance(mActivity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_no_check)
                                tvCanuseKey.setText(getString(com.chainup.contract.R.string.cp_order_text22))
                                tvCanuseValue.setText(canUseAmountShowStr + " " + marginCoin)
                                tvTips.setText(getString(com.chainup.contract.R.string.cp_order_text24))
                                imgTransfer.visibility = View.VISIBLE
                                isAdd = true
                                etVolume.setText("")
                                rg_trade.clearCheck()
                            }
                            tvSub.setOnClickListener {
                                tvSub.setTextAppearance(mActivity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_check)
                                tvAdd.setTextAppearance(mActivity, com.chainup.contract.R.style.item_adjust_margin_dialog_title_no_check)
                                tvCanuseKey.setText(getString(com.chainup.contract.R.string.cp_order_text23))
                                tvCanuseValue.setText(canSubMarginAmountShowStr + " " + marginCoin)
                                tvTips.setText(getString(com.chainup.contract.R.string.cp_order_text25))
                                imgTransfer.visibility = View.GONE
                                isAdd = false
                                etVolume.setText("")
                                rg_trade.clearCheck()
                            }
                            imgTransfer.setOnClickListener {
                                val mMessageEvent =
                                    CpMessageEvent(CpMessageEvent.sl_contract_go_fundsTransfer_page)
                                mMessageEvent.msg_content = CpClLogicContractSetting.getContractMarginCoinById(mActivity, clickData.contractId)
                                CpEventBusUtil.post(mMessageEvent)
                            }

                            val volumeDecimal = CpClLogicContractSetting.getContractMarginCoinPrecisionById(mActivity, clickData.contractId)
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
                                if (isAdd) {
                                    buff = canUseAmountStr
                                } else {
                                    buff = canSubAmountStr
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
                                    }
                                    //保证金汇率:
                                    var marginRate = CpClLogicContractSetting.getContractMarginRateById(mActivity, clickData.contractId)

                                    var mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(mActivity, clickData.contractId)
                                    //面值:
                                    var multiplier = CpClLogicContractSetting.getContractMultiplierById(mActivity, clickData.contractId)
                                    //标记价格
                                    var indexPrice = clickData?.indexPrice
                                    //合约方向:(反向：0，正向 : 1)
                                    var contractSide = CpClLogicContractSetting.getContractSideById(mActivity, clickData.contractId).toString()
                                    //已实现盈亏
                                    val realizedAmount = clickData?.realizedAmount
                                    //未实现盈亏
                                    val unRealizedAmount = clickData?.unRealizedAmount
                                    //逐仓权益
                                    var positionEquity = CpBigDecimalUtils.calcPositionEquity(amount, realizedAmount, unRealizedAmount, 3)
                                    //仓位方向
                                    var positionDirection = if (clickData?.orderSide.equals("BUY")) "1" else "-1"
                                    //维持保证金率
                                    var keepRate = clickData?.keepRate
                                    //手续费率
                                    var maxFeeRate = clickData?.maxFeeRate
                                    //仓位数量
                                    var positionVolume = CpBigDecimalUtils.mulStr(clickData?.positionVolume, multiplier, 4)
//                                var positionVolume = clickData?.positionVolume
                                    ChainUpLogUtil.e(TAG, positionVolume)
                                    ChainUpLogUtil.e(TAG, clickData?.positionVolume)
                                    ChainUpLogUtil.e(TAG, multiplier)

                                    //仓位方向：多仓是1，空仓是-1
                                    var reducePriceStr = CpBigDecimalUtils.calcForcedPrice(contractSide.equals("1"), positionEquity, marginRate, positionVolume, positionDirection, indexPrice, keepRate, maxFeeRate, mPricePrecision)
                                    if (CpBigDecimalUtils.compareTo(reducePriceStr, "0") != 1) {
                                        reducePriceStr = "--"
                                    }
                                    tv_expect_price.setText(reducePriceStr)

                                    /**
                                    实际杠杆（正向合约） = 仓位数量 * 标记价格 / 调整后仓位保证金 / 保证金汇率
                                    实际杠杆（反向合约） = 仓位数量 / 标记价格 / 调整后仓位保证金 / 保证金汇率
                                     */
                                    var adjustingLever = "0X"
                                    adjustingLever = if (contractSide.equals("1")) {
                                        //正向
                                        val buff1 = CpBigDecimalUtils.mul(positionVolume, indexPrice)//仓位数量 * 标记价格
                                        val buff2 = CpBigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
                                        CpBigDecimalUtils.div(buff1, buff2, 1)
                                    } else {
                                        val buff1 = CpBigDecimalUtils.div(positionVolume, indexPrice)//仓位数量 / 标记价格
                                        val buff2 = CpBigDecimalUtils.div(amount, marginRate)//调整后仓位保证金 / 保证金汇率
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
                                    if (CpBigDecimalUtils.compareTo(amount, currentPositionMargin) == 1) {
                                        // 增加保证金
                                        type = "1"
                                        amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                    } else {
                                        // 减少保证金
                                        type = "2"
                                        amount = CpBigDecimalUtils.subStr(amount, currentPositionMargin, marginCoinPrecision)
                                    }
                                    addDisposable(getContractModel().modifyPositionMargin(clickData?.contractId.toString(), clickData?.id.toString(), type.toString(), amount,
                                        consumer = object : CpNDisposableObserver(mActivity, true) {
                                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                                CpEventBusUtil.post(
                                                    CpMessageEvent(
                                                        CpMessageEvent.sl_contract_modify_position_margin_event
                                                    )
                                                )
                                            }
                                        }))
                                    mAdjustMarginDialog?.dismiss()
                                }
                            }
                        })
                    }
                    com.chainup.contract.R.id.tv_profit_loss -> {
                        CpContractStopRateLossActivity.show(this.mActivity!!, clickData)
                    }
                    com.chainup.contract.R.id.iv_share -> {
                        AppAnalyticsExt.instance.clickAction(AppAnalyticsExt.CONTRACT_APP_ACTION_15)
                        CpNewDialogUtils.showShareDialog(
                            mActivity!!,
                            clickData,
                            object : CpNewDialogUtils.DialogShareClickListener {
                                override fun clickItem(bitmap: Bitmap) {
                                    doShare(bitmap)
                                }
                            })
                    }
                    com.chainup.contract.R.id.tv_tag_price -> {
                        CpNewDialogUtils.showDialogNew(
                            mActivity!!,
                            getString(com.chainup.contract.R.string.cp_extra_text129),
                            true,
                            null,
                            getLineText("cl_margin_rate_str"),
                            getLineText("cp_extra_text28")
                        )
                    }
                    com.chainup.contract.R.id.tv_forced_close_price_key -> {
                        CpNewDialogUtils.showDialogNew(
                            mActivity!!,
                            getString(com.chainup.contract.R.string.cp_extra_text130),
                            true,
                            null,
                            getLineText("cp_calculator_text4"),
                            getLineText("cp_extra_text28")
                        )
                    }
                    com.chainup.contract.R.id.tv_settled_profit_loss_key -> {
                        val obj: JSONObject = JSONObject()
                        obj.put("profitRealizedAmount", clickData.profitRealizedAmount)
                        obj.put("tradeFee", clickData.tradeFee)
                        obj.put("capitalFee", clickData.capitalFee)
                        obj.put("closeProfit", clickData.closeProfit)
                        obj.put("shareAmount", clickData.shareAmount)
                        obj.put("settleProfit", clickData.settleProfit)
                        obj.put("marginCoin", CpClLogicContractSetting.getContractMarginCoinById(mActivity, clickData.contractId))
                        obj.put("marginCoinPrecision", CpClLogicContractSetting.getContractMarginCoinPrecisionById(mActivity, clickData.contractId))
                        CpSlDialogHelper.showProfitLossDetailsDialog(mActivity!!, obj, 0)
                    }
                }
            }
        }else{
            val obj = JSONObject(jsonStr)
            mList1.add(obj)
            adapter1 = ClContractHistoricalPositionAdapter(mContext!!,mList1)
            rv_hold_contract.layoutManager = CpMyLinearLayoutManager(mActivity)
            rv_hold_contract.adapter = adapter
        }

    }


    private fun quickClosePosition(contractId: String, open: String, side: String, positionType: String) {
        var side = if (side.equals("BUY")) "SELL" else "BUY"
        addDisposable(
            getContractModel().lightClose(contractId, open, side, positionType,
                consumer = object : CpNDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(com.chainup.contract.R.string.cp_extra_text109))
                        LogUtils.e("quickClosePosition :success")
                    }
                })
        )
    }

    private fun doShare(shareBitmap: Bitmap) {
        val rxPermissions =RxPermissions(mActivity)
        rxPermissions?.request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )?.subscribe { granted ->
                if (granted) {
                    if (shareBitmap != null) {
                        CpShareToolUtil.sendLocalShare(mActivity, shareBitmap)
                    } else {
                        CpDisplayUtils.showSnackBar(
                            mActivity?.window?.decorView,
                            getString(com.chainup.contract.R.string.cp_extra_text128),
                            false
                        )
                    }
                } else {
                    CpDisplayUtils.showSnackBar(
                        mActivity?.window?.decorView,
                        getString(com.chainup.contract.R.string.cp_extra_text128),
                        false
                    )
                }

            }
    }

    /**
     * 弹出平仓对话框
     */
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