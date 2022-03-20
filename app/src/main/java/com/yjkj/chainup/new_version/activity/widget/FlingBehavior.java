package com.yjkj.chainup.new_version.activity.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;
import java.lang.reflect.Field;

public class FlingBehavior extends AppBarLayout.Behavior {
    private static final int TOP_CHILD_FLING_THRESHOLD = 3;
    private boolean isPositive;

    public FlingBehavior() {
    }

    public FlingBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int[] iArr, int i3) {
        if (i3 == 1 && getTopAndBottomOffset() == 0) {
            ViewCompat.stopNestedScroll(view, i3);
        }
        super.onNestedPreScroll(coordinatorLayout, appBarLayout, view, i, i2, iArr, i3);
    }

    public boolean onInterceptTouchEvent(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            stopScrollerAnimation();
        }
        return super.onInterceptTouchEvent(coordinatorLayout, appBarLayout, motionEvent);
    }

    public void stopScrollerAnimation() {
        try {
            Field scrollerField = getScrollerField();
            scrollerField.setAccessible(true);
            Object obj = scrollerField.get(this);
            if (obj instanceof OverScroller) {
                ((OverScroller) obj).abortAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Field getScrollerField() throws Exception {
        try {
            return getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("scroller");
        } catch (NoSuchFieldException unused) {
            return getClass().getSuperclass().getSuperclass().getDeclaredField("mScroller");
        }
    }
}