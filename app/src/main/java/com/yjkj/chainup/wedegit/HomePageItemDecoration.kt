package com.yjkj.chainup.wedegit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.yjkj.chainup.R

class HomePageItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    var mPaint: Paint = Paint()
    var dividerHeight: Int = 0

    init {
        mPaint.color = ContextCompat.getColor(context, R.color.colorDivider)
        dividerHeight = context.resources.getDimensionPixelSize(R.dimen.line_height)
    }


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val childCount = parent.childCount
        val top = parent.top+(parent.context.resources.getDimensionPixelSize(R.dimen.dp_16)*2)
        val bottom = parent.bottom-parent.context.resources.getDimensionPixelSize(R.dimen.dp_16)
        for (i in 0 until childCount-1){
            val view: View = parent.getChildAt(i)
            val rectRight = view.right + parent.context.resources.getDimensionPixelSize(R.dimen.dp_1)
            c.drawRect(view.right.toFloat(),top.toFloat(), rectRight.toFloat(),bottom.toFloat(),mPaint)
        }
    }

}