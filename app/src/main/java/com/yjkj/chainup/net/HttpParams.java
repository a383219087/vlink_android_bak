package com.yjkj.chainup.net;

import java.util.TreeMap;

public class HttpParams {

    private TreeMap<String,String> map;

    /**
     * 有参数时，如需定义长度使用
     * @param mapLength int
     * @return this
     */
    public static HttpParams getInstance(int mapLength){
        return new HttpParams(mapLength);
    }

    private HttpParams(int mapLength ){
        map = new TreeMap<String,String>();
        map.put("time", String.valueOf(System.currentTimeMillis()));
    }

    public <T>HttpParams put(String key,T o){
        map.put(key,o == null ? "" : o.toString());
        return this;
    }

    public TreeMap<String,String> build(){
        return map;
    }

}
