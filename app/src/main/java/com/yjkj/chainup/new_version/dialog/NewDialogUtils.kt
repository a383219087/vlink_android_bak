package com.yjkj.chainup.new_version.dialog

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.common.sdk.LibCore
import com.coorchice.library.SuperTextView
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.wx.wheelview.widget.WheelView
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.adapter.BottomDialogAdapter
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.NewDialogAdapter
import com.yjkj.chainup.new_version.home.TAG_ADVERT
import com.yjkj.chainup.new_version.home.dialogType
import com.yjkj.chainup.new_version.redpackage.adapter.WheelViewAdapter
import com.yjkj.chainup.new_version.redpackage.bean.CreatePackageBean
import com.yjkj.chainup.new_version.redpackage.bean.RedPackageInitInfo
import com.yjkj.chainup.new_version.redpackage.bean.TempBean
import com.yjkj.chainup.new_version.view.*
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.LineAdapter4FundsLayout
import org.jetbrains.anko.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/3/8-11:59 AM
 * @Email buptjinlong@163.com
 * @description
 */
class NewDialogUtils {

    interface DialogOnclickListener {
        /**
         * ??????bottom  ?????? position
         *
         * TODO ????????????Any
         */
        fun clickItem(data: ArrayList<String>, item: Int)
    }


    interface DialogOnItemClickListener {
        fun clickItem(position: Int)
    }

    interface DialogOnSigningItemClickListener {
        fun clickItem(position: Int, text: String)
    }

    interface DialogBottomListener {
        /**
         * ??????dialog
         */
        fun sendConfirm()
    }

    interface DialogTransferBottomListener {
        /**
         * ??????dialog
         */
        fun sendConfirm()

        /**
         * ??????
         */
        fun showCancel()
    }

    interface DialogWebViewShareListener {

        fun webviewSaveImage(view: View)

        fun confirmShare(view: View)
    }


    interface DialogValidationGoogleListener {
        fun returnCode(googleCode: String?)

    }

    interface DialogSharePostersListener {
        fun saveIamgePosters(imageUrl: String, shareView: View, type: Int)
        fun saveIamgePostersNew(imageUrl: String)
    }


    interface DialogVerifiactionListener {
        /**
         * ?????????????????? ?????????
         */
        fun returnCode(phone: String?, mail: String?, googleCode: String?)
    }

    interface DialogVerifiactionNewListener {
        fun returnCode(phone: String?, mail: String?, phoneCode: String?, mailCode: String?, googleCode: String?, certifcateNumber: String?)
    }

    interface DialogReturnChangeEmail {
        fun returnCode(phone: String?, oldEmail: String?, newEmail: String?, googleCode: String?)
    }

    interface DialogSecondListener {
        /**
         * ?????????????????? ?????????
         */
        fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?)
    }

    interface DialogCertificationSecondListener {
        /**
         * ?????????????????? ?????????
         */
        fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?)

        fun cancelBtn()
    }

    interface DialogBottomAloneListener {

        fun returnContent(content: String)
    }

    interface DialogBottomPwdListener {

        fun returnContent(content: String)
        fun returnCancel()
    }

    interface DialogBottomCoinListener {
        /**
         * ?????????????????? ?????????
         */
        fun returnTypeCode(phone: String?, mail: String?)
    }


    companion object {

        /**
         * string ??????dialog
         *
         */
        fun showListDialog(context: Context,
                           list: ArrayList<String>,
                           position: Int,
                           listener: DialogOnclickListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_dialog)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        var adapter = NewDialogAdapter(list, position)
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
         * string ??????dialog
         *
         */
        fun showNewListDialog(context: Context,
                              list: ArrayList<TabInfo>,
                              position: Int,
                              listener: DialogOnItemClickListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_dialog)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        var adapter = BottomDialogAdapter(list, position)
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

        /**
         * ???????????????dialog
         */

        fun showNewsingleDialog2(context: Context,
                                 content: String,
                                 listener: DialogBottomListener?,
                                 cancelTitle: String = "",
                                 returnListener: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_normal_dialog2)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
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
         * ???????????????dialog
         */

        fun showNewsingleDialog(context: Context,
                                content: String,
                                listener: DialogBottomListener?,
                                title: String = "",
                                cancelTitle: String = "",
                                returnListener: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_normal_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setGone(R.id.tv_title, true)
                            viewHolder?.setText(R.id.tv_title, title)
                        } else {
                            viewHolder?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
                        }

                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
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
         * ??????????????????
         */
        fun showInvitationPosters(context: Activity,
                                  list: ArrayList<String>,
                                  listener: DialogSharePostersListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_invitation_posters1)
                .setScreenWidthAspect(context, 1f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(false)
                .setOnBindViewListener { viewHolder ->
                    var imageUrl = list[0]
                    var item = 0
                    val checkbox1 = viewHolder?.getView<CheckBox>(R.id.checkbox_invitation)
                    val checkbox2 = viewHolder?.getView<CheckBox>(R.id.checkbox_invitation_2)
                    viewHolder?.getView<RelativeLayout>(R.id.ll_share_layout)?.setOnClickListener {
                        imageUrl = list[0]
                        if (checkbox1?.isChecked!!) {
                            return@setOnClickListener
                        }
                        checkbox1?.isChecked = true
                        checkbox2?.isChecked = false
                        item = 0
                    }
                    viewHolder?.getView<RelativeLayout>(R.id.ll_share_layout_2)?.setOnClickListener {
                        imageUrl = list[1]
                        if (checkbox2?.isChecked!!) {
                            return@setOnClickListener
                        }
                        item = 1
                        checkbox2?.isChecked = true
                        checkbox1?.isChecked = false
                    }
                    viewHolder?.setText(R.id.tv_iphone, UserDataService.getInstance().userAccount)
                    viewHolder?.setText(R.id.tv_iphone_2, UserDataService.getInstance().userAccount)
                    if (SystemUtils.isZh()) {
                        viewHolder?.setText(R.id.tv_invitation_content, String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name")))
                        viewHolder?.setText(R.id.tv_invitation_content_2, String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name")))
                        val optionsOne = RequestOptions().placeholder(R.drawable.ic_share_cn_one).error(R.drawable.ic_share_cn_one)
                        GlideUtils.load(context, list[0], viewHolder?.getView(R.id.iv_share_image), optionsOne)
                        val optionsTwo = RequestOptions().placeholder(R.drawable.ic_share_cn_two).error(R.drawable.ic_share_cn_two)
                        GlideUtils.load(context, list[1], viewHolder?.getView(R.id.iv_share_image_2), optionsTwo)
                    } else {
                        viewHolder?.setText(R.id.tv_invitation_content, String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name")))
                        viewHolder?.setText(R.id.tv_invitation_content_2, String.format(LanguageUtil.getString(context, "invite_you_qr"), LanguageUtil.getString(context, "app_name")))
                        var optionsOne = RequestOptions().placeholder(R.drawable.ic_share_en_one).error(R.drawable.ic_share_en_one)
                        GlideUtils.load(context, list[0], viewHolder?.getView(R.id.iv_share_image), optionsOne)
                        val optionsTwo = RequestOptions().placeholder(R.drawable.ic_share_en_two).error(R.drawable.ic_share_en_two)
                        GlideUtils.load(context, list[1], viewHolder?.getView(R.id.iv_share_image_2), optionsTwo)
                    }


                    /**
                     * ???????????????
                     */
                    if (!TextUtils.isEmpty(imageUrl)) {
                        viewHolder?.getView<ImageView>(R.id.tv_invitation_qr_code)?.setImageBitmap(BitmapUtils.generateBitmap(imageUrl, 300, 300))
                        viewHolder?.getView<ImageView>(R.id.tv_invitation_qr_code_2)?.setImageBitmap(BitmapUtils.generateBitmap(imageUrl, 300, 300))
                    }


                    viewHolder?.setText(R.id.tv_invitaion_title, LanguageUtil.getString(context, "share_your_tailored_poster"))
                    viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                    viewHolder?.getView<TextView>(R.id.tvCopy)?.setOnClickListener {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("simple text", imageUrl)
                        clipboard.setPrimaryClip(clip)
                        ToastUtils.showToast(context.getString(R.string.copy_success))
                    }
                    viewHolder?.getView<TextView>(R.id.tvShare)?.setOnClickListener {
                            when (item) {
                                0 -> {
                                    listener.saveIamgePosters(imageUrl, viewHolder.getView(R.id.ll_share_all_1),1)
                                }
                                1 -> {
                                    listener.saveIamgePosters(imageUrl, viewHolder.getView(R.id.ll_share_all_2),1)
                                }
                            }
                    }
                    viewHolder?.getView<TextView>(R.id.tvSave)?.setOnClickListener {
                        when (item) {
                            0 -> {
                                listener.saveIamgePosters(imageUrl, viewHolder.getView(R.id.ll_share_all_1),2)
                            }
                            1 -> {
                                listener.saveIamgePosters(imageUrl, viewHolder.getView(R.id.ll_share_all_2),2)
                            }
                        }
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
         * ???????????????dialog
         */
        fun showNewDoubleDialog(context: Context,
                                content: String,
                                listener: DialogBottomListener?,
                                title: String = "",
                                cancelTitle: String = "",
                                confrimTitle: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_double_normal_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setGone(R.id.tv_title, true)
                            viewHolder?.setText(R.id.tv_title, title)
                        } else {
                            viewHolder?.getView<TextView>(R.id.tv_content)?.textSize = context.resources.getDimension(R.dimen.sp_16)
                            viewHolder?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))

                        }
                        viewHolder?.setText(R.id.tv_cancel_btn, LanguageUtil.getString(context, "common_text_btnCancel"))
                        if (confrimTitle.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_cancel_btn, cancelTitle)
                        }
                        if (!TextUtils.isEmpty(cancelTitle)) {
                            viewHolder?.setText(R.id.tv_confirm_btn, confrimTitle)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
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

        /**
         * ????????????
         */
        fun showDialog(context: Context,
                       content: String,
                       isSingle: Boolean,
                       listener: DialogBottomListener?,
                       title: String = "",
                       cancelTitle: String = "",
                       confrimTitle: String = "",
                       returnListener: Boolean = false, isFormatHtml: Boolean = true, isBackCancel: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_normal_dialog)
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
                                viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
                            }

                        } else {
                            viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                            if (confrimTitle.isNotEmpty()) {
                                viewHolder?.setText(R.id.tv_cancel, confrimTitle)
                            }
                            if (!TextUtils.isEmpty(cancelTitle)) {
                                viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                            } else {
                                viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
                            }
                        }
                        viewHolder?.setText(R.id.tv_content, if (isFormatHtml) Html.fromHtml(content) else content)

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
         * ??????
         */
        fun showTransferDialog(context: Context,
                               content: String,
                               isSingle: Boolean,
                               listener: DialogTransferBottomListener,
                               title: String = "",
                               cancelTitle: String = "",
                               confrimTitle: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_double_normal_dialog)
                    .setScreenWidthAspect(context, 0.9f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.9f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setGone(R.id.tv_title, true)
                            viewHolder?.setText(R.id.tv_title, title)
                        }

                        if (isSingle) {
                            viewHolder?.setGone(R.id.tv_cancel_btn, false)
                            if (!TextUtils.isEmpty(cancelTitle)) {
                                viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                            } else {
                                viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
                            }

                        } else {
                            viewHolder?.setText(R.id.tv_cancel_btn, LanguageUtil.getString(context, "common_text_btnCancel"))
                            if (confrimTitle.isNotEmpty()) {
                                viewHolder?.setText(R.id.tv_cancel_btn, confrimTitle)
                            }
                            if (!TextUtils.isEmpty(cancelTitle)) {
                                viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                            } else {
                                viewHolder?.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "common_text_btnConfirm"))
                            }
                        }
                        viewHolder?.setText(R.id.tv_content, content)

                    }
                    .addOnClickListener(R.id.tv_cancel_btn, R.id.tv_confirm_btn)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel_btn -> {
                                if (listener != null) {
                                    listener.showCancel()
                                }
                                tDialog.dismiss()
                            }
                            R.id.tv_confirm_btn -> {
                                if (listener != null && !isSingle) {
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
         * ????????????dialog
         *
         */
        fun showSecurityVerificationDialog(context: Context,
                                           type: Int,
                                           codeType: Int,
                                           listener: DialogVerifiactionListener,
                                           emailType: Int = -1,
                                           confirmTitle: String = ""): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        if (TextUtils.isEmpty(confirmTitle)) {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        } else {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(confirmTitle)

                        }
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        when (type) {
                            /**
                             *  ????????????????????????
                             */
                            0 -> {
                                viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                                viewHolder?.setGone(R.id.rl_google_layout, true)
                            }
                            /**
                             *  ??????????????????
                             */
                            1 -> {
                                viewHolder?.setGone(R.id.rl_phone_layout, true)
                                viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)

                            }
                            /**
                             * ??????????????????
                             */
                            2 -> {
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                                viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                                if (emailType != -1) {
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = emailType
                                } else {
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                }

                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL)

                            }
                            /**
                             * ????????????
                             */
                            -1 -> {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    viewHolder?.setGone(R.id.rl_google_layout, true)
                                    viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                                }
                                if (UserDataService.getInstance().email.isNotEmpty()) {
                                    viewHolder?.setGone(R.id.rl_mail_layout, true)
                                    viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                                    if (emailType != -1) {
                                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = emailType
                                    } else {
                                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                    }
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL)
                                }

                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    viewHolder?.setGone(R.id.rl_phone_layout, true)
                                    viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)
                                } else {
                                    viewHolder?.setGone(R.id.rl_phone_layout, false)
                                }


                            }

                        }
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                when (type) {
                                    -1 -> {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                        mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                ?: ""
                                    }
                                    0 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                    ?: ""
                                        }
                                    }
                                    1 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                    ?: ""
                                        }
                                    }
                                    2 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                    ?: ""
                                        }
                                    }
                                }

                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        fun showForgetPwdSecurityVerificationDialog(context: Context,
                                                    isPhone: Boolean,
                                                    isEmail: Boolean,
                                                    isGoogleAuth: Boolean,
                                                    isCertificateNumber: Boolean,
                                                    codeType: Int,
                                                    listener: DialogVerifiactionNewListener,
                                                    emailType: Int = -1,
                                                    confirmTitle: String = "",
                                                    token: String,
                                                    account: String): TDialog {
            var phone = ""
            var mail = ""
            var phoneCode = ""
            var mailCode = ""
            var googleCode = ""
            var certifcateNumber = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_new_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        if (TextUtils.isEmpty(confirmTitle)) {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        } else {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(confirmTitle)
                        }
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        if (isPhone) {
                            phone = account
                            viewHolder?.setGone(R.id.rl_phone_layout, true)
                            viewHolder?.setText(R.id.tv_phone_title, account)
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE, token4last = token)
                        }
                        if (isEmail) {
                            mail = account
                            viewHolder?.setGone(R.id.rl_mail_layout, true)
                            viewHolder?.setText(R.id.tv_mail_title, account)
                            if (emailType != -1) {
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = emailType
                            } else {
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                            }
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL, token4last = token)
                        }
                        if (isGoogleAuth) {
                            viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        }
                        if (isCertificateNumber) {
                            viewHolder?.setText(R.id.tv_certificatenumber_title, LanguageUtil.getString(context, "kyc_text_idnumber"))
                            viewHolder?.setGone(R.id.rl_certificatenumber_layout, true)
                            viewHolder?.getView<CustomizeEditText>(R.id.ce_certificatenumber)?.isFocusableInTouchMode = true
                            viewHolder?.getView<CustomizeEditText>(R.id.ce_certificatenumber)?.isFocusable = true
                            viewHolder?.getView<CustomizeEditText>(R.id.ce_certificatenumber)?.isShowLine = true
                        }

                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                phoneCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                        ?: ""
                                mailCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                        ?: ""
                                googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                        ?: ""
                                certifcateNumber = viewHolder?.getView<CustomizeEditText>(R.id.ce_certificatenumber)?.textContent
                                        ?: ""
                                listener.returnCode(phone, mail, phoneCode, mailCode, googleCode, certifcateNumber)
                            }
                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ???????????????
         *
         */
        fun showAccountDialog(context: Context,
                              type: Int,
                              account: String,
                              codeType: Int,
                              listener: DialogVerifiactionListener): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        when (type) {
                            /**
                             *  ????????????????????????
                             */
                            0 -> {
                                viewHolder?.setGone(R.id.rl_google_layout, true)
                                viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                            }
                            /**
                             *  ??????????????????
                             */
                            1 -> {
                                viewHolder?.setGone(R.id.rl_phone_layout, true)
                                viewHolder?.setText(R.id.tv_phone_title, account)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)

                            }
                            /**
                             * ??????????????????
                             */
                            2 -> {
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                                viewHolder?.setText(R.id.tv_mail_title, account)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.setAccount(account)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.setValidation(true)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL, true, account)

                            }
                            /**
                             * ????????????
                             */
                            -1 -> {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                                    viewHolder?.setGone(R.id.rl_google_layout, true)
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)
                                    viewHolder?.setGone(R.id.rl_phone_layout, true)
                                } else {
                                    viewHolder?.setGone(R.id.rl_phone_layout, false)
                                }


                                viewHolder?.setText(R.id.tv_mail_title, account)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.setAccount(account)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.setValidation(true)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL, true, account)
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                            }

                        }
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                when (type) {
                                    -1 -> {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                        mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                ?: ""
                                    }
                                    0 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                    ?: ""
                                        }
                                    }
                                    1 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                    ?: ""
                                        }
                                    }
                                    2 -> {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {
                                            ToastUtils.showToast(LanguageUtil.getString(context, "login_tip_inputCode"))
                                        } else {
                                            mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                    ?: ""
                                        }
                                    }
                                }

                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????dialog
         *
         */
        fun showSecurityForBindDialog(context: Context,
                                      codeType: Int,
                                      listener: DialogVerifiactionListener,
                                      type: Int = 0): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                        viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         *  ??????????????????
                         */
                        if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                            viewHolder?.setGone(R.id.rl_phone_layout, true)
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)
                        } else {
                            viewHolder?.setGone(R.id.rl_phone_layout, false)

                        }
                        /**
                         * ??????????????????
                         */
                        if (type == 0) {
                            if (!TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL)
                            } else {
                                viewHolder?.setGone(R.id.rl_mail_layout, false)
                            }
                        } else {
                            viewHolder?.setGone(R.id.rl_mail_layout, false)
                        }
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                    }
                                }
                                if (type == 0) {
                                    if (TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {

                                        } else {
                                            mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                    ?: ""
                                        }
                                    }
                                }

                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()

                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????dialog
         *
         */
        fun showValidationGoogleDialog(context: Context,
                                       listener: DialogValidationGoogleListener
        ): TDialog {
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")

                        /**
                         *  ????????????????????????
                         */
                        viewHolder?.setGone(R.id.rl_google_layout, true)


                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                    ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                } else {
                                    googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                            ?: ""
                                }

                                listener.returnCode(googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**2
         * bind??????dialog
         *
         */
        fun showSecurityForBindPhoneCodeDialog(context: Context,
                                               codeType: Int,
                                               listener: DialogVerifiactionListener
        ): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                        viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         *  ??????????????????
                         */

                        viewHolder?.setGone(R.id.rl_phone_layout, true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)


                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                    }
                                }

                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()

                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????????????????dialog
         *
         */
        fun showSecurityForBindPhoneDialog(context: Context,
                                           codeType: Int,
                                           newphone: String,
                                           countryCode: String = "",
                                           listener: DialogVerifiactionListener
        ): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_phone_title, newphone)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         * ?????????
                         */
                        viewHolder?.setGone(R.id.rl_mail_layout, false)


                        /**
                         *  ?????????
                         */
                        viewHolder?.setGone(R.id.rl_phone_layout, true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setAccount(newphone)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setCountry(countryCode)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE, true, newphone)



                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                    ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                } else {
                                    phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                            ?: ""
                                }
                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()

                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????????????????dialog
         *
         */
        fun showSecurityForChangePhoneDialog(context: Context,
                                             codeType: Int,
                                             newphone: String,
                                             countryCode: String = "",
                                             listener: DialogVerifiactionListener
        ): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_phone_title, newphone)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         * ?????????
                         */

                        if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                            viewHolder?.setGone(R.id.rl_mail_layout, true)
                            val cvMail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)
                            cvMail?.otypeForPhone = codeType
                            cvMail?.setType(ComVerifyView.MOBILE)
                            cvMail?.sendVerify(ComVerifyView.MOBILE)
                        } else {
                            viewHolder?.setGone(R.id.rl_phone_layout, false)
                            val cvMail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)
                            cvMail?.otypeForPhone = codeType
                            cvMail?.setType(ComVerifyView.EMAIL)
                            cvMail?.sendVerify(ComVerifyView.EMAIL)
                        }
                        /**
                         *  ?????????
                         */
                        viewHolder?.setGone(R.id.rl_phone_layout, true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setAccount(newphone)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setCountry(countryCode)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE, false, newphone)

                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                    }
                                }
                                if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {

                                } else {
                                    mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                            ?: ""
                                }

                                listener.returnCode(phone, mail, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()

                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????????????????dialog
         *
         */
        fun showSecurityForBindEmailDialog(context: Context,
                                           codeType: Int,
                                           newEmail: String,
                                           countryCode: String = "",
                                           listener: DialogReturnChangeEmail
        ): TDialog {
            var phone = ""
            var newEmailcode = ""
            var oldEmail = ""
            var googleCode = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.rl_new_email_layout, true)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_phone_title, newEmail)
                        viewHolder?.setText(R.id.tv_new_email_title, UserDataService.getInstance().email)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         * ??????
                         */
                        if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                            viewHolder?.setGone(R.id.rl_mail_layout, true)
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForPhone = codeType
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.MOBILE)
                        } else {
                            viewHolder?.setGone(R.id.rl_mail_layout, false)
                        }


                        /**
                         * ?????????
                         */
                        viewHolder?.setGone(R.id.rl_new_email_layout, true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_new_email)?.otypeForEmail = codeType
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_new_email)?.setValidation(false)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_new_email)?.sendVerify(ComVerifyView.EMAIL)

                        /**
                         *  ?????????
                         */
                        viewHolder?.setGone(R.id.rl_phone_layout, true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForEmail = codeType
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setAccount(newEmail)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setCountry(countryCode)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.setValidation(true)
                        viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.EMAIL, true, newEmail)


                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                ?: ""
                                    }
                                }
                                if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {

                                } else {
                                    newEmailcode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                            ?: ""
                                }
                                if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_new_email)?.code)) {

                                } else {
                                    oldEmail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_new_email)?.code
                                            ?: ""
                                }

                                listener.returnCode(phone, oldEmail, newEmailcode, googleCode)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()

                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????
         */
        fun showSecurityForSecondDialog(context: Context,
                                        codeType: Int,
                                        listener: DialogSecondListener,
                                        type: Int = 0,
                                        loginPwdShow: Boolean,
                                        confirmTitle: String = "",
                                        codeType4Email: Int = -1): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            var pwd = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, true)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                        viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setGone(R.id.rl_pwd_layout, loginPwdShow)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        if (TextUtils.isEmpty(confirmTitle)) {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        } else {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(confirmTitle)
                        }

                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         *  ??????????????????
                         */
                        if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                            viewHolder?.setGone(R.id.rl_phone_layout, true)
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)

                        } else {
                            viewHolder?.setGone(R.id.rl_phone_layout, false)

                        }
                        /**
                         * ??????????????????
                         */
                        if (type == 0) {
                            if (!TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                                if (codeType4Email != -1) {
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType4Email
                                } else {
                                    viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                }
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL)

                            } else {
                                viewHolder?.setGone(R.id.rl_mail_layout, false)
                            }
                        } else {
                            viewHolder?.setGone(R.id.rl_mail_layout, false)
                        }
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                        return
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                        return
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                    }
                                }
                                if (loginPwdShow && TextUtils.isEmpty(viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text)) {
                                    ToastUtils.showToast(LanguageUtil.getString(context, "register_tip_inputPassword"))
                                    return
                                } else {
                                    pwd = viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text
                                            ?: ""
                                }
                                if (type == 0) {
                                    if (TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {
                                        } else {
                                            mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                    ?: ""
                                        }
                                    }
                                }
                                listener.returnCode(phone, mail, googleCode, pwd)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {

                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????
         */
        fun showCertificationSecurityForSecondDialog(context: Context,
                                                     codeType: Int,
                                                     listener: DialogCertificationSecondListener,
                                                     type: Int = 0,
                                                     loginPwdShow: Boolean,
                                                     confirm: String = ""): TDialog {
            var phone = ""
            var mail = ""
            var googleCode = ""
            var pwd = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.addFocusList(object : PwdSettingView.FocusChangeListener {
                            override fun focusChange(status: Boolean) {
                                viewHolder?.getView<View>(R.id.v_line_login_pwd)?.setBackgroundResource(if (status) R.color.main_blue else R.color.new_edit_line_color)
                            }
                        })

                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, true)
                        viewHolder?.setText(R.id.tv_mail_title, UserDataService.getInstance().email)
                        viewHolder?.setText(R.id.tv_phone_title, UserDataService.getInstance().mobileNumber)
                        viewHolder?.setText(R.id.tv_google_title, LanguageUtil.getString(context, "personal_text_googleCode"))
                        viewHolder?.setText(R.id.tv_pwd_title, LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.setHintEditText(LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.setGone(R.id.rl_pwd_layout, loginPwdShow)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        if (TextUtils.isEmpty(confirm)) {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        } else {
                            viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(confirm)
                        }
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        /**
                         *  ????????????????????????
                         */
                        if (UserDataService.getInstance().googleStatus == 1) {
                            viewHolder?.setGone(R.id.rl_google_layout, true)
                        } else {
                            viewHolder?.setGone(R.id.rl_google_layout, false)
                        }
                        /**
                         *  ??????????????????
                         */
                        if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                            viewHolder?.setGone(R.id.rl_phone_layout, true)
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.otypeForPhone = codeType
                            viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.sendVerify(ComVerifyView.MOBILE)

                        } else {
                            viewHolder?.setGone(R.id.rl_phone_layout, false)

                        }
                        /**
                         * ??????????????????
                         */
                        if (type == 0) {
                            if (!TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                viewHolder?.setGone(R.id.rl_mail_layout, true)
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.otypeForEmail = codeType
                                viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.sendVerify(ComVerifyView.EMAIL)

                            } else {
                                viewHolder?.setGone(R.id.rl_mail_layout, false)
                            }
                        } else {
                            viewHolder?.setGone(R.id.rl_mail_layout, false)
                        }
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (UserDataService.getInstance().googleStatus == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "hint_google_certification_code"))
                                        return
                                    } else {
                                        googleCode = viewHolder?.getView<ComVerifyView>(R.id.cv_content_google)?.code
                                                ?: ""
                                    }
                                }
                                if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                                    if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code)) {
                                        ToastUtils.showToast(LanguageUtil.getString(context, "toast_no_mobile_verification_code"))
                                        return
                                    } else {
                                        phone = viewHolder?.getView<ComVerifyView>(R.id.cv_content_phone)?.code
                                                ?: ""
                                    }
                                }
                                if (loginPwdShow && TextUtils.isEmpty(viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text)) {
                                    ToastUtils.showToast(LanguageUtil.getString(context, "register_tip_inputPassword"))
                                    return
                                } else {
                                    pwd = viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text
                                            ?: ""
                                }
                                if (type == 0) {
                                    if (TextUtils.isEmpty(UserDataService.getInstance().email)) {
                                        if (TextUtils.isEmpty(viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code)) {
                                        } else {
                                            mail = viewHolder?.getView<ComVerifyView>(R.id.cv_content_mail)?.code
                                                    ?: ""
                                        }
                                    }
                                }
                                listener.returnCode(phone, mail, googleCode, pwd)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                if (null != listener) {
                                    listener.cancelBtn()
                                }
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ??????????????????dialog
         */
        fun showAloneEdittextDialog(context: Context,
                                    title: String,
                                    listener: DialogBottomAloneListener): TDialog {

            var nickName = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_title, title)
                        viewHolder?.setText(R.id.tv_pwd_title, LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.setHintEditText(LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setGone(R.id.ce_account, true)
                        var editText = viewHolder?.getView<CustomizeEditText>(R.id.ce_account)
                        editText?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")
                        var v_line = viewHolder?.getView<View>(R.id.v_line)
                        var button = viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)
                        button?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        editText?.isFocusable = true
                        editText?.isFocusableInTouchMode = true
                        button?.isEnable(false)
                        editText?.setOnFocusChangeListener { v, hasFocus ->
                            v_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
                        }

                        editText?.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {

                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                nickName = s.toString()
                                if (TextUtils.isEmpty(nickName)) {
                                    button?.isEnable(false)
                                } else {
                                    button?.isEnable(true)
                                }
                            }
                        })
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                listener.returnContent(nickName)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }
        /**
         * ??????????????????dialog
         */
        fun showAloneEdittextSignDialog(context: Context,
                                    title: String,
                                    listener: DialogBottomAloneListener): TDialog {

            var nickName = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter1)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_title, title)
                        viewHolder?.setText(R.id.tv_pwd_title, LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.setHintEditText(LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, false)
                        viewHolder?.setGone(R.id.ce_account, true)
                        var editText = viewHolder?.getView<CustomizeEditText>(R.id.ce_account)
                        var v_line = viewHolder?.getView<View>(R.id.v_line)
                        var button = viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)
                        button?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        editText?.isFocusable = true
                        editText?.isFocusableInTouchMode = true
                        button?.isEnable(false)
                        editText?.setOnFocusChangeListener { v, hasFocus ->
                            v_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
                        }

                        editText?.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {

                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                nickName = s.toString()
                                if (TextUtils.isEmpty(nickName)) {
                                    button?.isEnable(false)
                                } else {
                                    button?.isEnable(true)
                                }
                            }
                        })
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                listener.returnContent(nickName)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????
         */
        fun showPwdEdittextDialog(context: Context,
                                  title: String,
                                  listener: DialogBottomPwdListener, content: String): TDialog {

            var pwd = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_security_verification_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setText(R.id.tv_title, title)
                        viewHolder?.setText(R.id.tv_pwd_title, LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.setHintEditText(LanguageUtil.getString(context, "register_text_loginPwd"))
                        viewHolder?.setText(R.id.tv_security_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.setContent(LanguageUtil.getString(context, "common_action_next"))
                        viewHolder?.getView<CustomizeEditText>(R.id.ce_account)?.hint = LanguageUtil.getString(context, "userinfo_tip_inputNickname")


                        viewHolder?.setGone(R.id.tv_pwd_title, false)
                        viewHolder?.setGone(R.id.rl_pwd_layout, true)
                        viewHolder?.setGone(R.id.rl_google_layout, false)
                        viewHolder?.setGone(R.id.rl_phone_layout, false)
                        viewHolder?.setGone(R.id.rl_mail_layout, false)
                        viewHolder?.setGone(R.id.ce_account, false)
                        viewHolder?.setGone(R.id.v_line, false)
                        viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.setHintEditText(content)

                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.isEnable(true)
                        viewHolder?.getView<CommonlyUsedButton>(R.id.tv_confirm)?.listener = object : CommonlyUsedButton.OnBottonListener {
                            override fun bottonOnClick() {
                                if (TextUtils.isEmpty(viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text)) {
                                    ToastUtils.showToast(LanguageUtil.getString(context, "register_tip_inputPassword"))

                                    return
                                } else {
                                    pwd = viewHolder?.getView<PwdSettingView>(R.id.pwd_login_pwd)?.text
                                            ?: ""
                                }
                                listener.returnContent(pwd)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel, R.id.tv_confirm)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                listener.returnCancel()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????????????????????????????
         */

        fun OTCOorderContactDialog(context: Context, numberContent: String, emailContent: String, listener: DialogBottomListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_contact_buy_or_sell_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        if (numberContent.isEmpty()) {
                            viewHolder?.setGone(R.id.tv_number_layout, false)
                        }
                        if (emailContent.isEmpty()) {
                            viewHolder?.setGone(R.id.tv_email_layout, false)
                        }
                        viewHolder?.setText(R.id.tv_contact, LanguageUtil.getString(context, "common_text_contactTitle"))
                        viewHolder?.setText(R.id.tv_number_title, LanguageUtil.getString(context, "personal_text_phoneNumber"))
                        viewHolder?.setText(R.id.tv_email_title, LanguageUtil.getString(context, "register_text_mail"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_number_content, numberContent)
                        viewHolder?.setText(R.id.tv_email_content, emailContent)
                        viewHolder?.getView<TextView>(R.id.tv_number_content)?.setOnClickListener {
                            ScreenShotUtil.diallPhone(context, numberContent)
                            if (listener != null) {
                                listener.sendConfirm()
                            }
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
         * otc ????????????
         * ??????
         */

        fun OTCTradingPermissionsDialog(context: Context,
                                        listener: DialogBottomListener,
                                        type: Int = 1,
                                        title: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_otc_trading_trading_permissions_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "common_text_tip"))
                        viewHolder?.setText(R.id.tv_nickname, LanguageUtil.getString(context, "otcSafeAlert_action_nickname"))
                        viewHolder?.setText(R.id.tv_phone, LanguageUtil.getString(context, "title_bind_phone"))
                        viewHolder?.setText(R.id.tv_google, LanguageUtil.getString(context, "otcSafeAlert_action_bindphoneOrGoogle"))
                        viewHolder?.setText(R.id.tv_realname_certification, LanguageUtil.getString(context, "otcSafeAlert_action_identify"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))


                        if (type != 1) {
                            viewHolder?.setGone(R.id.ll_nick_layout, false)
                            if (type != -1) {
                                viewHolder?.setGone(R.id.ll_trading_real_layout, false)
                            }
                        }
                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setText(R.id.tv_trading_content, title)
                        } else {
                            val currencyTypeTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title_forotc")
                            } else {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title")
                            }
                            viewHolder?.setText(R.id.tv_trading_content, currencyTypeTitle)
                        }

                        if (TextUtils.isEmpty(UserDataService.getInstance().nickName)) {
                            viewHolder?.getView<ImageView>(R.id.iv_nickname)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_nickname)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_nickname)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_nickname)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }
                        if (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ??????????????? ??????
         * ??????
         */

        fun OTCTradingMustPermissionsDialog(context: Context,
                                            listener: DialogBottomListener,
                                            type: Int = 1,
                                            title: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_validation_must_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        if (type != 1) {
                            viewHolder?.setGone(R.id.ll_nick_layout, false)
                            if (type != -1) {
                                viewHolder?.setGone(R.id.ll_real_layout, false)
                            }
                        }
                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "common_text_tip"))
                        viewHolder?.setText(R.id.tv_nickname, LanguageUtil.getString(context, "otcSafeAlert_action_nickname"))
                        viewHolder?.setText(R.id.tv_google, LanguageUtil.getString(context, "otcSafeAlert_action_bindGoogle"))
                        viewHolder?.setText(R.id.tv_realname_certification, LanguageUtil.getString(context, "otcSafeAlert_action_identify"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))
                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setText(R.id.tv_validation_content, title)
                        } else {
                            val currencyTypeTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title_forotc")
                            } else {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title")
                            }
                            viewHolder?.setText(R.id.tv_validation_content, currencyTypeTitle)
                        }

                        if (TextUtils.isEmpty(UserDataService.getInstance().nickName)) {
                            viewHolder?.getView<ImageView>(R.id.iv_nickname)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_nickname)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_nickname)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_nickname)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                        if (UserDataService.getInstance().googleStatus != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????????????????
         */
        fun OTCTradingOnlyPermissionsDialog(context: Context,
                                            listener: DialogBottomListener,
                                            title: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_validation_must_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setGone(R.id.ll_nick_layout, false)
                        viewHolder?.setGone(R.id.ll_google_layout, false)
                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "common_text_tip"))
                        viewHolder?.setText(R.id.tv_nickname, LanguageUtil.getString(context, "otcSafeAlert_action_nickname"))
                        viewHolder?.setText(R.id.tv_google, LanguageUtil.getString(context, "otcSafeAlert_action_bindGoogle"))
                        viewHolder?.setText(R.id.tv_realname_certification, LanguageUtil.getString(context, "otcSafeAlert_action_identify"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))

                        if (!TextUtils.isEmpty(title)) {
                            viewHolder?.setText(R.id.tv_validation_content, title)
                        } else {
                            val currencyTypeTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title_forotc")
                            } else {
                                LanguageUtil.getString(context, "otcSafeAlert_text_title")
                            }
                            viewHolder?.setText(R.id.tv_validation_content, currencyTypeTitle)
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ?????????????????????
         */
        fun adjustDepositDialog(context: Context, jsonObject: JSONObject, listener: DialogBottomAloneListener) {
            val side = jsonObject.optString("side")
            val volume = jsonObject.optString("volume")
            val usedMargin = jsonObject.optString("usedMargin")
            val canUseMargin = jsonObject.optString("canUseMargin")
            var isAdd = true

            /**
             * ???????????????
             */
            var depositAmount = ""

            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_adjust_deposit)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.rb_add_deposit, LanguageUtil.getString(context, "contract_action_increaseMargin"))
                        viewHolder?.setText(R.id.btn_adjust_deposit, LanguageUtil.getString(context, "common_text_btnConfirm"))
                        viewHolder?.setText(R.id.rb_sub_deposit, LanguageUtil.getString(context, "contract_action_decreaseMargin"))
                        viewHolder?.setText(R.id.tv_deposit_title, LanguageUtil.getString(context, "contract_text_increaseMarginVolume"))
                        viewHolder?.getView<EditText>(R.id.et_deposit_amount)?.hint = LanguageUtil.getString(context, "contract_text_increaseMarginVolume")
                        viewHolder?.getView<PositionITemView>(R.id.pit_position_amount)?.setHeadTitle(LanguageUtil.getString(context, "position_amount"))
                        viewHolder?.getView<PositionITemView>(R.id.pit_allocated_deposit)?.setHeadTitle(LanguageUtil.getString(context, "contract_allocated_margin"))
                        viewHolder?.getView<PositionITemView>(R.id.pit_available_deposit)?.setHeadTitle(LanguageUtil.getString(context, "contract_distributable_security"))
                        viewHolder?.getView<RadioGroup>(R.id.rg_deposit)?.setOnCheckedChangeListener { group, checkedId ->
                            when (checkedId) {
                                R.id.rb_add_deposit -> {
                                    viewHolder.getView<EditText>(R.id.et_deposit_amount).hint = LanguageUtil.getString(context, "contract_text_increaseMarginVolume")
                                    viewHolder.getView<TextView>(R.id.tv_deposit_title).text = LanguageUtil.getString(context, "contract_text_increaseMarginVolume")
                                    isAdd = true
                                }

                                R.id.rb_sub_deposit -> {
                                    viewHolder.getView<EditText>(R.id.et_deposit_amount).hint = LanguageUtil.getString(context, "contract_text_decreaseMarginVolume")
                                    viewHolder.getView<TextView>(R.id.tv_deposit_title).text = LanguageUtil.getString(context, "contract_text_decreaseMarginVolume")
                                    isAdd = false
                                }
                            }
                        }

                        /**
                         * ?????????????????????
                         */
                        val pit_position_amount = viewHolder?.getView<PositionITemView>(R.id.pit_position_amount)
                        pit_position_amount?.tailValueColor = ColorUtil.getMainColorType(side == "BUY")
                        if (side == "BUY") {
                            pit_position_amount?.value = "+${volume}"
                        } else {
                            pit_position_amount?.value = "-${volume}"
                        }

                        /**
                         * ??????????????????
                         */
                        viewHolder?.getView<PositionITemView>(R.id.pit_allocated_deposit)?.run {
                            val usedMargin = Contract2PublicInfoManager.cutDespoitByPrecision(usedMargin)
                            value = usedMargin
                            title = LanguageUtil.getString(context, "contract_allocated_margin") + " (BTC)"
                        }

                        /**
                         * ???????????????
                         */
                        viewHolder?.getView<PositionITemView>(R.id.pit_available_deposit)?.run {
                            val canUseMargin = Contract2PublicInfoManager.cutDespoitByPrecision(canUseMargin)
                            value = canUseMargin
                            title = LanguageUtil.getString(context, "contract_text_availableMargin") + "(BTC)"
                        }

                        viewHolder?.getView<TextView>(R.id.tv_base_symbol)?.text = "BTC"

                        val btn_adjust_deposit = viewHolder?.getView<TextView>(R.id.btn_adjust_deposit)

                        val et_deposit_amount = viewHolder?.getView<EditText>(R.id.et_deposit_amount)
                        et_deposit_amount?.filters = arrayOf(DecimalDigitsInputFilter(Contract2PublicInfoManager.getCoinByName("btc")?.showPrecision
                                ?: 8))

                        et_deposit_amount?.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                depositAmount = s.toString()
                                btn_adjust_deposit?.isEnabled = !TextUtils.isEmpty(depositAmount)
                                if (TextUtils.isEmpty(depositAmount)) {
                                    btn_adjust_deposit?.backgroundColorResource = R.color.normal_text_color
                                } else {
                                    btn_adjust_deposit?.backgroundColorResource = R.color.main_blue
                                }
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            }

                        })


                    }
                    .addOnClickListener(R.id.tv_cancel, R.id.btn_adjust_deposit)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.btn_adjust_deposit -> {
                                val activity = context as Activity
                                if (TextUtils.isEmpty(depositAmount)) return@setOnViewClickListener
                                if (depositAmount.toDouble() == 0.0) {
                                    tDialog.dismiss()
                                    NToastUtil.showTopToastNet(activity, false, LanguageUtil.getString(context, "transfer_tip_emptyVolume"))

                                    return@setOnViewClickListener
                                }

//                                Log.d("==?????????===", "======depositAmount:${depositAmount.toDouble()},canUseMargin${position.canUseMargin}===")
                                if (isAdd && (depositAmount.toDouble() > canUseMargin.toDoubleOrNull() ?: 0.0)) {
                                    tDialog.dismiss()
                                    NToastUtil.showTopToastNet(activity, false, LanguageUtil.getString(context, "contract_tip_volumeError"))
                                    return@setOnViewClickListener
                                }
                                var value = if (isAdd) depositAmount else "-$depositAmount"
                                listener.returnContent(value)
                                tDialog.activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                                tDialog.dismiss()
                                // ????????????
                                val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                Log.d("=isActive=", "=======${inputManager.isActive}===========")
                                inputManager.hideSoftInputFromWindow(context.window?.decorView?.windowToken, 0)
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * kyc??????  dialog
         */
        fun KycSecurityDialog(context: Context, contentTitle: String, listener: DialogBottomListener) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_otc_trading_security_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        val tipsTitle = contentTitle

                        viewHolder?.setText(R.id.tv_tips_title, tipsTitle)

                        viewHolder?.setText(R.id.tv_money_password, context.getString(R.string.otcSafeAlert_action_identify))
                        viewHolder?.getView<ImageView>(R.id.iv_money_password)?.setImageResource(R.drawable.fiat_unfinished)
                        viewHolder?.getView<TextView>(R.id.tv_money_password)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))


                        viewHolder?.setGone(R.id.rl_collect_money_layout, false)

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * otc ????????????
         * ??????
         */
        fun OTCTradingSecurityDialog(context: Context,
                                     listener: DialogBottomListener,
                                     paymentStatus: Boolean
        ) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_otc_trading_security_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        val tipsTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                            LanguageUtil.getString(context, "otcSafeAlert_text_settingDesc_forotc")
                        } else {
                            LanguageUtil.getString(context, "otcSafeAlert_text_settingDesc")
                        }

                        viewHolder?.setText(R.id.tv_tips_title, tipsTitle)
                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "common_text_tip"))
                        viewHolder?.setText(R.id.tv_money_password, LanguageUtil.getString(context, "safety_action_otcPassword"))
                        viewHolder?.setText(R.id.tv_collect_money, LanguageUtil.getString(context, "noun_order_paymentTerm"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))

                        if (UserDataService.getInstance().isCapitalPwordSet != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_money_password)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_money_password)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_money_password)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_money_password)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }
                        if (UserDataService.getInstance().isCapitalPwordSet == 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_collect_money)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_collect_money)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else if (!paymentStatus) {
                            viewHolder?.getView<ImageView>(R.id.iv_collect_money)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_collect_money)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_collect_money)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_collect_money)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }


                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }



        /**
         * otc ????????????
         * ??????
         */

        fun OTCTradingNickeSecurityDialog(context: Context,
                                          listener: DialogBottomListener) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_otc_trading_security_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        val tipsTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                            LanguageUtil.getString(context, "otcSafeAlert_text_settingDesc_forotc")
                        } else {
                            LanguageUtil.getString(context, "otcSafeAlert_text_settingDesc")
                        }

                        viewHolder?.setText(R.id.tv_tips_title, tipsTitle)
                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "common_text_tip"))

                        viewHolder?.setText(R.id.tv_money_password, LanguageUtil.getString(context, "nickname"))
                        viewHolder?.setText(R.id.tv_collect_money, LanguageUtil.getString(context, "otcSafeAlert_action_identify"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))
                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            viewHolder?.getView<ImageView>(R.id.iv_money_password)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_money_password)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_money_password)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_money_password)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }
                        if (UserDataService.getInstance().authLevel != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_collect_money)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_collect_money)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_collect_money)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_collect_money)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }


                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ?????? ????????????
         */
        fun tradingOTCConfirm(context: Context, title: String, payment: String, accountName: String, peyAmount: String, listener: DialogBottomListener, rightString: String = "", buyOrSell: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_confirm_payment)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        if (rightString.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_confirm, rightString)
                        } else {
                            viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "common_text_btnConfirm"))

                        }
                        viewHolder?.setText(R.id.tv_payment_type, payment)
                        viewHolder?.setText(R.id.tv_title, title)
                        viewHolder?.setText(R.id.tv_confirm_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))

                        viewHolder?.setText(R.id.tv_account_name, accountName)
                        viewHolder?.setText(R.id.tv_payment_amount, peyAmount)
                        if (buyOrSell) {
                            viewHolder?.setText(R.id.tv_content, LanguageUtil.getString(context, "otc_tip_remindBuyerClickDidPay"))
                            viewHolder?.setText(R.id.tv_1_content, LanguageUtil.getString(context, "noun_order_paymentTerm"))
                            viewHolder?.setText(R.id.tv_2_content, LanguageUtil.getString(context, "otc_text_payee"))
                            viewHolder?.setText(R.id.tv_3_content, LanguageUtil.getString(context, "otc_text_paymentAmount"))
                            viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "common_text_btnConfirm"))

                        } else {
                            viewHolder?.setText(R.id.tv_content, LanguageUtil.getString(context, "otc_tip_remindSellerSendCoin"))
                            viewHolder?.setText(R.id.tv_1_content, LanguageUtil.getString(context, "common_text_paymentTypeBuyer"))
                            viewHolder?.setText(R.id.tv_2_content, LanguageUtil.getString(context, "otc_text_payee"))
                            viewHolder?.setText(R.id.tv_3_content, LanguageUtil.getString(context, "journalAccount_text_amount"))
                            viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "otc_action_confirmSendCoin"))
                        }

                    }
                    .addOnClickListener(R.id.tv_confirm, R.id.tv_confirm_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_confirm_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_confirm -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ?????? ????????????
         */
        fun tradingOTCCancelOrder(context: Context, listener: DialogBottomListener) {
            var isTurelyChecked = false
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_cancel_order)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setText(R.id.tv_otc_action_cancel, LanguageUtil.getString(context, "otc_action_cancel"))
                        viewHolder?.setText(R.id.tv_oct_tip_buyerCancel, LanguageUtil.getString(context, "oct_tip_buyerCancel"))
                        viewHolder?.setText(R.id.cb_pay, LanguageUtil.getString(context, "otc_tip_buyerCancelConfirm"))
                        viewHolder?.setText(R.id.tv_order_cancel, LanguageUtil.getString(context, "common_action_thinkAgain"))
                        viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "common_text_btnConfirm"))

                        viewHolder?.getView<CheckBox>(R.id.cb_pay)?.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked) {
                                isTurelyChecked = true
                                viewHolder.getView<TextView>(R.id.tv_confirm)?.setTextColor(ContextCompat.getColor(context, R.color.main_blue))
                            } else {
                                isTurelyChecked = false
                                viewHolder.getView<TextView>(R.id.tv_confirm)?.setTextColor(ContextCompat.getColor(context, R.color.normal_text_color))
                            }
                        }
                    }
                    .addOnClickListener(R.id.tv_confirm, R.id.tv_order_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_order_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_confirm -> {
                                if (isTurelyChecked) {
                                    listener.sendConfirm()
                                    tDialog.dismiss()
                                }

                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ???????????????????????????dialog
         */
        fun showSingleDialog(context: Context, content: String, listener: DialogBottomListener?, title: String = "", cancelTitle: String = "", isFormatHtml: Boolean = true) {
            showDialog(context, content, true, listener, title, cancelTitle, "", false, isFormatHtml)
        }

        fun showSingle2Dialog(context: Context, content: String, listener: DialogBottomListener, title: String = "", cancelTitle: String = "") {
            showDialog(context, content, true, listener, title, cancelTitle, "", true)
        }

        /**
         * ?????? ???????????????dialog
         */

        fun showNormalDialog(context: Context, content: String, listener: DialogBottomListener, title: String = "", cancelTitle: String = "", confirmTitle: String = "") {
            showDialog(context, content, false, listener, title, cancelTitle, confirmTitle)
        }

        /**
         * ????????????
         */
        fun showNormalTransferDialog(context: Context, content: String, listener: DialogTransferBottomListener, title: String = "", cancelTitle: String = "", confirmTitle: String = "") {
            showTransferDialog(context, content, false, listener, title, cancelTitle, confirmTitle)
        }


        /**
         * string list ??????dialog
         *
         */
        fun showBottomListDialog(context: Context, list: ArrayList<String>, position: Int, listener: DialogOnclickListener): TDialog {
            return showListDialog(context, list, position, listener)
        }

        /**
         * ???????????????????????????
         */
        fun showNewBottomListDialog(context: Context, list: ArrayList<TabInfo>, position: Int, listener: DialogOnItemClickListener): TDialog {
            return showNewListDialog(context, list, position, listener)
        }


        /**
         *  ?????????????????? ??????????????????
         *   @param type  -1 ???????????????
         */
        fun showSecurityDialog(context: Context, type: Int = -1, codeType: Int, listener: DialogVerifiactionListener, emailType: Int = -1, confirmTitle: String = ""): TDialog {
            return showSecurityVerificationDialog(context, type, codeType, listener, emailType, confirmTitle)
        }


        /**
         * ???????????? dialog
         */
        fun showPwdDialog(context: Context, title: String, listener: DialogBottomPwdListener, content: String = ""): TDialog {
            return showPwdEdittextDialog(context, title, listener, content)
        }

        /**
         *  ???????????????????????????????????????
         *  @param type  0 ?????? 1 ????????????????????????
         *
         */
        fun showBindDialog(context: Context, codeType: Int, listener: DialogVerifiactionListener, type: Int = 0, account: String = ""): TDialog {
            return showSecurityForBindDialog(context, codeType, listener, type)
        }

        /**
         * ??????????????????
         */
        fun showBindPhoneCodeDialog(context: Context, codeType: Int, listener: DialogVerifiactionListener): TDialog {
            return showSecurityForBindPhoneCodeDialog(context, codeType, listener)
        }

        /**
         * ??????????????????
         */
        fun showBindPhoneDialog(context: Context, codeType: Int, listener: DialogVerifiactionListener, account: String = "", countryCode: String = ""): TDialog {
            return showSecurityForBindPhoneDialog(context, codeType, account, countryCode, listener)
        }

        /**
         * ??????????????????
         */
        fun showChangePhoneDialog(context: Context, codeType: Int, listener: DialogVerifiactionListener, account: String = "", countryCode: String = ""): TDialog {
            return showSecurityForChangePhoneDialog(context, codeType, account, countryCode, listener)
        }


        /**
         * ????????????
         */
        fun showBindEmailDialog(context: Context, codeType: Int, listener: DialogReturnChangeEmail, account: String = "", countryCode: String = ""): TDialog {
            return showSecurityForBindEmailDialog(context, codeType, account, countryCode, listener)
        }


        /**
         *  ??????
         *  @param type  0 ?????? 1 ????????????????????????
         *
         */
        fun showAccountBindDialog(context: Context, account: String, type: Int = -1, codeType: Int, listener: DialogVerifiactionListener): TDialog {
            return showAccountDialog(context, type, account, codeType, listener)
        }

        /**
         * ????????????
         */
        fun showSecondDialog(context: Context, cointype: Int, listener: DialogSecondListener, type: Int = 1, loginPwdShow: Boolean = true, confirmTitle: String = "", cointype4Email: Int = -1): TDialog {
            return showSecurityForSecondDialog(context, cointype, listener, type, loginPwdShow, confirmTitle, cointype4Email)
        }

        fun showCertificationSecondDialog(context: Context, cointype: Int, listener: DialogCertificationSecondListener, type: Int = 1, loginPwdShow: Boolean = true, confirmTitle: String = ""): TDialog {
            return showCertificationSecurityForSecondDialog(context, cointype, listener, type, loginPwdShow, confirmTitle)
        }


        /**
         * ?????????google??????
         */
        fun showOnlyGoogleDialog(context: Context, listener: DialogValidationGoogleListener): TDialog {
            return showValidationGoogleDialog(context, listener)
        }

        /**
         * ??????????????????
         */
        fun selectSymbol4RedPackage(context: Context, data: ArrayList<RedPackageInitInfo.Symbol?>, listener: DialogOnItemClickListener) {
            var wheelView: WheelView<RedPackageInitInfo.Symbol>? = null
            var index = 0

            for (i in 0..3) {
                val redPackageInitInfo = RedPackageInitInfo.Symbol()
                redPackageInitInfo.amount = "-1"
                data.add(redPackageInitInfo)
            }

            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_wheel_red_package)
                    .setScreenWidthAspect(context, 1f)
                    .setScreenHeightAspect(context, 0.3f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        wheelView = viewHolder?.getView<WheelView<RedPackageInitInfo.Symbol>>(R.id.wheelview)
                        wheelView?.backgroundColorResource = R.color.bg_card_color
                        wheelView?.setWheelAdapter(WheelViewAdapter(context))
                        viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "redpacket_payment_confirm"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "redpacket_payment_cancel"))
                        val style = WheelView.WheelViewStyle()
                        style.selectedTextColor = ContextCompat.getColor(context, R.color.text_color)
                        style.textColor = ContextCompat.getColor(context, R.color.normal_text_color)
                        style.holoBorderColor = ContextCompat.getColor(context, R.color.line_color)
                        style.backgroundColor = ContextCompat.getColor(context, R.color.bg_card_color)
                        wheelView?.skin = WheelView.Skin.Holo
                        wheelView?.setWheelData(data)

                        wheelView?.style = style
                        wheelView?.invalidate()
//                        wheelView?.setLoop(true)
                        wheelView?.setOnWheelItemSelectedListener { position, t ->
                            index = position
                        }

                    }.addOnClickListener(R.id.tv_cancel, R.id.tv_confirm)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_confirm -> {
                                Log.d("XX", "===XXX====${index},${wheelView?.selectedItemPosition}======")
                                val index = if (index < 0) {
                                    0
                                } else {
                                    index
                                }
                                listener.clickItem(index)
                                tDialog?.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ?????????????????????
         */
        fun order4RedPackage(context: Context, bean: TempBean, listener: DialogBottomListener) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_create_red_package)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        var tv_money = viewHolder?.getView<TextView>(R.id.tv_money)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "redpacket_payment_payment"))
                        viewHolder?.setText(R.id.tv_money_title, LanguageUtil.getString(context, "redpacket_payment_amount"))
                        viewHolder?.setText(R.id.tv_money_type_title, LanguageUtil.getString(context, "redpacket_payment_type"))
                        viewHolder?.setText(R.id.tv_account_title, LanguageUtil.getString(context, "redpacket_payment_account"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "redpacket_payment_cancel"))
                        viewHolder?.setText(R.id.tv_confirm, LanguageUtil.getString(context, "redpacket_payment_confirm"))



                        tv_money?.text = "${bean.money} ${NCoinManager.getShowMarket(bean.coin)}"

                        var tv_account = viewHolder?.getView<TextView>(R.id.tv_account)
                    }.addOnClickListener(R.id.tv_cancel, R.id.tv_confirm)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_confirm -> {
                                listener.sendConfirm()
                                tDialog?.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????
         */
        fun share4RedPackage(context: Context, bean: CreatePackageBean?, isDirectShare: Boolean = false) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_red_package)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        var iv_logo = viewHolder?.getView<ImageView>(R.id.iv_logo)
                        var logoBean = PublicInfoDataService.getInstance().getApp_logo_list_new(null)
                        if (null != logoBean && logoBean.size >= 2) {
                            if (!TextUtils.isEmpty(logoBean[1])) {
                                GlideUtils.loadImageHeader(context, logoBean[0], iv_logo)
                            }
                        }

                        viewHolder?.setText(R.id.tv_redpacket_send_longPress, LanguageUtil.getString(context, "redpacket_send_longPress"))
                        var iv_qr_code = viewHolder?.getView<ImageView>(R.id.iv_qr_code)

                        /**
                         * ???????????????
                         */
                        if (!TextUtils.isEmpty(bean?.shareUrl)) {
                            iv_qr_code?.setImageBitmap(BitmapUtils.generateBitmap(bean?.shareUrl, 300, 300))
                        }

                        var tv_coin = viewHolder?.getView<TextView>(R.id.tv_coin)
                        if (bean?.coinSymbol?.length ?: 0 > 4) {
                            tv_coin?.textSize = 12f
                        } else {
                            tv_coin?.textSize = 16f
                        }
                        tv_coin?.text = NCoinManager.getShowMarket(bean?.coinSymbol).toString().toUpperCase()


                        val tv_red_package_owner = viewHolder?.getView<TextView>(R.id.tv_red_package_owner)

                        tv_red_package_owner?.text = LanguageUtil.getString(context, "redpacket_send_from").format(bean?.nickName)

                        val ly_red_package = viewHolder?.getView<ConstraintLayout>(R.id.ly_red_package)
                        if (SystemUtils.isZh()) {
                            ly_red_package?.backgroundResource = R.drawable.bg_red_package
                        } else {
                            ly_red_package?.backgroundResource = R.drawable.background_english
                        }
                        if (context != null) {
                            Glide.with(context).load(bean?.background).into(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    ly_red_package?.backgroundDrawable = resource
                                }
                            })
                        }

                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                val screenshotBitmap = ScreenShotUtil.getScreenshotBitmap(ly_red_package
                                        ?: return)

                                ShareToolUtil.sendLocalShare(context, screenshotBitmap)
//                                var imgName = "${System.currentTimeMillis()}${context.packageName}.jpg"
//                                if (screenshotBitmap != null) {
//                                    val path = ImageTools.saveBitmap(screenshotBitmap, imgName)
//                                    ImageTools.insertImageToSystem(context, path, imgName)
//                                    val shareImageIntent = Intent(Intent.ACTION_SEND)
//                                    shareImageIntent.type = "image/*"
//                                    shareImageIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
//                                    context.startActivity(Intent.createChooser(shareImageIntent, LanguageUtil.getString(context, "contract_share_label")))
//
//                                }
                            }

                        }, if (isDirectShare) 200 else 2500)
                    }
                    .create()
                    .show()
        }

        /**
         * ??????????????????
         */
        fun redPackageConditionDialog(context: Context) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_red_package_condition)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        /**
                         * ????????????
                         *   ???????????? 0???????????????1????????????2????????????  3?????????
                         */
                        if (UserDataService.getInstance().authLevel != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_realname_certification)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_realname_certification)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                        /**
                         * ??????Google
                         */
                        if (UserDataService.getInstance().googleStatus != 1 && UserDataService.getInstance().isOpenMobileCheck != 1) {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_unfinished)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        } else {
                            viewHolder?.getView<ImageView>(R.id.iv_google)?.setImageResource(R.drawable.fiat_complete)
                            viewHolder?.getView<TextView>(R.id.tv_google)?.setTextColor(ColorUtil.getColor(R.color.main_blue))
                        }

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                tDialog.dismiss()
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        //RealNameCertificaionSuccessActivity.enter(context)
                                        ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                        return@setOnViewClickListener
                                    }
                                    2, 3 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                        return@setOnViewClickListener
                                    }
                                }

                                if (UserDataService.getInstance().googleStatus != 1 || UserDataService.getInstance().isOpenMobileCheck != 1) {
                                    ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                                    return@setOnViewClickListener
                                }
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ????????????
         */
        fun closePositionByLimit(context: Context, quoteSymbol: String, listener: DialogBottomAloneListener) {
            var mount = 0L
            var price = "0.0"
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_limit_close_position)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.getView<TextView>(R.id.tv_coin)?.text = quoteSymbol

                        val btn_close_position = viewHolder?.getView<Button>(R.id.btn_close_position)?.apply {
                            backgroundColorResource = R.color.normal_text_color
                        }
                        viewHolder?.run {
                            setText(R.id.tv_contract_limit_balance, LanguageUtil.getString(context, "contract_text_limitPositions"))
                            setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                            setText(R.id.tv_contract_text_price, LanguageUtil.getString(context, "contract_text_price"))
                            setText(R.id.btn_close_position, LanguageUtil.getString(context, "common_text_btnConfirm"))

                        }

                        viewHolder?.getView<CustomizeEditText>(R.id.et_price)?.hint = LanguageUtil.getString(context, "contract_tip_pleaseInputPrice")

                        viewHolder?.getView<EditText>(R.id.et_price)?.apply {
                            filters = arrayOf(DecimalDigitsInputFilter(Contract2PublicInfoManager.getCoinByName(quoteSymbol)?.showPrecision
                                    ?: 8))

                            addTextChangedListener(object : TextWatcher {
                                override fun afterTextChanged(s: Editable?) {
                                    if (isEnable(this@apply)) {
                                        btn_close_position?.run {
                                            backgroundColorResource = R.color.main_blue
                                            isEnabled = true
                                            price = s.toString()
                                        }
                                    } else {
                                        btn_close_position?.run {
                                            backgroundColorResource = R.color.normal_text_color
                                            isEnabled = false
                                        }
                                    }
                                }

                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                }

                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                }

                            })
                        }


//                        et_mount?.addTextChangedListener(object : TextWatcher {
//                            override fun afterTextChanged(s: Editable?) {
//                                if (isEnable(et_price) && isEnable(et_mount)) {
//                                    mount = s.toString().toLong()
//                                } else {
//                                    btn_close_position?.isEnable(false)
//                                }
//                            }
//
//                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                            }
//
//                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                            }
//
//                        })

                    }
                    .addOnClickListener(R.id.tv_cancel, R.id.btn_close_position)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.btn_close_position -> {
                                if (TextUtils.isEmpty(price) || price.toDouble() == 0.0) {
                                    return@setOnViewClickListener
                                }
//                                if (mount > position?.volume?.toLong() ?: 0) {
//                                    tDialog.dismiss()
//                                    DisplayUtil.showSnackBar(context.window.decorView, "??????????????????????????????" + position?.volume, isSuc = false)
//                                    return@setOnViewClickListener
//                                }


                                /**
                                 * ???????????????&???????????????/??????????????????
                                 */
                                listener.returnContent(price + "/$mount")
                                tDialog.activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                                tDialog.dismiss()
                                // ????????????
                                val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                Log.d("=isActive=", "=======${inputManager.isActive}===========")
                                inputManager.hideSoftInputFromWindow(context.window?.decorView?.windowToken, 0)
                            }
                        }
                    }
                    .create()
                    .show()
        }

        fun isEnable(editText: EditText?): Boolean {
            val string = editText?.text.toString()
            return !(TextUtils.isEmpty(string) || string.toDouble() == 0.0)
        }


        fun redPackageCondition(context: Context) {
            OTCTradingPermissionsDialog(context, object : DialogBottomListener {
                override fun sendConfirm() {
                    val isEnforceGoogleAuth = PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)
                    val googleStatus = UserDataService.getInstance().googleStatus
                    val isOpenMobileCheck = UserDataService.getInstance().isOpenMobileCheck

                    if (isEnforceGoogleAuth) {
                        if (googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        } else {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        }
                    } else {
                        if ((googleStatus != 1 && isOpenMobileCheck != 1)) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        } else {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        }
                    }
                }
            }, -1, LanguageUtil.getString(context, "redpacket_click_prompt"))
        }

        /**
         * TODO dialog_recharge_b2c
         */
        fun confirmRecharge(context: Context, symbol: String, amount: String, imgUrl: String, listener: DialogBottomListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_recharge_b2c)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.run {
                            /* ???????????? */
                            setText(R.id.tv_amount, BigDecimalUtils.showNormal(amount) + " $symbol")

                            setText(R.id.tv_title, LanguageUtil.getString(context, "b2c_text_rechargeConfirm"))
                            setText(R.id.tv_tips, LanguageUtil.getString(context, "b2c_text_rechargeNote"))
                            setText(R.id.tv_amount_title, LanguageUtil.getString(context, "otc_text_paymentAmount"))
                            setText(R.id.tv_purchase_voucher_title, LanguageUtil.getString(context, "b2c_Transfer_Vouchers"))
                            setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                            setText(R.id.tv_confirm, LanguageUtil.getString(context, "b2c_text_confirmRecharge"))

                            Log.d("????????????", "======imageUrl:$imgUrl=====")
                            /* ???????????? */
                            GlideUtils.loadImageHeader(context, imgUrl, getView<ImageView>(R.id.iv_voucher))
                        }
                    }
                    .addOnClickListener(R.id.tv_confirm, R.id.tv_cancel)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                                ViewUtils.showSnackBar(context.window?.decorView, "????????????", false)
                            }
                            R.id.tv_confirm -> {
                                listener.sendConfirm()

                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * otc ??????????????????
         * ??????
         */
        fun activationPaymentMethodDialog(context: Context?, listener: DialogBottomListener, payments: JSONArray?) {
            if (null == context)
                return
            if (null == payments || payments.length() <= 0)
                return

            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_otc_activate_paymentmethod_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        var paymentMethodString = StringBuilder()
                        for (i in 0 until payments.length()) {
                            var title = payments.optJSONObject(i)?.optString("title")
                            if (!StringUtil.checkStr(title)) {
                                title = payments.optJSONObject(i)?.optString("key") ?: ""
                            }
                            paymentMethodString.append(title)
                            paymentMethodString.append(",")
                        }
                        viewHolder?.getView<TextView>(R.id.tv_tip)?.text = LanguageUtil.getString(context, "common_text_tip")
                        viewHolder?.getView<TextView>(R.id.tv_cancel)?.text = LanguageUtil.getString(context, "common_text_btnCancel")
                        viewHolder?.getView<TextView>(R.id.tv_goto_activation)?.text = LanguageUtil.getString(context, "otc_string_goActivate")
                        var methodString = paymentMethodString.toString().substring(0, paymentMethodString.toString().length - 1)
                        viewHolder?.getView<TextView>(R.id.tv_content)?.text = LanguageUtil.getString(context, "otc_string_buyerOnlyCan") + methodString + LanguageUtil.getString(context, "otc_string_youNeedDo")
                    }
                    .addOnClickListener(R.id.tv_goto_activation, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_activation -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * ????????????
         */
        fun leverAccountDialog(context: Context, symbol: String, jsonObject: JSONObject?, listener: DialogOnItemClickListener): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_layout_lever)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.TOP)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setDialogAnimationRes(R.style.dialogTopAnim)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.run {
                            val showName = NCoinManager.getMarketName4Symbol(symbol)
                            var quoteCoin = NCoinManager.getMarketCoinName(showName)
                            var baseCoin = NCoinManager.getMarketName(showName)

                            setText(R.id.tv_explosion_price_title, LanguageUtil.getString(context, "leverage_text_blowingUp") + " $quoteCoin")
                            setText(R.id.tv_available_balance_title, "${LanguageUtil.getString(context, "assets_text_available")} $baseCoin")
                            setText(R.id.tv_borrowed_title, "${LanguageUtil.getString(context, "assets_text_available")} $quoteCoin")

                            setText(R.id.tv_risk_rate_value, LanguageUtil.getString(context, "leverage_risk"))
                            setText(R.id.tv_title, LanguageUtil.getString(context, "leverage_asset") + " $showName")
                            setText(R.id.tv_risk_tip, LanguageUtil.getString(context, "leverage_risk_prompt"))
                            setText(R.id.btn_borrow, LanguageUtil.getString(context, "leverage_borrow"))
                            setText(R.id.btn_transfer, LanguageUtil.getString(context, "assets_action_transfer"))

                            jsonObject?.run {
                                val quoteCoin = optString("quoteCoin", "")
                                val baseCoin = optString("baseCoin", "")

                                setText(R.id.tv_risk_rate, "--")

                                var riskRate = optString("riskRate", "0")

                                var showName = NCoinManager.getShowMarketName("$baseCoin/$quoteCoin")
                                setText(R.id.tv_title, LanguageUtil.getString(context, "leverage_asset") + " $showName")
                                setText(R.id.tv_risk_rate, "$riskRate%")

                                val imStatus = getView<ImageView>(R.id.im_status)

                                if (riskRate.toInt() >= 200) {
                                    setTextColor(R.id.tv_risk_rate, ColorUtil.getColor(context, R.color.green))
                                    imStatus.imageResource = R.mipmap.coins_pointer1
                                } else if (riskRate.toInt() in (150..200)) {
                                    setTextColor(R.id.tv_risk_rate, ColorUtil.getColor(context, R.color.red))
                                    imStatus.imageResource = R.mipmap.coins_pointer2
                                } else if (riskRate.toInt() in (120..150)) {
                                    imStatus.imageResource = R.mipmap.coins_pointer3
                                } else {
                                    setTextColor(R.id.tv_risk_rate, ColorUtil.getColor(context, R.color.text_color))
                                    setText(R.id.tv_risk_rate, "--")
                                    imStatus.imageResource = if (riskRate.toInt() == 0) R.mipmap.coins_pointer1 else R.mipmap.coins_pointer4
                                }

                                var quoteRate = NCoinManager.getCoinShowPrecision(quoteCoin)
                                var baseRate = NCoinManager.getCoinShowPrecision(baseCoin)


                                val precision = NCoinManager.getSymbolObj(symbol)?.optInt("price", 2)
                                        ?: 2

                                setText(R.id.tv_explosion_price_title, LanguageUtil.getString(context, "leverage_text_blowingUp") + " ${NCoinManager.getShowMarket(quoteCoin)}")
                                setText(R.id.tv_explosion_price, BigDecimalUtils.divForDown(optString("burstPrice", ""), precision).toPlainString())

                                setText(R.id.tv_available_balance_title, "${LanguageUtil.getString(context, "assets_text_available")} ${NCoinManager.getShowMarket(baseCoin)}")
                                setText(R.id.tv_available_balance, BigDecimalUtils.divForDown(optString("baseNormalBalance", ""), 8).toPlainString())
                                setText(R.id.tv_borrowed_title, "${LanguageUtil.getString(context, "assets_text_available")} ${NCoinManager.getShowMarket(quoteCoin)}")
                                setText(R.id.tv_borrowed, BigDecimalUtils.divForDown(optString("quoteNormalBalance", ""), 8).toPlainString())
                            }
                        }
                    }
                    .addOnClickListener(R.id.btn_borrow, R.id.btn_return, R.id.btn_transfer)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_borrow -> {
                                tDialog.dismiss()
                                listener.clickItem(1)
                            }
                            R.id.btn_return -> {
                                listener.clickItem(2)
                            }
                            R.id.btn_transfer -> {
                                listener.clickItem(3)
                            }
                        }
                    }
                    .create()
                    .show()
        }


        /**
         * ???????????????
         */
        fun showHomePageDialog(context: Context) {
            val homePageDialogTitle = PublicInfoDataService.getInstance().getHomePageDialogTitle(null)
            val homePageDialogStatus = PublicInfoDataService.getInstance().homePageDialogStatus
            if (homePageDialogStatus || TextUtils.isEmpty(homePageDialogTitle)) return
            LogUtil.e(TAG_ADVERT, "showHomePageDialog ${dialogType}")
            if (dialogType != 0) {
                return
            }
            dialogType = 3
            var isSelected = true
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_homepage_warn)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_text, homePageDialogTitle)

                        viewHolder?.setText(R.id.tv_has_known, LanguageUtil.getString(context, "common_has_known"))
                        viewHolder?.setText(R.id.btn_know, LanguageUtil.getString(context, "alert_common_iknow"))
                        val tv_text = viewHolder?.getView<TextView>(R.id.tv_text)
                        tv_text?.movementMethod = ScrollingMovementMethod()

                        val imageView = viewHolder?.getView<ImageView>(R.id.iv_state)
                        imageView?.setOnClickListener {
                            isSelected = !isSelected
                            if (isSelected) {
                                imageView.imageResource = R.drawable.selected
                            } else {
                                imageView.imageResource = R.drawable.unchecked
                            }
                        }
                    }.setOnDismissListener {
                        dialogType = 0
                    }
                    .addOnClickListener(R.id.btn_know)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_know -> {
                                if (!isSelected) return@setOnViewClickListener
                                tDialog.dismiss()
                                PublicInfoDataService.getInstance().saveHomePageDialogStatus(isSelected)
                            }
                        }

                    }.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return@OnKeyListener true
                        }
                        false  //???????????????
                    }).create().show()
        }


        /**
         * ???????????????
         */
        fun showLeverDialog(context: Context, listener: DialogTransferBottomListener) {
            val leverDialogURL = PublicInfoDataService.getInstance().getLeverDialogURL(null)
            var isSelected = false
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_lever_warn)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val text = LanguageUtil.getString(context, "lever_need_read")
//                                if (SystemUtils.isZh()) {
//                            Html.fromHtml("${ LanguageUtil.getString(context,"lever_need_read")}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>")
//                        } else if (SystemUtils.isTW()) {
//                            Html.fromHtml("${ LanguageUtil.getString(context,"lever_need_read")}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>")
//                        } else if (SystemUtils.isJapanese()) {
//                            Html.fromHtml("${"?????????????????????????????????????????????"}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>" + "???????????????????????????")
//                        } else if (SystemUtils.isKorea()) {
//                            Html.fromHtml("${"???????????? ????????? ???????????? ?????? \""}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>" + "\" ??? ?????? ??????????????????")
//                        } else if (SystemUtils.isVietNam()) {
//                            Html.fromHtml("${"?????c v?? ?????ng ?? v???i\""}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>" + "\" tr?????c khi b???t ?????u giao d???ch c?? ????n b???y")
//                        } else {
//                            Html.fromHtml("${"Read and agree to the \""}<font color='#3078FF'>${ LanguageUtil.getString(context,"lever_trade_agreement")}</font>" + "\" before starting leveraged trading")
//                        }
                        viewHolder?.setText(R.id.tv_text, text)
                        viewHolder?.setText(R.id.tv_has_known, LanguageUtil.getString(context, "common_has_known"))
                        viewHolder?.setText(R.id.btn_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.btn_know, LanguageUtil.getString(context, "common_start_trade"))
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
                    .addOnClickListener(R.id.btn_know, R.id.btn_cancel, R.id.tv_text)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.tv_text -> {
                                ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, Bundle().apply {
                                    putString(ParamConstant.head_title, LanguageUtil.getString(context, "lever_trade_agreement"))
                                    putString(ParamConstant.web_url, leverDialogURL)
                                    putInt(ParamConstant.web_type, WebTypeEnum.NORMAL_INDEX.value)
                                })
                            }

                            R.id.btn_cancel -> {
                                tDialog.dismiss()
                                listener.showCancel()
                            }

                            R.id.btn_know -> {
                                if (!isSelected) return@setOnViewClickListener
                                tDialog.dismiss()
                                PublicInfoDataService.getInstance().saveLeverDialogStatus(isSelected)
                                listener.sendConfirm()
                            }
                        }

                    }.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return@OnKeyListener true
                        }
                        false  //???????????????
                    })
                    .create()
                    .show()
        }

        /**
         * ????????????????????????
         */
        fun loginTypeDialog(context: Context,
                            listener: DialogTransferBottomListener,
                            title: String = "", googleAuth: String? = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_validatin_type_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->

                        viewHolder?.setText(R.id.tv_tip, LanguageUtil.getString(context, "login_action_fogetpwdSafety"))
                        viewHolder?.setText(R.id.tv_validation_content, LanguageUtil.getString(context, "login_success_action_alert_message"))
                        viewHolder?.setText(R.id.tv_google, LanguageUtil.getString(context, "safety_action_changeLoginPassword"))
                        viewHolder?.setText(R.id.tv_realname_certification, LanguageUtil.getString(context, "login_success_action_alert_google"))
                        viewHolder?.setText(R.id.tv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))
                        viewHolder?.setText(R.id.tv_goto_set, LanguageUtil.getString(context, "common_text_btnSetting"))
                        if (googleAuth == "1") {
                            viewHolder?.setGone(R.id.tv_realname_certification, true)
                        } else {
                            viewHolder?.setGone(R.id.tv_realname_certification, false)
                        }

                    }
                    .addOnClickListener(R.id.tv_goto_set, R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                listener.showCancel()
                                tDialog.dismiss()
                            }
                            R.id.tv_goto_set -> {
                                listener.sendConfirm()
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        var show4KolDialog: TDialog? = null

        /**
         * ????????????????????????
         */
        fun webShare(context: Context,
                     listener: DialogWebViewShareListener,
                     profitRate: Double = 0.0, winRateWeek: Double = 0.0, winRate: Double = 0.0, labe: String = "", userName: String = ""): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_webview_documentary_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        var threshold = 0
                        var inviteUrl = UserDataService.getInstance().inviteUrl

                        var imageDown = viewHolder?.getView<ImageView>(R.id.tv_swipe_down)
                        var imageNext = viewHolder?.getView<ImageView>(R.id.tv_swipe_next)
                        var tvProfit = viewHolder?.getView<TextView>(R.id.tv_profit)
                        var tvNumber = viewHolder?.getView<TextView>(R.id.tv_number)
                        var tvExchange = viewHolder?.getView<TextView>(R.id.tv_exchange)
                        var tvQrCode = viewHolder?.getView<ImageView>(R.id.tv_qr_code)
                        var ivLogo = viewHolder?.getView<ImageView>(R.id.iv_logo)

                        viewHolder?.setText(R.id.tv_user_name, userName)
                        viewHolder?.setText(R.id.tv_describe, labe)
                        tvQrCode?.setImageBitmap(BitmapUtils.generateBitmap(inviteUrl, 300, 300))
                        var app_logo_list_new = PublicInfoDataService.getInstance().getApp_logo_list_new(null)
                        if (null != app_logo_list_new && app_logo_list_new.size > 0) {
                            var logoWhite = app_logo_list_new[1]
                            if (StringUtil.isHttpUrl(logoWhite)) {
                                GlideUtils.loadImageHeader(context, logoWhite, ivLogo)
                            }
                        }
                        tvExchange?.text = "${AppUtils.getAppName(context)}-" + context.getString(R.string.contract_digital_currency_exchange)
                        tvProfit?.text = context.getString(R.string.contract_total_profit)
                        tvNumber?.run {
                            textColor = ColorUtil.getMainColorType(winRateWeek >= 0)
                            text = "${BigDecimalUtils.divForDown(winRateWeek.toString(), 2).toPlainString()}"
                        }

                        var mainLayout = viewHolder?.getView<RelativeLayout>(R.id.rl_contract_share_layout)
                        mainLayout?.setOnLongClickListener {
                            show4KolDialog = showListDialog(context, arrayListOf(context.getString(R.string.sl_str_save_image), context.getString(R.string.sl_str_share_confirm)), 0, object : DialogOnclickListener {
                                override fun clickItem(data: ArrayList<String>, item: Int) {
                                    imageDown?.visibility = View.INVISIBLE
                                    imageNext?.visibility = View.INVISIBLE
                                    if (listener != null) {
                                        when (item) {
                                            0 -> {
                                                listener.webviewSaveImage(mainLayout)
                                            }
                                            1 -> {
                                                listener.confirmShare(mainLayout)
                                            }
                                        }
                                        show4KolDialog?.dismiss()
                                    }
                                }
                            })
                            false
                        }
                        imageDown?.setOnClickListener {
                            when (threshold) {
                                0 -> {
                                    threshold = 2
                                    tvProfit?.text = context.getString(R.string.contract_win_rate)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(winRate >= 0)
                                        text = "$winRate%"
                                    }

                                }
                                1 -> {
                                    threshold = 0
                                    tvProfit?.text = context.getString(R.string.contract_total_profit)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(winRateWeek >= 0)
                                        text = "${BigDecimalUtils.divForDown(winRateWeek.toString(), 2).toPlainString()}"
                                    }

                                }
                                2 -> {
                                    threshold = 1
                                    tvProfit?.text = context.getString(R.string.sl_str_profit_rate1)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(profitRate >= 0)
                                        text = "$profitRate%"
                                    }
                                }
                            }
                        }

                        imageNext?.setOnClickListener {
                            when (threshold) {
                                0 -> {
                                    threshold = 1
                                    tvProfit?.text = context.getString(R.string.sl_str_profit_rate1)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(profitRate >= 0)
                                        text = "$profitRate%"
                                    }
                                }
                                1 -> {
                                    threshold = 2
                                    tvProfit?.text = context.getString(R.string.contract_win_rate)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(winRate >= 0)
                                        text = "$winRate%"
                                    }
                                }
                                2 -> {
                                    threshold = 0
                                    tvProfit?.text = context.getString(R.string.contract_total_profit)
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainColorType(winRateWeek >= 0)
                                        text = "${BigDecimalUtils.divForDown(winRateWeek.toString(), 2).toPlainString()}"
                                    }
                                }
                            }
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
         * ???????????????????????????dialog
         */
        fun showSingleForceDialog(context: Context, content: String, listener: DialogBottomListener) {
            showDialog(context, content, true, listener, "", "", "", true, false, true)
        }

        /**
         * ????????????dialog
         *
         */
        fun showCoinBottomDialog(context: Context,
                                 codeType: String,
                                 symbolAndUnit: String,
                                 isLevel: Boolean,
                                 listener: DialogBottomCoinListener): TDialog {
            var commissiondSelectSymbolStatus = false
            var commissionedSymbol = ""
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_bottom_coin_adapter)
                    .setScreenWidthAspect(context, 1f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val itemCoins = viewHolder?.getView<DownSettingView>(R.id.pet_select_coin)
                        val layoutCoin = viewHolder?.getView<LineAdapter4FundsLayout>(R.id.ll_total_title)
                        val etCurrency = viewHolder?.getView<CustomizeEditText>(R.id.et_currency)
                        val cubConfirm = viewHolder?.getView<SuperTextView>(R.id.tv_confirm)
                        etCurrency?.isFocusable = true
                        etCurrency?.isFocusableInTouchMode = true
                        cubConfirm?.isEnabled = false
                        etCurrency?.transformationMethod = TransInformation()
                        etCurrency?.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                val temp = s.toString()
                                if (temp.isNotEmpty() || codeType.isNotEmpty()) {
                                    if (LanguageUtil.getString(context, "common_text_allDay") != s.toString()) {
                                        commissionedSymbol = s.toString().getCoinToUpper()
                                    }
                                    cubConfirm?.isEnabled = true
                                    cubConfirm?.solid = ColorUtil.getColor(R.color.main_blue)
                                } else {
                                    cubConfirm?.isEnabled = false
                                    cubConfirm?.solid = ColorUtil.getColor(R.color.btn_unclickable_color)
                                }

                            }
                        })
                        if (codeType.isNotEmpty()) {
                            etCurrency?.text = codeType.editable()
                        }
                        layoutCoin?.apply {
                            val coinList = arrayListOf<String>()
                            if (isLevel) {
                                coinList.addAll(NCoinManager.getLeverGroup())
                                if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                                    coinList.add(0, LanguageUtil.getString(context, "common_action_sendall"))
                                }
                            } else {
                                coinList.addAll(PublicInfoDataService.getInstance().getMarketSortList(null))
                                if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                                    coinList.add(0, LanguageUtil.getString(context, "common_action_sendall"))
                                }
                            }
                            if (symbolAndUnit.isNotEmpty()) {
                                itemCoins?.setEditText(symbolAndUnit)
                                selectPosition = coinList.indexOf(symbolAndUnit)
                            }
                            setStringBeanData(coinList, false)
                            setLineSelectOncilckListener(object : LineSelectOnclickListener {
                                override fun selectMsgIndex(index: String?) {
                                    itemCoins?.setEditText(NCoinManager.getShowMarket(index) ?: "")
                                }

                                override fun sendOnclickMsg() {

                                }
                            })
                        }
                        itemCoins?.onTextListener = object : DownSettingView.OnTextListener {
                            override fun showText(text: String): String {

                                return text
                            }

                            override fun returnItem(item: Int) {

                            }

                            override fun onclickImage() {
                                commissiondSelectSymbolStatus = !commissiondSelectSymbolStatus
                                if (commissiondSelectSymbolStatus) {
                                    layoutCoin?.visibility = View.VISIBLE
                                } else {
                                    layoutCoin?.visibility = View.GONE
                                }
                                itemCoins?.priceDownEdit(commissiondSelectSymbolStatus)
                            }

                        }
                    }
                    .addOnClickListener(R.id.tv_security_cancel, R.id.btn_return, R.id.tv_confirm)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_security_cancel -> {
                                tDialog.dismiss()
                            }
                            R.id.btn_return -> {
                                //
                                val cubConfirm = viewHolder.getView<SuperTextView>(R.id.tv_confirm)
                                cubConfirm.isEnabled = false
                                cubConfirm.solid = ColorUtil.getColor(R.color.btn_unclickable_color)
                                viewHolder?.getView<CustomizeEditText>(R.id.et_currency)?.text = "".editable()
                                val itemCoins = viewHolder?.getView<DownSettingView>(R.id.pet_select_coin)
                                val layoutCoin = viewHolder?.getView<LineAdapter4FundsLayout>(R.id.ll_total_title)
                                layoutCoin?.visibility = View.GONE
                                itemCoins?.isFocusableInTouchMode = false
                                itemCoins?.resetEdit()
                                KeyBoardUtils.closeKeyBoard(context)
                            }
                            R.id.tv_confirm -> {
                                val itemCoins = viewHolder?.getView<DownSettingView>(R.id.pet_select_coin)
                                listener.returnTypeCode(commissionedSymbol, itemCoins?.text)
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }
        /**
         * PoS????????????
         */
        fun showSuccessDialog(context: Context,
                              content: String,
                              isSingle: Boolean,
                              listener: DialogBottomListener,
                              title: String = "",
                              cancelTitle: String = "",
                              confrimTitle: String = "") {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_normal_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
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
                                viewHolder?.setText(R.id.tv_confirm_btn, context.getString(R.string.common_text_btnConfirm))
                            }

                        } else {
                            viewHolder?.setText(R.id.tv_cancel, context.getString(R.string.common_text_btnCancel))
                            if (confrimTitle.isNotEmpty()) {
                                viewHolder?.setText(R.id.tv_cancel, confrimTitle)
                            }
                            if (!TextUtils.isEmpty(cancelTitle)) {
                                viewHolder?.setText(R.id.tv_confirm_btn, cancelTitle)
                            } else {
                                viewHolder?.setText(R.id.tv_confirm_btn, context.getString(R.string.common_text_btnConfirm))
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
                                if (listener != null && (!isSingle)) {
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
         * ????????????
         */
        fun webGridShare(context: Context,
                         listener: DialogWebViewShareListener?,
                         totalProfit: String = "", profitRate: String = "", time: String = "", count: String = "", coin: String = "", coinSymbol: String = ""): TDialog {

            val total = context.getString(R.string.quant_grid_profit) + "($coinSymbol)"
            val rate = context.getString(R.string.grid_annualized_yield)
            val totalProfitTemp = "${ColorUtil.getMainGridResTypeCorner(totalProfit)}${BigDecimalUtils.divForDown(totalProfit, 2).toPlainString()}"
            val profitRateTemp = "${ColorUtil.getMainGridResTypeCorner(profitRate)}${profitRate}"
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_webview_grid_share_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        var threshold = 0
                        var inviteUrl = UserDataService.getInstance().inviteUrl

                        var imageDown = viewHolder?.getView<ImageView>(R.id.tv_swipe_down)
                        var imageNext = viewHolder?.getView<ImageView>(R.id.tv_swipe_next)
                        var tvProfit = viewHolder?.getView<TextView>(R.id.tv_profit)
                        var tvNumber = viewHolder?.getView<TextView>(R.id.tv_number)
                        var tvQrCode = viewHolder?.getView<ImageView>(R.id.tv_qr_code)
                        var ivLogo = viewHolder?.getView<ImageView>(R.id.iv_logo)

                        var tvProfitSum = viewHolder?.getView<TextView>(R.id.tv_profit_sum_value)
                        var tvProfitPrice = viewHolder?.getView<TextView>(R.id.tv_profit_price_vale)
                        var tvTime = viewHolder?.getView<TextView>(R.id.tv_loss_price_value)

                        viewHolder?.setText(R.id.tv_app_name, AppUtils.getAppName(context))
                        tvProfitSum?.text = count
                        tvProfitPrice?.text = coin
                        tvTime?.text = time

                        tvQrCode?.setImageBitmap(BitmapUtils.generateBitmap(inviteUrl, 300, 300))
                        ivLogo?.imageResource = R.mipmap.ic_launcher
                        tvProfit?.text = total
                        tvNumber?.run {
                            textColor = ColorUtil.getMainGridResType(totalProfit)
                            text = totalProfitTemp
                        }
                        imageDown?.setOnClickListener {
                            when (threshold) {
                                0 -> {
                                    threshold = 1
                                    tvProfit?.text = rate
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainGridResType(profitRate)
                                        text = "$profitRateTemp%"
                                    }

                                }
                                1 -> {
                                    threshold = 0
                                    tvProfit?.text = total
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainGridResType(totalProfit)
                                        text = totalProfitTemp
                                    }

                                }
                            }
                        }

                        imageNext?.setOnClickListener {
                            when (threshold) {
                                0 -> {
                                    threshold = 1
                                    tvProfit?.text = rate
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainGridResType(profitRate)
                                        text = "$profitRateTemp%"
                                    }
                                }
                                1 -> {
                                    threshold = 0
                                    tvProfit?.text = total
                                    tvNumber?.run {
                                        textColor = ColorUtil.getMainGridResType(totalProfit)
                                        text = "$totalProfitTemp"
                                    }
                                }
                            }
                        }
                    }
                    .addOnClickListener(R.id.btn_share)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.btn_share -> {
                                val mainLayout = viewHolder?.getView<RelativeLayout>(R.id.rl_contract_share_layout)
                                mainLayout?.apply {
                                    Handler().postDelayed({
                                        listener?.confirmShare(this)
                                    }, 200)
                                }
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        interface DialogWebViewAliYunSlideListener {

            fun webviewSlideListener(json: Map<String, String>)
        }

        fun webAliyunShare(context: Context,
                           listener: DialogWebViewAliYunSlideListener?,
                           webUrl: String = ""): TDialog {

            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_aliyun_slide)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val testWebview = viewHolder?.getView<WebView>(R.id.rv_webview)
                        testWebview!!.settings.useWideViewPort = true
                        testWebview.settings.loadWithOverviewMode = true
                        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        testWebview.settings.cacheMode = WebSettings.LOAD_NO_CACHE //LOAD_NO_CACHE
                        testWebview.settings.domStorageEnabled = true //????????????DOM??????,???????????????????????????????????????????????????
                        testWebview.settings.allowContentAccess = true
                        // ????????????????????????????????????????????????WebView?????????????????????
                        testWebview.setWebViewClient(object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                                view.loadUrl(url!!)
                                return true
                            }

                        })
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            WebView.setWebContentsDebuggingEnabled(true)
                        }
                        // ??????WebView??????????????????JavaScript???
                        testWebview.settings.javaScriptEnabled = true
                        // ??????JavaScript??????Java??????????????????
                        testWebview.addJavascriptInterface(SlideJsInterface(listener), "testInterface")
                        // ?????????????????????
                        testWebview.loadUrl(webUrl)
                    }
                    .addOnClickListener(R.id.imClose)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.imClose -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

    }

    class SlideJsInterface(mListener: DialogWebViewAliYunSlideListener?) {
        var listener: DialogWebViewAliYunSlideListener? = mListener

        @JavascriptInterface
        fun getSlideData(callData: String?) {
            println("callData ${callData}")
            if (!callData.isNullOrEmpty()) {
                var mapClass: Map<String, String> = HashMap()
                val map = JsonUtils.jsonToBean(callData, mapClass.javaClass)
                listener?.webviewSlideListener(map)
            }
        }
    }





}








