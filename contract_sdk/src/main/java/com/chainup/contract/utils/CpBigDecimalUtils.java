package com.chainup.contract.utils;


import android.text.TextUtils;
import android.util.Log;


import com.chainup.contract.app.CpMyApp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class CpBigDecimalUtils {

    //默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static BigDecimal add(String v1, String v2) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).add(new BigDecimal(v2));
    }

    public static String addStr(String v1, String v2, int scale) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).add(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static BigDecimal sub(String v1, String v2) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).subtract(new BigDecimal(v2));
    }

    public static String subStr(String v1, String v2, int scale) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).subtract(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(String v1, String v2) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).multiply(new BigDecimal(v2));

    }

    public static String mulStr(String v1, String v2, int scale) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).multiply(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();

    }

    public static String mulStrRoundUp(String v1, String v2, int scale) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).multiply(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_UP).toPlainString();

    }

    /**
     * 提供精确的乘法运算。(TODO 舍入)
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(String v1, String v2, int scale) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).multiply(new BigDecimal(v2)).setScale(scale, BigDecimal.ROUND_DOWN);

    }


    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字舍入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal div(String v1, String v2, int scale) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }

        if (0 == compareTo(v2, "0"))
            return new BigDecimal(v1);

        if (scale < 0) {
            scale = 0;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_DOWN);
    }

    public static String div(BigDecimal v1, BigDecimal v2, int scale) {
        return v1.divide(v2, scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 此方法不四舍五入
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度。
     *
     * @param v1    参数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal divForDown(String v1, int scale) {
        if (!CpStringUtil.checkStr(v1)) {
            v1 = "0";
        }
        if (!CpStringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;
        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_DOWN);
    }


    /**
     * 此方法四舍五入
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度。
     *
     * @param v1    参数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static BigDecimal divForUp(String v1, int scale) {
        if (!CpStringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_UP);
    }

    public static String scaleStr(String v1, int scale) {

        if (!CpStringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_FLOOR).toPlainString();
    }

    /**
     * 截取数字
     * 四舍五入
     *
     * @param v1
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return
     */
    public static BigDecimal intercept(String v1, int scale) {

        if (!CpStringUtil.isNumeric(v1)) {
            v1 = "0";
        }
        if (scale < 0)
            scale = 0;

        return new BigDecimal(v1).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 精确对比两个数字
     *
     * @param v1 需要被对比的第一个数
     * @param v2 需要被对比的第二个数
     * @return 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
     */
    public static int compareTo(String v1, String v2) {

        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }
        return new BigDecimal(v1).compareTo(new BigDecimal(v2));

    }

    public static String divStr(String v1, String v2, int scale) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (!CpStringUtil.isNumeric(v2)) {
            v2 = "0";
        }

        if (0 == compareTo(v2, "0"))
            return new BigDecimal(v1).toPlainString();

        if (scale < 0) {
            scale = 0;
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 禁用科学计数法
     *
     * @return 返回double类型
     */
    public static double showDNormal(Double data) {
        return Double.valueOf(showSNormal(data));
    }

    /**
     * 禁用科学计数法
     *
     * @return 返回double类型
     */
    public static String showSNormal(Double data) {
        try {
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(data));
            String plainString = bigDecimal.toPlainString();
            return subZeroAndDot(plainString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "0.0";
        }

    }


    /**
     * 禁用科学计数法
     * <p>
     * 补充：toPlainString()
     * No scientific notation is used. This methods adds zeros where necessary.
     * return: a string representation of {@code this} without exponent part
     * <p>
     * IAW, 返回的字符串不会带指数形式
     *
     * @return 返回double类型
     */
    public static String showSNormal(String data) {
        if (!CpStringUtil.checkStr(data)) {
            return "";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!CpStringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).toPlainString();
        return subZeroAndDot(plainString);
    }

    public static String showSNormal(String data, int scale) {
        if (!CpStringUtil.checkStr(data)) {
            return "";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!CpStringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
//        return subZeroAndDot(plainString);
        return plainString;
    }

    public static String showSNormalUp(String data, int scale) {
        if (!CpStringUtil.checkStr(data)) {
            return "";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!CpStringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).setScale(scale, BigDecimal.ROUND_UP).toPlainString();
//        return subZeroAndDot(plainString);
        return plainString;
    }

    public static String showSNormalNew(String data, int scale) {
        if (!CpStringUtil.checkStr(data)) {
            return "--";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!CpStringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString();
//        return subZeroAndDot(plainString);
        return plainString;
    }

    public static String showSNormalNew(String data) {
        if (!CpStringUtil.checkStr(data)) {
            return "";
        }

        if (data.contains("\"")) {
            data = stringReplace(data);
        }
        if (!CpStringUtil.isNumeric(data)) {
            data = "0";
        }
        String plainString = new BigDecimal(data).toPlainString();
        return plainString;
    }


    public static String showNormal(String data) {
        if (!CpStringUtil.isNumeric(data)) {
            return "0";
        }
        return new BigDecimal(data).toPlainString();
    }

    /**
     * 去掉双引号
     *
     * @param wifiInfo
     * @return
     */
    public static String stringReplace(String wifiInfo) {
        String str = wifiInfo.replace("\"", "");
        return str;
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        if (!CpStringUtil.isNumeric(s))
            return "0";

        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }


    public static String showDepthVolume(String value) {
        if (!CpStringUtil.isNumeric(value))
            value = "0";

        String temp = new BigDecimal(value).toPlainString();
        if (compareTo(temp, "0.0001") <= 0) {
            return "0";
        } else if (compareTo(temp, "1000") >= 0) {
            return formatNumber(temp);
        } else {
            if (temp.contains(".")) {
                return (temp + "00000").substring(0, 5);
            } else {
                String substring = (temp + ".0000").substring(0, 4);
                if (substring.endsWith(".")) {
                    return substring.substring(0, 3);
                } else {
                    return substring;
                }
            }
        }
    }

    public static String showDepthContractVolume(String value) {
        if (!CpStringUtil.isNumeric(value))
            value = "0";

        String temp = new BigDecimal(value).toPlainString();
        if (compareTo(temp, "1000") >= 0) {
            return formatNumber(temp);
        } else {
            return temp;
        }
    }


    public static String formatNumber(String str) {
        if (!CpStringUtil.isNumeric(str))
            return "--";
        String number = "";
        BigDecimal b0 = new BigDecimal("1000");
        BigDecimal b1 = new BigDecimal("1000000");
        BigDecimal b2 = new BigDecimal("1000000000");
        BigDecimal temp = new BigDecimal(str);
        if (temp.compareTo(b0) == -1) {
            number = str;
            return showSNormal(number);
        } else if ((temp.compareTo(b0) == 0 || temp.compareTo(b0) == 1) && temp.compareTo(b1) == -1) {
            String substring = temp.divide(b0, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "K";
        } else if (temp.compareTo(b1) >= 0 && temp.compareTo(b2) < 0) {
            String substring = temp.divide(b1, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "M";
        } else if (temp.compareTo(b2) >= 0) {
            String substring = temp.divide(b2, 2, BigDecimal.ROUND_DOWN).toString().substring(0, 4);
            if (substring.endsWith(".")) {
                number = substring.substring(0, 3);
            } else {
                number = substring;
            }
            return number + "B";
        } else {
            return showSNormal(number);
        }
    }

    public static int compareToDraw(String v1, String v2) {
        if (!CpStringUtil.isNumeric(v1))
            v1 = "0";

        if (v1.equals("0")) {
            return -1;
        }
        return compareTo(v1, v2);
    }


    /**
     * 判断num1是否大于num2
     *
     * @param num1
     * @param num2
     * @return num1大于num2返回true
     */
    public static boolean greaterThan(String num1, String num2) {
        BigDecimal b1 = new BigDecimal(num1);
        BigDecimal b2 = new BigDecimal(num2);
        return b1.compareTo(b2) == 1;
    }

    /**
     * 计算可买可卖数量
     *
     * @return
     */
    public static String canBuyStr(boolean isOpen, boolean isLimit, boolean isForward, String price, String parValue, String canUseAmount, String canCloseVolume, String nowLevel, String rate, int scale, String unit) {

        String defaultStr = "0" + " " + unit;
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) != 0) {
            defaultStr = "0.00" + " " + unit;
        } else {
            defaultStr = "0" + " " + unit;
        }
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal canCloseVolumeBig = new BigDecimal(canCloseVolume);
        BigDecimal buff;
        if (!CpClLogicContractSetting.isLogin()) {
            return defaultStr;
        }
        if (!isOpen) {
            if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
                return canCloseVolumeBig.setScale(0, BigDecimal.ROUND_DOWN).toPlainString() + " " + unit;
            } else {
                return parValueBig.multiply(canCloseVolumeBig).setScale(scale, BigDecimal.ROUND_DOWN).toPlainString() + " " + unit;
            }
        }
        if (compareTo(price, "0") == 0) {
            return defaultStr;
        }
        BigDecimal priceBig = new BigDecimal(price);
        BigDecimal canUseAmountBig = new BigDecimal(canUseAmount);
        BigDecimal nowLevelBig = new BigDecimal(nowLevel);
        BigDecimal rateBig = new BigDecimal(rate);


        if (isForward) {
            buff = canUseAmountBig.multiply(nowLevelBig).divide(priceBig, scale, BigDecimal.ROUND_DOWN).divide(rateBig, scale, BigDecimal.ROUND_DOWN);
        } else {
            buff = canUseAmountBig.multiply(nowLevelBig).multiply(priceBig).divide(rateBig, scale, BigDecimal.ROUND_DOWN);
        }
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            scale = 0;
            buff = buff.divide(parValueBig, scale, BigDecimal.ROUND_DOWN);
        }
        return buff.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString() + " " + unit;
    }

    /**
     * 计算可开数量
     * <p>
     * <p>
     * 最大可开张数/数量 = min{风险限额计算的可开张数，保证金计算的可开张数}
     * <p>
     * 风险限额计算的可开张数计算：
     * 正向：最大可开张数 = （当前合约最大可开额度-当前合约同方向持仓仓位价值-当前合约同方向未成交委托价值）*汇率/（价格*面值）
     * 反向：最大可开张数 = （当前合约最大可开额度-当前合约同方向持仓仓位价值-当前合约同方向未成交委托价值）*价格/面值
     * 正向：最大可开量 = （当前合约最大可开额度-当前合约同方向持仓仓位价值-当前合约同方向未成交委托价值）*汇率/价格
     * 反向：最大可开量 = （当前合约最大可开额度-当前合约同方向持仓仓位价值-当前合约同方向未成交委托价值）*价格
     * <p>
     * 持仓价值计算：
     * 正向：持仓价值 = 持仓均价*持仓数量*合约面值*汇率
     * 反向：持仓价值 =持仓数量*合约面值/持仓均价
     * 委托价值计算：
     * 正向：委托价值 = 委托价格 *开仓委托数量*合约面值*汇率
     * 反向：委托价值 = 开仓委托数量*合约面值/委托价格
     *
     * @return
     */
    public static String canOpenStr(boolean isForward, boolean isLimit, String price, String maxOpenLimit, String positionValue, String entrustedValue, String parValue, String rate, int scale, String unit) {
        String defaultStr = "0" + " " + unit;
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) != 0) {
            defaultStr = "0.00" + " " + unit;
        } else {
            defaultStr = "0" + " " + unit;
        }
        if (TextUtils.isEmpty(parValue)) parValue = "0";
        if (TextUtils.isEmpty(price)) price = "0";
        if (TextUtils.isEmpty(maxOpenLimit)) maxOpenLimit = "0";
        if (TextUtils.isEmpty(positionValue)) positionValue = "0";
        if (TextUtils.isEmpty(entrustedValue)) entrustedValue = "0";
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal priceBig = new BigDecimal(price);
        BigDecimal rateBig = new BigDecimal(rate);
        BigDecimal maxOpenLimitBig = new BigDecimal(maxOpenLimit);//（币的单位）
        BigDecimal positionValueBig = new BigDecimal(positionValue);//（币的单位）
        BigDecimal entrustedValueBig = new BigDecimal(entrustedValue);//（币的单位）
        BigDecimal buff;
        if (compareTo(price, "0") == 0) {
            return defaultStr;
        }
        //张
        //正向 /价格*面值
        //反向 *价格/面值
        //币
        //正向 /价格
        //反向 *价格
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            if (isForward) {
                if (isLimit) {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).divide((priceBig.multiply(parValueBig)), scale, BigDecimal.ROUND_DOWN);
                } else {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(rateBig).divide((priceBig.multiply(parValueBig)), scale, BigDecimal.ROUND_DOWN);
                }
            } else {
                if (isLimit) {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(priceBig).divide(parValueBig, scale, BigDecimal.ROUND_DOWN);
                } else {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(priceBig).divide(parValueBig, scale, BigDecimal.ROUND_DOWN);
                }
            }
            return buff.setScale(0, BigDecimal.ROUND_DOWN).toPlainString() + " " + unit;
        } else {
            if (isForward) {
                if (isLimit) {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).divide(priceBig, scale, BigDecimal.ROUND_DOWN);
                } else {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(rateBig).divide(priceBig, scale, BigDecimal.ROUND_DOWN);
                }
            } else {
                if (isLimit) {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(priceBig);
                } else {
                    buff = (maxOpenLimitBig.subtract(positionValueBig).subtract(entrustedValueBig)).multiply(priceBig);
                }
            }
            return buff.setScale(scale, BigDecimal.ROUND_DOWN).toPlainString() + " " + unit;
        }
    }


    /**
     * 计算持仓价值
     * 正向：持仓价值 = 持仓均价*持仓数量*合约面值*汇率
     * 反向：持仓价值 =持仓数量*合约面值/持仓均价
     *
     * @return
     */
    public static String calcPositionValue(boolean isForward, String positionNum, String positionAveragePrice, String parValue, String rate, int scale) {
        if (TextUtils.isEmpty(positionNum)) positionNum = "0";
        if (TextUtils.isEmpty(positionAveragePrice)) positionAveragePrice = "0";
        if (TextUtils.isEmpty(parValue)) parValue = "0";
        BigDecimal positionNumBig = new BigDecimal(positionNum);
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal positionAveragePriceBig = new BigDecimal(positionAveragePrice);
        BigDecimal rateBig = new BigDecimal(rate);

        if (isForward) {
            //持仓价值 = 持仓均价*持仓数量*合约面值*汇率
            return positionAveragePriceBig.multiply(positionNumBig).multiply(parValueBig).multiply(rateBig).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        } else {
            //反向：持仓价值 =持仓数量*合约面值/持仓均价
            return positionNumBig.multiply(parValueBig).divide(positionAveragePriceBig, scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }
    }

    /**
     * 计算委托价值
     * 正向：委托价值 = 委托价格 *开仓委托数量*合约面值*汇率
     * 反向：委托价值 = 开仓委托数量*合约面值/委托价格
     *
     * @return
     */
    public static String calcEntrustedValue(boolean isForward, String entrustedPrice, String openEntrustedNum, String parValue, String rate, int scale) {
        if (TextUtils.isEmpty(entrustedPrice)) entrustedPrice = "0";
        if (TextUtils.isEmpty(parValue)) parValue = "0";
        if (TextUtils.isEmpty(openEntrustedNum)) openEntrustedNum = "0";
        BigDecimal entrustedPriceBig = new BigDecimal(entrustedPrice);
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal openEntrustedNumBig = new BigDecimal(openEntrustedNum);
        BigDecimal rateBig = new BigDecimal(rate);
        if (isForward) {
            //委托价值 = 委托价格 *开仓委托数量*合约面值*汇率
            return entrustedPriceBig.multiply(openEntrustedNumBig).multiply(parValueBig).multiply(rateBig).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        } else {
            //委托价值 = 开仓委托数量*合约面值/委托价格
            return openEntrustedNumBig.multiply(parValueBig).divide(entrustedPriceBig, scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }
    }


    /**
     * 计算预估成本价
     *
     * @param orderType
     * @param price
     * @param position
     * @param nowLevel
     * @param rate
     * @param scale
     * @param unit
     * @return
     */
    public static String canCostStr(boolean isOpen, boolean isForward, int orderType, String price, String position, String parValue, String nowLevel, String rate, int scale, String unit) {

        String defaultStr = "0" + " " + unit;
        if (!isOpen) {
            return "0" + " " + unit;
        }
        if (TextUtils.isEmpty(position)) {
            return defaultStr;
        }
        String sheets = "0";

        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            sheets = position;
        } else {
            sheets = divStr(position, parValue, 0);
        }

        BigDecimal positionBig = new BigDecimal(position);
        BigDecimal sheetsBig = new BigDecimal(sheets);
        BigDecimal nowLevelBig = new BigDecimal(nowLevel);
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal rateBig = new BigDecimal(rate);

        BigDecimal buff = null;

        if (orderType == 1 || orderType == 4 || orderType == 5 || orderType == 6) { // 限价单、PostOnly、IOC、FOK
            if (TextUtils.isEmpty(price)) {
                return defaultStr;
            }
            if (compareTo(price, "0") != 1) {
                return defaultStr;
            }
            BigDecimal priceBig = new BigDecimal(price);
            if (isForward) {
                buff = sheetsBig.multiply(parValueBig).multiply(priceBig).divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
            } else {
                buff = sheetsBig.multiply(parValueBig).divide(priceBig, scale, BigDecimal.ROUND_HALF_DOWN).divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
            }
        } else if (orderType == 2) {//市价单
            buff = positionBig.divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
        } else if (orderType == 3) {//条件单
            //0限价 1市价
            if (CpClLogicContractSetting.getExecution(CpMyApp.Companion.instance()) == 1) {
                //条件市价单
                buff = positionBig.divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
            } else {
                //条件限价单
                if (TextUtils.isEmpty(price)) {
                    return defaultStr;
                }
                if (compareTo(price, "0") != 1) {
                    return defaultStr;
                }
                BigDecimal priceBig = new BigDecimal(price);
                if (isForward) {
                    buff = sheetsBig.multiply(parValueBig).multiply(priceBig).divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
                } else {
                    buff = sheetsBig.multiply(parValueBig).divide(priceBig, scale, BigDecimal.ROUND_HALF_DOWN).divide(nowLevelBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(rateBig);
                }
            }
        } else {
            return "0" + " " + unit;
        }

        return buff.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString() + " " + unit;
    }

    /**
     * 计算数量换算展示
     *
     * @param position
     * @param parValue
     * @param scale
     * @param unit
     * @return
     */
    public static String canPositionStr(String position, String parValue, int scale, String unit) {
        String defaultStr = "";
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) != 0) {
            defaultStr = "0.00" + " " + unit;
        } else {
            defaultStr = "0" + " " + unit;
        }
        //0 张 1 币
        if (TextUtils.isEmpty(position)) {
            return defaultStr;
        }
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            //数量=张数*面值
            return mulStr(position, parValue, scale) + " " + unit;
        } else {
            //张=数量/面值
            return divStr(position, parValue, 0) + " " + unit;

        }
    }

    /**
     * 计算市价单/条件市价单的数据展示
     *
     * @param openValue  开仓价值
     * @param price      本交易所最新价格
     * @param scale      精度
     * @param unit       单位
     * @param isForward  是否输入正向合约
     * @param marginRate 保证金汇率
     * @return
     */
    public static String canPositionMarketStr(boolean isForward, String marginRate, String parValue, String openValue, String price, int scale, String unit) {
        String defaultStr = "";
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance())  != 0) {
            defaultStr = "0.00" + " " + unit;
        } else {
            defaultStr = "0" + " " + unit;
        }
        if (TextUtils.isEmpty(openValue)) {
            return defaultStr;
        }
        if (TextUtils.isEmpty(price)) {
            return defaultStr;
        }



        /**
         * 正向合约
         * ≈ 开仓价值 / 本交易所最新价格 {币}
         * ≈ 开仓价值 / 本交易所最新价格 / 面值 {张}
         *
         * 反向合约
         * ≈ 开仓价值 * 本交易所最新价格  {币}
         * ≈ 开仓价值 * 本交易所最新价格 / 面值 {张}
         */
        BigDecimal openValueBig = new BigDecimal(openValue);
        BigDecimal priceBig = new BigDecimal(price);
        BigDecimal marginRateBig = new BigDecimal(marginRate);
        BigDecimal parValueBig = new BigDecimal(parValue);
         if (priceBig.doubleValue()==0){
             return defaultStr;
         }
        BigDecimal buff;
        if (isForward) {
            buff = openValueBig.divide(priceBig, scale, RoundingMode.HALF_DOWN);
        } else {
            buff = openValueBig.multiply(priceBig);
        }
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            return buff.setScale(scale, RoundingMode.HALF_DOWN).toPlainString() + " " + unit;
        } else {
            return buff.divide(parValueBig, 0, RoundingMode.HALF_DOWN).toPlainString() + " " + unit;
        }
    }
    /**
     * 计算市价单/条件市价单的数据展示
     *
     * @param openValue  开仓价值
     * @param price      本交易所最新价格
     * @param scale      精度
     * @param isForward  是否输入正向合约
     * @return判断是否大于1张
     */
    public static Boolean canPositionMarketBoolean(boolean isForward, String openValue,String parValue, String price, int scale, String unit) {

        if (TextUtils.isEmpty(openValue)) {
            return false;
        }
        if (TextUtils.isEmpty(price)) {
            return false;
        }

        /**
         * 正向合约
         * ≈ 开仓价值 / 本交易所最新价格 / 面值 {张}
         *
         * 反向合约
         * ≈ 开仓价值 * 本交易所最新价格 / 面值 {张}
         */
        BigDecimal openValueBig = new BigDecimal(openValue);
        BigDecimal priceBig = new BigDecimal(price);
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal buff;
        if (isForward) {
            buff = openValueBig.divide(priceBig, scale, BigDecimal.ROUND_HALF_DOWN);
        } else {
            buff = openValueBig.multiply(priceBig);
        }

        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            BigDecimal a=buff.setScale(scale, RoundingMode.HALF_DOWN);
            return a.intValue()>=1;

        } else {
            BigDecimal a= buff.divide(parValueBig, 0, RoundingMode.HALF_DOWN);
            //a/buff a=输入的币币，buff等于1张的币币
            BigDecimal b= a.divide(buff, 0, RoundingMode.HALF_DOWN);
            return  b.intValue()>=1;
        }
    }

    /**
     * 计算中位数
     *
     * @param buyOne
     * @param sellOne
     * @param latestPrice
     * @return
     */
    public static String median(String buyOne, String sellOne, String latestPrice) {
        if (compareTo(buyOne, "0") == 0 && compareTo(sellOne, "0") == 0) {
            if (compareTo(latestPrice, "0") == 0) {
                return "0";
            } else {
                return latestPrice;
            }
        }
        String[] arr = {buyOne, sellOne, latestPrice};
        Arrays.sort(arr);
        return arr[1];
    }

    public static String min(String oneStr, String towStr) {
        if (compareTo(oneStr, towStr) == -1) {
            return oneStr;
        }
        return towStr;
    }

    /**
     * 下单最小数量校验
     *
     * @param inputNum
     * @return
     */
    public static boolean orderNumMinCheck(String inputNum, String minNum, String multiplier) {
        int ret = 0;
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            ret = compareTo(minNum, inputNum);
        } else {
            BigDecimal multiplierBig = new BigDecimal(multiplier);
            BigDecimal inputNumBig = new BigDecimal(inputNum);
            ret = compareTo(minNum, inputNumBig.divide(multiplierBig, 0, BigDecimal.ROUND_HALF_DOWN).toPlainString());
        }
        //如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
        return ret == 1;
    }

    /**
     * 下单最大数量校验
     *
     * @param inputNum
     * @return
     */
    public static boolean orderNumMaxCheck(String inputNum, String maxNum, String multiplier) {
        int ret = 0;
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            ret = compareTo(inputNum, maxNum);
        } else {
            BigDecimal multiplierBig = new BigDecimal(multiplier);
            BigDecimal inputNumBig = new BigDecimal(inputNum);
            ret = compareTo(inputNumBig.divide(multiplierBig, 0, BigDecimal.ROUND_HALF_DOWN).toPlainString(), maxNum);
        }
        //如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
        return ret == 1;
    }

    /**
     * 下单最小金额校验
     *
     * @param inputNum
     * @return
     */
    public static boolean orderMoneyMinCheck(String inputNum, String minNum, String multiplier) {
        int ret = 0;
        ret = compareTo(minNum, inputNum);
        //如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
        return ret == 1;
    }

    /**
     * 下单最大金额校验
     *
     * @param inputNum
     * @return
     */
    public static boolean orderMoneyMaxCheck(String inputNum, String maxNum, String multiplier) {
        int ret = 0;
        ret = compareTo(inputNum, maxNum);
        //如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
        return ret == 1;
    }

    /**
     * 计算下单数量（币转换为张）
     *
     * @param inputNum
     * @param multiplier
     * @return
     */
    public static String getOrderNum(boolean isOpen, String inputNum, String multiplier, int orderType) {
        if (orderType == 2) {//市价单
            if (isOpen) {
                return inputNum;
            }
        } else if (orderType == 3) {//条件单
            //0限价 1市价
            if (CpClLogicContractSetting.getExecution(CpMyApp.Companion.instance()) == 1) {
                //条件市价单
                if (isOpen) {
                    return inputNum;
                }
            }
        }
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            return new BigDecimal(inputNum).setScale(0, BigDecimal.ROUND_DOWN).toPlainString();
        } else {
            BigDecimal multiplierBig = new BigDecimal(multiplier);
            BigDecimal inputNumBig = new BigDecimal(inputNum);
            return inputNumBig.divide(multiplierBig, 0, BigDecimal.ROUND_DOWN).toPlainString();
        }
    }

    public static String getOrderLossNum(String inputNum, String multiplier) {
        if (TextUtils.isEmpty(inputNum)){
            inputNum="0";
        }
        if (CpClLogicContractSetting.getContractUint(CpMyApp.Companion.instance()) == 0) {
            return new BigDecimal(inputNum).setScale(0, BigDecimal.ROUND_FLOOR).toPlainString();
        } else {
            BigDecimal multiplierBig = new BigDecimal(multiplier);
            BigDecimal inputNumBig = new BigDecimal(inputNum);
            return inputNumBig.divide(multiplierBig, 0, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }
    }

    /**
     * 计算逐仓权益
     *
     * @param positionMargin   仓位保证金
     * @param realizedAmount   已实现盈亏
     * @param unRealizedAmount 未实现盈亏
     * @return 仓位保证金 + 已实现盈亏 + 未实现盈亏
     */

    public static String calcPositionEquity(String positionMargin, String realizedAmount, String unRealizedAmount, int scale) {
        BigDecimal positionMarginBig = new BigDecimal(positionMargin);
        BigDecimal realizedAmountBig = new BigDecimal(realizedAmount);
        BigDecimal unRealizedAmountBig = new BigDecimal(unRealizedAmount);
        return positionMarginBig.add(realizedAmountBig).add(unRealizedAmountBig).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }

    /**
     * 计算强平价格
     *
     * @param positionEquity    逐仓权益
     * @param marginRate        保证金汇率
     * @param positionVolume    仓位数量
     * @param positionDirection 仓位方向
     * @param markPrice         标记价格
     * @param keepMarginRate    维持保证金率
     * @param feeRate           手续费率
     * @return 强平价格（正向合约） =（逐仓权益 / 保证金汇率 - 仓位数量 * 仓位方向 * 标记价格） / （（维持保证金率 + 手续费率）* 仓位数量 - 仓位 * 仓位方向）
     * 强平价格（反向合约） =（（维持保证金率 + 手续费率）* 仓位数量 + 仓位 * 仓位方向）/ （逐仓权益 / 保证金汇率 + 仓位数量 * 仓位方向 / 标记价格）
     */
    public static String calcForcedPrice(boolean isForward, String positionEquity, String marginRate, String positionVolume, String positionDirection, String markPrice, String keepMarginRate, String feeRate, int scale) {
        BigDecimal positionEquityBig = new BigDecimal(positionEquity);
        BigDecimal marginRateBig = new BigDecimal(marginRate);
        BigDecimal positionVolumeBig = new BigDecimal(positionVolume);
        BigDecimal positionDirectionBig = new BigDecimal(positionDirection);
        BigDecimal markPriceBig = new BigDecimal(markPrice);
        BigDecimal keepMarginRateBig = new BigDecimal(keepMarginRate);
        BigDecimal feeRateBig = new BigDecimal(feeRate);

        if (isForward) {
            BigDecimal buff1 = positionEquityBig.divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff2 = positionVolumeBig.multiply(positionDirectionBig).multiply(markPriceBig);
            BigDecimal buff3 = buff1.subtract(buff2);
            BigDecimal buff4 = keepMarginRateBig.add(feeRateBig);
            BigDecimal buff5 = buff4.multiply(positionVolumeBig);
            BigDecimal buff6 = positionVolumeBig.multiply(positionDirectionBig);
            BigDecimal buff7 = buff5.subtract(buff6);
            return buff3.divide(buff7, scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        } else {
            BigDecimal buff1 = keepMarginRateBig.add(feeRateBig);
            BigDecimal buff2 = buff1.multiply(positionVolumeBig);
            BigDecimal buff3 = positionVolumeBig.multiply(positionDirectionBig);
            BigDecimal buff4 = buff2.add(buff3);

            BigDecimal buff5 = positionEquityBig.divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff6 = positionVolumeBig.multiply(positionDirectionBig).divide(markPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff7 = buff5.add(buff6);
            return buff4.divide(buff7, scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }


    }

    /**
     * 计算开仓保证金
     *
     * @param amount
     * @param openingPrice
     * @param lever
     * @param marginRate
     * @param scale
     * @return
     */
    public static String calcMarginValue(boolean isForward, String amount, String openingPrice, String lever, String marginRate, int scale) {

        BigDecimal amountBig = new BigDecimal(amount);
        BigDecimal openingPriceBig = new BigDecimal(openingPrice);
        BigDecimal leverBig = new BigDecimal(lever);
        BigDecimal marginRateBig = new BigDecimal(marginRate);

        if (isForward) {
            //正向合约：所需保证金=初始保证金 = 数量 * 开仓价格 / 杠杆 / 保证金汇率
            return amountBig.multiply(openingPriceBig).divide(leverBig, scale, BigDecimal.ROUND_HALF_DOWN).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        } else {
            //反向合约：所需保证金=初始保证金 = 数量 / 开仓价格 / 杠杆 / 保证金汇率
            return amountBig.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN).divide(leverBig, scale, BigDecimal.ROUND_HALF_DOWN).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        }
    }

    /**
     * 计算收益额
     *
     * @param isForward    是否属于正向合约
     * @param direction    0 多  1 空
     * @param amount       数量
     * @param openingPrice 开仓价格
     * @param closePrice   平仓价格
     * @param marginRate   保证金汇率
     * @param scale        精度
     * @return
     */
    public static String calcIncomeValue(boolean isForward, int direction, String amount, String openingPrice, String closePrice, String marginRate, int scale) {

        BigDecimal amountBig = new BigDecimal(amount);
        BigDecimal openingPriceBig = new BigDecimal(openingPrice);
        BigDecimal closePriceBig = new BigDecimal(closePrice);
        BigDecimal marginRateBig = new BigDecimal(marginRate);
        /**
         * 收益额（单位：保证金币种）
         * 正向合约：
         * 买入做多 收益额=（平仓价格 - 开仓均价）* 数量 / 保证金汇率
         * 卖出做空 收益额=（平仓价格 - 开仓均价）* 数量 / 保证金汇率 * -1
         *
         * 反向合约：
         * 买入做多 收益额=（1/平仓价格 - 1/开仓均价）* 数量 / 保证金汇率 * -1
         * 卖出做空 收益额=（1/平仓价格 - 1/开仓均价）* 数量 / 保证金汇率
         */
        String buff = "";
        if (isForward) {
            BigDecimal buff1 = closePriceBig.subtract(openingPriceBig);
            if (direction == 0) {
                //买入做多
                buff = buff1.multiply(amountBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                buff = buff1.multiply(amountBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1")).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        } else {
            BigDecimal buff1 = BigDecimal.ONE.divide(closePriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff2 = BigDecimal.ONE.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff3 = buff1.subtract(buff2);
            if (direction == 0) {
                //买入做多
                buff = buff3.multiply(amountBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1")).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                buff = buff3.multiply(amountBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        }
        return buff;
    }

    /**
     * 计算平仓价格
     *
     * @param isForward    是否属于正向合约
     * @param direction    0 多  1 空
     * @param amount       回报率
     * @param openingPrice 开仓价格
     * @param lever        杠杆
     * @param scale        精度
     * @return 平仓价格
     */
    public static String calcClosePriceValue(boolean isForward, int direction, String amount, String openingPrice, String lever, int scale) {

        BigDecimal amountBig = CpBigDecimalUtils.div(amount, "100", 5);
        BigDecimal openingPriceBig = new BigDecimal(openingPrice);
        BigDecimal leverBig = new BigDecimal(lever);
        /**
         平仓价格（单位：计价货币）
         正向合约：
         买入做多 平仓价格 = 开仓价格 *（杠杆 + 回报率） / 杠杆
         卖出做空 平仓价格 = 开仓价格 *（杠杆 - 回报率） / 杠杆

         反向合约：
         买入做多 平仓价格 = 开仓价格 * 杠杆  /  （杠杆 - 回报率）
         卖出做空 平仓价格 = 开仓价格 * 杠杆  /  （杠杆 + 回报率）
         */
        String buff = "";
        if (isForward) {
            BigDecimal buff1 = leverBig.add(amountBig);
            BigDecimal buff2 = leverBig.subtract(amountBig);
            if (direction == 0) {
                //买入做多
                buff = openingPriceBig.multiply(buff1).divide(leverBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                buff = openingPriceBig.multiply(buff2).divide(leverBig, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        } else {

            BigDecimal buff1 = leverBig.subtract(amountBig);
            BigDecimal buff2 = leverBig.add(amountBig);
            if (buff1.compareTo(BigDecimal.ZERO) == 0) {
                return "-1";
            }
            if (direction == 0) {
                //买入做多
                buff = openingPriceBig.multiply(leverBig).divide(buff1, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                buff = openingPriceBig.multiply(leverBig).divide(buff2, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        }
        return buff;
    }

    /**
     * 计算强平价格
     *
     * @param isForward      是否属于正向合约
     * @param direction      0 多  1 空
     * @param marginAmount   保证金数量
     * @param positionAmount 仓位数量
     * @param openingPrice   开仓价格
     * @param keepMarginRate 维持保证金率
     * @param marginRate     保证金汇率
     * @param scale          精度
     * @return 强平价格
     */
    public static String calcForceClosePriceValue(boolean isForward, int direction, String marginAmount, String positionAmount, String openingPrice, String keepMarginRate, String marginRate, int scale) {

        BigDecimal marginAmountBig = new BigDecimal(marginAmount);//保证金数量
        BigDecimal openingPriceBig = new BigDecimal(openingPrice);//开仓价格
        BigDecimal positionAmountBig = new BigDecimal(positionAmount);//仓位数量
        BigDecimal marginRateBig = new BigDecimal(marginRate);//保证金汇率
        BigDecimal keepMarginRateBig = new BigDecimal(keepMarginRate);//维持保证金率
        BigDecimal feeRateBig = new BigDecimal("0.00075");//手续费率
        /**
         * 强平价格（单位：计价货币）
         * 正向合约：
         * 多仓 强平价格 = （保证金数量 / 保证金汇率 - 仓位数量 * 开仓价格） / （（维持保证金率 + 手续费率 - 1）* 仓位数量）
         * 空仓 强平价格 = （保证金数量 / 保证金汇率 + 仓位数量 * 开仓价格） / （（维持保证金率 + 手续费率 + 1）* 仓位数量 ）
         *
         * 反向合约：
         * 多仓 强平价格 = （（维持保证金率 + 手续费率 + 1）* 仓位数量）/ （保证金数量 / 保证金汇率 + 仓位数量 / 开仓价格）
         * 空仓 强平价格 = （（维持保证金率 + 手续费率 - 1）* 仓位数量）/ （保证金数量 / 保证金汇率 - 仓位数量 / 开仓价格）
         *
         * 维持保证金率 = （仓位数量 * 标记价格）所在的挡位的维持保证金率
         * 手续费=0.075%
         */
        String buff = "";
        if (isForward) {
            BigDecimal buff1 = marginAmountBig.divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff2 = positionAmountBig.multiply(openingPriceBig);
            if (direction == 0) {
                //买入做多
                BigDecimal buff3 = buff1.subtract(buff2);
                BigDecimal buff4 = keepMarginRateBig.add(feeRateBig).subtract(BigDecimal.ONE);
                BigDecimal buff5 = buff4.multiply(positionAmountBig);
                buff = buff3.divide(buff5, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                BigDecimal buff3 = buff1.add(buff2);
                BigDecimal buff4 = keepMarginRateBig.add(feeRateBig).add(BigDecimal.ONE);
                BigDecimal buff5 = buff4.multiply(positionAmountBig);
                buff = buff3.divide(buff5, scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        } else {
            BigDecimal buff1 = marginAmountBig.divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal buff2 = positionAmountBig.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
            if (direction == 0) {
                //买入做多
                BigDecimal buff3 = keepMarginRateBig.add(feeRateBig).add(BigDecimal.ONE);
                BigDecimal buff4 = buff3.multiply(positionAmountBig);
                return buff4.divide(buff1.add(buff2), scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            } else {
                //卖出做空
                BigDecimal buff3 = keepMarginRateBig.add(feeRateBig).subtract(BigDecimal.ONE);
                BigDecimal buff4 = buff3.multiply(positionAmountBig);
                return buff4.divide(buff1.subtract(buff2), scale, BigDecimal.ROUND_HALF_DOWN).setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
            }
        }
        return buff;
    }

    /**
     * 止盈止损 计算预计盈亏
     *
     * @param isForward      是否属于正向合约
     * @param direction      0 多  1 空
     * @param isLimit        是否输入限价单
     * @param openPrice      开仓均价
     * @param triggerPrice   触发价
     * @param commissionPrice   委托价
     * @param positionAmount 仓位数量（张）
     * @param parValue       面值
     * @param marginRate     保证金汇率
     * @param scale
     * @return
     */

    public static String calcEstimatedProfitLoss(boolean isForward, int direction, boolean isLimit, String openPrice, String triggerPrice, String commissionPrice, String positionAmount, String parValue, String marginRate, int scale) {
        if (TextUtils.isEmpty(positionAmount)) positionAmount ="0";
        if (TextUtils.isEmpty(openPrice)) openPrice ="0";
        if (TextUtils.isEmpty(triggerPrice)) triggerPrice ="0";
        if (TextUtils.isEmpty(commissionPrice)) commissionPrice ="0";

        BigDecimal positionAmountBig = new BigDecimal(positionAmount);
        BigDecimal openingPriceBig = new BigDecimal(openPrice);
        BigDecimal parValueBig = new BigDecimal(parValue);
        BigDecimal marginRateBig = new BigDecimal(marginRate);
        BigDecimal triggerPriceBig = new BigDecimal(triggerPrice);
        BigDecimal commissionPriceBig = new BigDecimal(commissionPrice);

        Log.e("-------isForward:",isForward+"");
        Log.e("-------direction:",direction+"");
        Log.e("-------position:",positionAmount);
        Log.e("-------openPrice:",openPrice);
        Log.e("-------parValue面值:",parValue);
        Log.e("-------marginRate保证金汇率:",marginRate);
        Log.e("-------triggerPrice触发价:",triggerPrice);
        Log.e("-------commission:",commissionPrice);
        /**
         * 预计盈亏（单位：保证金币种）
         * 正向合约：
         * 预计盈亏（市价-多仓） =（触发价 - 开仓均价）* 仓位张数 * 面值 / 保证金汇率
         * 预计盈亏（市价-空仓） =（触发价 - 开仓均价）* 仓位张数 * 面值 / 保证金汇率 * -1
         * 预计盈亏（限价-多仓） =（委托价 - 开仓均价）* 仓位张数 * 面值 / 保证金汇率
         * 预计盈亏（限价-空仓） =（委托价 - 开仓均价）* 仓位张数 * 面值 / 保证金汇率 * -1
         *
         * 反向合约：
         * 预计盈亏（市价-多仓） =（1/开仓均价 - 1/触发价）* 仓位张数 * 面值 / 保证金汇率
         * 预计盈亏（市价-空仓） =（1/开仓均价 - 1/触发价）* 仓位张数 * 面值 / 保证金汇率 * -1
         * 预计盈亏（限价-多仓） =（1/开仓均价 - 1/委托价）* 仓位张数 * 面值 / 保证金汇率
         * 预计盈亏（限价-空仓） =（1/开仓均价 - 1/委托价）* 仓位张数 * 面值 / 保证金汇率 * -1
         */
        BigDecimal resultBig = BigDecimal.ZERO;
        if (isForward) {
            if (isLimit) {
                //限价
                if (direction == 0) {
                    //多仓
                    BigDecimal buff = commissionPriceBig.subtract(openingPriceBig);
                    resultBig = buff.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
                } else {
                    //空仓
                    BigDecimal buff = commissionPriceBig.subtract(openingPriceBig);
                    resultBig = buff.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1"));
                }
            } else {
                //市价
                if (direction == 0) {
                    //多仓
                    BigDecimal buff = triggerPriceBig.subtract(openingPriceBig);
                    resultBig = buff.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
                } else {
                    //空仓
                    BigDecimal buff = triggerPriceBig.subtract(openingPriceBig);
                    resultBig = buff.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1"));
                }
            }
        } else {
            if (!isLimit) {
                if (triggerPriceBig.compareTo(BigDecimal.ZERO)==0){
                    return "0";
                }
                //限价
                if (direction == 0) {
                    //多仓
                    BigDecimal buff1 = BigDecimal.ONE.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff2 = BigDecimal.ONE.divide(triggerPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff3 = buff1.subtract(buff2);
                    resultBig = buff3.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
                } else {
                    //空仓
                    BigDecimal buff1 = BigDecimal.ONE.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff2 = BigDecimal.ONE.divide(triggerPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff3 = buff1.subtract(buff2);
                    resultBig = buff3.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1"));
                }
            } else {
                if (commissionPriceBig.compareTo(BigDecimal.ZERO)==0){
                    return "0";
                }
                //市价
                if (direction == 0) {
                    //多仓
                    BigDecimal buff1 = BigDecimal.ONE.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff2 = BigDecimal.ONE.divide(commissionPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff3 = buff1.subtract(buff2);
                    resultBig = buff3.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN);
                } else {
                    //空仓
                    BigDecimal buff1 = BigDecimal.ONE.divide(openingPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff2 = BigDecimal.ONE.divide(commissionPriceBig, scale, BigDecimal.ROUND_HALF_DOWN);
                    BigDecimal buff3 = buff1.subtract(buff2);
                    resultBig = buff3.multiply(positionAmountBig).multiply(parValueBig).divide(marginRateBig, scale, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal("-1"));
                }
            }
        }
        return resultBig.setScale(scale, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }

    public static String showDepthVolumeNew(String value) {
        if (!CpStringUtil.isNumeric(value))
            value = "0";

        String temp = new BigDecimal(value).toPlainString();
        if (compareTo(temp, "0.0001") < 0) {
            return "0.0000";
        } else if (compareTo(temp, "1000") >= 0) {
            return formatNumber(temp);
        } else {
            if (temp.contains(".")) {
                return (temp + "00000").substring(0, 6);
            } else {
                String substring = (temp + ".0000").substring(0, 4);
                if (substring.endsWith(".")) {
                    return substring.substring(0, 3);
                } else if (substring.endsWith(".0")||substring.endsWith(".00")){
                    return CpBigDecimalUtils.showSNormal(substring,0);
                }else {
                    return substring;
                }
            }
        }
    }

}
