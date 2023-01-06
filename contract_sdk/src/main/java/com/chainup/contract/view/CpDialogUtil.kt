package com.chainup.contract.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.chainup.contract.R
import com.chainup.contract.adapter.*
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.*
import com.chainup.contract.view.dialog.CpTDialog
import com.chainup.contract.view.dialog.listener.OnCpBindViewListener
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.manager.CpLanguageUtil
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import org.json.JSONArray

//Created by $USER_NAME on 2018/10/15.

class CpDialogUtil {

    interface ConfirmListener {
        fun click(pos: Int = 0)
    }
    interface DialogBottomListener {
        fun sendConfirm()
    }
    companion object {

        fun showNewsingleDialog2(
            context: Context,
            content: String,
            listener: CpNewDialogUtils.DialogBottomListener?,
            cancelTitle: String = "",
            returnListener: Boolean = false
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.item_new_normal_dialog2)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->

                    if (!TextUtils.isEmpty(cancelTitle)) {
                        viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                    } else {
                        viewHolder?.setText(R.id.tv_confirm_btn, CpLanguageUtil.getString(context, "cp_calculator_text16"))
                    }
                    viewHolder?.setText(R.id.tv_content, content)

                }
                .addOnClickListener(R.id.tv_confirm_btn)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            if (listener != null && returnListener) {
                                listener.sendConfirm()
                            }
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 两个按钮新dialog
         */
        fun showNewDoubleDialog(
            context: Context,
            content: String,
            listener: DialogBottomListener?,
            title: String = "",
            cancelTitle: String = "",
            confrimTitle: String = ""
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.item_new_double_normal_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->

                    if (!TextUtils.isEmpty(title)) {
                        viewHolder?.setGone(R.id.tv_title, true)
                        viewHolder?.setText(R.id.tv_title, title)
                    }
                    viewHolder?.setText(R.id.tv_cancel_btn, CpLanguageUtil.getString(context, "cp_overview_text56"))
                    if (confrimTitle.isNotEmpty()) {
                        viewHolder?.setText(R.id.tv_cancel_btn, cancelTitle)
                    }
                    if (!TextUtils.isEmpty(cancelTitle)) {
                        viewHolder?.setText(R.id.tv_confirm_btn, confrimTitle)
                    } else {
                        viewHolder?.setText(R.id.tv_confirm_btn, CpLanguageUtil.getString(context, "cp_calculator_text16"))
                    }
                    viewHolder?.setText(R.id.tv_content, content)

                }
                .addOnClickListener(R.id.tv_cancel_btn, R.id.tv_confirm_btn)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            listener?.sendConfirm()
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * K线行情分享功能
         */
        fun showKLineShareDialog(context: Context, mView: View) {
            var screenshotBitmap: Bitmap? = null

//            screenshotBitmap = CpScreenShotUtil.getScreenshotBitmap((context as AppCompatActivity).window?.decorView
//                    ?: return)

            screenshotBitmap = CpScreenShotUtil.getScreenshotBitmap(mView)
            var img_share: ImageView? = null
            var ll_share: View? = null
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_dialog_share_market)
                .setScreenWidthAspect(context, 1.0f)
                .setScreenHeightAspect(context, 1.0f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.3f)
                .setCancelableOutside(true)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
//                        val iv_qrcode = viewHolder?.getView<ImageView>(R.id.iv_qrcode)
                    ll_share = viewHolder?.getView<View>(R.id.ll_share)
//                        viewHolder?.setText(R.id.tv_title, CpLanguageUtil.getString(context, "cp_overview_text56"))
                    viewHolder?.setText(R.id.btn_share, CpLanguageUtil.getString(context, "common_share_confirm"))
                    var imgUrl = CpClLogicContractSetting.getInviteUrl()
                    if (TextUtils.isEmpty(imgUrl)) {
                        imgUrl = "error!"
                    }
                    val bmp: Bitmap? = CpBitmapUtils.Create2DCode(imgUrl, 500, 500)
                    viewHolder?.setImageBitmap(R.id.iv_qr_code, bmp)
                    viewHolder?.setImageResource(R.id.iv_app_icon, AppUtils.getAppIconId())
//                        GlideUtils.loadImageQr(context, iv_qrcode)
                    if (screenshotBitmap != null) {
                        // 获得状态栏高度
                        val resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android")
                        val mStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId)
                        screenshotBitmap = Bitmap.createBitmap(
                            screenshotBitmap!!, 0, mStatusBarHeight, screenshotBitmap!!.getWidth(),
                            screenshotBitmap!!.getHeight() - mStatusBarHeight, null, true
                        );
                        img_share = viewHolder?.getView<ImageView>(R.id.img_share)
                        img_share?.setImageDrawable(BitmapDrawable(context.resources, screenshotBitmap))
                    }
                }
                .addOnClickListener(R.id.btn_share, R.id.tv_cancel_btn, R.id.ll_bg)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.btn_share -> {
                            if (ll_share != null) {
                                var bitmap: Bitmap? = CpScreenShotUtil.getScreenshotBitmap(ll_share)
                                CpZXingUtils.shareImageToWechat(bitmap, context.getString(R.string.cp_extra_text116), context)
                                tDialog.dismiss()
                            }
                        }
                        R.id.ll_bg -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                    }

                }.create().show()
        }

        fun createCVCOrderPop(
            context: Context?,
            index: Int = 0,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_new_pop)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(targetView.width)
                .setDimValue(0.3f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rView = findViewById<RecyclerView>(R.id.recycler_view)
                var list = ArrayList<CpTabInfo>()
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text3)!!, 1))
                list.add(CpTabInfo(context.getString(R.string.cp_overview_text4), 2))
                list.add(CpTabInfo(context.getString(R.string.cp_overview_text5), 3))
//                list.add(CpTabInfo(context.getString(R.string.cp_overview_text511), 4))
//                list.add(CpTabInfo(context.getString(R.string.cp_overview_text521), 5))
//                list.add(CpTabInfo(context.getString(R.string.cp_overview_text531), 6))
                var adapter = CpPopAdapter(list, index)
                rView?.layoutManager = LinearLayoutManager(context)
                rView?.adapter = adapter
                rView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    dialogItemClickListener?.clickItem(list[position].index, list[position].name)
                    cvcEasyPopup.dismiss()
                }

            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }

        fun createModifyPositionPop(
            context: Context?,
            marginModelCanSwitch: Int = 0,
            mMarginModel: Int = 0,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            var isShowPositionDesc = false
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_modify_position_dialog)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setDimValue(0f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val llDissmiss = findViewById<LinearLayout>(R.id.ll_dissmiss)
                val tvNoSwitchPosition = findViewById<TextView>(R.id.tv_no_switch_position)
                val tvTabFull = findViewById<Button>(R.id.tv_tab_full)
                val tvTabGradually = findViewById<Button>(R.id.tv_tab_gradually)
                val llShowPositionInfo = findViewById<LinearLayout>(R.id.ll_show_position_info)
                val imgShowPositionInfo = findViewById<ImageView>(R.id.img_show_position_info)
                val llPositionDesc = findViewById<LinearLayout>(R.id.ll_position_desc)



                tvTabFull.setBackgroundResource(if (mMarginModel == 1) R.drawable.cp_btn_linear_blue_bg else R.drawable.cp_btn_linear_grey_bg)
                tvTabGradually.setBackgroundResource(if (mMarginModel == 2) R.drawable.cp_btn_linear_blue_bg else R.drawable.cp_btn_linear_grey_bg)
                tvTabFull.setTextColor(if (mMarginModel == 1) CpColorUtil.getColor(R.color.main_blue) else CpColorUtil.getColor(R.color.normal_text_color))
                tvTabGradually.setTextColor(
                    if (mMarginModel == 2) CpColorUtil.getColor(R.color.main_blue) else CpColorUtil.getColor(
                        R.color.normal_text_color
                    )
                )
                if (marginModelCanSwitch == 0) {
                    tvTabFull.setTextColor(CpColorUtil.getColor(R.color.hint_color))
                    tvTabGradually.setTextColor(CpColorUtil.getColor(R.color.hint_color))
                    tvTabFull.setBackgroundResource(if (mMarginModel == 1) R.drawable.cp_btn_linear_blue_grey_bg else R.drawable.cp_border_grey_fill)
                    tvTabGradually.setBackgroundResource(if (mMarginModel == 2) R.drawable.cp_btn_linear_blue_grey_bg else R.drawable.cp_border_grey_fill)
                }
                llDissmiss.setOnClickListener { cvcEasyPopup?.dismiss() }
                llShowPositionInfo.setOnClickListener {
                    llPositionDesc.visibility = if (!isShowPositionDesc) View.VISIBLE else View.GONE
                    if (!isShowPositionDesc) {
                        imgShowPositionInfo.animate().setDuration(200).rotation(180f).start()
                    } else {
                        imgShowPositionInfo.animate().setDuration(200).rotation(0f).start()
                    }
                    isShowPositionDesc = !isShowPositionDesc
                }
                tvNoSwitchPosition.visibility = if (marginModelCanSwitch == 0) View.VISIBLE else View.GONE
                tvTabFull.setOnClickListener {
                    if (marginModelCanSwitch == 0) {
                        return@setOnClickListener
                    }
                    cvcEasyPopup?.dismiss()
                    val event = CpMessageEvent(CpMessageEvent.sl_contract_switch_lever_event)
                    event.msg_content = "1"
                    CpEventBusUtil.post(event)
                }
                tvTabGradually.setOnClickListener {
                    if (marginModelCanSwitch == 0) {
                        return@setOnClickListener
                    }
                    cvcEasyPopup?.dismiss()
                    val event = CpMessageEvent(CpMessageEvent.sl_contract_switch_lever_event)
                    event.msg_content = "2"
                    CpEventBusUtil.post(event)
                }
            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }


        /**
         * string 底部dialog
         *
         */
        fun showNewListDialog(context: Context,
                              list: ArrayList<CpTabInfo>,
                              position: Int,
                              listener: CpNewDialogUtils.DialogOnItemClickListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_new_dialog)
                .setScreenWidthAspect(context, 1f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    var adapter = CpTopPopAdapter(list, position)
                    var listView = viewHolder?.getView<RecyclerView>(R.id.recycler_view)
                    listView?.layoutManager = LinearLayoutManager(context)
                    listView?.adapter = adapter
                    listView?.setHasFixedSize(true)
                    adapter.setOnItemClickListener { adapter, view, position ->
                        listener.clickItem(position)
                    }
                }
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()
        }

        fun createTopListPop(
            context: Context?,
            index: Int = 1,
            data: ArrayList<CpTabInfo>,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_new_top_pop)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setDimValue(0f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val llDissmiss = findViewById<LinearLayout>(R.id.ll_dissmiss)
                val rView = findViewById<RecyclerView>(R.id.recycler_view)

                var adapter = CpTopPopAdapter(data, index)
                rView?.layoutManager = LinearLayoutManager(context)
                rView?.adapter = adapter
                rView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    dialogItemClickListener?.clickItem(position, data[position].name)
                    cvcEasyPopup?.dismiss()
                }

                llDissmiss.setOnClickListener { cvcEasyPopup?.dismiss() }
            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }


        fun createOrderTypePop(
            context: Context?,
            index: Int = 1,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_new_pop)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(targetView.width)
                .setDimValue(0.3f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rView = findViewById<RecyclerView>(R.id.recycler_view)
                var list = ArrayList<CpTabInfo>()
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text53).toString(), 1))
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text54).toString(), 2))
                var adapter = CpPopAdapter(list, index)
                rView?.layoutManager = LinearLayoutManager(context)
                rView?.adapter = adapter
                rView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    dialogItemClickListener?.clickItem(list[position].index, list[position].name)
                    cvcEasyPopup?.dismiss()
                }

            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }

        fun createRivalPricePop(
            context: Context?,
            mCpContractBuyOrSellHelper: CpContractBuyOrSellHelper,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_new_pop)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(targetView.width)
                .setDimValue(0.3f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rView = findViewById<RecyclerView>(R.id.recycler_view)
                var list = ArrayList<CpTabInfo>()
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text38).toString(), 0, 0))
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text39).toString(), 1, 4))
                list.add(CpTabInfo(context?.getString(R.string.cp_overview_text40).toString(), 2, 9))
                var adapter = CpPopAdapter(list, mCpContractBuyOrSellHelper.rivalPriceType)
                rView?.layoutManager = LinearLayoutManager(context)
                rView?.adapter = adapter
                rView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    mCpContractBuyOrSellHelper.rivalPriceType = position
                    mCpContractBuyOrSellHelper.rivalPricePosition = list[position].extrasNum!!
                    dialogItemClickListener?.clickItem(position, list[position].name)
                    cvcEasyPopup?.dismiss()
                }

            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }


        /**
         * 切换杠杆对话框
         */
        fun showSelectLeverDialog(
            context: Context,
            view: ImageView,
            listener: OnCpBindViewListener
        ): CpTDialog {
            view.animate().setDuration(200).rotation(180f).start()
            return CpTDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_modify_lever_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .setDialogAnimationRes(R.style.dialogBottomAnim)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .setOnDismissListener {
                    if (KeyboardUtils.isSoftInputVisible(context)) {
                        KeyboardUtils.toggleSoftInput()
                    }
                    view.animate().setDuration(200).rotation(0f).start()
                }
                .create()
                .show()

        }
        /**
         * 反向开仓对话框
         */
        fun showReverseOpeningDialog(
            context: Context,
            listener: OnBindViewListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_reverse_open_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .setDialogAnimationRes(R.style.dialogBottomAnim)
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
         * 闪电平仓对话框
         */
        fun showQuickClosePositionDialog(
            context: Context,
            listener: OnBindViewListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_quick_close_position_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .setDialogAnimationRes(R.style.dialogBottomAnim)
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
         * 平仓对话框
         */
        fun showClosePositionDialog(
            context: Context,
            listener: OnBindViewListener
        ): TDialog {
            val view = LayoutInflater.from(context).inflate(R.layout.cp_item_close_position_new_dialog, null)
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setDialogView(view)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .setDialogAnimationRes(R.style.dialogBottomAnim)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .setOnDismissListener {
                    if (KeyboardUtils.isSoftInputVisible(context)) {
                        KeyboardUtils.toggleSoftInput()
                    }
                }
                .create()
                .show()

        }

        /**
         * 调整保证金对话框
         */
        fun showAdjustMarginDialog(
            context: Context,
            listener: OnBindViewListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_adjust_margin_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.5f)
                .setCancelableOutside(true)
                .setOnBindViewListener(listener)
                .setDialogAnimationRes(R.style.dialogBottomAnim)
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .setOnDismissListener {
                    if (KeyboardUtils.isSoftInputVisible(context)) {
                        KeyboardUtils.toggleSoftInput()
                    }
                }
                .create()
                .show()

        }

        /**
         * 展示资金费率对话框
         */
        fun showCapitalRateDialog(
            context: Context,
            listener: OnBindViewListener?
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_capital_rate_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener(listener)
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
         * 展示标记价格对话框
         */
        fun showIndexPriceDialog(
            context: Context,
            listener: OnBindViewListener?
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_index_price_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener(listener)
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
         * 开通合约风险告知对话框
         */
        fun showCreateContractDialog(
            context: Context,
            listener: CpNewDialogUtils.DialogBottomListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_open_contract_dialog)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener {
                    it.setText(R.id.tv_vontract_info, context.getString(R.string.cp_extra_text117))
                }
                .addOnClickListener(R.id.tv_confirm_btn, R.id.tv_close)
                .setOnViewClickListener { _, view, tDialog ->
                    when (view.id) {
                        R.id.tv_confirm_btn -> {
                            tDialog.dismiss()
                            listener.sendConfirm()
                        }
                        R.id.tv_close -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

        }

        //下单确认提示框
        fun showCreateOrderDialog(
            context: Context,
            titleColor: Int,
            dialogTitle: String,
            contractName: String,
            price: String,
            triggerPrice: String,
            costPrice: String,
            amountValue: String,
            orderType: Int,
            profitTriggerPrice: String,
            lossTriggerPrice: String,
            quote: String,
            showTag: String,
            listener: CpNewDialogUtils.DialogBottomListener?
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.item_create_order_dialog)
                .setScreenWidthAspect(context, 0.85f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.setTextColor(R.id.tv_title, titleColor)
                    viewHolder?.setText(R.id.tv_title, dialogTitle)
                    viewHolder?.setText(R.id.tv_contract_name, contractName)
                    //价格
                    viewHolder?.setText(R.id.tv_price_value, price)
                    //委托价格
                    viewHolder?.setText(R.id.tv_commission_price_value, price)
                    //触发价格
                    viewHolder?.setText(R.id.tv_trigger_price_value, triggerPrice)
                    //成本
                    viewHolder?.setText(R.id.tv_cost_value, costPrice)
                    //数量
                    viewHolder?.setText(R.id.tv_number_value, amountValue)
                    //止盈触发价
                    viewHolder?.setText(R.id.tv_stop_profit_entrust_price_value, profitTriggerPrice + " " + quote)
                    //止损触发价
                    viewHolder?.setText(R.id.tv_stop_loss_trigger_price_value, lossTriggerPrice + " " + quote)

                    viewHolder?.setVisibility(
                        R.id.ll_stop_profit,
                        if (TextUtils.isEmpty(profitTriggerPrice)) View.GONE else View.VISIBLE
                    )
                    viewHolder?.setVisibility(
                        R.id.ll_stop_loss,
                        if (TextUtils.isEmpty(lossTriggerPrice)) View.GONE else View.VISIBLE
                    )
                    viewHolder?.setText(R.id.tv_open_type, showTag)

                    when (orderType) {
                        1, 2, 4, 5, 6 -> {
                            viewHolder?.setVisibility(R.id.ll_price, View.VISIBLE)
                            viewHolder?.setVisibility(R.id.ll_cost, View.VISIBLE)

                            viewHolder?.setVisibility(R.id.ll_trigger_price, View.GONE)
                            viewHolder?.setVisibility(R.id.ll_commission_price, View.GONE)
                        }
                        else -> {
                            viewHolder?.setVisibility(R.id.ll_price, View.GONE)
                            viewHolder?.setVisibility(R.id.ll_cost, View.GONE)
                            viewHolder?.setText(R.id.tv_title, context.getString(R.string.cp_overview_text55) + dialogTitle)
                            viewHolder?.setVisibility(R.id.ll_trigger_price, View.VISIBLE)
                            viewHolder?.setVisibility(R.id.ll_commission_price, View.VISIBLE)
                        }
                    }
                    viewHolder?.setVisibility(R.id.ll_cost, View.GONE)
                }
                .addOnClickListener(R.id.tv_cancel_btn, R.id.tv_confirm_btn, R.id.ll_not_again)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.ll_not_again -> {
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            cbNotAgain.isChecked = !cbNotAgain.isChecked
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            CpPreferenceManager.getInstance(CpMyApp.instance()).putSharedBoolean(
                                CpPreferenceManager.PREF_TRADE_CONFIRM, !cbNotAgain.isChecked
                            )
                            if (listener != null) {
                                if (!CpChainUtil.isFastClick()) {
                                    listener.sendConfirm()
                                    tDialog.dismiss()
                                }
                            }

                        }
                    }
                }
                .create()
                .show()
        }

        //平仓确定框
        fun showCloseOrderDialog(
            context: Context,
            titleColor: Int,
            dialogTitle: String,
            contractName: String,
            price: String,
            triggerPrice: String,
            costPrice: String,
            amountValue: String,
            orderType: Int,
            profitTriggerPrice: String,
            lossTriggerPrice: String,
            showTag: String,
            listener: CpNewDialogUtils.DialogBottomListener?
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.item_close_order_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.setTextColor(R.id.tv_title, titleColor)
                    viewHolder?.setText(R.id.tv_title, dialogTitle)
                    viewHolder?.setText(R.id.tv_contract_name, contractName)
                    //价格
                    viewHolder?.setText(R.id.tv_price_value, price)
                    //委托价格
                    viewHolder?.setText(R.id.tv_commission_price_value, price)
                    //触发价格
                    viewHolder?.setText(R.id.tv_trigger_price_value, triggerPrice)
                    //成本
                    viewHolder?.setText(R.id.tv_cost_value, costPrice)
                    //数量
                    viewHolder?.setText(R.id.tv_number_value, amountValue)
                    //止盈触发价
                    viewHolder?.setText(R.id.tv_stop_profit_entrust_price_value, profitTriggerPrice)
                    //止损触发价
                    viewHolder?.setText(R.id.tv_stop_loss_trigger_price_value, lossTriggerPrice)

                    viewHolder?.setVisibility(
                        R.id.ll_stop_profit,
                        if (TextUtils.isEmpty(profitTriggerPrice)) View.GONE else View.VISIBLE
                    )
                    viewHolder?.setVisibility(
                        R.id.ll_stop_loss,
                        if (TextUtils.isEmpty(lossTriggerPrice)) View.GONE else View.VISIBLE
                    )

                    viewHolder?.setText(R.id.tv_open_type, showTag)
                }
                .addOnClickListener(R.id.tv_cancel_btn, R.id.tv_confirm_btn, R.id.ll_not_again)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.ll_not_again -> {
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            cbNotAgain.isChecked = !cbNotAgain.isChecked
                        }
                        R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            val cbNotAgain = viewHolder.getView<CheckBox>(R.id.cb_not_again)
                            CpPreferenceManager.getInstance(CpMyApp.instance()).putSharedBoolean(
                                CpPreferenceManager.PREF_TRADE_CONFIRM, !cbNotAgain.isChecked
                            )
                            if (listener != null) {
                                listener.sendConfirm()
                            }
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()
        }


        fun createSelectCoinsPop(
            context: Context?,
            mContractId: Int = 0,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_select_coins)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setDimValue(0f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rLeftView = findViewById<RecyclerView>(R.id.rv_left)
                val rRightView = findViewById<RecyclerView>(R.id.rv_right)
                var sideList = ArrayList<CpTabInfo>()
                var sideListBuff = ArrayList<CpTabInfo>()
                var sideListU = ArrayList<CpTabInfo>()
                var sideListB = ArrayList<CpTabInfo>()
                var sideListH = ArrayList<CpTabInfo>()
                var sideListM = ArrayList<CpTabInfo>()

                var isHasU = false //正向合约
                var isHasB = false //币本位
                var isHasH = false //混合合约
                var isHasM = false //模拟合约
                val mContractList = JSONArray(CpClLogicContractSetting.getContractJsonListStr(context))
                var positionLeft = 0
                for (i in 0..(mContractList.length() - 1)) {
                    val obj = mContractList.getJSONObject(i)
                    val contractSide = obj.getInt("contractSide")
                    val contractType = obj.getString("contractType")
                    val id = obj.getInt("id")
                    if (contractSide == 1 && contractType == "E") {
                        isHasU = true
                        sideListU.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                    } else if (contractSide == 0 && contractType == "E") {
                        isHasB = true
                        sideListB.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                    } else if (contractType == "S") {
                        isHasM = true
                        sideListM.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                    } else {
                        isHasH = true
                        sideListH.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                    }
                    if (mContractId == id) {
                        positionLeft = if (contractSide == 1 && contractType == "E") {
                            0
                        } else if (contractSide == 0 && contractType == "E") {
                            1
                        } else if (contractType == "S") {
                            3
                        } else {
                            2
                        }
                    }
                }
                if (isHasU) {
                    sideList.add(CpTabInfo(context?.getString(R.string.cp_contract_data_text13).toString(), 0))
                }
                if (isHasB) {
                    sideList.add(CpTabInfo(context?.getString(R.string.cp_contract_data_text10).toString(), 1))
                }
                if (isHasH) {
                    sideList.add(CpTabInfo(context?.getString(R.string.cp_contract_data_text12).toString(), 2))
                }
                if (isHasM) {
                    sideList.add(CpTabInfo(context?.getString(R.string.cp_contract_data_text11).toString(), 3))
                }
                if (positionLeft == 0) {
                    sideListBuff.addAll(sideListU)
                } else if (positionLeft == 1) {
                    sideListBuff.addAll(sideListB)
                } else if (positionLeft == 3) {
                    sideListBuff.addAll(sideListM)
                } else {
                    sideListBuff.addAll(sideListH)
                }
                var mRightAdapter = CpCoinSelectRightAdapter(sideListBuff, mContractId)
                rRightView?.layoutManager = LinearLayoutManager(context)
                rRightView?.adapter = mRightAdapter
                rRightView?.setHasFixedSize(true)
                mRightAdapter.setOnItemClickListener { adapter, view, position ->
                    dialogItemClickListener?.clickItem(sideListBuff[position].index, sideListBuff[position].name)
                    dismiss()
                }
                var adapter = CpCoinSelectLeftAdapter(sideList, positionLeft)
                rLeftView?.layoutManager = LinearLayoutManager(context)
                rLeftView?.adapter = adapter
                rLeftView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    sideListBuff.clear()
                    if (sideList[position].index == 0) {
                        sideListBuff.addAll(sideListU)
                    } else if (sideList[position].index == 1) {
                        sideListBuff.addAll(sideListB)
                    } else if (sideList[position].index == 2) {
                        sideListBuff.addAll(sideListH)
                    } else {
                        sideListBuff.addAll(sideListM)
                    }
                    mRightAdapter.notifyDataSetChanged()
                }
            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.CENTER, 0, 0)
        }


        fun createCommonTopPop(
            context: Context?,
            list: ArrayList<CpTabInfo>,
            position: Int,
            targetView: View,
            dialogItemClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_select_list)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setDimValue(0f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rView = findViewById<RecyclerView>(R.id.recycler_view)
                val llDissmiss = findViewById<LinearLayout>(R.id.ll_dissmiss)
                var adapter = CpTopPopAdapter(list, position)
                rView?.layoutManager = LinearLayoutManager(context)
                rView?.adapter = adapter
                rView?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    dialogItemClickListener?.clickItem(position, list[position].name)
                    cvcEasyPopup?.dismiss()
                }
                llDissmiss.setOnClickListener { dismiss() }

            }

            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 0)

            cvcEasyPopup?.setOnDismissListener {
                dialogDismissClickListener?.clickItem()
            }
        }

        fun createMoreTimeKlinePop(
            context: Context?,
            targetView: View,
            dialogDismissClickListener: CpNewDialogUtils.DialogOnSigningItemClickListener?,
            mDialogDismissClickListener: CpNewDialogUtils.DialogOnDismissClickListener?,
            type: Int=1,
        ) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.cp_item_kline_time_more)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setDimValue(0f)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
            cvcEasyPopup?.run {
                val rvKlineCtrlMore = findViewById<RecyclerView>(R.id.rv_kline_ctrl_more)
                val llDissmiss = findViewById<LinearLayout>(R.id.ll_dissmiss)
                var list = ArrayList<CpTabInfo>()
                list.add(CpTabInfo("line", CpKLineUtil.getKLineScale().indexOf("line")))
                list.add(CpTabInfo("1min", CpKLineUtil.getKLineScale().indexOf("1min")))
                list.add(CpTabInfo("5min", CpKLineUtil.getKLineScale().indexOf("5min")))
                list.add(CpTabInfo("30min", CpKLineUtil.getKLineScale().indexOf("30min")))
                list.add(CpTabInfo("1week", CpKLineUtil.getKLineScale().indexOf("1week")))
                list.add(CpTabInfo("1month", CpKLineUtil.getKLineScale().indexOf("1month")))
                var adapter = CpKlineMorePopAdapter(list, 0,type=type)
                rvKlineCtrlMore?.layoutManager = GridLayoutManager(context, 6)
                rvKlineCtrlMore?.adapter = adapter
                rvKlineCtrlMore?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    CpKLineUtil.setCurTime(list[position].name, type = type)
                    CpKLineUtil.setCurTime4KLine(list[position].index, type = type)
                    dialogDismissClickListener?.clickItem(position, list[position].name)
                    cvcEasyPopup?.dismiss()
                }
                llDissmiss.setOnClickListener { dismiss() }
            }
            cvcEasyPopup?.setOnDismissListener {
                mDialogDismissClickListener?.clickItem()
            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)
        }


    }

}
