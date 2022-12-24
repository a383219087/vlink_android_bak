package com.yjkj.chainup.new_version.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.eventbus.CpNLiveDataUtil
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpStringUtil
import com.chainup.contract.utils.CpSymbolWsData
import com.chainup.contract.view.CpSearchCoinEmptyForAdapterView
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.MarketContractDetailAdapter
import com.yjkj.chainup.new_version.adapter.MarketContractDropAdapter
import com.yjkj.chainup.util.LanguageUtil
import kotlinx.android.synthetic.main.fragment_sl_contra_child.*
import kotlinx.android.synthetic.main.include_market_sort.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.imageResource
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MarketContracthItemFragment : CpNBaseFragment(){
    ///  (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
    private var index = 0
    /// 0显示在首页1是行情页
    private var type = 0
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
        type = arguments!!.getInt(CpParamConstant.COIN_TYPE)
        rv_search_coin.layoutManager = LinearLayoutManager(context)
        if (type==0){
            rv_search_coin?.isNestedScrollingEnabled = false
            //首页用首页显示的 item
            contractDropAdapter = MarketContractDropAdapter(tickers)
            ll_item_titles.visibility = View.GONE
        }else{
            //行情的合约另一个 item
            contractDropAdapter = MarketContractDetailAdapter(tickers)
            ll_item_titles.visibility = View.VISIBLE
        }
        val animator: ItemAnimator? = rv_search_coin.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }



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
        tv_name?.text = LanguageUtil.getString(context, "home_action_coinNameTitle")
        tv_new_price?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice")
        tv_limit?.text = LanguageUtil.getString(context, "common_text_priceLimit")
        setOnclick()
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
        fun newInstance(index: Int,type: Int, contractListJson: String): MarketContracthItemFragment {
            val fg = MarketContracthItemFragment()
            val bundle = Bundle()
            bundle.putInt(CpParamConstant.CUR_INDEX, index)
            bundle.putInt(CpParamConstant.COIN_TYPE, type)
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


    var nameIndex = 0
    var newPriceIndex = 0
    var limitIndex = 0

    fun setOnclick() {
        /**
         * 点击名称
         */
        ll_name.setOnClickListener {
            refreshTransferImageView(0)
            when (nameIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    tickers.sortBy { CpClLogicContractSetting.getContractShowNameById(context, it.optInt("id")) }
                    nameIndex = 1
                    iv_name_up?.imageResource = R.drawable.quotes_up_daytime

                }
                /**
                 * 正序
                 */
                1 -> {
                    tickers.sortByDescending { CpClLogicContractSetting.getContractShowNameById(context, it.optInt("id")) }
                    nameIndex = 2
                    iv_name_up?.imageResource = R.drawable.quotes_under_daytime

                }
                /**
                 * 倒序
                 */
                2 -> {
                    tickers.clear()
                    tickers.addAll(localTickers)
                    nameIndex = 0
                    iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                }
            }
            if (tickers.size > 0) {
                refreshAdapter(tickers)
            }

        }
        /**
         * 点击最新价
         */
        ll_new_price.setOnClickListener {
            refreshTransferImageView(1)
            when (newPriceIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    tickers.sortBy { it.optDouble("close") }
                    newPriceIndex = 1
                    iv_new_price?.imageResource = R.drawable.quotes_up_daytime

                }
                /**
                 * 正序
                 */
                1 -> {
                    tickers.sortByDescending { it.optDouble("close") }
                    newPriceIndex = 2
                    iv_new_price?.imageResource = R.drawable.quotes_under_daytime
                }
                /**
                 * 倒序
                 */
                2 -> {
                    tickers.clear()
                    tickers.addAll(localTickers)
                    newPriceIndex = 0
                    iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime

                }
            }
            if (tickers.size > 0) {
                refreshAdapter(tickers)
            }
        }
        /**
         * 点击24小时涨幅
         */
        ll_limit.setOnClickListener {
            refreshTransferImageView(2)
            when (limitIndex) {
                /**
                 * 正常
                 */
                0 -> {
                    tickers.sortBy { it.optDouble("rose") }
                    limitIndex = 1
                    iv_new_limit?.imageResource = R.drawable.quotes_up_daytime
                }
                /**
                 * 正序
                 */
                1 -> {
                    tickers.sortByDescending { it.optDouble("rose") }
                    limitIndex = 2
                    iv_new_limit?.imageResource = R.drawable.quotes_under_daytime
                }
                /**
                 * 倒序
                 */
                2 -> {
                    tickers.clear()
                    tickers.addAll(localTickers)
                    limitIndex = 0
                    iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                }
            }
            if (tickers.size > 0) {
                refreshAdapter(tickers)
            }
        }
    }

    private fun refreshAdapter(list: java.util.ArrayList<JSONObject>) {
        contractDropAdapter?.setList(list)
    }


    private fun refreshTransferImageView(status: Int) {
        when (status) {
            0 -> {
                iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                newPriceIndex = 0
                limitIndex = 0
            }
            1 -> {
                iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_limit?.imageResource = R.drawable.quotes_upanddown_default_daytime
                nameIndex = 0
                limitIndex = 0
            }
            2 -> {
                iv_name_up?.imageResource = R.drawable.quotes_upanddown_default_daytime
                iv_new_price?.imageResource = R.drawable.quotes_upanddown_default_daytime
                nameIndex = 0
                newPriceIndex = 0
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