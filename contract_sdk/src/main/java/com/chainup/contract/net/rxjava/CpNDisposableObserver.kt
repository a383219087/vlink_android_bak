package com.yjkj.chainup.net_new.rxjava

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.net.CpJSONUtil
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpContextUtil
import com.chainup.contract.utils.CpNToastUtil
import com.chainup.contract.view.CpNLoadingDialog
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
abstract class CpNDisposableObserver : DisposableObserver<ResponseBody> {

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
        closeLoadingDialog()
        var jsonObj = CpJSONUtil.parse(responseBody, isShowToast)
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
            is HttpException -> {
                val code = e.code()
                val message = e.message
                onResponseFailure(code, message)
            }
            is SocketTimeoutException -> {
                onResponseFailure(net_errorCode, CpContextUtil.getString(R.string.cp_extra_text10))
            }
            is IOException -> {
                onResponseFailure(net_errorCode, CpContextUtil.getString(R.string.cp_extra_text11))
            }
            else -> {
                //server Error
                onResponseFailure(net_errorCode, CpContextUtil.getString(R.string.cp_extra_text12))
            }
        }
    }

    abstract fun onResponseSuccess(jsonObject: JSONObject)

    /*
     * 公共错误请求码，可在此处理
     */
    open fun onResponseFailure(code: Int, msg: String?) {
        if (isShowToast) {
            val app = CpMyApp.instance() as CpMyApp
            if (app.appCount != 0){
                if (code != 200002&&code != 109006) {
                    CpNToastUtil.showTopToast(false, msg)
                }
            }
        }
        if (code == 10021 || code == 10002 || code == 3 || code == 104008) {
//            CpNToastUtil.showTopToast(false, msg+"  "+code)
            CpClLogicContractSetting.cleanToken()
        }

    }

    private var mLoadingDialog: CpNLoadingDialog? = null
    private fun showLoadingDialog() {
        closeLoadingDialog()
        if (null != mActivity) {
            if (null == mLoadingDialog) {
                mLoadingDialog =
                    CpNLoadingDialog(mActivity)
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