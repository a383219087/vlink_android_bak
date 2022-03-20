package com.chainup.contract.listener;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author lianshangljl
 * @Date 2020-02-24-10:38
 * @Email buptjinlong@163.com
 * @description
 */
public class CpForegroundCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final long CHECK_DELAY = 600;
    private static CpForegroundCallbacks instance;
    private boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable check;
    public static CpForegroundCallbacks init(Application application) {
        if (instance == null) {
            instance = new CpForegroundCallbacks();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static CpForegroundCallbacks get(Application application) {
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static CpForegroundCallbacks get(Context ctx) {
        if (instance == null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application) appCtx);
            }
            throw new IllegalStateException("Foreground is not initialised and " + "cannot obtain the Application object");
        }
        return instance;
    }

    public static CpForegroundCallbacks get() {
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private int appCount = 0;

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;
        if (check != null)
            handler.removeCallbacks(check);
        if (wasBackground) {
            for (Listener l : listeners) {
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    Log.i("ForegroundCallbacks", exc.getMessage());
                }
            }
        }

        Log.e("ForegroundCallbacks", "onActivityResumed  " + appCount);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;
        if (check != null) handler.removeCallbacks(check);
        handler.postDelayed(check = new Runnable() {
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    for (Listener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.i("ForegroundCallbacks", exc.getMessage());
                        }
                    }
                }
            }
        }, CHECK_DELAY);
        Log.e("ForegroundCallbacks", "onActivityPaused  " + appCount);
    }

    @Override
    public void onActivityCreated(Activity activity, @NonNull Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.e("ForegroundCallbacks", "onActivityStarted  " + appCount);
        if (appCount == 0) {
            // 前台
            Log.e("ForegroundCallbacks", "前台");
//            if (activity instanceof NewMainActivity) {
//                wsBackgroup(false);
//            }
        }
        appCount++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        appCount--;
        Log.e("ForegroundCallbacks", "onActivityStopped  " + appCount);
        if (appCount == 0) {
            // 后台
            Log.e("ForegroundCallbacks", "后台");
        }
    }

    private HashMap<String, Boolean> sendArrays = new HashMap<String, Boolean>();

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public interface Listener {
        public void onBecameForeground();

        public void onBecameBackground();

        public void onBackChange(boolean visiable);
    }

    public void activityChange(Fragment fragment) {
        if (fragment != null) {
        }
    }

}
