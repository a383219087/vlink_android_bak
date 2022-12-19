package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager

import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_asset.*
import org.json.JSONObject


//资产
open class AssetFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_asset

    val assetlist = ArrayList<JSONObject>()
    var fragments = ArrayList<Fragment>()

    val showTitles = arrayListOf<String>()
    var tabTitles = arrayListOf<String>()


    var openContract = 0






    override fun initView() {
        init()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun init() { //如果合约ID传0则获取默认的数据，此处主要就是获取是否开通合约
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(getContractModel().getUserConfig("0", consumer = object : NDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data").run { //  1已开通, 0未开通
                    openContract = optInt("openContract")
                    showTitles.clear()
                    tabTitles.clear()
                    fragments.clear()
                    showTitles.add(LanguageUtil.getString(context, "trade_all_titile"))
                    showTitles.add(LanguageUtil.getString(context, "trade_bb_titile"))
                    if (openContract == 1) {
                        showTitles.add(LanguageUtil.getString(context, "mainTab_text_contract"))
                    }
                    showTitles.add(LanguageUtil.getString(context, "mainTab_text_otc"))
                    tabTitles.add(LanguageUtil.getString(context, "assets_text_exchange"))
                    tabTitles.add(LanguageUtil.getString(context, "assets_text_exchange"))
                    if (openContract == 1) {
                        tabTitles.add(LanguageUtil.getString(context, "assets_text_contract"))
                    }
                    val otcText = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                        LanguageUtil.getString(context, "assets_text_otc_forotc")
                    } else {
                        LanguageUtil.getString(context, "assets_text_otc")
                    }
                    tabTitles.add(otcText)
                    fragments.add(NewVersionMyAssetFragment.newInstance(openContract))
                    fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[1], ParamConstant.BIBI_INDEX))
                    if (openContract == 1) {
                        fragments.add(ClContractAssetFragment())
                        fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[3], ParamConstant.FABI_INDEX))
                    } else {
                        fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[2], ParamConstant.FABI_INDEX))
                    }

                    val marketPageAdapter = NVPagerAdapter(childFragmentManager, tabTitles.toMutableList(), fragments)
                    vp_otc_asset?.adapter = marketPageAdapter
                    vp_otc_asset?.setCurrentItem(0,true)
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
                    } catch (e: Exception) {

                    }
                }

            }
        }))


    }
}



