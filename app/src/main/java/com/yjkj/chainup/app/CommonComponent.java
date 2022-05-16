package com.yjkj.chainup.app;

import android.app.Application;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.yjkj.chainup.BuildConfig;
import com.yjkj.chainup.R;
import com.yjkj.chainup.extra_service.arouter.ArouterUtil;
import com.yjkj.chainup.util.ContextUtil;
import com.yjkj.chainup.util.DateUtils;
import com.yjkj.chainup.util.LogUtil;

import cn.ljuns.logcollector.LogNetCollector;

//import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * @Description: 项目公共组件服务的初始化操作
 * @Author: wanghao
 * @CreateDate: 2019-08-08 11:27
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-08-08 11:27
 * @UpdateRemark: 更新说明
 */
public class CommonComponent {

    private static CommonComponent mCommonComponent;

    private CommonComponent() {

    }

    public static CommonComponent getInstance() {
        if (null == mCommonComponent) {
            mCommonComponent = new CommonComponent();
        }
        return mCommonComponent;
    }


    public void init(Application context) {
        GlobalAppComponent.init(context);
        LogUtil.e(this.getClass().getSimpleName(),"init start");
        MMKV.initialize(context);
        LogUtil.e(this.getClass().getSimpleName(),"init MMKV");
        ArouterUtil.init(context);
        LogUtil.e(this.getClass().getSimpleName(),"init ArouterUtil");
        if (AppConfig.isBuglyOpen) {
            CrashReport.UserStrategy user = new CrashReport.UserStrategy(context);
            user.setAppReportDelay(10000);
            Bugly.init(context, ContextUtil.getString(R.string.buglyId), BuildConfig.DEBUG,user);
        }

//        if (AppConfig.isFirebaseAnalyticsOpen) {
//            FirebaseAnalytics.getInstance(context);
//        }
        String time = DateUtils.Companion.getYearLongDayMS();
        LogNetCollector.getInstance(context)
                .setCleanCache(false)
                .start(time);
        LogUtil.e(this.getClass().getSimpleName(),"init end");


    }
}
