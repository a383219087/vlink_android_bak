package com.yjkj.chainup.new_version.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.request.RequestOptions
import com.yjkj.chainup.util.JsonUtils
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.databinding.ActivityHorizonMarketDetailBinding
import com.yjkj.chainup.databinding.ActivityMarketDetail4Binding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.adapter.HKLineScaleAdapterV2
import com.yjkj.chainup.new_version.kline.bean.KLineBean
import com.yjkj.chainup.new_version.kline.data.DataManager
import com.yjkj.chainup.new_version.kline.data.KLineChartAdapter
import com.yjkj.chainup.new_version.kline.view.MainKlineViewStatus
import com.yjkj.chainup.new_version.kline.view.vice.ViceViewStatus
import com.yjkj.chainup.util.*
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.activity_horizon_market_detail.*
import kotlinx.android.synthetic.main.activity_horizon_market_detail.iv_logo
import kotlinx.android.synthetic.main.activity_horizon_market_detail.rv_kline_scale
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_24h_vol
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_close_price
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_coin_map
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_converted_close_price
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_high_price
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_low_price
import kotlinx.android.synthetic.main.activity_horizon_market_detail.tv_rose
import kotlinx.android.synthetic.main.activity_horizon_market_detail.v_kline
import kotlinx.android.synthetic.main.activity_market_detail4.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

/**
 * @description : 横屏行情的详细界面
 * @date 2019-3-16
 * @author Bertking
 *
 */
@Route(path = RoutePath.HorizonMarketDetailActivity)
class HorizonMarketDetailActivity : NBaseActivity(), WsAgentManager.WsResultCallback {

    override fun setContentView(): Int {
        return R.layout.activity_horizon_market_detail
    }

    lateinit var context: Context

    @JvmField
    @Autowired(name = "curTime")
    var curTime = ""


    @JvmField
    @Autowired(name = ParamConstant.TYPE)
    var type = ParamConstant.BIBI_INDEX

    var isFrist = true

    var klineData: ArrayList<KLineBean> = arrayListOf()
    private val adapter by lazy { KLineChartAdapter() }


    var coinMap = "BTC/USDT"

    var symbol = ""

    var jsonObject: JSONObject? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        context = this
        curTime = KLineUtil.getCurTime()
        changeTheme(true)
//        setContentView(R.layout.activity_horizon_market_detail)
        isLandscape = true
        super.onCreate(savedInstanceState)
        changeTheme(true)
        symbol = intent.getStringExtra("symbolHorizon") ?: ""

        val themeMode = PublicInfoDataService.getInstance().themeMode
        val kLineLogo = PublicInfoDataService.getInstance().getKline_background_logo_img(null, themeMode == PublicInfoDataService.THEME_MODE_DAYTIME)
        GlideUtils.load(this, kLineLogo, iv_logo, RequestOptions())


        tv_rose_title?.text = LanguageUtil.getString(context, "contract_text_upsdowns")
        tv_high_price_title?.text = LanguageUtil.getString(context, "kline_text_high")
        tv_low_price_title?.text = LanguageUtil.getString(context, "kline_text_low")

        tv_main_title?.text = LanguageUtil.getString(context, "kline_action_main")
        tv_vice_title?.text = LanguageUtil.getString(context, "kline_action_assistant")
        tv_rose_title?.text = LanguageUtil.getString(context, "contract_text_upsdowns")


        v_kline?.let {
            it.adapter = adapter
            it.startAnimation()
            it.justShowLoading()
        }

        initKLineScale()

        action4KLineIndex()

        jsonObject = NCoinManager.getSymbolObj(symbol)


        coinMap = NCoinManager.showAnoterName(jsonObject)
        symbol = jsonObject?.optString("symbol") ?: ""

        tv_coin_map?.text = coinMap
        isFrist = true
        klineData.clear()
        WsAgentManager.instance.addWsCallback(this)


        iv_close?.setOnClickListener {
            var message = MessageEvent(MessageEvent.market_switch_curTime)
            message.msg_content = curTime
            EventBusUtil.post(message)
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        WsAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        initSocket()
    }

    override fun onBackPressed() {
        var message = MessageEvent(MessageEvent.market_switch_curTime)
        message.msg_content = curTime
        EventBusUtil.post(message)
        super.onBackPressed()
    }

    /**
     * 渲染24H行情数据
     */
    private fun render24H(tick: QuotesData.Tick) {
        val price = jsonObject?.optInt("price") ?: 4

        runOnUiThread {
            if (tick.rose >= 0.0) {
                tv_close_price?.textColor = ColorUtil.getMainColorType()
            } else {
                tv_close_price?.textColor = ColorUtil.getMainColorType(isRise = false)
            }

            tv_coin_map?.text = coinMap
            tv_close_price?.text = DecimalUtil.cutValueByPrecision(tick.close, price)


            /**
             * 收盘价的法币换算
             */
            if (!TextUtils.isEmpty(coinMap) && coinMap.contains("/")) {
                val split = coinMap.split("/")
                tv_converted_close_price?.text = RateManager.getCNYByCoinName(split[1], tick.close, isLogo = true)
            }

            val rose = tick.rose.toString()
            RateManager.getRoseText(tv_rose ?: return@runOnUiThread, rose)
            tv_rose?.textColor = ColorUtil.getMainColorType(RateManager.getRoseTrend(rose) >= 0)
            tv_high_price?.text = DecimalUtil.cutValueByPrecision(tick.high, price)

            tv_low_price?.text = DecimalUtil.cutValueByPrecision(tick.low, price)
            tv_24h_vol?.text = BigDecimalUtils.showDepthVolume(tick.vol)

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
    @SuppressLint("ResourceType")
    private fun action4KLineIndex() {
        when (KLineUtil.getMainIndex()) {
            MainKlineViewStatus.MA.status -> {
                setKLineViewIndexStatus(position = 0)
                rb_ma?.typeface = Typeface.DEFAULT_BOLD
                rb_boll?.typeface = Typeface.DEFAULT
            }

            MainKlineViewStatus.BOLL.status -> {
                setKLineViewIndexStatus(position = 1)
                rb_ma?.typeface = Typeface.DEFAULT
                rb_boll?.typeface = Typeface.DEFAULT_BOLD
            }

            MainKlineViewStatus.NONE.status -> {
                setKLineViewIndexStatus(position = 2)
                rb_ma?.typeface = Typeface.DEFAULT
                rb_boll?.typeface = Typeface.DEFAULT
            }
        }

        when (KLineUtil.getViceIndex()) {
            ViceViewStatus.MACD.status -> {
                setKLineViewIndexStatus(isMain = false, position = 0)
                v_kline?.setChildDraw(0)
                rb_macd?.typeface = Typeface.DEFAULT_BOLD
                rb_kdj?.typeface = Typeface.DEFAULT
                rb_rsi?.typeface = Typeface.DEFAULT
                rb_wr?.typeface = Typeface.DEFAULT
            }

            ViceViewStatus.KDJ.status -> {
                setKLineViewIndexStatus(isMain = false, position = 1)
                v_kline?.setChildDraw(1)
                rb_macd?.typeface = Typeface.DEFAULT
                rb_kdj?.typeface = Typeface.DEFAULT_BOLD
                rb_rsi?.typeface = Typeface.DEFAULT
                rb_wr?.typeface = Typeface.DEFAULT
            }

            ViceViewStatus.RSI.status -> {
                setKLineViewIndexStatus(isMain = false, position = 2)
                v_kline?.setChildDraw(2)
                rb_macd?.typeface = Typeface.DEFAULT
                rb_kdj?.typeface = Typeface.DEFAULT
                rb_rsi?.typeface = Typeface.DEFAULT_BOLD
                rb_wr?.typeface = Typeface.DEFAULT

            }

            ViceViewStatus.WR.status -> {
                setKLineViewIndexStatus(isMain = false, position = 3)
                v_kline?.setChildDraw(3)
                rb_macd?.typeface = Typeface.DEFAULT
                rb_kdj?.typeface = Typeface.DEFAULT
                rb_rsi?.typeface = Typeface.DEFAULT
                rb_wr?.typeface = Typeface.DEFAULT_BOLD
            }

            ViceViewStatus.NONE.status -> {
                v_kline?.hideChildDraw()
                rb_macd?.typeface = Typeface.DEFAULT
                rb_kdj?.typeface = Typeface.DEFAULT
                rb_rsi?.typeface = Typeface.DEFAULT
                rb_wr?.typeface = Typeface.DEFAULT
            }
        }


        rg_main?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_ma -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                    KLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                    rb_ma?.typeface = Typeface.DEFAULT_BOLD
                    rb_boll?.typeface = Typeface.DEFAULT
                }
                R.id.rb_boll -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                    KLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                    rb_ma?.typeface = Typeface.DEFAULT
                    rb_boll?.typeface = Typeface.DEFAULT_BOLD
                }

                R.id.rb_hide_main -> {
                    v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                    KLineUtil.setMainIndex(MainKlineViewStatus.NONE.status)
                }
            }
        }


        rg_vice?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_macd -> {
                    v_kline?.setChildDraw(0)
                    KLineUtil.setViceIndex(ViceViewStatus.MACD.status)
                    rb_macd?.typeface = Typeface.DEFAULT_BOLD
                    rb_kdj?.typeface = Typeface.DEFAULT
                    rb_rsi?.typeface = Typeface.DEFAULT
                    rb_wr?.typeface = Typeface.DEFAULT
                }
                R.id.rb_kdj -> {
                    v_kline?.setChildDraw(1)
                    KLineUtil.setViceIndex(ViceViewStatus.KDJ.status)
                    rb_macd?.typeface = Typeface.DEFAULT
                    rb_kdj?.typeface = Typeface.DEFAULT_BOLD
                    rb_rsi?.typeface = Typeface.DEFAULT
                    rb_wr?.typeface = Typeface.DEFAULT
                }

                R.id.rb_rsi -> {
                    v_kline?.setChildDraw(2)
                    KLineUtil.setViceIndex(ViceViewStatus.RSI.status)
                    rb_macd?.typeface = Typeface.DEFAULT
                    rb_kdj?.typeface = Typeface.DEFAULT
                    rb_rsi?.typeface = Typeface.DEFAULT_BOLD
                    rb_wr?.typeface = Typeface.DEFAULT
                }

                R.id.rb_wr -> {
                    v_kline?.setChildDraw(3)
                    rb_macd?.typeface = Typeface.DEFAULT
                    rb_kdj?.typeface = Typeface.DEFAULT
                    rb_rsi?.typeface = Typeface.DEFAULT
                    rb_wr?.typeface = Typeface.DEFAULT_BOLD
                    KLineUtil.setViceIndex(ViceViewStatus.WR.status)
                }

                R.id.rb_hide_vice -> {
                    v_kline?.hideChildDraw()
                    KLineUtil.setViceIndex(ViceViewStatus.NONE.status)
                }
            }
        }
        if (isblack){
            rb_ma.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
            rb_boll.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
            rb_macd.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
            rb_kdj.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
            rb_rsi.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
            rb_wr.setTextColor(resources.getColorStateList(R.drawable.kline_index_text_selector_night))
        }
    }

    /**
     * 处理K线刻度
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true)
        val klineScale = KLineUtil.getKLineScale()
        val layoutManager = GridLayoutManager(context, klineScale.size)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        val adapter = HKLineScaleAdapterV2(klineScale,isblack)
        rv_kline_scale?.adapter = adapter
        /**
         * 分时线
         */
        v_kline?.setMainDrawLine(KLineUtil.getCurTime4Index() == 0)

        adapter.setOnItemClickListener { viewHolder, view, position ->
            /**
             * 分时线
             */
            v_kline?.setMainDrawLine(position == 0)
            if (position != KLineUtil.getCurTime4Index()) {
                for (i in 0 until klineScale.size) {
                    val textView = viewHolder.getViewByPosition(i, R.id.tv_scale) as TextView
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    if (isblack){
                        textView.setTextColor(ColorUtil.getColor(R.color.normal_text_color_kline_night))
                    }else{
                        textView.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                    }

                }

                val textView = viewHolder.getViewByPosition(position, R.id.tv_scale) as TextView
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.kline_item_selected_shape)
                if (isblack){
                    textView.setTextColor(ColorUtil.getColor(R.color.text_color_kline_night))
                }else{
                    textView.setTextColor(ColorUtil.getColor(R.color.text_color))
                }

                KLineUtil.setCurTime4KLine(position)
                val time = curTime
                curTime = klineScale[position]
                Log.e("jinlong", "v_kline：$curTime   time:$time ")
                KLineUtil.setCurTime(klineScale[position])
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
            WsAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "line" to scale), this)
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
                    val quotesData = JsonUtils.convert2Quote(jsonObj.toString())
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
                        val kLineEntity = KLineBean()
                        val data = jsonObj.optJSONObject("tick") ?: return@doAsync
                        kLineEntity.id = data.optLong("id")
                        kLineEntity.openPrice = BigDecimal(data.optString("open")).toFloat()
                        kLineEntity.closePrice = BigDecimal(data.optString("close")).toFloat()
                        kLineEntity.highPrice = BigDecimal(data.optString("high")).toFloat()
                        kLineEntity.lowPrice = BigDecimal(data.optString("low")).toFloat()
                        kLineEntity.volume = BigDecimal(data.optString("vol")).toFloat()

                        try {
                            if (klineData.isNotEmpty() && klineData.size != 0) {
                                val isRepeat = klineData.last().id == data.optLong("id")
                                if (isRepeat) {
                                    klineData[klineData.lastIndex] = kLineEntity
                                    DataManager.calculate(klineData)
                                    adapter.changeItem(klineData.lastIndex, kLineEntity)
                                } else {
                                    klineData.add(kLineEntity)
                                    DataManager.calculate(klineData)
                                    runOnUiThread {
                                        adapter.addItem(kLineEntity)
                                        v_kline?.refreshEnd()
                                    }
                                }
                            } else {
                                klineData.add(kLineEntity)
                                DataManager.calculate(klineData)
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
            val type = object : TypeToken<ArrayList<KLineBean>>() {
            }.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                klineData.addAll(gson.fromJson(json.getJSONArray("data").toString(), type))
                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    var scale = if (curTime == "line") "1min" else curTime
                    WsAgentManager.instance.sendData(WsLinkUtils.getKlineHistoryOther(symbol, scale, klineData[0].id.toString()))
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
            DataManager.calculate(klineData)
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
            WsAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
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
        WsAgentManager.instance.unbind(this, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        WsAgentManager.instance.removeWsCallback(this)
    }

    /**
     * Ws 发送消息
     */
    private fun sendMsg(msg: String) {

    }


    override fun onWsMessage(json: String) {
        handleData(json)
    }
    var isblack = true


    fun changeTheme(iskline: Boolean) {
        if (PublicInfoDataService.getInstance().klineThemeMode == 1){
            isblack = true
            StatusBarUtil.setDarkMode(this)
            window.statusBarColor = ColorUtil.getColorByMode(R.color.bg_card_color, true)
            window.navigationBarColor = ColorUtil.getColorByMode(R.color.bg_card_color_kline_night, true)

        }

        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_horizon_market_detail)
        binding.isblack = isblack
    }

    lateinit var binding: ActivityHorizonMarketDetailBinding

}
