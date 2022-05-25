package com.yjkj.chainup.new_version.activity.oldgrid

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import kotlinx.android.synthetic.main.activity_grid_execution_details.*
import kotlinx.android.synthetic.main.activity_grid_execution_details.fragment_market
import kotlinx.android.synthetic.main.activity_grid_execution_details.stl_grid_list

/**
 * @Author lianshangljl
 * @Date 2021/2/1-5:25 PM
 * @Email buptjinlong@163.com
 * @description
 */
//@Route(path = RoutePath.GridExecutionDetailsActivity)
class OldGridExecutionDetailsActivity : NBaseActivity() {

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


    var titles = arrayListOf<String>()
    val fragments = arrayListOf<Fragment>()


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        tv_title?.text = "${NCoinManager.getShowMarketName(symbol)} ${LanguageUtil.getString(this, "grid_execution_details")}"
        iv_back?.setOnClickListener { finish() }
    }

    override fun initView() {
        super.initView()
        titles.clear()
        titles.add(LanguageUtil.getString(this, "quant_ordering"))
        titles.add(LanguageUtil.getString(this, "quant_has_been_performed"))

        fragments.clear()

        fragments.add(OldBeingGridFragment.newInstance(strategyId, coin))
        fragments.add(OldAlreadyPerformedGridFragment.newInstance(strategyId, coin))

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


}