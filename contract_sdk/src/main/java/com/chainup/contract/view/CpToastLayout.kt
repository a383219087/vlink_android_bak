package com.chainup.contract.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.androidadvance.topsnackbar.TSnackbar
import com.blankj.utilcode.util.BarUtils
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.utils.toPx
import kotlinx.android.synthetic.main.cp_base_toast_top_style.view.*
import org.jetbrains.anko.singleLine


@SuppressLint("StaticFieldLeak")
object CpToastLayout {
    private var toast: Toast? = null
    private var mColor = -1

    private var actionCountDownTimer: CountDownTimer? = null

    /**
     * 设置吐司背景色 默认是橘黄色
     * color 颜色资源id
     */
    fun setBgColor(@ColorRes color: Int) {
        mColor = color

    }

    fun showToast(message: String) {
        show(ContextCompat.getColor(CpMyApp.instance(), mColor), message, Toast.LENGTH_LONG)
    }


    fun showErrorToast(message: String) {
        show(
            ContextCompat.getColor(CpMyApp.instance(), R.color.red),
            message,
            Toast.LENGTH_LONG
        )
    }

    fun showSuccessToast(message: String) {
        show(
            ContextCompat.getColor(CpMyApp.instance(), R.color.base_green),
            message,
            Toast.LENGTH_LONG
        )
    }

    fun showNormalToast(message: String) {
        show(
            ContextCompat.getColor(CpMyApp.instance(), R.color.base_translucence),
            message,
            Toast.LENGTH_LONG
        )
    }


    private fun show(
        backgroundColor: Int,
        message: String,
        duration: Int,
        onDismiss: (() -> Unit)? = null
    ) {
        //加载Toast布局
        val toastRoot = View.inflate(CpMyApp.instance(), R.layout.cp_base_toast_top_style, null)

        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        toastRoot.systemUiVisibility = uiOptions

        toastRoot.setBackgroundColor(backgroundColor)

        //设置显示内容
        toastRoot.tv_toast_content.text = message

        //设置高度
        val params = toastRoot.rl_toast_bg.layoutParams as LinearLayout.LayoutParams
        params.height = 44.toPx() + BarUtils.getStatusBarHeight()
        toastRoot.layoutParams = params

        // 写死的等待时间
        val sleepDuration = when (duration) {
            Toast.LENGTH_LONG -> 400L
            else -> 0L
        }
        onDismiss?.let {
            actionCountDownTimer?.cancel()
            actionCountDownTimer = object : CountDownTimer(sleepDuration, sleepDuration) {
                override fun onFinish() {
                    it.invoke()
                    actionCountDownTimer = null
                }

                override fun onTick(millisUntilFinished: Long) = Unit
            }
            actionCountDownTimer?.start()
        }

//        if (toast == null) {
        toast = Toast(CpMyApp.instance())
//        }
        toast?.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
        toast?.duration = duration
        toast?.view = toastRoot
        toast?.show()
    }


    class Builder {
        @ColorInt
        private var color: Int =
            ContextCompat.getColor(CpMyApp.instance(), R.color.red)

        private var message: String = ""

        private var duration = Toast.LENGTH_LONG

        private var onDismissListener: (() -> Unit)? = null

        fun setBackgroundColor(@ColorInt color: Int): Builder {
            this.color = color
            return this
        }

        fun setDuration(duration: Int): Builder {
            if (duration != Toast.LENGTH_LONG
                || duration != Toast.LENGTH_SHORT
            ) {
                this.duration = Toast.LENGTH_LONG
            } else {
                this.duration = duration
            }
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setOnDismissListener(onDismissListener: (() -> Unit)? = null): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun show() {
            show(color, message, duration, onDismissListener)
        }
    }


    fun showSnackBar(view: View?, text: String?, isSuc: Boolean = true) {
        if (view != null) {
            try {
                val snackBar = TSnackbar
                    .make(view, text ?: "", TSnackbar.LENGTH_SHORT)
                val snackBarView = snackBar.view
                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    80.toPx()
                )
                snackBarView.layoutParams = layoutParams
                snackBarView.setBackgroundColor(Color.GRAY)

                val textView: TextView =
                    snackBarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
                textView.minLines = 3
                textView.singleLine = false
                textView.setTextColor(Color.WHITE)
                textView.gravity = Gravity.CENTER
                textView.textSize = 14f
                snackBar.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}