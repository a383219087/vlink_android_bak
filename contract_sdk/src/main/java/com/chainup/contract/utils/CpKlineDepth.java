package com.chainup.contract.utils;

import com.chainup.contract.bean.CpDepthBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lianshangljl
 * @Date 2020-03-31-12:23
 * @Email buptjinlong@163.com
 * @description
 */
public class CpKlineDepth {


    /**
     * // 买盘,按价格由大到小排序
     */
    private List<CpDepthBean> bids = new ArrayList<>();
    /**
     * 卖盘,按价格由小到大排序
     */
    private List<CpDepthBean> asks = new ArrayList<>();


    public void clear() {
        bids.clear();
        asks.clear();
    }

    public List<CpDepthBean> getBids() {
        if (bids == null) {
            bids = new ArrayList<>();
        }
        return bids;
    }

    public void setBids(List<CpDepthBean> bids) {
        this.bids = bids;
    }

    public List<CpDepthBean> getAsks() {
        if (asks == null) {
            asks = new ArrayList<>();
        }
        return asks;
    }

    public void setAsks(List<CpDepthBean> asks) {
        this.asks = asks;
    }


    public void fromJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        if (asks == null) {
            asks = new ArrayList<>();
        }
        asks.clear();
        JSONArray array_sells = jsonObject.optJSONArray("asks");
        try {
            if (array_sells != null) {
                for (int i = 0; i < array_sells.length(); i++) {
                    JSONArray obj = array_sells.getJSONArray(i);
                    if (obj == null) {
                        continue;
                    }

                    CpDepthBean item = new CpDepthBean();
                    int count = obj.length();
                    if (count >= 2) {
                        for (int k = 0; k < obj.length(); k++) {
                            if (k == 0) {
                                item.setPrice(obj.getString(0));
                            } else if (k == 1) {
                                item.setVol(obj.getString(1));
                            }else if (k == 2) {
                                item.setSum(obj.getString(2));
                            }
                        }
                    }
                    asks.add(item);
                }
            }
        } catch (JSONException ignored) {
        }

        if (bids == null) {
            bids = new ArrayList<>();
        }
        bids.clear();
        JSONArray array_buys = jsonObject.optJSONArray("buys");
        try {
            if (array_buys != null) {
                for (int i = 0; i < array_buys.length(); i++) {
                    JSONArray obj = array_buys.getJSONArray(i);
                    if (obj == null) {
                        continue;
                    }
                    CpDepthBean item = new CpDepthBean();
                    int count = obj.length();
                    if (count >= 2) {
                        for (int k = 0; k < obj.length(); k++) {
                            if (k == 0) {
                                item.setPrice(obj.getString(0));
                            } else if (k == 1) {
                                item.setVol(obj.getString(1));
                            }else if (k == 2) {
                                item.setSum(obj.getString(2));
                            }
                        }
                    }
                    bids.add(item);
                }
            }
        } catch (JSONException ignored) {
        }
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray array_buys = new JSONArray();
            for (int i = 0; i < bids.size(); i++) {
                array_buys.put(bids.get(i).toJson());
            }
            jsonObject.put("buys", array_buys);

            JSONArray array_sells = new JSONArray();
            for (int i = 0; i < asks.size(); i++) {
                array_sells.put(asks.get(i).toJson());
            }
            jsonObject.put("asks", array_sells);

        } catch (JSONException ignored) {
        }
        return jsonObject;
    }

    public String toString() {

        JSONObject jsonObj = toJson();
        if (jsonObj == null)
            return "";
        else
            return jsonObj.toString();
    }


    public CpKlineDepth clone() {
        CpKlineDepth depth = new CpKlineDepth();
        List<CpDepthBean> cloneBids = new ArrayList<>();
        for (CpDepthBean depthData : bids) {
            cloneBids.add(depthData);
        }
        List<CpDepthBean> cloneSells = new ArrayList<>();
        for (CpDepthBean depthData : asks) {
            cloneSells.add(depthData);
        }
        depth.setBids(cloneBids);
        depth.setAsks(cloneSells);
        return depth;
    }

}
