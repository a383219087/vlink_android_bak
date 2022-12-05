package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.fragment.ClContractAssetFragment
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_asset.*
import org.json.JSONObject


//资产
open class AssetFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_asset

    val assetlist = ArrayList<JSONObject>()
    var fragments = ArrayList<Fragment>()

    val showTitles = arrayListOf<String>()
    var indexList = ArrayList<String>()
    var tabTitles = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        showTitles.add(LanguageUtil.getString(context, "trade_all_titile"))
        showTitles.add(LanguageUtil.getString(context, "trade_bb_titile"))
        showTitles.add(LanguageUtil.getString(context, "mainTab_text_contract"))
        showTitles.add(LanguageUtil.getString(context, "mainTab_text_otc"))
        indexList.add(ParamConstant.All_INDEX)
        indexList.add(ParamConstant.BIBI_INDEX)
        indexList.add(ParamConstant.CONTRACT_INDEX)
        indexList.add(ParamConstant.FABI_INDEX)
        tabTitles.add(LanguageUtil.getString(context, "assets_text_exchange"))
        tabTitles.add(LanguageUtil.getString(context, "assets_text_exchange"))
        tabTitles.add(LanguageUtil.getString(context, "assets_text_contract"))
        val otcText = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(context, "assets_text_otc_forotc")
        } else {
            LanguageUtil.getString(context, "assets_text_otc")
        }
        tabTitles.add(otcText)
        fragments.add(NewVersionMyAssetFragment())
        fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[1],  indexList[1]))
        fragments.add( ClContractAssetFragment())
        fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[3], indexList[3]))
        val marketPageAdapter = NVPagerAdapter(childFragmentManager, tabTitles.toMutableList(), fragments)
        vp_otc_asset?.adapter = marketPageAdapter
        vp_otc_asset?.offscreenPageLimit = tabTitles.size
        vp_otc_asset?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
            }
        })
        try {
            stl_assets_type.setViewPager(vp_otc_asset, showTitles.toTypedArray())
        }catch(e :Exception){

        }
    }
}



