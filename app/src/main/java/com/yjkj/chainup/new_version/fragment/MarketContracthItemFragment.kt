package com.yjkj.chainup.new_version.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.eventbus.CpNLiveDataUtil
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpStringUtil
import com.chainup.contract.utils.CpSymbolWsData
import com.chainup.contract.view.CpSearchCoinEmptyForAdapterView
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.adapter.MarketContractDropAdapter
import kotlinx.android.synthetic.main.fragment_sl_contra_child.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MarketContracthItemFragment : CpNBaseFragment(){
    ///  (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
    private var index = 0
    private var contractDropAdapter: MarketContractDropAdapter? = null
    private var tickers: ArrayList<JSONObject> = ArrayList()
    private val localTickers: ArrayList<JSONObject> = ArrayList()

    private lateinit var mContractList: JSONArray
    private var contractListJson: String? = null
    override fun setContentView(): Int {
        return R.layout.fragment_sl_contra_child
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView() {
        contractDropAdapter = MarketContractDropAdapter(tickers)
        rv_search_coin.layoutManager = LinearLayoutManager(context)
        rv_search_coin.adapter = contractDropAdapter
        contractDropAdapter?.setEmptyView(CpSearchCoinEmptyForAdapterView(context ?: return))
        rv_search_coin.adapter = contractDropAdapter
        CpNLiveDataUtil.observeData(this) {
            val content = it?.msg_content
            if (content is String) {
                if (CpStringUtil.checkStr(content)) {
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
        }
    }

    override fun loadData() {
        super.loadData()

        index = arguments!!.getInt(CpParamConstant.CUR_INDEX)
        try {
            contractListJson=CpClLogicContractSetting.getContractJsonListStr(activity)
            mContractList = JSONArray(contractListJson)
            updateData()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateData() {
        //index 1:usdt 0:反向 2：混合 3：模拟
        for (i in 0 until mContractList.length()) {
            var obj: JSONObject = mContractList.get(i) as JSONObject
            var contractSide = obj.getInt("contractSide")
            val contractType = mContractList.getJSONObject(i).getString("contractType")
            //classification
            //E,USDT合约 2,币本位合约 H,混合合约 S,模拟合约
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





    companion object {
        @JvmStatic
        fun newInstance(index: Int, contractListJson: String): MarketContracthItemFragment {
            val fg = MarketContracthItemFragment()
            val bundle = Bundle()
            bundle.putInt(CpParamConstant.CUR_INDEX, index)
            bundle.putString("contractList", contractListJson)
            fg.arguments = bundle
            return fg
        }
    }


    private fun showWsData(jsonObject: JSONObject) {
        val obj = CpSymbolWsData().getNewSymbolObjContract(tickers, jsonObject)
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
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_sidebar_market_event -> {
                showWsData(event.msg_content as JSONObject)
            }
        }
    }

}