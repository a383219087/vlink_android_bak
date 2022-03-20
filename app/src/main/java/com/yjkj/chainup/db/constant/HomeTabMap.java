package com.yjkj.chainup.db.constant;

import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.net.api.ApiConstants;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * @Description: 首页底下5个tab类型常量值
 * @Author: wanghao
 * @CreateDate: 2019-10-25 20:38
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-10-25 20:38
 * @UpdateRemark: 更新说明
 */
public class HomeTabMap {

    public static final HashMap<String, Integer> maps = new HashMap<>();
    public static final String homeTab = "homeTab"; //首页
    public static final String marketTab = "marketTab"; //行情tab
    public static final String coinTradeTab = "coinTradeTab"; //币币交易
    public static final String otccoinTradeTab = "otccoinTradeTab"; //法币交易
    public static final String contractTab = "contractTab"; //合约
    public static final String assetsTab = "assetsTab"; //资产

    public static void initMaps(JSONObject data) {
        maps.clear();
        boolean contractOpen = PublicInfoDataService.getInstance().contractOpen(data);
        maps.put(homeTab, 0);
        if (ApiConstants.HOME_VIEW_STATUS.equals(ParamConstant.CONTRACT_HOME_PAGE)) {
            maps.put(contractTab, 1);
            maps.put(assetsTab, 2);
        } else {
            maps.put(marketTab, 1);
            maps.put(coinTradeTab, 2);
            if (contractOpen) {
                maps.put(contractTab, 3);
                maps.put(assetsTab, 4);
            } else {
                maps.put(assetsTab, 3);
            }
        }

    }
}
