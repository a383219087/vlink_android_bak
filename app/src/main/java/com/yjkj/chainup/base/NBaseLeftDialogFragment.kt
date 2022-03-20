package com.yjkj.chainup.base

import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.yjkj.chainup.R
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.SoftKeyboardUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class NBaseLeftDialogFragment() : DialogFragment(),View.OnClickListener {

    protected var layoutView: View? = null

    fun setTheme() {
        val window = this.dialog!!.window
        //去掉dialog默认的padding
        window!!.decorView.setPadding(0, 0, 0, 0)
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        //设置dialog的位置在底部
        lp.gravity = Gravity.BOTTOM
        //设置dialog的动画
        lp.windowAnimations = R.style.leftin_rightout_DialogFg_animstyle
        window.attributes = lp
        window.setBackgroundDrawable(ColorDrawable())
    }


    protected abstract fun setContentView(): Int

    protected fun <T : View?> findViewById(id: Int): T? {
        return layoutView?.findViewById<T>(id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBusUtil.register(this)
        hideKeyboard()
        initView()
    }

    protected abstract fun initView()

    protected abstract fun loadData()

    override fun onStart() {
        super.onStart()
    }

    /*
     * 处理线程跟发消息线程一致
     * 子类重载
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onMessageEvent(event: MessageEvent?) {
    }

    /*
     * 黏性事件处理
     * 子类重载处理完事件后需调用 EventBusUtil.removeStickyEvent(event);
     */
    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    fun onMessageStickyEvent(event: MessageEvent?) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBusUtil.unregister(this)
    }


    override fun onClick(v: View?) {
        dismissDialog()
    }

    open fun dismissDialog() {
        if (isVisible) {
            dismiss()
        }
    }

    override fun dismiss() {
        hideKeyboard()
        super.dismiss()
    }

    private fun hideKeyboard() {
        val view = dialog!!.currentFocus
        SoftKeyboardUtil.hideSoftKeyboard(view)
    }

    /*
     * 展示dialog
     */
    open fun showDialog(manager: FragmentManager?, tag: String?) {
        show(manager!!, tag)
    }

    /* access modifiers changed from: protected */
    fun doCoverViewShowAnimation(view: View?) {
        if (view != null) {
            ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(getDuration()).start()
        }
    }

    /* access modifiers changed from: protected */
    fun doCoverViewHideAnimation(view: View?) {
        if (view != null) {
            ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(getDuration()).start()
        }
    }

    /* access modifiers changed from: protected */
    fun getDuration(): Long {
        return 300
    }
}