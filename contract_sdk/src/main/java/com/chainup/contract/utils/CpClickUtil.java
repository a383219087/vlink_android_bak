package com.chainup.contract.utils;

import android.view.View;


public final class CpClickUtil {

    /**
     * 最近一次点击的时间
     */
    private static long mLastClickTime;
    /**
     * 最近一次点击的控件ID
     */
    private static int mLastClickViewId;

    /**
     * 是否是快速点击
     *
     * @return  true:是，false:不是
     */
    public static boolean isFastDoubleClick() {
        long intervalMillis=1500;
        long time = System.currentTimeMillis();
        long timeInterval = Math.abs(time - mLastClickTime);
        if (timeInterval < intervalMillis ) {
            return true;
        } else {
            mLastClickTime = time;
            return false;
        }
    }
}