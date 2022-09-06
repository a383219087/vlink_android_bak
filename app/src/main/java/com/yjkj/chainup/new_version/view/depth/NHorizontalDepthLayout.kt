package com.yjkj.chainup.new_version.view.depth

import androidx.lifecycle.Observer
import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.get
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.ParamConstant.TYPE_LIMIT
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.NewMainActivity
import com.yjkj.chainup.new_version.activity.leverage.NLeverFragment
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.new_version.fragment.NCVCTradeFragment
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.depth_etf_layout.view.*
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.*
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.ll_etf_item
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.trade_bottomInformation
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.tv_change_depth
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.tv_close_price
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.tv_converted_close_price
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.tv_etf_price
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.v_tape_line
import kotlinx.android.synthetic.main.item_transaction_detail.view.*
import kotlinx.android.synthetic.main.trade_amount_view.view.et_price
import kotlinx.android.synthetic.main.trade_amount_view.view.tv_convert_price
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColor
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019-09-06-11:15
 * @Description:
 */
class NHorizontalDepthLayout @JvmOverloads constructor(context: Context,
                                                       attrs: AttributeSet? = null,
                                                       defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val TAG = NHorizontalDepthLayout::class.java.simpleName


    var dialog: TDialog? = null

    var tapeDialog: TDialog? = null

    var tapeLevel: Int = 0

    var depth_level = 0

    val depthLevels = arrayListOf<String>()

    var transactionData: JSONObject? = null


    var coinMapData: JSONObject? = NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
        set(value) {
            field = value
            trade_amount_view?.coinMapData = value ?: return
            trade_amount_view?.setPrice()
            judgeDepthLevel(isInited = true)
            setDepth(context)
        }

    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()

    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    var orderList = mutableListOf<JSONObject>()

    init {

        /**
         * 这里的必须为：True
         */
        LayoutInflater.from(context).inflate(R.layout.depth_horizontal_layout, this, true)

        setDepth(context)
        tv_contract_text_price?.text = LanguageUtil.getString(context, "contract_text_price")
        tv_charge_text_volume?.text = LanguageUtil.getString(context, "charge_text_volume")
        TradeFragment.liveData4DepthData.observe((this.context as NewMainActivity), Observer<MessageEvent> {
            if (null == it) {
                return@Observer
            }

            if (TradeFragment.currentIndex == ParamConstant.CVC_INDEX_TAB) {
                if (it.isLever) {
                    return@Observer
                }
            } else {
                if (!it.isLever) {
                    return@Observer
                }
            }
            when (it.msg_type) {
                MessageEvent.DEPTH_DATA_TYPE -> {
                    if (null != it.msg_content) {
                        val symbol = coinMapData?.optString("symbol")
                        LogUtil.e(TAG, "DEPTH_DATA_TYPE ${symbol} [] ${it.msg_content_data} ||  ${it.isLever}")
                        if (symbol == it.msg_content_data as String) {
                            val jsonObject = it.msg_content as JSONObject
                            transactionData = jsonObject
                            ll_buy_price?.visibility = View.VISIBLE
                            ll_sell_price?.visibility = View.VISIBLE
                            refreshDepthView(jsonObject)
                        } else {
                            ll_buy_price?.visibility = View.INVISIBLE
                            ll_sell_price?.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        })


        NLiveDataUtil.observeData((this.context as NewMainActivity), Observer<MessageEvent> {
            if (null == it) {
                return@Observer
            }

            if (TradeFragment.currentIndex == ParamConstant.CVC_INDEX_TAB) {
                if (it.isLever) {
                    return@Observer
                }
            } else {
                if (!it.isLever) {
                    return@Observer
                }
            }

            when (it.msg_type) {
                MessageEvent.TAB_TYPE -> {
                    LogUtil.e(TAG, "TAB_TYPE  setDepth ${isLevel}  ${it.isLever}  ${coinMapData?.optString("symbol")}  ${depth_level} ")
                    if (it.isLever != isLevel) {
                        return@Observer
                    }
                    coinMapData = if (it.isLever) {
                        NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol4Lever)
                    } else {
                        NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
                    }
                    LogUtil.e(TAG, "TAB_TYPE  setDepth success() ")
                    tapeLevel = if (it.isLever) NLeverFragment.tapeLevel else NCVCTradeFragment.tapeLevel
                    changeTape(tapeLevel, false)
                    setDepth(context)
                }

                MessageEvent.symbol_switch_type -> {
                    if (null != it.msg_content) {
                        val symbol = it.msg_content as String
                        if (symbol != coinMapData?.optString("symbol")) {
                            coinMapData = NCoinManager.getSymbolObj(symbol)
                            judgeDepthLevel(isInited = true)
                            setDepth(context)
                        }
                    }
                }


            }
        })
        ll_etf_item?.setOnClickListener {
            NewDialogUtils.showSingleDialog(context, LanguageUtil.getString(context, "etf_text_networthPrompt"), null,
                    "", LanguageUtil.getString(context, "alert_common_iknow"), false)
        }
        /**
         * 选择深度
         */
        tv_change_depth?.setOnClickListener {
            dialog = NewDialogUtils.showBottomListDialog(context, depthLevels, depthLevels.indexOf(depth_level.toString()), object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    dialog?.dismiss()

                    val curDepthLevel = judgeDepthLevel()
//                    Log.d(TAG, "=======curDepth:${curDepthLevel},click:$item=====")

                    if (curDepthLevel != item) {
                        tv_change_depth?.text = LanguageUtil.getString(context, "kline_action_depth") + " " + data[item]
                        depth_level = data[item].toInt()
                        /**
                         * 切记：这里需要给后端传的仅仅是下标,深度有3个维度，0、1、2
                         * 详情：http://wiki.365os.com/pages/viewpage.action?pageId=2261055
                         */
                        NLiveDataUtil.postValue(MessageEvent(MessageEvent.DEPTH_LEVEL_TYPE, item, TradeFragment.currentIndex == ParamConstant.LEVER_INDEX_TAB))

                    }
                }
            })

        }




        initDetailView()

        /**
         * 改变盘口的样式
         */
        ib_tape?.setOnClickListener {
            tapeDialog = NewDialogUtils.showBottomListDialog(context, arrayListOf(LanguageUtil.getString(context, "contract_text_defaultMarket"), LanguageUtil.getString(context, "contract_text_buyMarket"), LanguageUtil.getString(context, "contract_text_sellMarket")), tapeLevel, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    tapeDialog?.dismiss()
                    tapeLevel = item
                    if (TradeFragment.currentIndex == ParamConstant.LEVER_INDEX_TAB) {
                        NLeverFragment.tapeLevel = tapeLevel
                    } else {
                        NCVCTradeFragment.tapeLevel = tapeLevel
                    }
                    changeTape(item)
                }
            })
        }

    }

    // 多倍比率
    private fun setHeadetLeve(context: Context) {
        if (isLevel) {
            trade_bottomInformationview.visibility = View.GONE
            trade_top.visibility = View.GONE
            return
        }
        var topview = findViewById<LinearLayout>(R.id.trade_top)
        topview?.setVisibility(View.VISIBLE)


        var topviewright = findViewById<LinearLayout>(R.id.trade_topright)
        topviewright?.setVisibility(View.VISIBLE)


        val etfUpAndDown = coinMapData?.optJSONArray("etfUpAndDown")
        if (etfUpAndDown?.length() == 2) {
            val coinLeft = NCoinManager.getMarketCoinObj(etfUpAndDown.getString(0))
            changeCoinUpOrDown(true, coinLeft)
            val coinRight = NCoinManager.getMarketCoinObj(etfUpAndDown.getString(1))
            changeCoinUpOrDown(false, coinRight)
            if (coinLeft != null || coinRight != null) {
                topview?.visibility = View.VISIBLE
            } else {
                topview?.visibility = View.GONE
            }
        } else if (etfUpAndDown?.length() == 1) {
            val coinLeft = NCoinManager.getMarketCoinObj(etfUpAndDown.getString(0))
            changeCoinUpOrDown(true, coinLeft)
        } else {
            topview?.visibility = View.GONE
        }

        var bottomview = findViewById<LinearLayout>(R.id.trade_bottomInformationview)

        if (coinMapData?.getInt("etfOpen") == 1) {

            val name = coinMapData?.optString("etfBase") + coinMapData?.optString("etfMultiple") + coinMapData?.optString("etfSide")
            val base = coinMapData?.getString("etfBase")
            val upDown = getUpdown(coinMapData?.getString("etfSide"), coinMapData?.getString("etfMultiple"))
            val hrefItem = LanguageUtil.getString(context,"etf_notes_explain_info")
            val span = LanguageUtil.getString(context, "etf_notes_explain").format(name, base, upDown,hrefItem)
            val spanable = SpannableString(span)
            trade_bottomInformation?.apply {
                text = spanable
            }
            bottomview?.visibility = View.VISIBLE
        } else {
            bottomview?.visibility = View.GONE
        }


    }

    private fun setDepth(context: Context) {
        try {
            setHeadetLeve(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        depthLevels.clear()
        val depthData = coinMapData?.optString("depth")
        if (!TextUtils.isEmpty(depthData)) {
            val depths = depthData?.split(",") ?: emptyList()
            if (depths.isNotEmpty()) {
                depths.forEach {
                    val depth = if (it.contains(".")) {
                        it.replace("0.", "").length.toString()
                    } else {
                        "0"
                    }
                    depthLevels.add(depth)
                }
            }
        }


        val curDepthLevel = judgeDepthLevel()

        if (depthLevels.size > curDepthLevel) {
            depth_level = depthLevels[curDepthLevel].toInt()
        }
//        LogUtil.e(TAG, "bibi ${isLevel}  setDepth ${coinMapData?.optString("symbol")} ${curDepthLevel}  ${depth_level} ")
        tv_change_depth?.text = LanguageUtil.getString(context, "kline_action_depth") + " " + depth_level
    }

    private fun judgeDepthLevel(isInited: Boolean = false): Int {
        return if (TradeFragment.currentIndex == ParamConstant.LEVER_INDEX_TAB) {
            if (isInited) {
                0
            } else {
                NLeverFragment.curDepthIndex
            }
        } else {
            if (isInited) {
                0
            } else {
                NCVCTradeFragment.curDepthIndex
            }
        }
    }

    fun changeTape(item: Int, needData: Boolean = true) {
        when (item) {
            AppConstant.DEFAULT_TAPE -> {
                ll_buy_price?.visibility = View.VISIBLE
                ll_sell_price?.visibility = View.VISIBLE
                val params = v_tape_line.layoutParams as LayoutParams
                params.topMargin = 4.getDip()
                params.bottomMargin = 4.getDip()
                v_tape_line.layoutParams = params
                ColorUtil.setTapeIcon(ib_tape, AppConstant.DEFAULT_TAPE)
                initDetailView()
            }

            AppConstant.BUY_TAPE -> {
                ll_buy_price?.visibility = View.VISIBLE
                ll_sell_price?.visibility = View.GONE
                val params = v_tape_line.layoutParams as LayoutParams
                params.topMargin = 4.getDip()
                params.bottomMargin = 4.getDip()
                v_tape_line.layoutParams = params
                ColorUtil.setTapeIcon(ib_tape, AppConstant.BUY_TAPE)
                initDetailView(10)
            }

            AppConstant.SELL_TAPE -> {
                ll_buy_price?.visibility = View.GONE
                ll_sell_price?.visibility = View.VISIBLE

                val params = v_tape_line.layoutParams as LayoutParams
                params.topMargin = 4.getDip()
                params.bottomMargin = 4.getDip()
                v_tape_line.layoutParams = params
                ColorUtil.setTapeIcon(ib_tape, AppConstant.SELL_TAPE)
                initDetailView(10)
            }
        }
        if (needData) {
            refreshDepthView(transactionData)
        }
    }

    /**
     * 买卖盘
     *
     * 初始化交易详情记录view
     */
    fun initDetailView(items: Int = 5) {
        sellViewList.clear()
        buyViewList.clear()

        if (ll_buy_price?.childCount ?: 0 > 0) {
            (ll_buy_price as LinearLayout).removeAllViews()
        }

        if (ll_sell_price?.childCount ?: 0 > 0) {
            (ll_sell_price as LinearLayout).removeAllViews()
        }

        val pricePrecision = coinMapData?.optInt("price", 2) ?: 2

        for (i in 0 until items) {
            /**
             * 卖盘
             */
            val sell_layout: View = context.layoutInflater.inflate(R.layout.item_transaction_detail, null)

            sell_layout.im_sell?.backgroundResource = ColorUtil.getMainTickColorType(false)
            sell_layout.tv_price_item?.textColor = ColorUtil.getMainColorType(isRise = false)
            NLiveDataUtil.observeForeverData {
                if (null != it && MessageEvent.color_rise_fall_type == it.msg_type) {
                    sell_layout.tv_price_item?.textColor = ColorUtil.getMainColorType(isRise = false)
                }
            }

            sell_layout.setOnClickListener {
                val result = sell_layout.tv_price_item?.text.toString()
                click2Data(result)
            }
            sellViewList.add(sell_layout)

            /**
             * 买盘
             */
            val buy_layout: View = context.layoutInflater.inflate(R.layout.item_transaction_detail, null)
            buy_layout.im_sell?.backgroundResource = ColorUtil.getMainTickColorType()
            buy_layout.tv_price_item?.textColor = ColorUtil.getMainColorType()
            NLiveDataUtil.observeForeverData {
                if (null != it && MessageEvent.color_rise_fall_type == it.msg_type) {
                    buy_layout.tv_price_item?.textColor = ColorUtil.getMainColorType()
                }
            }

            buy_layout.setOnClickListener {
                val result = buy_layout.tv_price_item?.text.toString()
                click2Data(result)
            }
            buyViewList.add(buy_layout)
        }


        buyViewList.forEach {
            ll_buy_price?.addView(it)
        }

        sellViewList.forEach {
            ll_sell_price?.addView(it)
        }
        tv_close_price?.text = "--"
        tv_converted_close_price?.text = "--"

    }

    private fun click2Data(result: String) {
        if (!TextUtils.isEmpty(result) && result != "--" && result != "null") {
            if (trade_amount_view.priceType == TYPE_LIMIT) {
                val pricePrecision = coinMapData?.optInt("price", 2) ?: 2
                et_price?.setText(BigDecimalUtils.divForDown(result, pricePrecision).toPlainString())
                tv_convert_price?.text = RateManager.getCNYByCoinMap(DataManager.getCoinMapBySymbol(PublicInfoDataService.getInstance().currentSymbol), result)
            }
        }
    }


    /**
     * 更新买卖盘的数据
     */
    fun refreshDepthView(data: JSONObject?) {
        data?.run {
            val tick = this.optJSONObject("tick")

            /**
             * 卖盘交易量最大的
             */
            val askList = arrayListOf<JSONArray>()
            val asks = tick.optJSONArray("asks")
            if (asks.length() != 0) {
                val item = asks.optJSONArray(0)
                if (isFirstSetValue() && transactionType()) {
                    trade_amount_view.initTick(item, depth_level, type = 3)
                }
            }
            for (i in 0 until asks.length()) {
                askList.add(asks.optJSONArray(i))
            }

            val askMaxVolJson = askList.maxByOrNull {
                it.optDouble(1)
            }
            val askMaxVol = askMaxVolJson?.optDouble(1) ?: 1.0
//            Log.d(TAG, "========askMAX:$askMaxVol=======")

            /**
             * 买盘交易量最大的
             */
            val buyList = arrayListOf<JSONArray>()
            val buys = tick.optJSONArray("buys")
            if (buys.length() != 0) {
                val item = buys.optJSONArray(0)
                if (isFirstSetValue() && !transactionType()) {
                    trade_amount_view.initTick(item, depth_level, type = 4)
                }
            }
            for (i in 0 until buys.length()) {
                buyList.add(buys.optJSONArray(i))
            }

            /**
             * 买盘交易量最大的
             */
            val buyMaxVolJson = buyList.maxByOrNull {
                it.optDouble(1)
            }
            val buyMaxVol = buyMaxVolJson?.optDouble(1) ?: 1.0
//            Log.d(TAG, "========buyMAX:$buyMaxVol=======")

            val maxVol = Math.max(askMaxVol, buyMaxVol)

//            Log.d(TAG, "========maxVol:$maxVol=========")

            sellTape(askList, maxVol)
            buyTape(buyList, maxVol)
        }


    }

    /**
     * 卖盘
     */
    private fun sellTape(list: ArrayList<JSONArray>, maxVol: Double) {
        list.sortByDescending {
            it.optDouble(0)
        }

        for (i in 0 until sellViewList.size) {
            /**
             * 卖盘
             */
            if (list.size > sellViewList.size) {
                val subList = list.subList(list.size - sellViewList.size, list.size)
                if (subList.isNotEmpty()) {
                    /*****深度背景色START****/
                    sellViewList[i].fl_bg_item.backgroundColor = ColorUtil.getMinorColorType(isRise = false)
                    val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                    val curVolume = subList[i].optDouble(1)
                    val width = (curVolume / maxVol) * measuredWidth * 0.37
                    layoutParams.width = width.toInt()
                    sellViewList[i].fl_bg_item.layoutParams = layoutParams
                    /*****深度背景色END****/
                    val price = SymbolInterceptUtils.interceptData(
                            subList[i].optString(0).replace("\"", "").trim(),
                            depth_level,
                            "price")
                    sellViewList[i].tv_price_item.text = price
                    val order = orderList.filter { it.optString("side") == "SELL" && it.getPriceSplitZero() == price.getPriceSplitZero() }
                    sellViewList[i].im_sell.visibility = order.isNotEmpty().visiableOrGone()
                    sellViewList[i].tv_quantity_item.text = BigDecimalUtils.showDepthVolumeNew(subList[i].optString(1))
                }
            } else {
//                Log.d(TAG, "======VVV=======")
                val temp = sellViewList.size - list.size
                sellViewList[i].tv_price_item.text = "--"
                sellViewList[i].tv_quantity_item.text = "--"
                sellViewList[i].ll_item.backgroundColor = ColorUtil.getColor(R.color.transparent)
                if (i >= temp) {
                    /*****深度背景色START****/
                    sellViewList[i].fl_bg_item.backgroundColor = ColorUtil.getMinorColorType(isRise = false)
                    val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                    val width = (list[i - temp].optDouble(1) / maxVol) * measuredWidth * 0.4
                    layoutParams.width = width.toInt()
                    sellViewList[i].fl_bg_item.layoutParams = layoutParams
                    /*****深度背景色END****/
                    val price = SymbolInterceptUtils.interceptData(
                            list[i - temp].optString(0).replace("\"", "").trim(),
                            depth_level,
                            "price")
                    sellViewList[i].tv_price_item.text = price
                    val order = orderList.filter { it.optString("side") == "SELL" && it.getPriceSplitZero() == price.getPriceSplitZero() }
                    sellViewList[i].im_sell.visibility = order.isNotEmpty().visiableOrGone()
                    sellViewList[i].tv_quantity_item.text = BigDecimalUtils.showDepthVolumeNew(list[i - temp].optString(1))

                } else {
                    sellViewList[i].run {
                        tv_price_item.text = "--"
                        tv_quantity_item.text = "--"
                        fl_bg_item.setBackgroundResource(R.color.transparent)
                        im_sell.visibility = View.GONE
                    }

                }
            }


        }
    }

    /**
     * 买盘
     */
    private fun buyTape(list: ArrayList<JSONArray>, maxVol: Double) {

        /**
         * 买盘取最大
         */
        list.sortByDescending {
            it.optDouble(0)
        }

        for (i in 0 until buyViewList.size) {
            /**
             * 买盘
             */
            if (list.size > i) {

                ll_buy_price.get(i).run {
                    /*****深度背景色START****/
                    fl_bg_item.backgroundColor = ColorUtil.getMinorColorType()
                    val layoutParams = fl_bg_item.layoutParams
                    val width = (list[i].optDouble(1) / maxVol) * measuredWidth * 0.37
                    layoutParams.width = width.toInt()
                    fl_bg_item.layoutParams = layoutParams
                    /*****深度背景色END****/
                    val price = SymbolInterceptUtils.interceptData(
                            list[i].optString(0).replace("\"", "").trim(),
                            depth_level,
                            "price")
                    tv_price_item.text = price
                    val order = orderList.filter { it.optString("side") == "BUY" && it.getPriceSplitZero() == price.getPriceSplitZero() }
                    buyViewList[i].im_sell.visibility = order.isNotEmpty().visiableOrGone()
                    tv_quantity_item.text = BigDecimalUtils.showDepthVolumeNew(list[i].optString(1).trim())
                }

            } else {
                buyViewList[i].run {
                    tv_price_item.text = "--"
                    tv_quantity_item.text = "--"
                    fl_bg_item.setBackgroundResource(R.color.transparent)
                    im_sell.visibility = View.GONE
                }

            }
        }
    }

    fun changeData(data: JSONObject?) {
        if (data != null) {
            trade_amount_view.changeSellOrBuyData(data)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            setDepth(context)
        }
    }

    fun coinSwitch(coinSymbol: String) {
        val symbol = coinSymbol
        if (symbol != coinMapData?.optString("symbol")) {
            coinMapData = NCoinManager.getSymbolObj(symbol)
            judgeDepthLevel(isInited = true)
            setDepth(context)
        }
        resetPrice()
    }

    /**
     * 限价的情况下，价格默认为：收盘价
     */
    private fun isFirstSetValue(): Boolean {
        return trade_amount_view.editPriceIsNull()
    }


    fun transactionType(): Boolean {
        return trade_amount_view.transactionType == 0
    }


    fun changeTickData(tick: JSONObject) {
        val rose = tick.optString("rose")
        val close = tick.optString("close")
        val pricePrecision = coinMapData?.optString("price", "4")?.toInt() ?: 4

        tv_close_price?.run {
            textColor = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)
            text = DecimalUtil.cutValueByPrecision(close, pricePrecision)
        }
        val marketName = NCoinManager.getMarketName(coinMapData?.optString("name", ""))
        tv_converted_close_price?.text = RateManager.getCNYByCoinName(marketName, close)
    }

    fun resetPrice() {
        trade_amount_view.resetPrice()
    }

    fun resetEtf(isGone: Boolean = true) {
        tv_etf_price?.text = "--"
        if (isGone) {
            ll_etf_item.visibility = View.GONE
            trade_amount_view.changeEtfLayout(true)
        }
        val isETF = coinMapData?.coinIsEtf() ?: false
        trade_etf.visibility = isETF.getVisible()
        if (isETF) {
            tv_etf_tips.text = coinMapData?.coinEtfTips(context)
        }
    }

    fun changeEtf(data: JSONObject?) {
        ll_etf_item.visibility = View.VISIBLE

        trade_amount_view.changeEtfLayout()
        val price = data?.optJSONObject("data")?.optString("price") ?: "--"
        tv_etf_price?.text = price
    }

    fun changeOrder(mData: ArrayList<JSONObject>) {
        orderList.clear()
        orderList.addAll(mData)
        refreshDepthView(transactionData)
    }

    fun loginSwitch() {
//        trade_amount_view.notLoginLayout(UserDataService.getInstance().isLogined)
    }

    private var isLevel = false

    fun initCoinSymbol(symbols: String?, isLevel: Boolean = false) {
        if (symbols != null && symbols.isNotEmpty()) {
            this.isLevel = isLevel
            var symbol = ""
            if (isLevel) {
                symbol = symbols.split(" ")[0]
            } else {
                symbol = symbols
            }
            val result = symbol.split("/")
            if (result.size == 2) {
                tv_contract_text_price.text = coinBySplit(true, result[1])
            }
        }
    }

    private fun coinBySplit(isPrice: Boolean, value: String): String {
        val first = LanguageUtil.getString(context, if (isPrice) "contract_text_price" else "charge_text_volume")
        val message = StringBuffer(first)
        return message.append("(").append(value).append(")").toString()
    }

    fun getPrecision(): Int {
        return trade_amount_view.getPrecision()
    }

    // 盘口数据是否存在
    fun isDepth(isBuy: Boolean = true): Boolean {
        val items = if (isBuy) buyViewList else sellViewList
        if (items.size == 0) {
            return false
        }
        val price = items[0].tv_price_item.text
        if (price == "--") {
            return false
        }
        return true
    }

    fun depthIsRender(): Boolean {
        return isDepth() || isDepth(false)
    }

    fun depthBuyOrSell(): ArrayList<Any> {
        val array = arrayListOf<Any>();
        if (depthIsRender()) {
            array.add("true")
        }
        return array
    }


    private fun changeCoinUpOrDown(isLeft: Boolean = true, item: JSONObject?) {
        if (item != null) {
            if (isLeft) {
                trade_topleft_coin?.text = item.optString("etfBase") + item.optString("etfMultiple") + item.optString("etfSide")
                trade_topleft_coinleve?.text = "[" + item.optString("etfMultiple") + getMultstring(item.optString("etfSide")) + "]"
                trade_topleft_coinleve?.textColor = getMulcolor(item.optString("etfSide"))
                trade_topleft?.visibility = View.VISIBLE
            } else {
                trade_topright_coin?.text = item.optString("etfBase") + item.optString("etfMultiple") + item.optString("etfSide")
                trade_topright_coinleve?.text = "[" + item.optString("etfMultiple") + getMultstring(item.optString("etfSide")) + "]"
                trade_topright_coinleve?.textColor = getMulcolor(item.optString("etfSide"))
                trade_topright?.visibility = View.VISIBLE
            }
        } else {
            if (isLeft) {
                trade_topleft?.visibility = View.GONE
            } else {
                trade_topright?.visibility = View.GONE
            }
        }
    }

    fun getMultstring(etfMultiple: String): String {
        if (etfMultiple.equals("L")) {
            return LanguageUtil.getString(context, "etf_multipleL")
        } else {
            return LanguageUtil.getString(context, "etf_multipleS")
        }
    }

    fun getUpdown(etfSide: String?, etfmultiple: String?): String {
        if (etfSide.equals("L")) {
            return LanguageUtil.getString(context, "etf_notes_multipleL").format(etfmultiple)
        } else {
            return LanguageUtil.getString(context, "etf_notes_multipleS").format(etfmultiple)
        }
    }

    fun getMulcolor(etfMultiple: String): Int {
        if (etfMultiple.equals("L")) {
            return ColorUtil.getMainColorType()
        } else {
            return ColorUtil.getMainColorType(false)
        }

    }

    fun changeEtfInfo(data: JSONObject?) {
        trade_amount_view.etfInfo = data
    }


}