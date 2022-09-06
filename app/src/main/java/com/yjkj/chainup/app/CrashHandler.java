package com.yjkj.chainup.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.yjkj.chainup.ui.NewMainActivity;
import com.yjkj.chainup.util.ToastUtils;

/**
 * Created by Bertking on 2018/7/14.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getSimpleName();

    private static volatile CrashHandler instance = null;

    private Context mContext;

    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;


    private CrashHandler() {

    }


    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;

        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        // 设置该 CrashHandler 为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当 UncaughtException 发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //延迟2秒杀进程
        try {
            Thread.sleep(2000);
            ToastUtils.showToast("程序出错了~");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

        Intent intent = new Intent(mContext, NewMainActivity.class);
        PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        //退出程序
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); // 1秒钟后重启应用


    }

}
