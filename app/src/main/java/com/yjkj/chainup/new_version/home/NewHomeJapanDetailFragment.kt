package com.yjkj.chainup.new_version.home

import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import androidx.lifecycle.Observer
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NewHomepageBottomClinchDealAdapter
import com.yjkj.chainup.new_version.adapter.NewHomepageMarketAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.wedegit.WrapContentViewPager
import kotlinx.android.synthetic.main.fragment_new_home_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-01-15-17:22
 * @Email buptjinlong@163.com
 * @description
 */
class NewHomeJapanDetailFragment : NBaseFragment(), NewHomeDetailWsData.RefreshWSListener {

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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Int, chooseType: String, viewPager: WrapContentViewPager) =
                NewHomeJapanDetailFragment().apply {
                    this.viewPager = viewPager
                    arguments = Bundle().apply {
                        putString(ParamConstant.MARKET_NAME, param1)
                        putString(ParamConstant.TYPE, chooseType)
                        putInt(ParamConstant.CUR_INDEX, param2)
                    }
                }
    }

    override fun setContentView(): Int = R.layout.fragment_new_home_detail

    override fun initView() {
        initParams()
        getdata()

        NLiveDataUtil.observeData(this, Observer<MessageEvent> {
            if (null != it) {
                if (MessageEvent.color_rise_fall_type == it.msg_type) {
                    bottomDealAdapter?.notifyDataSetChanged()
                    bottomMarketAdapter?.notifyDataSetChanged()
                }
            }
        })


    }

    private var curShowData: ArrayList<JSONObject>? = null
    private fun initV(data: JSONArray?) {

        LogUtil.d(TAG, "initV==tradeType is $tradeType,data is $data")
        var dataList = JSONUtil.arrayToList(data)
        if (null == dataList || dataList.size <= 0)
            return

        if (dataList.size > 10) {
            dataList = ArrayList(dataList.subList(0, 10))
        }

        /**
         * 成交榜
         */
        if ("deal".equals(tradeType)) {
            tv_24h_title?.text = LanguageUtil.getString(context, "home_text_deal24hour") + "(BTC)"
            tv_new_price_title?.text = LanguageUtil.getString(context, "home_text_dealLatestPrice") + "(${RateManager.getCurrencyLang()})"

            if (null == bottomDealAdapter) {
                bottomDealAdapter = NewHomepageBottomClinchDealAdapter()
                rv_market_detail?.adapter = bottomDealAdapter
                rv_market_detail?.layoutManager = LinearLayoutManager(context)
                rv_market_detail?.isNestedScrollingEnabled = false
                bottomDealAdapter?.setEmptyView(EmptyForAdapterView(context ?: return))
            }
            bottomDealAdapter?.setList(dataList)
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
            var temp: ArrayList<JSONObject>? = NCoinManager.getSymbols(data)
            if (null == temp || temp.size <= 0)
                return

            if (null == bottomMarketAdapter) {
                bottomMarketAdapter = NewHomepageMarketAdapter()
                rv_market_detail?.adapter = bottomMarketAdapter
                rv_market_detail?.layoutManager = LinearLayoutManager(context)
                rv_market_detail?.isNestedScrollingEnabled = false
                bottomMarketAdapter?.setEmptyView(EmptyForAdapterView(context ?: return))

            }
            bottomMarketAdapter?.setList(temp)
            curShowData = temp
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
        }

        if (viewPager != null && null != root_ll) {
            viewPager?.setObjectForPosition(root_ll, curIndex)
        }
    }

    /**
     * 获取数据
     */
    fun getdata() {
        mMarketWsData?.closeWS()
        startReq = 1
        var disposable = getMainModel().trade_list_v4(tradeType, object : NDisposableObserver(null, false) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                LogUtil.d(TAG, "tradeType is $tradeType,jsonObject is $jsonObject")
                isFirstLoad = false
                startReq = 0
                initV(jsonObject.optJSONArray("data"))
                initSocket()
                loopData()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                startReq = 0
                isFirstLoad = false
                initSocket()
                loopData()
            }
        })
        addDisposable(disposable)
    }

    /*
     * 先暂时轮询处理
     */
    private var startReq = 0
    private var mHandler: Handler? = null;
    private fun loopData() {
        LogUtil.d(TAG, "loopData()===111==isFirstLoad is $isFirstLoad,tradeType is $tradeType")
        if (isFirstLoad)
            return

        if (1 == startReq || !userVisibleHint)
            return

        if (null == mHandler)
            mHandler = Handler()
        mHandler!!.postDelayed({
            if (0 == startReq && userVisibleHint) {
                LogUtil.d(TAG, "loopData()===222")
                getdata()
            }
        }, 30000)
    }


    private var mMarketWsData: NewHomeDetailWsData? = null
    private fun initSocket() {
        LogUtil.d(TAG, "tradeType is $tradeType,curShowData is $curShowData")

        if ("deal" == tradeType) {
            return
        }
        if (null == mMarketWsData)
            mMarketWsData = NewHomeDetailWsData()
        mMarketWsData!!.initSocket(curShowData, this)
    }

    /*
     *  rasing,falling,deal
     */
    override fun onRefreshWS(pos: Int) {
        activity?.runOnUiThread {
            bottomMarketAdapter?.notifyItemChanged(pos)
            var data = bottomMarketAdapter?.data
            if ("rasing" == tradeType) {
                data?.sortByDescending { it.optDouble("rose") }
            } else if ("falling" == tradeType) {
                data?.sortBy { it.optDouble("rose") }
            }
            if (null != data) {
                bottomMarketAdapter?.replaceData(data)
            }
        }
    }


    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        LogUtil.d(TAG, "fragmentVisibile==NewHomeDetailFragment==isVisibleToUser is $isVisibleToUser")
        if (isVisibleToUser) {
            if (!isFirstLoad) {
                LogUtil.d(TAG, "fragmentVisibile==NewHomeDetailFragment==loopData==isVisibleToUser is $isVisibleToUser")
                startReq = 0
                loopData()
            }
        } else {
            mMarketWsData?.closeWS()
        }
    }
}