package com.yjkj.chainup.new_version.activity

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.EntrusDetialsAdapter
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.getTradeCoinPrice
import com.yjkj.chainup.util.getTradeCoinPriceNumber8
import com.yjkj.chainup.util.getTradeCoinVolume
import kotlinx.android.synthetic.main.activity_entrust_detials.*
import org.json.JSONObject


/**
 * @Author lianshangljl
 * @Date 2020-03-10-18:26
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.EntrustDetialsActivity)
class EntrustDetialsActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_entrust_detials

    var list: ArrayList<JSONObject> = arrayListOf()
    var adapter = EntrusDetialsAdapter(list)

    @JvmField
    @Autowired(name = ParamConstant.TYPE)
    var orderType = ParamConstant.BIBI_INDEX

    /**
     * 成交量
     */
    @JvmField
    @Autowired(name = ParamConstant.DEAL_VOLUME)
    var dealVolume = ""
    /**
     * 方向
     */
    @JvmField
    @Autowired(name = ParamConstant.HISTORY_SIDE)
    var side = ""
    /**
     * 均价
     */
    @JvmField
    @Autowired(name = ParamConstant.AVG_PRICE)
    var avgPrice = ""
    /**
     * 成交总额
     */
    @JvmField
    @Autowired(name = ParamConstant.DEAL_MONEY)
    var dealMoney = ""

    /**
     * id
     */
    @JvmField
    @Autowired(name = ParamConstant.ENTRUST_ID)
    var entrustId = ""
    /**
     * 币种
     */
    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    /**
     * 币种
     */
    @JvmField
    @Autowired(name = ParamConstant.BASE_COIN)
    var baseCoin = ""

    /**
     * 币种
     */
    @JvmField
    @Autowired(name = ParamConstant.COUNT_COIN)
    var countCoin = ""


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
        collapsing_toolbar?.title = if (side == "BUY") "${LanguageUtil.getString(this,"contract_action_buy")} ${NCoinManager.getShowMarket(baseCoin)}" else "${LanguageUtil.getString(this,"contract_action_sell")} ${NCoinManager.getShowMarket(baseCoin)}"
        ArouterUtil.inject(this)
        initView()
        renderOrderInfo()
    }

    override fun initView() {
        if (TextUtils.isEmpty(symbol)) {
            symbol = (baseCoin + countCoin).toLowerCase()
        }
        recycler_view?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(R.layout.ly_empty_withdraw_address)
        adapter.countCoin = countCoin
        adapter.baseCoin = baseCoin
        recycler_view?.adapter = adapter
        if (orderType == ParamConstant.LEVER_INDEX) {
            getLeverEntrustDetail(entrustId, symbol)
        } else {
            getEntrustDetail(entrustId, symbol)
        }

    }


    fun renderOrderInfo() {
        val coinMap = NCoinManager.getSymbolObj(symbol)
        if (side == "BUY") {
            tv_direction?.text = LanguageUtil.getString(this,"otc_text_tradeObjectBuy")
            tv_direction?.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            tv_direction?.text = LanguageUtil.getString(this,"otc_text_tradeObjectSell")
            tv_direction?.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
        tv_market?.text = NCoinManager.getShowMarket(baseCoin)
        tv_symbol?.text = "/${NCoinManager.getShowMarket(countCoin)}"

        //成交总额
        tv_deal_amount?.text = "${LanguageUtil.getString(this,"noun_order_GMV")}(${NCoinManager.getShowMarket(countCoin)})"
        tv_amount?.text = dealMoney.getTradeCoinPriceNumber8()

        //成交均价
        tv_price_title?.text = "${LanguageUtil.getString(this,"contract_text_dealAverage")}(${NCoinManager.getShowMarket(countCoin)})"
        tv_price?.text = avgPrice.getTradeCoinPrice(coinMap)

        //成交量
        tv_volume_title?.text = "${LanguageUtil.getString(this,"kline_text_volume")}(${NCoinManager.getShowMarket(baseCoin)})"
        tv_volume?.text = dealVolume.getTradeCoinVolume(coinMap)
    }

    /**
     * 获取委托详情
     */
    private fun getEntrustDetail(id: String, symbol: String = PublicInfoDataService.getInstance().currentSymbol) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        addDisposable(getMainModel().getEntrustDetail4(id = id, symbol = symbol,
                page = "1",
                pageSize = "1000", consumer = object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                list.clear()
                jsonObject.optJSONObject("data").run {

                    val orderJsonArray = optJSONArray("trade_list") ?: return
                    if (orderJsonArray.length() != 0) {
                        orderJsonArray?.run {
                            for (i in 0 until orderJsonArray.length()) {
                                list.add(orderJsonArray.optJSONObject(i))
                            }
                        }
                        adapter.setList(list)
                    }
                }
            }

        }))
    }

    /**
     * 杠杆历史详情
     */
    private fun getLeverEntrustDetail(id: String, symbol: String = PublicInfoDataService.getInstance().currentSymbol4Lever) {
        addDisposable(getMainModel().getLeverEntrustDetail4(id, symbol, "1000", "1", object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                list.clear()

                jsonObject.optJSONObject("data").run {

                    val orderJsonArray = optJSONArray("trade_list") ?: return
                    if (orderJsonArray.length() != 0) {
                        orderJsonArray?.run {
                            for (i in 0 until orderJsonArray.length()) {
                                list.add(orderJsonArray.optJSONObject(i))
                            }
                        }
                        adapter.setList(list)
                    }
                }
            }

        }))
    }
}