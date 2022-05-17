package com.yjkj.chainup.net;

/**
 * Created by zl on 2020/4/22.
 */
public class AppException extends RuntimeException {
    private final String mCode;
    private final String message;

    public AppException(String code, String msg) {
        super(msg);
        this.mCode = code;
        this.message = msg;
    }

    public String getCode() {
        return mCode;
    }

    public String getMessage() {
        return message;
    }

}
