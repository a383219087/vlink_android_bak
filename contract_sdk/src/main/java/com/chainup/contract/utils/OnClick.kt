package com.chainup.contract.utils

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import java.text.SimpleDateFormat
import java.util.*


inline fun View.setSafeListener(crossinline action:()->Unit){
    var lastClick=0L
    setOnClickListener {
        val gap = System.currentTimeMillis() - lastClick
        lastClick=System.currentTimeMillis()
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

class TimeUtil private constructor() {
    private val formatDate: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val formatDateTime: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    companion object {
        @JvmStatic
        val instance: TimeUtil by lazy {
            TimeUtil()
        }
    }

    private var lastClickTime: Long = 0
    private val CLICK_TIME = 800 //快速点击间隔时间


    /**
     * @Author: hl
     * @Date: created at 2020/3/27 14:39
     * @Description: 是否快速点击判断
     */
    fun isFastDoubleClick(): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - lastClickTime
        if (0 < timeD && timeD < CLICK_TIME) {
            return true
        }
        lastClickTime = time
        return false
    }



}