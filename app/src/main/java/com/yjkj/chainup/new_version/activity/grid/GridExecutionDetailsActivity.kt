package com.yjkj.chainup.new_version.activity.grid

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.util.getSymbolByMarketName
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.activity_grid_execution_details.*
import kotlinx.android.synthetic.main.activity_grid_execution_details.fragment_market
import kotlinx.android.synthetic.main.activity_grid_execution_details.stl_grid_list
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2021/2/1-5:25 PM
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.GridExecutionDetailsActivity)
class
GridExecutionDetailsActivity : NBaseActivity(), WsAgentManager.WsResultCallback {

    override fun setContentView() = R.layout.activity_grid_execution_details

    @JvmField
    @Autowired(name = ParamConstant.GIRD_ID)
    var strategyId = ""

    @JvmField
    @Autowired(name = ParamConstant.GIRD_COIN)
    var coin = ""

    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    @JvmField
    @Autowired(name = ParamConstant.GIRD_COIN_INFO)
    var coinInfo: String = ""


    var titles = arrayListOf<String>()
    val fragments = arrayListOf<Fragment>()


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        WsAgentManager.instance.addWsCallback(this)
        tv_title?.text = "${NCoinManager.getShowMarketName(symbol)} ${LanguageUtil.getString(this, "grid_execution_details")}"
        iv_back?.setOnClickListener { finish() }
    }

    override fun initView() {
        super.initView()
        titles.clear()
        titles.add(LanguageUtil.getString(this, "quant_ordering"))
        titles.add(LanguageUtil.getString(this, "quant_has_been_performed"))

        fragments.clear()
        val coinJson = coinInfo
        fragments.add(BeingGridFragment.newInstance(strategyId, coin, symbol, coinJson))
        fragments.add(AlreadyPerformedGridFragment.newInstance(strategyId, coin, coinJson))

        var pageAdapter = NVPagerAdapter(supportFragmentManager, titles, fragments)

        fragment_market?.adapter = pageAdapter
        fragment_market?.offscreenPageLimit = fragments.size
        fragment_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

            }
        })

        stl_grid_list?.setViewPager(fragment_market, titles.toTypedArray())
    }

    fun updateTitleOrder(num: Int) {
        titles[0] = "${LanguageUtil.getString(this, "quant_ordering")}($num)"
        stl_grid_list?.setViewPager(fragment_market, titles.toTypedArray())
    }

    fun updateTitleHasBeen(num: Int) {
        titles[1] = "${LanguageUtil.getString(this, "quant_has_been_performed")}($num)"
        stl_grid_list?.setViewPager(fragment_market, titles.toTypedArray())
    }

    override fun onWsMessage(json: String) {
        val currentItem = fragment_market.currentItem
        if (currentItem == 0) {
            val fragment = fragments.get(currentItem)
            if (fragment is BeingGridFragment) {
                fragment.tick24H(JSONObject(json))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val isUpdate  = isUpdateIng()
        if(isUpdate){
            Handler().postDelayed({
                val temp = symbol.getSymbolByMarketName()
                WsAgentManager.instance.sendMessage(hashMapOf("symbol" to temp), this)
            }, 200)
        }
    }

    override fun onStop() {
        super.onStop()
        WsAgentManager.instance.unbind(this, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        WsAgentManager.instance.removeWsCallback(this)
    }

    private fun isUpdateIng(): Boolean {
        val info = JSONObject(coinInfo)
        if (info.length() != 0) {
            if (!info.isNull("strategyStatus")) {
                val strategyStatus = info.optString("strategyStatus")
                if (strategyStatus == "0" || strategyStatus == "1") return true
            }
        }
        return false
    }


}