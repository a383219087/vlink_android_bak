package com.yjkj.chainup.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity

import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.model.model.ContractModel
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.model.model.NewContractModel
import com.yjkj.chainup.model.model.OTCModel
import com.yjkj.chainup.net.NLoadingDialog
import com.yjkj.chainup.new_version.view.ForegroundCallbacksListener
import com.yjkj.chainup.new_version.view.ForegroundCallbacksObserver
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.wedegit.ForegroundCallbacks
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

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-07-31 11:56

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-07-31 11:56

 * @UpdateRemark:   更新说明

 */

abstract class NBaseFragment : Fragment() {

    val TAG = this::class.java.simpleName

    val MARKET_NAME = "market_name"
    val CUR_INDEX = "cur_index"
    val CUR_TYPE = "cur_type"


    var isFirstLoad = false

    protected var layoutView: View? = null

    protected var mActivity: FragmentActivity? = null
    protected var mContext: ChainUpApp? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as FragmentActivity
        mContext = mActivity?.application as ChainUpApp
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
        EventBusUtil.register(this)
        initView()
        setForegroundCallbacks()
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
            /*var fgs = fragmentManager?.fragments
            LogUtil.d("setOnScrowListener", "NBaseFragment==onResume==userVisibleHint is $userVisibleHint，isVisible is $isVisible,fgs is $fgs")
            if(null!=fgs){
                for(i in 0 until fgs.size  ){
                    var fg = fgs[i]
                    LogUtil.d("setOnScrowListener", "NBaseFragment==onResume==userVisibleHint is $userVisibleHint，isVisible is $isVisible,fg is $fg,this is $this")

                    if(null!=fg && fg == this){

                    }
                }
            }*/
            //
        }
    }

    override fun onPause() {
        super.onPause()
        isFirstLoad = false
        if (userVisibleHint) {
            LogUtil.d("setOnScrowListener", "NBaseFragment==onPause==userVisibleHint is $userVisibleHint")
            fragmentVisibile(false)
        }

        closeLoadingDialog()
        clearDisposable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBusUtil.unregister(this)
        ForegroundCallbacksObserver.getInstance().removeListener(listener)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && null != layoutView && isResumed) {
            super.setUserVisibleHint(true)
        } else {
            super.setUserVisibleHint(false)
        }
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
            ForegroundCallbacks.get().activityChange(this)
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
    open fun onMessageEvent(event: MessageEvent) {
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
    fun clearDisposableTrade() {
        disposablesTrade?.clear()
        disposablesTrade = null
    }

    private var mainModel: MainModel? = null
    protected fun getMainModel(): MainModel {
        if (null == mainModel) {
            mainModel = MainModel()
        }
        return mainModel!!
    }

    private var otcModel: OTCModel? = null

    protected fun getOTCModel(): OTCModel {
        if (null == otcModel) {
            otcModel = OTCModel()
        }
        return otcModel ?: OTCModel()
    }

    private var contractModelNet: ContractModel? = null
    protected fun getContractModelOld() = contractModelNet ?: ContractModel()

    private var contractModel: NewContractModel? = null
    protected fun getContractModel() = contractModel ?: NewContractModel()


    private var mLoadingDialog: NLoadingDialog? = null
    protected fun showLoadingDialog(loadText: String = "") {
        if (mActivity != null && mActivity?.isFinishing == false) {
            if (null == mLoadingDialog) {
                mLoadingDialog = NLoadingDialog(mActivity, loadText)
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
            val parentFragment = parentFragment ?: return true

            return if (parentFragment is NBaseFragment) {
                parentFragment.isVisibleOnScreen()
            } else {
                parentFragment.isVisible
            }
        }
        return false
    }
    fun toRequestBody(params: Map<String, String>): RequestBody {
        return JSONObject(params).toString().toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
    }
}