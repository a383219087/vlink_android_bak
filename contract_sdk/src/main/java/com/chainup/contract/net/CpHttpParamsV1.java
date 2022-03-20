package com.chainup.contract.net;

import java.util.TreeMap;

public class CpHttpParamsV1 {

    private TreeMap<String, Object> map;

    /**
     * 有参数时，如需定义长度使用
     * @param mapLength int
     * @return this
     */
    public static CpHttpParamsV1 getInstance(int mapLength){
        return new CpHttpParamsV1(mapLength);
    }

    private CpHttpParamsV1(int mapLength ){
        map = new TreeMap<String, Object>();
        map.put("time", String.valueOf(System.currentTimeMillis()));
    }

    public <T> CpHttpParamsV1 put(String key, T o){
        map.put(key,o == null ? "" : o.toString());
        return this;
    }

    public TreeMap<String, Object> build(){
        return map;
    }

}
