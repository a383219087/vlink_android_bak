package com.yjkj.chainup.db.service;

import android.text.TextUtils;

import com.chainup.contract.utils.CpClLogicContractSetting;
import com.contract.sdk.ContractSDKAgent;
import com.contract.sdk.data.ContractUser;
import com.yjkj.chainup.contract.cloud.ContractCloudAgent;
import com.yjkj.chainup.db.MMKVDb;
import com.yjkj.chainup.manager.LoginManager;
import com.yjkj.chainup.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Description:
 * @Author: wanghao
 * @CreateDate: 2019-08-12 15:43
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-08-12 15:43
 * @UpdateRemark: 更新说明
 */
public class UserDataService {

    private static final String userDataKey = "userData";
    private static final String SP_GESTURE_PASS = "sp_gesture_pass";
    private static final String userTokenKey = "userToken";
    private static final String quicktoken = "QUICKTOKEN";
    private static final String showAssets = "showAssets";
    private static final String show_little_assets = "show_little_assets";//隐藏小额资产
    private static final String home_data = "home_data";//首页数据

    private static String LOGIN_TOKEN = "";
    private MMKVDb mMMKVDb;

    private boolean isNetworkCheckIng = false;

    private UserDataService() {
        mMMKVDb = new MMKVDb();
    }

    private static UserDataService mUserDataService;

    public static UserDataService getInstance() {
        if (null == mUserDataService)
            mUserDataService = new UserDataService();
        return mUserDataService;

    }

    public void saveData(JSONObject data) {
        if (null != data) {
            mMMKVDb.saveData(userDataKey, data.toString());
        }
        CpClLogicContractSetting.setInviteUrl(data.optString("inviteUrl"));
        //谷歌验证登录后只有token没有id,无法创建合约子账户,获取用户信息后需要通知合约再次创建
        if (ContractCloudAgent.INSTANCE.isCloudOpen()) {
            ContractCloudAgent.INSTANCE.init(getUserInfo4UserId(), null);
        }
    }

    public JSONObject getUserData() {
        String data = mMMKVDb.getData(userDataKey);
        if (StringUtil.checkStr(data)) {
            try {
                return new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void saveQuickToken(String token) {
        mMMKVDb.saveData(quicktoken, token);
    }

    public String getQuickToken() {
        return mMMKVDb.getData(quicktoken);
    }


    public String getToken() {
        if (LOGIN_TOKEN.isEmpty()) {
            LOGIN_TOKEN = getTokenData();
            return LOGIN_TOKEN;
        }
        return LOGIN_TOKEN;
    }

    public void saveToken(String token) {
        LOGIN_TOKEN = token;
        setToken(token);
        CpClLogicContractSetting.setToken(LOGIN_TOKEN);
        notifyContractLoginStatusListener();
    }

    public void clearToken() {
        LOGIN_TOKEN = "";
        setToken("");
        CpClLogicContractSetting.cleanToken();
        notifyContractLoginStatusListener();
    }

    public void clearTokenByContract() {
        LOGIN_TOKEN = "";
        setToken("");
    }

    /**
     * 合约业务需要，需监听登录回调
     */
    public void notifyContractLoginStatusListener() {
        if (TextUtils.isEmpty(LOGIN_TOKEN)) {
            ContractSDKAgent.INSTANCE.exitLogin();
            CpClLogicContractSetting.cleanToken();
        } else {
            CpClLogicContractSetting.setToken(LOGIN_TOKEN);
            if (ContractCloudAgent.INSTANCE.isCloudOpen()) {
                ContractCloudAgent.INSTANCE.init(getUserInfo4UserId(), null);
            } else {
                try {
                    ContractUser contractUser = new ContractUser();
                    contractUser.setToken(LOGIN_TOKEN);
                    ContractSDKAgent.INSTANCE.setUser(contractUser);
                } catch (Exception e) {
//                    LogUtil.e("ContractSDKAgent.INSTANCE.setUser", e.getMessage());
                }
            }
        }
    }

    public void saveGesturePass(String pass) {
        mMMKVDb.saveData(SP_GESTURE_PASS, pass);
        LoginManager.getInstance().saveGesturePwdErrorTimes(5);
    }

    public String getGesturePass() {
        return mMMKVDb.getData(SP_GESTURE_PASS);
    }

    public void clearLoginState() {
        LOGIN_TOKEN = "";
        saveData(new JSONObject());
        notifyContractLoginStatusListener();
    }

    public String getGesturePwd() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("gesturePwd");
        return "";
    }

    public String getNickName() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("nickName");
        return "";

    }

    public String getCountryCode() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("countryCode");
        return "";

    }

    public String getEmail() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("email");
        return "";
    }

    public String getInviteCode() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("inviteCode");
        return "";
    }

    public String getInviteUrl() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("inviteUrl");
        return "";
    }

    public String getInviteLeftQuota() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("inviteLeftQuota");
        return "";
    }

    public String getMobileNumber() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("mobileNumber");
        return "";
    }

    public String getRealName() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("realName");
        return "";
    }

    public int getAuthLevel() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("authLevel");
        return -1;
    }

    public int getGoogleStatus() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("googleStatus");
        return 0;

    }

    public int getIsOpenMobileCheck() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("isOpenMobileCheck");
        return 0;
    }

    public int getIsCapitalPwordSet() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("isCapitalPwordSet");
        return 0;
    }

    public int getAccountStatus() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("accountStatus");
        return 0;

    }

    public int getAgentStatus() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("agentStatus");
        return 0;

    }

    public boolean isLogined() {
        return StringUtil.checkStr(getToken());
    }


    public String getUserInfo4UserId() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("id");
        return "";
    }

    public String getUserAccount() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optString("userAccount");
        return "";
    }


    /*
     * 资产的隐藏与展示
     */
    public void setShowAssetStatus(boolean isShowAssets) {
        mMMKVDb.saveBooleanData(showAssets, isShowAssets);
    }

    public boolean isShowAssets() {
        return mMMKVDb.getBooleanData(showAssets, true);
    }


    /**
     * 是否隐藏"小额资产"
     */
    public boolean getAssetState() {
        return mMMKVDb.getBooleanData(show_little_assets, false);
    }

    public void saveAssetState(boolean isHide) {
        mMMKVDb.saveBooleanData(show_little_assets, isHide);
    }

    private void setByKey(String key, String object) {
        if (null != object) {
            mMMKVDb.saveData(key, object);
        }
    }

    private String getKeyByKey(String key) {
        if (null != mMMKVDb) {
            return mMMKVDb.getData(key);
        }
        return "";
    }

    private void setToken(String tokenData) {
        setByKey(userTokenKey, tokenData);
    }

    private String getTokenData() {
        return getKeyByKey(userTokenKey);
    }

    public boolean isNetworkCheckIng() {
        return isNetworkCheckIng;
    }

    public void setNetworkCheckIng(boolean networkCheckIng) {
        isNetworkCheckIng = networkCheckIng;
    }

    /**
     * 是否开启 ETF
     *
     * @return
     */
    public boolean getUserIsOpenEtf() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("useEtf",0) == 1;
        return false;
    }

    public boolean getETFIsLimit() {
        JSONObject data = getUserData();
        if (null != data)
            return data.optInt("etfLocalLimit") == 1;
        return false;
    }

    public void saveETFOpen() {
        JSONObject data = getUserData();
        if (null != data) {
            try {
                data.put("useEtf", 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
