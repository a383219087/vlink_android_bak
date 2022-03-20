package com.minminaya.policy

import android.graphics.Canvas


interface IRoundViewPolicy {
    fun beforeDispatchDraw(canvas: Canvas?)
    fun afterDispatchDraw(canvas: Canvas?)
    fun onLayout(left: Int, top: Int, right: Int, bottom: Int)
    fun setCornerRadius(cornerRadius: Float)
}