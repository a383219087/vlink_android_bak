package com.yjkj.chainup.new_version.home;

import com.yjkj.chainup.manager.SymbolWsData;
import com.yjkj.chainup.net.api.ApiConstants;
import com.yjkj.chainup.net.MsgWSSClient;
import com.yjkj.chainup.util.LogUtil;
import com.yjkj.chainup.util.WsLinkUtils;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @Author lianshangljl
 * @Date 2019-12-12-11:55
 * @Email buptjinlong@163.com
 * @description
 */
public class NewHomeDetailWsData {

    private static final String TAG = "MarketWsData";

    private RefreshWSListener mRefreshListener;

    public interface RefreshWSListener {
        void onRefreshWS(int pos);
    }

    private MsgWSSClient mMsgWSSClient;
    private ArrayList<JSONObject> dataList;

    public void initSocket(ArrayList<JSONObject> list, RefreshWSListener l) {
        LogUtil.d(TAG, "initSocket==list is " + list);
        if (null == list || list.size() <= 0) {
            return;
        }
        this.dataList = list;
        this.mRefreshListener = l;
        if (null == mMsgWSSClient || !mMsgWSSClient.isConnected()) {
            mMsgWSSClient = new MsgWSSClient(ApiConstants.SOCKET_ADDRESS);
            mMsgWSSClient.setSocketListener(new MsgWSSClient.SocketResultListener() {

                @Override
                public void onSuccess(JSONObject jsonObject) {
                    LogUtil.d(TAG, "initSocket==onSuccess==jsonObject is " + jsonObject);
                    boolean history = false;
                    if (!jsonObject.isNull("data")) {
                        /**
                         * 这里是历史k线
                         */
                        String temp = jsonObject.optString("channel", "");

                        String[] all = temp.split("_");
                        if (all != null && all.length > 1) {
                            ArrayList<String> paramList = new ArrayList<String>();
                            paramList.add(WsLinkUtils.getKlineNewLink(all[1], "1min", false).getJson());
                            mMsgWSSClient.setSendMsg(paramList);
                        }
                        history = true;
                    }

                    showWsData(jsonObject, history);
                }

                @Override
                public void onFailure(String message) {
                    LogUtil.d(TAG, "initSocket==message is " + message);
                }
            });

            ArrayList<String> paramList = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                String symbol = list.get(i).optString("symbol");
                paramList.add(WsLinkUtils.getKLineHistoryLink(symbol, "1min").getJson());
                paramList.add(WsLinkUtils.tickerFor24HLink(symbol, true, false));
            }
            mMsgWSSClient.setSendMsg(paramList);
            mMsgWSSClient.connectWS();
        }

    }

    /**
     * 返回24小时行情
     *
     * @param jsonObject
     */
    private void showWsData(JSONObject jsonObject, boolean history) {
        if (null == mRefreshListener || null == dataList)
            return;
        JSONObject obj = new SymbolWsData().getNewSymbolObj(dataList, jsonObject, history);
        if (null != obj && obj.length() > 0) {
            int pos = dataList.indexOf(obj);
            if (pos >= 0) {
                mRefreshListener.onRefreshWS(pos);
            }
        }
    }


    public void closeWS() {
        if (null != mMsgWSSClient) {
            mMsgWSSClient.close();
        }
    }

}
