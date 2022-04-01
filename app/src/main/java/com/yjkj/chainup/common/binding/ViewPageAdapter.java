package com.yjkj.chainup.common.binding;


import androidx.databinding.BindingAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yjkj.chainup.common.binding.command.BindingCommand;


/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public class ViewPageAdapter {

    @BindingAdapter(value = {"addOnPageChangeListener"}, requireAll = false)
    public static void addOnPageChangeListener(ViewPager layout, final BindingCommand addOnPageChangeListener) {
        layout.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (addOnPageChangeListener != null) {
                    addOnPageChangeListener.execute(position);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    @BindingAdapter(value = {"select"}, requireAll = false)
    public static void select(ViewPager layout, final int select) {
        layout.setCurrentItem(select);
    }

}
