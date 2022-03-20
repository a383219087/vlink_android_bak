package com.yjkj.chainup.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author lianshangljl
 * @Date 2020-03-31-12:25
 * @Email buptjinlong@163.com
 * @description
 */
public class DepthBean {
    private String price;
    private String vol;
    private String sum;


    public String getPrice() {
        return price;
    }

    public String getVol() {
        return vol;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public JSONObject toJson() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("price", price);
            jsonObj.put("vol", vol);
            jsonObj.put("sum", sum);
        } catch (JSONException ignored) {
        }
        return jsonObj;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }
}
