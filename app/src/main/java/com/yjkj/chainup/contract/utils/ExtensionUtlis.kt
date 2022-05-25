package com.yjkj.chainup.contract.utils

import android.content.Context
import androidx.fragment.app.Fragment
import android.text.*
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.Contract
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.listener.SLDoListener
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.widget.ContractInputTextWatcher
import com.yjkj.chainup.util.LanguageUtil


/**
 * EditText设置编辑状态
 */
fun EditText.edit(edit: Boolean = true) {
    if (edit) {
        isFocusableInTouchMode = true
        isFocusable = true
        isEnabled = true
        requestFocus()
        if (!TextUtils.isEmpty(text)) {
            setSelection(text.length)
        }

    } else {
        isFocusable = false
        isFocusableInTouchMode = false
        isEnabled = false
        setText("")
        clearFocus()
    }
}

/**
 * 更新背景焦点,如果view为空，则修改自身背景
 */
fun EditText.updateFocusBgListener(pView: View?) {
    setOnFocusChangeListener { v, hasFocus ->
        val bgRes = if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused
        if (pView == null) {
            setBackgroundResource(bgRes)
        } else {
            pView.setBackgroundResource(bgRes)
        }
    }
}


/**
 * TextView动态设置文本
 * key string.xml中定义的key
 */
fun TextView.onLineText(key: String) {
    text = LanguageUtil.getString(context, key)
}

fun Context.getLineText(key: String): String {
    return LanguageUtil.getString(this, key)
}

fun Context.getLineText(key: String, isFormat: Boolean = false): String {
    if (isFormat) {
        if (!TextUtils.isEmpty(key)) {
            return LanguageUtil.getString(this, key).replace("\\n", "\n")
        }
    }
    return LanguageUtil.getString(this, key)
}

fun Fragment.getLineText(key: String): String {
    return LanguageUtil.getString(activity, key)
}

fun Fragment.getLineText(key: String, isFormat: Boolean = false): String {
    if (isFormat) {
        if (!TextUtils.isEmpty(key)) {
            return LanguageUtil.getString(activity, key).replace("\\n", "\n")
        }
    }
    return LanguageUtil.getString(activity, key)
}

/**
 * 本地语言
 */
fun String.localized(context: Context): String {
    return LanguageUtil.getString(context, this)
}

/**
 * 获取EditText的值，为空返回字符串0
 */
fun EditText.textNull2Zero(): String {
    return if (TextUtils.isEmpty(text.toString())) "0" else text.toString()
}

/**
 * EditText输入监听
 */
fun EditText.afterTextChanged(slDoListener: SLDoListener) {
    val editText = this
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            slDoListener.doThing(editText)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

/**
 * 同时限制小数位数和整数位数
 */
fun EditText.numberFilterContract(contract: Contract?, integer: Int = 9, otherFilter: SLDoListener? = null) {
    val unit: Int = LogicContractSetting.getContractUint(ContractSDKAgent.context)
    if (unit == 0) {
        //单位张
        numberFilterUnit(contract?.qty_unit ?: "0", integer, otherFilter)
    } else {
        //单位币
        numberFilterUnit(contract?.value_unit ?: "0.0001", integer, otherFilter)
    }
}

/**
 * 同时限制小数位数和整数位数
 */
fun EditText.numberFilterUnit(pxUnit: String, integer: Int = 9, otherFilter: SLDoListener? = null) {
    var decimal = 0
    if (pxUnit.isNotEmpty() && pxUnit.contains(".")) {
        val split = pxUnit.split(".")
        if (split.size == 2) {
            decimal = split[1].length
        }
    }
    numberFilter(decimal, integer, otherFilter)
}

/**
 * 同时限制小数位数和整数位数
 */
fun EditText.numberFilter(decimal: Int = 1, integer: Int = 9, otherFilter: SLDoListener? = null) {
    if (tag != null && tag is ContractInputTextWatcher) {
        val watcher = tag as ContractInputTextWatcher
        watcher.decimal = decimal
        watcher.integer = integer
        watcher.otherFilter = otherFilter
    } else {
        val textWatcher = ContractInputTextWatcher(this, decimal, integer)
        textWatcher.otherFilter = otherFilter
        addTextChangedListener(textWatcher)
        tag = textWatcher
    }
}

/**
 * View动画
 */
fun View.startResAnimation(animRes: Int) {
    if (visibility != View.VISIBLE) {
        return
    }
    val animationTag = getTag(animRes)
    val animation = if (animationTag != null && animationTag is Animation) {
        animationTag
    } else {
        val loadAnimation = AnimationUtils.loadAnimation(context, animRes)
        setTag(animRes, loadAnimation)
        loadAnimation
    }
    startAnimation(animation)
}