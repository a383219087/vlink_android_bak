package com.yjkj.chainup.contract.widget

import android.content.Context
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.utils.PreferenceManager
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
     * 资产页面--总资产信息
     */
    fun showAllAssetDialog(context: Context): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.sl_item_all_asset_dialog)
            .setScreenWidthAspect(context, 0.8f)
            .setGravity(Gravity.CENTER)
            .setDimAmount(0.8f)
            .setScreenWidthAspect(context, 0.89f)
            .setCancelableOutside(false)
            .setOnBindViewListener { viewHolder ->
            }
            .addOnClickListener(R.id.tv_confirm_btn)
            .setOnViewClickListener { _, view, tDialog ->
                when (view.id) {
                    R.id.tv_confirm_btn -> {
                        tDialog.dismiss()
                    }

                }
            }
            .create()
            .show()

    }
    /**
     * 资产页面--收益分析
     */
    fun showIncomeDialog(context: Context,msg:String): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.sl_item_income_dialog)
            .setScreenWidthAspect(context, 0.8f)
            .setGravity(Gravity.CENTER)
            .setDimAmount(0.8f)
            .setScreenWidthAspect(context, 0.89f)
            .setCancelableOutside(false)
            .setOnBindViewListener { viewHolder ->
                viewHolder.setText(R.id.tv_text1, msg)
            }
            .addOnClickListener(R.id.tv_confirm_btn)
            .setOnViewClickListener { _, view, tDialog ->
                when (view.id) {
                    R.id.tv_confirm_btn -> {
                        tDialog.dismiss()
                    }

                }
            }
            .create()
            .show()

    }


}