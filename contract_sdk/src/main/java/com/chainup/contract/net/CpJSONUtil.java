package com.chainup.contract.net;

import com.chainup.contract.R;
import com.chainup.contract.utils.CpContextUtil;
import com.chainup.contract.utils.CpNToastUtil;
import com.chainup.contract.utils.CpStringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;

public class CpJSONUtil {

    private static final String TAG = "JSONUtil";

    public static JSONObject parse(ResponseBody responseBody, boolean isShowToast) {
        if (null == responseBody) {
            if (isShowToast) {
                CpNToastUtil.showTopToast(false, CpContextUtil.getString(R.string.cp_extra_text11));
            }
            return null;
        }
        try {
            String jsonStr = responseBody.string();
            if (CpStringUtil.checkStr(jsonStr)) {
                JSONObject obj = new JSONObject(jsonStr);
                String code = obj.optString("code");
                if ("0".equalsIgnoreCase(code)) {

                } else {
                    String msg = obj.optString("msg");
                    if (CpStringUtil.checkStr(msg) && isShowToast) {
                        if (!"200002".equalsIgnoreCase(code)||!"109006".equalsIgnoreCase(code)) {
//                            CpNToastUtil.showTopToast( false,""+msg);

                        }
                    }
                }
                return obj;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isShowToast) {
                CpNToastUtil.showTopToast(false, "IOException " + e.getMessage());
            }
        } catch (JSONException e) {
            if (isShowToast) {
                CpNToastUtil.showTopToast(false, "JSONException " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isShowToast) {
                CpNToastUtil.showTopToast(false, "Server error " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 将json对象转换成Map
     *
     * @param jsonObject json对象
     * @return Map对象
     */
    public static Map<String, Object> jsonObjtoMap(JSONObject jsonObject) {
        if (null == jsonObject)
            return null;
        Map<String, Object> result = new HashMap<String, Object>();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.opt(key);
            result.put(key, value);
        }
        return result;
    }

    /*
     * jsonObject to jsonArray
     */
    public static JSONArray getJSONArray(JSONObject jsonObject) {

        if (null == jsonObject || jsonObject.length() <= 0)
            return null;

        Iterator<String> iterator = jsonObject.keys();
        JSONArray jsonArray = new JSONArray();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.optJSONObject(key);
            JSONObject jsoObject = new JSONObject();
            try {
                jsoObject.put(key, value);
                jsonArray.put(jsoObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonArray;
    }




    /*
     * 将jsonArray转换为List
     */
    public static ArrayList<JSONObject> arrayToList(JSONArray array) {
        if (null == array || array.length() <= 0)
            return null;
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.optJSONObject(i);
            if (null != jsonObject) {
                list.add(jsonObject);
            }
        }
        return list;
    }





    /*
     * 要保证参数名和变量值名一样
     */
    public static JSONArray getJSONArray(String... arg) {
        if (null == arg || 0 == arg.length) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (String str : arg) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("str", str);
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        //Add strategy if needed.
        gson = builder.create();
    }

}
