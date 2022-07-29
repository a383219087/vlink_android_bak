package com.yjkj.chainup.wedegit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.yjkj.chainup.new_version.activity.NewMainActivity;
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment;
import com.yjkj.chainup.new_version.fragment.LikesFragment;
import com.yjkj.chainup.new_version.fragment.MarketFragment;
import com.yjkj.chainup.new_version.fragment.MarketTrendFragment;
import com.yjkj.chainup.new_version.fragment.NCVCTradeFragment;
import com.yjkj.chainup.new_version.fragment.NewVersionMarketFragment;
import com.yjkj.chainup.new_version.home.NewHomeDetailFragment;
import com.yjkj.chainup.ui.home.NewVersionHomepageFragment;
import com.yjkj.chainup.ws.WsAgentManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author lianshangljl
 * @Date 2020-02-24-10:38
 * @Email buptjinlong@163.com
 * @description
 */
public class ForegroundCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final long CHECK_DELAY = 600;
    private static ForegroundCallbacks instance;
    private boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Runnable check;

    private final static String ACTIVITY_HOME = NewVersionHomepageFragment.class.getSimpleName();
    public final static String ACTIVITY_MARKET = NewVersionMarketFragment.class.getSimpleName();
    private final static String ACTIVITY_TRADE_DEFAULT = TradeFragment.class.getSimpleName();
    private final static String ACTIVITY_NCVC_DEFAULT = NCVCTradeFragment.class.getSimpleName();
    public final static String ACTIVITY_MARKET_TAB = MarketTrendFragment.class.getSimpleName();
    public final static String ACTIVITY_MARKET_HOME_TAB = MarketFragment.class.getSimpleName();
    public final static String ACTIVITY_MARKET_LIKE_TAB = LikesFragment.class.getSimpleName();
    public final static String ACTIVITY_HOME_TAB = NewHomeDetailFragment.class.getSimpleName();

    private static String wsLastWindow = "";

    public static ForegroundCallbacks init(Application application) {
        if (instance == null) {
                wsLastWindow = ACTIVITY_HOME;

            instance = new ForegroundCallbacks();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static ForegroundCallbacks get(Application application) {
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static ForegroundCallbacks get(Context ctx) {
        if (instance == null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application) appCtx);
            }
            throw new IllegalStateException("Foreground is not initialised and " + "cannot obtain the Application object");
        }
        return instance;
    }

    public static ForegroundCallbacks get() {
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
        if (activity instanceof NewMainActivity) {
            wsBackgroup(false);
            for (Listener l : listeners) {
                try {
                    l.onBackChange(true);
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
        if (activity instanceof NewMainActivity) {
            wsBackgroup(true);
            for (Listener l : listeners) {
                try {
                    l.onBackChange(false);
                } catch (Exception exc) {
                    Log.i("ForegroundCallbacks", exc.getMessage());
                }
            }
        }
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
            if (activity instanceof NewMainActivity) {
                wsBackgroup(false);
            }
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
            wsBackgroup(true);
        }
    }

    private HashMap<String, Boolean> sendArrays = new HashMap<String, Boolean>();

    private void wsBackgroup(boolean unBind) {
        String tempName = wsLastWindow;
        if (wsLastWindow.equals(ACTIVITY_MARKET_TAB) || wsLastWindow.equals(ACTIVITY_MARKET_LIKE_TAB) || wsLastWindow.equals(ACTIVITY_MARKET)) {
            tempName = ACTIVITY_MARKET_HOME_TAB;
        }
        if (wsLastWindow.equals(ACTIVITY_HOME_TAB)) {
                tempName = ACTIVITY_HOME;

        }
        if (wsLastWindow.equals(ACTIVITY_TRADE_DEFAULT)) {
            tempName = ACTIVITY_NCVC_DEFAULT;
        }
            WsAgentManager.Companion.getInstance().wsBackgroupChange(tempName, unBind);
    }

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
            wsLastWindow = fragment.getClass().getSimpleName();
            Log.e("ForegroundCallbacks", "fragment " + wsLastWindow + " ");
        }
    }

}
