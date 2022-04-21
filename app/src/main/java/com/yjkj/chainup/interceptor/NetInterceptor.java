package com.yjkj.chainup.interceptor;

import android.app.Application;
import android.content.Intent;

import com.chainup.talkingdata.AppAnalyticsExt;
import com.elvishew.xlog.XLog;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.db.service.UserDataService;
import com.yjkj.chainup.manager.NetworkLineErrorService;
import com.yjkj.chainup.model.model.MainModel;
import com.yjkj.chainup.util.JsonUtils;
import com.yjkj.chainup.net.api.HttpResult;
import com.yjkj.chainup.net_new.NetUrl;
import com.yjkj.chainup.util.DateUtils;
import com.yjkj.chainup.util.LogUtil;
import com.yjkj.chainup.util.SystemUtils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/*
 * 请求拦截器
 */
public class NetInterceptor implements Interceptor {

    private static final String TAG = "NetInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originReq = chain.request();
        String oriUrl = originReq.url().toString();

        if (oriUrl.contains(NetUrl.biki_monitor_appUrl)) {
            Request.Builder builder = originReq.newBuilder();
            originReq = builder.url(NetUrl.biki_monitor_appUrl).build();
        }

        originReq = getBuilderHeader(originReq.newBuilder()).build();

        String neworiUrl = originReq.url().toString();
        LogUtil.d("我是LogUtil", "NetInterceptor==neworiUrl is " + neworiUrl);


        Response response = chain.proceed(originReq);
        StringBuffer string = new StringBuffer("code [%s] url %s  (%sms)  [%s - %s] ");
        long start = response.sentRequestAtMillis();
        long end = response.receivedResponseAtMillis();
        long time = end - start;
        String startTime = DateUtils.Companion.getLogTimeMS(start);
        String endTime = DateUtils.Companion.getLogTimeMS(end);
        String printTime = String.format(string.toString(), response.code(), neworiUrl, time, startTime, endTime);
        LogUtil.e(TAG, printTime);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("url", neworiUrl);
        LogUtil.e(TAG, "NetInterceptor==response is " + response);
        AppAnalyticsExt.Companion.getInstance().network(AppAnalyticsExt.Companion.getAPP_HTTP_httpTrack(), map);
        if (response.code() == 200) {
            if (time >= 400) {
                XLog.e(printTime);
                AppAnalyticsExt.Companion.getInstance().network(AppAnalyticsExt.Companion.getAPP_HTTP_httpTrackLow(), map);
            }
            String json = readResponseStr(response);
            if (json != null) {
                HttpResult result = JsonUtils.INSTANCE.jsonToBean(json, HttpResult.class);
                if (result != null) {
                    String code = result.getCode();
                    Boolean login = !code.equals("10021") && !code.equals("10002") && !code.equals("104008") && !code.equals("3") && !code.equals("0");
                    Boolean otc = !code.equals("2001") && !code.equals("2056") && !code.equals("2069") && !code.equals("2074") && !code.equals("2055");
                    if (login && otc) {
                        String print = printTime + "[chainUP:code] " + code;
                        XLog.e(print);
                        HashMap<String, Object> errorMap = new HashMap<String, Object>();
                        errorMap.put("error", neworiUrl + code + " " + result.getMsg());
                        AppAnalyticsExt.Companion.getInstance().network(AppAnalyticsExt.Companion.getAPP_HTTP_httpError(), errorMap);
                    }
                    if (code.equals("10055")) {
                        new MainModel().savePublicInfo();
                    }
                }
            }
        } else if (response.code() != 200) {
            XLog.e(printTime);
            if (!neworiUrl.contains("health_check")) {
                LogUtil.e(TAG, "网络错误 需要切换网络 ");
                if (!UserDataService.getInstance().isNetworkCheckIng()) {
                    UserDataService.getInstance().setNetworkCheckIng(true);
                    Application context = ChainUpApp.app;
                    Intent intent = new Intent(context, NetworkLineErrorService.class);
                    context.startService(intent);
                }
            }
        }
        return response;
    }

    private Request.Builder getBuilderHeader(Request.Builder builder) {
        HashMap<String, String> headers = SystemUtils.getHeaderParams();
        for (String key : headers.keySet()) {
            String value = headers.get(key);
            if (value != null && !value.isEmpty())
                builder.addHeader(key, value);
        }
        return builder;
    }

    /**
     * 读取Response返回String内容
     *
     * @param response
     * @return
     */
    private String readResponseStr(Response response) {
        ResponseBody body = response.body();
        BufferedSource source = body.source();
        try {
            source.request(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        MediaType contentType = body.contentType();
        Charset charset = Charset.forName("UTF-8");
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        String s = null;
        Buffer buffer = source.buffer();
        if (isPlaintext(buffer)) {
            s = buffer.clone().readString(charset);
        }
        return s;
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
