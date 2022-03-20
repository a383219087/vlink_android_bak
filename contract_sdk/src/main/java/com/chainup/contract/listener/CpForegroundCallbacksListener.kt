package com.chainup.contract.listener

/**
 * @Author lianshangljl
 * @Date 2020-02-24-11:46
 * @Email buptjinlong@163.com
 * @description
 */
interface CpForegroundCallbacksListener {
    fun ForegroundListener()
    fun BackgroundListener()
    fun appBackChange(visible: Boolean)
}