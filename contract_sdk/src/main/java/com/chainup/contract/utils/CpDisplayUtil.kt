package com.chainup.contract.utils

import android.content.Context
import android.view.View
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.view.CpSnackLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

/**
 * @Author: Bertking
 * @Date：2019/3/6-11:53 AM
 * @Description: 获取屏幕信息的工具类
 * <p>至于 dp & sp的互转，Anko 库已经内置dip() & sp()  </p>
 * <link>https://github.com/Kotlin/anko/wiki/Anko-Commons-%E2%80%93-Misc#dimensions</link>
 */

object CpDisplayUtil {
    /**
     * @return 屏幕宽度
     */
    fun getScreenWidth(context: Context = CpMyApp.instance()): Int = context.resources.displayMetrics.widthPixels

    /**
     * @return 屏幕高度
     */
    fun getScreenHeight(context: Context = CpMyApp.instance()): Int = context.resources.displayMetrics.heightPixels

    /**
     * @return 分辨率
     */
    fun getDisplayDensity(context: Context = CpMyApp.instance()): Float = context.resources.displayMetrics.density


    /**
     * @param view
     * @param text
     * @param isSuc 是否是成功的状态
     */
    fun showSnackBar(view: View?, text: String?, isSuc: Boolean = true) {
        CpSnackLayout.showSnackBar(view, text, isSuc)
    }

    fun dip2px(int: Int): Int {
        return CpMyApp.instance().dip(int)
    }


    fun sp2px(int: Int): Int {
        return CpMyApp.instance().sp(int)
    }


    fun dip2px(float: Float): Float {
        return CpMyApp.instance().dip(float).toFloat()
    }


    fun sp2px(float: Float): Float {
        return CpMyApp.instance().sp(float).toFloat()
    }
}