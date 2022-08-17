package com.yjkj.chainup.new_contract.fragment

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.SymbolWsData
import com.yjkj.chainup.new_contract.adapter.ClContractDropAdapter
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.ws.WsAgentManager
import com.yjkj.chainup.ws.WsContractAgentManager
import kotlinx.android.synthetic.main.fragment_market_detail.*
import kotlinx.android.synthetic.main.fragment_sl_search_coin.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ClCoinSearchItemFragment : NBaseFragment(), WsContractAgentManager.WsResultCallback {
    ///  (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
    private var index = 0
    private var contractDropAdapter: ClContractDropAdapter? = null
    private var tickers: ArrayList<JSONObject> = ArrayList()
    private val localTickers: ArrayList<JSONObject> = ArrayList()

    private lateinit var mContractList: JSONArray
    private var contractListJson: String? = null
    override fun setContentView(): Int {
        return R.layout.fragment_sl_search_coin
    }

    override fun initView() {


        contractDropAdapter = ClContractDropAdapter(tickers)
        rv_search_coin.layoutManager = LinearLayoutManager(context)
        rv_search_coin.adapter = contractDropAdapter
        contractDropAdapter?.setEmptyView(R.layout.item_new_empty)
        rv_search_coin.adapter = contractDropAdapter
        NLiveDataUtil.observeData(this, androidx.lifecycle.Observer {
            val content = it?.msg_content
            if (content is String) {
                if (StringUtil.checkStr(content)) {
                    tickers.clear()
                    for (index in localTickers.indices) {
                        if (localTickers[index].optString("symbol").contains(content.toUpperCase())) {
                            tickers.add(localTickers[index])
                        }
                    }
                    contractDropAdapter?.notifyDataSetChanged()
                } else {
                    tickers.clear()
                    tickers.addAll(localTickers)
                    contractDropAdapter?.notifyDataSetChanged()
                }
            }
        })
    }

    override fun loadData() {
        super.loadData()

        index = arguments!!.getInt(ParamConstant.CUR_INDEX)
//        contractListJson = arguments!!.getString("contractList")
        try {
            contractListJson= LogicContractSetting.getContractJsonListStr(mActivity)
            mContractList = JSONArray(contractListJson)
            updateData()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun updateData() {
        //index 1:usdt 0:反向 2：混合 3：模拟
        for (i in 0..(mContractList.length() - 1)) {
            var obj: JSONObject = mContractList.get(i) as JSONObject
            var contractSide = obj.getInt("contractSide")
            val contractType = mContractList.getJSONObject(i).getString("contractType")
            if (index == 1 && contractSide == 1 && contractType.equals("E")) {
                tickers.add(obj)
                localTickers.add(obj)
            } else if (index == 0 && contractSide == 0 && contractType.equals("E")) {
                tickers.add(obj)
                localTickers.add(obj)
            } else if (index == 2 && !contractType.equals("E")&& !contractType.equals("S")) {
                tickers.add(obj)
                localTickers.add(obj)
            } else if (index == 3 && contractType.equals("S")) {
                tickers.add(obj)
                localTickers.add(obj)
            }
            tickers.sortBy { it.getInt("sort") }
            localTickers.sortBy { it.getInt("sort") }
        }
        contractDropAdapter?.notifyDataSetChanged()
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
//        if (tickers.size!=0) {
//            val arrays = arrayOfNulls<String>(tickers.size)
//            for (i in tickers.indices) {
//                var obj: JSONObject = tickers.get(i)
//                var currentSymbolBuff = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
//                arrays.set(i, currentSymbolBuff)
//            }
//            if (isVisibleToUser) {
//                LogUtil.e(TAG, "index:" + arguments!!.getInt(ParamConstant.CUR_INDEX).toString())
//                WsContractAgentManager.instance.sendMessage(hashMapOf("bind" to true, "symbols" to JsonUtils.gson.toJson(arrays)), this)
//            } else {
//                WsContractAgentManager.instance.sendMessage(hashMapOf("bind" to false, "symbols" to JsonUtils.gson.toJson(arrays)), this)
//            }
//        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
    }

    companion object {
        @JvmStatic
        fun newInstance(index: Int, contractListJson: String): ClCoinSearchItemFragment {
            val fg = ClCoinSearchItemFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, index)
            bundle.putString("contractList", contractListJson)
            fg.arguments = bundle
            return fg
        }
    }

    override fun onWsMessage(json: String) {
        handleData(json)
    }

    fun handleData(data: String) {
        Log.d(TAG, "侧边栏涨跌副:$data")
        try {
            val json = JSONObject(data)
            if (!json.isNull("tick")) {
                doAsync {
                    val quotesData = json
                    showWsData(quotesData)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showWsData(jsonObject: JSONObject) {
        if (null == tickers)
            return
        LogUtil.e(TAG, "index1:" + arguments!!.getInt(ParamConstant.CUR_INDEX).toString())
        LogUtil.e(TAG, "tickers:" + tickers.size)
        val obj = SymbolWsData().getNewSymbolObjContract(tickers, jsonObject)
        val layoutManager = rv_search_coin?.layoutManager as LinearLayoutManager
        val firstView = layoutManager.findFirstVisibleItemPosition()
        val lastItem = layoutManager.findLastVisibleItemPosition()
        if (null != obj && obj.length() > 0) {
            val pos = tickers.indexOf(obj)
            if (pos >= 0) {
                val isRange = (firstView..lastItem).contains(pos)
                if (!isRange) return
                activity?.runOnUiThread {
                    contractDropAdapter?.notifyItemChanged(pos, null)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_contract_sidebar_market_event -> {
                showWsData(event.msg_content as JSONObject)
            }
        }
    }

}