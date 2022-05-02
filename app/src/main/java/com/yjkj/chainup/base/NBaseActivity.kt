package com.yjkj.chainup.base

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.eventbus.CpMessageEvent
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.push.ActivityCollector
import com.yjkj.chainup.model.model.*
import com.yjkj.chainup.net_new.NLoadingDialog
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.activity.SplashActivity
import com.yjkj.chainup.new_version.view.ForegroundCallbacksListener
import com.yjkj.chainup.new_version.view.ForegroundCallbacksObserver
import com.yjkj.chainup.util.LocalManageUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
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

abstract class NBaseActivity : AppCompatActivity(), View.OnClickListener {

    open val TAG = this::class.java.simpleName

    val MARKET_NAME = "market_name"
    val CUR_INDEX = "cur_index"
    val CUR_TYPE = "cur_type"

    var mActivity: FragmentActivity = this
    var mContext: ChainUpApp? = null

    var layoutView: View? = null
    var mInflater: LayoutInflater? = null
    var isLandscape = false


    // var mHeadView: HeadView?=null
    abstract fun setContentView(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        if (this.javaClass.simpleName != SplashActivity::class.simpleName && this.javaClass.simpleName != NewMainActivity::class.simpleName ) {
            ARouter.getInstance().inject(this)
        }
        EventBusUtil.register(this)
        mActivity = this
        if (Build.VERSION.SDK_INT >= 26) {
//            convertActivityFromTranslucent(mActivity)
        }
        mContext = mActivity.application as ChainUpApp
        mInflater = LayoutInflater.from(this)
        setBarColor(PublicInfoDataService.getInstance().themeMode)
        if (!isLandscape) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        ActivityCollector.addActivity(this, javaClass)
        onInit(savedInstanceState)
        setForegroundCallbacks()
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        Log.e(TAG, "切换黑白版 ${this}")
    }
    fun setForegroundCallbacks() {
        ForegroundCallbacksObserver.getInstance().addListener(listener)
    }

    var listener = object : ForegroundCallbacksListener {
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
    open fun onMessageEvent(event: MessageEvent) {
        if (event.getMsg_type() == MessageEvent.data_req_error) {

        }
    }
    /*
        * 处理线程跟发消息线程一致
        * 子类重载
        */
    @Subscribe(threadMode = ThreadMode.POSTING)
    open fun onCpMessageEvent(event: CpMessageEvent) {

    }
    /*
     * 黏性事件处理
     * 子类重载处理完事件后需调用 EventBusUtil.removeStickyEvent(event);
     */
    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    open fun onMessageStickyEvent(event: MessageEvent) {

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
    /**
     * 启动网络任务
     */
    fun <D> startTask(single: Observable<D>, onNext: Consumer<in D>, onError: Consumer<in Throwable>) {
        if (disposables == null) {
            disposables = CompositeDisposable()
        }
        disposables!!.add(
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError)
        )
    }

    /*
     *注销观察者，防止内存泄漏
     */
    fun clearDisposable() {
        disposables?.clear()
        disposables?.dispose()
        disposables = null
    }

    private var mainModel: MainModel? = null
    protected fun getMainModel() = mainModel ?: MainModel()



    private var redPackageModel: RedPackageModel? = null
    protected fun getRedPackageModel() = redPackageModel ?: RedPackageModel()

    private var otcModel: OTCModel? = null
    protected fun getOTCModel() = otcModel ?: OTCModel()

    private var assetModel: AssetModel? = null
    protected fun getAssetModel() = assetModel ?: AssetModel()


    private var contractModelNet: ContractModel? = null
    protected fun getContractModelOld() = contractModelNet ?: ContractModel()

    private var contractModel: NewContractModel? = null
    protected fun getContractModel() = contractModel ?: NewContractModel()



    private var mLoadingDialog: NLoadingDialog? = null
    protected fun showLoadingDialog() {
        if (null == mLoadingDialog) {
            mLoadingDialog = NLoadingDialog(mActivity)
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
        NToastUtil.showTopToastNet(this, isSuc, msg)
    }


    /**
     * 页面从右面进入
     */
    private val WINDOW_EXIT_ANIM_MODE_RIGHT_OUT = 0x110
    private val WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT = 0x111
    private var currentWindowTransitionMode = -1

    protected fun windowTransitionRightInRightOut() {
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
        currentWindowTransitionMode = WINDOW_EXIT_ANIM_MODE_RIGHT_OUT
    }

    protected fun windowTransitionBottomInBottomOut() {
        overridePendingTransition(R.anim.slide_down_in, R.anim.activity_stay)
        currentWindowTransitionMode = WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT
    }

    override fun onPause() {
        super.onPause()
        closeLoadingDialog()
    }

    override fun finish() {
        super.finish()
        if (currentWindowTransitionMode == WINDOW_EXIT_ANIM_MODE_RIGHT_OUT) {
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out)
        } else if (currentWindowTransitionMode == WINDOW_EXIT_ANIM_MODE_BOTTOM_OUT) {
            overridePendingTransition(0, R.anim.slide_down_out)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        EventBusUtil.unregister(this)
        clearDisposable()
        closeLoadingDialog()
        ForegroundCallbacksObserver.getInstance().removeListener(listener)
        ActivityCollector.removeActivity(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase))
    }


    fun isNotEmpty(str: String?): Boolean {
        return !isEmpty(str)
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }
    fun toRequestBody(params: Map<String, String>): RequestBody {
        return JSONObject(params).toString().toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
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
    }

}