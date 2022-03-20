package com.yjkj.chainup.update;

public interface ApkDownloadListener {

    void onStartDownload();

    void onProgress(int progress);

    void onFinishDownload();

    void onFail(String errorInfo);
}
