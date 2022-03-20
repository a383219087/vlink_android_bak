package com.chainup.contract.utils;

import android.widget.TextView;

public class CpChainUtil {


    /*
     * 首页资产数据显示与隐藏控制
     */
    public static void assetsHideShow(boolean isShow, TextView textView, String content) {
        if (isShow) {
            if (CpStringUtil.checkStr(content)) {
                textView.setText(content + "");
            } else {
                textView.setText("0");
            }
        } else {
            textView.setText("*****");
        }
    }

    /**
     * 用于判断是否快速点击
     *
     * @return
     */
    private static final int FAST_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;


    public synchronized static boolean isFastClick() {
        boolean flag = false;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) <= FAST_CLICK_DELAY_TIME) {
            return true;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

}
