package com.yjkj.chainup.new_version.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coorchice.library.SuperTextView
import com.tencent.mmkv.MMKV
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.common.Constants
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.TradeTypeEnum
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.*
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.grid.NGridFragment
import com.yjkj.chainup.new_version.activity.leverage.NLeverFragment
import com.yjkj.chainup.new_version.adapter.trade.NSearchCoinAdapter
import com.yjkj.chainup.new_version.fragment.NCVCTradeFragment
import com.yjkj.chainup.new_version.home.AdvertModel
import com.yjkj.chainup.new_version.home.dialogType
import com.yjkj.chainup.util.*
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cvctrade.*
import kotlinx.android.synthetic.main.fragment_nlever.*
import org.jetbrains.anko.*
import org.json.JSONObject
import java.math.BigDecimal

//Created by $USER_NAME on 2018/10/15.

class DialogUtil {

    companion object {

        /**iv_cancel
         *  ???????????? & ???????????????Dialog
         */
        fun showFingerprintOrFaceIDDialog(context: Context, title: String = "", tips: String = ""): TDialog {

            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_fingerprint_faceid)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        if (title.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_title, title)
                        }
                        if (tips.isNotEmpty()) {
                            viewHolder?.setText(R.id.tv_tips, tips)
                        }
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "login_text_fingerprint"))
                        viewHolder?.setText(R.id.iv_cancel, LanguageUtil.getString(context, "common_text_btnCancel"))

                    }
                    .addOnClickListener(R.id.iv_cancel)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.iv_cancel -> {
                                tDialog.dismiss()
                            }
                        }

                    }.setOnKeyListener { dialog, keyCode, event -> true }
                .create().show()

        }



        /**
         * ???????????????popupWindow
         */
        fun createCVCPop(context: Context?, targetView: View, fragment: NCVCTradeFragment) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.popwindow_cvc_entrance)
                    .setFocusAndOutsideEnable(true)
                    .setBackgroundDimEnable(true)
                    .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply()

            val coinMapBean = DataManager.getCoinMapBySymbol(PublicInfoDataService.getInstance().currentSymbol)
            val coin = NCoinManager.getMarketName(coinMapBean.name)

            cvcEasyPopup?.run {
                /* ?????? */
                findViewById<View>(R.id.ll_recharge)?.setOnClickListener {
                    cvcEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }

                    if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
                        NewDialogUtils.KycSecurityDialog(context!!, context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                ?: "", object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        NToastUtil.showTopToastNet(context as Activity, false, context?.getString(R.string.noun_login_pending))
                                    }

                                    2, 3 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                    }
                                }
                            }
                        })
                        return@setOnClickListener
                    }


                    ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
                        putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
                        putBoolean(ParamConstant.COIN_FROM, true)
                    })
                }
                findViewById<TextView>(R.id.tv_text_recharge).text = LanguageUtil.getString(context, "coin_text_recharge")
                findViewById<TextView>(R.id.tv_action_transfer).text = LanguageUtil.getString(context, "assets_action_transfer")


                /*??????*/
                val llTransfer = findViewById<View>(R.id.ll_transfer)
                if (PublicInfoDataService.getInstance().otcOpen(null)) {
                    llTransfer.visibility = View.GONE
                } else {
                    llTransfer.visibility = View.GONE
                }
                llTransfer?.setOnClickListener {
                    cvcEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }
                    ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, coin)
                }

                val isHorizontalDepth = PublicInfoDataService.getInstance().isHorizontalDepth
                if (isHorizontalDepth) {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_vertical_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_horizontalDish")
                } else {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_horizontal_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_verticalDish")
                }

                findViewById<View>(R.id.ll_change_pan)?.setOnClickListener {
                    PublicInfoDataService.getInstance().setDepthType(!isHorizontalDepth)
                    fragment.run {
                        v_horizontal_depth?.visibility = if (!isHorizontalDepth) View.VISIBLE else View.GONE
                        v_vertical_depth?.visibility = if (!isHorizontalDepth) View.GONE else View.VISIBLE
                        v_horizontal_depth?.resetPrice()
                        v_vertical_depth?.resetPrice()
                        v_vertical_depth?.clearDepthView()
                        v_horizontal_depth?.changeTape(AppConstant.DEFAULT_TAPE)
                    }
                    cvcEasyPopup.dismiss()
                }
            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
        }


        fun createGridPop(context: Context?, targetView: View, fragment: NGridFragment) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.popwindow_cvc_entrance)
                    .setFocusAndOutsideEnable(true)
                    .setBackgroundDimEnable(true)
                    .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply()

            val coinMapBean = DataManager.getCoinMapBySymbol(PublicInfoDataService.getInstance().currentSymbol)
            val coin = NCoinManager.getMarketName(coinMapBean.name)

            cvcEasyPopup?.run {
                /* ?????? */
                findViewById<View>(R.id.ll_recharge)?.setOnClickListener {
                    cvcEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }

                    if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
                        NewDialogUtils.KycSecurityDialog(context!!, context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                ?: "", object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        NToastUtil.showTopToastNet(context as Activity, false, context?.getString(R.string.noun_login_pending))
                                    }

                                    2, 3 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                    }
                                }
                            }
                        })
                        return@setOnClickListener
                    }


                    ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
                        putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
                        putBoolean(ParamConstant.COIN_FROM, true)
                    })
                }
                findViewById<TextView>(R.id.tv_text_recharge).text = LanguageUtil.getString(context, "coin_text_recharge")
                findViewById<TextView>(R.id.tv_action_transfer).text = LanguageUtil.getString(context, "assets_action_transfer")


                /*??????*/
                val llTransfer = findViewById<View>(R.id.ll_transfer)
                if (PublicInfoDataService.getInstance().otcOpen(null)) {
                    llTransfer.visibility = View.VISIBLE
                } else {
                    llTransfer.visibility = View.GONE
                }
                llTransfer?.setOnClickListener {
                    cvcEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }
                    ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, coin)
                }

                val isHorizontalDepth = PublicInfoDataService.getInstance().isHorizontalDepth
                if (isHorizontalDepth) {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_vertical_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_horizontalDish")
                } else {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_horizontal_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_verticalDish")
                }

                findViewById<View>(R.id.ll_change_pan)?.visibility = View.GONE
            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
        }


        /**
         * ???????????????popupWindow
         */
        fun createLeverPop(context: Context?, targetView: View, fragment: NLeverFragment, symbol: String = PublicInfoDataService.getInstance().currentSymbol4Lever) {
            val leverEasyPopup = EasyPopup.create().setContentView(context, R.layout.popwindow_lever_entrance)
                    .setFocusAndOutsideEnable(true)
                    .setBackgroundDimEnable(true)
                    .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply()

            leverEasyPopup?.run {
                // ??????
                findViewById<View>(R.id.ll_borrow)?.setOnClickListener {
                    leverEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }
                    ArouterUtil.navigation(RoutePath.NewVersionBorrowingActivity, Bundle().apply {
                        putString(ParamConstant.symbol, symbol)
                    })
                }
                findViewById<TextView>(R.id.tv_borrow).text = LanguageUtil.getString(context, "leverage_borrow")
                findViewById<TextView>(R.id.tv_action_transfer).text = LanguageUtil.getString(context, "assets_action_transfer")
                findViewById<TextView>(R.id.tv_return).text = LanguageUtil.getString(context, "leverage_return")

                // ??????
                findViewById<View>(R.id.ll_transfer)?.setOnClickListener {
                    leverEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }
                    ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                        putString(ParamConstant.TRANSFERSTATUS, ParamConstant.LEVER_INDEX)
                        putString(ParamConstant.TRANSFERCURRENCY, symbol)
                    })
                }
                // ??????
                findViewById<View>(R.id.ll_return)?.setOnClickListener {
                    leverEasyPopup.dismiss()
                    if (!LoginManager.checkLogin(context, true)) {
                        return@setOnClickListener
                    }
                    ArouterUtil.navigation(RoutePath.CurrencyLendingRecordsActivity, Bundle().apply {
                        putString(ParamConstant.symbol, symbol)
                    })
                }

                val isHorizontalDepth = PublicInfoDataService.getInstance().isHorizontalDepth4Lever
                if (isHorizontalDepth) {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_vertical_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_horizontalDish")
                } else {
                    findViewById<ImageView>(R.id.iv_change_direction)?.setImageResource(R.mipmap.coins_horizontal_version)
                    findViewById<TextView>(R.id.tv_change_direction).text = LanguageUtil.getString(context, "coin_text_verticalDish")
                }

                findViewById<View>(R.id.ll_change_pan)?.setOnClickListener {
                    PublicInfoDataService.getInstance().setDepthType4Lever(!isHorizontalDepth)
                    fragment.run {
                        v_horizontal_depth_lever?.visibility = if (!isHorizontalDepth) View.VISIBLE else View.GONE
                        v_vertical_depth_lever?.visibility = if (!isHorizontalDepth) View.GONE else View.VISIBLE
                        v_vertical_depth_lever?.clearDepthView()
//                        v_vertical_depth_lever?.refreshDepthView()
                        v_horizontal_depth_lever?.changeTape(AppConstant.DEFAULT_TAPE)
                    }
                    leverEasyPopup.dismiss()
                }
            }
            leverEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -50, 50)
        }



        /**
         * ETF????????????
         */
        fun showETFStatement(context: Context, domain: String, url: String) {
            val homePageDialogStatus = PublicInfoDataService.getInstance().etfStateDialogStatus
            if (homePageDialogStatus) return
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_etf_statement)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val tv_text1 = viewHolder?.getView<TextView>(R.id.tv_text1)
                        tv_text1?.text = LanguageUtil.getString(context, "etf_text_disclaimerDetail1").toNewLine().format(domain)

                        viewHolder?.getView<SuperTextView>(R.id.btn_know)?.text = LanguageUtil.getString(context, "etf_text_knowRisk")
                        val tv_text2 = viewHolder?.getView<TextView>(R.id.tv_text2)
                        val fromHtml = Html.fromHtml("${LanguageUtil.getString(context, "etf_text_disclaimerDetail2").toNewLineHtml().format("<font color='#3078FF'>${LanguageUtil.getString(context, "etf_text_faq")}</font>")}")
                        tv_text2?.text = fromHtml
                    }
                    .addOnClickListener(R.id.btn_know, R.id.tv_text2, R.id.btn_action_next)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_know -> {
                                tDialog.dismiss()
                                ArouterUtil.greenChannel(RoutePath.TradeETFQuestionActivity,Bundle())
                            }
                            R.id.tv_text2 -> {
                                ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, Bundle().apply {
                                    putString(ParamConstant.head_title, LanguageUtil.getString(context, "etf_text_faq"))
                                    putString(ParamConstant.web_url, url)
                                    putInt(ParamConstant.web_type, WebTypeEnum.NORMAL_INDEX.value)
                                })
                            }
                            R.id.btn_action_next -> {
                                tDialog.dismiss()
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
         * ??????????????????
         */
        fun showPositionShareDialog(context: Context, obj: JSONObject?) {
            var ll_share: View? = null
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_position_share)
                    .setScreenWidthAspect(context, 1.0f)
                    .setScreenHeightAspect(context, 1.0f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_contract_deposit_rate, LanguageUtil.getString(context, "contract_deposit_rate"))
                        viewHolder?.setText(R.id.tv_contract_deposit_rate, LanguageUtil.getString(context, ""))
                        ll_share = viewHolder?.getView<View>(R.id.ll_share)
                        obj?.run {
                            val baseSymbol = optString("baseSymbol")
                            val quoteSymbol = optString("quoteSymbol")
                            val contractId = optString("contractId")
                            val pricePrecision = optString("pricePrecision").toIntOrNull() ?: 2
                            val side = optString("side")
                            val unrealisedAmountIndex = optString("unrealisedAmountIndex")
                            val avgPrice = optString("avgPrice")
                            // ????????????
                            val indexPrice = optString("indexPrice")
                            // ?????????
                            var unrealisedRateIndex = optString("unrealisedRateIndex")

                            val tv_contract_orientation = viewHolder?.getView<TextView>(R.id.tv_contract_orientation)


                            // ????????????
                            tv_contract_orientation?.backgroundColor = ColorUtil.getMainColorType(side == "BUY")
                            val orderSide = if (side == "BUY") {
                                LanguageUtil.getString(context, "sl_str_open_long")
                            } else {
                                LanguageUtil.getString(context, "sl_str_open_short")
                            }
                            tv_contract_orientation?.text = orderSide
                            // ?????????
                            val tv_contract_profit_rate = viewHolder?.getView<TextView>(R.id.tv_contract_profit_rate)
                            val tv_title = viewHolder?.getView<TextView>(R.id.tv_title)
                            val iv_share = viewHolder?.getView<ImageView>(R.id.iv_share)
                            tv_title?.text = LanguageUtil.getContractShareText(context, unrealisedRateIndex)
                            if (unrealisedRateIndex.contains("-")) {
                                iv_share?.setImageResource(R.drawable.contract_share_cry)
                            } else {
                                iv_share?.setImageResource(R.drawable.contract_share_smile)
                            }

                            unrealisedRateIndex = if (StringUtil.checkStr(unrealisedAmountIndex)) {
                                BigDecimal(unrealisedRateIndex).setScale(2, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                            } else {
                                "0.00"
                            }
                            if (unrealisedRateIndex.contains("-")) {
                                tv_contract_profit_rate?.text = unrealisedRateIndex + "%"
                            } else {
                                tv_contract_profit_rate?.text = "+$unrealisedRateIndex%"
                            }

                            /**
                             * ????????????
                             */
                            val tv_contract_type_title = viewHolder?.getView<TextView>(R.id.tv_contract_type_title)
                            LogUtil.d(TAG, "===LL:${Contract2PublicInfoManager.getContractTypeText(context, contractId.toInt())}===")
                            tv_contract_type_title?.text = Contract2PublicInfoManager.getContractTypeText(context, contractId.toInt())
                            val tv_contract_symbol = viewHolder?.getView<TextView>(R.id.tv_contract_symbol)
                            tv_contract_symbol?.text = baseSymbol + quoteSymbol


                            val tv_contract_price = viewHolder?.getView<TextView>(R.id.tv_contract_price)

                            /**
                             * ????????????
                             */
                            val indexPriceByPrecision = Contract2PublicInfoManager.cutValueByPrecision(indexPrice, pricePrecision)
                            tv_contract_price?.text = indexPriceByPrecision
                            /**
                             * ????????????
                             */
                            val tv_open_avg_price = viewHolder?.getView<TextView>(R.id.tv_open_avg_price)
                            tv_open_avg_price?.text = Contract2PublicInfoManager.cutValueByPrecision(avgPrice.toString(), pricePrecision)

                            val iv_qrcode = viewHolder?.getView<ImageView>(R.id.iv_qrcode)
                            GlideUtils.loadImage(context, iv_qrcode)

                            val tv_download_tip = viewHolder?.getView<TextView>(R.id.tv_download_tip)
                            tv_download_tip?.text = String.format(context.getString(R.string.sl_str_down_app), context.getString(R.string.app_name))


                        }
                    }
                    .addOnClickListener(R.id.btn_share, R.id.ll_bg)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_share -> {
                                if (ll_share != null) {
                                    var screenshotBitmap: Bitmap? = ScreenShotUtil.getScreenshotBitmap(ll_share)
                                    ZXingUtils.shareImageToWechat(screenshotBitmap, context.getString(R.string.contract_share_label), context)
                                }

                                tDialog.dismiss()
                            }
                            R.id.ll_bg -> {
                                tDialog.dismiss()
                            }
                        }

                    }.create().show()
        }




        /**
         * K?????????????????????
         */
        fun showKLineShareDialog(context: Context) {
            var screenshotBitmap: Bitmap? = null
            screenshotBitmap = ScreenShotUtil.getScreenshotBitmap((context as AppCompatActivity).window?.decorView
                    ?: return)
            var ll_share: View? = null
            TDialog.Builder(context.supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_share_market)
                    .setScreenWidthAspect(context, 1.0f)
                    .setScreenHeightAspect(context, 1.0f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val iv_qrcode = viewHolder?.getView<ImageView>(R.id.iv_qrcode)
                        viewHolder?.setText(R.id.tv_title, LanguageUtil.getString(context, "common_share_detail"))
                        viewHolder?.setText(R.id.btn_share, LanguageUtil.getString(context, "common_share_confirm"))

                        GlideUtils.loadImageQr(context, iv_qrcode)
                        if (screenshotBitmap != null) {
                            ll_share = viewHolder?.getView<View>(R.id.ll_share)
                            ll_share?.backgroundDrawable = BitmapDrawable(context.resources, screenshotBitmap)
                        }
                    }
                    .addOnClickListener(R.id.btn_share, R.id.ll_bg)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_share -> {
                                if (ll_share != null) {
                                    var bitmap: Bitmap? = ScreenShotUtil.getScreenshotBitmap(ll_share)
                                    ZXingUtils.shareImageToWechat(bitmap, context.getString(R.string.contract_share_label), context)
                                    tDialog.dismiss()
                                }
                            }
                            R.id.ll_bg -> {
                                tDialog.dismiss()
                            }
                        }

                    }.create().show()
        }

        /**
         * ETF????????????
         */
        fun showContractStatement(context: Context) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_contract_statement)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val tv_text1 = viewHolder?.getView<TextView>(R.id.tv_text1)
                        tv_text1?.text = LanguageUtil.getString(context, "customSetting_coAlert_desc")
                        viewHolder?.getView<SuperTextView>(R.id.btn_know)?.text = LanguageUtil.getString(context, "sl_str_isee")
                    }
                    .addOnClickListener(R.id.btn_know)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_know -> {
                                tDialog.dismiss()
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
         * ???????????????popupWindow
         */
        fun createCVCPopCoins(context: Context?, targetView: View, coinList: ArrayList<JSONObject>, type: Int = 0) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.popwindow_cvc_coin)
                    .setFocusAndOutsideEnable(true)
                    .setBackgroundDimEnable(true)
                    .setWidth(DisplayUtil.dip2px(180))
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply()

            val coinMapBean = DataManager.getCoinMapBySymbol(PublicInfoDataService.getInstance().currentSymbol)
            val coin = NCoinManager.getMarketName(coinMapBean.name)

            cvcEasyPopup?.run {
                /* ?????? */
                findViewById<RecyclerView>(R.id.rv_coin_entrust)?.apply {
                    layoutManager = LinearLayoutManager(context)
                    (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
                    val mAdapter = NSearchCoinAdapter()
                    mAdapter.isSelfData = coinMapBean.symbol
                    adapter = mAdapter
                    setHasFixedSize(true)
                    mAdapter.setList(coinList)
                    mAdapter.setOnItemClickListener { adapter, _, position ->
                        cvcEasyPopup.dismiss()
                        val messageEvent = MessageEvent(MessageEvent.symbol_switch_type)
                        messageEvent.msg_content = (adapter.getItem(position) as JSONObject).optString("symbol")
                        messageEvent.isLever = TradeTypeEnum.LEVER_TRADE.value == type
                        NLiveDataUtil.postValue(messageEvent)
                    }
                }

            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.ALIGN_TOP, XGravity.ALIGN_RIGHT, -20, 10)
        }

        /**
         * ???????????????popupWindow
         */
        fun createCVCOrderPop(context: Context?, index: Int = 0, targetView: View, dialogItemClickListener: NewDialogUtils.DialogOnSigningItemClickListener?) {
            val cvcEasyPopup = EasyPopup.create().setContentView(context, R.layout.popwindow_order_type)
                    .setFocusAndOutsideEnable(true)
                    .setBackgroundDimEnable(true)
                    .setWidth(targetView.width)
                    .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply()


            cvcEasyPopup?.run {
                /* ?????? */
                val topView = findViewById<View>(R.id.ll_recharge)
                topView?.setOnClickListener {
                    dialogItemClickListener?.clickItem(0, LanguageUtil.getString(context, "contract_action_limitPrice"))
                    cvcEasyPopup.dismiss()
                }
                val bottomView = findViewById<View>(R.id.ll_transfer)
                if (index == 0) {
                    findViewById<TextView>(R.id.tv_text_recharge).textColor = ColorUtil.getColor(context!!, R.color.trade_main_blue)
                    topView.backgroundResource = R.drawable.bg_layout_more_top
                } else {
                    bottomView.backgroundResource = R.drawable.bg_layout_more_top
                    findViewById<TextView>(R.id.tv_action_transfer).textColor = ColorUtil.getColor(context!!, R.color.trade_main_blue)
                }
                bottomView?.setOnClickListener {
                    dialogItemClickListener?.clickItem(1, LanguageUtil.getString(context, "contract_action_marketPrice"))
                    cvcEasyPopup.dismiss()
                }

            }
            cvcEasyPopup?.showAtAnchorView(targetView, YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 10)
        }

        /**
         * ????????????
         */
        fun showAdvertDialog(context: Context, obj: AdvertModel?) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_center_advert)
                    .setScreenWidthAspect(context, 0.8f)
                    .setScreenHeightAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDialogAnimationRes(R.style.dialogTopAnim)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        obj?.imageDownloadUrl()
                        val img = viewHolder?.getView<ImageView>(R.id.tv_head)
                        GlideUtils.loadLocalImage(context, obj?.localFile, img)
                    }
                    .setOnDismissListener {
                        dialogType = 0
                    }
                    .addOnClickListener(R.id.btn_dis, R.id.tv_head)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_dis -> {
                                tDialog.dismiss()
                            }
                            R.id.tv_head -> {
                                ArouterUtil.forwardWebView("", obj?.imageHttpUrl())
                                tDialog.dismiss()
                            }
                        }

                    }.create().show()
        }

        /**
         * ETF????????????
         */
        fun showGridStatement(context: Context?) {
            var setupOne: ImageView? = null
            var setupTwo: ImageView? = null
            var setupThree: ImageView? = null
            var setupCount = 0
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_grid_statement)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        setupOne = viewHolder?.getView<ImageView>(R.id.imageOne)
                        setupTwo = viewHolder?.getView<ImageView>(R.id.imageTwo)
                        setupThree = viewHolder?.getView<ImageView>(R.id.imageThree)

                        setupOne?.imageResource = Constants.getGuideOne()
                        setupTwo?.imageResource = Constants.getGuideTwo()
                        setupThree?.imageResource = Constants.getGuideThree()

                        setupOne?.visibility = View.VISIBLE
                    }
                    .addOnClickListener(R.id.btn_know, R.id.btn_next)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_know -> {
                                tDialog.dismiss()
                                PublicInfoDataService.getInstance().saveGridStateDialogStatus(true)
                            }
                            R.id.btn_next -> {
                                setupCount++
                                when (setupCount) {
                                    1 -> {
                                        setupOne?.visibility = View.GONE
                                        setupTwo?.visibility = View.VISIBLE
                                    }
                                    2 -> {
                                        setupTwo?.visibility = View.GONE
                                        setupThree?.visibility = View.VISIBLE
                                        view.visibility = View.GONE
                                    }
                                    else -> {
                                        PublicInfoDataService.getInstance().saveGridStateDialogStatus(true)
                                        tDialog.dismiss()
                                    }
                                }
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
         * ETF????????????
         */
        fun showRegisterStatement(context: Context) {
            val homePageDialogStatus = PublicInfoDataService.getInstance().isOpenETFSwitch
            if (!homePageDialogStatus) return
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_register_tips)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        val tv_text1 = viewHolder?.getView<TextView>(R.id.tv_text1)
                    }
                    .addOnClickListener(R.id.btn_know, R.id.btn_next)
                    .setOnViewClickListener { _, view, tDialog ->
                        when (view.id) {
                            R.id.btn_know -> {
                                tDialog.dismiss()
                                (context as Activity).finish()
                            }
                            R.id.btn_next -> {
                                tDialog.dismiss()
                            }
                        }

                    }.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return@OnKeyListener true
                        }
                        false  //???????????????
                    }).create().show()
        }

    }


}
