package com.yjkj.chainup.contract.uilogic;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yjkj.chainup.contract.utils.PreferenceManager;
import com.yjkj.chainup.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhoujing on 2017/10/19.
 */

public class LogicContractSetting {

    public interface IContractSettingListener {
        void onContractSettingChange();
    }

    private static LogicContractSetting instance = null;

    public static LogicContractSetting getInstance() {
        if (null == instance)
            instance = new LogicContractSetting();
        return instance;
    }

    private List<IContractSettingListener> mListeners = new ArrayList<>();

    private LogicContractSetting() {

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

    //0 张 1 币
    public static int getContractUint(Context context) {
        if (s_contract_unit_first) {
            s_contract_unit = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_UNIT, 0);
            s_contract_unit_first = false;
        }
        return s_contract_unit;
    }

    public static void setContractUint(Context context, int unit) {
        s_contract_unit = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_UNIT, unit);
    }

    private static int s_pnl_calculate = 0;
    private static boolean s_pnl_calculate_first = true;

    //0 合理价格 1 最新   成交价
    public static int getPnlCalculate(Context context) {
        if (s_pnl_calculate_first) {
            s_pnl_calculate = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_PNL_CALCULATE, 1);
            s_pnl_calculate_first = false;
        }
        return s_pnl_calculate;
    }

    public static void setPnlCalculate(Context context, int unit) {
        s_pnl_calculate = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_PNL_CALCULATE, unit);
    }

    private static int s_trigger_price_type = 1;
    private static boolean s_trigger_price_type_first = true;

    //1市场价 2合理价 4指数价
    public static int getTriggerPriceType(Context context) {
        if (s_trigger_price_type_first) {
            s_trigger_price_type = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_TRIGGER_PRICE_TYPE, 1);
            s_trigger_price_type_first = false;
        }
        //字段升级，需兼容老版本为0的情况
        if (s_trigger_price_type == 0) {
            s_trigger_price_type = 1;
        }
        return s_trigger_price_type;
    }

    public static void setTriggerPriceType(Context context, int unit) {
        s_trigger_price_type = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_TRIGGER_PRICE_TYPE, unit);
    }

    private static int s_execution = 0;
    private static boolean s_execution_first = true;

    //0限价 1市价
    public static int getExecution(Context context) {
        if (s_execution_first) {
            s_execution = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_EXECUTION, 0);
            s_execution_first = false;
        }
        return s_execution;
    }

    public static void setExecution(Context context, int unit) {
        s_execution = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_EXECUTION, unit);
    }

    private static int s_strategy_effect_time = 0;
    private static boolean s_strategy_effect_time_first = true;

    //0 24h 17day
    public static int getStrategyEffectTime(Context context) {
        if (s_strategy_effect_time_first) {
            s_strategy_effect_time = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, 1);
            s_strategy_effect_time_first = false;
        }
        return s_strategy_effect_time;
    }

    //获取有效期时间 默认取14天
    public static int getStrategyEffectTimeStr(Context context) {
        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, 14);
    }


    public static void setStrategyEffectTime(Context context, int unit) {
        s_strategy_effect_time = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, unit);
    }

    public static void setStrategyEffectTimeStr(Context context, int unit) {
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, unit);
    }

    public static String getContractJsonListStr(Context context) {
        return PreferenceManager.getInstance(context).getSharedString(PreferenceManager.CONTRACT_JSON_LIST_STR, "");
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
     * 根据id获取合约币对价格精度
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
     * 根据id获取合约保证金币种显示精度
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

    /**
     * 根据保证金币种获取合约保证金币种显示精度
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
                        LogUtil.e("------------", multiplierBuff);
                        LogUtil.e("------------", multiplierBuff.split(".").length + "");
                        int index = multiplierBuff.indexOf(".");
                        int num = index < 0 ? 0 : multiplierBuff.length() - index - 1;
                        LogUtil.e("------------", num + "");
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
     * 获取合约面值单位
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
                    String contractType = mJSONObject.getString("contractType");
                    String marginCoin = mJSONObject.getString("marginCoin");
                    String symbol = mJSONObject.getString("symbol");
                    if (contractType.equals("E")) {
                        return symbol;
                    } else {
                        return symbol + "-" + marginCoin;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "--";
        }
        return "--";
    }

    public static void setContractJsonListStr(Context context, String data) {
        PreferenceManager.getInstance(context).putSharedString(PreferenceManager.CONTRACT_JSON_LIST_STR, data);
    }

    public static void setContractMarginCoinListStr(Context context, String data) {
        PreferenceManager.getInstance(context).putSharedString(PreferenceManager.CONTRACT_MARGIN_COIN_STR, data);
    }

    public static String getContractMarginCoinListStr(Context context) {
        return PreferenceManager.getInstance(context).getSharedString(PreferenceManager.CONTRACT_MARGIN_COIN_STR, "");
    }

    public static void setContractCurrentSelectedId(Context context, int ContractId) {
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.CONTRACT_CURRENT_SELECTED_ID, ContractId);
    }

    public static int getContractCurrentSelectedId(Context context) {
        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.CONTRACT_CURRENT_SELECTED_ID, -1);
    }


//    // 1已开通, 0未开通
//    public static int getContractIsOpen(Context context) {
//        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_IS_OPEN, 0);
//    }
//
//    public static void setContractIsOpen(Context context, int unit) {
//        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_IS_OPEN, unit);
//    }

    // 1单向, 2双向
    public static int getPositionModel(Context context) {
        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.CONTRACT_POSITION_MODEL, 0);
    }

    public static void setPositionModel(Context context, int unit) {
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.CONTRACT_POSITION_MODEL, unit);
    }
}
