package com.chainup.contract.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import androidx.appcompat.app.AppCompatDelegate;

import com.chainup.contract.R;
import com.chainup.contract.app.CpMyApp;
import com.yjkj.chainup.manager.CpLanguageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujing on 2017/10/19.
 */

public class CpClLogicContractSetting {


    public interface IContractSettingListener {
        void onContractSettingChange();
    }

    private static CpClLogicContractSetting instance = null;

    public static CpClLogicContractSetting getInstance() {
        if (null == instance)
            instance = new CpClLogicContractSetting();
        return instance;
    }

    private List<IContractSettingListener> mListeners = new ArrayList<>();

    private CpClLogicContractSetting() {

    }

    public void registListener(IContractSettingListener listener) {

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount < mListeners.size(); iCount++) {
            if (listener.equals(mListeners.get(iCount)))
                break;
        }

        if (iCount >= mListeners.size())
            mListeners.add(listener);
    }


    public void unregistListener(IContractSettingListener listener) {

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount < mListeners.size(); iCount++) {
            if (listener.equals(mListeners.get(iCount))) {
                mListeners.remove(mListeners.get(iCount));
                return;
            }
        }
    }

    public void refresh() {
        for (int i = 0; i < mListeners.size(); i++) {
            if (mListeners.get(i) != null) {
                mListeners.get(i).onContractSettingChange();
            }
        }
    }


    private static int s_contract_unit = 0;
    private static boolean s_contract_unit_first = true;

    //0 ??? 1 ???
    public static int getContractUint(Context context) {
        return 1;
//        if (s_contract_unit_first) {
//            s_contract_unit = CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_CONTRACT_UNIT, 0);
//            s_contract_unit_first = false;
//        }
//        return s_contract_unit;
    }

    public static void setContractUint(Context context, int unit) {
        s_contract_unit = unit;
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_CONTRACT_UNIT, unit);
    }



    private static boolean s_usdt_unit = true;

    //0 BTC 1 USDT
    public static boolean getIsUSDT(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedBoolean(CpPreferenceManager.PREF_CONTRACT_USDT, true);

    }

    public static void setIsUSDT(Context context, boolean unit) {
        CpPreferenceManager.getInstance(context).putSharedBoolean(CpPreferenceManager.PREF_CONTRACT_USDT, unit);
    }


    /**
     * 0 - ????????????
     */
    public static final int THEME_MODE_DAYTIME = 0;
    /**
     * 1 - ????????????
     */
    public static final int THEME_MODE_NIGHT = 1;

    public static int getThemeMode(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_THEMEMODE, THEME_MODE_DAYTIME);
    }

    /**
     * app???????????????
     */
    public static void setThemeMode(int mode) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).putSharedInt(CpPreferenceManager.PREF_THEMEMODE, mode);
        if (mode == THEME_MODE_NIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    /**
     * ??????
     */
    public static void setLanguage(String currentLan) {
//        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).putSharedInt(CpPreferenceManager.PREF_LANGUAGE, currentLan);
//        CpLocalManageUtil.saveSelectLanguage();
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static boolean isLogin() {
        return !TextUtils.isEmpty(getToken());
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static String getToken() {
        return CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).getSharedString(CpPreferenceManager.CONTRACT_TOKEN, "");
    }

    /**
     * ????????????????????????
     *
     * @param key
     */
    public static void setToken(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).putSharedString(CpPreferenceManager.CONTRACT_TOKEN, key);
    }

    /**
     * ????????????????????????
     *
     */
    public static void cleanToken() {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).removeShare(CpPreferenceManager.CONTRACT_TOKEN);
    }

    public static void setInviteUrl(String key) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).putSharedString(CpPreferenceManager.CONTRACT_INVITE_URL,key);
    }

    public static String getInviteUrl() {
        return CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).getSharedString(CpPreferenceManager.CONTRACT_INVITE_URL,"");
    }



    private static int s_pnl_calculate = 0;
    private static boolean s_pnl_calculate_first = true;

    //0 ???????????? 1 ??????   ?????????
    public static int getPnlCalculate(Context context) {
        if (s_pnl_calculate_first) {
            s_pnl_calculate = CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_CONTRACT_PNL_CALCULATE, 1);
            s_pnl_calculate_first = false;
        }
        return s_pnl_calculate;
    }

    public static void setPnlCalculate(Context context, int unit) {
        s_pnl_calculate = unit;
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_CONTRACT_PNL_CALCULATE, unit);
    }

    private static int s_trigger_price_type = 1;
    private static boolean s_trigger_price_type_first = true;

    //1????????? 2????????? 4?????????
    public static int getTriggerPriceType(Context context) {
        if (s_trigger_price_type_first) {
            s_trigger_price_type = CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_TRIGGER_PRICE_TYPE, 1);
            s_trigger_price_type_first = false;
        }
        //????????????????????????????????????0?????????
        if (s_trigger_price_type == 0) {
            s_trigger_price_type = 1;
        }
        return s_trigger_price_type;
    }

    public static void setTriggerPriceType(Context context, int unit) {
        s_trigger_price_type = unit;
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_TRIGGER_PRICE_TYPE, unit);
    }

    private static int s_execution = 0;
    private static boolean s_execution_first = true;

    //0?????? 1??????
    public static int getExecution(Context context) {
        if (s_execution_first) {
            s_execution = CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_EXECUTION, 0);
            s_execution_first = false;
        }
        return s_execution;
    }

    public static void setExecution(Context context, int unit) {
        s_execution = unit;
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_EXECUTION, unit);
    }

    private static int s_strategy_effect_time = 0;
    private static boolean s_strategy_effect_time_first = true;

    //0 24h 17day
    public static int getStrategyEffectTime(Context context) {
        if (s_strategy_effect_time_first) {
            s_strategy_effect_time = CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, 1);
            s_strategy_effect_time_first = false;
        }
        return s_strategy_effect_time;
    }

    //????????????????????? ?????????14???
    public static int getStrategyEffectTimeStr(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, 14);
    }


    public static void setStrategyEffectTime(Context context, int unit) {
        s_strategy_effect_time = unit;
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, unit);
    }

    public static void setStrategyEffectTimeStr(Context context, int unit) {
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, unit);
    }

    public static String getContractJsonListStr(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedString(CpPreferenceManager.CONTRACT_JSON_LIST_STR, "");
    }

    public static JSONObject getContractJsonStrById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * ??????id??????????????????????????????
     *
     * @param context
     * @param contractId
     * @return
     */
    public static int getContractSymbolPricePrecisionById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getJSONObject("coinResultVo").getInt("symbolPricePrecision");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    /**
     * ??????id??????????????????????????????
     *
     * @param context
     * @param contractId
     * @return
     */
    public static String getContractQuoteById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("quote");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static String getContractWsSymbolById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return (mJSONObject.getString("contractType") + "_" + mJSONObject.getString("symbol").replace("-", "")).toLowerCase();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * ??????id???????????????????????????????????????
     *
     * @param context
     * @param contractId
     * @return
     */
    public static int getContractMarginCoinPrecisionById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getJSONObject("coinResultVo").getInt("marginCoinPrecision");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public static String getContractMarginRateById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("marginRate");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "0";
        }
        return "0";
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param context
     * @param marginCoin
     * @return
     */
    public static int getContractMarginCoinPrecisionByMarginCoin(Context context, String marginCoin) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                String mMarginCoin = mJSONObject.getString("marginCoin");
                if (mMarginCoin.equals(marginCoin)) {
                    return mJSONObject.getJSONObject("coinResultVo").getInt("marginCoinPrecision");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public static int getContractMultiplierPrecisionById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    String multiplier = mJSONObject.getString("multiplier");
                    String multiplierBuff = new BigDecimal(multiplier).stripTrailingZeros().toPlainString();
                    if (multiplierBuff.contains(".")) {
                        int index = multiplierBuff.indexOf(".");
                        int num = index < 0 ? 0 : multiplierBuff.length() - index - 1;
                        return num;
                    } else {
                        return multiplierBuff.length();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public static String getContractMultiplierById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("multiplier");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "0";
        }
        return "0";
    }

    /**
     * ????????????????????????
     *
     * @param context
     * @param contractId
     * @return
     */
    public static String getContractMultiplierCoinById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("multiplierCoin");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static String getContractMultiplierCoinPrecisionById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("multiplierCoin");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static String getContractSymbolNameById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    String contractType = mJSONObject.getString("contractType");
                    String symbol = mJSONObject.getString("symbol");
                    String marginCoin = mJSONObject.getString("marginCoin");
                    if (!contractType.equals("H") && !contractType.equals("S"))
                        symbol = symbol + "-" + marginCoin;
                    return symbol;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static String getContractMarginCoinById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getString("marginCoin");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static int getContractSideById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    return mJSONObject.getInt("contractSide");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    public static String getContractShowNameById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                   if (mJSONObject.isNull("contractOtherName")){
                       String contractType = mJSONObject.getString("contractType");
                       String marginCoin = mJSONObject.getString("marginCoin");
                       String symbol = mJSONObject.getString("symbol");
                       if (contractType.equals("E")) {
                           return symbol;
                       } else {
                           return symbol + "-" + marginCoin;
                       }
                   }else {
                       String contractOtherName = mJSONObject.getString("contractOtherName");
                       return contractOtherName;
                   }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }
    public static String getContractNameById(Context context, int contractId) {
        String contractJsonListStr = getContractJsonListStr(context);
        try {
            JSONArray jsonArray = new JSONArray(contractJsonListStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mJSONObject = (JSONObject) jsonArray.get(i);
                int id = mJSONObject.getInt("id");
                if (contractId == id) {
                    String symbol = mJSONObject.getString("symbol");
                    return symbol;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static void setContractJsonListStr(Context context, String data) {
        CpPreferenceManager.getInstance(context).putSharedString(CpPreferenceManager.CONTRACT_JSON_LIST_STR, data);
    }

    public static void setContractMarginCoinListStr(Context context, String data) {
        CpPreferenceManager.getInstance(context).putSharedString(CpPreferenceManager.CONTRACT_MARGIN_COIN_STR, data);
    }

    public static String getContractMarginCoinListStr(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedString(CpPreferenceManager.CONTRACT_MARGIN_COIN_STR, "");
    }

    public static void setContractCurrentSelectedId(Context context, int ContractId) {
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.CONTRACT_CURRENT_SELECTED_ID, ContractId);
    }

    public static int getContractCurrentSelectedId(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.CONTRACT_CURRENT_SELECTED_ID, -1);
    }


//    // 1?????????, 0?????????
//    public static int getContractIsOpen(Context context) {
//        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_IS_OPEN, 0);
//    }
//
//    public static void setContractIsOpen(Context context, int unit) {
//        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_IS_OPEN, unit);
//    }

    // 1??????, 2??????
    public static int getPositionModel(Context context) {
        return CpPreferenceManager.getInstance(context).getSharedInt(CpPreferenceManager.CONTRACT_POSITION_MODEL, 0);
    }

    public static void setPositionModel(Context context, int unit) {
        CpPreferenceManager.getInstance(context).putSharedInt(CpPreferenceManager.CONTRACT_POSITION_MODEL, unit);
    }

    public static String getShareInfo(Context context, String profitRate) {
        if (CpBigDecimalUtils.compareTo(profitRate, "0") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "5") < 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_win_intro1")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "5") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "20") < 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_win_intro2")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "20") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "50") < 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_win_intro3")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "50") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "100") < 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_win_intro4")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "100") >= 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_win_intro5")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "0") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-5") >= 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_lose_intro1")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-5") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-20") >= 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_lose_intro2")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-20") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-50") >= 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_lose_intro3")+" ??? ";
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-50") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-100") >= 0) {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_lose_intro4")+" ??? ";
        } else {
            return "??? "+CpLanguageUtil.getString(context, "cp_str_lose_intro5")+" ??? ";
        }
    }

    public static int getShareBg(String profitRate) {
        if (CpBigDecimalUtils.compareTo(profitRate, "0") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "5") < 0) {
            return R.drawable.contract_smallcompany;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "5") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "20") < 0) {
            return R.drawable.contract_smallcompany;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "20") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "50") < 0) {
            return R.drawable.contract_smallcompany;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "50") >= 0 && CpBigDecimalUtils.compareTo(profitRate, "100") < 0) {
            return R.drawable.contract_profit;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "100") >= 0) {
            return R.drawable.contract_profit;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "0") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-5") >= 0) {
            return R.drawable.contract_profit_smallloss;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-5") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-20") >= 0) {
            return R.drawable.contract_profit_smallloss;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-20") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-50") >= 0) {
            return R.drawable.contract_profit_smallloss;
        } else if (CpBigDecimalUtils.compareTo(profitRate, "-50") < 0 && CpBigDecimalUtils.compareTo(profitRate, "-100") >= 0) {
            return R.drawable.contract_bigloss;
        } else {
            return R.drawable.contract_bigloss;
        }
    }
}
