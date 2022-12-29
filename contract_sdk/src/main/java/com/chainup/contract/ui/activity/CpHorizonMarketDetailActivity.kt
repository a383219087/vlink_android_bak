package com.yjkj.chainup.new_contract.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.chainup.contract.R
import com.chainup.contract.adapter.CpHKLineScaleAdapter
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.KlineTick
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.*
import com.chainup.contract.ws.CpWsContractAgentManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.new_version.kline.bean.CpKLineBean
import com.chainup.contract.kline.data.CpDataManager
import com.yjkj.chainup.new_version.kline.data.CpKLineChartAdapter
import com.yjkj.chainup.new_version.kline.view.cp.MainKlineViewStatus
import com.yjkj.chainup.new_version.kline.view.vice.CpViceViewStatus
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*


import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.*
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.rv_kline_scale
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_24h_vol
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_close_price
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_coin_map
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_converted_close_price
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_high_price
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_low_price
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.tv_rose
import kotlinx.android.synthetic.main.cp_activity_horizon_market_detail.v_kline

/**
 * @description : 横屏行情的详细界面
 * @date 2019-3-16
 * @author Bertking
 *
 */
class CpHorizonMarketDetailActivity : CpNBaseActivity(), CpWsContractAgentManager.WsResultCallback {

    override fun setContentView(): Int {
        return R.layout.cp_activity_horizon_market_detail
    }

    lateinit var context: Context
    var curTime = ""
    var type = CpParamConstant.BIBI_INDEX
    var isFrist = true

    var klineData: ArrayList<CpKLineBean> = arrayListOf()
    private val adapter by lazy { CpKLineChartAdapter() }


    var coinMap = "BTC/USDT"

    var symbol = ""

    var jsonObject: JSONObject? = null

    var tv24hVolUnit = ""
    var mMultiplierCoin = ""
    var mPricePrecision = 0
    var mMultiplierPrecision = 0
    var mMultiplier = "0"
    var coUnit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        context = this
        curTime = CpKLineUtil.getCurTime()
        setContentView(R.layout.cp_activity_horizon_market_detail)
//        isLandscape = true
        super.onCreate(savedInstanceState)
        symbol = intent.getStringExtra("symbolHorizon").toString()

//        val themeMode = PublicInfoDataService.getInstance().themeMode
//        val kLineLogo = PublicInfoDataService.getInstance().getKline_background_logo_img(null, themeMode == PublicInfoDataService.THEME_MODE_DAYTIME)
//        GlideUtils.load(this, kLineLogo, iv_logo, RequestOptions())


//        tv_rose_title?.text = CpLanguageUtil.getString(context, "cp_extra_text110")
        tv_rose_title?.text = getString(R.string.cp_extra_text110)
//        tv_high_price_title?.text = CpLanguageUtil.getString(context, "cp_extra_text111")
        tv_high_price_title?.text = getString(R.string.cp_extra_text111)
//        tv_low_price_title?.text = CpLanguageUtil.getString(context, "cp_extra_text112")
        tv_low_price_title?.text = getString(R.string.cp_extra_text112)

        tv_main_title?.text = CpLanguageUtil.getString(context, "cp_extra_text155")
        tv_vice_title?.text = CpLanguageUtil.getString(context, "cp_extra_text156")
        tv_rose_title?.text = CpLanguageUtil.getString(context, "cp_extra_text110")


        v_kline?.let {
            it.adapter = adapter
            it.startAnimation()
            it.justShowLoading()
            it.setPricePrecision(mPricePrecision)
        }

        initKLineScale()

        action4KLineIndex()

        mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(this, intent.getIntExtra("contractId", -1))

        mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(this, intent.getIntExtra("contractId", -1))

        mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(this,  intent.getIntExtra("contractId", -1))

        coUnit = CpClLogicContractSetting.getContractUint(this)

        mMultiplier = CpClLogicContractSetting.getContractMultiplierById(this, intent.getIntExtra("contractId", -1))

        tv24hVolUnit = if (CpClLogicContractSetting.getContractUint(this) == 0) " " + getString(R.string.cp_overview_text9) else " " + mMultiplierCoin


//        jsonObject = NCoinManager.getSymbolObj(symbol)
//
//
//        coinMap = NCoinManager.showAnoterName(jsonObject)
//        symbol = jsonObject?.optString("symbol") ?: ""

//        tv_coin_map?.text = coinMap
        tv_coin_map?.text = CpClLogicContractSetting.getContractShowNameById(mActivity,intent.getIntExtra("contractId", -1));
        isFrist = true
        klineData.clear()
        CpWsContractAgentManager.instance.addWsCallback(this)
        tv_converted_close_price.visibility=View.GONE

        iv_close?.setOnClickListener {
            var message =
                CpMessageEvent(CpMessageEvent.market_switch_curTime)
            message.msg_content = curTime
            CpEventBusUtil.post(message)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        CpWsContractAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        initSocket()
    }

    override fun onBackPressed() {
        var message =
            CpMessageEvent(CpMessageEvent.market_switch_curTime)
        message.msg_content = curTime
        CpEventBusUtil.post(message)
        super.onBackPressed()
    }

    /**
     * 渲染24H行情数据
     */
    private fun render24H(tick: KlineTick) {
//        val price = jsonObject?.optInt("price") ?: 4
        val price = mPricePrecision

        runOnUiThread {
            if (tick.rose >= 0.0) {
                tv_close_price?.textColor = CpColorUtil.getMainColorType()
            } else {
                tv_close_price?.textColor = CpColorUtil.getMainColorType(isRise = false)
            }

//            tv_coin_map?.text = coinMap
            tv_coin_map?.text = CpClLogicContractSetting.getContractShowNameById(mActivity, intent.getIntExtra("contractId", -1))
            tv_close_price?.text = CpDecimalUtil.cutValueByPrecision(tick.close, price)



            val rose = tick.rose.toString()
            RateManager.getRoseText(tv_rose ?: return@runOnUiThread, rose)
            tv_rose?.textColor = CpColorUtil.getMainColorType(RateManager.getRoseTrend(rose) >= 0)
            tv_high_price?.text = CpDecimalUtil.cutValueByPrecision(tick.high, price)

            tv_low_price?.text = CpDecimalUtil.cutValueByPrecision(tick.low, price)
//            tv_24h_vol?.text = BigDecimalUtils.showDepthVolume(tick.vol)
            val amount = if (coUnit == 0) tick.vol else CpBigDecimalUtils.mulStr(tick.vol, mMultiplier, mMultiplierPrecision)
            tv_24h_vol?.text = CpBigDecimalUtils.showDepthVolume(amount) + tv24hVolUnit
        }
    }

    private fun setKLineViewIndexStatus(isMain: Boolean = true, position: Int = 0) {
        val parent = if (isMain) rg_main ?: return else rg_vice ?: return
        for (i in 0 until parent!!.childCount) {
            if (i == position) {
                (parent.getChildAt(position) as RadioButton).isChecked = true
            } else {
                (parent.getChildAt(i) as RadioButton).isChecked = false
            }
        }
    }

    /**
     * K线的指标处理
     */
    private fun action4KLineIndex() {
        when (CpKLineUtil.getMainIndex()) {
            MainKlineViewStatus.MA.status -> {
                setKLineViewIndexStatus(position = 0)
            }

            MainKlineViewStatus.BOLL.status -> {
                setKLineViewIndexStatus(position = 1)
            }

            MainKlineViewStatus.NONE.status -> {
                setKLineViewIndexStatus(position = 2)
            }
        }

        when (CpKLineUtil.getViceIndex()) {
            CpViceViewStatus.MACD.status -> {
                setKLineViewIndexStatus(isMain = false, position = 0)
                v_kline?.setChildDraw(0)
            }

            CpViceViewStatus.KDJ.status -> {
                setKLineViewIndexStatus(isMain = false, position = 1)
                v_kline?.setChildDraw(1)
            }

            CpViceViewStatus.RSI.status -> {
                setKLineViewIndexStatus(isMain = false, position = 2)
                v_kline?.setChildDraw(2)

            }

            CpViceViewStatus.WR.status -> {
                setKLineViewIndexStatus(isMain = false, position = 3)
                v_kline?.setChildDraw(3)
            }

            CpViceViewStatus.NONE.status -> {
                v_kline?.hideChildDraw()
            }
        }


        rg_main?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_ma -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                    CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                }
                R.id.rb_boll -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                    CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                }

                R.id.rb_hide_main -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                    CpKLineUtil.setMainIndex(MainKlineViewStatus.NONE.status)
                }
            }
        }


        rg_vice?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_macd -> {
                    v_kline?.setChildDraw(0)
                    CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status)
                }
                R.id.rb_kdj -> {
                    v_kline?.setChildDraw(1)
                    CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status)
                }

                R.id.rb_rsi -> {
                    v_kline?.setChildDraw(2)
                    CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status)
                }

                R.id.rb_wr -> {
                    v_kline?.setChildDraw(3)
                    CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status)
                }

                R.id.rb_hide_vice -> {
                    v_kline?.hideChildDraw()
                    CpKLineUtil.setViceIndex(CpViceViewStatus.NONE.status)
                }
            }
        }
    }

    /**
     * 处理K线刻度
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true)
        val klineScale = CpKLineUtil.getKLineScale()
        val layoutManager = GridLayoutManager(context, klineScale.size)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        val adapter = CpHKLineScaleAdapter(klineScale)
        rv_kline_scale?.adapter = adapter
//        adapter.bindToRecyclerView(rv_kline_scale ?: return)
        /**
         * 分时线
         */
        v_kline?.setMainDrawLine(CpKLineUtil.getCurTime4Index() == 0)

        adapter.setOnItemClickListener { viewHolder, view, position ->
            /**
             * 分时线
             */
            v_kline?.setMainDrawLine(position == 0)
            if (position != CpKLineUtil.getCurTime4Index()) {
                for (i in 0 until klineScale.size) {
                    val textView = viewHolder.getViewByPosition(i, R.id.tv_scale) as TextView
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    textView.setTextColor(CpColorUtil.getColor(R.color.normal_text_color))
                }

                val textView = viewHolder.getViewByPosition(position, R.id.tv_scale) as TextView
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.cp_kline_item_selected_shape)
                textView.setTextColor(CpColorUtil.getColor(R.color.main_blue))
                CpKLineUtil.setCurTime4KLine(position)
                val time = curTime
                curTime = klineScale[position]
                Log.e("jinlong", "v_kline：$curTime   time:$time ")
                CpKLineUtil.setCurTime(klineScale[position])
                switchKLineScale(time, klineScale[position])
            }
        }
    }

    /**
     * 初始化WebSocket
     */
    private fun initSocket() {
        if (isNotEmpty(symbol)) {
            val scale: String = if (curTime == "line") "1min" else curTime
            CpWsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "line" to scale), this)
        }

    }

    /**
     * 处理 24H,KLine数据
     */
    fun handleData(data: String) {
        try {
            val jsonObj = JSONObject(data)
            if (!jsonObj.isNull("tick")) {
                /**
                 * 24H行情
                 */
                if (jsonObj.getString("channel") == WsLinkUtils.tickerFor24HLink(symbol = symbol, isChannel = true)) {
                    val quotesData = CpJsonUtils.convert2Quote(jsonObj.toString())
                    render24H(quotesData.tick)
                    return
                }


                /**
                 * 最新K线数据
                 */
                if (jsonObj.getString("channel") == WsLinkUtils.getKlineNewLink(symbol, curTime).channel) {

                    doAsync {
                        /**
                         * 这里需要处理：重复添加的问题
                         */
                        val kLineEntity = CpKLineBean()
                        val data = jsonObj.optJSONObject("tick") ?: return@doAsync
                        kLineEntity.id = data.optLong("id")
                        kLineEntity.openPrice = BigDecimal(data.optString("open")).toFloat()
                        kLineEntity.closePrice = BigDecimal(data.optString("close")).toFloat()
                        kLineEntity.highPrice = BigDecimal(data.optString("high")).toFloat()
                        kLineEntity.lowPrice = BigDecimal(data.optString("low")).toFloat()
//                        kLineEntity.volume = BigDecimal(data.optString("vol")).toFloat()
                        val vol = if (coUnit == 0) data.optString("vol") else CpBigDecimalUtils.mulStr(data.optString("vol"), mMultiplier, mMultiplierPrecision)
                        kLineEntity.volume = BigDecimal(vol).toFloat()
                        try {
                            if (klineData.isNotEmpty() && klineData.size != 0) {
                                val isRepeat = klineData.last().id == data.optLong("id")
                                if (isRepeat) {
                                    klineData[klineData.lastIndex] = kLineEntity
                                    CpDataManager.calculate(klineData)
                                    adapter.changeItem(klineData.lastIndex, kLineEntity)
                                } else {
                                    klineData.add(kLineEntity)
                                    CpDataManager.calculate(klineData)
                                    runOnUiThread {
                                        adapter.addItem(kLineEntity)
                                        v_kline?.refreshEnd()
                                    }
                                }
                            } else {
                                klineData.add(kLineEntity)
                                CpDataManager.calculate(klineData)
                                uiThread {
                                    adapter.addItems(arrayListOf(kLineEntity))
                                    v_kline?.refreshEnd()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }

                return
            }


            if (!jsonObj.isNull("data")) {
                /**
                 * 请求(req) ----> K线历史数据
                 * 即：K线图的历史数据
                 *
                 * channel ---> channel":"market_ltcusdt_kline_1week
                 */
                handlerKLineHistory(data)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理K线历史数据
     * @param data K线历史数据
     */
    private fun handlerKLineHistory(data: String) {
        doAsync {
            val json = JSONObject(data)
            val type = object : TypeToken<ArrayList<CpKLineBean>>() {
            }.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                klineData.addAll(gson.fromJson(json.getJSONArray("data").toString(), type))
                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    var scale = if (curTime == "line") "1min" else curTime
                    CpWsContractAgentManager.instance.sendData(WsLinkUtils.getKlineHistoryOther(symbol, scale, klineData[0].id.toString()))
                }
                isFrist = false
                return@doAsync
            } else {
                klineData.addAll(0, gson.fromJson(json.getJSONArray("data").toString(), type))
            }
            initKlineData()
        }
    }

    private fun initKlineData() {
        runOnUiThread {
            CpDataManager.calculate(klineData)
            adapter.addFooterData(klineData)
            v_kline?.refreshEnd()
            if (v_kline?.minScrollX != null) {
                if (klineData.size < 30) {
                    v_kline?.scrollX = 0
                } else {
                    v_kline?.scrollX = v_kline!!.minScrollX
                }
            }
        }
        /**
         * 获取最新K线数据
         */
        runOnUiThread {
            val scale = if (curTime == "line") "1min" else curTime
            CpWsContractAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
        }

    }

    /**
     * 切换K线刻度
     * @param kLineScale K线刻度
     */
    private fun switchKLineScale(curTime: String, kLineScale: String) {
        if (curTime != kLineScale) {
            isFrist = true
            klineData.clear()
            initSocket()
        }
    }


    override fun onPause() {
        super.onPause()
        CpWsContractAgentManager.instance.unbind(this, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        CpWsContractAgentManager.instance.removeWsCallback(this)
    }

    /**
     * Ws 发送消息
     */
    private fun sendMsg(msg: String) {

    }


    override fun onWsMessage(json: String) {
        handleData(json)
    }
}
