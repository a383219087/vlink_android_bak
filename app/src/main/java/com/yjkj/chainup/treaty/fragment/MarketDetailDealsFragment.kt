package com.yjkj.chainup.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.bean.NewTransactionData
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.treaty.bean.ContractBean
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.fragment_market_detail_deals.*
import kotlinx.android.synthetic.main.item_recent_deal.view.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer

/**
 * @author Bertking
 * @description 市场详情下的"成交订单"
 * @date 2018-12-13
 */
class MarketDetailDealsFragment : Fragment() {
    var TAG = MarketDetailDealsFragment::class.java.simpleName


    private var viewPager: WrapContentViewPager? = null

    private val mainFontColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.main_font_color)



    // TODO: Rename and change types of parameters
    private var coinMapName: String ?= "BTC/USD"
    var symbol = "btcusd"

    lateinit var currentContract: ContractBean

    /**
     * 成交item集合
     */
    private var tradeDealViewList = arrayListOf<View>()

    /**
     * 成交订单
     */
    private var newTransactions = arrayListOf<NewTransactionData.NewTransactionDetail>()

    private lateinit var mSocketClient: WebSocketClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coinMapName = it.getString(COIN_MAP_NAME)
//             symbol = coinMapName.replace("/","").toLowerCase()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_market_detail_deals, container, false)
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, 1)
        }
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initTradeDealView()
    }

    override fun onResume() {
        super.onResume()
        currentContract = Contract2PublicInfoManager.currentContract(lastSymbol = symbol) ?: return
        symbol = currentContract.symbol?.toLowerCase() ?: return

        val lastSymbol = currentContract.lastSymbol ?: return
        if (symbol == lastSymbol) {
        } else {
            clearTradeDealViewData()
            newTransactions = arrayListOf()
        }
        Log.d(TAG, "=====symbol::$symbol=====" + "lastSymbol:" + currentContract.lastSymbol)
        initSocket()
    }

    companion object {
        private const val COIN_MAP_NAME = "coinMapName"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MarketDetailDealsFragment.
         */
        @JvmStatic
        fun newInstance(coinMapName: String = "", viewPager: WrapContentViewPager) =
                MarketDetailDealsFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putString(COIN_MAP_NAME, coinMapName)
                    }
                }
    }


    /**
     * 初始化"成交"部分View
     */
    private fun initTradeDealView() {
        for (i in 0 until 20) {
            val view: View = layoutInflater.inflate(R.layout.item_recent_deal, null)
            ll_deal.addView(view)
            tradeDealViewList.add(view)
        }
    }


    private fun clearTradeDealViewData() {
        for (i in 0 until 20) {
            tradeDealViewList[i].tv_time.text = "--"
            tradeDealViewList[i].tv_time.textColor = mainFontColor
            tradeDealViewList[i].tv_price.text = "--"
            tradeDealViewList[i].tv_price.textColor = mainFontColor
            tradeDealViewList[i].tv_quantity.text = "--"
            tradeDealViewList[i].tv_quantity.textColor = mainFontColor
            tradeDealViewList[i].iv_price_trend.visibility = View.GONE
        }
    }


    /**
     * 更新"成交"部分
     */
    private fun refreshDealView(datas: List<NewTransactionData.NewTransactionDetail>) {
        Log.d(TAG, "===========name == " + coinMapName)
//        if (TextUtils.isEmpty(coinMapName) || (null!=coinMapName && !coinMapName!!.contains("/"))) {
//            tv_price_title?.text = StringUtils.getString(R.string.contract_text_price)
//            tv_price?.text = StringUtils.getString(R.string.contract_text_price)
//
//            tv_quantity_title?.text = StringUtils.getString(R.string.charge_text_volume)
//            tv_buy_volume?.text = StringUtils.getString(R.string.charge_text_volume)
//            tv_sell_volume?.text = StringUtils.getString(R.string.charge_text_volume)
//        } else {
//            if(null!=coinMapName){
//                tv_price_title?.text = StringUtils.getString(R.string.contract_text_price) + "(${coinMapName!!.split("/")[1]})"
//                tv_price?.text = tv_price_title.text
//                tv_quantity_title?.text = StringUtils.getString(R.string.charge_text_volume) + "(${coinMapName!!.split("/")[0]})"
//                tv_buy_volume?.text = tv_quantity_title.text
//                tv_sell_volume?.text = tv_quantity_title.text
//            }
//        }



        newTransactions.addAll(0, datas)
        if (newTransactions.size > 20) {
            newTransactions = ArrayList(newTransactions.subList(0, 20))
        }
        if (newTransactions.isEmpty()) return


        doAsync {
            uiThread {
                handlePriceTrend(newTransactions)
            }
        }

        newTransactions.indices.forEach {
            val view = tradeDealViewList[it]
            /**
             * 买卖方向
             */
            if (newTransactions[it].side == "BUY") {
                view.tv_orientation?.text = StringUtils.getString(R.string.contract_action_buy)
                /**
                 * 时间
                 */
                view.tv_time.text = newTransactions[it].ds.substring(11) + " B"
                val green = ContextCompat.getColor(ChainUpApp.appContext, R.color.green)

                view.tv_orientation?.setTextColor(green)
                view.tv_price?.setTextColor(green)
                view.tv_time?.setTextColor(green)
                view.tv_quantity?.setTextColor(green)
            } else {
                /**
                 * 时间
                 */
                view.tv_time.text = newTransactions[it].ds.substring(11) + " S"
                view.tv_orientation?.text = StringUtils.getString(R.string.contract_action_sell)
                val red = ContextCompat.getColor(ChainUpApp.appContext, R.color.red)
                view.tv_orientation?.setTextColor(red)
                view.tv_price?.setTextColor(red)
                view.tv_time?.setTextColor(red)
                view.tv_quantity?.setTextColor(red)
            }


            /**
             * 数量 （合约的数量都是整数）
             */
            view.tv_quantity?.text = newTransactions[it].vol
        }

    }


    /**
     * 处理合约最新成交的价格对比
     *
     */
    private fun handlePriceTrend(newTransactions: List<NewTransactionData.NewTransactionDetail>) {
        var lashIndex = newTransactions.lastIndex
        for (it in newTransactions.indices.reversed()) {
            val view = tradeDealViewList[it]
            //            newTransactions.size - 1
            /**
             * TODO 优化
             * 价格
             */
            val price = newTransactions[it].price
            val compareTo = BigDecimalUtils.compareTo(price, newTransactions[lashIndex].price)
            lashIndex = it
            view.tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(price)
            when (compareTo) {
                1 -> {
                    view.iv_price_trend.visibility = View.VISIBLE
                    view.iv_price_trend?.setImageResource(R.drawable.ic_price_ascend)
                }

                -1 -> {
                    view.iv_price_trend.visibility = View.VISIBLE
                    view.iv_price_trend?.setImageResource(R.drawable.ic_price_descend)
                }

                else -> {
                    view.iv_price_trend.visibility = View.GONE
                }
            }
        }
    }


    /**
     * 处理数据
     */
    fun handleData(data: String) {
        Log.d(TAG, "=======历史成交：=======" + data)
        var json: JSONObject? = null
        try {
            json = JSONObject(data)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (!json?.isNull("data")!!) {
            if (json.getString("channel").contains("trade")) {
                /**
                 * 请求(req) ----> 历史成交量
                 * 即：下方的最新成交的列表的历史数据
                 * channel ---> "channel": "market_ltcusdt_trade_ticker
                 */
                doAsync {
                    var datas = JsonUtils.jsonToList(json.optJSONArray("data").toString(), NewTransactionData
                            .NewTransactionDetail::class.java)

                    uiThread {
                        refreshDealView(datas)
                    }
                }
                /**
                 * 订阅 "实时成交" 的数据
                 */
                val lastSymbol = currentContract.lastSymbol ?: return
                if (symbol == lastSymbol) {
                } else {
                    sendMessage(WsLinkUtils.getDealNewLink(lastSymbol, false).json)
                    sendMessage(WsLinkUtils.getDealNewLink(symbol).json)
                }
            }
        }

        /**
         * 深度
         * 最新成交
         * 最新K线
         * 都有tick，需根据channel字段来判断
         */
        if (!json.isNull("tick")) {
            /**
             * 最新成交
             */
            if (json.getString("channel") == WsLinkUtils.getDealNewLink(symbol).channel) {
                Log.i("最新成交的==", json.toString())
                doAsync {
                    val newTransactionData: NewTransactionData? = JsonUtils.jsonToBean(json.toString(), NewTransactionData::class.java)
                    if (newTransactionData != null) {
                        uiThread {
                            refreshDealView(newTransactionData.tick.data)
                        }
                    }
                }
            }
        }


    }


    private fun initSocket() {
        mSocketClient = object : WebSocketClient(URI(ApiConstants.SOCKET_CONTRACT_ADDRESS)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                //成交历史
                sendMessage(WsLinkUtils.getDealHistoryLink(symbol).json)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
            }

            override fun onMessage(bytes: ByteBuffer?) {
                super.onMessage(bytes)
                if (bytes == null) return
                val data = GZIPUtils.uncompressToString(bytes.array())
                if (!data.isNullOrBlank()) {
                    if (data.contains("ping")) {
                        val replace = data.replace("ping", "pong")
                        Log.d(TAG, "=====lllllllllllll=======" + replace)
                        mSocketClient.send(replace)
                    } else {
                        handleData(data)
                    }
                }
            }

            override fun onMessage(message: String?) {
                Log.i("test", "onMessage")
            }

            override fun onError(ex: Exception?) {
                Log.i("test", "onError" + ex?.printStackTrace())
            }

        }
        mSocketClient.connect()
    }

    /**
     * Ws 发送消息
     */
    private fun sendMessage(msg: String) {
        Log.i(TAG, msg)

        if (!::mSocketClient.isInitialized || !mSocketClient.isOpen) {
            return
        }

        Thread(Runnable {
            mSocketClient.send(msg)
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearTradeDealViewData()
    }
}
