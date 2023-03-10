package com.yjkj.chainup.model.model

import android.text.TextUtils
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.model.api.ContractApiService
import com.yjkj.chainup.model.api.MainApiService
import com.yjkj.chainup.model.datamanager.BaseDataManager
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.net.HttpClient.Companion.LOGIN_PWORD
import com.yjkj.chainup.net.HttpClient.Companion.MOBILE_NUMBER
import com.yjkj.chainup.net.HttpClient.Companion.VERIFICATION_TYPE
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.net.api.HttpResult
import com.yjkj.chainup.util.*
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-08-28 16:51

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-08-28 16:51

 * @UpdateRemark:   更新说明

 */
class MainModel : BaseDataManager() {

    val TAG = "MainModel"


    fun limit_ip_login(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .limit_ip_login(getBaseReqBody()), consumer
        )
    }


    /**
     * 币币收益分析
     */
    fun accountStats(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).accountStats(getBaseReqBody()),
            consumer
        )
    }

    /**
     * 合约收益分析
     */
    fun accountStatsCon(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getContractUrlService(ContractApiService::class.java)
                .accountStatsCon(getBaseReqBody()), consumer
        )
    }

    /**
     * 获取 public_info_v4
     * @param consumer
     * @return
     */
    fun public_info_v4(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .public_info_v4(getBaseReqBody()), consumer
        )
    }


    /**
     * 13. 账户余额 ：
     */
    fun account_balance(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getContractUrlService(ContractApiService::class.java)
                .getAccountBalance4Contract1(getBaseReqBody()), consumer
        )
    }

    /**
     * 交易账户
     */
    fun accountBalance(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .accountBalance(getBaseReqBody()), consumer
        )
    }

    /**
     * 获取热门币种
     */
    fun getHotcoin(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getHotcoin(getBaseReqBody()),
            consumer
        )
    }

    /**
     * 13. 账户余额 ：
     */
    fun getAccountBalance4Contract(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getContractUrlService(ContractApiService::class.java)
                .getAccountBalance4Contract1(getBaseReqBody()), consumer
        )
    }

    /**
     * 新首页 头部币对24小时行情
     */
    fun header_symbol(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).header_symbol(getBaseMaps()),
            consumer
        )
    }

    /**
     * 新首页
     */
    fun common_index(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).common_index(getBaseReqBody()),
            consumer
        )
    }

    /**
     * 我的资金
     */
    fun otc_account_list(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .otc_account_list(getBaseReqBody()), consumer
        )
    }

    /**
     * 新首页 涨跌幅榜和成交量榜单
     *
     * type : rasing：涨幅榜 falling：跌幅榜 deal：成交量榜
     */
    fun trade_list_v4(type: String?, consumer: DisposableObserver<ResponseBody>): Disposable? {
        var params = getBaseMaps()//HttpParams.getInstance(1).build()
        if (null != type) {
            params["type"] = type
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .trade_list_v4(getBaseReqBody(params)), consumer
        )
    }


    /**********币币交易START*******************/
    /**
     * 1.创建订单(币币)
     * @param side 买卖方向 BUY，SELL
     * @param type 挂单类型，1:限价委托、2:市价委托
     * @param volume type=1:表示买卖数,type=2:买则表示总价格，卖表示总个数
     * @param price 委托单价：type=2：不需要此参数
     * @param symbol 市场标记，ethbtc
     * @param isLever 是否是杠杆
     */
    fun createOrder(
        side: String,
        type: Int,
        volume: String,
        price: String,
        symbol: String,
        isLever: Boolean = false,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {

        val hashMap = getBaseMaps().apply {
            this["side"] = side
            this["type"] = type.toString()
            this["volume"] = volume
            if (type == 2)
                this["price"] = ""
            else
                this["price"] = price
            this["symbol"] = symbol
        }
        val mainApiService = httpHelper.getBaseUrlService(MainApiService::class.java)
        return if (isLever) {
            changeIOToMainThread(
                mainApiService.createOrder4Lever(getBaseReqBody(hashMap)),
                consumer
            )
        } else {
            changeIOToMainThread(mainApiService.createOrder(getBaseReqBody(hashMap)), consumer)
        }
    }


    /**
     * 2.取消订单(币币)
     * @param isLever 是否是杠杆
     */
    fun cancelOrder(
        order_id: String,
        symbol: String,
        isLever: Boolean = false,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["orderId"] = order_id
            this["symbol"] = symbol
        }
        val mainApiService = httpHelper.getBaseUrlService(MainApiService::class.java)
        return if (isLever) {
            changeIOToMainThread(
                mainApiService.cancelOrder4Lever(getBaseReqBody(hashMap)),
                consumer
            )
        } else {
            changeIOToMainThread(mainApiService.cancelOrder(getBaseReqBody(hashMap)), consumer)
        }
    }


    /**
     * 3.获取当前委托(币币)
     */
    fun getNewEntrust(
        symbol: String="",
        type: String = "",
        side: String = "",
        isLever: Boolean = false,
        isOnly20: Boolean = true,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        if (!UserDataService.getInstance().isLogined) return null
        val hashMap = getBaseMaps().apply {
            this["symbol"] = symbol
            this["page"] = "1"
            this["pageSize"] = if (isOnly20) "50" else "200"
            this["type"] = type
            this["side"] = side
        }
        val mainApiService = httpHelper.getBaseUrlService(MainApiService::class.java)
        return if (isLever) {
            changeIOToMainThread(mainApiService.newOrders4Lever(getBaseReqBody(hashMap)), consumer)
        } else {
            changeIOToMainThread(mainApiService.getNewEntrust(getBaseReqBody(hashMap)), consumer)
        }
    }


    /**
     * 4.获取历史委托(币币)
     * @param symbol
     * @param pageSize default 10
     * @param page  default 1
     * @param isShowCanceled 是否展示已取消的订单，0表示不展示，1表示展示，默认1
     * @param side 订单买卖方向，BUY买入 SELL卖出 ,不传全部
     * @param type 委托类型：1 limit，2 market ,不传全部
     * @param startTime 年月日，禁止输入时分秒：2019-04-22
     * @param endTime 年月日，禁止输入时分秒：2019-04-22
     */
    fun getHistoryEntrust4(
        symbol: String,
        pageSize: String = "10",
        page: String = "1",
        isShowCanceled: String = "1",
        side: String = "",
        type: String = "",
        statusType: String = "",
        isLever: Boolean = false,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["symbol"] = symbol
            this["pageSize"] = pageSize
            this["page"] = page
            this["isShowCanceled"] = isShowCanceled
            this["side"] = side
            this["type"] = type
            this["status"] = statusType
        }
        val mainApiService = httpHelper.getBaseUrlService(MainApiService::class.java)
        return if (isLever) {
            changeIOToMainThread(
                mainApiService.historyOrders4Lever(getBaseReqBody(hashMap)),
                consumer
            )
        } else {
            changeIOToMainThread(
                mainApiService.getHistoryEntrust4(getBaseReqBody(hashMap)),
                consumer
            )
        }
    }


    /**
     * 获取历史委托
     * @param entrust 1:当前委托、2：历史委托
     * @param side  BUY：买、SELL：卖
     * @param symbol 币对
     * @param orderType 订单类型1:常规订单，2 杠杆订单
     * @param status  订单状态：1 新订单，2 已完成，3 部分成交，4 已取消，5 待撤销，6 异常单
     * @param isShowCanceled 0:不展示已撤单、其余默认展示已撤单
     * @param quote 所在交易区（usdt。。。）
     * @param page 分页
     * @param pageSize 页面大小
     */
    fun getNewEntrustSearch(
        side: String = "",
        symbol: String = "",
        isShowCanceled: String = "1",
        statusType: Int = 0,
        type: String = "",
        page: String = "1",
        pageSize: String = "100",
        isLever: Boolean = false,
        entrust: String = "2",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {

        val map = getBaseMaps().apply {
            this["entrust"] = entrust
            this["side"] = side
            this["symbol"] = symbol
            this["orderType"] = if (isLever) "2" else "1"
            this["isShowCanceled"] = isShowCanceled
            if (type != "0") {
                this["type"] = type
            }
            this["page"] = page
            this["pageSize"] = pageSize
            if (statusType != 0) {
                this["status"] = when (statusType) {
                    1 -> "2"
                    else -> "4"
                }
            }
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getNewEntrustSearch(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取历史委托
     * @param entrust 1:当前委托、2：历史委托
     * @param side  BUY：买、SELL：卖
     * @param symbol 币对
     * @param orderType 订单类型1:常规订单，2 杠杆订单
     * @param status  订单状态：1 新订单，2 已完成，3 部分成交，4 已取消，5 待撤销，6 异常单
     * @param isShowCanceled 0:不展示已撤单、其余默认展示已撤单
     * @param quote 所在交易区（usdt。。。）
     * @param page 分页
     * @param pageSize 页面大小
     */
    fun getNewCurrentEntrustSearch(
        side: String = "",
        symbol: String = "",
        isShowCanceled: String = "0",
        type: String = "",
        page: String = "1",
        pageSize: String = "100",
        isLever: Boolean = false,
        entrust: String = "1",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {

        val map = getBaseMaps().apply {
            this["entrust"] = entrust
            this["side"] = side
            this["symbol"] = symbol
            this["orderType"] = if (isLever) "2" else "1"
            this["isShowCanceled"] = isShowCanceled
            if (type.isNotEmpty()) {
                this["type"] = type
            }
            this["page"] = page
            this["pageSize"] = pageSize
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getNewEntrustSearch(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 委托详情
     * @param needOrder 0:不要订单 1:要订单
     */
    fun getEntrustDetail4(
        id: String, symbol: String, pageSize: String = "10",
        page: String = "1", consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["order_id"] = id
            this["symbol"] = symbol
            this["needOrder"] = "1"
            this["pageSize"] = pageSize
            this["page"] = page
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getEntrustDetail4(getBaseReqBody(hashMap)), consumer
        )
    }

    /**
     * 委托详情
     * @param needOrder 0:不要订单 1:要订单
     */
    fun getLeverEntrustDetail4(
        id: String, symbol: String, pageSize: String = "10",
        page: String = "1", consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["order_id"] = id
            this["symbol"] = symbol
            this["pageSize"] = pageSize
            this["page"] = page
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getLeverEntrustDetail4(getBaseReqBody(hashMap)), consumer
        )
    }


    /**
     *  5.获取交易限制文案
     */
    fun getTradeLimitInfo(
        symbol: String?,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["symbol"] = symbol ?: ""
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getTradeLimitInfo(getBaseReqBody(hashMap)), consumer
        )
    }

    /**********币币交易END*******************/


    /**
     * 获取币种简介(app4.0)
     */
    fun getCoinIntro(coin: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["coinSymbol"] = coin
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getCoinIntro(getBaseReqBody(hashMap)), consumer
        )
    }

    /*************登录注册***************/
    /**
     * 手机登陆&邮箱登陆
     * 统一使用
     */
    fun getLoginByMobile(
        account: String = "",
        password: String = "",
        verificationType: Int = 0,
        geetest_challenge: String = "",
        geetest_validate: String = "",
        geetest_seccode: String = "",
        json: Map<String, String>? = null,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this[MOBILE_NUMBER] = account
            this[LOGIN_PWORD] = password
            this[VERIFICATION_TYPE] = verificationType.toString()
            if (json != null) {
                putAll(json)
                if (json.containsKey("sessionId")) {
                    this["csessionid"] = json["sessionId"] ?: ""
                }
                this["scene"] = "other"
            } else {
                this["geetest_challenge"] = geetest_challenge
                this["geetest_validate"] = geetest_validate
                this["geetest_seccode"] = geetest_seccode
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .loginByMobile(getBaseReqBody(hashMap)), consumer
        )
    }

    /**
     * 登录确认
     * @param authCode 验证码
     * @param 1 谷歌验证，2 短信验证，3 邮箱验证
     */
    fun confirmLogin(
        authCode: String,
        checkType: String = "1",
        token: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["authCode"] = authCode
            this["checkType"] = checkType
            this["token"] = token
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .confirmLogin(getBaseReqBody(hashMap)), consumer
        )
    }


    /**
     * 注册(Step 2)
     * 手机&邮箱
     * @param registerCode 填写手机号或者邮箱
     * @param numberCode 邮箱或者短信验证码
     */
    fun reg4Step2(
        registerCode: String,
        numberCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["registerCode"] = registerCode
            this["numberCode"] = numberCode
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .reg4Step2(getBaseReqBody(hashMap)), consumer
        )
    }

    /**
     * 找回密码 Step 2
     */
    fun findPwdStep2(
        token: String,
        smsCode: String,
        mobileNumber: String,
        emailCode: String,
        email: String,
        certifcateNumber: String,
        googleCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["token"] = token
            if (!TextUtils.isEmpty(mobileNumber)) {
                this["smsCode"] = smsCode
                this["mobileNumber"] = mobileNumber
            }
            if (!TextUtils.isEmpty(email)) {
                this["emailCode"] = emailCode
                this["email"] = email
            }
            if (!TextUtils.isEmpty(certifcateNumber)) {
                this["certifcateNumber"] = certifcateNumber
            }
            if (!TextUtils.isEmpty(googleCode)) {
                this["googleCode"] = googleCode
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .findPwdStep2(getBaseReqBody(hashMap)), consumer
        )
    }

    /**
     * 获取用户信息
     */
    fun getUserInfo(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).user_info(getBaseReqBody()),
            consumer
        )
    }

    fun saveUserInfo() {
        var consumer: DisposableObserver<ResponseBody> = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                UserDataService.getInstance().saveData(json)

            }
        }

        changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).user_info(getBaseReqBody()),
            consumer
        )
    }

    /**
     * 获取用户签名
     */
    fun getSign(consumer: DisposableObserver<HttpResult<String?>>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getDescriptionUsing(), consumer
        )
    }


    /**
     * 获取是否模拟账号
     */
    fun testUser(
        userName: String,
        consumer: DisposableObserver<HttpResult<Boolean?>>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["username"] = userName
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .testUser(getBaseReqBody(hashMap)), consumer
        )
    }


    /**
     * 找回密码 Step 4
     */
    fun findPwdStep4(
        token: String,
        loginPword: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["token"] = token
            this["loginPword"] = loginPword
//            this["newPassword"] = loginPword
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .findPwdStep4(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 注册(Step 3)
     * 手机&邮箱
     * @param registerCode 手机或者邮箱验证码
     * @param loginPword 登录密码
     * @param newPassword 确认密码
     * @param invitedCode    邀请码
     */
    fun reg4Step3(
        registerCode: String,
        loginPword: String,
        newPassword: String,
        invitedCode: String = "", consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["registerCode"] = registerCode
            this["loginPword"] = loginPword
            this["newPassword"] = newPassword
            this["invitedCode"] = invitedCode
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).reg4Step3(getBaseReqBody(map)),
            consumer
        )
    }

    /**
     * 设置手势密码
     * @param token 开启手势密码第一步成功时返回的字符串（必填）
     */
    fun setHandPwd(
        token: String,
        handPwd: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["handPwd"] = handPwd
            this["token"] = token
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .setHandPwd(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 找回密码 Step 1
     */
    fun findPwdStep1(
        mobileNumber: String,
        email: String,
        verificationType: Int = 0,
        geetest_challenge: String = "",
        geetest_validate: String = "",
        geetest_seccode: String = "",
        aliyun: Map<String, String>? = null,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (!TextUtils.isEmpty(mobileNumber)) {
                this["mobileNumber"] = mobileNumber
            }
            if (!TextUtils.isEmpty(email)) {
                this["email"] = email
            }
            this["verificationType"] = verificationType.toString()
            if (aliyun != null) {
                putAll(aliyun)
                if (aliyun.containsKey("sessionId")) {
                    this["csessionid"] = aliyun["sessionId"] ?: ""
                }
                this["scene"] = "other"
            } else {
                this["geetest_challenge"] = geetest_challenge
                this["geetest_validate"] = geetest_validate
                this["geetest_seccode"] = geetest_seccode
            }
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .findPwdStep1(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 注册(Step 1)
     * 手机&邮箱
     */
    fun reg4Step1(
        country: String = "86",
        mobile: String = "",
        wemail: String = "",
        verificationType: Int,
        geetest_challenge: String = "",
        geetest_validate: String = "",
        geetest_seccode: String = "",
        json: Map<String, String>? = null,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["country"] = country
            if (StringUtils.isNumeric(mobile)) {
                this["mobile"] = mobile
                this["email"] = ""
            } else {
                this["mobile"] = ""
                this["email"] = mobile
            }

            this["verificationType"] = verificationType.toString()
            if (json != null) {
                putAll(json)
                if (json.containsKey("sessionId")) {
                    this["csessionid"] = json["sessionId"] ?: ""
                }
                this["scene"] = "other"
            } else {
                this["geetest_challenge"] = geetest_challenge
                this["geetest_validate"] = geetest_validate
                this["geetest_seccode"] = geetest_seccode
            }
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).reg4Step1(getBaseReqBody(map)),
            consumer
        )
    }


    /*************登录注册END***************/

    /*
     *  获取服务端的用户自选币对
     */
    fun getOptionalSymbol(consumer: DisposableObserver<ResponseBody>, symbol: String): Disposable? {
        return if (TextUtils.isEmpty(symbol)) {
            changeIOToMainThread(
                httpHelper.getBaseUrlService(MainApiService::class.java)
                    .getOptionalSymbol(getBaseReqBody()), consumer
            )
        } else {
            val map = getBaseMaps().apply {
                this["symbol"] = symbol
            }
            changeIOToMainThread(
                httpHelper.getBaseUrlService(MainApiService::class.java)
                    .getOptionalSymbol(getBaseReqBody(map)), consumer
            )
        }
    }

    /*
     *  获取服务端的用户自选币对
     */
    fun addOrDeleteSymbol(
        type: Int = 0,
        list: ArrayList<String>?,
        symbol: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["operationType"] = type.toString()

            if (null != list && list.size > 0) {
                var builder = StringBuilder()
                list.forEach {
                    builder.append(it)
                    builder.append(",")
                }
                this["symbols"] = builder.substring(0, builder.length - 1)
            } else {
                this["symbols"] = ""
            }
            if(!TextUtils.isEmpty(symbol)) {
                this["symbol"] = symbol
            }
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .addOrDeleteSymbol(getBaseReqBody(map)), consumer
        )
    }

    /*
     *  common_rate
     */
    fun common_rate(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).common_rate(getBaseReqBody()),
            consumer
        )
    }


    /******B2c****/
    /**
     * 法币资产列表
     */
    fun fiatBalance(symbol: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatBalance(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 充值
     * @param symbol 币种
     * @param transferVoucher 转账凭证
     * @param amount 转账金额
     */
    fun fiatDeposit(
        symbol: String,
        transferVoucher: String,
        amount: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["transferVoucher"] = transferVoucher
            this["amount"] = amount
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatDeposit(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 充值记录
     */
    fun fiatDepositList(
        symbol: String,
        page: String = "1",
        pageSize: String = "100",
        startTime: String = "",
        endTime: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["page"] = page
            this["pageSize"] = pageSize
            this["startTime"] = startTime
            this["endTime"] = endTime
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatDepositList(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 充值撤销
     */
    fun fiatCancelDeposit(id: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatCancelDeposit(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 提现
     * @param symbol 币种
     * @param userWithdrawBankId  用户提现银行id
     * @param amount 转账金额
     */
    fun fiatWithdraw(
        symbol: String,
        userWithdrawBankId: String,
        amount: String,
        smsAuthCode: String,
        googleCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["userWithdrawBankId"] = userWithdrawBankId
            this["amount"] = amount
            this["smsAuthCode"] = smsAuthCode
            this["googleCode"] = googleCode
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatWithdraw(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 提现记录
     */
    fun fiatWithdrawList(
        symbol: String,
        startTime: String = "",
        endTime: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["startTime"] = startTime
            this["endTime"] = endTime
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatWithdrawList(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 提现撤销
     */
    fun fiatCancelWithdraw(id: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatCancelWithdraw(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 用户提现银行列表
     */
    fun fiatBankList(
        symbol: String,
        page: String = "1",
        pageSize: String = "10",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["page"] = page
            this["pageSize"] = pageSize
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatBankList(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 查询用户提现银行
     */
    fun fiatGetBank(id: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatGetBank(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 新增用户提现银行
     * @param bankId 银行id
     * @param bankSub 支行
     * @param cardNo 卡号
     * @param name 人名
     * @param symbol 币种
     * @param smsAuthCode 短信验证码
     * @param googleCode 谷歌验证码
     */
    fun fiatAddBank(
        bankId: String,
        bankSub: String,
        cardNo: String,
        name: String,
        symbol: String,
        smsAuthCode: String,
        googleCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["bankId"] = bankId
            this["bankSub"] = bankSub
            this["cardNo"] = cardNo
            this["name"] = name
            this["symbol"] = symbol
            this["smsAuthCode"] = smsAuthCode
            this["googleCode"] = googleCode
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatAddBank(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 修改用户提现银行
     */
    fun fiatEditBank(
        id: String,
        bankId: String,
        bankSub: String,
        cardNo: String,
        name: String,
        symbol: String,
        smsAuthCode: String,
        googleCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
            this["bankId"] = bankId
            this["bankSub"] = bankSub
            this["cardNo"] = cardNo
            this["name"] = name
            this["symbol"] = symbol
            this["smsAuthCode"] = smsAuthCode
            this["googleCode"] = googleCode
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatEditBank(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 删除用户提现银行
     */
    fun fiatDeleteBank(id: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatDeleteBank(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 查询平台充值银行信息
     */
    fun fiatBankInfo(symbol: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatBankInfo(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 查询平台支持提现银行列表
     */
    fun fiatAllBank(symbol: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .fiatAllBank(getBaseReqBody(map)), consumer
        )
    }


    /**
     *图片临时token
     * @param operate_type 1 实名认证 2 其它
     */
    fun getImageToken(
        operate_type: String = "1",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["operate_type"] = operate_type
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getImageToken(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 上传照片
     */
    fun uploadImg(imgBase64: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["imageData"] = imgBase64
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).uploadImg(getBaseReqBody(map)),
            consumer
        )
    }


    /**
     * app公告详情页
     */
    fun getNoticeDetail(id: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            if (StringUtil.checkStr(id)) {
                this["id"] = id
            }
            this["dayType"] = PublicInfoDataService.getInstance().themeMode.toString()
            this["lan"] = LanguageUtil.getSelectLanguage()
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getNoticeDetail(map), consumer
        )
    }


    /**
     * 获取H5的域名
     */
    fun getCommonKV(
        param: String? = "h5_url",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps()
        if (StringUtil.checkStr(param)) {
            map["key"] = param!!
        } else {
            map["key"] = "h5_url"
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getCommonKV(map), consumer
        )
    }

    /**
     * 获取App版本
     */
    fun getAppVersion(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getAppVersion(DataHandler.encryptParams(getBaseMaps())), consumer
        )
    }

    /**
     * 用户体系首页
     */
    fun getRoleIndex(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getRoleIndex(getBaseReqBody()),
            consumer
        )
    }

    /*
     *  手势密码
     */
    fun getGesturePwd(
        uid: String,
        gesturePwd: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps()
        if (StringUtil.checkStr(uid)) {
            map["uid"] = uid
        }
        if (StringUtil.checkStr(gesturePwd)) {
            map["gesturePwd"] = gesturePwd
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getGesturePwd(getBaseReqBody(map)), consumer
        )
    }


    /**********杠杆*************/
    /**
     * 当前申请(未归还记录)
     */
    fun borrowNew(
        symbol: String,
        startTime: String = "",
        endTime: String = "",
        page: String = "1",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps()
        map.apply {
            this["symbol"] = symbol
            if (!TextUtils.isEmpty(startTime)) {
                this["startTime"] = startTime
            }
            if (!TextUtils.isEmpty(endTime)) {
                this["endTime"] = endTime
            }
            this["page"] = page
            this["pageSize"] = pageSize
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).borrowNew(getBaseReqBody(map)),
            consumer
        )
    }


    /**
     * 历史申请(已归还记录)
     */
    fun borrowHistory(
        symbol: String,
        startTime: String = "",
        endTime: String = "",
        page: String = "1",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps()
        map.apply {
            this["symbol"] = symbol
            if (!TextUtils.isEmpty(startTime)) {
                this["startTime"] = startTime
            }
            if (!TextUtils.isEmpty(endTime)) {
                this["endTime"] = endTime
            }
            this["page"] = page
            this["pageSize"] = pageSize
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .borrowHistory(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 杠杆账户列表
     */

    fun getBalanceList(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getBalanceList(getBaseReqBody()), consumer
        )
    }

    /**
     * 归还
     */
    fun setReturn(
        id: String,
        amount: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        var map = getBaseMaps().apply {
            this["id"] = id
            this["amount"] = amount
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).setReturn(getBaseReqBody(map)),
            consumer
        )
    }

    /**
     * 借贷
     */
    fun setBorrow(
        symbol: String,
        coin: String,
        amount: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        var map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["coin"] = coin
            this["amount"] = amount
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).setBorrow(getBaseReqBody(map)),
            consumer
        )
    }


    /**
     * 根据币对获取账户信息
     */
    fun getBalance4Lever(symbol: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        if (!UserDataService.getInstance().isLogined) return null
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getBalance4Lever(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 根据币对获取账户信息
     */
    fun setTransfer4Lever(
        fromAccount: String,
        toAccount: String,
        amount: String,
        coinSymbol: String,
        symbol: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["fromAccount"] = fromAccount
            this["toAccount"] = toAccount
            this["amount"] = amount
            this["coinSymbol"] = coinSymbol
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .setTransfer4Lever(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 划转首页
     */
    fun transher4OTC(
        fromAccount: String,
        toAccount: String,
        amount: String,
        coinSymbol: String?,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["fromAccount"] = fromAccount
            this["toAccount"] = toAccount
            this["amount"] = amount
            if (null != coinSymbol) {
                this["coinSymbol"] = coinSymbol
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .transher4OTC(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 获取某币种的交易账户
     */
    fun accountGetCoin4OTC(coin: String?, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            if (null != coin) {
                this["coin"] = coin
            }
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .accountGetCoin4OTC(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 获取明细
     */
    fun getDetail4Lever(
        id: String,
        page: String = "1",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["id"] = id
            this["page"] = page
            this["pageSize"] = pageSize
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getDetail4Lever(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取明细
     */
    fun getTransferList(
        symbol: String = "",
        transactionType: String = "",
        coinSymbol: String = "",
        page: String = "1",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["coinSymbol"] = coinSymbol
            this["symbol"] = symbol
            this["transactionType"] = transactionType
            this["page"] = page
            this["pageSize"] = pageSize
        }

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getTransferList(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 获取总资产首页用
     */
    fun getTotalAsset(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getContractTotalAccountBalanceV2(getBaseReqBody()), consumer
        )

    }

    /**
     * 根据币种获取手续费和提现地址
     */
    fun getCost(symbol: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getCost(getBaseReqBody(map)),
            consumer
        )
    }

    /**
     * 根据币种获取手续费和提现地址
     */
    fun addWithdrawAddrValidate(
        symbol: String = "",
        address: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["coinSymbol"] = symbol
            this["address"] = address
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .addWithdrawAddrValidate(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 获取币对etf净值
     * @param base 基础货币
     * @param quote     计价货币
     */
    fun getETFValue(
        base: String,
        quote: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["base"] = base
            this["quote"] = quote
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getETFValue(getBaseReqBody(map)), consumer
        )
    }

    /**
     * ETF免责信息url和域名
     */
    fun getETFInfo(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getETFInfo(getBaseReqBody()),
            consumer
        )
    }


    /**
     * 获取账户余额信息
     */
    fun getAccountBalance(
        coinSymbols: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["coinSymbols"] = coinSymbols
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .accountBalance(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 手势登录new
     */
    fun newHandLogin(
        quicktoken: String,
        handPwd: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["quicktoken"] = quicktoken
            this["handPwd"] = handPwd
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .newHandLogin(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 手势开启  引导页new
     */
    fun newOpenHand(
        quicktoken: String,
        handPwd: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["quicktoken"] = quicktoken
            this["handPwd"] = handPwd
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .newOpenHand(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取用户身份认证所需要的信息
     */
    fun getIdentityAuthInfo(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getIdentityAuthInfo(getBaseReqBody()), consumer
        )
    }

    /**
     * 获取币对etf净值
     * @param base 基础货币
     * @param quote     计价货币
     */
    fun submitAuthInfoCheck(
        idNumber: String,
        userName: String,
        withdrawId: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["idNumber"] = idNumber
            this["userName"] = userName
            this["withdrawId"] = withdrawId
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .submitAuthInfoCheck(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 登录确认
     * @param authCode 验证码
     * @param 1 谷歌验证，2 短信验证，3 邮箱验证
     */
    fun confirmLoginV2(
        array: Map<String, String>,
        token: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            for ((key, value) in array) {
                this[key.verfitionTypeCheck()] = value
            }
            this["token"] = token
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .confirmLogin(getBaseReqBody(hashMap)), consumer
        )
    }


    /**
     * 获取首页数据
     */
    fun getHomeData(type: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["type"] = type
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java).getHome(getBaseReqBody(map)),
            consumer
        )
    }

    /**
     * 获取首页数据
     */
    fun getChargeAddress(
        symbol: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getChargeAddress(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取 publicInfoMarket
     * @param consumer
     * @return
     */
    fun publicInfoMarket(consumer: DisposableObserver<ResponseBody>): Disposable? {

        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .publicInfoMarket(getBaseReqBody()), consumer
        )
    }

    /**
     *
     * @param consumer
     * @return
     */
    fun getCommonRecommendCoin(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getCommonRecommendCoin(getBaseReqBody(null)), consumer
        )
    }

    /**
     *
     * @param consumer
     * @return
     */
    fun likesCoinsUpload(
        symbols: String = "",
        symbol: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (symbols.isNotEmpty()) {
                this["symbols"] = symbols
            }
            if(!TextUtils.isEmpty(symbol)) {
                this["symbol"] = symbol
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .optionalUploadSymbol(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 新版本获取合约总资产接口
     */
    fun contractTotalAccountBalanceV2(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getContractTotalAccountBalanceV2(getBaseReqBody()), consumer
        )
    }

    /**
     * 查询（AI）配置
     * @return
     */
    fun getAIStrategyInfo(
        symbol: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getAIStrategyInfo(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 保存策略
     * @param symbol 币对 BTC/USDT
     * @param quantType 量化交易类型 1:网格
     * @param gridLineType  网格类型 1:等差 2:等比
     * @param gridNumber 网格数量
     * @param lowestPrice 网格下限
     * @param highestPrice 网格上限
     * @param stopHighPrice 停止网格上限
     * @param stopLowPrice 停止网格下限
     * @param totalQuoteAmount 用户投入资产
     * @param useOwnBase 是否使用Base资产 0:不使用 1:使用
     * @return
     */
    fun saveStrategy(
        symbol: String,
        quantType: String,
        gridLineType: String,
        gridNumber: String,
        lowestPrice: String,
        highestPrice: String,
        stopHighPrice: String,
        stopLowPrice: String,
        totalQuoteAmount: String,
        useOwnBase: String,
        fee: String,
        totalBaseAmount: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["quantType"] = quantType
            this["gridLineType"] = gridLineType
            this["gridNumber"] = gridNumber
            this["lowestPrice"] = lowestPrice
            this["highestPrice"] = highestPrice
            if (stopHighPrice.isEmpty()) {
                this["stopHighPrice"] = "0"
            } else {
                this["stopHighPrice"] = stopHighPrice
            }
            if (stopLowPrice.isEmpty()) {
                this["stopLowPrice"] = "0"
            } else {
                this["stopLowPrice"] = stopLowPrice
            }

            this["totalQuoteAmount"] = totalQuoteAmount
            this["useOwnBase"] = useOwnBase
            this["fee"] = fee
            this["totalBaseAmount"] = totalBaseAmount

        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .saveStrategy(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 计算使用base投入总资产
     * @param symbol 币对 BTC/USDT
     * @param gridLineType  网格类型 1:等差 2:等比
     * @param gridNumber 网格数量
     * @param lowestPrice 网格下限
     * @param highestPrice 网格上限
     * @param totalQuoteAmount 用户投入资产
     * @param currentPrice 当前价格
     * @return
     */
    fun calBaseAmount(
        symbol: String,
        lowestPrice: String,
        highestPrice: String,
        gridNumber: String,
        gridLineType: String,
        totalQuoteAmount: String,
        currentPrice: String,
        fee: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["lowestPrice"] = lowestPrice
            this["highestPrice"] = highestPrice
            this["gridNumber"] = gridNumber
            this["gridLineType"] = gridLineType
            this["fee"] = fee
            this["totalQuoteAmount"] = totalQuoteAmount
            this["currentPrice"] = currentPrice

        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .calBaseAmount(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 策略交易列表（详情）
     * @return
     */
    fun getStrategyList(
        isHideOtherSymbol: Boolean = false,
        symbols: String = "",
        status: String = "1",
        page: String = "",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (symbols.isNotEmpty()) {
                if (isHideOtherSymbol) {
                    this["symbol"] = symbols
                }
                this["status"] = status
                this["page"] = page
                this["pageSize"] = pageSize

            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getStrategyList(getBaseReqBody(map)), consumer
        )
    }


    fun getStrategyList(
        symbols: String = "",
        status: String = "1",
        page: String = "",
        pageSize: String = "20",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (symbols.isNotEmpty()) {
                this["symbol"] = symbols
                this["status"] = status
                this["page"] = page
                this["pageSize"] = pageSize

            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getStrategyList(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 查询正在进行挂单记录
     * @return
     */
    fun getOrderingGridList(
        strategyId: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (strategyId.isNotEmpty()) {
                this["strategyId"] = strategyId
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getOrderingGridList(getBaseReqBody(map)), consumer
        )
    }


    /**
     *网格已完成挂单记录
     * @return
     */
    fun getFinishGridList(
        strategyId: String = "",
        page: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (strategyId.isNotEmpty()) {
                this["strategyId"] = strategyId
                this["page"] = page
                this["pageSize"] = "20"
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getFinishGridList(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 停止策略
     * @return
     */
    fun stopStrategy(
        strategyId: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            if (strategyId.isNotEmpty()) {
                this["strategyId"] = strategyId
            }
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .stopStrategy(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取资产详情
     * @return
     */
    fun getAccountBalanceByMarginCoin(
        symbols: String = "",
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["coinSymbols"] = symbols
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getAccountBalanceByMarginCoin(getBaseReqBody(map)), consumer
        )
    }

    /**
     * 获取资产详情
     * @return
     */
    fun getETFCoin(consumer: NDisposableObserver): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getETFCoin(getBaseReqBody(null)), consumer
        )
    }

    /**
     * 已读
     * @return
     */
    fun saveETFStatus(consumer: NDisposableObserver): Disposable? {
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .saveETFStatus(getBaseReqBody(null)), consumer
        )
    }

    /**
     * 获取币种简介(app4.0)
     */
    fun getETFPositionRecordList(
        symbol: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val hashMap = getBaseMaps().apply {
            this["symbol"] = symbol
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .getETFPositionRecordList(getBaseReqBody(hashMap)), consumer
        )
    }

    /**
     * 保存配置
     */
    fun savePublicInfo() {
        val consumer: DisposableObserver<ResponseBody> = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                val json = jsonObject.optJSONObject("data")
                PublicInfoDataService.getInstance().saveData(json)
            }
        }
        changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .public_info_v4(getBaseReqBody()), consumer
        )
    }


    /**
     * 内部转账用户认证
     */
    fun innerTransferUserAuth(
        transferUid: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["transferUid"] = transferUid
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .innerTransferUserAuth(getBaseReqBody(map)), consumer
        )
    }


    /**
     * 内部转账
     */
    fun innerTransferDoWithdraw(
        transferUid: String,
        amount: String,
        fee: String,
        symbol: String,
        smsAuthCode: String,
        googleCode: String,
        consumer: DisposableObserver<ResponseBody>
    ): Disposable? {
        val map = getBaseMaps().apply {
            this["transferUid"] = transferUid
            this["amount"] = amount
            this["fee"] = fee
            this["symbol"] = symbol
            this["smsAuthCode"] = smsAuthCode
            this["googleCode"] = googleCode
        }
        return changeIOToMainThread(
            httpHelper.getBaseUrlService(MainApiService::class.java)
                .innerTransferDoWithdraw(getBaseReqBody(map)), consumer
        )
    }


}