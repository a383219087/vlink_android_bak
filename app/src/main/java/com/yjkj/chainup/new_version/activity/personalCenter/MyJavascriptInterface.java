package com.yjkj.chainup.new_version.activity.personalCenter;

import android.content.Context;
import android.webkit.JavascriptInterface;



public class MyJavascriptInterface {

    private Context context;

    public MyJavascriptInterface(Context context) {
        this.context = context;
    }

    /**
     * 返回主页
     *
     * @param src
     */
    @JavascriptInterface
    public void finishActivity(String src) {
        if (context instanceof UdeskWebViewActivity) {
            ((UdeskWebViewActivity) context).finish();
        }
    }


}
