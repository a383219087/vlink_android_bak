package com.yjkj.chainup.wedegit.item

import android.graphics.Rect

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.wedegit.DisplayUtils


class SpacesItemDecoration(private var spaceTemp: Float = 16f) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        val space = DisplayUtils.dip2px(ChainUpApp.appContext, spaceTemp)
        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) outRect.top = space
    }

}