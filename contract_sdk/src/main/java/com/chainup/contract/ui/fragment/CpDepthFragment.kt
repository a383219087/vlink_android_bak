package com.chainup.contract.ui.fragment


import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.R
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpDepthBean
import com.chainup.contract.bean.CpFlagBean
import com.chainup.contract.bean.CpTransactionData
import com.chainup.contract.utils.*
import com.chainup.contract.view.CpBBKlineDataDepthHelper
import com.chainup.contract.view.CpDepthMarkView
import com.chainup.contract.view.CpDepthYValueFormatter
import com.chainup.contract.view.CpWrapContentViewPager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.yjkj.chainup.bean.kline.cp.DepthItem
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.cp_fragment_depth.*
import kotlinx.android.synthetic.main.cp_depth_chart_com.*
import kotlinx.android.synthetic.main.cp_item_depth_sell.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.math.max


/**
 * @author Bertking
 * @description 市场详情下的"深度"
 * @date 2019-3-20
 *
 */

class CpDepthFragment : CpNBaseFragment() {
//    val TAG = ClDepthFragment::class.java.simpleName

    private var viewPager: CpWrapContentViewPager? = null
    private var datas: DepthItem? = null

    private var contractId: Int = -1
    private var symbol: String = ""

    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()

    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    private var pricePrecision = 2
    private var volumePrecision = 2

    private var riseColor = CpColorUtil.getMainColorType(isRise = true)
    private var fallColor = CpColorUtil.getMainColorType(isRise = false)

    private val riseMinorColor = CpColorUtil.getMinorColorType(isRise = true)
    private val fallMinorColor = CpColorUtil.getMinorColorType(isRise = false)

    var flagBean: CpFlagBean? = null

    // 记录上次订阅的币对
    var lastSymbol: String = ""

    var defaultThreshold = "0.1"

    override fun setContentView(): Int {
        return R.layout.cp_fragment_depth
    }


    override fun initView() {
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, 0)
        }
    }

    companion object {

        val liveData = MutableLiveData<CpFlagBean>()
        var liveData4closePrice: MutableLiveData<List<String>> = MutableLiveData()

        @JvmStatic
        fun newInstance(viewPager: CpWrapContentViewPager, contractId: Int, symbol: String) =
                CpDepthFragment().apply {
                    this.viewPager = viewPager
                    this.contractId = contractId
                    this.symbol = symbol
                    arguments = Bundle().apply {
                        //                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDepthView()
        initDepthChart()
        liveData.observe(this, Observer {
            this.symbol = it.symbol
            this.contractId = it.contractId.toInt()
            if (it == null || flagBean?.symbol == it.symbol) {
                return@Observer
            }

            activity?.runOnUiThread {
                clearDepthView()
            }

            //un sub last symbol 's depth
            if (flagBean != null) {
                sendMessage(WsLinkUtils.getDepthLink(flagBean?.symbol ?: "", false).json)
            }
            flagBean = it
            /**
             * 取精度
             */
            pricePrecision = it.pricePrecision
            volumePrecision = it.volumePrecision

            val priceUnit =   "(${flagBean?.quotesSymbol})"

            tv_price_title?.text = context?.getString(R.string.cp_overview_text6) + priceUnit

            val amountUnit =   if (CpClLogicContractSetting.getContractUint(activity) == 0) "(${getString(R.string.cp_overview_text9)})" else "(${flagBean?.baseSymbol})"

            tv_buy_volume_title?.text = context?.getString(R.string.cp_overview_text8_buy) + amountUnit
            tv_sell_volume_title?.text =
                    context?.getString(R.string.cp_overview_text8_sell) + amountUnit


            if (flagBean?.isContract == true) {
                tv_buy_tape_title?.text = context?.getString(R.string.cp_extra_text50)
                tv_sell_tape_title?.text = context?.getString(R.string.cp_extra_text51)
            } else {
                tv_buy_tape_title?.text = context?.getString(R.string.cp_extra_text50)
                tv_sell_tape_title?.text = context?.getString(R.string.cp_extra_text51)
            }

            showProgressDialog("")
            initSocket(URI(""))
            initTimerDepthView()
        })

    }

    fun showProgressDialog(msg: String) {

        prb_loading?.visibility = View.VISIBLE
        tv_loading?.visibility = View.VISIBLE

        ll_depth?.visibility = View.GONE
        ll_depth_title?.visibility = View.GONE
        view_line?.visibility = View.GONE
        customize_depth_chart?.visibility = View.GONE

    }

    fun cancelProgressDialog() {
        prb_loading?.visibility = View.GONE
        tv_loading?.visibility = View.GONE

        ll_depth?.visibility = View.VISIBLE
        ll_depth_title?.visibility = View.VISIBLE
//        view_line?.visibility = View.VISIBLE
//        customize_depth_chart?.visibility = View.VISIBLE

    }


    /**
     * 买卖盘
     * 初始化交易详情记录view
     */
    private fun initDepthView() {
        for (i in 1..20) {
            /**
             * 卖盘
             */
            val view: View = layoutInflater.inflate(R.layout.cp_item_depth_sell, null)
            val layout = view.findViewById<FrameLayout>(R.id.fl_bg_item)
            view.tv_price_item_for_depth?.setTextColor(fallColor)
            ll_sell?.addView(view)
            sellViewList.add(view)
            /***********/

            /**
             * 买盘
             */
            val view1: View = layoutInflater.inflate(R.layout.cp_item_depth_buy, null)
            view1.tv_price_item_for_depth.setTextColor(riseColor)
            ll_buy?.addView(view1)
            buyViewList.add(view1)
        }

    }


    private fun initSocket(uri: URI) {
        sendMessage(WsLinkUtils.getDepthLink(flagBean?.symbol ?: "").json)
    }

    /**
     * WebSocket 发送消息
     */
    private fun sendMessage(msg: String) {

    }


    /**
     * 重置买卖盘的数据
     */
    private fun clearDepthView() {
        clearDepthChart()

        if (sellViewList.isEmpty()) return
        if (buyViewList.isEmpty()) return

        for (i in 0 until 20) {
            sellViewList[i].run {
                tv_price_item_for_depth?.text = "--"
                tv_quantity_item_for_depth?.text = "--"
                fl_bg_item_for_depth?.setBackgroundResource(R.color.transparent)
            }
            buyViewList[i].run {
                tv_price_item_for_depth?.text = "--"
                tv_quantity_item_for_depth?.text = "--"
                fl_bg_item_for_depth?.setBackgroundResource(R.color.transparent)
            }
        }
    }


    /**
     * 配置深度图的基本属性
     */
    @SuppressLint("NewApi")
    private fun initDepthChart() {
        depth_chart?.setNoDataText(context?.getString(R.string.cp_extra_text52))
        depth_chart?.setNoDataTextColor(resources.getColor(R.color.normal_text_color))
        depth_chart?.setTouchEnabled(true)

        v_buy_tape?.backgroundColor = CpColorUtil.getMainColorType()
        v_sell_tape?.backgroundColor = CpColorUtil.getMainColorType(isRise = false)
        tv_buy_tape_title?.textColor = CpColorUtil.getMainColorType()
        tv_sell_tape_title?.textColor = CpColorUtil.getMainColorType(isRise = false)

        /**
         * 图例 的相关设置
         */
        val legend = depth_chart.legend
        legend.isEnabled = false
        /**
         * 是否缩放
         */
        depth_chart?.setScaleEnabled(false)
        /**
         * X,Y同时缩放
         */
        depth_chart?.setPinchZoom(false)

        /**
         * 关闭图表的描述信息
         */
        depth_chart?.description?.isEnabled = false


        // 打开触摸手势
        depth_chart?.setTouchEnabled(true)
        depth_chart.isLongClickable = true
        depth_chart.isNestedScrollingEnabled = false

        /**
         * X
         */
        val xAxis: XAxis = depth_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(3, true)
        // 不绘制竖直方向的方格线
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 0.5f
        //x坐标轴不可见
        xAxis.isEnabled = true
        //禁止x轴底部标签
        xAxis.setDrawLabels(true)
        //最小的间隔设置
        xAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        xAxis.textSize = 10f
        //在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项。
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toString().toTextPrice(pricePrecision)
            }
        }
        /**
         * Y
         */
        depth_chart.axisRight.isEnabled = true
        depth_chart.axisLeft.isEnabled = false
        /**********左边Y轴********/
        depth_chart.axisLeft.axisMinimum = 0f
        /**********右边Y轴********/
        val yAxis = depth_chart.axisRight
        yAxis.setDrawGridLines(false)
        yAxis.setDrawAxisLine(false)
        //设置Y轴的Label显示在图表的内侧还是外侧，默认外侧
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //不绘制水平方向的方格线
        yAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        yAxis.textSize = 10f
        //设置Y轴显示的label个数
        yAxis.setLabelCount(6, true)
        //控制上下左右坐标轴显示的距离
        depth_chart?.setViewPortOffsets(0f, 20f, 0f, CpDisplayUtil.dip2px(14f))
        yAxis.valueFormatter = CpDepthYValueFormatter()


        depth_chart?.setOnClickListener {
            if (depth_chart.marker != null) {
                depth_chart.marker = null
            }
        }

        depth_chart?.setOnLongClickListener {
            val mv = CpDepthMarkView(activity!!, R.layout.cp_layout_depth_marker)
            mv.chartView = depth_chart // For bounds control
            depth_chart?.marker = mv // Set the marker to the ch
            false
        }


    }


    /**
     * 设置lineDataSet  in深度图
     */
    private fun lineDataSet(yData: ArrayList<Entry>, isBuy: Boolean): LineDataSet {
        val lineDataSet: LineDataSet?
        if (isBuy) {
            lineDataSet = LineDataSet(yData, "Buy")
            lineDataSet.color = CpColorUtil.getMainColorType()
            lineDataSet.fillColor = CpColorUtil.getMainColorType()
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = CpColorUtil.getMainColorType()

        } else {
            lineDataSet = LineDataSet(yData, "Sell")
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
            lineDataSet.fillColor = CpColorUtil.getMainColorType(isRise = false)
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
        }
        /**
         * 是否填充折线以及填充色设置
         */
        lineDataSet.setDrawFilled(true)

        /**
         * 控制MarkView的显示与隐藏
         * 点击是否显示高亮线
         */
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = Color.TRANSPARENT


        /**
         * 设置折线的宽度
         */
        lineDataSet.lineWidth = 2.0f


        /**
         * 隐藏每个数据点的值
         */
        lineDataSet.setDrawValues(false)

        /**
         * 数据点是否用小圆圈表示
         */
        lineDataSet.setDrawCircles(false)
        return lineDataSet
    }

    /**
     * 清理深度
     */
    fun clearDepthChart() {
        depth_chart?.clear()
        depth_chart?.notifyDataSetChanged()
        depth_chart?.invalidate()
    }


    fun updateDepth() {
        activity?.runOnUiThread {
            setData4DepthChart()
        }
    }


    /**
     * 设置深度图数据
     */
    private fun setData4DepthChart() {

        if (datas == null) {
            clearDepthChart()
            return
        }
        datas = datas?.parseDepth()
        val sellList = ArrayList<CpDepthBean>()
        val buyList = ArrayList<CpDepthBean>()

        sellList.addAll(datas?.sellItem!! as ArrayList<CpDepthBean>)
        buyList.addAll(datas?.buyItem!! as ArrayList<CpDepthBean>)
        if (sellList.size == 0 || buyList.size == 0) {
            return
        }
        val yData = arrayListOf<Entry>()
        var buyVolumeSum = 0.0
        // TODO 优化
        for (i in buyList.indices) {
            buyVolumeSum = CpBigDecimalUtils.add(buyVolumeSum.toString(), buyList[i].vol).toDouble()
            val volSum = buyList[i].sum.toFloat()
            val entry = Entry(
                    (buyList[i].price).toFloat(),
                    volSum,
                    buyList[i].price.toTextPrice(pricePrecision)
            )
            yData.add(0, entry)
        }
        /*************处理卖盘数据*********/
        var sellVolumeSum = 0.0
        val sellYData = ArrayList<Entry>()
        for (i in sellList.indices) {
            sellVolumeSum =
                    CpBigDecimalUtils.add(sellVolumeSum.toString(), sellList[i].vol).toDouble()
            val volSum = sellList[i].sum.toFloat()
            val entry = Entry(
                    sellList[i].price.toFloat(),
                    volSum,
                    sellList[i].price.toTextPrice(pricePrecision)
            )
            sellYData.add(entry)
        }

        /**
         * Y 轴最大值 和 最小值
         */
        var maxVolume: Float = max(
                buyList.get(buyList.size - 1).sum.toFloat(),
                sellList.get(sellList.size - 1).sum.toFloat()
        )
        maxVolume = if (flagBean?.isContract == true) {
            if (!flagBean?.mMultiplier.equals("0")) {
                if (flagBean?.coUnit == 0) maxVolume else CpBigDecimalUtils.mulStr(
                        maxVolume.toString(),
                        flagBean?.mMultiplier,
                        flagBean?.volumePrecision!!
                ).toFloat()
            } else {
                maxVolume
            }
        } else {
            maxVolume
        }

        depth_chart ?: return
        var xAxis = depth_chart.xAxis
        xAxis.axisMinimum = buyList.get(buyList.size - 1).price.toFloat()
        xAxis.axisMaximum = sellList.get(sellList.size - 1).price.toFloat()
        var yAxis = depth_chart.axisRight
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = maxVolume * 1.1f

        val buyLineDataSet = lineDataSet(yData, true)
        val sellLineDataSet = lineDataSet(sellYData, false)

        val lineData = LineData(buyLineDataSet, sellLineDataSet)

        depth_chart.data = lineData
        depth_chart.invalidate()

//        tv_buy_price?.text = DecimalUtil.cutValueByPrecision(boundaryValue4Buy, pricePrecision)
//        tv_close_price?.text = DecimalUtil.cutValueByPrecision(closePrice, pricePrecision)
//        tv_sell_price?.text = DecimalUtil.cutValueByPrecision(boundaryValue4Sell, pricePrecision)
    }


    /**
     * 处理数据
     */
    fun handleData(data: String) {
        val json = JSONObject(data)
        /**
         * 深度
         * 有tick，需根据channel字段来判断
         */
        if (!json.isNull("tick")) {
            /**
             * 深度
             */
            val channel = json.optString("channel")
            if (channel == WsLinkUtils.getDepthLink(
                            flagBean?.symbol
                                    ?: ""
                    ).channel
            ) {
//                    Log.d(TAG, "=======深度：$data")

                CpBBKlineDataDepthHelper.instance?.updateDepthByType(json)


                val transactionData =
                        CpJsonUtils.jsonToBean(json.toString(), CpTransactionData::class.java)
                /**
                 *卖盘取最小
                 */
                if (null != transactionData.tick) {
                    val tick = transactionData.tick
                    tick.asks.sortByDescending { it.get(0).asDouble }
                    /**
                     * 买盘取最大
                     */
                    tick.buys.sortByDescending { it.get(0).asDouble }
                }

                if (isAdded && activity != null) {
                    //深度图
                    activity?.runOnUiThread {
                        refreshDepthView(json)
                    }
                }
            }
        }
    }


    /**
     * 更新买卖盘的数据
     */
    private fun refreshDepthView(jsonObject: JSONObject) {
        val tickJSONObject = jsonObject.optJSONObject("tick")

        val buys = tickJSONObject.optJSONArray("buys")
        val buysList = arrayListOf<JSONArray>()

        val asks = tickJSONObject.optJSONArray("asks")
        val asksList = arrayListOf<JSONArray>()
        /**
         * 买盘取最大
         */
        if (buys.length() != 0) {
            for (i in 0 until buys.length()) {
                buysList.add(buys.optJSONArray(i))
            }
            buysList.sortByDescending {
                it.optDouble(0)
            }
        }

        /**
         *卖盘取最小
         */
        if (asks.length() != 0) {
            for (i in 0 until asks.length()) {
                asksList.add(asks.optJSONArray(i))
            }

            asksList.sortByDescending {
                it.optDouble(0)
            }
        }


        var subList = mutableListOf<JSONArray>()
        subList = if (asksList.size >= 20) {
            if (asksList.size - sellViewList.size < 0) {
                asksList
            } else {
                asksList.subList(asksList.size - sellViewList.size, asksList.size)
            }

        } else {
            asksList
        }
        subList.sortBy {
            it.optDouble(0)
        }

        var sellVolumeSum = 0.0
        for (sell in subList) {
            sellVolumeSum += sell.optDouble(1)
        }


        var buySubList = mutableListOf<JSONArray>()
        buySubList = if (buysList.size >= 20) {
            buysList.subList(0, 20)
        } else {
            buysList
        }
        var buyVolumeSum = 0.0
        for (buy in buySubList) {
            buyVolumeSum += buy.optDouble(1)
        }


        for (i in 0 until sellViewList.size) {
            /**
             * 卖盘
             */
            if (asksList.size > sellViewList.size) {
                /**
                 * 移除大值
                 */
                /*****深度背景色START****/
                sellViewList[0].ll_item_layout.post {
                    sellViewList[i].fl_bg_item_for_depth?.backgroundColor = fallMinorColor
                    val layoutParams = sellViewList[i].fl_bg_item_for_depth?.layoutParams
                    var curSellVolumeSum = 0.0
                    for (x in 0..i) {
                        curSellVolumeSum += subList[x].optDouble(1)
                    }
                    val width =
                            (curSellVolumeSum / sellVolumeSum) * CpDisplayUtil.getScreenWidth() * 0.5
                    layoutParams?.width = width.toInt()
                    sellViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams
                }

                /*****深度背景色END****/
                sellViewList[i].tv_price_item_for_depth?.text = CpDecimalUtil.cutValueByPrecision(
                        subList[i].get(0).toString().trim(),
                        pricePrecision
                )
                val vol = subList[i].get(1).toString()
                sellViewList[i].tv_quantity_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            if (!flagBean?.mMultiplier.equals("0")) {
                                if (flagBean?.coUnit == 0) vol else CpBigDecimalUtils.mulStr(
                                        vol,
                                        flagBean?.mMultiplier,
                                        flagBean?.volumePrecision!!
                                )
                            } else {
                                vol
                            }
                        } else {
                            CpDecimalUtil.cutValueByPrecision(
                                    subList[i].get(1).toString(),
                                    volumePrecision
                            )
                        }

            } else {
                sellViewList[i].run {
                    tv_price_item_for_depth?.text = "--"
                    tv_quantity_item_for_depth?.text = "--"
                }

                if (i < asksList.size) {
                    /*****深度背景色START****/
                    sellViewList[i].fl_bg_item_for_depth?.backgroundColor = fallMinorColor
                    val layoutParams = sellViewList[i].fl_bg_item_for_depth?.layoutParams
                    var curSellVolumeSum = 0.0
                    for (x in 0..i) {
                        curSellVolumeSum += asksList[x].optDouble(1)
                    }
                    val width =
                            (curSellVolumeSum / sellVolumeSum) * CpDisplayUtil.getScreenWidth() * 0.5
                    layoutParams?.width = width.toInt()
                    sellViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams

                    /*****深度背景色END****/
                    val price4DepthSell = asksList[i].optString(0).trim()
                    sellViewList[i].tv_price_item_for_depth?.text =
                            CpDecimalUtil.cutValueByPrecision(price4DepthSell, pricePrecision)

                    var vol = asksList[i].get(1).toString().trim()
                    sellViewList[i].tv_quantity_item_for_depth?.text =
                            if (flagBean?.isContract == true) {
                                if (!flagBean?.mMultiplier.equals("0")) {
                                    if (flagBean?.coUnit == 0) vol else CpBigDecimalUtils.mulStr(
                                            vol,
                                            flagBean?.mMultiplier,
                                            flagBean?.volumePrecision!!
                                    )
                                } else {
                                    vol
                                }
                            } else {
                                CpDecimalUtil.cutValueByPrecision(
                                        asksList[i].optString(1).trim(),
                                        volumePrecision
                                )
                            }
                }
            }

            /**
             * 买盘
             */
            if (buysList.size > i) {
                /*****深度背景色START****/
                buyViewList[i].fl_bg_item_for_depth?.backgroundColor = riseMinorColor
                val layoutParams = buyViewList[i].fl_bg_item_for_depth?.layoutParams
                var curBuyVolumeSum = 0.0
                for (x in 0..i) {
                    curBuyVolumeSum += buysList[x].optDouble(1)
                }

                val width = (curBuyVolumeSum / buyVolumeSum) * CpDisplayUtil.getScreenWidth() * 0.5
//                Log.d(TAG, "=====buy======${DisplayUtil.getScreenWidth() * 0.5}=======")
                layoutParams?.width = width.toInt()
                buyViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams

                /*****深度背景色END****/
                val price4DepthBuy = buysList[i].optString(0).trim()

//                Log.d(TAG, "=======price4Depth:$price4DepthBuy===")
                buyViewList[i].tv_price_item_for_depth?.text =
                        CpDecimalUtil.cutValueByPrecision(price4DepthBuy, pricePrecision)
                val vol = buysList[i].get(1).toString().trim()
                buyViewList[i].tv_quantity_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            if (!flagBean?.mMultiplier.equals("0")) {
                                if (flagBean?.coUnit == 0) vol else CpBigDecimalUtils.mulStr(
                                        vol,
                                        flagBean?.mMultiplier,
                                        flagBean?.volumePrecision!!
                                )
                            } else {
                                vol
                            }
                        } else {
                            CpDecimalUtil.cutValueByPrecision(
                                    buysList[i].optString(1).trim(),
                                    volumePrecision
                            )
                        }

            } else {
                buyViewList[i].run {
                    tv_price_item_for_depth?.text = "--"
                    tv_quantity_item_for_depth?.text = "--"
                    fl_bg_item_for_depth?.setBackgroundResource(R.color.transparent)
                }
            }
            cancelProgressDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restart()

    }



    fun onClDepthFragment(json: String) {
        handleData(json)
    }

    var subscribe: Disposable? = null//保存订阅者

    private fun initTimerDepthView() {
        if (flagBean == null || flagBean?.symbol == null) {
            return
        }
        restart()
        val loopTime = if (flagBean?.isContract == true) 5 else CpCommonConstant.depthLoopTime
        subscribe = Observable.interval(0L, loopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "getCoinSymbol==data is ")
                    getCoinDepth()
                }
    }

    private fun getCoinDepth() {
        addDisposable(
                getContractModel().getCoinDepth(contractId, symbol,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data")?.run {
                                    datas =
                                            Gson().fromJson<DepthItem>(this.toString(), DepthItem::class.java)
                                    updateDepth()
                                }
                                cancelProgressDialog()
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                cancelProgressDialog()
                            }
                        })
        )
    }

    /**
     * 结束计时,重新开始
     */
    fun restart() {
        Log.e("LogUtils", "dispose ClDepthFragment ${subscribe}")
        if (subscribe != null) {
            subscribe?.dispose()//取消订阅
            Log.e("LogUtils", "dispose ClDepthFragment time")
        }
    }

//    private fun depthView(): Observable<DepthItem> {
////        var retryCount = 0
////        return HttpClient.instance.getCoinDepth(flagBean?.symbol!!, flagBean?.contractId!!).map {
////            it.data
////        }.retryWhen { throwableObservable ->
////            return@retryWhen throwableObservable.flatMap {
////                if (retryCount == 3) {
////                    return@flatMap Observable.error<Throwable>(IllegalArgumentException(""))
////                } else {
////                    retryCount++
////                    return@flatMap Observable.timer(2000, TimeUnit.MILLISECONDS)
////                }
////            }
////        }.compose(RxUtil.applySchedulersToObservable())
//    }

    /**
     * 获取币种简介
     */
    private fun getCoinSymbol() {
//        depthView().subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    Log.d(TAG, "getCoinSymbol==data ${it}")
//                    datas = it
//                    cancelProgressDialog()
//                    updateDepth()
//                }, {
//                    it.printStackTrace()
//                    cancelProgressDialog()
//                })
    }

    override fun onPause() {
        super.onPause()
        restart()
    }

    override fun onResume() {
        super.onResume()
        initTimerDepthView()
    }


}