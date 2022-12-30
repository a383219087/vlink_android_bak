package com.chainup.contract.kline1.data

import android.database.DataSetObservable
import android.database.DataSetObserver

/**
 * @Author: Bertking
 * @Date：2019/3/14-2:11 PM
 * @Description:
 */

abstract class CpBaseKLineChartAdapter : CpIAdapter {
    private val mDataSetObservable = DataSetObservable()

    override fun notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged()
        } else {
            mDataSetObservable.notifyInvalidated()
        }
    }


    override fun registerDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.unregisterObserver(observer)
    }
}