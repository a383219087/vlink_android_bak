package com.yjkj.chainup.contract.widget

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.yjkj.chainup.contract.listener.SLDoListener

/**
 * @author ZhongWei
 * @time 2020/8/19 19:07
 * @description EditText同时限制输入整数和小数的位数，互不干扰
 **/
class ContractInputTextWatcher(private val editText: EditText, var decimal: Int, var integer: Int) : TextWatcher {

    private var beforeText = ""

    var otherFilter: SLDoListener? = null

    override fun afterTextChanged(s: Editable?) {
        val text = editText.text.toString()
        if (text.startsWith(".")) {
            if(decimal == 0){
                editText.setText("")
                return
            }
            editText.setText("0.")
            editText.setSelection(2)
            return
        } else if (text.contains(".")) {
            if(decimal == 0){
                var replace = text.replace(".", "")
                if(replace.length > integer){
                    replace = replace.substring(0,integer)
                }
                editText.setText(replace)
                editText.setSelection(replace.length)
                return
            }
            val split = text.split(".")
            if (split[0].length > integer) {
                editText.setText(beforeText)
                editText.setSelection(beforeText.length - 1)
                return
            }
            if (split.size == 2 && split[1].length > decimal) {
                editText.setText(beforeText)
                editText.setSelection(beforeText.length - 1)
                return
            }
        } else {
            if (text.length > integer) {
                editText.setText(beforeText)
                editText.setSelection(beforeText.length - 1)
                return
            }
        }
        otherFilter?.doThing(editText)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        beforeText = editText.text.toString()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

}