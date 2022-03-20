package com.yjkj.chainup.wedegit

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.androidadvance.topsnackbar.TSnackbar
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.util.toViewDp

object SnackLayout {

    fun showErrorToast(activity: Activity?, message: String, action: (() -> Unit)? = null) {
        show(
                activity,
                ContextCompat.getColor(ChainUpApp.appContext, R.color.red),
                message,
                action
        )
    }

    fun showSuccessToast(activity: Activity?, message: String, action: (() -> Unit)? = null) {
        show(
                activity,
                ContextCompat.getColor(ChainUpApp.appContext, R.color.base_green),
                message,
                action
        )
    }

    fun showSnackBar(view: View?, message: String?, isSuc: Boolean = true, action: (() -> Unit)? = null) {
        if (message.isNullOrEmpty() || "网络异常".equals(message, ignoreCase = true)) return
        val backgroundColor = when (isSuc) {
            true -> ContextCompat.getColor(ChainUpApp.appContext, R.color.base_green)
            else -> ContextCompat.getColor(ChainUpApp.appContext, R.color.red)
        }
        if (view != null) {
            showView(view, backgroundColor, message, action)
        } else {
            ToastLayout.Builder()
                    .setBackgroundColor(backgroundColor)
                    .setMessage(message)
                    .setOnDismissListener(action)
                    .show()
        }
    }

    fun showNormalToast(activity: Activity?, message: String, action: (() -> Unit)? = null) {
        show(
                activity,
                ContextCompat.getColor(ChainUpApp.appContext, R.color.base_translucence),
                message,
                action
        )
    }

    private fun show(
            activity: Activity?,
            @ColorInt backgroundColor: Int,
            message: String,
            action: (() -> Unit)? = null
    ) {
        val decorView = activity?.window?.decorView
        if (decorView != null) {
            showView(decorView, backgroundColor, message, action)
        } else {
            ToastLayout.Builder()
                    .setBackgroundColor(backgroundColor)
                    .setMessage(message)
                    .setOnDismissListener(action)
                    .show()
        }
    }

    private fun showView(decorView: View, @ColorInt backgroundColor: Int,
                         message: String,
                         action: (() -> Unit)? = null
    ) {
        try {
            val snackBar = TSnackbar.make(decorView, message, TSnackbar.LENGTH_SHORT)
            val snackBarView = snackBar.view
            val layoutParams = ViewGroup.MarginLayoutParams(
                    MATCH_PARENT,
                    message.toViewDp()
            )
            snackBarView.layoutParams = layoutParams
            snackBarView.setBackgroundColor(backgroundColor)

            snackBarView.findViewById<TextView>(com.androidadvance.topsnackbar.R.id.snackbar_text)
                    .buildView(message.length)

            snackBar.show()

            action?.let {
                snackBar.setCallback(object : TSnackbar.Callback() {
                    override fun onDismissed(snackbar: TSnackbar?, event: Int) {
                        it.invoke()
                    }
                })
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun TextView.buildView(length: Int) {
        if (this.tag != "initialized") {
            this.setTextColor(Color.WHITE)
            this.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            this.textSize = 14F
            this.setPadding(0, 0, 0, this.context.resources.getDimensionPixelSize(R.dimen.dp_12))
            val dpTop =
                    this.context.resources.getDimensionPixelSize(if (length > 20) R.dimen.dp_zero else R.dimen.dp_1)
            val layouts = this.layoutParams as ViewGroup.MarginLayoutParams
            layouts.topMargin = dpTop
            this.layoutParams.width = MATCH_PARENT
            this.layoutParams.height = MATCH_PARENT
            this.layoutParams = layouts
            this.tag = "initialized"
        }
    }
}