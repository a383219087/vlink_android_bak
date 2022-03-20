package com.yjkj.chainup.new_version.activity.asset

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.CurrencyLendingAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.activity_currency_lending_records.*
import kotlinx.android.synthetic.main.item_currency_lending_view.*
import org.jetbrains.anko.textColor
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-11-13-11:04
 * @Email buptjinlong@163.com
 * @description 当前借贷
 */
@Route(path = RoutePath.CurrencyLendingRecordsActivity)
class CurrencyLendingRecordsActivity : NBaseActivity() {

    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    @JvmField
    @Autowired(name = ParamConstant.JSON_BEAN)
    var jsonbean = ""

    var symbolJSONObject = JSONObject()
    var dialog: TDialog? = null


    override fun setContentView() = R.layout.activity_currency_lending_records


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM



        recycler_view?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this))
        recycler_view?.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(symbol)) {
            getBalanceList()
            getCurrent()
        } else {
            symbolJSONObject = JSONObject(jsonbean)
            if (null != symbolJSONObject) {
                symbol = symbolJSONObject.optString("symbol", "")
                initView()
                getCurrent()
            }

        }
    }

    var basePrecision = 0
    var quotePrecision = 0
    override fun initView() {
        collapsing_toolbar?.title = NCoinManager.getShowMarketName(symbolJSONObject.optString("name", ""))
        tv_contract_text_type?.text = LanguageUtil.getString(this,"contract_text_type")
        tv_assets_text_available?.text = LanguageUtil.getString(this,"assets_text_available")
        tv_assets_text_freeze?.text = LanguageUtil.getString(this,"assets_text_freeze")
        tv_leverage_have_borrowed?.text = LanguageUtil.getString(this,"leverage_have_borrowed")
        tv_currency_equivalence?.text = LanguageUtil.getString(this,"assets_text_equivalence")
        tv_current_application?.text = LanguageUtil.getString(this,"asset_lever_current_application")
        tv_asset_lever_history?.text = LanguageUtil.getString(this,"asset_lever_history")

        basePrecision = NCoinManager.getCoinShowPrecision(symbolJSONObject.optString("baseCoin"))
        quotePrecision = NCoinManager.getCoinShowPrecision(symbolJSONObject.optString("quoteCoin"))

        type_base?.text = NCoinManager.getShowMarket(symbolJSONObject.optString("baseCoin", ""))
        type_quote?.text = NCoinManager.getShowMarket(symbolJSONObject.optString("quoteCoin", ""))

        setRiskView()

        setBaseContent(canUse_base, symbolJSONObject.optString("baseNormalBalance", ""))
        setBaseContent(lock_base, symbolJSONObject.optString("baseLockBalance", ""))
        setBaseContent(tv_have_borrow_base, symbolJSONObject.optString("baseBorrowBalance", ""))

        setQuoteContent(canUse_quote, symbolJSONObject.optString("quoteNormalBalance", ""))
        setQuoteContent(lock_quote, symbolJSONObject.optString("quoteLockBalance", ""))
        setQuoteContent(tv_have_borrow_quote, symbolJSONObject.optString("quoteBorrowBalance", ""))
        var temp = RateManager.getCNYByCoinName("BTC", symbolJSONObject?.optString("symbolBalance", ""), isOnlyResult = true)

        tv_currency_equivalence?.text = "${ LanguageUtil.getString(mActivity,"assets_text_equivalence")} $temp ${RateManager.getCurrencyLang()}"

        /**
         * 借贷记录
         */
        ll_history_layout?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.LeverActivity, Bundle().apply {
                putString(ParamConstant.symbol, symbol)
                putInt(ParamConstant.CUR_INDEX, ParamConstant.HISTORY_TYPE)
            })
        }
    }

    /**
     * 设置 风险率页面
     */
    fun setRiskView() {
        // 风险率
        var riskRate = symbolJSONObject.optString("riskRate", "--")

        tv_risk_rate?.text =  LanguageUtil.getString(mActivity,"leverage_risk") + " $riskRate%"
        if (BigDecimalUtils.compareTo(riskRate, "150") >= 0) {
            riskRate = "150"
        }
        progress_bar?.progress = riskRate.toInt() - 110
        if (riskRate.toInt() in 131..Int.MAX_VALUE) {
            tv_risk_rate?.textColor = ColorUtil.getColor(R.color.green)
            progress_bar?.progressDrawable = ContextCompat.getDrawable(mContext!!, R.drawable.bg_progressbar_green)
        } else if (riskRate.toInt() in (110..130)) {
            tv_risk_rate?.textColor = ColorUtil.getColor(R.color.red)
            progress_bar?.progressDrawable = ContextCompat.getDrawable(mContext!!, R.drawable.bg_progressbar_red)
        } else {
            tv_risk_rate?.textColor = ColorUtil.getColor(R.color.normal_text_color)
            tv_risk_rate?.text =  LanguageUtil.getString(mActivity,"leverage_risk") + " --"
            progress_bar?.progressDrawable = ContextCompat.getDrawable(mContext!!, R.drawable.bg_progressbar_default)
        }
        iv_risk_rate?.setOnClickListener {
            NewDialogUtils.showSingleDialog(this@CurrencyLendingRecordsActivity,  LanguageUtil.getString(mActivity,"leverage_risk_prompt"), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                }
            })
        }
    }


    fun setBaseContent(view: TextView, content: String) {
        view?.text = BigDecimalUtils.divForDown(content, 8).toPlainString()
    }

    fun setQuoteContent(view: TextView, content: String) {
        view?.text = BigDecimalUtils.divForDown(content, 8).toPlainString()
    }

    /**
     * 获取杠杆账户列表
     */
    fun getBalanceList() {
        addDisposable(getMainModel().getBalanceList(object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                if (null != json) {
                    var leverMap = json.optJSONObject("leverMap")
                    if (null != leverMap) {
                        getSymbolJSONobject(leverMap)
                    }
                }
            }
        }))
    }

    /**
     * 从大接口获取symbol
     */
    fun getSymbolJSONobject(jsonObject: JSONObject) {
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val data = jsonObject.optJSONObject(iterator.next())
            if (null != data && data!!.length() > 0) {
                val symbolTemp = data!!.optString("symbol", "")
                if (symbolTemp == symbol) {
                    symbolJSONObject = data
                    initView()
                    return
                }
            }
        }
    }

    var list: ArrayList<JSONObject> = arrayListOf()

    val adapter = CurrencyLendingAdapter(list)


    /**
     * 获取当前借贷
     */
    private fun getCurrent() {
        addDisposable(getMainModel().borrowNew(symbol,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        list.clear()
                        jsonObject.optJSONObject("data").run {
                            val orderJsonArray = optJSONArray("financeList")
                            if (null != orderJsonArray && orderJsonArray.length() != 0) {
                                list?.addAll(JSONUtil.arrayToList(orderJsonArray))
                            }
                            adapter.setList(list)
                        }
                    }
                }, pageSize = "1000"))
    }


}