package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.flyco.tablayout.listener.OnTabSelectListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.data.bean.TabEntity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.ActivityManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.BorrowingAndReturnView
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.BigDecimalUtils
import kotlinx.android.synthetic.main.activity_borrowing.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-11-09-10:44
 * @Email buptjinlong@163.com
 * @description  借贷
 */
@Route(path = RoutePath.NewVersionBorrowingActivity)
class NewVersionBorrowingActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_borrowing


    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    var selectEmptyOrMore: ArrayList<String> = arrayListOf()


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
        collapsing_toolbar?.title = LanguageUtil.getString(this, "leverage_borrow")
        ArouterUtil.inject(this)
        bar_layout?.setFirstTitleContent(LanguageUtil.getString(this, "leverage_asset"))
        bar_layout?.setSecondTitleContent(LanguageUtil.getString(this, "leverage_have_borrowed"))
        bar_layout?.setThirdTitleContent(LanguageUtil.getString(this, "leverage_text_biggestLimit"))
        bar_layout?.setFourthTitleContent(LanguageUtil.getString(this, "leverage_rate"))
        bar_layout?.setColumeTitle(LanguageUtil.getString(this, "charge_text_volume"))
        tv_recharge_record?.text = LanguageUtil.getString(this, "leverage_borrowRecord")
        btn_confirm?.setBottomTextContent(LanguageUtil.getString(this, "leverage_borrow"))
    }


    override fun onResume() {
        super.onResume()
        bar_layout?.setEdittextContent("")
        getBalanceList()
    }

    var symbolJSONObject = JSONObject()


    /**
     * 获取杠杆账户列表
     */
    fun getBalanceList() {
        addDisposable(getMainModel().getBalance4Lever(symbol, object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                symbolJSONObject = jsonObject.optJSONObject("data")
                if (null != symbolJSONObject) {
                    setEmptyAndMore()
                }

            }
        }))
    }


    /**
     * 填充数据
     */
    fun setBorrowingAndReturnView() {
        bar_layout?.setFirst(NCoinManager.getShowMarketName(symbolJSONObject.optString("name", "")))
        var rate = BigDecimalUtils.mulStr(symbolJSONObject.optString("rate"), "100",2)
        when (dialogSelect) {
            /**
             * 做空
             */
            ParamConstant.TYPE_BUY -> {

                bar_layout?.setSecond("${BigDecimalUtils.divForDown(symbolJSONObject.optString("baseBorrowBalance", ""), ParamConstant.NORMAL_PRECISION).toPlainString()} ${NCoinManager.getShowMarket(baseCoin)}")
                bar_layout?.setThird("${BigDecimalUtils.divForDown(BigDecimalUtils.add(symbolJSONObject.optString("baseCanBorrow", ""), symbolJSONObject.optString("baseBorrowBalance", "")).toPlainString(), ParamConstant.NORMAL_PRECISION).toPlainString()} ${NCoinManager.getShowMarket(baseCoin)}")
                bar_layout?.setFourth("$rate%")
                bar_layout?.setEdittextFilter(ParamConstant.NORMAL_EDITTEXT)
                bar_layout?.setEditHintContent(BigDecimalUtils.divForDown(symbolJSONObject.optString("baseMinBorrow", ""), ParamConstant.NORMAL_EDITTEXT).toPlainString())
                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(symbolJSONObject.optString("baseCoin", "")))
                bar_layout?.setEndTextViewContent(BigDecimalUtils.divForDown(symbolJSONObject.optString("baseCanBorrow", ""), ParamConstant.NORMAL_EDITTEXT).toPlainString())
            }
            /**
             * 做多
             */
            ParamConstant.TYPE_SELL -> {

                bar_layout?.setSecond("${BigDecimalUtils.divForDown(symbolJSONObject.optString("quoteBorrowBalance", ""), ParamConstant.NORMAL_PRECISION).toPlainString()} ${NCoinManager.getShowMarket(quoteCoin)}")
                bar_layout?.setThird("${BigDecimalUtils.divForDown(BigDecimalUtils.add(symbolJSONObject.optString("quoteCanBorrow", ""), symbolJSONObject.optString("quoteBorrowBalance", "")).toPlainString(), ParamConstant.NORMAL_PRECISION).toPlainString()} ${NCoinManager.getShowMarket(quoteCoin)}")
                bar_layout?.setFourth("$rate%")
                bar_layout?.setEdittextFilter(ParamConstant.NORMAL_EDITTEXT)
                bar_layout?.setEditHintContent(BigDecimalUtils.divForDown(symbolJSONObject.optString("quoteMinBorrow", ""), ParamConstant.NORMAL_EDITTEXT).toPlainString())
                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(symbolJSONObject.optString("quoteCoin", "")))
                bar_layout?.setEndTextViewContent(BigDecimalUtils.divForDown(symbolJSONObject.optString("quoteCanBorrow", ""), ParamConstant.NORMAL_EDITTEXT).toPlainString())
            }
        }
        btn_confirm?.isEnable(true)
        /**
         * 借贷按钮
         */
        btn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (setConfirmError(bar_layout?.minVolume ?: "0")) {
                    when (dialogSelect) {
                        /**
                         * 做空
                         */
                        ParamConstant.TYPE_BUY -> {
                            setBorrowing(symbolJSONObject.optString("symbol", ""), symbolJSONObject.optString("baseCoin", ""), bar_layout?.minVolume
                                    ?: "0")
                        }
                        /**
                         * 做多
                         */
                        ParamConstant.TYPE_SELL -> {
                            setBorrowing(symbolJSONObject.optString("symbol", ""), symbolJSONObject.optString("quoteCoin", ""), bar_layout?.minVolume
                                    ?: "0")
                        }

                    }

                }
            }
        }


        /**
         * 点击选择币对
         */
        rl_lever_account_layout?.setOnClickListener {
            ArouterUtil.navigation4Result(RoutePath.CoinMapActivity, Bundle().apply {
                putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER, true)
            }, mActivity, ParamConstant.BORROW_TYPE)
            bar_layout?.setEdittextContent("")
        }
        /**
         * 点击全部
         */
        bar_layout?.listener = object : BorrowingAndReturnView.AllBtnClickListener {
            override fun btnClick() {

                var canBorrow = ""
                var orecision = 0
                var normalBalance = ""
                var coin = ""
                when (dialogSelect) {
                    ParamConstant.TYPE_BUY -> {
                        canBorrow = symbolJSONObject.optString("baseCanBorrow", "")
                        coin = symbolJSONObject.optString("baseCoin", "")
                        normalBalance = symbolJSONObject.optString("baseNormalBalance", "")
                        orecision = NCoinManager.getCoinShowPrecision(symbolJSONObject.optString("baseCoin", ""))
                    }
                    ParamConstant.TYPE_SELL -> {
                        canBorrow = symbolJSONObject.optString("quoteCanBorrow", "")
                        coin = symbolJSONObject.optString("quoteCoin", "")
                        normalBalance = symbolJSONObject.optString("quoteNormalBalance", "")
                        orecision = NCoinManager.getCoinShowPrecision(symbolJSONObject.optString("quoteCoin", ""))
                    }
                }
                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(coin))
                /**
                 * 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
                 */
                bar_layout?.setEdittextContent(BigDecimalUtils.divForDown(canBorrow, ParamConstant.NORMAL_EDITTEXT).toPlainString())
            }
        }
        if (BigDecimalUtils.compareTo(symbolJSONObject.optString("symbolBalance"), "0") == 0 && !isShow) {
            showTransferDialog()
            return
        }
    }

    fun setConfirmError(amount: String): Boolean {
        if (BigDecimalUtils.compareTo("0", amount) == 0 || amount == "") {
            return false
        }
        /**
         * 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
         */
        when (dialogSelect) {
            /**
             * 做空
             */
            ParamConstant.TYPE_BUY -> {

                val min = BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(symbolJSONObject.optString("baseMinBorrow", ""), NCoinManager.getCoinShowPrecision(baseCoin)).toPlainString(), amount)
                if (min == 1) {
                    var msg = String.format(LanguageUtil.getString(mActivity, "leverage_text_noLess"), BigDecimalUtils.showSNormal(symbolJSONObject.optString("baseMinBorrow", "")).toString(), NCoinManager.getShowMarket(baseCoin))
                    showSnackBar(msg, isSuc = false)
                    bar_layout?.setReturnError(msg)
                    return false
                }
                val max = BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(symbolJSONObject.optString("baseCanBorrow", ""), NCoinManager.getCoinShowPrecision(baseCoin)).toPlainString(), amount)
                if (max == -1) {
                    showSnackBar(LanguageUtil.getString(mActivity, "leverage_text_lessThanCanuse"), isSuc = false)
                    bar_layout?.setReturnError(LanguageUtil.getString(mActivity, "leverage_text_lessThanCanuse"))
                    return false
                }

            }
            /**
             * 做多
             */
            ParamConstant.TYPE_SELL -> {

                val min = BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(symbolJSONObject.optString("quoteMinBorrow", ""), NCoinManager.getCoinShowPrecision(baseCoin)).toPlainString(), amount)
                /**
                 * 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
                 */
                if (min == 1) {
                    var msg = String.format(LanguageUtil.getString(mActivity, "leverage_text_noLess"), BigDecimalUtils.showSNormal(symbolJSONObject.optString("quoteMinBorrow", "")).toString(), NCoinManager.getShowMarket(quoteCoin))
                    showSnackBar(msg, isSuc = false)
                    bar_layout?.setReturnError(msg)
                    return false
                }
                val max = BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(symbolJSONObject.optString("quoteCanBorrow", ""), NCoinManager.getCoinShowPrecision(baseCoin)).toPlainString(), amount)
                if (max == -1) {
                    showSnackBar(LanguageUtil.getString(mActivity, "leverage_text_lessThanCanuse"), isSuc = false)
                    bar_layout?.setReturnError(LanguageUtil.getString(mActivity, "leverage_text_lessThanCanuse"))
                    return false
                }
            }
        }
        return true
    }
    private var isShow = false
    private fun showTransferDialog() {
        var msg = String.format(LanguageUtil.getString(mActivity, "leverage_notEnught_prompt"), NCoinManager.getMarketName4Symbol(symbolJSONObject.optString("symbol")))
        isShow = true
        NewDialogUtils.showNormalDialog(this@NewVersionBorrowingActivity, msg, object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                ActivityManager.pushAct2Stack(this@NewVersionBorrowingActivity)
                ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                    putString(ParamConstant.TRANSFERSTATUS, ParamConstant.LEVER_INDEX)
                    putString(ParamConstant.TRANSFERSYMBOL, "")
                    putString(ParamConstant.TRANSFERCURRENCY, symbol)
                    putBoolean(ParamConstant.FROMBORROW, true)
                })
                finish()
            }

        }, "", LanguageUtil.getString(mActivity, "assets_action_transfer"), LanguageUtil.getString(mActivity, "common_text_btnCancel"))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                /** 币种*/
                ParamConstant.BORROW_TYPE -> {
                    symbol = data?.getStringExtra(ParamConstant.symbol) ?: ""
                }
            }
        }
    }

    var dialogSelect = 0
    var baseCoin = ""
    var quoteCoin = ""

    /**
     * 获取数据多和空
     */
    fun setEmptyAndMore() {
        baseCoin = symbolJSONObject.optString("baseCoin", "")
        quoteCoin = symbolJSONObject.optString("quoteCoin", "")
        selectEmptyOrMore.clear()

        selectEmptyOrMore?.add(String.format(LanguageUtil.getString(mActivity, "leverage_short"), NCoinManager.getShowMarket(baseCoin)))
        selectEmptyOrMore?.add(String.format(LanguageUtil.getString(mActivity, "leverage_more"), NCoinManager.getShowMarket(quoteCoin)))
        changeCurrent()
        when (dialogSelect) {
            ParamConstant.TYPE_BUY -> {
                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(baseCoin))
            }
            ParamConstant.TYPE_SELL -> {
                bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(quoteCoin))
            }
        }

        tv_recharge_record?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.LeverActivity, Bundle().apply {
                putString(ParamConstant.symbol, symbol)
                putInt(ParamConstant.CUR_INDEX, ParamConstant.CURRENT_TYPE)
            })
        }

        tv_lever_account_title?.text = "${NCoinManager.getShowMarketName(symbolJSONObject.optString("name", ""))} ${LanguageUtil.getString(mActivity, "leverage_asset")}"
        stl_market_loop?.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                dialogSelect = position
                when (dialogSelect) {
                    ParamConstant.TYPE_BUY -> {
                        bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(baseCoin))
                    }
                    ParamConstant.TYPE_SELL -> {
                        bar_layout?.setEditTextCoinContent(NCoinManager.getShowMarket(quoteCoin))
                    }
                }
                bar_layout?.setEdittextContent("")
                setBorrowingAndReturnView()
            }

            override fun onTabReselect(position: Int) {

            }

        })
        setBorrowingAndReturnView()

    }

    /**
     * 借贷
     */
    fun setBorrowing(symbol: String, coin: String, amount: String) {
        addDisposable(getMainModel().setBorrow(symbol, coin, amount, object : NDisposableObserver(mActivity,true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                finish()
            }
        }))
    }

    private fun changeCurrent() {
        stl_market_loop?.apply {
            val itemFirst = TabEntity(NCoinManager.getShowMarket(selectEmptyOrMore[0]), 0, 0)
            val itemTwo = TabEntity(NCoinManager.getShowMarket(selectEmptyOrMore[1]), 0, 0)
            setTabData(arrayListOf(itemFirst, itemTwo))
        }
    }


}