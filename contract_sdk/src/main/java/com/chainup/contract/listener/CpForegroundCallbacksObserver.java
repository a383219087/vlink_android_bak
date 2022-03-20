package com.chainup.contract.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lianshangljl
 * @Date 2020-02-24-11:45
 * @Email buptjinlong@163.com
 * @description
 */
public class CpForegroundCallbacksObserver {
    private List<CpForegroundCallbacksListener> listeners;

    public CpForegroundCallbacksObserver() {
        listeners = new ArrayList<>();
    }

    private static CpForegroundCallbacksObserver foregroundCallbacksObserver;

    public static CpForegroundCallbacksObserver getInstance() {
        if (null == foregroundCallbacksObserver) {
            foregroundCallbacksObserver = new CpForegroundCallbacksObserver();
        }
        return foregroundCallbacksObserver;
    }


    public synchronized void addListener(CpForegroundCallbacksListener listener) {
        if (null == listener || listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void ForegroundListener() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).ForegroundListener();
        }
    }

    public void CallBacksListener() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).BackgroundListener();
        }
    }

    public void BackAppListener(boolean visiable) {
        if (listeners.size() == 0) {
            return;
        }
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).appBackChange(visiable);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }


    public void removeListener(CpForegroundCallbacksListener listener) {
        if (null == listener || !listeners.contains(listener)) {
            return;
        }
        listeners.remove(listener);
    }
}
