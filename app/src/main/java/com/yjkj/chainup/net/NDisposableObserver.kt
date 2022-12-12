package com.yjkj.chainup.net

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.new_version.activity.login.TouchIDFaceIDActivity
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.NetworkUtils
import io.reactivex.observers.DisposableObserver
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-08-06 19:20

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-08-06 19:20

 * @UpdateRemark:   更新说明

 */
abstract class NDisposableObserver : DisposableObserver<ResponseBody> {

    private val TAG = "MyDisposableObserver"

    private val server_errorCode = -2
    private val net_errorCode = -1

    constructor(showToast: Boolean = false) {
        isShowToast = showToast
    }

    var mActivity: Activity? = null
    private var isShowToast = false
    var mapParams: Any? = null

    constructor(activity: Activity?, showToast: Boolean = false) {
        this.mActivity = activity
        this.isShowToast = showToast
        this.showLoadingDialog()
    }

    constructor(activity: Activity?, showToast: Boolean = false, map: Any = "") {
        this.mActivity = activity
        this.isShowToast = showToast
        this.mapParams = map
        this.showLoadingDialog()
    }

    /*
     * toast展示开关控制，默认展示
     */
    fun setShowToast(isShowToast: Boolean) {
        this.isShowToast = isShowToast
    }

    override fun onNext(responseBody: ResponseBody) {
        LogUtil.d(TAG, "MyDisposableObserver==onNext==t is $responseBody")
        closeLoadingDialog()
        var jsonObj = JSONUtil.parse(responseBody, isShowToast)
        if (null != jsonObj) {
            val code = jsonObj.optString("code")

            if ("0".equals(code, true)) {
                onResponseSuccess(jsonObj)
            } else {
                var msg = jsonObj.optString("msg")
                onResponseFailure(jsonObj.optInt("code"), msg)
            }
        } else {
            onResponseFailure(-1, "json is error")
        }
    }

    override fun onComplete() {
        closeLoadingDialog()
    }

    override fun onError(e: Throwable) {
        closeLoadingDialog()
        when (e) {
            is AppException -> {
                onResponseFailure(e.code.toInt(), e.message)
            }
            is HttpException -> {
                val code = e.code()
                val message = e.message
                onResponseFailure(code, message)
            }
            is SocketTimeoutException -> {
                onResponseFailure(net_errorCode, LanguageUtil.getString(null,"network_connection_is_out_of_time"))
            }
            is IOException -> {
                onResponseFailure(net_errorCode, LanguageUtil.getString(null,"network_is_exception"))
            }
            else -> {
                //server Error
                onResponseFailure(net_errorCode, LanguageUtil.getString(null,"Server_error_please_try_again_later"))
            }
        }
    }

    abstract fun onResponseSuccess(jsonObject: JSONObject)

    /*
     * 公共错误请求码，可在此处理
     */
    open fun onResponseFailure(code: Int, msg: String?) {
         if (code==404){
             return
         }
        if (isShowToast) {
            val app = ChainUpApp.app as ChainUpApp
            LogUtil.e(TAG, "code:" + code)
            if (app.appCount != 0) {
                if (code != 200002) {
                    LogUtil.e(TAG, "msg:" + msg)
                    NToastUtil.showTopToastNet(mActivity,false, msg)
                }
            }
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


    private var mLoadingDialog: NLoadingDialog? = null
    private fun showLoadingDialog() {
        closeLoadingDialog()
        if (null != mActivity) {
            if (null == mLoadingDialog) {
                mLoadingDialog = NLoadingDialog(mActivity)
            }
            mLoadingDialog!!.showLoadingDialog()
        }
    }

    private fun closeLoadingDialog() {
        if (null != mActivity) {
            if (null != mLoadingDialog) {
                mLoadingDialog!!.closeLoadingDialog()
                mLoadingDialog = null
            }
        }
    }

    fun getHomeTabType(): String {
        if (mapParams is String) {
            return mapParams.toString()
        }
        return ""
    }

}