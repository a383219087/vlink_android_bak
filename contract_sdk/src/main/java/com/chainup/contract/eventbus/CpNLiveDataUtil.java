package com.chainup.contract.eventbus;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * @Description:
 * @Author: wanghao
 * @CreateDate: 2019-10-14 18:31
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-10-14 18:31
 * @UpdateRemark: 更新说明
 */
public class CpNLiveDataUtil {

    private static MutableLiveData<CpMessageEvent> liveData = null;

    private static MutableLiveData<CpMessageEvent> getLiveData() {
        if (null == liveData) {
            liveData = new MutableLiveData<CpMessageEvent>();
        }
        return liveData;
    }

    public static void postValue(CpMessageEvent value) {
        getLiveData().postValue(value);
    }

    public static void setValue(CpMessageEvent value) {
        getLiveData().setValue(value);
    }

    public static void observeData(@NonNull LifecycleOwner owner, @NonNull Observer<CpMessageEvent> observer) {  //
        getLiveData().observe(owner, observer);
    }

    public static void observeForeverData(@NonNull Observer<CpMessageEvent> observer) {  //
        getLiveData().observeForever(observer);
    }

    /*
     * 类型事件处理完后需要调用此方法，防止事件再次触发
     */
    public static void removeObservers() {
        liveData = null;
    }

    public static void removeEvent(int eventType) {

    }

}
