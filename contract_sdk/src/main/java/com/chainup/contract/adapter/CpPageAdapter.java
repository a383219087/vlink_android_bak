package com.chainup.contract.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Bertking
 * @Date：2019-08-22-14:36
 * @Description:
 */
public class CpPageAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PageAdapter";
    private ArrayList<String> titles;
    private List<Fragment> mFragments;
    public CpPageAdapter(FragmentManager fm, ArrayList<String> titles, List<Fragment> fragments) {
        super(fm);
        this.titles = titles;
        this.mFragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return null == mFragments ? 0 : mFragments.size();
    }

    /*@Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(null==titles || titles.isEmpty()){
            return super.getPageTitle(position);
        }
        return titles.get(position);
    }*/

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
