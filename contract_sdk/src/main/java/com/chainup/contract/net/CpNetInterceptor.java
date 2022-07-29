package com.chainup.contract.net;



import com.chainup.contract.api.CpHttpResult;
import com.chainup.contract.utils.CpDateUtils;
import com.chainup.contract.utils.CpJsonUtils;
import com.chainup.contract.utils.ChainUpLogUtil;
import com.chainup.contract.utils.CpSystemUtils;

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
public class CpNetInterceptor implements Interceptor {

    private static final String TAG = "NetInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originReq = chain.request();
        String oriUrl = originReq.url().toString();
        ChainUpLogUtil.d(TAG, "NetInterceptor==oriUrl is " + oriUrl);


        originReq = getBuilderHeader(originReq.newBuilder()).build();

        String neworiUrl = originReq.url().toString();
        ChainUpLogUtil.d("我是ChainUpLogUtil", "NetInterceptor==neworiUrl is " + neworiUrl);


        Response response = chain.proceed(originReq);
        StringBuffer string = new StringBuffer("code [%s] url %s  (%sms)  [%s - %s] ");
        long start = response.sentRequestAtMillis();
        long end = response.receivedResponseAtMillis();
        long time = end - start;
        String startTime = CpDateUtils.Companion.getLogTimeMS(start);
        String endTime = CpDateUtils.Companion.getLogTimeMS(end);
        String printTime = String.format(string.toString(), response.code(), neworiUrl, time, startTime, endTime);
        ChainUpLogUtil.e(TAG, printTime);
        if (response.code() == 200) {
            if (time >= 400) {
                ChainUpLogUtil.e(printTime);
            }
            String json = readResponseStr(response);
            if (json != null) {
                CpHttpResult result = CpJsonUtils.INSTANCE.jsonToBean(json, CpHttpResult.class);
                if (result != null) {
                    String code = result.getCode();
                    Boolean login = !code.equals("10021") && !code.equals("10002") && !code.equals("104008") && !code.equals("3") && !code.equals("0");
                    Boolean otc = !code.equals("2001") && !code.equals("2056") && !code.equals("2069") && !code.equals("2074") && !code.equals("2055");
                    if (login && otc) {
                        String print = printTime + "[chainUP:code] " + code;
                        ChainUpLogUtil.e(print);
                    }
                }
            }
        } else if (response.code() != 200) {
            ChainUpLogUtil.e(printTime);
        }
        return response;
    }

    private Request.Builder getBuilderHeader(Request.Builder builder) {
        HashMap<String, String> headers = CpSystemUtils.getHeaderParams();
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
