package com.yjkj.chainup.new_version.home

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.impl.ContractTickerListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.activity.SlContractKlineActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.new_version.adapter.NewContractDropAdapter
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.fragment_new_home_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONArray
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2020-09-20-16:54
 * @Email buptjinlong@163.com
 * @description
 */
class NewSlCoinSearchItemFragment : NBaseFragment() {
    /// 0 USDT 1币本位 2模拟
    private var index = 0
    private var coinlist = ""
    private var contractDropAdapter: NewContractDropAdapter? = null
    private val tickers: ArrayList<ContractTicker> = ArrayList()
    private val localTickers: ArrayList<ContractTicker> = ArrayList()
    override fun setContentView(): Int {
        return R.layout.fragment_new_home_detail
    }

    override fun initView() {
        index = arguments!!.getInt(ParamConstant.CUR_INDEX)
        coinlist = arguments?.getString(ParamConstant.CUR_HOME_COINS) ?: ""
        updateData()
        if (coinlist.isNotEmpty()) {
            var jsonArray = JSONArray(coinlist)
            var jsonlist = JSONUtil.arrayToList(jsonArray)
            if (jsonlist.isNotEmpty()) {
                tickers.clear()
                for (i in jsonlist.indices) {
                    val json = jsonlist[i]
                    var conractTicker = ContractTicker()
                    conractTicker.instrument_id = json?.optInt("instrument_id") ?: 0
                    conractTicker.high = json?.optString("highest")
                    conractTicker.low = json?.optString("lowest")
                    conractTicker.close = json?.optString("price")
                    conractTicker.change_value = json?.optString("change_value")
                    conractTicker.change_rate = json?.optString("change_rate")
                    conractTicker.qty24 = json?.optString("24h_trades_vol")
                    tickers.add(conractTicker)
                }
            }
            initAdapter()
        }
        if (viewPager != null && null != root_ll) {
            viewPager?.setObjectForPosition(root_ll, index)
        }
    }


    fun initAdapter() {
        contractDropAdapter = NewContractDropAdapter(tickers)
        rv_market_detail.layoutManager = LinearLayoutManager(context)
        contractDropAdapter?.setEmptyView(R.layout.item_new_empty)
        rv_market_detail.adapter = contractDropAdapter

        contractDropAdapter?.setOnItemClickListener { adapter, view, position ->
            val ticker = adapter.data[position] as ContractTicker
            if (!Utils.isFastClick()) {
                SlContractKlineActivity.show(mActivity!!, ticker?.instrument_id)
            }
        }
    }

    private fun updateData() {
        val data: List<ContractTicker> = ContractPublicDataAgent.getContractTickers()
        if (data != null) {
            for (i in data.indices) {
                var item = data[i]
                Log.e("jinlong", "item:$item")
                when (index) {
                    0 -> {
                        if (item.block == Contract.CONTRACT_BLOCK_USDT) {
                            tickers.add(item)
                            localTickers.add(item)
                        }
                    }
                    1 -> {
                        if (item.block == Contract.CONTRACT_BLOCK_MAIN || item.block == Contract.CONTRACT_BLOCK_INNOVATION) {
                            tickers.add(item)
                            localTickers.add(item)
                        }
                    }
                    2 -> {
                        if (item.block == Contract.CONTRACT_BLOCK_SIMULATION) {
                            tickers.add(item)
                            localTickers.add(item)
                        }
                    }
                }

            }
        }
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            override fun onWsContractTicker(ticker: ContractTicker) {
                if (isHidden || !isVisible || !isResumed) {
                    return
                }
                handDataContract(ticker)
            }
        })
        // LogUtil.d("DEBUG","侧边栏:${tickers.size}---$index")
    }

    fun handDataContract(ticker: ContractTicker) {
        doAsync {
            for (i in tickers?.indices!!) {
                if (tickers?.get(i)?.instrument_id == ticker.instrument_id) {
                    Log.e("shengong", "ticker:$ticker")
                    tickers[i] = ticker
                    runOnUiThread {
                        contractDropAdapter?.notifyItemChanged(i, 0)
                    }
                    break
                }
            }

        }
    }


    private var viewPager: WrapContentViewPager? = null

    companion object {
        @JvmStatic
        fun newInstance(index: Int, viewPager: WrapContentViewPager, coins: String?) =
                NewSlCoinSearchItemFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putInt(ParamConstant.CUR_INDEX, index)
                        putString(ParamConstant.CUR_HOME_COINS, coins)
                    }

                }
    }
}