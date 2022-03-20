package com.yjkj.chainup.app;

import android.app.Application;
import android.content.Context;

/**
 * @author：admin on 2017/4/15 15:26.
 *
 */

public class GlobalAppComponent {

    public static boolean hasEnterLogin = false;//标记是否已进入登录页面
    public static boolean isAutoForwardLogin = true;//标记是否已进入登录页面


    /**
     * 初始化全局AppComponent
     * @param context applicationContext
     */
    private static Application mContext;
    public static void init(Application context){
        mContext = context;
    }

    public static Context getContext(){
        return mContext;
    }

}
