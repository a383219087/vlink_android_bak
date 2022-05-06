package com.yjkj.chainup.net;

import java.util.TreeMap;

public class HttpParamsV1 {

    private TreeMap<String, Object> map;

    /**
     * 有参数时，如需定义长度使用
     * @param mapLength int
     * @return this
     */
    public static HttpParamsV1 getInstance(int mapLength){
        return new HttpParamsV1(mapLength);
    }

    private HttpParamsV1(int mapLength ){
        map = new TreeMap<String, Object>();
        map.put("time", String.valueOf(System.currentTimeMillis()));
    }

    public <T> HttpParamsV1 put(String key, T o){
        map.put(key,o == null ? "" : o.toString());
        return this;
    }

    public TreeMap<String, Object> build(){
        return map;
    }

}
