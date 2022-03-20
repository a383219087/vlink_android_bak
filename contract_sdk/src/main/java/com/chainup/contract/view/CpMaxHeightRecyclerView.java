package com.chainup.contract.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.chainup.contract.R;


public class CpMaxHeightRecyclerView extends RecyclerView {
    private int mMaxHeight;

    public CpMaxHeightRecyclerView(Context context) {
        super(context);
    }

    public CpMaxHeightRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public CpMaxHeightRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CpMaxHeightRecyclerView);
        mMaxHeight = arr.getLayoutDimension(R.styleable.CpMaxHeightRecyclerView_maxHeight, mMaxHeight);
        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
