package com.chainup.contract.utils

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter


inline fun View.setSafeListener(crossinline action:()->Unit){
    var lastClick=0L
    setOnClickListener {
        val gap = System.currentTimeMillis() - lastClick
        lastClick=System.currentTimeMillis()
        ChainUpLogUtil.e("gap:"+gap)
        if(gap<500) return@setOnClickListener
        action.invoke()
    }
}

var _viewClickFlag = false
var _clickRunnable = Runnable { _viewClickFlag = false }
fun View.click(action: (view: View) -> Unit) {
    setOnClickListener {
        if (!_viewClickFlag) {
            _viewClickFlag = true
            action(it)
        }
        removeCallbacks(_clickRunnable)
        postDelayed(_clickRunnable, 500)
    }
}