package com.yjkj.chainup.new_version.home

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.ui.NewMainActivity
import com.yjkj.chainup.new_version.adapter.NewHomepageBottomClinchDealAdapter
import com.yjkj.chainup.new_version.adapter.NewHomepageMarketAdapter
import com.yjkj.chainup.new_version.home.callback.EmployeeDiffCallback
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.fragment_new_home_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/11/9-4:40 PM
 * @Email buptjinlong@163.com
 * @description 首页行情详细页面 rasing,falling,deal
 */
class NewHomeDetailFragmentItem : NBaseFragment() {

    /**
     * 底部行情
     */
    private var bottomMarketAdapter: NewHomepageMarketAdapter? = null
    /**
     * 底部行情 成交榜
     */
    private var bottomDealAdapter: NewHomepageBottomClinchDealAdapter? = null

    private var tradeType = ""
    private var marketName = ""
    private var viewPager: WrapContentViewPager? = null
    private var curIndex = 0
    private var coins: JSONArray? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Int, chooseType: String, viewPager: WrapContentViewPager, coins: String?) =
                NewHomeDetailFragmentItem().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putString(ParamConstant.MARKET_NAME, param1)
                        putString(ParamConstant.TYPE, chooseType)
                        putInt(ParamConstant.CUR_INDEX, param2)
                        putString(ParamConstant.CUR_HOME_COINS, coins)
                    }
                }
    }

    override fun setContentView(): Int = R.layout.fragment_new_home_detail

    override fun initView() {
        initParams()
        rv_market_detail?.apply {
            itemAnimator?.moveDuration = 0
            if (itemAnimator is SimpleItemAnimator) {
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
        }
        NLiveDataUtil.observeData(this, Observer<MessageEvent> {
            if (null != it) {
                if (MessageEvent.color_rise_fall_type == it.msg_type) {
                    bottomDealAdapter?.notifyDataSetChanged()
                    bottomMarketAdapter?.notifyDataSetChanged()
                }
            }
        })
        setTextConetnt()
    }

    fun setTextConetnt() {
        tv_home_action_coinNameTitle?.text = LanguageUtil.getString(context, "home_action_coinNameTitle")
    }

    private var curShowData: ArrayList<JSONObject>? = null
    fun initV(data: JSONArray?) {
        var tempData: JSONArray? = null
        if (data == null) {
            return
        } else {
            tempData = data
        }
        var dataList = JSONUtil.arrayToList(tempData)
        if (null == dataList || dataList.size <= 0)
            return
        if (dataList.size > 10) {
            dataList = ArrayList(dataList.subList(0, 10))
        }
        /**
         * 成交榜
         */
        if ("deal" == tradeType) {
            tv_24h_title?.text = LanguageUtil.getString(context, "home_text_deal24hour") + "(BTC)"
            tv_new_price_title?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice") + "(${RateManager.getCurrencyLang()})"
            if (null == bottomDealAdapter) {
                bottomDealAdapter = NewHomepageBottomClinchDealAdapter()
                rv_market_detail?.adapter = bottomDealAdapter
                rv_market_detail?.layoutManager = LinearLayoutManager(context)
                rv_market_detail?.isNestedScrollingEnabled = false
                bottomDealAdapter?.setEmptyView(EmptyForAdapterView(context ?: return))
            }
            if (null == curShowData) {
                bottomDealAdapter?.setList(dataList)
            } else {
                bottomDealAdapter?.replaceData(dataList)
            }
            curShowData = dataList

            /**
             * 跳转至 交易详情界面
             */
            bottomDealAdapter?.setOnItemClickListener { adapter, view, position ->
                /**
                 * Tick(amount='39.96450966', vol='17.56774781', high='2.30000000', low='2.18970000', rose=0.0, close='2.30000000', open='2.30000000', name='BCH/BTC', symbol='bchbtc')
                 */
                var dataList = bottomDealAdapter?.data
                LogUtil.d("bottomDealAdapter", "dataList is $dataList")

                if (null != dataList && dataList!!.size >= 0) {
                    var symbol = dataList!![position].optString("symbol")

                    symbol = NCoinManager.getSymbol(symbol)
                    ArouterUtil.forwardKLine(symbol)
                }
            }

        } else {
            tv_24h_title?.text = LanguageUtil.getString(context, "common_text_priceLimit")
            tv_new_price_title?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice")
            var temp: ArrayList<JSONObject>? = NCoinManager.getSymbols(tempData)
            if (null == temp || temp.size <= 0)
                return

            if (null == bottomMarketAdapter) {
                bottomMarketAdapter = NewHomepageMarketAdapter()
                rv_market_detail?.adapter = bottomMarketAdapter
                rv_market_detail?.layoutManager = LinearLayoutManager(context)
                rv_market_detail?.isNestedScrollingEnabled = false
                bottomMarketAdapter?.setEmptyView(EmptyForAdapterView(context ?: return))
                if (null == curShowData) {
                    bottomMarketAdapter?.setList(temp)
                } else {
                    bottomMarketAdapter?.replaceData(temp)
                }
            } else {
                val diffCallback = EmployeeDiffCallback(bottomMarketAdapter?.data!!, temp)
                bottomMarketAdapter?.setDiffData(diffCallback)
            }

            curShowData = temp
            // 保证http数据 晚到达
            val isMain = isMaineTabSort()
            LogUtil.d(TAG, "isMaineTabSort is  ${curIndex}  ${marketName} $isMain")
            if (isMaineTabSort()) {
                startInit()
            }
            /**
             * 跳转至 交易详情界面
             */
            bottomMarketAdapter?.setOnItemClickListener { adapter, view, position ->
                var dataList = bottomMarketAdapter?.data
                LogUtil.d("bottomMarketAdapter", "dataList is $dataList")
                if (null != dataList && dataList!!.size >= 0) {
                    var symbol = dataList!![position].optString("symbol")
                    ArouterUtil.forwardKLine(symbol)
                }

            }
        }
    }

    private fun initParams() {
        arguments?.let {
            marketName = it.getString(ParamConstant.MARKET_NAME) ?: ""
            tradeType = it.getString(ParamConstant.TYPE) ?: ""
            curIndex = it.getInt(ParamConstant.CUR_INDEX)
            coins = JSONArray(it.getString(ParamConstant.CUR_HOME_COINS))
        }
        initV(coins)
        if (viewPager != null && null != root_ll) {
            viewPager?.setObjectForPosition(root_ll, curIndex)
        }
    }

    fun startInit() {
        if (curIndex == 2) {
            return
        }
        pageEventSymbol()
    }

    private fun pageEventSymbol() {
        if (bottomMarketAdapter?.data != null) {
            val data = bottomMarketAdapter?.data
            if (data.isNullOrEmpty()) {
                return
            }
            val arrays = arrayOfNulls<String>(data.size)
            for ((index, item) in data.withIndex()) {
                arrays.set(index, item.getString("symbol"))
            }
            forwardMarketTab(arrays)
        } else {
        }
    }

    private fun forwardMarketTab(coin: Array<String?>) {
        val messageEvent = MessageEvent(MessageEvent.home_event_page_symbol_type)
        messageEvent.msg_content = hashMapOf("symbols" to coin, "curIndex" to curIndex)
        EventBusUtil.post(messageEvent)
    }


    private fun isMaineTabSort(): Boolean {
        if (activity is NewMainActivity) {
            return (activity as NewMainActivity).curPosition == 0
        }
        return false
    }
    private val wsArrayTempList: ArrayList<JSONObject> = arrayListOf()
    private val wsArrayMap = hashMapOf<String, JSONObject>()
    private var wsTimeFirst: Long = 0L



    @Synchronized
    fun dropListsAdapter(items: HashMap<String, JSONObject>) {
        val data = bottomMarketAdapter?.data
        if (data?.isEmpty()!!) {
            return
        }
        val message = Gson().toJson(data)
        val jsonCopy = Utils.jsonToArrayList(message, JSONObject::class.java)
        val tempNew = jsonCopy
        for ((index, item) in items.entries) {
            val jsonObject = item
            val channel = jsonObject.optString("channel")
            var tempData = 0
            for ((index, item) in data.withIndex()) {
                if (channel.contains(item.optString("symbol"))) {
                    tempData = index
                    break
                }
            }
            val tick = jsonObject.optJSONObject("tick")

            val item = tempNew.get(tempData)
            item.put("rose", tick.optString("rose"))
            item.put("close", tick.optString("close"))
            item.put("vol", tick.optString("vol"))
            tempNew.set(tempData, item)

        }
        if ("rasing" == tradeType) {
            tempNew.sortByDescending { it.optDouble("rose", 0.0) }
        } else if ("falling" == tradeType) {
            tempNew.sortBy { it.optDouble("rose", 0.0) }
        }
        for ((index, item) in tempNew.withIndex()) {
            item.put("Index", "|${(index + 1)}")
            item.put("homeIndex", "${(index + 1)}")
        }
        val diffCallback = EmployeeDiffCallback(data, tempNew)
        activity?.runOnUiThread {
            bottomMarketAdapter?.setDiffData(diffCallback)
        }

    }


}