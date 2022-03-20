package com.yjkj.chainup.update;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ApkDownloadInterceptor  implements Interceptor {
    private ApkDownloadListener downloadListener;
    public ApkDownloadInterceptor(ApkDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder().body(
                new ApkDownLoadResponseBody(response.body(), downloadListener)).build();
    }
}
