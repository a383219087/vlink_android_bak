package com.yjkj.chainup.new_version.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yjkj.chainup.util.JsonUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.DepthBean
import com.yjkj.chainup.bean.TransactionData
import com.yjkj.chainup.bean.kline.DepthItem
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.new_version.bean.FlagBean
import com.yjkj.chainup.new_version.view.depth.BBKlineDataDepthHelper
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.depth_chart_com.*
import kotlinx.android.synthetic.main.fragment_depth.*
import kotlinx.android.synthetic.main.item_depth_buy.view.*
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

class DepthFragment : Fragment() {
    val TAG = DepthFragment::class.java.simpleName

    private var viewPager: WrapContentViewPager? = null
    private var datas: DepthItem? = null

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

    private var riseColor = ColorUtil.getMainColorType(isRise = true)
    private var fallColor = ColorUtil.getMainColorType(isRise = false)

    private val riseMinorColor = ColorUtil.getMinorColorType(isRise = true)
    private val fallMinorColor = ColorUtil.getMinorColorType(isRise = false)
    private var dataHelper = BBKlineDataDepthHelper.instance

    var flagBean: FlagBean? = null

    // 记录上次订阅的币对
    var lastSymbol: String = ""

    var defaultThreshold = "0.1"

    /**
     * 计算买卖盘的百分比
     */


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_depth, container, false)
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, 0)
        }
        return view
    }

    companion object {

        val liveData = MutableLiveData<FlagBean>()
        var liveData4closePrice: MutableLiveData<List<String>> = MutableLiveData()

        @JvmStatic
        fun newInstance(viewPager: WrapContentViewPager) =
                DepthFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        //                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDepthView()
        liveData.observe(this, Observer {
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

            val priceUnit = if (flagBean?.isContract == true) {
                ""
            } else {
                "(${flagBean?.quotesSymbol})"
            }

            tv_price_title?.text = context?.getString(R.string.contract_text_price) + priceUnit

            val amountUnit = if (flagBean?.isContract == true) {
//                "(张)"
                ""
            } else {
                "(${flagBean?.baseSymbol})"
            }
            tv_buy_volume_title?.text = context?.getString(R.string.charge_text_volume) + amountUnit
            tv_sell_volume_title?.text = context?.getString(R.string.charge_text_volume2) + amountUnit


            if (flagBean?.isContract == true) {
                tv_buy_tape_title?.text = context?.getString(R.string.contract_text_buyMarket)
                tv_sell_tape_title?.text = context?.getString(R.string.contract_text_sellMarket)
            } else {
                tv_buy_tape_title?.text = context?.getString(R.string.contract_text_buyMarket)
                tv_sell_tape_title?.text = context?.getString(R.string.contract_text_sellMarket)
            }

            defaultThreshold = NCoinManager.getDefaultThresholdForSort(flagBean?.symbol)
            showProgressDialog("")
            initSocket(URI(""))
        })

    }

    fun showProgressDialog(msg: String) {

        prb_loading?.visibility = View.VISIBLE
        tv_loading?.visibility = View.VISIBLE

        ll_depth?.visibility = View.GONE
        ll_depth_title?.visibility = View.GONE

    }

    fun cancelProgressDialog() {
        prb_loading?.visibility = View.GONE
        tv_loading?.visibility = View.GONE

        ll_depth?.visibility = View.VISIBLE
        ll_depth_title?.visibility = View.VISIBLE

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
            val view: View = layoutInflater.inflate(R.layout.item_depth_sell, null)
            val layout = view.findViewById<FrameLayout>(R.id.fl_bg_item)
            view.tv_price_item_for_depth?.setTextColor(fallColor)
            ll_sell?.addView(view)
            sellViewList.add(view)
            /***********/

            /**
             * 买盘
             */
            val view1: View = layoutInflater.inflate(R.layout.item_depth_buy, null)
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
            if (channel == WsLinkUtils.getDepthLink(flagBean?.symbol
                            ?: "").channel) {
//                    Log.d(TAG, "=======深度：$data")

                BBKlineDataDepthHelper.instance?.updateDepthByType(json)


                val transactionData = JsonUtils.jsonToBean(json.toString(), TransactionData::class.java)
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
                    val width = (curSellVolumeSum / sellVolumeSum) * DisplayUtil.getScreenWidth() * 0.5
                    layoutParams?.width = width.toInt()
                    sellViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams
                }

                /*****深度背景色END****/
                sellViewList[i].tv_price_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            Contract2PublicInfoManager.cutValueByPrecision(subList[i].get(0).toString().trim(), pricePrecision)
                        } else {
                            DecimalUtil.cutValueByPrecision(subList[i].get(0).toString().trim(), pricePrecision)
                        }
                val vol = subList[i].get(1).toString()
                sellViewList[i].tv_quantity_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            if (!flagBean?.mMultiplier.equals("0")) {
                                if (flagBean?.coUnit == 0) vol else BigDecimalUtils.mulStr(vol, flagBean?.mMultiplier, flagBean?.volumePrecision!!)
                            } else {
                                vol
                            }
                        } else {
                            DecimalUtil.cutValueByPrecision(subList[i].get(1).toString(), volumePrecision)
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
                    val width = (curSellVolumeSum / sellVolumeSum) * DisplayUtil.getScreenWidth() * 0.5
                    layoutParams?.width = width.toInt()
                    sellViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams

                    /*****深度背景色END****/
                    val price4DepthSell = asksList[i].optString(0).trim()
                    sellViewList[i].tv_price_item_for_depth?.text =
                            if (flagBean?.isContract == true) {
                                Contract2PublicInfoManager.cutValueByPrecision(price4DepthSell, pricePrecision)
                            } else {
                                DecimalUtil.cutValueByPrecision(price4DepthSell, pricePrecision)
                            }

                    var vol = asksList[i].get(1).toString().trim()
                    sellViewList[i].tv_quantity_item_for_depth?.text =
                            if (flagBean?.isContract == true) {
                                if (!flagBean?.mMultiplier.equals("0")) {
                                    if (flagBean?.coUnit == 0) vol else BigDecimalUtils.mulStr(vol, flagBean?.mMultiplier, flagBean?.volumePrecision!!)
                                } else {
                                    vol
                                }
                            } else {
                                DecimalUtil.cutValueByPrecision(asksList[i].optString(1).trim(), volumePrecision)
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

                val width = (curBuyVolumeSum / buyVolumeSum) * DisplayUtil.getScreenWidth() * 0.5
//                Log.d(TAG, "=====buy======${DisplayUtil.getScreenWidth() * 0.5}=======")
                layoutParams?.width = width.toInt()
                buyViewList[i].fl_bg_item_for_depth?.layoutParams = layoutParams

                /*****深度背景色END****/
                val price4DepthBuy = buysList[i].optString(0).trim()

//                Log.d(TAG, "=======price4Depth:$price4DepthBuy===")
                buyViewList[i].tv_price_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            Contract2PublicInfoManager.cutValueByPrecision(price4DepthBuy, pricePrecision)
                        } else {
                            DecimalUtil.cutValueByPrecision(price4DepthBuy, pricePrecision)
                        }
                val vol = buysList[i].get(1).toString().trim()
                buyViewList[i].tv_quantity_item_for_depth?.text =
                        if (flagBean?.isContract == true) {
                            if (!flagBean?.mMultiplier.equals("0")) {
                                if (flagBean?.coUnit == 0) vol else BigDecimalUtils.mulStr(vol, flagBean?.mMultiplier, flagBean?.volumePrecision!!)
                            } else {
                                vol
                            }
                        } else {
                            DecimalUtil.cutValueByPrecision(buysList[i].optString(1).trim(), volumePrecision)
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


    override fun onStop() {
        super.onStop()
        Log.e("LogUtils", "DepthFragment onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("LogUtils", "DepthFragment onDestroy()")
    }

    fun onDepthFragment(json: String) {
        handleData(json)
    }



}

