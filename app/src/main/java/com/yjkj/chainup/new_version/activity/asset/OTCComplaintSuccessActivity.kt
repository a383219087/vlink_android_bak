package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.otcTrading.NewVersionBuyOrderActivity.Companion.ORDERID
import com.yjkj.chainup.new_version.bean.OTCOrderDetailBean
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_buy_order.*
import kotlinx.android.synthetic.main.item_new_version_order.*
import kotlinx.android.synthetic.main.item_payment_information.*

/**
 * @Author lianshangljl
 * @Date 2018/10/15-上午11:46
 * @Email buptjinlong@163.com
 * @description 申诉成功页面
 */
const val OTC_ORDER = "otc_complain_order"
const val OTC_Trading_STATUS = "otc_trading_status"

class OTCComplaintSuccessActivity : NewBaseActivity() {

    var orderId = ""

    companion object {

        /**
         *  @param orderId 订单id
         *  @param tradingType 状态 买或者卖
         */
        fun newIntent(context: Context, orderId: String, tradingType: Boolean) {
            var intent = Intent(context, OTCComplaintSuccessActivity::class.java)
            intent.putExtra(OTC_ORDER, orderId)
            intent.putExtra(OTC_Trading_STATUS, tradingType)
            context.startActivity(intent)
        }
    }

    var handler: Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_buy_order)
        getOrderDetail4OTC()

    }

    fun initView(bean: OTCOrderDetailBean) {
        /**
         * 点击消息
         */
        title_layout.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickName() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {

            }

        }


        /**
         *
         * status：订单状态:
         * 待支付1
         * 已支付2
         * 交易成功3
         * 取消 4
         * 申诉中 5
         * 打币中6
         * 异常订单7
         * 申诉处理结束8
         */

        when (bean.status) {
            1 -> {
                title_layout.setContentTitle(LanguageUtil.getString(this,"otc_please_payment"))
                pay_attention_to.text = LanguageUtil.getString(this,"otc_tip_buyerConfirmPaid")
                cub_confirm_for_buy.setContent(LanguageUtil.getString(this,"otc_action_buyerDidPay"))
                tv_user_copy.visibility = View.VISIBLE
                tv_payment_account_copy.visibility = View.VISIBLE

            }

            2, 6 -> {
                title_layout.setContentTitle(LanguageUtil.getString(this,"otc_text_orderWaitSendCoin"))
                cub_confirm_for_buy.setContent(LanguageUtil.getString(this,"otc_tip_sellerPendingCoin"))
                pay_attention_to.text = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                    LanguageUtil.getString(this,"otc_tip_remindBuyerWaitConfirm_forotc")
                } else {
                    LanguageUtil.getString(this,"otc_tip_remindBuyerWaitConfirm")
                }


                btn_cancel.setContent(LanguageUtil.getString(this,"otc_action_appeal"))
                btn_cancel.isEnable(false)
                cub_confirm_for_buy.isEnable(false)
                payment_information_layout.visibility = View.GONE
                waiting_attention_to.visibility = View.VISIBLE

            }


            4 -> {
                title_layout.setContentTitle(LanguageUtil.getString(this,"filter_otc_cancel"))
                order_cancellation_layout.visibility = View.VISIBLE
                line_layout.visibility = View.GONE
                ll_trading_layout.visibility = View.GONE
                when (bean.cancelStatus) {
                    "0" -> {

                    }
                    "1" -> {
                        tv_order_cancellation_type.text = LanguageUtil.getString(this,"otc_text_cancelByBuyer")
                    }
                    "2" -> {
                        tv_order_cancellation_type.text = LanguageUtil.getString(this,"otc_text_cancelByAppeal")
                    }
                    "3" -> {
                        tv_order_cancellation_type.text = LanguageUtil.getString(this,"otc_text_cancelReasonNotPay")
                    }
                }

            }

            3 -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this,"otc_text_orderComplete"))
                ll_trading_layout?.visibility = View.GONE
                waiting_attention_to?.visibility = View.GONE
                pay_attention_to?.visibility = View.VISIBLE

                pay_attention_to?.text = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                    LanguageUtil.getString(this,"otc_tip_buyerOrderComplete_forotc")
                } else {
                    LanguageUtil.getString(this,"otc_tip_buyerOrderComplete")
                }

                put_coin_code_layout?.visibility = View.VISIBLE
                pey_time_layout?.visibility = View.VISIBLE
            }

            /**
             * 申诉中
             */
            5 -> {
                title_layout.setContentTitle(LanguageUtil.getString(this,"filter_otc_appeal"))
                pey_time_layout.visibility = View.VISIBLE
                tv_pay_time_copy.visibility = View.GONE
                btn_cancel.setContent(LanguageUtil.getString(this,"otc_cancel_complaint"))
                tv_pay_time.text = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", bean.payTime.toLong())


//                tv_complainCommand.text = bean?.complainCommand
            }

            8 -> {
                /***
                 * 申诉结束
                 * 显示订单完成的状态
                 */
//                handler.postDelayed({
//                    OTCOrderStateActivity.enter2(context, true, false, orderId)
//                    finish()
//                }, 3000)

            }

            9 -> {
                /**
                 * 显示订单取消的状态
                 */
//                handler.postDelayed({
//                    OTCOrderStateActivity.enter2(context, false, false, orderId)
//                    finish()
//                }, 3000)
            }


        }

        /**
         * 卖家昵称
         */
        tv_user_nick_name.text = bean.seller.otcNickName


        /**
         * 订单号
         */
        tv_order_number.text = bean.sequence

        /**
         * 交易额
         */
        tv_money.text = BigDecimalUtils.showSNormal(bean.totalPrice.toString()) + " ${RateManager.getCurrencyLang()}"
        /**
         * 交易单价
         */
        tv_otc_price.text = BigDecimalUtils.showSNormal(bean.price.toString()) + " ${bean.paycoin}"

        /**
         * 交易数量
         */
        tv_quantity.text = BigDecimalUtils.showSNormal(bean.volume.toString()) + " ${bean.coin}"


//        /**
//         * 支付方式的信息
//         */
//
//        val accountStr = bean?.payment.account as String
//        val paymentBean = JsonUtils.jsonToBean(accountStr, PaymentBean::class.java)
//
//        /**
//         * 订单时间
//         */
//
//        tv_otc_place_order_time.text = DateUtils.longToString("yyyy/MM/dd HH:mm:ss", bean.ctime)
//
//        /**
//         * 设置订单 卖家信息
//         */
//        when (paymentBean.payment) {
//            "otc.payment.wxpay" -> {
//                iv_payment_imageview.setImageResource(R.drawable.wechat)
//                tv_firstaname.text = paymentBean.userName
//                tv_firstaname_title.text = getString(R.string.firstname)
//                tv_user_title.text = getString(R.string.new_otc_account)
//                tv_payment_type.text = getString(R.string.new_otc_wx)
//                tv_user_content.text = paymentBean.account
//                tv_payment_title.text = "微信收款码"
//
//                peyment_code.visibility = View.VISIBLE
//                account_number_layout.visibility = View.GONE
//                GlideUtils.loadNetCoinIcon(this, bean?.payment.icon, tv_payment_copy)
//            }
//
//
//            "otc.payment.alipay" -> {
//                iv_payment_imageview.setImageResource(R.drawable.alipay)
//                tv_firstaname.text = paymentBean.userName
//                tv_firstaname_title.text = getString(R.string.firstname)
//                tv_payment_type.text = getString(R.string.new_otc_alipay)
//                tv_user_content.text = paymentBean.account
//                tv_user_title.text = getString(R.string.new_otc_account)
//                tv_payment_title.text = "支付宝收款码"
//                peyment_code.visibility = View.VISIBLE
//                GlideUtils.loadNetCoinIcon(this, bean?.payment.icon, tv_payment_copy)
//                account_number_layout.visibility = View.GONE
//            }
//
//            "otc.payment.domestic.bank.transfer" -> {
//                iv_payment_imageview.setImageResource(R.drawable.bankcard)
//                tv_firstaname.text = paymentBean.bankName
//                tv_firstaname_title.text = getString(R.string.bank)
//                tv_payment_type.text = getString(R.string.new_otc_bank)
//                tv_payment_account_title.text = getString(R.string.account_name)
//                account_number_layout.visibility = View.VISIBLE
//                tv_payment_account.text = paymentBean.bankOfDeposit
//            }
//        }

    }


    /**
     * 获取订单详情
     */
    private fun getOrderDetail4OTC() {
        orderId = intent?.getStringExtra(ORDERID)!!
        showProgressDialog()
        HttpClient.instance
                .getOrderDetail4OTC(sequence = orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderDetailBean>() {
                    override fun onHandleSuccess(t: OTCOrderDetailBean?) {
                        cancelProgressDialog()
                        t ?: return
                        initView(t)

                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        ToastUtils.showToast(msg)
                    }
                })
    }


}