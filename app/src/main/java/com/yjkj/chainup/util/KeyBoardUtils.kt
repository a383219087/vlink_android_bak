package com.yjkj.chainup.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @Author: Bertking
 * @Date：2019-06-26-16:10
 * @Description:
 */
object KeyBoardUtils {
    /**
     * 不适用Dialog
     */
    fun closeKeyBoard(context: Context) {
        // 关闭键盘
        val inputManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow((context as Activity)?.window?.decorView?.windowToken, 0)
    }

    fun showKeyBoard(context: Context) {
        // 关闭键盘
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    fun showKeyBoard(context: Context, editText: EditText) {
        // 关闭键盘
         try {
             val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
             inputManager.showSoftInput(editText,InputMethodManager.SHOW_FORCED)
         }catch (e:java.lang.Exception){

         }

    }
}