package com.chainup.contract.kline1

interface ViewCallBack<T> {
    fun onCallback(t: T)
}