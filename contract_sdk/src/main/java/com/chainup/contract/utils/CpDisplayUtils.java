package com.chainup.contract.utils;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.chainup.contract.view.CpSnackLayout;

public class CpDisplayUtils {

    public static int[] getWidthHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        return new int[]{width, height};
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
       return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;

    }

    public static void showSnackBar(View mView, String  text,  Boolean isSuc) {
        CpSnackLayout.showSnackBar(mView, text, isSuc,null);
    }

}
