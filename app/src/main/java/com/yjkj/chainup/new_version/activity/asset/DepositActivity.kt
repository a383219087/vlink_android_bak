package com.yjkj.chainup.new_version.activity.asset

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.*
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.activity_deposit.*
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/17-16:26 AM
 * @Description: 充币4.0
 */
class DepositActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_deposit

    var symbol = ""
    var showSymbol = ""
    var symbolPrefix = ""

    companion object {
        const val RECHAEGE_SYMBOL = "RECHAEGE_SYMBOL"
        fun enter2(context: Context, symbol: String?) {
            val intent = Intent(context, DepositActivity::class.java)
            intent.putExtra(RECHAEGE_SYMBOL, symbol)
            context.startActivity(intent)
        }
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        symbol = intent?.getStringExtra(RECHAEGE_SYMBOL) ?: ""
        showSymbol = symbol
        tv_coin?.text = NCoinManager.getShowMarket(showSymbol)
        title_layout?.setContentTitle(LanguageUtil.getString(this, "assets_action_chargeCoin"))
        title_layout?.setRightTitle(LanguageUtil.getString(this, "charge_action_chargeHistory"))

        tv_select_coin?.text = LanguageUtil.getString(this, "b2c_text_changecoin")
        stv_save_qrcode?.text = LanguageUtil.getString(this, "charge_action_saveQR")
        tv_charge_text_chargeAddress?.text = LanguageUtil.getString(this, "charge_text_chargeAddress")
        stv_copy_address?.text = LanguageUtil.getString(this, "charge_action_copyAddress")
        tv_charge_text_tagAddress?.text = LanguageUtil.getString(this, "charge_text_tagAddress")
        stv_copy_tag?.text = LanguageUtil.getString(this, "charge_action_copyTag")
        tv_charge_chargeAlert_title?.text = LanguageUtil.getString(this, "charge_chargeAlert_title")
        tv_charge_tip_addressWarning?.text = LanguageUtil.getString(this, "recharge_warning_tips")

        var list = PublicInfoDataService.getInstance().getFollowCoinsByMainCoinName(showSymbol)
        if (null == list || list.size == 0) {
            getChargeAddress(symbol)
        } else {
            initManyChain()
        }
        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                WithDrawRecordActivity.enter2(this@DepositActivity, showSymbol, ParamConstant.TRANSFER_DEPOSIT_RECORD, ASSETTOPUP)
            }

            override fun onclickName() {

            }

        }

        /**
         * 标签
         */
        tv_charge_text_tagAddress?.setOnClickListener {
            NewDialogUtils.showNewsingleDialog(this, LanguageUtil.getString(this, "charge_tip_tagWarning"), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {

                }
            }, LanguageUtil.getString(this, "common_text_tip"), LanguageUtil.getString(this, "alert_common_i_understand"))

        }


        /**
         * 选择币对
         */
        rl_select_layout?.setOnClickListener {
            ArouterUtil.navigation4Result(RoutePath.SelectCoinActivity, Bundle().apply {
                putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
                putBoolean(ParamConstant.COIN_FROM, false)
            }, this, 321)
        }

    }


    /**
     * 设置多链
     */
    private fun initManyChain() {
        symbolPrefix = ""
        mcv_layout?.listener = object : ManyChainSelectListener {
            override fun selectCoin(coinName: JSONObject) {
                Log.e("initManyChain", "coinName:$coinName")
                symbol = coinName.optString("name", "")
                symbolPrefix = coinName.optString("mainChainName", "")
                changeResetError()
                getChargeAddress(symbol)
                getCost(symbol)
            }
        }
        mcv_layout?.setManyChainView(symbol)
    }


    /**
     * @param coinName 币种
     */
    private fun getChargeAddress(coinName: String) {
        addDisposable(getMainModel().getChargeAddress(coinName, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                val json = jsonObject.optJSONObject("data")
                if (coinName == symbol) {
                    initImageView(json)
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(this@DepositActivity, false, msg)
                changeResetError()
            }
        }))

//        HttpClient.instance.getChargeAddress(symbol = coinName)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : NetObserver<JsonObject>() {
//                    override fun onHandleSuccess(t: JsonObject?) {
//                        Log.d(TAG, "====suc::===" + t.toString())
//
//                    }
//
//                    override fun onHandleError(code: Int, msg: String?) {
//                        super.onHandleError(code, msg)
//                        Log.d(TAG, "====err=" + msg)
//                        NToastUtil.showTopToastNet(mActivity,false, msg)
//                    }
//                })
    }

    private fun initImageView(t: JSONObject?) {


        /**
         * 充值地值
         */
        var jsonObject = JSONObject(t.toString())
        val coinCoin = StringBuffer()
        if (symbolPrefix.isNotEmpty()) {
            coinCoin.append("${symbolPrefix}_")
        }
        coinCoin.append("${NCoinManager.getShowMarket(showSymbol)}")
        tv_charge_tip_addressWarning?.text = String.format(LanguageUtil.getString(this, "recharge_warning_tips"), coinCoin.toString(), jsonObject.optString("depositConfirm"))
        tv_charge_tip_addressWarning.visibility = View.VISIBLE
        var rechargeAddress = jsonObject.optString("addressStr")
        if (TextUtils.isEmpty(rechargeAddress)) {
            tv_recharge_address?.text = "---"
        } else {
            val split = rechargeAddress.split("_")
            if (split.size > 1) {
                ll_tag?.visibility = View.VISIBLE
                NewDialogUtils.showNewsingleDialog(this, LanguageUtil.getString(mActivity, "charge_tip_tagWarning"), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {

                    }

                }, LanguageUtil.getString(mActivity, "common_text_tip"), LanguageUtil.getString(mActivity, "alert_common_i_understand"))
                tv_tag?.text = split[1]
                /**
                 * 复制TAG
                 */
                stv_copy_tag?.setOnClickListener {
                    if (TextUtils.isEmpty(split[1])) {
                        NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "copy_failed"))
                    } else {
                        Utils.copyString(tv_tag)
                        NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(mActivity, "common_tip_copySuccess"))
                    }
                }

            } else {
                ll_tag?.visibility = View.GONE
            }


            tv_recharge_address?.text = split[0]

            /**
             * 复制地址
             */
            stv_copy_address?.setOnClickListener {
                Utils.copyString(tv_recharge_address)
                NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(mActivity, "toast_copy_addr_suc"))
            }

        }


        /**
         * 二维码
         */
        val qrCodeString = jsonObject.optString("addressQRCode")
        if (TextUtils.isEmpty(qrCodeString)) {
            iv_address_qr_code?.visibility = View.INVISIBLE
            stv_save_qrcode?.setOnClickListener {
                NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "common_tip_saveImgFail"))
            }

        } else {
            iv_address_qr_code?.visibility = View.VISIBLE
            val split = qrCodeString?.split(",")!!
            val qrCode = split[1]
            Log.d(TAG, "=====GoogleImg:====" + qrCode)
            var size = resources.getDimensionPixelSize(R.dimen.dp_140)
            var qrBitmap = Utils.stringtoBitmap(qrCode)
            qrBitmap = BitmapDeleteNoUseSpaceUtil.deleteNoUseWhiteSpace(qrBitmap)
            iv_address_qr_code?.setImageBitmap(qrBitmap)
            /**
             * 保存二维码
             */
            stv_save_qrcode?.setOnClickListener {
                val rxPermissions = RxPermissions(this@DepositActivity)
                /**
                 * 获取读写权限
                 */
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe { granted ->
                            if (granted) {
//                                val bitmap = Utils.stringtoBitmap(qrCode)
                                val bitmap = ScreenShotUtil.capture(this@DepositActivity)
                                if (bitmap != null) {
                                    val saveImageToGallery = ImageTools.saveImageToGallery(this, bitmap)
                                    if (saveImageToGallery) {
                                        NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(mActivity, "common_tip_saveImgSuccess"))
                                    } else {
                                        NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "common_tip_saveImgFail"))
                                    }
                                } else {
                                    NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "common_tip_saveImgFail"))
                                }
                            } else {
                                NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "warn_storage_permission"))
                            }

                        }
            }


        }
    }


    /**
     * 根据币种查询手续费和提现地址
     */
    private fun getCost(symbol: String = "") {
        addDisposable(getMainModel().getCost(symbol, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                if (null == data || data.length() == 0) return
                mcv_layout?.content = data?.optString("mainChainNameTip", "")
            }
        }))
    }


    /**
     * 接收币种
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {

            var coin = data.getStringExtra(CoinActivity.SELECTED_COIN)
                    ?: LanguageUtil.getString(mActivity, "b2c_text_changecoin")
            symbol = coin
            showSymbol = symbol
            tv_recharge_address?.text = "---"
            tv_tag?.text = ""
        }
        tv_coin.text = NCoinManager.getShowMarket(showSymbol)
        getChargeAddress(symbol)
        initManyChain()
    }

    private fun changeResetError() {
        iv_address_qr_code?.setImageBitmap(null)
        tv_recharge_address?.text = ""
        ll_tag?.visibility = View.GONE
        tv_charge_tip_addressWarning.visibility = View.INVISIBLE
    }


}
