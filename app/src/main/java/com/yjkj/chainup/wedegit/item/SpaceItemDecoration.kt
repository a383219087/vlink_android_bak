package com.yjkj.chainup.wedegit.item

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.wedegit.DisplayUtils


class SpaceItemDecoration(private var spaceTemp: Float = 10f) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        val space = DisplayUtils.dip2px(ChainUpApp.appContext, spaceTemp)
        outRect.bottom = space
        outRect.left = space
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) % 3 == 0) outRect.left = 0
    }

}