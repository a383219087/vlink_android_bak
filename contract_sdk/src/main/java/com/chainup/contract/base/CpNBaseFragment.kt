package com.chainup.contract.base


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpForegroundCallbacks
import com.chainup.contract.listener.CpForegroundCallbacksListener
import com.chainup.contract.listener.CpForegroundCallbacksObserver
import com.chainup.contract.model.CpNewContractModel
import com.chainup.contract.utils.ChainUpLogUtil
import com.chainup.contract.view.CpNLoadingDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-07-31 11:56

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-07-31 11:56

 * @UpdateRemark:   更新说明

 */

abstract class CpNBaseFragment : Fragment() {

    val TAG = this::class.java.simpleName

    val MARKET_NAME = "market_name"
    val CUR_INDEX = "cur_index"
    val CUR_TYPE = "cur_type"


    var isFirstLoad = false

    protected var layoutView: View? = null

    protected var mActivity: Activity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (null == layoutView) {
            isFirstLoad = true

            layoutView = inflater.inflate(setContentView(), container, false)
            loadData()

        } else {
            var viewGroup = layoutView?.parent as ViewGroup?
            viewGroup?.removeView(layoutView)
        }
        return layoutView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CpEventBusUtil.register(this)
        initView()
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
            appBackGroundChange(visible)
        }
    }

    open fun background() {

    }

    open fun foreground() {

    }

    open fun appBackGroundChange(isVisible: Boolean) {

    }

    /**
     * 刷新接口
     */
    open fun refreshOkhttp(position: Int) {

    }

    private var isCanShowing = true
    override fun onResume() {
        super.onResume()
        isCanShowing = isVisible
        if (userVisibleHint) {
            fragmentVisibile(true)
        }
    }

    override fun onPause() {
        super.onPause()
        isFirstLoad = false
        if (userVisibleHint) {
            fragmentVisibile(false)
        }

        closeLoadingDialog()
        clearDisposable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CpEventBusUtil.unregister(this)
        CpForegroundCallbacksObserver.getInstance().removeListener(listener)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && null != layoutView && isResumed) {
            super.setUserVisibleHint(true)
        } else {
            super.setUserVisibleHint(false)
        }
        LogUtils.e("setUserVisibleHint"+userVisibleHint)
        fragmentVisibile(userVisibleHint)
        isCanShowing = isVisibleToUser
        onVisibleChanged(isVisibleOnScreen())
    }

    override fun onStop() {
        super.onStop()
        isCanShowing = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isCanShowing = !hidden
        fragmentVisibile(!hidden)

        onVisibleChanged(isVisibleOnScreen())
    }

    open fun onVisibleChanged(isVisible: Boolean) {
        if(isVisible){
            CpForegroundCallbacks.get().activityChange(this)
        }
    }

    /*
     * 此方法处理view显示，或view绑定数据
     */
    abstract fun initView()

    /*
     * 数据请求，建议重载保持代码风格统一
     */
    open fun loadData() {}

    /*
     * 加载布局文件
     */
    protected abstract fun setContentView(): Int

    /*
     * Fragment 的显示与隐藏
     * @param isVisibleToUser  true为可见，否则不可见
     */
    open var mIsVisibleToUser = false

    open fun fragmentVisibile(isVisibleToUser: Boolean) {
        mIsVisibleToUser = isVisibleToUser
    }

    /*
     * 处理线程跟发消息线程一致
     * 子类重载
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    open fun onMessageEvent(event: CpMessageEvent) {
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

    /*
    * 添加观察者
    */
    var disposablesTrade: CompositeDisposable? = null

    fun addDisposableTrade(disposable: Disposable?) {
        if (null == disposable)
            return
        if (disposablesTrade == null) {
            disposablesTrade = CompositeDisposable()
        }
        disposablesTrade!!.add(disposable)
    }

    /*
     *注销观察者，防止内存泄漏
     */
    fun clearDisposableTrade() {
        disposablesTrade?.clear()
        disposablesTrade = null
    }

    private var contractModel: CpNewContractModel? = null
    protected fun getContractModel() = contractModel ?: CpNewContractModel()


    private var mLoadingDialog: CpNLoadingDialog? = null
    protected fun showLoadingDialog(loadText: String = "") {
        if (mActivity != null && mActivity?.isFinishing == false) {
            if (null == mLoadingDialog) {
                mLoadingDialog = CpNLoadingDialog(
                    mActivity,
                    loadText
                )
            } else {
                mLoadingDialog?.setLoadText(loadText)
            }
            mLoadingDialog?.showLoadingDialog()
        }
    }

    protected fun closeLoadingDialog() {
        mLoadingDialog?.closeLoadingDialog()
        mLoadingDialog = null
    }

    fun isVisibleOnScreen(): Boolean {
        if (isCanShowing && userVisibleHint && isVisible) {
            val parentFragment = parentFragment
            if (parentFragment == null) {
                return true
            }

            if (parentFragment is CpNBaseFragment) {
                return parentFragment.isVisibleOnScreen()
            } else {
                return parentFragment.isVisible
            }
        }
        return false
    }

}