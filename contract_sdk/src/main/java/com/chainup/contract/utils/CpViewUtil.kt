package com.chainup.contract.utils

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import java.lang.reflect.Field
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import com.androidadvance.topsnackbar.TSnackbar
import com.chainup.contract.R
import com.google.android.material.tabs.TabLayout


object CpViewUtil {

    fun pxToDp(px: Float): Float {
        val densityDpi = Resources.getSystem().displayMetrics.densityDpi.toFloat()
        return px / (densityDpi / 160f)
    }

    fun dpToPx(dp: Float): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    fun spToPx(sp: Float): Float {
        val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
        return sp * scaledDensity + 0.5f
    }

    fun screenWidth() = Resources.getSystem().displayMetrics.widthPixels

    fun screenHeight() = Resources.getSystem().displayMetrics.heightPixels

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

    fun showKeyBoard(activity: Activity, editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    fun shakeView(view: View) {
        if (view == null) return
        var transXValueHolder = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(0.25f, 20f),
                Keyframe.ofFloat(0.5f, 0f),
                Keyframe.ofFloat(0.75f, -20f),
                Keyframe.ofFloat(1f, 0f)
        )
        var objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, transXValueHolder)
        objectAnimator.duration = 200
        objectAnimator.repeatCount = 3
        objectAnimator.start()

    }

    /****
     * if you have your owen title bar , you have to use CoordinatorLayout
     * wrap your messageContent view other wise snackbar view show below statusbar
     */
    fun showErrorSnackbar(view: View, text: String, showLength: Int): TSnackbar {
        var tSnackbar = TSnackbar.make(view, text, showLength)
        var snackbarView = tSnackbar.view
        snackbarView.setBackgroundColor(Color.argb(220, 245, 98, 98))
        val textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setTextColor(Color.WHITE)
        tSnackbar.show()
        return tSnackbar
    }

    /****
     * if you have your owen title bar , you have to use CoordinatorLayout
     * wrap your messageContent view other wise snackbar view show below statusbar
     */
    fun showSuccesSnackbar(view: View, text: String, showLength: Int): TSnackbar {
        var tSnackbar = TSnackbar.make(view, text, showLength)
        var snackbarView = tSnackbar.view
        snackbarView.setBackgroundColor(CpColorUtil.getColor(R.color.base_green))
        val textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setTextColor(Color.WHITE)
        tSnackbar.show()
        return tSnackbar
    }

    fun getTextSpan(text: CharSequence, startIndex: Int, endIndex: Int, textColor: Int, textSize: Int): CharSequence? {

        val spannableStringBuilder = SpannableStringBuilder(text)
        val span = ForegroundColorSpan(textColor)
        spannableStringBuilder.setSpan(span, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

        val sizeSpan = AbsoluteSizeSpan(textSize, true)
        spannableStringBuilder.setSpan(sizeSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

        return spannableStringBuilder
    }
    fun getTextSpanCoinLock(text: CharSequence, startIndex: Int, endIndex: Int, textColor: Int,endColor: Int): CharSequence? {

        val spannableStringBuilder = SpannableStringBuilder(text)
        val span = ForegroundColorSpan(textColor)
        spannableStringBuilder.setSpan(span, 0, startIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

        val spanColor = ForegroundColorSpan(endColor)
        var styleSpan =  StyleSpan(Typeface.BOLD)
        spannableStringBuilder.setSpan(styleSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannableStringBuilder.setSpan(spanColor, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

        return spannableStringBuilder
    }

    fun getIndicatorOvalDrawable(context: Context, selectColor: Int, unSelectColor: Int, radius: Int = CpViewUtil.dpToPx(12f), margin: Int = CpViewUtil.dpToPx(8f)): ImageView {
        return ImageView(context)?.apply {
            var drawable = StateListDrawable()?.apply {
                var drawableSelect = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(selectColor)
                }
                addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
                var drawableUnSelect = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(unSelectColor)
                }
                addState(IntArray(1), drawableUnSelect)
            }
            setImageDrawable(drawable)
            var params = LinearLayout.LayoutParams(radius, radius)
            params.leftMargin = margin
            params.rightMargin = margin
            layoutParams = params
        }
    }

    fun setUpIndicatorWidth(tabLayout: TabLayout, marginLeft: Int, marginRight: Int) {
        val tabLayoutClass = tabLayout.javaClass
        var tabStrip: Field? = null
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip")
            tabStrip!!.isAccessible = true
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        var layout: LinearLayout? = null
        try {
            if (tabStrip != null) {
                layout = tabStrip.get(tabLayout) as LinearLayout
            }
            for (i in 0 until layout!!.childCount) {
                val child = layout.getChildAt(i)
                child.setPadding(0, 0, 0, 0)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.marginStart = dpToPx(marginLeft.toFloat())
                    params.marginEnd = dpToPx(marginRight.toFloat())
                }
                child.layoutParams = params
                child.invalidate()
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * ??????TabLayout???????????????.
     */
    fun reflex(tabLayout: TabLayout) {
        //?????????????????? ????????????????????? tabView?????????????????????
        tabLayout.post {
            try {
                //??????tabLayout???mTabStrip??????
                val mTabStrip = tabLayout.getChildAt(0) as LinearLayout

                val dp10 = dpToPx(10.0f)

                for (i in 0 until mTabStrip.childCount) {
                    val tabView = mTabStrip.getChildAt(i)

                    //??????tabView???mTextView??????  tab????????????????????????????????????mTextView
                    val mTextViewField = tabView.javaClass.getDeclaredField("mTextView")
                    mTextViewField.isAccessible = true

                    val mTextView = mTextViewField.get(tabView) as TextView

                    tabView.setPadding(0, 0, 0, 0)

                    //???????????????????????????   ????????????????????????????????????mTextView?????????
                    var width = 0
                    width = mTextView.width
                    if (width == 0) {
                        mTextView.measure(0, 0)
                        width = mTextView.measuredWidth
                    }

                    //??????tab???????????????10dp  ????????????????????????Padding ???????????????????????????????????? tabView?????????????????????
                    val params = tabView.layoutParams as LinearLayout.LayoutParams
                    params.width = width
                    params.leftMargin = dp10
                    params.rightMargin = dp10
                    tabView.layoutParams = params

                    tabView.invalidate()
                }

            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    fun setBgColor(view: View, @ColorRes id: Int) {
        view.setBackgroundColor(CpColorUtil.getColor(id))
    }

    fun setTxColor(view: TextView, @ColorRes id: Int) {
        view.setTextColor(CpColorUtil.getColor(id))
    }



    /**
     * ????????????????????????
     */
    fun copy(string: String, context: Context) {
        var cmb: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cmb.text = string.trim()
    }

    /**
     * ????????????????????????
     */
    fun paste(context: Context): String {
        var cmb: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return cmb.text.toString().trim()
    }
}
