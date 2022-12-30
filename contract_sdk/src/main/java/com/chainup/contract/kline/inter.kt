package com.chainup.contract.kline

interface ViewCallBack<T> {
    fun onCallback(t: T)
}