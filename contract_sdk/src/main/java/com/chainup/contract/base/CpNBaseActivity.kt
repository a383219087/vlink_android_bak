package com.chainup.contract.base

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpForegroundCallbacksListener
import com.chainup.contract.listener.CpForegroundCallbacksObserver
import com.chainup.contract.model.CpNewContractModel
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpLocalManageUtil
import com.chainup.contract.utils.CpNToastUtil
import com.chainup.contract.view.CpNLoadingDialog
import com.jaeger.library.StatusBarUtil
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Method

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-07-31 11:29

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-07-31 11:29

 * @UpdateRemark:   更新说明

 */

abstract class CpNBaseActivity : AppCompatActivity(), View.OnClickListener {

    open val TAG = this::class.java.simpleName

    val MARKET_NAME = "market_name"
    val CUR_INDEX = "cur_index"
    val CUR_TYPE = "cur_type"

    var mActivity: FragmentActivity = this
    var mContext: CpMyApp? = null

    var layoutView: View? = null
    var mInflater: LayoutInflater? = null
    var isLandscape = false

    // var mHeadView: HeadView?=null
    abstract fun setContentView(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CpEventBusUtil.register(this)
        mActivity = this
        if (Build.VERSION.SDK_INT >= 26) {
//            convertActivityFromTranslucent(mActivity)
        }
        mContext = mActivity.application as CpMyApp
        mInflater = LayoutInflater.from(this)
        setBarColor(CpClLogicContractSetting.getThemeMode(mContext))
//        if (!isLandscape) {
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }
        CpActivityCollector.addActivity(this, javaClass)
        onInit(savedInstanceState)
        setForegroundCallbacks()
    }

    fun setForegroundCallbacks() {
        CpForegroundCallbacksObserver.getInstance().addListener(listener)
    }

    var listener = object : CpForegroundCallbacksListener {
        override fun BackgroundListener() {
            background()
        }

        override fun ForegroundListener() {
            foreground()
        }

        override fun appBackChange(visible: Boolean) {

        }
    }

    /**
     * 进入后台
     */
    open fun background() {

    }

    /**
     * 进入前台
     */
    open fun foreground() {

    }


    open fun onInit(savedInstanceState: Bundle?) {
        layoutView = mInflater?.inflate(setContentView(), null)
        setContentView(layoutView)
        layoutView?.fitsSystemWindows = true
        // mHeadView = HeadView(layoutView)
    }

    /**
     * 处理 8.0以上系统 强制竖屏crash问题
     */
    fun convertActivityFromTranslucent(activity: Activity) {
        try {
            var method: Method = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
            method.isAccessible = true
            method.invoke(activity)
        } catch (t: Throwable) {
        }
    }


    /*
     * 为保持代码风格，子类涉及数据请求时最好重载
     */
    open fun loadData() {}

    /*
     * 为保持代码风格统一，建议子类重载
     */
    open fun initView() {

    }


    /*
     * 设置全屏
     */
    fun setFullScreen() {
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /*
     * 页面左上角返回按钮退出事件处理，子类重载即可
     */
    override fun onClick(view: View) {

    }

    /*
     * 处理线程跟发消息线程一致
     * 子类重载
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    open fun onMessageEvent(event: CpMessageEvent) {
        if (event.getMsg_type() == CpMessageEvent.data_req_error) {

        }
    }

    /*
     * 黏性事件处理
     * 子类重载处理完事件后需调用 EventBusUtil.removeStickyEvent(event);
     */
    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    open fun onMessageStickyEvent(event: CpMessageEvent) {

    }


    /*
     * 添加观察者
     */
    var disposables: CompositeDisposable? = null

    fun addDisposable(disposable: Disposable?) {
        if (null == disposable)
            return
        if (disposables == null) {
            disposables = CompositeDisposable()
        }
        disposables!!.add(disposable)
    }

    /*
     *注销观察者，防止内存泄漏
     */
    fun clearDisposable() {
        disposables?.clear()
        disposables = null
    }


    private var contractModel: CpNewContractModel? = null
    protected fun getContractModel() = contractModel ?: CpNewContractModel()



    private var mLoadingDialog: CpNLoadingDialog? = null
    protected fun showLoadingDialog() {
        if (null == mLoadingDialog) {
            mLoadingDialog =
                CpNLoadingDialog(mActivity)
        }
        try {
            mLoadingDialog?.showLoadingDialog()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun closeLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog?.closeLoadingDialog()
            mLoadingDialog = null
        }

    }

    fun showSnackBar(msg: String?, isSuc: Boolean = true) {
        CpNToastUtil.showTopToastNet(this,isSuc, msg)
    }


    /**
     * 页面从右面进入
     */
    private val WINDOW_EXIT_ANIM_MODE_RIGHT_OUT = 0x110
    private val WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT = 0x111
    private var currentWindowTransitionMode = -1

    protected fun windowTransitionRightInRightOut() {
        overridePendingTransition(R.anim.cp_slide_right_in, R.anim.cp_slide_left_out_v2)
        currentWindowTransitionMode = WINDOW_EXIT_ANIM_MODE_RIGHT_OUT
    }

    protected fun windowTransitionBottomInBottomOut() {
        overridePendingTransition(R.anim.cp_slide_down_in, R.anim.cp_activity_stay)
        currentWindowTransitionMode = WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT
    }

    override fun onPause() {
        super.onPause()
        closeLoadingDialog()
    }



    override fun finish() {
        super.finish()
        if (currentWindowTransitionMode == WINDOW_EXIT_ANIM_MODE_RIGHT_OUT) {
            overridePendingTransition(R.anim.cp_slide_left_in_v2, R.anim.cp_slide_right_out)
        } else if (currentWindowTransitionMode == WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT) {
            overridePendingTransition(0, R.anim.cp_slide_down_out)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        CpEventBusUtil.unregister(this)
        clearDisposable()
        closeLoadingDialog()
        CpForegroundCallbacksObserver.getInstance().removeListener(listener)
        CpActivityCollector.removeActivity(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CpLocalManageUtil.setLocal(newBase))
    }


    fun isNotEmpty(str: String?): Boolean {
        return !isEmpty(str)
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    /**
     * 设置状态栏的颜色
     * @param 0 是 白天模式，状态栏是白底黑字  1是夜间模式 状态栏是黑底白字
     */
    fun setBarColor(index: Int) {
        when (index) {
            0 -> {
                StatusBarUtil.setLightMode(this)
            }
            1 -> {
                StatusBarUtil.setDarkMode(this)
            }
        }
        StatusBarUtil.setColor(this,getResources().getColor(R.color.bg_color),0)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.getAction() == MotionEvent.ACTION_DOWN) {
            val v = getCurrentFocus()
            v?.let {
                if (isShouldHideKeyboard(it, ev)) {
                    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
                    it.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun isShouldHideKeyboard(v: View, event: MotionEvent): Boolean {
        if (v != null && (v is EditText)) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false
    }
}