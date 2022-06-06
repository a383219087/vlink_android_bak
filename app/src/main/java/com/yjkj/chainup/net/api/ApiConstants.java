package com.yjkj.chainup.net.api;

import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.util.Utils;

/**
 * @author bertking
 */
public class ApiConstants {




    public static String EX_CHAINUP_BUNDLE_VERSION = "4550";


    /**
     * 现用 以上都是给用户使用的 url
     * URL
     */
    public static String BASE_URL = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.baseUrl),true,"baseUrl");  //线上环境
    /**
     * OTC http
     */
    public static String BASE_OTC_URL = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.otcBaseUrl),true,"otcBaseUrl");
    /**
     * WebSocket地址
     */
    public static String SOCKET_ADDRESS = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.socketAddress),false,"socketAddress");  //线上环境

    /**
     * OTC WebSocket地址
     */
    public static String SOCKET_OTC_ADDRESS = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.otcSocketAddress),false,"otcSocketAddress");

    /**
     * 合约地址
     */
    public static String CONTRACT_URL = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.contractUrl),true,"contractUrl");  //线上环境
    /**
     * 新合约地址
     */
    public static String NEW_CONTRACT_URL = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.httpHostUrlContractV2),true,"httpHostUrlContractV2");  //线上环境

    /**
     * 合约 WebSocket地址
     */
    public static String SOCKET_CONTRACT_ADDRESS = Utils.returnAPIUrl(ChainUpApp.appContext.getApplicationContext().getString(R.string.contractSocketAddress),false,"contractSocketAddress");







}
