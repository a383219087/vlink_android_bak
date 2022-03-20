package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.View
import androidx.lifecycle.Observer
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.adapter.PageAdapter
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_new_version_market.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * @Author lianshangljl
 * @Date 2019/3/15-4:10 PM
 * @Email buptjinlong@163.com
 * @description  新版本行情页面
 */
class NewVersionMarketFragment : NBaseFragment() {


    override fun setContentView(): Int {
        return R.layout.fragment_new_version_market
    }

    var adapterScroll = true
    override fun initView() {
        NLiveDataUtil.observeData(this, Observer<MessageEvent> {
            if (null != it) {
                if (MessageEvent.home_event_page_market_type == it.msg_type) {
                    LogUtil.v(TAG, "MessageEvent 处理market ${it}")
                    if (fragments.size == 0) {
                        showVP()
                    }
                }
            }
        })
        showVP()
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
                    vp_market?.currentItem = i
                }
            }
        }
    }

    val fragments = arrayListOf<Fragment>()
    private fun showVP() {
        var marketSort = PublicInfoDataService.getInstance().getMarketSort(null)
        LogUtil.d(TAG, "marketSort:$marketSort")

        if (null == marketSort || marketSort.length() <= 0)
            return
        fragments.clear()
        val titles = arrayListOf<String>()
        val showTitles = arrayListOf<String>()

        for (i in 0 until marketSort.length()) {
            var name = marketSort.optString(i)
            titles.add(name)
            showTitles.add(NCoinManager.getShowMarket(name))

            var marketTrendFragment = MarketTrendFragment()
            var bundle = Bundle()
            bundle.putString("market_name", name)
            bundle.putInt("cur_index", i + 1)
            marketTrendFragment.arguments = bundle

            fragments.add(marketTrendFragment)
        }
        LogUtil.e("initMakert", "initMakert ${marketSort}")
        vp_market?.adapter = PageAdapter(childFragmentManager, titles, fragments)
        var limitSize = fragments?.size
        vp_market?.offscreenPageLimit = limitSize

        if (showTitles.size > 5) {
            stl_market_loop?.setViewPager(vp_market, showTitles.toTypedArray())
            stl_market_loop.visibility = View.VISIBLE
        } else {
            stl_market?.setViewPager(vp_market, showTitles.toTypedArray())
            stl_market.visibility = View.VISIBLE
        }
        vp_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewpagePosotion = position
                clickTabItem()
            }

        })
        clickTabItem()
    }

    var viewpagePosotion = 0


    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
    }

    override fun background() {
        super.background()
        LogUtil.e(TAG, "fragmentVisibile==NewVersionMarketFragment== background ")
    }

    override fun foreground() {
        super.foreground()
        LogUtil.e(TAG, "fragmentVisibile==NewVersionMarketFragment== foreground ")
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        LogUtil.e(TAG, "onVisibleChanged==NewVersionMarketFragment ${isVisible} ")
        sendWS(isVisible)
    }

    private fun sendWS(isVisibleToUser: Boolean) {
        var mainActivity = activity
        if (mainActivity is NewMainActivity) {
            LogUtil.d(TAG, "fragmentVisibile==NewVersionMarketFragment== ${mainActivity.curPosition}  isVisible is $isVisible  isVisibleToUser ${isVisibleToUser}")
            if (fragments.size <= viewpagePosotion) return
            if (isVisibleToUser) {
                clickTabItem()
            } else {
                // unbind
            }
        }
    }


    fun handleData(json: String) {
        LogUtil.d(TAG, "==NewVersionMarketFragment==handleData ${fragments.size} isVisibleToUser ${json}")
        if (json.contains("review")) {
            val count = fragments.size - 1
            for (index in 0..count) {
                val temp = fragments[index]
                if (temp is MarketTrendFragment) {
                    temp.handleData(json)
                }
            }
        } else {
            var fragment = fragments[viewpagePosotion]
            if (fragment is MarketTrendFragment) {
                fragment.handleData(json)
            }
        }
    }

    fun clickTabItem() {
        if (fragments.size == 0) {
            return
        }
        var fragment = fragments[viewpagePosotion]
        if (fragment is MarketTrendFragment) {
            fragment.startInit()
        }
    }

    fun getCoins(): MutableList<JSONObject>? {
        val fragment = fragments[viewpagePosotion]
        if (fragment is MarketTrendFragment) {
            return fragment.adapter?.data
        }
        return arrayListOf()
    }


    fun handleData(items: HashMap<String, JSONObject>) {
        val fragment = fragments[viewpagePosotion]
        if (fragment is MarketTrendFragment) {
            fragment.dropListsAdapter(items)
        }
    }

}