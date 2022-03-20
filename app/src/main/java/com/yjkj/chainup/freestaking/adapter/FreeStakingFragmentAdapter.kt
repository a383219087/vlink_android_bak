package com.yjkj.chainup.freestaking.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FreeStakingFragmentAdapter(fm: FragmentManager, private var tabList: List<String>, private var fragmentList: List<Fragment>) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return if (fragmentList.isEmpty()) 0 else fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position]
    }

}

