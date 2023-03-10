package com.yjkj.chainup.net


import android.text.TextUtils
import android.util.Log
import com.contract.sdk.ContractSDKAgent
import com.google.gson.JsonObject
import com.yjkj.chainup.app.AppConfig
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.bean.*
import com.yjkj.chainup.bean.address.AddressBean
import com.yjkj.chainup.bean.dev.MessageBean
import com.yjkj.chainup.bean.dev.NoticeBean
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.bean.kline.DepthItem
import com.yjkj.chainup.contract.cloud.ContractCloudAgent
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.freestaking.bean.CurrencyBean
import com.yjkj.chainup.freestaking.bean.FreeStakingBean
import com.yjkj.chainup.freestaking.bean.FreeStakingDetailBean
import com.yjkj.chainup.freestaking.bean.MyPosRecordBean
import com.yjkj.chainup.model.api.ContractApiService
import com.yjkj.chainup.model.api.OTCApiService
import com.yjkj.chainup.model.api.RedPackageApiService
import com.yjkj.chainup.net.api.ApiConstants.*
import com.yjkj.chainup.net.api.ApiService
import com.yjkj.chainup.net.api.HttpResult
import com.yjkj.chainup.net.retrofit.ResponseConverterFactory
import com.yjkj.chainup.new_version.bean.*
import com.yjkj.chainup.new_version.home.AdvertModel
import com.yjkj.chainup.new_version.redpackage.bean.*
import com.yjkj.chainup.treaty.bean.*
import com.yjkj.chainup.util.*
import com.yjkj.chainup.ws.WsAgentManager
import io.reactivex.Observable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.POST
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


lateinit var originalRequest: Request

class HttpClient private constructor() {
    val TAG = HttpClient::class.java.simpleName

    var mOkHttpClient: OkHttpClient? = null


    private var token: String = ""

    private var apiService: ApiService

    /**
     * OTC 场外 service
     */
    private var apiOTCService: OTCApiService

    private var contractService: ContractApiService

    private var newContractService: ContractApiService

    private var redPackageService: RedPackageApiService

    private fun getBaseMap(isAddToken: Boolean = false): TreeMap<String, String> {
        val map = TreeMap<String, String>()
        map["time"] = System.currentTimeMillis().toString()
//        if (isAddToken && !TextUtils.isEmpty(token)) {
//            map["token"] = token
//        }

//        map.put("exchange-token", token!!)

        return map
    }


    companion object {

        /**
         * 国家码
         */
        const val COUNTRY_CODE = "countryCode"

        /**
         * 手机号
         */
        const val MOBILE_NUMBER = "mobileNumber"

        /**
         * 登录密码
         */
        const val LOGIN_PWORD = "loginPword"

        /**
         * 验证码
         */
        const val SMS_AUTHCODE = "smsAuthCode"


        /**
         * 邮箱
         */
        const val EMAIL = "email"

        /**
         * 邮箱验证码
         */
        const val EMAIL_AUTHCODE = "emailAuthCode"

        /**
         * 短信验证码的类型
         */
        const val OPERATION_TYPE = "operationType"

        /**
         * 用户昵称
         */
        const val NICKNAME = "nickname"

        /**
         * GoogleKey
         */
        const val GOOGLE_KEY = "googleKey"


        /**
         * Google验证码
         */
        const val GOOGLE_CODE = "googleCode"

        /**
         * 验证类型
         */
        const val VERIFICATION_TYPE = "verificationType"

        private var INSTANCE: HttpClient? = null

        val instance: HttpClient
            get() {
                if (INSTANCE == null) {
                    synchronized(HttpClient::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = HttpClient()
                        }
                    }
                }
                return INSTANCE!!
            }
    }


    init {
        initOkHttpClient()
        apiService = createApi()
        apiOTCService = createOTCApi()
        contractService = createContractApi()
        newContractService = createNewContractApi()
        redPackageService = createRedPackageApi()
    }


    fun refreshApi() {
        apiService = createApi()
        apiOTCService = createOTCApi()
        contractService = createContractApi()
        redPackageService = createRedPackageApi()
        newContractService = createNewContractApi()
    }


     fun createApi(): ApiService {
        if (!StringUtil.isHttpUrl(BASE_URL))
            BASE_URL = AppConfig.default_host

        val retrofit = Retrofit.Builder()
                .baseUrl(NetUrl.baseUrl())  // 设置服务器路径
                .client(mOkHttpClient!!)  // 设置okhttp的网络请求
                .addConverterFactory(ResponseConverterFactory.create())// 添加转化库,默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加回调库，采用RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ApiService::class.java)
    }

    private fun createOTCApi(): OTCApiService {

        if (!StringUtil.isHttpUrl(BASE_OTC_URL))
            BASE_OTC_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_OTC_URL)  // 设置服务器路径
                .client(mOkHttpClient!!)  // 设置okhttp的网络请求
                .addConverterFactory(ResponseConverterFactory.create())// 添加转化库,默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加回调库，采用RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(OTCApiService::class.java)
    }


    fun createContractApi(): ContractApiService {
        if (!StringUtil.isHttpUrl(CONTRACT_URL))
            CONTRACT_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(CONTRACT_URL)  // 设置服务器路径
                .client(mOkHttpClient!!)  // 设置okhttp的网络请求
                .addConverterFactory(ResponseConverterFactory.create())// 添加转化库,默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加回调库，采用RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ContractApiService::class.java)
    }

    private fun createNewContractApi(): ContractApiService {
        if (!StringUtil.isHttpUrl(NEW_CONTRACT_URL))
            NEW_CONTRACT_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(NEW_CONTRACT_URL)  // 设置服务器路径
                .client(mOkHttpClient!!)  // 设置okhttp的网络请求
                .addConverterFactory(ResponseConverterFactory.create())// 添加转化库,默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加回调库，采用RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ContractApiService::class.java)
    }


    private fun createRedPackageApi(): RedPackageApiService {
        var redPackUrl = NetUrl.getredPackageUrl()
        if (!StringUtil.isHttpUrl(redPackUrl))
            redPackUrl = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(redPackUrl)  // 设置服务器路径
                .client(mOkHttpClient!!)  // 设置okhttp的网络请求
                .addConverterFactory(ResponseConverterFactory.create())// 添加转化库,默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //添加回调库，采用RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(RedPackageApiService::class.java)
    }


    fun setToken(token: String?) {
        if (null != token) {
            this.token = token
            if (token.isNotEmpty()) {
                var messageEvent = MessageEvent(MessageEvent.login_bind_type)
                EventBusUtil.post(messageEvent)
            }
        } else {
            this.token = ""
        }
    }

    private fun toRequestBody(params: Map<String, String>): RequestBody {
        return JSONObject(params).toString().toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
    }


    /**
     * 修改登录密码
     */
    fun changeLoginPwd(smsAuthCode: String = "", loginPwd: String, newLoginPwd: String,
                       googleCode: String = "", identificationNumber: String? = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["smsAuthCode"] = smsAuthCode
        map[LOGIN_PWORD] = loginPwd
        map["newLoginPword"] = newLoginPwd
        map["googleCode"] = googleCode
        if (identificationNumber != null && identificationNumber.isNotEmpty()) {
            map["IdentificationNumber"] = identificationNumber
        }
        return apiService.changeLoginPwdV4(toRequestBody(DataHandler.encryptParams(map)))

    }


    /**
     * 用户昵称修改
     */
    fun editNickname(nickName: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map[NICKNAME] = nickName
        return apiService.editNickname(toRequestBody(DataHandler.encryptParams(map)))
    }
    /**
     * 用户签名修改
     */
    fun editSign(sign: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["description"] = sign
        return apiService.description(map)
    }


    /**
     * 退出登录
     */
    fun logout(): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        return apiService.logout(toRequestBody(DataHandler.encryptParams(map)))
    }


    /*******Google认证相关*START*******/

    /**
     * 获取GoogleKey
     */
    fun getGoogleKey(): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap(false)
        return apiService.getGoogleKey(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 绑定Google认证
     */
    fun bindGoogleVerify(googleKey: String, loginPwd: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map[GOOGLE_KEY] = googleKey
        map["loginPwd"] = loginPwd
        map[GOOGLE_CODE] = googleCode
        return apiService.bindGoogleVerify(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 关闭谷歌验证
     */
    fun unbindGoogleVerify(smsValidCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["smsValidCode"] = smsValidCode
        map["googleCode"] = googleCode
        return apiService.unbindGoogleVerify(toRequestBody(DataHandler.encryptParams(map)))

    }


    /**
     * 关闭手机验证
     */
    fun unbindMobileVerify(smsValidCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["smsValidCode"] = smsValidCode
        map["googleCode"] = googleCode
        return apiService.unbindMobileVerify(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 开启手机验证
     */
    fun openMobileVerify(): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        return apiService.openMobileVerify(toRequestBody(DataHandler.encryptParams(map)))
    }


    /*******认证相关*END*******/


    /**
     * 修改手机号码
     */

    fun changeMobile(newSmsCode: String, originalSmsCode: String, country: String, newMobile: String, googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["authenticationCode"] = originalSmsCode
        map["countryCode"] = country
        map["mobileNumber"] = newMobile
        map["googleCode"] = googleCode
        map["smsAuthCode"] = newSmsCode
        return apiService.changeMobile(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 修改邮箱
     */
    fun changeEmail(oldEmailCode: String, newEmail: String, newEmailCode: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["emailOldValidCode"] = oldEmailCode
        map["email"] = newEmail
        map["emailNewValidCode"] = newEmailCode
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.changeEmail(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 绑定邮箱
     */
    fun bindEmail(email: String, emailCode: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["email"] = email
        map["emailValidCode"] = emailCode
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.bindEmail(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 绑定手机
     */
    fun bindMobile(country: String, mobile: String, smsCode: String, googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map[COUNTRY_CODE] = country
        map[MOBILE_NUMBER] = mobile
        map[SMS_AUTHCODE] = smsCode
        map[GOOGLE_CODE] = googleCode
        return apiService.bindMobile(toRequestBody(DataHandler.encryptParams(map)))
    }




    /**
     * 提现操作
     * @amount 提现金额（不包含手续费）
     * @symbol 币种
     */
    fun doWithdraw(addressId: String = "", fee: String = "", smsCode: String = "", googleCode: String = "", amount: String = "", symbol: String? = "",
                   address: String = "", label: String = "", trustType: String = "", emailValidCode: String = ""): Observable<HttpResult<AuthBean>> {
        val map = getBaseMap()
        if (addressId.isNotEmpty()) {
            map["addressId"] = addressId
        }
        if (address.isNotEmpty()) {
            map["address"] = address
        }
        if (label.isNotEmpty()) {
            map["label"] = label
        }
        if (trustType.isNotEmpty()) {
            map["trustType"] = trustType
        }
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        map["fee"] = fee
        map["amount"] = amount

        if (null != symbol) {
            map["symbol"] = symbol
        }

        if (emailValidCode.isNotEmpty()) {
            map["emailValidCode"] = emailValidCode
        }
        return apiService.doWithdraw(toRequestBody(DataHandler.encryptParams(map)))
    }

    /***********地址管理******START********/
    /**
     * 钱包地址列表
     */
    fun getAddressList(symbol: String = ""): Observable<HttpResult<AddressBean>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        return apiService.getAddressList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 添加地址
     */
    fun addWithdrawAddress(symbol: String, address: String, smsCode: String = "", label: String, googleCode: String = "", trustType: String = "0", emailValidCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        map["address"] = address
        map["smsValidCode"] = smsCode
        map["label"] = label
        map["googleValidCode"] = googleCode
        map["trustType"] = trustType
        if (emailValidCode.isNotEmpty()) {
            map["emailValidCode"] = emailValidCode
        }
        return apiService.addWithdrawAddress(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 删除钱包地址
     */
    fun delWithdrawAddress(id: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["ids"] = id
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.delWithdrawAddress(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 消息中心
     */
    fun getMessages(type: Int, page: Int = 1, pageSize: Int = 1000): Observable<HttpResult<MessageBean>> {
        val map = getBaseMap()
        map["messageType"] = type.toString()
        map["page"] = page.toString()
        map.put("pageSize", pageSize.toString())
        return apiService.getMessages(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 公告列表
     */
    fun getNotices(page: Int = 1, pageSize: Int = 1000): Observable<HttpResult<NoticeBean>> {
        val map = getBaseMap()
//        map["page"] = page.toString()
        map.put("pageSize", pageSize.toString())
        return apiService.getNotices(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 上传照片
     */
    fun uploadImg(imgBase64: String, name: String = ""): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        if (name.isNotEmpty()) {
            map["name"] = name
        }
        map["imageData"] = imgBase64
//        val body = RequestBody.create(MediaType.parse("form-data"), JSONObject(DataHandler.encryptParams(map)).toString())

        return apiService.uploadImg(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 获取kyc配置
     */
    fun getKYCConfig(): Observable<HttpResult<KYCBean>> {
        val map = getBaseMap(false)
        return apiService.getKYCConfig(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 资金划转
     */
    fun doAssetExchange(coinSymbol: String, amount: String, transferType: String): Observable<HttpResult<Any>> {
        //futuresType 0 私有化   1 合约云
         // 币币  -> 合约 币币的接口 app/co_transfer

         //  合约  -> 币币
        //0 : 币币的接口 app/co_transfer
        //1: 合约的接口： assets/saas_trans/co_to_ex
        val map = getBaseMap()
        map["coinSymbol"] = coinSymbol
        map["amount"] = amount
        map["transferType"] = transferType

        return if(transferType.equals(ContractCloudAgent.WALLET_TO_CONTRACT)){
            // 币币  -> 合约
            apiService.doAssetExchange(toRequestBody(DataHandler.encryptParams(map)))
        }else{
            // 合约  -> 币币
            val futuresType= PublicInfoDataService.getInstance().getfuturesType(null);
            if (futuresType.equals("0")){
                //私有化
                apiService.doAssetExchange(toRequestBody(DataHandler.encryptParams(map)))
            }else{
                //合约云
                contractService.coTransferEx(toRequestBody(DataHandler.encryptParams(map)))
            }
        }


    }


    /**
     * 实名认证
     */
    fun authVerify(countryCode: String,
                   certType: Int,
                   certNum: String,
                   userName: String,// 姓
                   firstPhoto: String,
                   secondPhoto: String,
                   thirdPhoto: String,
                   familyName: String,
                   name: String,
                   numberCode: String
    ): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["countryCode"] = countryCode
        map["certificateType"] = certType.toString()
        map["certificateNumber"] = certNum
        map["userName"] = userName
        map["firstPhoto"] = firstPhoto
        map["secondPhoto"] = secondPhoto
        map["thirdPhoto"] = thirdPhoto
        map["familyName"] = familyName
        map["name"] = name
        map["numberCode"] = numberCode


        return apiService.authVerify(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 开启手势密码
     * @param loginPwd 必填
     * @param smsCode 必填（开启手机验证时）
     * @param googleCode 必填（开启谷歌验证时）
     */
    fun openHandPwd(loginPwd: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["loginPwd"] = loginPwd
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.openHandPwd(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 关闭手势密码
     * @param loginPwd 必填
     * @param smsCode 必填（开启手机验证时）
     * @param googleCode 必填（开启谷歌验证时）
     */
    fun closeHandPwd(loginPwd: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["loginPwd"] = loginPwd
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.closeHandPwd(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * 帮助中心列表
     */
    fun getHelpCenterList(): Observable<HttpResult<ArrayList<HelpCenterBean>>> {
        val map = getBaseMap()
        return apiService.getHelpCenterList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 获取H5的域名
     */
    fun getCommonKV(key: String = "h5_url"): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["key"] = key
        return apiService.getCommonKV(DataHandler.encryptParams(map))
    }


    /**
     * 清空用户手势密码
     */
    fun cleanGesturePwd(uid: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["uid"] = uid
        return apiService.cleanGesturePwd(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 关于我们
     */
    fun getAboutUs(): Observable<HttpResult<ArrayList<AboutUSBean>>> {
        val map = getBaseMap()
        return apiService.getAboutUs(DataHandler.encryptParams(map))
    }


    /*****************OTC*************************/


    /***
     * 移除黑名单
     */
    fun removeRelationFromBlack(friendId: Int): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["friendId"] = friendId.toString()
        return apiOTCService.removeRelationFromBlack4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }




    /**
     * 申诉页面
     */
    fun createProblem4OTC(rqDescribe: String, rqType: String, rqUnreleased: String, rqUnpaid: String, imageDataStr: String): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["rqDescribe"] = rqDescribe
        map["rqType"] = rqType
        if (!TextUtils.isEmpty(rqUnreleased)) {
            map["rqUnreleased"] = rqUnreleased
        }
        if (!TextUtils.isEmpty(rqUnpaid)) {
            map["rqUnpaid"] = rqUnpaid
        }
        if (!TextUtils.isEmpty(imageDataStr)) {
            map["imageDataStr"] = imageDataStr
        }
        return apiService.createProblem4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 获取我的订单
     */
    fun byStatus4OTC(status: String? = "", payCoin: String = "", startTime: String = "", endTime: String = "", pageSize: Int = 20, page: Int = 1, coinSymbol: String = "", tradeType: String = ""): Observable<HttpResult<OTCOrderBean>> {
        val map = getBaseMap()
        if (!TextUtils.isEmpty(status)) {
            map["status"] = status!!
        }
        if (!TextUtils.isEmpty(tradeType)) {
            map["tradeType"] = tradeType
        }
        if (!TextUtils.isEmpty(startTime)) {
            map["startTime"] = startTime
        }
        if (!TextUtils.isEmpty(endTime)) {
            map["endTime"] = endTime
        }
        if (!TextUtils.isEmpty(payCoin)) {
            map["payCoin"] = payCoin
        }
        if (!TextUtils.isEmpty(coinSymbol)) {
            map["coinSymbol"] = coinSymbol
        }
        map["pageSize"] = pageSize.toString()
        map["page"] = page.toString()

        return apiService.byStatus4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 加入黑名单
     */
    fun userContacts4OTC(otherUid: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["otherUid"] = otherUid.toString()
        map["relationType"] = "BLACKLIST"
        return apiOTCService.userContacts4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 获取聊天历史消息
     */
    fun gethistoryMessage(fromId: Int, orderId: String, toId: Int): Observable<HttpResult<ArrayList<OTCIMMessageBean>>> {
        val map = getBaseMap()
        map["fromId"] = fromId.toString()
        map["orderId"] = orderId
        map["toId"] = toId.toString()
        map["uaTime"] = DateUtil.longToString("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis())
        return apiOTCService.gethistoryMessage(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 问题详情 页面  客服聊天
     */
    fun getDetailsProblem(id: Int): Observable<HttpResult<OTCIMDetailsProblemBean>> {
        val map = getBaseMap()
        map["id"] = id.toString()
        return apiService.getDetailsProblem(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 问题详情 页面  发送消息
     */
    fun getReplyCreate(rqId: Int, rqReplyContent: String, contentType: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["rqId"] = rqId.toString()
        map["rqReplyContent"] = rqReplyContent
        map["contentType"] = contentType
        return apiService.getReplyCreate(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 其他交易 record/other_transfer_list
     */
    fun otherTransList4V2(symbol: String = "", transactionScene: String = "", startTime: String = "", endTime: String = "", pageSize: String = "20", page: String = "1"): Observable<HttpResult<CashFlowBean>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        map["transactionScene"] = transactionScene
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["pageSize"] = pageSize
        map["page"] = page
        return apiService.otherTransList4V2(toRequestBody(DataHandler.encryptParams(map)))
    }

    fun getTransferRecord(symbol: String = "", transactionScene: String = "", startTime: String = "", endTime: String = "", pageSize: String = "20", page: String = "1"): Observable<HttpResult<CashFlowBean>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        map["transactionScene"] = transactionScene
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["pageSize"] = pageSize
        map["page"] = page
        return newContractService.getTransferRecord(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 新增支付方式
     * @param payment 支付方式key
     * @param userName 姓名
     * @param account 账号信息，除西联汇款外，其他必填
     * @param qrcodeImg base64图片
     * @param bankName 开户银行
     * @param bankOfDeposit 开户支行
     * @param ifscCode IFSC码
     * @param remittanceInformation 汇款信息
     * @param smsAuthCode 手机验证码，短信验证码和google验证码两者选其一
     * @param googleCode Google验证码，短信验证码和google验证码两者选其一
     *
     */
    fun addPayment4OTC(payment: String,
                       userName: String,
                       account: String,
                       qrcodeImg: String,
                       bankName: String,
                       bankOfDeposit: String,
                       ifscCode: String,
                       remittanceInformation: String,
                       smsAuthCode: String,
                       googleCode: String
    ): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["payment"] = payment
        map["userName"] = userName
        map["account"] = account
        map["qrcodeImg"] = qrcodeImg
        map["bankName"] = bankName
        map["bankOfDeposit"] = bankOfDeposit
        map["ifscCode"] = ifscCode
        map["remittanceInformation"] = remittanceInformation
        map["smsAuthCode"] = smsAuthCode
        map["googleCode"] = googleCode
        return apiOTCService.addPayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 删除支付方式
     * @param id 支付方式id
     * @param smsAuthCode 手机验证码，短信验证码和google验证码两者选其一
     * @param googleCode Google验证码，短信验证码和google验证码两者选其一
     */
    fun removePayment4OTC(id: String, smsAuthCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = id
        map["smsAuthCode"] = smsAuthCode
        map["googleCode"] = googleCode
        return apiOTCService.removePayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 支付方式开关设置
     * @param id 支付方式id
     * @param isOpen 1/0
     */
    fun operatePayment4OTC(id: String, isOpen: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = id
        map["isOpen"] = isOpen
        return apiOTCService.operatePayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 修改支付方式
     * @param id 支付方式id
     * @param payment 支付方式key   --- 禁止修改
     * @param userName 姓名
     * @param account 账号信息，除西联汇款外，其他必填
     * @param qrcodeImg base64图片
     * @param bankName 开户银行
     * @param bankOfDeposit 开户支行
     * @param ifscCode IFSC码
     * @param remittanceInformation 汇款信息
     * @param smsAuthCode 手机验证码，短信验证码和google验证码两者选其一
     * @param googleCode Google验证码，短信验证码和google验证码两者选其一
     *
     */
    fun updatePayment4OTC(
            id: String,
            payment: String,
            userName: String,
            account: String,
            qrcodeImg: String,
            bankName: String,
            bankOfDeposit: String,
            ifscCode: String,
            remittanceInformation: String,
            smsAuthCode: String,
            googleCode: String
    ): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = id
        map["payment"] = payment
        map["userName"] = userName
        map["account"] = account
        map["qrcodeImg"] = qrcodeImg
        map["bankName"] = bankName
        map["bankOfDeposit"] = bankOfDeposit
        map["ifscCode"] = ifscCode
        map["remittanceInformation"] = remittanceInformation
        map["smsAuthCode"] = smsAuthCode
        map["googleCode"] = googleCode
        return apiOTCService.updatePayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     *
     * 订单详情数据
     * @param sequence 订单号
     */
    fun getOrderDetail4OTC(sequence: String): Observable<HttpResult<OTCOrderDetailBean>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.getOrderDetail4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *
     * 取消订单
     * @param sequence 订单号
     */
    fun cancelOrder4OTC(sequence: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.cancelOrder4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 针对于卖家
     * 确认打币
     * @param sequence 订单号
     */
    fun confirmOrder2Seller4OTC(sequence: String, capitalPword: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["capitalPword"] = capitalPword
        return apiOTCService.confirmOrder2Seller4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 针对于买家
     * 确认支付
     * @param sequence 订单号
     */
    fun confirmPay2Buyer4OTC(sequence: String, payment: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["payment"] = payment
        return apiOTCService.confirmPay2Buyer4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 申诉修改订单状态
     * @param sequence 订单号
     * @param complainId 工单id
     */
    fun complain2changeOrderState4OTC(sequence: String, complainId: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["complainId"] = complainId
        return apiOTCService.complain2changeOrderState4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 取消申诉
     * @param sequence 订单id
     */
    fun cancelComplain4OTC(sequence: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.cancelComplain4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 生成购买的订单(Step 3)
     */
    /**
     * 新生成购买的订单(Step 1)
     * @param advertId 广告id
     * @param volume 数量
     * @param price 单价
     * @param totalPrice    总价格
     * @param payment 支付方式key
     * @param description 订单备注 （选填）
     * @param type 以价格或数量为准 price/volume
     */
    fun buyOrderEnd4OTC(advertId: String,
                        volume: String,
                        price: String,
                        totalPrice: String,
                        description: String = "",
                        type: String
    ): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["advertId"] = advertId
        map["volume"] = volume
        map["price"] = price
        map["totalPrice"] = totalPrice
        map["description"] = description
        map["type"] = type
        return apiOTCService.buyOrderEnd4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 生成出售的订单(Step 3)
     * @param advertId 广告id
     * @param volume 数量
     * @param price 单价
     * @param totalPrice    总价格
     * @param payment 支付方式key
     * @param description 订单备注 （选题）
     * @param capitalPword 资金密码
     * @param type 以价格或数量为准 price/volume
     */
    fun sellOrderEnd4OTC(advertId: String,
                         volume: String,
                         price: String,
                         totalPrice: String,
                         description: String = "",
                         capitalPword: String,
                         type: String
    ): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["advertId"] = advertId
        map["volume"] = volume
        map["price"] = price
        map["totalPrice"] = totalPrice
        map["description"] = description
        map["capitalPword"] = capitalPword
        map["type"] = type
        return apiOTCService.sellOrderEnd4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 发送手机验证码
     */
    fun sendMobileCode(countryCode: String = "", mobile: String = "", otype: Int, token: String = "")
            : Observable<HttpResult<String>> {
        val map = getBaseMap(false)

        if (TextUtils.isEmpty(token)) {
            map["countryCode"] = countryCode
            map["mobile"] = mobile
        } else {
            map["token"] = token
        }
        map[OPERATION_TYPE] = otype.toString()
        return apiService.sendMobileVerifyCode(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 发送邮箱验证码
     */
    fun sendEmailCode(email: String = "", otype: Int, token: String = ""): Observable<HttpResult<String>> {
        val map = getBaseMap(false)
        if (TextUtils.isEmpty(token)) {
            map[EMAIL] = email
        } else {
            map["token"] = token
        }
        map[OPERATION_TYPE] = otype.toString()
        return apiService.sendEmailVerifyCode(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 找回密码 Step 3
     */
    fun findPwdStep3(token: String, certifcateNumber: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map["token"] = token
        map["certifcateNumber"] = certifcateNumber
        map["googleCode"] = googleCode
        return apiService.findPwdStep3(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * 指纹或者人脸识别
     */
    fun newQuickLogin(quicktoken: String): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap(false)
        map["quicktoken"] = quicktoken
        return apiService.newQuickLogin(toRequestBody(DataHandler.encryptParams(map)))
    }






    /**
     * 8. 修改杠杆倍数
     */
    fun changeLevel4Contract(contractId: String, newLevel: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map["contractId"] = contractId
        map["leverageLevel"] = newLevel
        return contractService.changeLevel4Contract(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * 16.合约资金流水(need登录)
     *
     * @param item 流水类型
     * @param childItem 子级流水类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页数
     * @param pageSize 每页几条
     *
     */
    fun getBusinessTransferList(item: String = "", childItem: String = "", startTime: String = "", endTime: String = "", page: Int = 1, pageSize: Int = 1000): Observable<HttpResult<ContractCashFlowBean>> {
        val map = getBaseMap(false)
        map["item"] = item
        map["childItem"] = childItem
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["page"] = page.toString()
        map["pageSize"] = pageSize.toString()
        return contractService.getBusinessTransferList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /********合约 END********/


    //检查更新
    fun checkVersion(time: String): Observable<HttpResult<VersionData>> {
        return apiService.checkVersion(time)
    }

    //意见反馈
    fun feedback(rqDescribe: String, imageData: String): Observable<HttpResult<String>> {
        val map = getBaseMap()
        map.put("rqDescribe", rqDescribe)
        DataHandler.encryptParams(map)
        if (!TextUtils.isEmpty(imageData)) {
            map["imageData"] = imageData
        }
        return apiService.feedback(map)
    }


    /**
     * 初始化OKHttpClient,设置缓存,设置超时时间,设置打印日志,设置UA拦截器
     */
    private fun initOkHttpClient() {


        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        if (mOkHttpClient == null) {

            val cache = Cache(File(ChainUpApp.appContext.cacheDir, "HttpCache"), (1024 * 1024 * 10).toLong())
            var builder = OkHttpClient.Builder()
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .cache(cache)
                    .addInterceptor(NetInterceptor())
                    .addNetworkInterceptor(CacheInterceptor())
                    .addInterceptor(interceptor)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
            var array = arrayOf(ChainUpApp.appContext.resources.assets.open("cert.cer"))
            val sslParams = HttpsUtils.getSslSocketFactory(array, null, null)
            if (null != sslParams) {
                builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            }
            mOkHttpClient = builder.build()
        }
    }

    fun refresh() {
        mOkHttpClient = OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .addInterceptor(NetInterceptor())
                .addNetworkInterceptor(CacheInterceptor())
                .retryOnConnectionFailure(true)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
    }

    /**
     * 为okhttp添加缓存，这里是考虑到服务器不支持缓存时，从而让okhttp支持缓存
     */
    private inner class CacheInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            // 有网络时 设置缓存超时时间1个小时
            val maxAge = 60 * 60
            // 无网络时，设置超时为1天
            val maxStale = 60 * 60 * 24
            var request = chain.request()
            request = if (NetworkUtils.isNetworkAvailable(ChainUpApp.appContext)) {
                //有网络时只从网络获取
                request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build()
            } else {
                //无网络时只从缓存中读取
                request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
            }


            var response = chain.proceed(request)
            response = if (NetworkUtils.isNetworkAvailable(ChainUpApp.appContext)) {
                response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build()
            } else {
                response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build()
            }
            return response
        }
    }


    //添加header
//   private inner class HeaderInterceptor : Interceptor {
//
//        override fun intercept(chain: Interceptor.Chain): Response {
//
//            var originalRequest: Request
//
//            val packageManager = ChainUpApp.appContext.packageManager
//            val packInfo = packageManager.getPackageInfo(ChainUpApp.appContext.packageName, 0)
//            if (UserDataService.getInstance().isLogined) {
//                if (token == null) {
//                    token = UserDataService.getInstance().token
//                }
//
//                originalRequest = chain.request()
//                        .newBuilder()
//                        .header("Content-Type", "application/json;charset=utf-8")
//                        .header("Build-CU", packInfo.versionCode.toString())
//                        .header("SysVersion-CU", SystemUtils.getSystemVersion())
//                        .header("SysSDK-CU", Build.VERSION.SDK_INT.toString())
//                        .header("Channel-CU", "")
//                        .header("Mobile-Model-CU", SystemUtils.getSystemModel())
//                        .header("UUID-CU:APP", Settings.System.getString(ChainUpApp.appContext.contentResolver, Settings.System.ANDROID_ID)
//                                ?: "")
//                        .header("Platform-CU", "android")
//                        .header("Network-CU", NetworkUtils.getNetType())
//                        .header("exchange-language", NLanguageUtil.getLanguage())
//                        .header("exchange-token", token)
//                        .header("exchange-client", "app")
//                        .build()
//            } else {
//                originalRequest = chain.request()
//                        .newBuilder()
//                        .header("Content-Type", "application/json;charset=utf-8")
//                        .header("Build-CU", packInfo.versionCode.toString())
//                        .header("SysVersion-CU", SystemUtils.getSystemVersion())
//                        .header("SysSDK-CU", Build.VERSION.SDK_INT.toString())
//                        .header("Channel-CU", "")
//                        .header("Mobile-Model-CU", SystemUtils.getSystemModel())
//                        .header("UUID-CU:APP", Settings.System.getString(ChainUpApp.appContext.contentResolver, Settings.System.ANDROID_ID)
//                                ?: "")
//                        .header("Platform-CU", "android")
//                        .header("Network-CU", NetworkUtils.getNetType())
//                        .header("exchange-language", NLanguageUtil.getLanguage())
//                        .header("exchange-client", "app")
//                        .build()
//            }
//
//            return chain.proceed(originalRequest)
//        }
//    }


    /**
     * 获取图片
     */
    fun getImageToken(operate_type: String = "1"): Observable<HttpResult<ImageTokenBean>> {
        val map = getBaseMap(false)
        map["operate_type"] = operate_type
        return apiService.getImageToken(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 实名制认证接口
     */
    fun AccountCertification(): Observable<HttpResult<AccountCertificationBean>> {
        val map = getBaseMap(false)
        return apiService.AccountCertification(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 实名制认证
     */
    fun AccountCertificationLanguage(): Observable<HttpResult<AccountCertificationLanguageBean>> {
        val map = getBaseMap(false)
        return apiService.AccountCertificationLanguage(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 场内资金流水场景列表
     */
    fun getCashFlowScene(): Observable<HttpResult<CashFlowSceneBean>> {
        val map = getBaseMap()
        return apiService.getCashFlowScene(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 资金流水列表
     * @param coinSymbol 币种类型
     * @param pageSize default 10
     * @param page  default 1
     * @param transactionScene 流水类型
     * @param startTime
     * @param endTime
     */
    fun getCashFlowList(symbol: String,
                        transactionScene: String = "1",
                        startTime: String = "",
                        endTime: String = "",
                        pageSize: String = "100",
                        page: String = "1"
    ): Observable<HttpResult<CashFlowBean>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        map["transactionScene"] = transactionScene
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["pageSize"] = pageSize
        map["page"] = page
        return apiService.getCashFlowList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 撤销提现
     */
    fun cancelWithdraw(withdrawId: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["withdrawId"] = withdrawId
        return apiService.cancelWithdraw(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 个人主页用户基本信息显示
     */
    fun getPerson4otc(uid: String): Observable<HttpResult<UserInfo4OTC>> {
        val map = getBaseMap()
        map["uid"] = uid
        return apiOTCService.getPerson4otc(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 个人主页用户基本信息显示
     */
    fun getPersonAds(uid: String, pageSize: String = "20", page: String = "1", adType: String = ""): Observable<HttpResult<PersonAdsBean>> {
        val map = getBaseMap()
        map["uid"] = uid
        map["pageSize"] = pageSize
        map["page"] = page
        map["adType"] = adType
        return apiOTCService.getPersonAds(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 获取当前用户未读信息数
     */
    fun getReadMessageCount(): Observable<HttpResult<ReadMessageCountBean>> {
        val map = getBaseMap()
        return apiService.getReadMessageCount(toRequestBody(DataHandler.encryptParams(map)))
    }
    /**
     * 获取当前是否合伙人
     */
    fun getInviteStatus(): Observable<HttpResult<String>> {
        val map = getBaseMap()
        return apiService.currentStatus(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 更新查看所有站内信
     */
    fun updateMessageStatus(): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = "0"
        return apiService.updateMessageStatus(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 获取黑名单
     * @param relationType 好友关系 默认黑名单
     * @param pageSize
     * @param page
     */
    fun getRelationShip(relationType: String = "BLACKLIST", pageSize: Int = 10000, page: Int = 1): Observable<HttpResult<BlackListData>> {
        val map = getBaseMap()
        map["relationType"] = relationType
        map["pageSize"] = pageSize.toString()
        map["page"] = page.toString()
        return apiOTCService.getRelationShip4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * =================================红包============================================================
     */


    /**
     * 红包的初识信息
     */
    fun redPackageInitInfo(): Observable<HttpResult<RedPackageInitInfo>> {
        val map = getBaseMap()
        return redPackageService.redPackageInitInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 创建红包
     * @param type 0.普通红包 1.拼手气红包
     * @param coinSymbol 红包币种
     * @param amount 红包额度
     * @param count 红包数量
     * @param tip 红包祝福语
     * @param onlyNew 1.只针对新用户 0.不做限制
     *
     */
    fun createRedPackage(type: Int = 0, coinSymbol: String, amount: String, count: String, tip: String, onlyNew: Int): Observable<HttpResult<CreatePackageBean>> {
        val map = getBaseMap()
        map["type"] = type.toString()
        map["coinSymbol"] = coinSymbol
        map["amount"] = amount
        map["tip"] = tip
        map["count"] = count
        map["onlyNew"] = onlyNew.toString()
        return redPackageService.createRedPackage(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 红包的支付
     * @param 订单编号
     */
    fun pay4redPackage(orderNum: String, googleCode: String, smsAuthCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["orderNum"] = orderNum
        map["googleCode"] = googleCode
        map["smsAuthCode"] = smsAuthCode
        return redPackageService.pay4redPackage(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 用户发出红包的统计信息
     */
    fun getGrantRedPackageInfo(): Observable<HttpResult<GrantRedPackageInfo>> {
        val map = getBaseMap()
        return redPackageService.getGrantRedPackageInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 用户发出红包列表
     */
    fun grantRedPackageList(pageNum: Int = 1, pageSize: Int = 10): Observable<HttpResult<GrantRedPackageListBean>> {
        val map = getBaseMap()
        map["pageNum"] = pageNum.toString()
        map["pageSize"] = pageSize.toString()
        return redPackageService.grantRedPackageList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 用户发出/收到红包的详情
     * @param packetSn 红包编号
     */
    fun getRedPackageDetail(packetSn: String): Observable<HttpResult<RedPackageDetailBean>> {
        val map = getBaseMap()
        map["packetSn"] = packetSn
        return redPackageService.getRedPackageDetail(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 用户收到红包的统计信息
     */
    fun getReceiveRedPackageInfo(): Observable<HttpResult<ReceiveRedPackageInfoBean>> {
        val map = getBaseMap()
        return redPackageService.getReceiveRedPackageInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 设置资金密码
     */
    fun capitalPassword4OTC(capitalPwd: String, smsAuthCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["capitalPwd"] = capitalPwd
        if (!TextUtils.isEmpty(smsAuthCode))
            map["smsAuthCode"] = smsAuthCode
        if (!TextUtils.isEmpty(googleCode))
            map["googleCode"] = googleCode
        return apiOTCService.capitalPassword4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 重置资金密码
     */
    fun capitalPasswordReset4OTC(oldCapitalPwd: String, smsAuthCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["newCapitalPwd"] = oldCapitalPwd
        if (!TextUtils.isEmpty(smsAuthCode))
            map["smsAuthCode"] = smsAuthCode
        if (!TextUtils.isEmpty(googleCode))
            map["googleCode"] = googleCode
        return apiOTCService.capitalPasswordReset4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 用户收到红包列表
     */
    fun receiveRedPackageList(pageNum: Int = 1, pageSize: Int = 10): Observable<HttpResult<ReceiveRedPackageListBean>> {
        val map = getBaseMap()
        map["pageNum"] = pageNum.toString()
        map["pageSize"] = pageSize.toString()
        return redPackageService.receiveRedPackageList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  获取后台更新币种版本号
     */
    fun getInvitationImg(): Observable<HttpResult<InvitationImgBean>> {
        val map = getBaseMap()
        return apiService.getInvitationImg(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 游戏授权
     */
    fun getGameAuth(gameId: String, gameToken: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["gameId"] = gameId
        map["token"] = gameToken
        return apiService.getGameAuth(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * 关于我们
     */
    fun getPushSettings(): Observable<HttpResult<PushItem>> {
        val map = getBaseMap()
        return apiService.getPush(DataHandler.encryptParams(map))
    }



    fun savePushType(pushType: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["type"] = pushType
        return apiService.saveAppPushU(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 上传照片
     */
    fun uploadZip(name: File): Observable<HttpResult<Any>> {
        return apiService.uploadZip(toRequestFileBody(name))
    }

    private fun toRequestFileBody(file: File): MultipartBody.Part {
        val type = MultipartBody.FORM
        val requestFile = RequestBody.create(type, file)
        val filePart = MultipartBody.Part.createFormData("file", file.getName(), requestFile)
        return filePart
    }

    /**
     * 上传照片
     */
    fun getCoinDepth(symbol: String, contractId: String): Observable<HttpResult<DepthItem>> {
        val map = getBaseMap()
        map["symbol"] = symbol
        if (TextUtils.isEmpty(contractId)) {
            return apiService.getCoinDepth(toRequestBody(DataHandler.encryptParams(map)))
        } else {
            map["contractId"] = contractId
            return newContractService.getCoinDepth(toRequestBody(DataHandler.encryptParams(map)))
        }
    }


    fun changeNetwork(serverUrl: String, isWs: Boolean = false) {
        if(isWs){
            PublicInfoDataService.getInstance().saveNewWorkWSURL(serverUrl)
            WsAgentManager.instance.stopWs(NetUrl.getsocketAddress(), true)
            ContractSDKAgent.httpConfig?.let {
                it.contractUrl = NetUrl.getcontractUrl() + "fe-cov2-api/swap/"
                it.reConfigWsUrl(NetUrl.getContractSocketUrl())
            }
        }else{
            Log.e("我是改变地址", "serverUrl：$serverUrl")
            PublicInfoDataService.getInstance().saveNewWorkURL(serverUrl)
            HttpHelper.instance.clearServiceMap()
            refreshApi()
        }
    }




    //网络上报
    fun uploadNetWorkLog(oldLine: String, newLine: String, network_line_json: String): Observable<ResponseBody> {
        val map = getBaseMap()
        map["oldLine"] = oldLine
        map["newLine"] = newLine
        map["network_line_json"] = network_line_json
        return apiService.uploadAppNetwork(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * 上传照片
     */
    fun getHomeAdvert(): Observable<HttpResult<AdvertModel>> {
        val map = getBaseMap()
        map["terminalType"] = "1"
        return apiService.getHomeAdvert(toRequestBody(DataHandler.encryptParams(map)))
    }

    //网络信息上报
    fun uploadNetWorkInfoLog(line: String, errorType: Int = 0, page: String, action: String, duration: Long?): Observable<ResponseBody> {
        val map = getBaseMap()
        map["line"] = line
        map["errorType"] = errorType.toString()
        if (errorType == 0) {
            map["duration"] = duration.toString() ?: "0"
        }
        map["page"] = page
        map["action"] = action
        return apiService.uploadAppNetworkInfo(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * FreeStaking首页的请求
     */
    fun getFreeStakingData(): Observable<HttpResult<FreeStakingBean>> {
        val map = getBaseMap()
        return apiService.getFreeStakingData(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * FreeStaking首页项目列表
     */
    fun getFreeStakingList(): Observable<HttpResult<java.util.ArrayList<CurrencyBean>>> {
        val map = getBaseMap()
        return apiService.getFreeStakingList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * FreeStaking项目详情
     */
    fun getProjectDetail(itemId: String): Observable<HttpResult<FreeStakingDetailBean>> {
        val map = getBaseMap()
        map["id"] = itemId
        return apiService.getProjectDetail(toRequestBody(DataHandler.encryptParams(map)))


    }

    /**
     * 我的PoS记录-持仓PoS
     */
    fun getMyPosRecord(page: Int, pageSize: Int, projectType: Int, baseCoin: String = "", strTime: String = "", entTime: String = ""): Observable<HttpResult<MyPosRecordBean>> {
        val map = getBaseMap()
        map["page"] = page.toString()
        map["pageSize"] = pageSize.toString()
        map["projectType"] = projectType.toString()
        map["baseCoin"] = baseCoin
        map["strTime"] = strTime
        map["entTime"] = entTime
        return apiService.getMyPosRecord(toRequestBody(DataHandler.encryptParams(map)))

    }

    /**
     *  FreeStaking项目申购
     */
    fun requestToBuy(amount: String, projectId: Int): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["amount"] = amount
        map["projectId"] = projectId.toString()
        return apiService.requestToBuy(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  获取合约经纪人邀请配置
     */
    fun getInviteConfig(): Observable<HttpResult<AgentBean>> {
        val map = getBaseMap()
        return apiService.getInviteConfig(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  获取合约经纪人邀请配置
     */
    fun getAgentUser(): Observable<HttpResult<AgentUserBean>> {
        val map = getBaseMap()
        return apiService.getAgentUser(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  获取合约经纪人邀请配置
     */
    fun getAgentInfo(): Observable<HttpResult<AgentInfoBean>> {
        val map = getBaseMap()
        return apiService.getAgentInfo(toRequestBody(DataHandler.encryptParams(map)))
    }
}
