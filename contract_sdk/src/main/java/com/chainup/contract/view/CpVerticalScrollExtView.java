package com.chainup.contract.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.widget.NestedScrollView;

public class CpVerticalScrollExtView extends NestedScrollView {
    private float mDownPosX = 0.0f;
    private float mDownPosY = 0.0f;

    public CpVerticalScrollExtView(Context context) {
        super(context);
    }

    public CpVerticalScrollExtView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CpVerticalScrollExtView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mDownPosX = x;
            this.mDownPosY = y;
        } else if (action == 2 && Math.abs(x - this.mDownPosX) > Math.abs(y - this.mDownPosY)) {
            return false;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }
}
