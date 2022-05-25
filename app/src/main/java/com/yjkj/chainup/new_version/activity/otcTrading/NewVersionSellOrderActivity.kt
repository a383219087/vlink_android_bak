package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.fengniao.news.util.DateUtil
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.ShowImageActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.bean.OTCOrderDetailBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_buy_order.*
import kotlinx.android.synthetic.main.item_new_version_order.*
import kotlinx.android.synthetic.main.item_payment_information.*
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2019/4/18-5:18 PM
 * @Email buptjinlong@163.com
 * @description 出售订单
 */
class NewVersionSellOrderActivity : NewBaseActivity() {

    var orderId = ""

    var title = ""


    companion object {
        val ORDERID = "orderId"

        fun enter2(context: Context, orderId: String) {
            var intent = Intent(context, NewVersionSellOrderActivity::class.java)
            intent.putExtra(ORDERID, orderId)
            context.startActivity(intent)
        }
    }

    fun setTextContent() {
        tv_nick_name?.text = getStringContent("otcSafeAlert_action_nickname")
        tv_real_name_title?.text = getStringContent("common_text_realNameTitle")
        tv_money_title?.text = getStringContent("journalAccount_text_amount")
        tv_quantity_title?.text = getStringContent("charge_text_volume")
        tv_otc_price_title?.text = getStringContent("otc_text_price")
        tv_orderCTime?.text = getStringContent("otc_text_orderCTime")
        tv_text_remark?.text = getStringContent("address_text_remark")
        tv_orderCancelReason?.text = getStringContent("otc_text_orderCancelReason")
        tv_switching?.text = getStringContent("noun_order_paymentTerm")
    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    var contactDilaog: TDialog? = null

    private var disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_buy_order)
        title = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(this, "otc_tip_sellerOrderComplete_forotc")
        } else {
            LanguageUtil.getString(this, "otc_tip_sellerOrderComplete")
        }
        getData()
        setTextContent()
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout.slidingShowTitle(status)
            }
        }
        setOnClick()
        getOrderStateEachMin()
    }

    var coin = ""
    var payCoin = ""
    var paymentList: ArrayList<String> = arrayListOf()
    fun getData() {
        if (intent != null) {
            orderId = intent.getStringExtra(NewVersionBuyOrderActivity.ORDERID) ?: ""
        }
        title_layout?.rightIcon(R.drawable.fiat_message)
    }

    var clickStatus = true

    var otcOrderDetailBean: OTCOrderDetailBean? = null

    fun initView(bean: OTCOrderDetailBean) {
        payment_information_layout?.visibility = View.VISIBLE
        line_layout?.visibility = View.VISIBLE
        waiting_attention_to?.visibility = View.GONE
        otcOrderDetailBean = bean
        coin = bean?.coin
        payCoin = bean?.paycoin
        /**
         * 点击消息
         */
        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickName() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (bean.status == 5 && bean.isComplainUser == 1) {
                    OTCIMActivity.newIntent(this@NewVersionSellOrderActivity, bean.complainId, bean.coin, bean.totalPrice.toString()
                            , bean.status.toString(), bean.paycoin, DateUtil.longToString("yyyy-MM-dd HH:mm:ss", bean.ctime), bean.buyer.uid, bean.sequence
                            , bean.buyer.otcNickName, (bean.limitTime / 1000).toLong(), bean.isComplainUser, "sell")
                } else {
                    /**
                     * 跳转至聊天
                     */
                    OTCIMActivity.newIntent4Buyer(this@NewVersionSellOrderActivity, bean.buyer.uid, bean.sequence,
                            bean.coin, bean.totalPrice.toString(), bean.status.toString(),
                            bean.paycoin, bean.buyer.otcNickName,
                            DateUtil.longToString("yyyy-MM-dd HH:mm:ss", bean.ctime),
                            (bean.limitTime / 1000).toLong(), bean.isComplainUser, bean.complainId, bean.buyer.imageUrl)
                }

            }

        }
        iv_payment_imageview?.visibility = View.INVISIBLE

        tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")

        tv_switching?.visibility = View.GONE
        ll_payment_type_layout?.isEnabled = false


        /**
         * 支付方式的信息
         */

        var paymentBean: OTCOrderDetailBean.Payment? = null
        if (bean?.payment.size > 0) {
            bean.payment.forEach {
                if (null == it) return@forEach
                if (it.payment == bean.payKey) {
                    paymentBean = it
                }
            }
        }


        if (paymentBean == null && bean?.payment.size > 0) {
            paymentBean = bean.payment[0]
        }

        if (bean.payment.size == 1) {
            tv_switching?.visibility = View.GONE
        }
        paymentList.clear()
        for (payment in bean.payment) {
            when (payment.payment) {
                "otc.payment.wxpay" -> {
                    paymentList.add(LanguageUtil.getString(this, "pyamethod_text_wxpay"))
                }
                "otc.payment.alipay" -> {
                    paymentList.add(LanguageUtil.getString(this, "payMethod_text_alipay"))
                }
                "otc.payment.domestic.bank.transfer" -> {
                    paymentList.add(LanguageUtil.getString(this, "new_otc_bank"))
                }
                "otc.payment.paypal" -> {
                    paymentList.add("PayPal")
                }
                else -> {
                    paymentList.add(payment.payment)
                }
            }
        }
        if (null != bean.payment && bean.payment.size > 0) {
            peymentString = setPaymentLayout(paymentBean ?: bean.payment[0])
        }

        /**
         *
         * status：订单状态:
         * 待收款 1
         * 代付款 2
         * 交易成功3
         * 取消 4
         * 申诉中 5
         * 打币中6
         * 异常订单7
         * 申诉处理结束8
         */

        when (bean.status) {
            1 -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this, "otc_text_orderWaitMoney"))
                btn_cancel?.isEnable(true)
                tv_switching?.visibility = View.VISIBLE
                waiting_attention_to?.visibility = View.VISIBLE
                waiting_attention_to?.text = LanguageUtil.getString(this, "otc_tip_remindSellerWaitPay")
                cub_confirm_for_buy?.isEnable(false)
                cub_confirm_for_buy?.setContent(LanguageUtil.getString(this, "otc_text_waitPay"))
                payment_information_layout?.visibility = View.GONE
                countTotalTime = bean.limitTime / 1000
                cancelBtnState()
            }

            2 -> {
                /**
                 * 待放币
                 */
                iv_payment_imageview?.visibility = View.GONE
                tv_switching?.visibility = View.GONE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                title_layout?.setContentTitle(LanguageUtil.getString(this, "otc_text_orderWaitSendCoin"))
                cub_confirm_for_buy?.setContent(LanguageUtil.getString(this, "otc_action_confirmSendCoin"))
                btn_cancel?.setContent(LanguageUtil.getString(this, "otc_action_appeal"))
                /**
                 * 5分钟之后才可以点击
                 * 申诉
                 */
                btn_cancel?.isEnable(true)
                var curTime = System.currentTimeMillis()
                if (bean.payTime.toLong() != 0L) {
                    clickStatus = (curTime / 1000 - 300 - bean.payTime.toLong() / 1000) > 0
                }
                pay_attention_to?.text = "对方已经支付，支付方式如上，请确认到账后，点击下方“确认收款并放币”按钮"
                cub_confirm_for_buy?.isEnable(true)
//                waiting_attention_to.visibility = View.VISIBLE
                disposeTime()
                /**
                 * 去申诉
                 */
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        if (clickStatus) {
                            NewComplaintActivity.enter2(this@NewVersionSellOrderActivity, bean.sequence, false, "9", payCoin)
                            finish()
                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewVersionSellOrderActivity, "otc_tip_appealTimeLimit"), isSuc = false)
                        }

                    }

                }

            }

            6 -> {
                /**
                 * 打币中
                 */
                payment_information_layout?.visibility = View.VISIBLE
                iv_payment_imageview?.visibility = View.GONE
                tv_switching?.visibility = View.GONE
                tv_money_copy?.visibility = View.GONE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                title_layout?.setContentTitle(LanguageUtil.getString(this, "otc_text_orderWaitSendCoin"))
                cub_confirm_for_buy?.setContent(LanguageUtil.getString(this, "otc_action_confirmSendCoin"))
                btn_cancel?.setContent(LanguageUtil.getString(this, "otc_action_appeal"))
                /**
                 * 5分钟之后才可以点击
                 * 申诉
                 */
                btn_cancel?.isEnable(true)
                var curTime = System.currentTimeMillis()
                if (bean.payTime.toLong() != 0L) {
                    clickStatus = curTime / 1000 - 300 - bean.payTime.toLong() / 1000 > 0
                }
                pay_attention_to?.text = "对方已经支付，支付方式如上，请确认到账后，点击下方“确认收款并放行”按钮"
                cub_confirm_for_buy?.isEnable(false)
//                waiting_attention_to.visibility = View.VISIBLE
                disposeTime()
                /**
                 * 取消
                 */
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        if (clickStatus) {
                            NewComplaintActivity.enter2(this@NewVersionSellOrderActivity, bean.sequence, false, "9", payCoin)
                            finish()
                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewVersionSellOrderActivity, "otc_tip_appealTimeLimit"), isSuc = false)
                        }

                    }

                }
            }
            7 -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this, "contract_text_orderError"))
                ll_trading_layout?.visibility = View.GONE
                tv_switching?.visibility = View.GONE
            }

            4 -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this, "filter_otc_cancel"))
                order_cancellation_layout?.visibility = View.VISIBLE
                line_layout?.visibility = View.GONE
                ll_trading_layout?.visibility = View.GONE
                tv_money_copy?.visibility = View.GONE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                payment_information_layout?.visibility = View.GONE
                when (bean.cancelStatus) {
                    "0" -> {
                        order_cancellation_layout?.visibility = View.GONE
                    }
                    "1" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelByBuyer")
                    }
                    "2" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelByAppeal")
                    }
                    "3" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelReasonNotPay")
                    }
                }
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        finish()
                    }
                }
            }

            3 -> {
                /***
                 * 显示订单完成的状态
                 */
                title_layout?.setContentTitle(LanguageUtil.getString(this, "otc_text_orderComplete"))
                ll_trading_layout?.visibility = View.GONE
                waiting_attention_to?.visibility = View.GONE
                iv_payment_imageview?.visibility = View.GONE
                pay_attention_to?.visibility = View.VISIBLE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                pay_attention_to?.text = title
                put_coin_code_layout?.visibility = View.VISIBLE
                pey_time_layout?.visibility = View.VISIBLE
                /**
                 * 放币时间
                 */
                if (!TextUtils.isEmpty(bean.sendCoinTime) && StringUtils.isNumeric(bean.sendCoinTime)) {
                    tv_put_coin?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.sendCoinTime.toLong())
                }
                /**
                 * 支付时间
                 */
                if (!TextUtils.isEmpty(bean.payTime) && StringUtils.isNumeric(bean.payTime)) {
                    tv_pay_time?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.payTime.toLong())
                }
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        finish()
                    }
                }
            }

            /**
             * 申诉中
             */
            5 -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this, "filter_otc_appeal"))
                pey_time_layout?.visibility = View.VISIBLE
                tv_pay_time_copy?.visibility = View.GONE
                tv_pay_time?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.payTime.toLong())


                iv_payment_imageview?.visibility = View.GONE
                tv_switching?.visibility = View.GONE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                if (bean.isComplainUser == 1) {
                    btn_cancel?.isEnable(true)
                    btn_cancel?.setContent(LanguageUtil.getString(this, "otc_action_cancelAppeal"))
                    cub_confirm_for_buy?.setContent(LanguageUtil.getString(this, "otc_text_orderPendingAppeal"))
                    cub_confirm_for_buy?.isEnable(false)
                    pay_attention_to?.text = LanguageUtil.getString(this, "otc_tip_appealOffence")
                    btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                        override fun bottonOnClick() {
                            NewDialogUtils.showNormalDialog(this@NewVersionSellOrderActivity, LanguageUtil.getString(this@NewVersionSellOrderActivity, "otc_tip_cancleAppealConfirm"), object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    cancelComplain4OTC()
                                }
                            }, "")
                        }
                    }

                } else {
                    pay_attention_to?.text = LanguageUtil.getString(this, "otc_tip_appealDefense")
                    btn_cancel?.visibility = View.GONE
                    cub_confirm_for_buy?.setContent(LanguageUtil.getString(this, "otc_tip_appealCharged"))
                    cub_confirm_for_buy?.isEnable(false)
                }


//                tv_complainCommand.text = bean?.complainCommand
            }

            8 -> {
                /***
                 * 申诉结束
                 * 显示订单完成的状态
                 */
                title_layout?.setContentTitle(LanguageUtil.getString(this, "otc_text_orderComplete"))
                ll_trading_layout?.visibility = View.GONE
                waiting_attention_to?.visibility = View.GONE
                iv_payment_imageview?.visibility = View.GONE

                pay_attention_to?.visibility = View.VISIBLE
                pay_attention_to?.text = title
                put_coin_code_layout?.visibility = View.VISIBLE
                pey_time_layout?.visibility = View.VISIBLE
                /**
                 * 放币时间
                 */
                if (!TextUtils.isEmpty(bean.sendCoinTime) && StringUtils.isNumeric(bean.sendCoinTime)) {
                    tv_put_coin?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.sendCoinTime.toLong())
                }
                /**
                 * 支付时间
                 */
                if (!TextUtils.isEmpty(bean.payTime) && StringUtils.isNumeric(bean.payTime)) {
                    tv_pay_time?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.payTime.toLong())
                }
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        finish()
                    }
                }
            }

            9 -> {
                /**
                 * 显示订单取消的状态
                 */
                title_layout?.setContentTitle(LanguageUtil.getString(this, "filter_otc_cancel"))
                order_cancellation_layout?.visibility = View.VISIBLE
                line_layout?.visibility = View.GONE
                ll_trading_layout?.visibility = View.GONE
                tv_money_copy?.visibility = View.GONE
                tv_payment_type?.text = LanguageUtil.getString(this, "common_text_paymentInfoSeller")
                payment_information_layout?.visibility = View.GONE
                when (bean.cancelStatus) {
                    "0" -> {
                        order_cancellation_layout?.visibility = View.GONE
                    }
                    "1" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelByBuyer")
                    }
                    "2" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelByAppeal")
                    }
                    "3" -> {
                        tv_order_cancellation_type?.text = LanguageUtil.getString(this, "otc_text_cancelReasonNotPay")
                    }
                }
                btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
                    override fun bottonOnClick() {
                        finish()
                    }
                }

            }
        }

        /**
         * 备注
         */
        tv_otc_note?.text = bean.description
        /**
         * 卖家昵称
         */
        ll_nick_name_layout?.visibility = View.GONE
        tv_user_nick_name?.text = bean.buyer.otcNickName

        tv_real_name.text = bean.buyer.realName

        /**
         * 判断是否显示真实姓名
         */
        if (bean.otcAuthnameOpen == "1") {
            tv_real_name_copy.visibility = View.VISIBLE
            tv_real_name_copy.setOnClickListener {
                if (bean?.isTwoMin == 0) {
                    NewDialogUtils.showSingleDialog(context, LanguageUtil.getString(this, "common_tip_showContactOTC"), object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {

                        }
                    })
                } else {
                    contactDilaog = NewDialogUtils.OTCOorderContactDialog(this, bean.buyer.mobileNumber, bean.buyer.email, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            contactDilaog?.dismiss()
                        }
                    })
                }
            }
        }


        /**
         * 订单号
         */
        tv_order_number?.text = LanguageUtil.getString(this, "otc_text_orderId") + " " + bean.sequence


        /**
         * 复制订单号
         */
        tv_order_number_copy.setOnClickListener {
            ClipboardUtil.copy(bean.sequence)
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
        }

        /**
        * 交易额
         */
        var paycoin = bean.paycoin
        tv_money_title?.text = LanguageUtil.getString(this, "journalAccount_text_amount") + "(${paycoin})"
        var totalPrice = bean.totalPrice
        var precision = RateManager.getFiat4Coin(paycoin)
        var totalPriceN = BigDecimalUtils.divForDown(totalPrice, precision).toPlainString()
        tv_money?.text = totalPriceN
        /**
         * 单价
         */
        tv_otc_price_title?.text = LanguageUtil.getString(this, "otc_text_price") + "(${paycoin})"
        var priceN = BigDecimalUtils.divForDown(bean.price, precision).toPlainString()
        tv_otc_price?.text = priceN

        /**
         * 交易数量
         */
        tv_quantity_title?.text = LanguageUtil.getString(this, "charge_text_volume") + "(${NCoinManager.getShowMarket(bean.coin)})"
        tv_quantity?.text = BigDecimalUtils.showSNormal(bean.volume.toString())


        tv_money_copy?.setOnClickListener {
            ClipboardUtil.copy(tv_money)
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
        }


        /**
         * 订单时间
         */

        tv_otc_place_order_time?.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.ctime)


        /**
         * 确认放款
         */
        cub_confirm_for_buy?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {

                if (UserDataService.getInstance().nickName.isEmpty() || UserDataService.getInstance().authLevel != 1 || (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1)) {
                    NewDialogUtils.OTCTradingPermissionsDialog(this@NewVersionSellOrderActivity, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (UserDataService.getInstance().nickName.isEmpty()) {
                                //认证状态 0、审核中，1、通过，2、未通过  3未认证
                                //PersonalInfoActivity.enter2(context!!)

                            } else if (UserDataService.getInstance().authLevel != 1) {
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        // RealNameCertificaionSuccessActivity.enter(context!!)
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                                    }

                                    2, 3 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                    }
                                }
                            } else {
                                ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            }
                        }

                    })
                    return
                }

                NewDialogUtils.tradingOTCConfirm(this@NewVersionSellOrderActivity, LanguageUtil.getString(this@NewVersionSellOrderActivity, "otc_action_confirmSendCoinTitle"), peymentString, selectPaymentBean?.userName
                        ?: "", BigDecimalUtils.showSNormal(bean.totalPrice.toString()) + bean.paycoin, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        disposables.clear()
                        disposeTime()
                        showPayCoinDialog()
                    }
                }, LanguageUtil.getString(this@NewVersionSellOrderActivity, "otc_action_confirmSendCoin"))
            }

        }

        ll_payment_type_layout?.setOnClickListener {
            if (bean.status == 1 && bean.payment.size != 1) {
                dialogForPayment = NewDialogUtils.showBottomListDialog(this@NewVersionSellOrderActivity, paymentList, selectPosition, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        selectPosition = item
                        peymentString = setPaymentLayout(bean.payment[item])
                        dialogForPayment?.dismiss()
                    }
                })
            }
        }

        if (bean?.showWarnTip && bean?.status == 1) {
            payment_information_layout?.visibility = View.GONE
            waiting_attention_to?.visibility = View.VISIBLE
            line_layout?.visibility = View.GONE
            waiting_attention_to?.text = LanguageUtil.getString(this, "otc_tip_orderDuringReview")
            title_layout?.setRightVisible(false)
        } else {
            pay_attention_to?.visibility = View.GONE
            title_layout?.setRightVisible(true)
        }
    }

    var selectPosition = 0
    var dialogForPayment: TDialog? = null

    var peymentString = ""
    var selectPaymentBean: OTCOrderDetailBean.Payment? = null
    fun setPaymentLayout(paymentBean: OTCOrderDetailBean.Payment): String {
        var peymentString = ""
        selectPaymentBean = paymentBean
        /**
         * 设置订单 卖家信息
         */
        when (paymentBean.payment) {
            "otc.payment.wxpay" -> {
                tv_payment_type?.text = LanguageUtil.getString(this, "pyamethod_text_wxpay")

                iv_payment_imageview?.setImageResource(R.drawable.wechat)

                tv_firstaname_title?.text = LanguageUtil.getString(this, "otc_text_payee")
                tv_firstaname?.text = paymentBean.userName

                tv_user_title?.text = LanguageUtil.getString(this, "otc_text_wxID")
                tv_user_content?.text = paymentBean.account
                tv_payment?.text = ""
                tv_payment_title?.text = LanguageUtil.getString(this, "wxpay_text_qrcode")
                peymentString = LanguageUtil.getString(this, "pyamethod_text_wxpay")
                peyment_code?.visibility = View.VISIBLE
                account_number_layout?.visibility = View.GONE
                tv_payment_copy?.setImageResource(R.drawable.fiat_orcode)
                tv_payment_copy?.setOnClickListener {
                    ShowImageActivity.enter2(this, paymentBean.qrcodeImg)
                }
                tv_user_copy?.visibility = View.VISIBLE
                tv_user_copy?.setOnClickListener {
                    Utils.copyString(tv_user_content)
                }
            }
            "otc.payment.paypal" -> {
                GlideUtils.loadImage(this, paymentBean.icon, iv_payment_imageview)

                if (TextUtils.isEmpty(paymentBean.icon)) {
                    iv_payment_imageview?.visibility = View.GONE
                }

                //收款人
                tv_firstaname_title?.text = LanguageUtil.getString(this, "otc_text_payee")
                tv_firstaname?.text = paymentBean.userName

                tv_payment_type?.text = paymentList[selectPosition]

                tv_user_title?.text = "PayPal" + LanguageUtil.getString(this, "noun_account_accountName")
                tv_user_content?.text = paymentBean.account
                tv_payment?.text = ""
                tv_payment_title?.text = ""
                peymentString = "PayPal"

                peyment_code?.visibility = View.GONE

                tv_payment_copy?.setImageResource(R.drawable.fiat_orcode)
                account_number_layout?.visibility = View.GONE
                tv_payment_copy?.setOnClickListener {
                    ShowImageActivity.enter2(this, paymentBean.qrcodeImg)
                }
                tv_user_copy?.visibility = View.VISIBLE
                tv_user_copy?.setOnClickListener {
                    Utils.copyString(tv_user_content)
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
                }
            }

            "otc.payment.alipay" -> {
                iv_payment_imageview?.setImageResource(R.drawable.alipay)
                //收款人
                tv_firstaname_title?.text = LanguageUtil.getString(this, "otc_text_payee")
                tv_firstaname?.text = paymentBean.userName

                tv_payment_type?.text = LanguageUtil.getString(this, "payMethod_text_alipay")

                tv_user_title?.text = LanguageUtil.getString(this, "alipay_text_account")
                tv_user_content?.text = paymentBean.account

                tv_payment_title?.text = LanguageUtil.getString(this, "alipay_text_qrcode")
                peymentString = LanguageUtil.getString(this, "payMethod_text_alipay")
                tv_payment?.text = ""
                peyment_code?.visibility = View.VISIBLE
                tv_payment_copy?.setImageResource(R.drawable.fiat_orcode)
                account_number_layout?.visibility = View.GONE
                tv_payment_copy?.setOnClickListener {
                    ShowImageActivity.enter2(this, paymentBean.qrcodeImg)
                }
                tv_user_copy?.visibility = View.VISIBLE
                tv_user_copy?.setOnClickListener {
                    Utils.copyString(tv_user_content)
                }
            }

            "otc.payment.domestic.bank.transfer" -> {
                iv_payment_imageview?.setImageResource(R.drawable.bankcard)
                //开户银行
                tv_firstaname_title?.text = LanguageUtil.getString(this, "otc_text_bankName")
                tv_firstaname?.text = paymentBean.bankName
                //开户支行
                tv_user_title?.text = LanguageUtil.getString(this, "otc_text_bankBranchName")
                tv_user_content?.text = paymentBean.bankOfDeposit
                tv_user_copy?.visibility = View.VISIBLE
                tv_user_copy?.setOnClickListener {
                    Utils.copyString(tv_user_content)
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
                }

                //收款人
                tv_payment_account_title?.text = LanguageUtil.getString(this, "otc_text_payee")
                tv_payment_account?.text = paymentBean.userName
                tv_payment_account_copy?.visibility = View.VISIBLE
                tv_payment_account_copy?.setImageResource(R.drawable.fiat_copy)
                tv_payment_copy?.setImageResource(R.drawable.fiat_copy)
                tv_payment_account_copy?.setOnClickListener {
                    Utils.copyString(tv_payment_account)
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
                }
                //卡号
                peyment_code?.visibility = View.VISIBLE
                tv_payment_title?.text = LanguageUtil.getString(this, "otc_text_paymentCardNumber")
                tv_payment?.text = paymentBean.account
                tv_payment_copy?.setOnClickListener {
                    Utils.copyString(tv_payment)
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_copySuccess"))
                }



                tv_payment_type?.text = LanguageUtil.getString(this, "new_otc_bank")
                peymentString = LanguageUtil.getString(this, "new_otc_bank")

                account_number_layout?.visibility = View.VISIBLE
            }
        }
        return peymentString
    }

    var fundsPsaaDialog: TDialog? = null

    /**
     * 资金密码弹窗
     */
    fun showPayCoinDialog() {
        fundsPsaaDialog = NewDialogUtils.showPwdDialog(this, LanguageUtil.getString(this, "otcSafeAlert_text_otcPwd"), object : NewDialogUtils.DialogBottomPwdListener {
            override fun returnCancel() {
                fundsPsaaDialog?.dismiss()
            }

            override fun returnContent(content: String) {
                confirmOrder2Seller4OTC(content)
                cub_confirm_for_buy.isEnable(false)
                fundsPsaaDialog?.dismiss()
            }

        }, LanguageUtil.getString(this, "safety_tip_inputOtcPassword"))
    }

    fun setOnClick() {

    }

    var confirmOrder = false
    var isfirst = true
    var countTotalTime = 60
    private var mdDisposable: Disposable? = null

    /**
     * 处理取消按钮
     */
    private fun cancelBtnState() {
        if (isfirst) {
            isfirst = !isfirst
        } else {
            return
        }
        mdDisposable = Flowable.intervalRange(0, countTotalTime.toLong(), 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(object : Consumer<Long> {
                    override fun accept(t: Long) {
                        if (otcOrderDetailBean?.status != 1) {
                            return
                        }
                        if (confirmOrder) {
                            return
                        }
                        if ((countTotalTime - t.toInt()) == 0) {
                            getOrderDetail4OTC()
                            return
                        }

                        var formatLongToTimeStr = formatLongToTimeStr((countTotalTime - t.toInt()).toLong())

                        var split = formatLongToTimeStr.split(":")

                        try {
                            btn_cancel.setContent(split[0] + "'" + split[1] + "\"" + LanguageUtil.getString(this@NewVersionSellOrderActivity, "oct_action_autoCancelDesc"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    }
                })


                .doOnComplete(object : Action {
                    override fun run() {
                        //倒计时完毕置为可点击状态
                        getOrderDetail4OTC()
                    }
                })
                .subscribe()

        /**
         * 取消
         */
        btn_cancel?.setOnClickListener {
            finish()
        }
    }

    fun formatLongToTimeStr(l: Long): String {
        if (isFinishing || isDestroyed) return ""
        var minute = 0
        var second = l.toInt()
        if (second > 60) {
            minute = second / 60 //取整
            second %= 60 // 取余
        }

        var strtime = ""

        if (minute < 10) {
            strtime += "0$minute:"
        } else {
            strtime += "$minute:"
        }
        if (second < 10) {
            strtime += "0$second"
        } else {
            strtime += "$second"
        }
        return strtime
    }

    /**
     * 确认放币
     */
    private fun confirmOrder2Seller4OTC(capitalPword: String) {
        showProgressDialog()
        HttpClient.instance
                .confirmOrder2Seller4OTC(sequence = orderId, capitalPword = capitalPword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        cancelProgressDialog()
                        /**
                         * TODO
                         * 重新获取订单信息
                         * 该：等待卖家放币
                         */
                        finish()

                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        cub_confirm_for_buy.isEnable(true)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

    /**
     * 获取订单详情
     */
    private fun getOrderDetail4OTC() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        showProgressDialog()
        HttpClient.instance
                .getOrderDetail4OTC(sequence = orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderDetailBean>() {
                    override fun onHandleSuccess(t: OTCOrderDetailBean?) {
                        cancelProgressDialog()
                        ll_trading_layout?.visibility = View.VISIBLE
                        nsv_layout?.visibility = View.VISIBLE
                        t ?: return
                        initView(t)

                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

    /**
     * 每1分钟调用一次接口x
     */
    private fun getOrderStateEachMin() {
        disposables?.add(Observable.interval(0, 60, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        disposeTime()

    }

    fun disposeTime() {
        if (mdDisposable != null) {
            mdDisposable?.dispose()
        }
    }

    fun getObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                Log.d("x", t.toString() + "time")
                loopOrderState()
            }

            override fun onError(e: Throwable) {
            }
        }

    }

    /**
     * 轮询订单接口
     */
    fun loopOrderState() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        HttpClient.instance
                .getOrderDetail4OTC(sequence = orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderDetailBean>() {
                    override fun onHandleSuccess(t: OTCOrderDetailBean?) {
                        t ?: return
                        initView(t)
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

    /**
     * 取消申诉
     */
    private fun cancelComplain4OTC() {
        showProgressDialog()
        HttpClient.instance
                .cancelComplain4OTC(sequence = orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        cancelProgressDialog()
                        /***
                         * 重新获取订单的状态
                         */
                        getOrderDetail4OTC()
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }
}