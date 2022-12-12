package com.yjkj.chainup.base



import android.os.Bundle
import android.text.TextUtils
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.lifecycle.*
import com.chainup.contract.api.CpContractApiService
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.manager.TAG
import com.yjkj.chainup.model.api.ContractApiService
import com.yjkj.chainup.net.AppException
import com.yjkj.chainup.net.HttpHelper
import com.yjkj.chainup.net.api.ApiService
import com.yjkj.chainup.new_version.activity.login.TouchIDFaceIDActivity
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NetworkUtils
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


abstract class BaseViewModel : ViewModel(), LifecycleObserver {

    var finish = MutableLiveData<Void?>()
    var refreshUI = MutableLiveData(false)
    private var mCompositeDisposable: CompositeDisposable? = null
     var apiService: ApiService =HttpHelper.instance.getBaseUrlService(ApiService::class.java)
    var contractApiService: ContractApiService = HttpHelper.instance.getContractUrlService(ContractApiService::class.java)
    var cpContractApiService: CpContractApiService = HttpHelper.instance.getContractNewUrlService(CpContractApiService::class.java)


    var otcDefaultPaycoin = MutableLiveData(RateManager.getCurrencyLang())

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected open fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onDestroy() {
        detachView()
    }

    fun finish() {
        finish.value = null
    }


    fun refresh() {
        refreshUI.value = !refreshUI.value!!
    }


    /**
     * 启动网络任务
     */
    fun <D> startTask(single: Observable<D>, onNext: Consumer<in D>? ,onErr:Consumer<Throwable>) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        mCompositeDisposable!!.add(
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onNext,onErr)
        )
    }

    /**
     * 启动网络任务
     */
    fun <D> startTask(single: Observable<D>, onNext: Consumer<in D>? ) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        mCompositeDisposable!!.add(
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, Consumer {
                    LogUtil.d("我的报错",it.message.toString())
                    when (it) {
                        is AppException -> {
                            ToastUtils.showToast(it.message)
                            onResponseFailure(it.code.toInt(), it.message)
                        }
                        is HttpException -> {
                            val code = it.code()
                            val message = it.message
                            onResponseFailure(code, message)
                        }
                        is SocketTimeoutException -> {
                            onResponseFailure(-1, LanguageUtil.getString(null,"network_connection_is_out_of_time"))
                        }
                        is IOException -> {
                            onResponseFailure(-1, LanguageUtil.getString(null,"network_is_exception"))
                        }
                        else -> {
                            onResponseFailure(-1, LanguageUtil.getString(null,"Server_error_please_try_again_later"))
                        }
                    }
                })
        )
    }




    private fun detachView() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable!!.dispose()
            mCompositeDisposable!!.clear()
            mCompositeDisposable = null
        }
    }

     fun toRequestBody(params: Map<String, Any>): RequestBody {
        return JSONObject(params).toString().toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
    }

    /*
 * 公共错误请求码，可在此处理
 */
    open fun onResponseFailure(code: Int, msg: String?) {
        if (code==404){
            return
        }
        if (code == 10021 || code == 10002 || code == 3 || code == ParamConstant.QUICK_LOGIN_FAILURE) {
            UserDataService.getInstance().clearToken()
            val userinfo = UserDataService.getInstance().userData
            if (null == userinfo) {
                ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
            } else {
                val fingerprintManager = FingerprintManagerCompat.from(ChainUpApp.appContext)
                if (fingerprintManager.isHardwareDetected) {
                    /**
                     * 判断是否输入指纹
                     */
                    if (fingerprintManager.hasEnrolledFingerprints() && LoginManager.getInstance().fingerprint == 1) {
                        val bundle = Bundle()
                        bundle.putInt("type", TouchIDFaceIDActivity.FINGERPRINT)
                        bundle.putBoolean("is_first_login", false)
                        ArouterUtil.navigation("/login/touchidfaceidactivity", bundle)
                    } else if (!TextUtils.isEmpty(UserDataService.getInstance().gesturePass) || !TextUtils.isEmpty(UserDataService.getInstance().gesturePwd)) {
                        val bundle = Bundle()
                        bundle.putInt("SET_TYPE", 1)
                        bundle.putString("SET_TOKEN", "")
                        bundle.putBoolean("SET_STATUS", true)
                        bundle.putBoolean("SET_LOGINANDSET", true)
                        ArouterUtil.navigation("/login/gesturespasswordactivity", bundle)
                    } else {
                        ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                    }
                } else if (!TextUtils.isEmpty(UserDataService.getInstance().gesturePass) || !TextUtils.isEmpty(UserDataService.getInstance().gesturePwd)) {

                    val bundle = Bundle()
                    bundle.putInt("SET_TYPE", 1)
                    bundle.putString("SET_TOKEN", "")
                    bundle.putBoolean("SET_STATUS", true)
                    bundle.putBoolean("SET_LOGINANDSET", true)
                    ArouterUtil.navigation("/login/gesturespasswordactivity", bundle)
                } else {
                    ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                }
            }

            //            Intent intent = new Intent(ChainUpApp.appContext, NewVersionLoginActivity.class);
            //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //            ChainUpApp.appContext.startActivity(intent);

        } else {
            LogUtil.e(TAG, "网络错误 需要切换网络 ${NetworkUtils.isNetworkAvailable(ChainUpApp.appContext)}")
        }
    }



}