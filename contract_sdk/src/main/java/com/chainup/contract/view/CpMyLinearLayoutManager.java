package com.chainup.contract.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CpMyLinearLayoutManager extends LinearLayoutManager {
    public CpMyLinearLayoutManager(Context context) {
        super( context );
    }

    public CpMyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super( context, orientation, reverseLayout );
    }

    public CpMyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super( context, attrs, defStyleAttr, defStyleRes );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren( recycler, state );
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }
}