package com.chainup.contract.eventbus;


import com.chainup.contract.app.CpAppConfig;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wanghao on 2018/5/3.
 */

public class CpEventBusUtil {

    public static void throwSubscriberException(){
        EventBus.builder().throwSubscriberException(CpAppConfig.IS_DEBUG);
    }

    /*
     * 注册订阅者
     */
    public static void register(Object obj){
        if(!EventBus.getDefault().isRegistered(obj)){
            EventBus.getDefault().register(obj);
        }
    }

    /*
     * 注销订阅者
     */
    public static void unregister(Object obj){
        EventBus.getDefault().unregister(obj);
    }

    /*
     *  发布普通事件
     */
    public static void post(CpMessageEvent event){
        EventBus.getDefault().post(event);
    }

    /*
     *  发布粘性事件
     */
    public static void postSticky(CpMessageEvent event){
        CpMessageEvent messageEvent = EventBus.getDefault().getStickyEvent(CpMessageEvent.class);
        if(null!=messageEvent){
            EventBus.getDefault().removeStickyEvent(event);
        }
        EventBus.getDefault().postSticky(event);
    }

    /*
     *  移除粘性事件
     */
    public static void removeStickyEvent(CpMessageEvent event){
        if(null == event){
            return;
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    public static void removeAllStickyEvents(){
        EventBus.getDefault().removeAllStickyEvents();
    }
}
