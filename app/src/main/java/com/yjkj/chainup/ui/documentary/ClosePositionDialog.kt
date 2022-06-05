package com.yjkj.chainup.ui.documentary

import android.annotation.SuppressLint
import android.text.Editable
import android.view.Gravity
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.numberFilter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogClosePositionBinding
import com.yjkj.chainup.ui.documentary.vm.ClosePositionViewModel
import org.jetbrains.anko.textColor


class ClosePositionDialog : BaseDialogMVFragment<ClosePositionViewModel?, DialogClosePositionBinding>() {





     private var clickData: CpContractPositionBean?=null
    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_close_position
    @SuppressLint("SetTextI18n")
    override fun initView() {
        clickData= arguments?.getSerializable("bean") as CpContractPositionBean?

        mBinding?.tvType?.text=  if (clickData?.orderSide == "BUY") getString(R.string.cp_order_text6) else getString(R.string.cp_order_text15)
        mBinding?.tvType?.textColor= if (clickData?.orderSide == "BUY") activity?.resources?.getColor(
        R.color.main_green)!! else activity?.resources?.getColor(R.color.main_red)!!
        mBinding?.tvContractName?.text=  CpClLogicContractSetting.getContractShowNameById(context, clickData!!.contractId)

        mBinding?.tvLevelValue?.text=(if (clickData?.positionType == 1) getString(com.chainup.contract.R.string.cp_contract_setting_text1) else getString(
                com.chainup.contract.R.string.cp_contract_setting_text2)) + " " + clickData?.leverageLevel + "X"

       mBinding?.tvPriceUnit?.text=CpClLogicContractSetting.getContractQuoteById(activity, clickData!!.contractId)
        val volumeUnit = if (CpClLogicContractSetting.getContractUint(context) == 0) getString(com.chainup.contract.R.string.cp_overview_text9) else CpClLogicContractSetting.getContractMultiplierCoinById(activity, clickData!!.contractId)
        mBinding?.tvVolumeUnit?.text = volumeUnit

        if (CpClLogicContractSetting.getContractUint(context) == 0) {
            mBinding?.etVolume?.text= Editable.Factory.getInstance().newEditable(clickData!!.canCloseVolume)
        } else {
            mBinding?.etVolume?.text= Editable.Factory.getInstance().newEditable(CpBigDecimalUtils.mulStr(
                clickData!!.canCloseVolume,
                CpClLogicContractSetting.getContractMultiplierById(activity, clickData!!.contractId),
                CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData!!.contractId)
            ))
        }
        val showPrice = CpBigDecimalUtils.showSNormal(clickData!!.indexPrice, CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, clickData!!.contractId))
        mBinding?.etPrice?.text=Editable.Factory.getInstance().newEditable(showPrice)

        var checkedIdBuff = 0
        val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, clickData!!.contractId)
        mBinding?.etVolume?.numberFilter(if (CpClLogicContractSetting.getContractUint(context) == 0) 0 else multiplierPrecision, otherFilter = object :
            CpDoListener {
            override fun doThing(obj: Any?): Boolean {
                return true
            }
        })

        mBinding?.etPrice?.setOnFocusChangeListener { _, hasFocus ->
            mBinding?.llPrice?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
        }

//
//        for (buff in 0 until rg_order_type?.childCount!!) {
//            rg_order_type.getChildAt(buff).setOnClickListener {
//                when (it.id) {
//                    com.chainup.contract.R.id.rb_1 -> {
//                        mBinding?.tvOrderTipsLayout.text=(getString(R.string.cp_overview_text53))
//                        mBinding?.tvOrderTipsLayout.visibility = View.VISIBLE
//                        mBinding?.llPrice.visibility = View.GONE
//                        if (checkedIdBuff == it.id) {
//                            checkedIdBuff = -1
//                            mBinding?.etPrice.setText(showPrice)
//                            rg_order_type.clearCheck()
//                            tvOrderTips.visibility = View.GONE
//                            llPrice.visibility = View.VISIBLE
//                        } else {
//                            checkedIdBuff = it.id
//                            rg_order_type.check(it.id)
//                        }
//                    }
//                    com.chainup.contract.R.id.rb_2 -> {
//                        tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text44))
//                        tvOrderTips.visibility = View.VISIBLE
//                        llPrice.visibility = View.GONE
//                        if (checkedIdBuff == it.id) {
//                            checkedIdBuff = -1
//                            etPrice.setText(showPrice)
//                            rg_order_type.clearCheck()
//                            tvOrderTips.visibility = View.GONE
//                            llPrice.visibility = View.VISIBLE
//                        } else {
//                            checkedIdBuff = it.id
//                            rg_order_type.check(it.id)
//                        }
//                    }
//                    com.chainup.contract.R.id.rb_3 -> {
//                        tvOrderTips.setText(getString(com.chainup.contract.R.string.cp_order_text45))
//                        tvOrderTips.visibility = View.VISIBLE
//                        llPrice.visibility = View.GONE
//                        if (checkedIdBuff == it.id) {
//                            checkedIdBuff = -1
//                            etPrice.setText(showPrice)
//                            rg_order_type.clearCheck()
//                            tvOrderTips.visibility = View.GONE
//                            llPrice.visibility = View.VISIBLE
//                        } else {
//                            checkedIdBuff = it.id
//                            rg_order_type.check(it.id)
//                        }
//                    }
//                }
//            }
//        }
//        rg_trade?.setOnCheckedChangeListener { group, checkedId ->
//            if (checkedId == -1) {
//                it.setText(com.chainup.contract.R.id.et_volume, "")
//                return@setOnCheckedChangeListener
//            }
//            if (checkedIdBuff == checkedId) {
//                it.setText(com.chainup.contract.R.id.et_volume, "")
//                return@setOnCheckedChangeListener
//            }
//            if (checkedId > -1) {
//                if (!CpClLogicContractSetting.isLogin()) {
//                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
//                    return@setOnCheckedChangeListener
//                }
//            }
//            etVolume.clearFocus()
//            checkedIdBuff = checkedId
//            val scale = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) 0 else multiplierPrecision
//            val mCanCloseVolumeStr = if (CpClLogicContractSetting.getContractUint(CpMyApp.instance()) == 0) {
//                clickData.canCloseVolume
//            } else {
//                val mulStr = CpBigDecimalUtils.mulStr(
//                    clickData?.canCloseVolume,
//                    CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId),
//                    multiplierPrecision
//                )
//                mulStr
//            }
//            when (checkedId) {
//                com.chainup.contract.R.id.rb_1st -> {
//                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.10", scale))
//                }
//                com.chainup.contract.R.id.rb_2nd -> {
//                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.20", scale))
//                }
//                com.chainup.contract.R.id.rb_3rd -> {
//                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "0.50", scale))
//                }
//                com.chainup.contract.R.id.rb_4th -> {
//                    it.setText(com.chainup.contract.R.id.et_volume, CpBigDecimalUtils.mulStrRoundUp(mCanCloseVolumeStr, "1", scale))
//                }
//                else -> {
//                    it.setText(com.chainup.contract.R.id.et_volume, "")
//                }
//            }
//        }
//        etVolume?.setOnFocusChangeListener { _, hasFocus ->
//            CpSoftKeyboardUtil.showORhideSoftKeyboard(activity)
//            if (hasFocus) {
//                rg_trade.clearCheck()
//                tvOrderTips.visibility = View.GONE
//                llPrice.visibility = View.VISIBLE
//            }
//            llVolume?.setBackgroundResource(if (hasFocus) com.chainup.contract.R.drawable.cp_bg_trade_et_focused else com.chainup.contract.R.drawable.cp_bg_trade_et_unfocused)
//        }
//        etPrice.setOnClickListener {
//            it.setFocusable(true);
//            it.setFocusableInTouchMode(true);
//            it.requestFocus();
//            it.findFocus();
//            rg_order_type.clearCheck()
//        }
//
//
//        val btn_close_position = it.getView<CpCommonlyUsedButton>(com.chainup.contract.R.id.btn_close_position)
//        btn_close_position.isEnable(true)
//        btn_close_position.listener = object : CpCommonlyUsedButton.OnBottonListener {
//            override fun bottonOnClick() {
//                var priceStr = etPrice.text.toString().trim()
//                var volStr = etVolume.text.toString().trim()
//                val multiplier = CpClLogicContractSetting.getContractMultiplierById(activity, clickData.contractId)
//                volStr = CpBigDecimalUtils.getOrderNum(false, volStr, multiplier, 1)
//                var type = 1
//                var priceType = ""
//                when {
//                    rb_1.isChecked -> {
//                        type = 2
//                        priceStr = ""
//                        showPrice = getString(com.chainup.contract.R.string.cp_overview_text53)
//                    }
//                    rb_2.isChecked -> {
//                        priceType = "1"
//                        priceStr = "0"
//                        showPrice = getString(com.chainup.contract.R.string.cp_order_text44)
//                    }
//                    rb_3.isChecked -> {
//                        priceType = "0"
//                        priceStr = "0"
//                        showPrice = getString(com.chainup.contract.R.string.cp_order_text45)
//                    }
//                    else -> {
//                        showPrice = priceStr + " " + CpClLogicContractSetting.getContractQuoteById(activity, clickData.contractId)
//                    }
//                }
//                if (rb_1st.isChecked) {
//                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.10", 0)
//                }
//                if (rb_2nd.isChecked) {
//                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.20", 0)
//                }
//                if (rb_3rd.isChecked) {
//                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "0.50", 0)
//                }
//                if (rb_4th.isChecked) {
//                    volStr = CpBigDecimalUtils.mulStrRoundUp(clickData.canCloseVolume, "1", 0)
//                }
//                if (type == 1 && TextUtils.isEmpty(priceType)) {
//                    if (TextUtils.isEmpty(priceStr)) {
//                        mClosePositionDialog?.dismiss()
//                        CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text33))
//                        return
//                    }
//                }
//                if (TextUtils.isEmpty(volStr)) {
//                    mClosePositionDialog?.dismiss()
//                    CpNToastUtil.showTopToastNet(getActivity(), false, getString(com.chainup.contract.R.string.cp_extra_text34))
//                    return
//                }
//                var dialogTitle = ""
////                                if (type==1){
////                                    dialogTitle= context?.getString(R.string.contract_action_limitPrice).toString()
////                                }else{
////                                    dialogTitle= context?.getString(R.string.cp_overview_text53).toString()
////                                }
//                val titleColor = if (clickData.orderSide.equals("BUY")) {
//                    dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text5)
//                    resources.getColor(com.chainup.contract.R.color.main_red)
//                } else {
//                    dialogTitle = dialogTitle + context?.getString(com.chainup.contract.R.string.cp_extra_text4)
//                    resources.getColor(com.chainup.contract.R.color.main_green)
//                }
//                val showTag = when (clickData.positionType) {
//                    1 -> {
//                        getString(com.chainup.contract.R.string.cp_contract_setting_text1) + " " + clickData.leverageLevel.toString() + "X"
//                    }
//                    2 -> {
//                        getString(com.chainup.contract.R.string.cp_contract_setting_text2) + " " + clickData.leverageLevel.toString() + "X"
//                    }
//                    else -> {
//                        ""
//                    }
//                }
//                context?.let { it1 ->
//                    CpDialogUtil.showCloseOrderDialog(it1,
//                        titleColor,
//                        dialogTitle,
//                        CpClLogicContractSetting.getContractShowNameById(context, clickData.contractId),
//                        showPrice,
//                        "",
//                        "",
//                        etVolume.text.toString() + " " + volumeUnit,
//                        type,
//                        "",
//                        "",
//                        showTag,
//                        object : CpNewDialogUtils.DialogBottomListener {
//                            override fun sendConfirm() {
//                                closePosition(clickData, type, priceType, priceStr, volStr)
//                            }
//                        })
//                }
////                                closePosition(clickData, type, priceType, priceStr, volStr)
//                mClosePositionDialog?.dismiss()
//            }
//        }
//    })


    }






}