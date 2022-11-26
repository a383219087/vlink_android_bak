package com.yjkj.chainup.new_version.fragment

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.new_version.bean.FlagBean
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.WsLinkUtils
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.fragment_dealt_record.*
import kotlinx.android.synthetic.main.item_dealt_record.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URI


/**
 * @author Bertking
 * @description 市场详情下的"成交记录"
 * @date 2019-3-19
 * DONE
 */
class DealtRecordFragment : Fragment() {
    val TAG = DealtRecordFragment::class.java.simpleName

    private var viewPager: WrapContentViewPager? = null

    /**
     * 成交订单
     */
    private var newTransactions = arrayListOf<JSONObject>()

    /**
     * 成交item集合
     */
    private var tradeDealViewList = arrayListOf<View>()

    var flagBean: FlagBean? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dealt_record, container, false)
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, 1)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(viewPager: WrapContentViewPager) =
                DealtRecordFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                    }
                }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDealRecordView()

        DepthFragment.liveData.observe(this, Observer {

            if (it == null || flagBean?.symbol == it?.symbol) {
                return@Observer
            }

            if (flagBean?.symbol != it?.symbol) {
                clearDealRecordView()
            }

            //un sub last symbol 's dealt record
            if (flagBean != null) {
                sendMessage(WsLinkUtils.getDealNewLink(flagBean?.symbol ?: "", false).json)
            }

            flagBean = it

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
            tv_amount_title?.text = context?.getString(R.string.charge_text_volume1) + amountUnit

            initSocket(URI(""))

        })
    }

    private fun initSocket(uri: URI?) {
        sendMessage(WsLinkUtils.getDealHistoryLink(flagBean?.symbol ?: "").json)
    }

    /**
     * WebSocket 发送消息
     */
    private fun sendMessage(msg: String) {

    }

    /**
     * 初始化"成交"部分View
     */
    private fun initDealRecordView() {
        for (i in 0 until 20) {
            val view: View = layoutInflater.inflate(R.layout.item_dealt_record, null)
            ll_dealt_record?.addView(view)
            tradeDealViewList.add(view)
        }

    }

    /**
     * 清空"成交"的View
     */
    private fun clearDealRecordView() {
        for (i in 0 until 20) {
            tradeDealViewList[i].run {
                tv_time?.text = "--"
                tv_price?.text = "--"
                tv_amount?.text = "--"
            }
        }
    }

    /**
     * 处理数据
     */
    fun handleData(string: String) {
        try {
            val json = JSONObject(string)

            /**
             * 最新成交
             */
            val channel = json.getString("channel")
            if (!json.isNull("tick")) {
                if (channel == WsLinkUtils.getDealNewLink(flagBean?.symbol ?: "").channel) {
                    parseDealData(json.optJSONObject("tick"))
                }
            }

            /**
             * 请求(req) ----> 历史成交量
             * 即：下方的最新成交的列表的历史数据
             * channel ---> "channel": "market_ltcusdt_trade_ticker
             */
            if (!json.isNull("data")) {
                if (channel == WsLinkUtils.getDealHistoryLink(flagBean?.symbol ?: "").channel) {
                    parseDealData(json)
                    /**
                     * 订阅 "实时成交" 的数据
                     */
                    sendMessage(WsLinkUtils.getDealNewLink(flagBean?.symbol ?: "").json)

                }
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e(TAG, "msg:${e.message}")
        }

    }

    private fun parseDealData(json: JSONObject) {
        doAsync {
            val dataJSONArray = json.optJSONArray("data")
            val list = arrayListOf<JSONObject>()
            for (i in 0 until dataJSONArray.length()) {
                list.add(dataJSONArray.optJSONObject(i))
            }
            uiThread {
                refreshDealRecordView(list)
            }
        }
    }


    /**
     * 更新"成交"部分
     */
    private fun refreshDealRecordView(data: List<JSONObject>) {

        if (data.isEmpty()) {
            return
        }

        newTransactions.addAll(0, data)
        if (newTransactions.size > 20) {
            newTransactions = ArrayList(newTransactions.subList(0, 20))
        }
        if (newTransactions.isEmpty()) return


        newTransactions.indices.forEach {

            val view = tradeDealViewList[it]
            val jsonObject = newTransactions[it]

            val ds = jsonObject.optString("ds")
            val price = jsonObject.optString("price")
            val side = jsonObject.optString("side")
            val vol = jsonObject.optString("vol")


            /**
             * 时间
             */
            view.tv_time?.text = ds.substring(11)
            /**
             * 价格
             */
            view.tv_price?.text =
                    if (flagBean?.isContract == true) {
                        Contract2PublicInfoManager.cutValueByPrecision(price, flagBean?.pricePrecision
                                ?: 0)
                    } else {
                        DecimalUtil.cutValueByPrecision(price, flagBean?.pricePrecision ?: 0)
                    }


            /**
             * 买卖方向
             */
            view.tv_price?.setTextColor(ColorUtil.getMainColorType(side == "BUY"))

            /**
             * 数量
             */
            view.tv_amount?.text =
                    if (flagBean?.isContract == true) {
                        if (!flagBean?.mMultiplier.equals("0")) {
                            if (flagBean?.coUnit == 0) vol else BigDecimalUtils.mulStr(vol, flagBean?.mMultiplier, flagBean?.volumePrecision!!)
                        } else {
                            vol
                        }
                    } else {
                        DecimalUtil.cutValueByPrecision(vol, flagBean?.volumePrecision ?: 0)
                    }
            if (PublicInfoDataService.getInstance().klineThemeMode == 1){
                view.tv_time?.setTextColor(ColorUtil.getColor(R.color.text_color_kline_night))
                view.tv_amount?.setTextColor(ColorUtil.getColor(R.color.text_color_kline_night))
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun onCallback(json: String) {
        handleData(json)
    }

}
