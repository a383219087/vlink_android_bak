package com.yjkj.chainup.new_version.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractPublicDataAgent.getContractTickers
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.impl.ContractTickerListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_version.adapter.PageAdapter
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.getConTractType
import kotlinx.android.synthetic.main.fragment_new_version_market.*


/**
 * @Author lianshangljl
 * @Date 2019/3/15-4:10 PM
 * @Email buptjinlong@163.com
 * @description  新版本行情页面
 */
class MarketContractFragment : NBaseFragment() {


    override fun setContentView(): Int {
        return R.layout.fragment_new_version_market
    }

    var adapterScroll = true
    override fun initView() {
        showVP()
    }

    override fun loadData() {
        super.loadData()
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            /**
             * 合约Ticker更新
             */
            override fun onWsContractTicker(ticker: ContractTicker) {
                LogUtil.e(TAG, "onWsContractTicker ticker")
                if (isHidden || !isVisible || !isResumed) {
                    return
                }
                if (fragments.size != 0) {
                    val fragment = fragments[viewpagePosotion]
                    if (fragment is MarketSLTrendFragment) {
                        fragment.handleData(ticker)
                    }
                }
            }
        })
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.market_switch_type == event.msg_type) {
            var coin = event.msg_content as String
            var marketSort = PublicInfoDataService.getInstance().getMarketSort(null)
            if (null == marketSort || marketSort.length() <= 0)
                return
            for (i in 0 until marketSort.length()) {
                if (coin == marketSort.optString(i)) {
                    vp_market?.currentItem = i + 1
                }
            }
        }
    }

    val fragments = arrayListOf<Fragment>()
    private fun showVP() {
        val contractTickers: List<ContractTicker> = getContractTickers()
        LogUtil.d(TAG, "contractTickers :${contractTickers.size}")
        val titles = arrayListOf<String>()
        val tickers = contractTickers.getConTractType(context ?: null!!)
        if (contractTickers.isEmpty())
            return
        fragments.clear()
        titles.addAll(tickers.first)
        fragments.addAll(tickers.second)
        vp_market?.adapter = PageAdapter(childFragmentManager, titles, fragments)
        if (titles.size > 5) {
            stl_market_loop?.setViewPager(vp_market, titles.toTypedArray())
            stl_market_loop.visibility = View.VISIBLE
        } else {
            stl_market?.setViewPager(vp_market, titles.toTypedArray())
            stl_market.visibility = View.VISIBLE
        }
        vp_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewpagePosotion = position
            }

        })
    }

    var viewpagePosotion = 0


    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
    }


    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        LogUtil.e(TAG, "onVisibleChanged==NewVersionMarketFragment ${isVisible} ")
    }




}