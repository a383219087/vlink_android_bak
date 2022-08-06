package com.yjkj.chainup.contract.widget

import android.content.Context
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import org.jetbrains.anko.imageResource


object SlDialogHelper {

    /**
     * 开通合约/购买提示对话框
     */
    fun showSimpleCreateContractDialog(context: Context, viewListener: OnBindViewListener?,
                                       listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_simple_create_contract_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(viewListener)
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }

    /**
     * 暂停充提对话框
     */
    fun showSuspensionChargingDialog(context: Context, viewListener: OnBindViewListener?,
                                     listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_suspension_charging)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(viewListener)
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }


    /**
     * 资产页面--安全建议弹窗
     */
    fun showSimpleSafetyAdviceDialog(context: Context, viewListener: OnBindViewListener?,
                                     listener: NewDialogUtils.DialogBottomListener
    ): TDialog {
        var isSelected = false
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_simple_safety_advice_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setScreenWidthAspect(context, 0.89f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder ->
                    val imageView = viewHolder?.getView<ImageView>(R.id.iv_state)
                    imageView?.setOnClickListener {
                        isSelected = !isSelected
                        if (isSelected) {
                            imageView.imageResource = R.drawable.selected
                        } else {
                            imageView.imageResource = R.drawable.unchecked
                        }
                    }
                }
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_cancel_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            PreferenceManager.putBoolean(context, "isShowSafetyAdviceDialog", !isSelected)
                            listener.sendConfirm()
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }



    /**
     * 仓位平仓对话框
     */
    fun showClosePositionDialog(context: Context,
                                listener: OnBindViewListener
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_close_position_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(listener)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }




    /**
     * 展示计算结果对话框
     */
    fun showCalculatorResultDialog(context: Context, itemList: List<TabInfo>?
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.sl_item_calculator_result_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener(OnBindViewListener {
                    it.getView<TextView>(R.id.tv_title).onLineText("sl_str_calculate_result")
                    it.getView<TextView>(R.id.tv_confirm_btn).onLineText("alert_common_i_understand")

                    val layoutInflater = LayoutInflater.from(context)
                    val llFeeWarpLayout = it.getView<LinearLayout>(R.id.ll_fee_warp_layout)
                    for (index in itemList!!.indices) {
                        val info = itemList[index]
                        val itemView = layoutInflater.inflate(R.layout.sl_auto_relative_item, llFeeWarpLayout, false)
                        llFeeWarpLayout.addView(itemView)
                        itemView.findViewById<TextView>(R.id.tv_left).text = info.name
                        itemView.findViewById<TextView>(R.id.tv_right).text = Html.fromHtml(info.extras)
                    }
                })
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

    }


}