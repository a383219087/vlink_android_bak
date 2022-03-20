package com.yjkj.chainup.new_version.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author bertking
 */
public class NVPagerAdapter extends FragmentPagerAdapter {

    List<String> titles;
    List<Fragment> fragments;
    public NVPagerAdapter(FragmentManager fm, List<String> titles, List<Fragment> fragments) {
        super(fm);
        this.titles = titles;
        this.fragments = fragments;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(null==titles || titles.isEmpty()){
            return super.getPageTitle(position);
        }
        return titles.get(position);
    }


    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }


}
