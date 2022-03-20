package com.chainup.contract.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CpTimeFormatUtils {
    public static final String TIME_FORMAT_DATE = "yyyy/MM/dd";
    public static final String FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND = "yyyy-MM-dd HH:mm:ss";

    public CpTimeFormatUtils() {
    }

    public static String convertUtcTimeToDate(String time) {
        return convertUTCTime(time, "yyyy/MM/dd");
    }

    public static String convertUtcTimeToDate(String time, String format) {
        return convertUTCTime(time, format);
    }

    public static long convertUtcTimeToMillis(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public static long getUtcTimeToMillis(String timeStr) {
        try {
            CpDateTime dateTime = CpDateTime.parseRfc3339(timeStr);
            long millis = dateTime.getValue();
            return millis;
        } catch (Exception var4) {
            var4.printStackTrace();
            return 0L;
        }
    }

    public static String convertUTCTime(String time, String outFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date date = sdf.parse(time);
            DateFormat gmtFormat = new SimpleDateFormat(outFormat);
            return gmtFormat.format(date);
        } catch (Exception var5) {
            var5.printStackTrace();
            return time;
        }
    }

    public static String convertZTime(String time, String outFormat) {
        if (TextUtils.isEmpty(time)) {
            return "--";
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                time = time.substring(0, time.lastIndexOf(".")) + "Z";
                Date date = sdf.parse(time);
                DateFormat gmtFormat = new SimpleDateFormat(outFormat);
                return gmtFormat.format(date);
            } catch (Exception var5) {
                var5.printStackTrace();
                return time;
            }
        }
    }

    public static String timeStampToDate(long timeStamp, String outFormat) {
        try {
            if ((timeStamp + "").trim().length() == 10) {
                timeStamp *= 1000L;
            }

            Date date = new Date(timeStamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(outFormat);
            String dateStr = simpleDateFormat.format(date);
            return dateStr;
        } catch (Exception var6) {
            return "";
        }
    }

    public static String timeStampToDate(long timeStamp) {
        if ((timeStamp + "").trim().length() == 10) {
            timeStamp *= 1000L;
        }

        Date date = new Date(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = simpleDateFormat.format(date);
        return dateStr;
    }

    public static String getCurrDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(5);
        return day + "";
    }

    public static int getCurYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(1);
        return year;
    }

    public static String getCurrMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(2) + 1;
        return month + "";
    }

    public static String getCurrYearAndMonthAndDay() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(2) + 1;
        int day = calendar.get(5);
        int year = calendar.get(1);
        return year + "-" + month + "-" + day;
    }

    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();

        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException var5) {
            var5.printStackTrace();
        }

        return date.getTime();
    }
}
