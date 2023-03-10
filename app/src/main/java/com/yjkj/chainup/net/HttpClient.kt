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
     * OTC ?????? service
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
         * ?????????
         */
        const val COUNTRY_CODE = "countryCode"

        /**
         * ?????????
         */
        const val MOBILE_NUMBER = "mobileNumber"

        /**
         * ????????????
         */
        const val LOGIN_PWORD = "loginPword"

        /**
         * ?????????
         */
        const val SMS_AUTHCODE = "smsAuthCode"


        /**
         * ??????
         */
        const val EMAIL = "email"

        /**
         * ???????????????
         */
        const val EMAIL_AUTHCODE = "emailAuthCode"

        /**
         * ????????????????????????
         */
        const val OPERATION_TYPE = "operationType"

        /**
         * ????????????
         */
        const val NICKNAME = "nickname"

        /**
         * GoogleKey
         */
        const val GOOGLE_KEY = "googleKey"


        /**
         * Google?????????
         */
        const val GOOGLE_CODE = "googleCode"

        /**
         * ????????????
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
                .baseUrl(NetUrl.baseUrl())  // ?????????????????????
                .client(mOkHttpClient!!)  // ??????okhttp???????????????
                .addConverterFactory(ResponseConverterFactory.create())// ???????????????,?????????Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //????????????????????????RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ApiService::class.java)
    }

    private fun createOTCApi(): OTCApiService {

        if (!StringUtil.isHttpUrl(BASE_OTC_URL))
            BASE_OTC_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_OTC_URL)  // ?????????????????????
                .client(mOkHttpClient!!)  // ??????okhttp???????????????
                .addConverterFactory(ResponseConverterFactory.create())// ???????????????,?????????Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //????????????????????????RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(OTCApiService::class.java)
    }


    fun createContractApi(): ContractApiService {
        if (!StringUtil.isHttpUrl(CONTRACT_URL))
            CONTRACT_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(CONTRACT_URL)  // ?????????????????????
                .client(mOkHttpClient!!)  // ??????okhttp???????????????
                .addConverterFactory(ResponseConverterFactory.create())// ???????????????,?????????Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //????????????????????????RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ContractApiService::class.java)
    }

    private fun createNewContractApi(): ContractApiService {
        if (!StringUtil.isHttpUrl(NEW_CONTRACT_URL))
            NEW_CONTRACT_URL = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(NEW_CONTRACT_URL)  // ?????????????????????
                .client(mOkHttpClient!!)  // ??????okhttp???????????????
                .addConverterFactory(ResponseConverterFactory.create())// ???????????????,?????????Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //????????????????????????RxJava
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit.create(ContractApiService::class.java)
    }


    private fun createRedPackageApi(): RedPackageApiService {
        var redPackUrl = NetUrl.getredPackageUrl()
        if (!StringUtil.isHttpUrl(redPackUrl))
            redPackUrl = AppConfig.default_host
        val retrofit = Retrofit.Builder()
                .baseUrl(redPackUrl)  // ?????????????????????
                .client(mOkHttpClient!!)  // ??????okhttp???????????????
                .addConverterFactory(ResponseConverterFactory.create())// ???????????????,?????????Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //????????????????????????RxJava
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
     * ??????????????????
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
     * ??????????????????
     */
    fun editNickname(nickName: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map[NICKNAME] = nickName
        return apiService.editNickname(toRequestBody(DataHandler.encryptParams(map)))
    }
    /**
     * ??????????????????
     */
    fun editSign(sign: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["description"] = sign
        return apiService.description(map)
    }


    /**
     * ????????????
     */
    fun logout(): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        return apiService.logout(toRequestBody(DataHandler.encryptParams(map)))
    }


    /*******Google????????????*START*******/

    /**
     * ??????GoogleKey
     */
    fun getGoogleKey(): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap(false)
        return apiService.getGoogleKey(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ??????Google??????
     */
    fun bindGoogleVerify(googleKey: String, loginPwd: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map[GOOGLE_KEY] = googleKey
        map["loginPwd"] = loginPwd
        map[GOOGLE_CODE] = googleCode
        return apiService.bindGoogleVerify(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ??????????????????
     */
    fun unbindGoogleVerify(smsValidCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["smsValidCode"] = smsValidCode
        map["googleCode"] = googleCode
        return apiService.unbindGoogleVerify(toRequestBody(DataHandler.encryptParams(map)))

    }


    /**
     * ??????????????????
     */
    fun unbindMobileVerify(smsValidCode: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["smsValidCode"] = smsValidCode
        map["googleCode"] = googleCode
        return apiService.unbindMobileVerify(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ??????????????????
     */
    fun openMobileVerify(): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        return apiService.openMobileVerify(toRequestBody(DataHandler.encryptParams(map)))
    }


    /*******????????????*END*******/


    /**
     * ??????????????????
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
     * ????????????
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
     * ????????????
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
     * ????????????
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
     * ????????????
     * @amount ????????????????????????????????????
     * @symbol ??????
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

    /***********????????????******START********/
    /**
     * ??????????????????
     */
    fun getAddressList(symbol: String = ""): Observable<HttpResult<AddressBean>> {
        val map = getBaseMap()
        map["coinSymbol"] = symbol
        return apiService.getAddressList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
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
     * ??????????????????
     */
    fun delWithdrawAddress(id: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["ids"] = id
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.delWithdrawAddress(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
     */
    fun getMessages(type: Int, page: Int = 1, pageSize: Int = 1000): Observable<HttpResult<MessageBean>> {
        val map = getBaseMap()
        map["messageType"] = type.toString()
        map["page"] = page.toString()
        map.put("pageSize", pageSize.toString())
        return apiService.getMessages(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????
     */
    fun getNotices(page: Int = 1, pageSize: Int = 1000): Observable<HttpResult<NoticeBean>> {
        val map = getBaseMap()
//        map["page"] = page.toString()
        map.put("pageSize", pageSize.toString())
        return apiService.getNotices(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
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
     * ??????kyc??????
     */
    fun getKYCConfig(): Observable<HttpResult<KYCBean>> {
        val map = getBaseMap(false)
        return apiService.getKYCConfig(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????
     */
    fun doAssetExchange(coinSymbol: String, amount: String, transferType: String): Observable<HttpResult<Any>> {
        //futuresType 0 ?????????   1 ?????????
         // ??????  -> ?????? ??????????????? app/co_transfer

         //  ??????  -> ??????
        //0 : ??????????????? app/co_transfer
        //1: ?????????????????? assets/saas_trans/co_to_ex
        val map = getBaseMap()
        map["coinSymbol"] = coinSymbol
        map["amount"] = amount
        map["transferType"] = transferType

        return if(transferType.equals(ContractCloudAgent.WALLET_TO_CONTRACT)){
            // ??????  -> ??????
            apiService.doAssetExchange(toRequestBody(DataHandler.encryptParams(map)))
        }else{
            // ??????  -> ??????
            val futuresType= PublicInfoDataService.getInstance().getfuturesType(null);
            if (futuresType.equals("0")){
                //?????????
                apiService.doAssetExchange(toRequestBody(DataHandler.encryptParams(map)))
            }else{
                //?????????
                contractService.coTransferEx(toRequestBody(DataHandler.encryptParams(map)))
            }
        }


    }


    /**
     * ????????????
     */
    fun authVerify(countryCode: String,
                   certType: Int,
                   certNum: String,
                   userName: String,// ???
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
     * ??????????????????
     * @param loginPwd ??????
     * @param smsCode ?????????????????????????????????
     * @param googleCode ?????????????????????????????????
     */
    fun openHandPwd(loginPwd: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["loginPwd"] = loginPwd
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.openHandPwd(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ??????????????????
     * @param loginPwd ??????
     * @param smsCode ?????????????????????????????????
     * @param googleCode ?????????????????????????????????
     */
    fun closeHandPwd(loginPwd: String, smsCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["loginPwd"] = loginPwd
        map["smsValidCode"] = smsCode
        map["googleCode"] = googleCode
        return apiService.closeHandPwd(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * ??????????????????
     */
    fun getHelpCenterList(): Observable<HttpResult<ArrayList<HelpCenterBean>>> {
        val map = getBaseMap()
        return apiService.getHelpCenterList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ??????H5?????????
     */
    fun getCommonKV(key: String = "h5_url"): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap()
        map["key"] = key
        return apiService.getCommonKV(DataHandler.encryptParams(map))
    }


    /**
     * ????????????????????????
     */
    fun cleanGesturePwd(uid: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["uid"] = uid
        return apiService.cleanGesturePwd(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????
     */
    fun getAboutUs(): Observable<HttpResult<ArrayList<AboutUSBean>>> {
        val map = getBaseMap()
        return apiService.getAboutUs(DataHandler.encryptParams(map))
    }


    /*****************OTC*************************/


    /***
     * ???????????????
     */
    fun removeRelationFromBlack(friendId: Int): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["friendId"] = friendId.toString()
        return apiOTCService.removeRelationFromBlack4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }




    /**
     * ????????????
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
     * ??????????????????
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
     * ???????????????
     */
    fun userContacts4OTC(otherUid: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["otherUid"] = otherUid.toString()
        map["relationType"] = "BLACKLIST"
        return apiOTCService.userContacts4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????????????????
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
     * ???????????? ??????  ????????????
     */
    fun getDetailsProblem(id: Int): Observable<HttpResult<OTCIMDetailsProblemBean>> {
        val map = getBaseMap()
        map["id"] = id.toString()
        return apiService.getDetailsProblem(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ???????????? ??????  ????????????
     */
    fun getReplyCreate(rqId: Int, rqReplyContent: String, contentType: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["rqId"] = rqId.toString()
        map["rqReplyContent"] = rqReplyContent
        map["contentType"] = contentType
        return apiService.getReplyCreate(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ???????????? record/other_transfer_list
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
     * ??????????????????
     * @param payment ????????????key
     * @param userName ??????
     * @param account ????????????????????????????????????????????????
     * @param qrcodeImg base64??????
     * @param bankName ????????????
     * @param bankOfDeposit ????????????
     * @param ifscCode IFSC???
     * @param remittanceInformation ????????????
     * @param smsAuthCode ????????????????????????????????????google????????????????????????
     * @param googleCode Google??????????????????????????????google????????????????????????
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
     * ??????????????????
     * @param id ????????????id
     * @param smsAuthCode ????????????????????????????????????google????????????????????????
     * @param googleCode Google??????????????????????????????google????????????????????????
     */
    fun removePayment4OTC(id: String, smsAuthCode: String = "", googleCode: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = id
        map["smsAuthCode"] = smsAuthCode
        map["googleCode"] = googleCode
        return apiOTCService.removePayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????????????????
     * @param id ????????????id
     * @param isOpen 1/0
     */
    fun operatePayment4OTC(id: String, isOpen: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = id
        map["isOpen"] = isOpen
        return apiOTCService.operatePayment4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ??????????????????
     * @param id ????????????id
     * @param payment ????????????key   --- ????????????
     * @param userName ??????
     * @param account ????????????????????????????????????????????????
     * @param qrcodeImg base64??????
     * @param bankName ????????????
     * @param bankOfDeposit ????????????
     * @param ifscCode IFSC???
     * @param remittanceInformation ????????????
     * @param smsAuthCode ????????????????????????????????????google????????????????????????
     * @param googleCode Google??????????????????????????????google????????????????????????
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
     * ??????????????????
     * @param sequence ?????????
     */
    fun getOrderDetail4OTC(sequence: String): Observable<HttpResult<OTCOrderDetailBean>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.getOrderDetail4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *
     * ????????????
     * @param sequence ?????????
     */
    fun cancelOrder4OTC(sequence: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.cancelOrder4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ???????????????
     * ????????????
     * @param sequence ?????????
     */
    fun confirmOrder2Seller4OTC(sequence: String, capitalPword: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["capitalPword"] = capitalPword
        return apiOTCService.confirmOrder2Seller4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ???????????????
     * ????????????
     * @param sequence ?????????
     */
    fun confirmPay2Buyer4OTC(sequence: String, payment: String = ""): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["payment"] = payment
        return apiOTCService.confirmPay2Buyer4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????????????????
     * @param sequence ?????????
     * @param complainId ??????id
     */
    fun complain2changeOrderState4OTC(sequence: String, complainId: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        map["complainId"] = complainId
        return apiOTCService.complain2changeOrderState4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
     * @param sequence ??????id
     */
    fun cancelComplain4OTC(sequence: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["sequence"] = sequence
        return apiOTCService.cancelComplain4OTC(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ?????????????????????(Step 3)
     */
    /**
     * ????????????????????????(Step 1)
     * @param advertId ??????id
     * @param volume ??????
     * @param price ??????
     * @param totalPrice    ?????????
     * @param payment ????????????key
     * @param description ???????????? ????????????
     * @param type ???????????????????????? price/volume
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
     * ?????????????????????(Step 3)
     * @param advertId ??????id
     * @param volume ??????
     * @param price ??????
     * @param totalPrice    ?????????
     * @param payment ????????????key
     * @param description ???????????? ????????????
     * @param capitalPword ????????????
     * @param type ???????????????????????? price/volume
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
     * ?????????????????????
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
     * ?????????????????????
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
     * ???????????? Step 3
     */
    fun findPwdStep3(token: String, certifcateNumber: String, googleCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map["token"] = token
        map["certifcateNumber"] = certifcateNumber
        map["googleCode"] = googleCode
        return apiService.findPwdStep3(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * ????????????????????????
     */
    fun newQuickLogin(quicktoken: String): Observable<HttpResult<JsonObject>> {
        val map = getBaseMap(false)
        map["quicktoken"] = quicktoken
        return apiService.newQuickLogin(toRequestBody(DataHandler.encryptParams(map)))
    }






    /**
     * 8. ??????????????????
     */
    fun changeLevel4Contract(contractId: String, newLevel: String): Observable<HttpResult<Any>> {
        val map = getBaseMap(false)
        map["contractId"] = contractId
        map["leverageLevel"] = newLevel
        return contractService.changeLevel4Contract(toRequestBody(DataHandler.encryptParams(map)))
    }



    /**
     * 16.??????????????????(need??????)
     *
     * @param item ????????????
     * @param childItem ??????????????????
     * @param startTime ????????????
     * @param endTime ????????????
     * @param page ??????
     * @param pageSize ????????????
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

    /********?????? END********/


    //????????????
    fun checkVersion(time: String): Observable<HttpResult<VersionData>> {
        return apiService.checkVersion(time)
    }

    //????????????
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
     * ?????????OKHttpClient,????????????,??????????????????,??????????????????,??????UA?????????
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
     * ???okhttp????????????????????????????????????????????????????????????????????????okhttp????????????
     */
    private inner class CacheInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            // ???????????? ????????????????????????1?????????
            val maxAge = 60 * 60
            // ??????????????????????????????1???
            val maxStale = 60 * 60 * 24
            var request = chain.request()
            request = if (NetworkUtils.isNetworkAvailable(ChainUpApp.appContext)) {
                //??????????????????????????????
                request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build()
            } else {
                //?????????????????????????????????
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


    //??????header
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
     * ????????????
     */
    fun getImageToken(operate_type: String = "1"): Observable<HttpResult<ImageTokenBean>> {
        val map = getBaseMap(false)
        map["operate_type"] = operate_type
        return apiService.getImageToken(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ?????????????????????
     */
    fun AccountCertification(): Observable<HttpResult<AccountCertificationBean>> {
        val map = getBaseMap(false)
        return apiService.AccountCertification(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ???????????????
     */
    fun AccountCertificationLanguage(): Observable<HttpResult<AccountCertificationLanguageBean>> {
        val map = getBaseMap(false)
        return apiService.AccountCertificationLanguage(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ??????????????????????????????
     */
    fun getCashFlowScene(): Observable<HttpResult<CashFlowSceneBean>> {
        val map = getBaseMap()
        return apiService.getCashFlowScene(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ??????????????????
     * @param coinSymbol ????????????
     * @param pageSize default 10
     * @param page  default 1
     * @param transactionScene ????????????
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
     * ????????????
     */
    fun cancelWithdraw(withdrawId: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["withdrawId"] = withdrawId
        return apiService.cancelWithdraw(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????????????????????????????
     */
    fun getPerson4otc(uid: String): Observable<HttpResult<UserInfo4OTC>> {
        val map = getBaseMap()
        map["uid"] = uid
        return apiOTCService.getPerson4otc(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????????????????????????????
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
     * ?????????????????????????????????
     */
    fun getReadMessageCount(): Observable<HttpResult<ReadMessageCountBean>> {
        val map = getBaseMap()
        return apiService.getReadMessageCount(toRequestBody(DataHandler.encryptParams(map)))
    }
    /**
     * ???????????????????????????
     */
    fun getInviteStatus(): Observable<HttpResult<String>> {
        val map = getBaseMap()
        return apiService.currentStatus(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ???????????????????????????
     */
    fun updateMessageStatus(): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["id"] = "0"
        return apiService.updateMessageStatus(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ???????????????
     * @param relationType ???????????? ???????????????
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
     * =================================??????============================================================
     */


    /**
     * ?????????????????????
     */
    fun redPackageInitInfo(): Observable<HttpResult<RedPackageInitInfo>> {
        val map = getBaseMap()
        return redPackageService.redPackageInitInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
     * @param type 0.???????????? 1.???????????????
     * @param coinSymbol ????????????
     * @param amount ????????????
     * @param count ????????????
     * @param tip ???????????????
     * @param onlyNew 1.?????????????????? 0.????????????
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
     * ???????????????
     * @param ????????????
     */
    fun pay4redPackage(orderNum: String, googleCode: String, smsAuthCode: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["orderNum"] = orderNum
        map["googleCode"] = googleCode
        map["smsAuthCode"] = smsAuthCode
        return redPackageService.pay4redPackage(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ?????????????????????????????????
     */
    fun getGrantRedPackageInfo(): Observable<HttpResult<GrantRedPackageInfo>> {
        val map = getBaseMap()
        return redPackageService.getGrantRedPackageInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????????????????
     */
    fun grantRedPackageList(pageNum: Int = 1, pageSize: Int = 10): Observable<HttpResult<GrantRedPackageListBean>> {
        val map = getBaseMap()
        map["pageNum"] = pageNum.toString()
        map["pageSize"] = pageSize.toString()
        return redPackageService.grantRedPackageList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????/?????????????????????
     * @param packetSn ????????????
     */
    fun getRedPackageDetail(packetSn: String): Observable<HttpResult<RedPackageDetailBean>> {
        val map = getBaseMap()
        map["packetSn"] = packetSn
        return redPackageService.getRedPackageDetail(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ?????????????????????????????????
     */
    fun getReceiveRedPackageInfo(): Observable<HttpResult<ReceiveRedPackageInfoBean>> {
        val map = getBaseMap()
        return redPackageService.getReceiveRedPackageInfo(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ??????????????????
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
     * ??????????????????
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
     * ????????????????????????
     */
    fun receiveRedPackageList(pageNum: Int = 1, pageSize: Int = 10): Observable<HttpResult<ReceiveRedPackageListBean>> {
        val map = getBaseMap()
        map["pageNum"] = pageNum.toString()
        map["pageSize"] = pageSize.toString()
        return redPackageService.receiveRedPackageList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  ?????????????????????????????????
     */
    fun getInvitationImg(): Observable<HttpResult<InvitationImgBean>> {
        val map = getBaseMap()
        return apiService.getInvitationImg(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????
     */
    fun getGameAuth(gameId: String, gameToken: String): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["gameId"] = gameId
        map["token"] = gameToken
        return apiService.getGameAuth(toRequestBody(DataHandler.encryptParams(map)))
    }


    /**
     * ????????????
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
     * ????????????
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
     * ????????????
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
            Log.e("??????????????????", "serverUrl???$serverUrl")
            PublicInfoDataService.getInstance().saveNewWorkURL(serverUrl)
            HttpHelper.instance.clearServiceMap()
            refreshApi()
        }
    }




    //????????????
    fun uploadNetWorkLog(oldLine: String, newLine: String, network_line_json: String): Observable<ResponseBody> {
        val map = getBaseMap()
        map["oldLine"] = oldLine
        map["newLine"] = newLine
        map["network_line_json"] = network_line_json
        return apiService.uploadAppNetwork(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * ????????????
     */
    fun getHomeAdvert(): Observable<HttpResult<AdvertModel>> {
        val map = getBaseMap()
        map["terminalType"] = "1"
        return apiService.getHomeAdvert(toRequestBody(DataHandler.encryptParams(map)))
    }

    //??????????????????
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
     * FreeStaking???????????????
     */
    fun getFreeStakingData(): Observable<HttpResult<FreeStakingBean>> {
        val map = getBaseMap()
        return apiService.getFreeStakingData(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * FreeStaking??????????????????
     */
    fun getFreeStakingList(): Observable<HttpResult<java.util.ArrayList<CurrencyBean>>> {
        val map = getBaseMap()
        return apiService.getFreeStakingList(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     * FreeStaking????????????
     */
    fun getProjectDetail(itemId: String): Observable<HttpResult<FreeStakingDetailBean>> {
        val map = getBaseMap()
        map["id"] = itemId
        return apiService.getProjectDetail(toRequestBody(DataHandler.encryptParams(map)))


    }

    /**
     * ??????PoS??????-??????PoS
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
     *  FreeStaking????????????
     */
    fun requestToBuy(amount: String, projectId: Int): Observable<HttpResult<Any>> {
        val map = getBaseMap()
        map["amount"] = amount
        map["projectId"] = projectId.toString()
        return apiService.requestToBuy(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  ?????????????????????????????????
     */
    fun getInviteConfig(): Observable<HttpResult<AgentBean>> {
        val map = getBaseMap()
        return apiService.getInviteConfig(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  ?????????????????????????????????
     */
    fun getAgentUser(): Observable<HttpResult<AgentUserBean>> {
        val map = getBaseMap()
        return apiService.getAgentUser(toRequestBody(DataHandler.encryptParams(map)))
    }

    /**
     *  ?????????????????????????????????
     */
    fun getAgentInfo(): Observable<HttpResult<AgentInfoBean>> {
        val map = getBaseMap()
        return apiService.getAgentInfo(toRequestBody(DataHandler.encryptParams(map)))
    }
}
