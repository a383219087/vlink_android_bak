package com.chainup.contract.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.widget.*
import com.blankj.utilcode.util.AppUtils
import com.chainup.contract.R
import com.chainup.contract.adapter.CpBottomDialogAdapter
import com.chainup.contract.adapter.CpNewDialogAdapter
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.*
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.yjkj.chainup.manager.CpLanguageUtil
import com.chainup.contract.bean.CpContractPositionBean
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.toString

/**
 * @Author lianshangljl
 * @Date 2019/3/8-11:59 AM
 * @Email buptjinlong@163.com
 * @description
 */
class CpNewDialogUtils {

    interface DialogOnclickListener {
        fun clickItem(data: ArrayList<String>, item: Int)
    }
    interface DialogOnItemClickListener {
        fun clickItem(position: Int)
    }
    interface DialogShareClickListener {
        fun clickItem(bitmap: Bitmap)
    }
    interface DialogBottomListener {
        fun sendConfirm()
    }
    interface DialogOnSigningItemClickListener {
        fun clickItem(position: Int, text: String)
    }
    interface DialogOnDismissClickListener {
        fun clickItem()
    }
    companion object {

        /**
         * string 底部dialog
         *
         */
        fun showListDialog(
            context: Context,
            list: ArrayList<String>,
            position: Int,
            listener: DialogOnclickListener
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_new_dialog)
                .setScreenWidthAspect(context, 1f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.setText(
                        R.id.tv_cancel,
                        CpLanguageUtil.getString(context, "cp_overview_text56")
                    )
                    var adapter = CpNewDialogAdapter(list, position)
                    adapter?.setList(list.size)
                    var listView = viewHolder?.getView<RecyclerView>(R.id.recycler_view)
                    listView?.layoutManager = LinearLayoutManager(context)
                    listView?.adapter = adapter
                    listView?.setHasFixedSize(true)
                    adapter.setOnItemClickListener { adapter, view, position ->
                        listener.clickItem(list, position)
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

        /**
         * string 底部dialog
         *
         */
        fun showNewListDialog(
            context: Context,
            list: ArrayList<CpTabInfo>,
            position: Int,
            listener: DialogOnItemClickListener,
            dismissListener:DialogInterface.OnDismissListener = DialogInterface.OnDismissListener{}
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_new_dialog)
                .setScreenWidthAspect(context, 1f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.setText(
                        R.id.tv_cancel,
                        CpLanguageUtil.getString(context, "cp_overview_text56")
                    )
                    var adapter = CpBottomDialogAdapter(list, position)
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
                .setOnDismissListener(dismissListener)
                .create()
                .show()
        }


        /**
         * 正常弹窗
         */
        fun showDialog(
            context: Context,
            content: String,
            isSingle: Boolean,
            listener: DialogBottomListener?,
            title: String = "",
            cancelTitle: String = "",
            confrimTitle: String = "",
            returnListener: Boolean = false, isBackCancel: Boolean = false
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_normal_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnKeyListener { p0, p1, p2 -> isBackCancel }
                .setOnBindViewListener { viewHolder: BindViewHolder? ->

                    if (!TextUtils.isEmpty(title)) {
                        viewHolder?.setGone(R.id.tv_title, true)
                        viewHolder?.setText(R.id.tv_title, title)
                    }

                    if (isSingle) {
                        viewHolder?.setGone(R.id.tv_cancel, false)
                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(
                                R.id.tv_confirm_btn,
                                CpLanguageUtil.getString(context, "cp_calculator_text16")
                            )
                        }

                    } else {
                        viewHolder?.setText(
                            R.id.tv_cancel,
                            CpLanguageUtil.getString(context, "cp_overview_text56")
                        )
                        if (confrimTitle.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_cancel, confrimTitle)
                        }
                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(
                                R.id.tv_confirm_btn,
                                CpLanguageUtil.getString(context, "cp_calculator_text16")
                            )
                        }
                    }
                    viewHolder?.setText(R.id.tv_content, Html.fromHtml(content))

                }
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            if (listener != null && (!isSingle || returnListener)) {
                                listener.sendConfirm()
                            }
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()

        }
        fun showDialogNew(context: Context,
                          content: String,
                          isSingle: Boolean,
                          listener: DialogBottomListener?,
                          title: String = "",
                          cancelTitle: String = "",
                          confrimTitle: String = "",
                          returnListener: Boolean = false, isBackCancel: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_normal_dialog)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnKeyListener { p0, p1, p2 -> isBackCancel }
                .setOnBindViewListener { viewHolder: BindViewHolder? ->

                    if (!TextUtils.isEmpty(title)) {
                        viewHolder?.setGone(R.id.tv_title, true)
                        viewHolder?.setText(R.id.tv_title, title)
                    }

                    if (isSingle) {
                        viewHolder?.setGone(R.id.tv_cancel, false)
                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm_btn, CpLanguageUtil.getString(context, "cp_calculator_text16"))
                        }

                    } else {
                        viewHolder?.setText(R.id.tv_cancel, CpLanguageUtil.getString(context, "cp_overview_text56"))
                        if (confrimTitle.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_cancel, confrimTitle)
                        }
                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm_btn, CpLanguageUtil.getString(context, "cp_calculator_text16"))
                        }
                    }
                    viewHolder?.setText(R.id.tv_content, content)

                }
                .addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_confirm_btn -> {
                            if (listener != null && (!isSingle || returnListener)) {
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
         * 分享弹窗
         */
        fun showShareDialog(
            context: Context,
            mContractPositionBean: CpContractPositionBean,
            listener: DialogShareClickListener?,
            isBackCancel: Boolean = false
        ) {
            var profitRate = CpNumberUtil().getDecimal(2).format(
                CpMathHelper.round(
                    CpMathHelper.mul(mContractPositionBean?.returnRate.toString(), "100"),
                    2
                )
            ).toString()
            val symbol = if (CpBigDecimalUtils.compareTo(profitRate, "0") != -1) "+" else ""
            val profitRateBuff = symbol + profitRate
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.cp_item_share_dialog)
                .setScreenWidthAspect(context, 0.9f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.5f)
                .setCancelableOutside(false)
                .setOnKeyListener { p0, p1, p2 -> isBackCancel }
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.setText(
                        R.id.tv_intro,
                        CpClLogicContractSetting.getShareInfo(context, profitRate)
                    )

                    viewHolder?.setImageResource(R.id.iv_share_header, CpClLogicContractSetting.getShareBg( profitRate))

                    var imgUrl = CpClLogicContractSetting.getInviteUrl()
                    if (TextUtils.isEmpty(imgUrl)) {
                        imgUrl = "error!"
                    }
                    val bmp: Bitmap? = CpBitmapUtils.generateBitmap(imgUrl, 500, 500)
                    viewHolder?.setImageBitmap(R.id.iv_qr_code,bmp)
                    viewHolder?.setImageResource(R.id.iv_app_icon, AppUtils.getAppIconId())
                    viewHolder?.setText(R.id.tv_share_time, CpV2DateUtil.getCurrentDate(CpV2DateUtil.dateFormatMDHMS))
                    viewHolder?.setText(R.id.tv_app_name, AppUtils.getAppName())
                    viewHolder?.setText(R.id.tv_earned, profitRateBuff + "%")

                    if (CpBigDecimalUtils.compareTo(profitRateBuff,"0")==-1){
                        viewHolder?.setTextColor(R.id.tv_earned,CpColorUtil.getColor(R.color.main_red))
                    }else{
                        viewHolder?.setTextColor(R.id.tv_earned,CpColorUtil.getColor(R.color.main_green))
                    }
                    viewHolder?.setTextColor(R.id.tv_earned, if (CpBigDecimalUtils.compareTo(profitRate, "0") != -1) context.resources.getColor(R.color.main_green) else context.resources.getColor(R.color.main_red))


                    var nav_up :Drawable?=null
                    if (mContractPositionBean?.orderSide.equals("BUY")) { //多仓
                        viewHolder?.setText(R.id.tv_type, context.getString(R.string.cp_content_text32))
                        viewHolder?.setBackgroundRes(R.id.tv_type, R.drawable.cp_border_green_fill)
                        nav_up= context.getResources().getDrawable(R.drawable.contract_domorethan);

                    } else if (mContractPositionBean?.orderSide.equals("SELL")) { //空仓
                        viewHolder?.setText(R.id.tv_type, context.getString(R.string.cp_content_text33))
                        viewHolder?.setBackgroundRes(R.id.tv_type, R.drawable.cp_border_red_fill)
                        nav_up= context.getResources().getDrawable(R.drawable.contract_domorethan);

                    }
                    nav_up?.setBounds(3, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                    viewHolder?.getView<TextView>(R.id.tv_type)?.setCompoundDrawables(null, null, nav_up, null);
                    viewHolder?.setText(
                        R.id.tv_open_price_value,
                        CpBigDecimalUtils.showSNormal(
                            mContractPositionBean?.openAvgPrice,
                            CpClLogicContractSetting.getContractSymbolPricePrecisionById(
                                context,
                                mContractPositionBean?.contractId!!
                            )
                        )
                    )
                    viewHolder?.setText(
                        R.id.tv_latest_price_value,
                        CpBigDecimalUtils.showSNormal(
                            mContractPositionBean?.indexPrice,
                            CpClLogicContractSetting.getContractSymbolPricePrecisionById(
                                context,
                                mContractPositionBean?.contractId!!
                            )
                        )
                    )
                    viewHolder?.setText(
                        R.id.tv_contract_value,
                        CpClLogicContractSetting.getContractShowNameById(
                            context,
                            mContractPositionBean?.contractId!!
                        )
                    )
                }
                .addOnClickListener(R.id.img_close, R.id.bt_share, R.id.tv_cancel_btn)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.img_close,  R.id.tv_cancel_btn -> {
                            tDialog.dismiss()
                        }
                        R.id.bt_share -> {
                            val llShareLayout =
                                viewHolder?.getView<LinearLayout>(R.id.ll_share_layout)
                            llShareLayout?.isDrawingCacheEnabled = true
                            llShareLayout?.buildDrawingCache()
                            val bitmap: Bitmap = Bitmap.createBitmap(llShareLayout?.drawingCache!!)
                            listener?.clickItem(bitmap)
                        }
                    }
                }
                .create()
                .show()

        }


        /**
         * 显示 两个按钮的dialog
         */

        fun showNormalDialog(
            context: Context,
            content: String,
            listener: DialogBottomListener,
            title: String = "",
            cancelTitle: String = "",
            confirmTitle: String = ""
        ) {
            showDialog(context, content, false, listener, title, cancelTitle, confirmTitle)
        }


        /**
         * string list 底部dialog
         *
         */
        fun showBottomListDialog(
            context: Context,
            list: ArrayList<String>,
            position: Int,
            listener: DialogOnclickListener
        ): TDialog {
            return showListDialog(context, list, position, listener)
        }

        /**
         * 合约功能增加此函数
         */
        fun showNewBottomListDialog(
            context: Context,
            list: ArrayList<CpTabInfo>,
            position: Int,
            listener: DialogOnItemClickListener,
            dismissListener: DialogInterface.OnDismissListener = DialogInterface.OnDismissListener {}
        ): TDialog {
            return showNewListDialog(
                context,
                list,
                position,
                listener,
                dismissListener = dismissListener
            )
        }

        fun isEnable(editText: EditText?): Boolean {
            val string = editText?.text.toString()
            return !(TextUtils.isEmpty(string) || string.toDouble() == 0.0)
        }
    }
}








