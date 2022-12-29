package com.chainup.contract.eventbus;

import org.json.JSONObject;

/**
 * k线图自选相关操作
 */
public class CpCollectionEvent2 {

    public int type;
    public JSONObject jsonObject;

    public CpCollectionEvent2(int type, JSONObject jsonObject) {
        this.type = type;
        this.jsonObject = jsonObject;
    }
}
