package com.yjkj.chainup.new_version.activity.asset

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.view.BorrowingAndReturnView
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.activity_give_back.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-11-11-11:48
 * @Email buptjinlong@163.com
 * @description  归还
 */
@Route(path = RoutePath.GiveBackActivity)
class GiveBackActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_give_back


    var giveBackJSONObject = JSONObject()
    var symbolJsonobject = JSONObject()

    @JvmField
    @Autowired(name = ParamConstant.JSON_BEAN)
    var json = ""

    var coin = ""
    var symbol = ""
    var oweAmount = ""

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        collapsing_toolbar?.title = LanguageUtil.getString(this,"asset_give_back")
        ArouterUtil.inject(this)
        initView()
        bar_layout?.setFirstTitleContent(LanguageUtil.getString(this,"leverage_asset"))
        bar_layout?.setSecondTitleContent(LanguageUtil.getString(this,"leverage_returnCoin"))
        bar_layout?.setThirdTitleContent(LanguageUtil.getString(this,"leverage_shouldReturn_amount"))
        bar_layout?.setFourthTitleContent(LanguageUtil.getString(this,"leverage_totalBorrow_amount"))
        bar_layout?.setFifthTitleContent(LanguageUtil.getString(this,"leverage_interest"))
        bar_layout?.setColumeTitle(LanguageUtil.getString(this,"charge_text_volume"))
    }


    override fun initView() {
        super.initView()
        giveBackJSONObject = JSONObject(json)
        if (null != giveBackJSONObject) {
            setDataView()
        }
    }


    fun setDataView() {
        //TODO 确定精度后加上精度
        if (null == giveBackJSONObject) return
        coin = giveBackJSONObject.optString("coin", "")
        symbol = giveBackJSONObject.optString("symbol", "")
        precision = NCoinManager.getCoinShowPrecision(coin)
        oweAmount = BigDecimalUtils.divForDown(BigDecimalUtils.add(giveBackJSONObject.optString("oweAmount", ""), giveBackJSONObject.optString("oweInterest", "")).toPlainString(), 8).toPlainString()
        bar_layout?.setFirst(NCoinManager.getShowMarketName(symbol))
        bar_layout?.setSecond(NCoinManager.getShowMarket(coin))
        bar_layout?.setEdittextFilter(8)
        bar_layout?.setThird("$oweAmount ${NCoinManager.getShowMarket(coin)}")
        bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(coin))
        bar_layout?.setFourth(BigDecimalUtils.divForDown(giveBackJSONObject.optString("borrowMoney", ""), 8).toPlainString())
        bar_layout?.setFifth(BigDecimalUtils.divForDown(giveBackJSONObject.optString("oweInterest", ""), 8).toPlainString())
        getBalanceList(symbol)
        bar_layout?.listener = object : BorrowingAndReturnView.AllBtnClickListener {
            override fun btnClick() {
                var canBorrow = ""
                var orecision = 0
                var coin = ""
                canBorrow = BigDecimalUtils.add(giveBackJSONObject.optString("oweAmount", ""), giveBackJSONObject.optString("oweInterest", "")).toPlainString()
                coin = giveBackJSONObject.optString("coin", "")
                orecision = NCoinManager.getCoinShowPrecision(coin)

                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(coin))
                var max = BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(canBorrow, orecision).toPlainString(), BigDecimalUtils.divForDown(normalBalance, orecision).toPlainString())
                /**
                 * 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
                 */
                if (max == 1) {
                    bar_layout?.setEdittextContent(BigDecimalUtils.divForDown(normalBalance, 8).toPlainString())
                } else {
                    bar_layout?.setEdittextContent(BigDecimalUtils.divForDown(canBorrow, 8).toPlainString())
                }
            }

        }
        btn_confirm?.isEnable(true)
        btn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var minVolume = bar_layout?.minVolume
                if (TextUtils.isEmpty(minVolume)) {
                    minVolume = "0"
                }
                if (BigDecimalUtils.compareTo(minVolume, "0") == 0) {
                    bar_layout?.setReturnError(LanguageUtil.getString(this@GiveBackActivity,"filter_Input_placeholder"))
                } else {
                    setReturn(giveBackJSONObject.optString("id", ""), bar_layout?.minVolume ?: "0")
                }
            }
        }

    }

    /**
     * 归还
     */
    fun setReturn(id: String, amount: String) {
        addDisposable(getMainModel().setReturn(id, amount, object : NDisposableObserver(mActivity,true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                finish()
            }
        }))
    }

    /**
     * 可用
     */
    var normalBalance = ""
    /**
     * 还款精度
     */
    var precision = 0
    /**
     * 最小归还
     */
    var minBorrow = ""

    /**
     * 获取杠杆账户列表
     */
    fun getBalanceList(symbol: String) {
        addDisposable(getMainModel().getBalance4Lever(symbol, object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                symbolJsonobject = jsonObject.optJSONObject("data")
                if (null != symbolJsonobject) {
                    if (coin == symbolJsonobject.optString("quoteCoin", "")) {
                        normalBalance = symbolJsonobject.optString("quoteNormalBalance", "")
                        minBorrow = symbolJsonobject.optString("quoteMinPayment", "")

                    } else if (coin == symbolJsonobject.optString("baseCoin", "")) {
                        normalBalance = symbolJsonobject.optString("baseNormalBalance", "")
                        minBorrow = symbolJsonobject.optString("baseMinPayment", "")
                    }
                }
                bar_layout?.setFirst(NCoinManager.getShowMarketName(symbolJsonobject.optString("name", "$symbol")))
                bar_layout?.setGiveEndTextViewContent("${BigDecimalUtils.divForDown(normalBalance, 8).toPlainString()}")
                bar_layout?.setEditHintGiveBackContent("${LanguageUtil.getString(this@GiveBackActivity,"withdraw_text_minimumVolume")}${BigDecimalUtils.divForDown(minBorrow, 8).toPlainString()}")
            }
        }))
    }
}