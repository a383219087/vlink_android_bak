package com.yjkj.chainup.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.common.sdk.utlis.TimeFormatUtils


object AppUtils {

    /**
     * 获取应用程序名称
     */
    @Synchronized
    fun getAppName(context: Context): String? {
        try {
            val packageManager = context.getPackageManager()
            val packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0)
            val labelRes = packageInfo.applicationInfo.labelRes
            return context.getResources().getString(labelRes)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getVersionName(context: Context): String? {
        try {
            val packageManager = context.getPackageManager()
            val packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0)
            return packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getVersionCode(context: Context): Int {
        try {
            val packageManager = context.getPackageManager()
            val packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0)
            return packageInfo.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }


    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    @Synchronized
    fun getPackageName(context: Context): String? {
        try {
            val packageManager = context.getPackageManager()
            val packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0)
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Synchronized
    fun getAPKPackageName(context: Context, apkPath: String): String? {
        val pm = context.packageManager
        val info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES)
        if (info != null) {
            val appInfo = info.applicationInfo
            return appInfo.packageName
        }
        return null
    }




    @JvmStatic
    fun main(args: Array<String>) {
        println("TimeFormatUtils.timeStampToDate(1608992150000, \"MM-dd  HH:mm\") = ${TimeFormatUtils.timeStampToDate("1608992150000".toLong(), "MM-dd  HH:mm")}")
    }

}