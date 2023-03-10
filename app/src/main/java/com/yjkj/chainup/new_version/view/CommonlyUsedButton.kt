package com.yjkj.chainup.new_version.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.wedegit.DisplayUtils
import kotlinx.android.synthetic.main.item_commonly_used_button.view.*
import org.jetbrains.anko.backgroundDrawable

/**
 * @Author lianshangljl
 * @Date 2019/3/7-2:14 PM
 * @Email buptjinlong@163.com
 * @description
 */
class CommonlyUsedButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    interface OnBottonListener {
        fun bottonOnClick()
    }
    interface OnBottonListener1 {
        fun bottonOnClick(v: View)
    }

    var listener: OnBottonListener? = null
    var listener1: OnBottonListener1? = null

    var normalBgColor = ContextCompat.getColor(context, R.color.main_blue)
        set(value) {
            field = value
            bg_color?.backgroundDrawable = getDrawable(true)
        }
    var textColor = ContextCompat.getColor(context, R.color.white)
    var noEnableColor = ContextCompat.getColor(context, R.color.btn_unclickable_color)
    var textSize: Float = 0.0f
    var textStyleBold: Boolean = false
    var isShowLoading: Boolean = true
    var normalEnable: Boolean = true
    var normalSelectEnable: Boolean = true
    var textContent = ""
        set(value) {
            field = value
            tv_complainCommand_content?.text = Html.fromHtml(textContent)
        }

    init {
        attrs.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonlyUsedButtonView)
            normalBgColor = typedArray.getColor(R.styleable.CommonlyUsedButtonView_bgColor, ContextCompat.getColor(context, R.color.main_blue))
            textColor = typedArray.getColor(R.styleable.CommonlyUsedButtonView_buttonTextColor, ContextCompat.getColor(context, R.color.white))
            textSize = typedArray.getDimension(R.styleable.CommonlyUsedButtonView_textSize, resources.getDimension(R.dimen.font_size_normal))
            textStyleBold = typedArray.getBoolean(R.styleable.CommonlyUsedButtonView_textStyleBold, false)
            isShowLoading = typedArray.getBoolean(R.styleable.CommonlyUsedButtonView_isShowLoading, true)
            normalEnable = typedArray.getBoolean(R.styleable.CommonlyUsedButtonView_normalEnable, true)
            normalSelectEnable = typedArray.getBoolean(R.styleable.CommonlyUsedButtonView_normalSelectEnable, true)
            textContent = typedArray.getString(R.styleable.CommonlyUsedButtonView_bottonTextContent).toString()
            typedArray.recycle()
        }
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_commonly_used_button, this, true)
        tv_complainCommand_content.text = textContent
        tv_complainCommand_content.setTextColor(textColor)
        bg_color.setBackgroundColor(normalBgColor)

        val bottonlistener = BottonListener()
        rl_layout.setOnTouchListener(bottonlistener)
        rl_layout.setOnClickListener(bottonlistener)
        if (!normalEnable) {
            bg_color.backgroundDrawable = getDrawable(false)
            rl_layout.backgroundDrawable = getMainLayoutDrawable(false)
        }
        isEnabled = normalSelectEnable
        rl_layout.setBackgroundColor(R.drawable.bg_new_provisions)
    }

    fun setBottomTextContent(contentId: String) {
        tv_complainCommand_content?.text = contentId
    }

    var clicked = false
    /**
     * ???????????????
     *  @param status true ????????? false ????????????
     */
    fun isEnable(status: Boolean) {
        if (status) {
            bg_color.backgroundDrawable = getDrawable(true)
            rl_layout.backgroundDrawable = getMainLayoutDrawable(true)

        } else {
            bg_color.backgroundDrawable = getDrawable(false)
            rl_layout.backgroundDrawable = getMainLayoutDrawable(true)
        }
        isEnabled = status
        clicked = status
    }

    fun setEnable(status: Boolean) {
        isEnabled = status
        clicked = status
    }

    fun getDrawable(isEnable: Boolean): GradientDrawable {
        val normalDrawable = GradientDrawable()
        normalDrawable.cornerRadius = DisplayUtils.dip2px(context, 4f).toFloat()
        normalDrawable.shape = GradientDrawable.RECTANGLE
        if (isEnable) {
            normalDrawable.setColor(normalBgColor)
        } else {
            normalDrawable.setColor(noEnableColor)
        }
        return normalDrawable
    }

    fun getMainLayoutDrawable(isEnable: Boolean): GradientDrawable {
        val normalDrawable = GradientDrawable()
        normalDrawable.cornerRadius = DisplayUtils.dip2px(context, 4f).toFloat()
        normalDrawable.shape = GradientDrawable.RECTANGLE
        if (isEnable) {
            normalDrawable.setColor(normalBgColor)
        } else {
            normalDrawable.setColor(noEnableColor)
        }
        return normalDrawable
    }

    fun setContent(content: String) {
        tv_complainCommand_content?.text = content
    }

    /**
     * ??????loading
     */
    fun showLoading() {
        tv_complainCommand_content?.visibility = View.GONE
        pb_view.visibility = View.VISIBLE
    }

    /**
     * ??????loading
     */
    fun hideLoading() {
        tv_complainCommand_content?.visibility = View.VISIBLE
        pb_view.visibility = View.GONE
    }


    fun setDownViewVisible(status: Boolean) {
        if (status) {
            down_view.visibility = View.VISIBLE
        } else {
            down_view.visibility = View.GONE

        }
    }


    inner class BottonListener : OnTouchListener, OnClickListener {
        override fun onClick(v: View?) {
            if (listener != null && clicked) {
                if (!Utils.isFastClick()) {
                    listener?.bottonOnClick()
                }
            }
            if (listener1 != null && clicked) {
                if (!Utils.isFastClick()) {
                    listener1?.bottonOnClick(v!!)
                }
            }

        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (clicked) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        setDownViewVisible(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        setDownViewVisible(false)
                    }
                }
            }
            return false
        }

    }



}