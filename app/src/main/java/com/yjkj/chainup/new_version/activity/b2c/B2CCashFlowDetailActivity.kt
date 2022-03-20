package com.yjkj.chainup.new_version.activity.b2c

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.ShowImageActivity
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.util.ToastUtils
import kotlinx.android.synthetic.main.activity_b2_ccash_flow_detail.*
import org.json.JSONObject

/**
 * @description:资金流水详情(B2C)
 * @author Bertking
 * @date 2019-10-23 AM
 */
@Route(path = RoutePath.B2CCashFlowDetailActivity)
class B2CCashFlowDetailActivity : NBaseActivity() {

    @JvmField
    @Autowired(name = "isRecharge")
    var isRecharge = true

    @JvmField
    @Autowired(name = "detail_data")
    var detailData = ""


    override fun setContentView() = R.layout.activity_b2_ccash_flow_detail


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            expandedTitleGravity = Gravity.BOTTOM
            title = LanguageUtil.getString(this@B2CCashFlowDetailActivity,"journalAccount_text_detail")
        }


        if (TextUtils.isEmpty(detailData)) return

        val jsonObject = JSONObject(detailData)

        /* 提现时间OR充值时间 */
        tv_time_title?.text = if (isRecharge) LanguageUtil.getString(this,"b2c_Recharge_Time") else LanguageUtil.getString(this,"b2c_Withdrawal_Time")
        val createTimeAt = jsonObject.optString("createdAtTime", "")
        val createTime = if (StringUtil.checkStr(createTimeAt)) {
            DateUtil.longToString("yyyy/MM/dd HH:mm", createTimeAt.toLong())
        } else {
            "--"
        }
        tv_time?.text = createTime
        /* 币种 */
        val symbol = jsonObject.optString("coinSymbol", "")
        val precision = NCoinManager.getCoinShowPrecision(symbol)


        tv_coin?.text = symbol
        /* 到账金额OR申请数量 */
        tv_third_title?.text = if (isRecharge) LanguageUtil.getString(this,"b2c_Application_Amount") else LanguageUtil.getString(this,"b2c_Arrive_Time")
        tv_third?.text = BigDecimalUtils.divForDown(jsonObject.optString("amount"), precision).toPlainString()


        /* 手续费OR实际到账 */
        tv_fourth_title?.text = if (isRecharge) LanguageUtil.getString(this,"withdraw_text_moneyWithoutFee") else LanguageUtil.getString(this,"withdraw_text_fee")

        tv_fourth?.text = if (isRecharge) {
            BigDecimalUtils.divForDown(jsonObject.optString("settledAmount"), precision).toPlainString()
        } else {
            BigDecimalUtils.divForDown(jsonObject.optString("fee"), precision).toPlainString()
        }
        /**
         *  状态:
         *  待审核、已放款、已拒绝、已撤销
         */
        tv_status?.text = jsonObject.optString("status_text", "")
        /* 处理时间 */
        /**
         * 日期yyyy-MM-dd HH:mm:ss
         */
        val timeLong = jsonObject.optString("updateTime", "")
        val updateTime = if (StringUtil.checkStr(timeLong)) {
            DateUtil.longToString("yyyy/MM/dd HH:mm", timeLong.toLong())
        } else {
            "--"
        }
        tv_handle_time?.text = updateTime
        // 收款方式(目前只支持银行卡)
        tv_payment?.text = when (jsonObject.optString("transferType", "1")) {
            "1" -> {
                LanguageUtil.getString(this,"otc_text_bankCard")
            }
            else -> {
                LanguageUtil.getString(this,"otc_text_bankCard")
            }
        }


        /* 收款人 */
        tv_payee?.text = jsonObject.optString("userName", "")
        /* 转账凭证 */
        val transferVoucher = jsonObject.optString("transferVoucher", "")
        if(TextUtils.isEmpty(transferVoucher)){
            tv_look?.visibility = View.GONE
            tv_transfer_voucher_title?.visibility = View.GONE
            v_transfer_voucher?.visibility = View.GONE
        }else{
            tv_look?.visibility = View.VISIBLE
            tv_transfer_voucher_title?.visibility = View.VISIBLE
            v_transfer_voucher?.visibility = View.VISIBLE
        }

        tv_look?.setOnClickListener {
            ShowImageActivity.enter2(this, transferVoucher, false)
        }

        val status = jsonObject.optString("status", "-1")
        if (status == "0") {
            btn_cancel?.visibility = View.VISIBLE
        } else {
            btn_cancel?.visibility = View.GONE
        }

        val id = jsonObject.optString("id", "")
        btn_cancel?.clicked = true
        btn_cancel?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (isRecharge) {
                    cancelRechargeAction(id)
                } else {
                    cancelWithdrawAction(id)
                }
            }
        }

    }


    fun cancelRechargeAction(id: String) {
        addDisposable(getMainModel().fiatCancelDeposit(id = id,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        finish()
                    }
                }))
    }


    fun cancelWithdrawAction(id: String) {
        addDisposable(getMainModel().fiatCancelWithdraw(id = id,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        finish()
                    }
                }))
    }

}
