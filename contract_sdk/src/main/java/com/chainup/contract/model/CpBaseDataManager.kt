package com.chainup.contract.model

import com.chainup.contract.api.CpNDataHandler
import com.chainup.contract.app.CpAppConfig
import com.chainup.contract.net.CpHttpHelper
import com.chainup.contract.net.CpHttpParams
import com.chainup.contract.net.CpHttpParamsV1
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*

open class CpBaseDataManager {

    var httpHelper: CpHttpHelper

    init {
        this.httpHelper = CpHttpHelper.instance
    }

    protected fun <T> changeIOToMainThread(observable: Observable<T>?, consumer: DisposableObserver<T>?): Disposable? {
        if (observable == null || consumer == null) {
            if (CpAppConfig.IS_DEBUG) {
                throw NullPointerException("consumer or observable not be null")
            }
            return null
        }
        return observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(consumer)
    }


    fun getBaseReqBody(maps : TreeMap<String, String>?=null) : RequestBody{
        var paramMaps = maps
        if(null==maps || maps.isEmpty()){
            paramMaps = CpHttpParams.getInstance(0).build()
        }
        var aa = CpNDataHandler.encryptParams(paramMaps!!)
        return getReqBody(aa)
    }


    fun getBaseReqBodyV1(maps : TreeMap<String, Any>?=null) : RequestBody{
        var paramMaps = maps
        if(null==maps || maps.isEmpty()){
            paramMaps = CpHttpParamsV1.getInstance(0).build()
        }
        var aa = CpNDataHandler.encryptParamsV1(paramMaps!!)
        return getReqBodyV1(aa)
    }


    fun getBaseMaps() : TreeMap<String, String>{
        var paramMaps = CpHttpParams.getInstance(0).build()
        return paramMaps
    }

    fun getBaseMapsV2() : TreeMap<String, Any>{
        var paramMaps = CpHttpParamsV1.getInstance(0).build()
        return paramMaps
    }


    /*
     * 参数值转为json串
     */
    private fun getReqBody(mapParams: Map<String, String>): RequestBody {
        val json = JSONObject(mapParams)
        return json.toString().toRequestBody("application/json;charset=UTF-8".toMediaTypeOrNull())
    }


    /*
     * 参数值转为json串
     */
    private fun getReqBodyV1(mapParams: Map<String, Any>): RequestBody {
        val json = JSONObject(mapParams)
        return json.toString().toRequestBody("application/json;charset=UTF-8".toMediaTypeOrNull())
    }
}
