package com.yjkj.chainup.wedegit;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @Author: Bertking
 * @Date：2019/1/28-11:51 AM
 * @Description:
 */
public class ForbidScrollViewPager extends ViewPager {
    private boolean scrollable = false;
    public ForbidScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public ForbidScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return scrollable;
    }

}
