package com.yjkj.chainup.new_version.view

import android.content.Context
import android.graphics.Rect
import android.text.method.ReplacementTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.yjkj.chainup.R
import com.yjkj.chainup.util.ViewUtils


/**
 * @Author: Bertking
 * @Date：2019/3/6-2:39 PM
 * @Description: 自定义EditText
 */

class CustomizeEditText @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var textContent = ""

    /**
     *  仅仅为了兼容之前部分代码
     * TODO 优化
     */
    var isShowLine = false
        set(value) {
            field = value
            if (value) {
                setBackgroundResource(R.drawable.et_underline_selector)
            }
        }



    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        textContent = text.toString()
        setClearIconVisible(hasFocus() && text?.isNotEmpty() == true)
    }

    var focusedListener = false
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        focusedListener = focused
        if (focusedListener && textContent.isNotEmpty()) {
            if (isShowLine) {
                setBackgroundResource(R.drawable.et_underline_selector)
            }
            setClearIconVisible(true)
        } else {
            if (isShowLine) {
                setBackgroundResource(R.drawable.et_underline_selector)
            }
            setClearIconVisible(false)
        }
    }

    fun setMaxLeng(len:Int){
        ViewUtils.setEditTextLength(this,len);
    }

    /**
     * clear 事件
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val isClean = event.x > width - totalPaddingRight && event.x < width - paddingRight
            if (isClean) {
                setText("")
            }else{
                isFocusable = true
            }
        }
        return super.onTouchEvent(event)
    }


    private fun setClearIconVisible(visible: Boolean) {
        if (visible && focusedListener) {
            val clearImg = context.getDrawable(R.drawable.login_del)
            setCompoundDrawablesWithIntrinsicBounds(null, null, clearImg, null)
        } else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }


}

class TransInformation : ReplacementTransformationMethod() {
    /**
     * 原本输入的小写字母
     */
    override fun getOriginal(): CharArray {
        return charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
    }

    /**
     * 替代为大写字母
     */
    override fun getReplacement(): CharArray {
        return charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    }
}
