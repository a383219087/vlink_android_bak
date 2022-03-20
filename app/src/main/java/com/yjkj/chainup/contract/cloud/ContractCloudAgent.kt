package com.yjkj.chainup.contract.cloud

import com.alibaba.fastjson.JSON
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.ContractUser
import com.yjkj.chainup.contract.data.bean.ContractCloudModel
import com.yjkj.chainup.contract.listener.SLDoListener
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.net_new.NetUrl
import com.yjkj.chainup.util.NToastUtil
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * @author ZhongWei
 * @time 2020/8/18 17:37
 * @description 合约云业务辅助类
 **/
object ContractCloudAgent {

    /**
     * 合约云是否打开
     */
    var isCloudOpen = false

    /**
     * 划转到合约云子账户
     */
    const val WALLET_TO_CONTRACT = "wallet_to_contract"

    /**
     * 合约云子账户划出
     */
    const val CONTRACT_TO_WALLET = "contract_to_wallet"

    /**
     * Retrofit
     */
    private val retrofit: Retrofit = defaultRetrofit()

    /**
     * api
     */
    private val contractCloudApi = retrofit.create(ContractCloudApi::class.java)

    /**
     * 初始化
     */
    fun init(userId: String?, successListener: SLDoListener? = null) {
        if (userId.isNullOrEmpty()) {
            return
        }
        val contractUid = ContractSDKAgent.user.uid ?: ""
        val contractToken = ContractSDKAgent.user.token ?: ""
        val contractAccessKey = ContractSDKAgent.user.accessKey
        if (contractUid == userId &&
                contractToken.isNotEmpty() &&
                contractAccessKey.isNotEmpty() &&
                !isExpire(ContractSDKAgent.user.expiredTs)) {
            return
        }
        initCloudChildUser(userId, successListener)
    }

    /**
     * 初始化合约云子账号
     * uid 用户id
     */
    private fun initCloudChildUser(uid: String?, successListener: SLDoListener? = null) {
        val manager = PreferenceManager.getInstance(ContractSDKAgent.context)
        var localUid = manager.getSharedString(PreferenceManager.CONTRACT_CLOUD_UID, "")
        var localToken = manager.getSharedString(PreferenceManager.CONTRACT_CLOUD_TOKEN, "")
        var localAccessKey = manager.getSharedString(PreferenceManager.CONTRACT_CLOUD_API_KEY, "")
        var localExpiredTs = manager.getSharedString(PreferenceManager.CONTRACT_CLOUD_EX_PRIED_TS, "0")
        //本地缓存有效时间戳未过期直接返回
        if (!isExpire(localExpiredTs)
                && localToken.isNotEmpty()
                && localAccessKey.isNotEmpty()
                && uid == localUid) {
            ContractSDKAgent.user = ContractUser().apply {
                this.uid = uid
                this.token = localToken
                this.accessKey = localAccessKey
                this.expiredTs = localExpiredTs
            }
            successListener?.doThing()
            return
        }
        val json = JSON.toJSONString(hashMapOf("uid" to uid))
        val headers = ContractCloudHttpClient.createHeaders()
        val requestBody = json.toRequestBody(ContractCloudHttpClient.JSON_TYPE)
        val createCloudChildUser = contractCloudApi.createCloudChildUser(headers, requestBody)
        createCloudChildUser.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                NToastUtil.showToast(t.message ?: "", false)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.apply {
                        JSON.parseObject(this, ContractCloudModel::class.java)?.apply {
                            if (code == "0") {
                                localUid = uid
                                localToken = getDataKeyValue("token")
                                localAccessKey = getDataKeyValue("api_key")
                                localExpiredTs = getDataKeyValue("ex_pried_ts")
                                ContractSDKAgent.user = ContractUser().apply {
                                    this.uid = uid
                                    this.token = localToken
                                    this.accessKey = localAccessKey
                                    this.expiredTs = localExpiredTs
                                }
                                successListener?.doThing()
                                manager.putSharedString(PreferenceManager.CONTRACT_CLOUD_UID, uid)
                                manager.putSharedString(PreferenceManager.CONTRACT_CLOUD_TOKEN, localToken)
                                manager.putSharedString(PreferenceManager.CONTRACT_CLOUD_API_KEY, localAccessKey)
                                manager.putSharedString(PreferenceManager.CONTRACT_CLOUD_EX_PRIED_TS, localExpiredTs)
                            } else {
                                NToastUtil.showToast(msg ?: "", false)
                            }
                        }
                    }
                } else {
                    NToastUtil.showToast(response.message() ?: "", false)
                }
            }
        })
    }

    /**
     * 时间是否过期
     */
    private fun isExpire(expiredTs: String?): Boolean {
        return expiredTs.isNullOrEmpty() || expiredTs.toLong() <= System.currentTimeMillis()
    }

    /**
     * 合约划转
     */
    fun contractCloudTransfer(symbol: String, amount: String, transferType: String, successListener: () -> Unit, endListener: () -> Unit) {
        when (transferType) {
            WALLET_TO_CONTRACT -> {
                cloudChildUserTransferIn(symbol, amount, successListener, endListener)
            }
            CONTRACT_TO_WALLET -> {
                cloudChildUserTransferOut(symbol, amount, successListener, endListener)
            }
        }
    }

    /**
     * 合约云子账号转入
     */
    private fun cloudChildUserTransferIn(symbol: String, amount: String, successListener: () -> Unit, endListener: () -> Unit) {
        val uid: String = UserDataService.getInstance().userInfo4UserId
        val json = JSON.toJSONString(hashMapOf("uid" to uid, "symbol" to symbol, "amount" to amount))
        val headers = ContractCloudHttpClient.createHeaders()
        val requestBody = json.toRequestBody(ContractCloudHttpClient.JSON_TYPE)
        val cloudChildUserTransferIn = contractCloudApi.cloudChildUserTransferIn(headers, requestBody)
        cloudChildUserTransferIn.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                NToastUtil.showToast(t.message ?: "", false)
                endListener.invoke()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.apply {
                        JSON.parseObject(this, ContractCloudModel::class.java)?.apply {
                            if (code == "0") {
                                successListener.invoke()
                            } else {
                                NToastUtil.showToast(msg ?: "", false)
                            }
                        }
                    }
                } else {
                    NToastUtil.showToast(response.message() ?: "", false)
                }
                endListener.invoke()
            }
        })
    }

    /**
     * 合约云子账号转出
     */
    private fun cloudChildUserTransferOut(symbol: String, amount: String, successListener: () -> Unit, endListener: () -> Unit) {
        val uid: String = UserDataService.getInstance().userInfo4UserId
        val json = JSON.toJSONString(hashMapOf("uid" to uid, "symbol" to symbol, "amount" to amount))
        val headers = ContractCloudHttpClient.createHeaders()
        val requestBody = json.toRequestBody(ContractCloudHttpClient.JSON_TYPE)
        val cloudChildUserTransferOut = contractCloudApi.cloudChildUserTransferOut(headers, requestBody)
        cloudChildUserTransferOut.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                NToastUtil.showToast(t.message ?: "", false)
                endListener.invoke()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.apply {
                        JSON.parseObject(this, ContractCloudModel::class.java)?.apply {
                            if (code == "0") {
                                successListener.invoke()
                            } else {
                                NToastUtil.showToast(msg ?: "", false)
                            }
                        }
                    }
                } else {
                    NToastUtil.showToast(response.message() ?: "", false)
                }
                endListener.invoke()
            }
        })
    }

    /**
     * 默认Retrofit
     */
    private fun defaultRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(NetUrl.baseUrl())
                .client(defaultHttpClient())
                .build()
    }

    /**
     * OkHttpClient
     */
    private fun defaultHttpClient(): OkHttpClient {
        return ContractCloudHttpClient.httpClient
    }

}