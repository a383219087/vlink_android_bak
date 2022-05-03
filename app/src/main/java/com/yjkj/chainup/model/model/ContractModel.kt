package com.yjkj.chainup.model.model

import android.text.TextUtils
import com.google.gson.Gson
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.model.NDataHandler
import com.yjkj.chainup.model.api.ContractApiService
import com.yjkj.chainup.model.datamanager.BaseDataManager
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.net.api.HttpResult
import com.yjkj.chainup.net_new.HttpParams
import com.yjkj.chainup.new_contract.bean.ClTpslOrderBean
import com.yjkj.chainup.treaty.bean.ActiveOrderListBean
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.RequestBody
import okhttp3.ResponseBody

/**
 * @Author: Bertking
 * @Date：2019-09-04-11:27
 * @Description: 合约具体请求
 */
class ContractModel : BaseDataManager() {


    /**
     * 1. 合约的公共接口
     */
    fun getPublicInfo4Contract(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPublicInfo4Contract1(getBaseReqBody()), consumer)
    }

    /**
     *
     * 2. 获取创建订单初始化信息（need login）
     *
     * @param contractId   合约id
     * @param volume   用户输入数量（不输入默认按照1算）
     * @param price    用户输入价格（不输入默认按照最新成交价，如果最新成交价为空，取币对开盘价格）
     * @param level   杠杆倍数(只有在选择杠杆的时候必填)
     * @param orderType 1:限价 2：市价
     *
     */
    fun getInitTakeOrderInfo4Contract(contractId: String,
                                      volume: String = "1",
                                      price: String,
                                      level: String = "",
                                      orderType: Int,
                                      consumer: DisposableObserver<ResponseBody>): Disposable? {

        val map = getBaseMaps()
        map["contractId"] = contractId
        map["volume"] = volume
        map["price"] = price
        if (!TextUtils.isEmpty(level)) {
            map["level"] = level
        }
        map["orderType"] = orderType.toString()
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getInitTakeOrderInfo4Contract1(getBaseReqBody(map)), consumer)
    }


    /**
     * 3. 创建订单
     *
     * @param contractId  合约id
     * @param volume  下单数量
     * @param price   下单价格
     * @param orderType (1：限价单   2：市价单)
     * @param copType (1：全仓    2：逐仓) 平仓时此参数不传
     * @param side  (BUY:买     SELL:卖)
     * @param closeType  (0:开仓单，1：平仓单)
     * @param level  杠杆倍数
     *
     */
    fun takeOrder4Contract(contractId: String,
                           volume: String,
                           price: String,
                           orderType: Int,
                           copType: String = "2",
                           side: String,
                           closeType: String = "0",
                           level: String,
                           positionId: String = "",
                           consumer: DisposableObserver<ResponseBody>): Disposable? {

        val map = getBaseMaps()
        map["contractId"] = contractId
        map["volume"] = volume
        map["price"] = price
        map["orderType"] = orderType.toString()
        map["copType"] = copType
        map["side"] = side
        map["closeType"] = closeType
        map["level"] = level
        if (!TextUtils.isEmpty(positionId)) {
            map["positionId"] = positionId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).takeOrder4Contract1(getBaseReqBody(map)), consumer)
    }


    /**
     * 4. 取消订单
     * @param orderId 订单id
     * @param contractId 合约id
     */
    fun cancelOrder4Contract(orderId: String,
                             contractId: String,
                             consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["orderId"] = orderId
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).cancelOrder4Contract1(getBaseReqBody(map)), consumer)

    }


    /**
     * 5. 获取合约订单列表
     * @param consumer
     * @return
     */
    fun getOrderList4Contract(contractId: String, page: String = "1", pageSize: String = "100", side: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        var paramMaps = getBaseMaps()
        paramMaps["contractId"] = contractId
        paramMaps["page"] = page
        paramMaps["pageSize"] = pageSize
        paramMaps["side"] = side
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getOrderList4Contract1(getBaseReqBody(paramMaps)), consumer)
    }


    /**
     * 7. 标记价格(needless 登录)
     * @param contractId 合约id
     */
    fun getTagPrice4Contract(contractId: String,
                             consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getTagPrice4Contract1(getBaseReqBody(map)), consumer)
    }

    /**
     * 8. 修改杠杆倍数
     */
    fun changeLevel4Contract(contractId: String, newLevel: String,
                             consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["leverageLevel"] = newLevel
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).changeLevel4Contract1(getBaseReqBody(map)), consumer)
    }


    /**
     * 9. 追加保证金 ：
     * @param contractId  合约id
     * @param amount 追加数量(转出为负数)
     */
    fun transferMargin4Contract(positionId: String,
                                contractId: String,
                                amount: String,
                                consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["amount"] = amount
            this["positionId"] = positionId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).transferMargin4Contract1(getBaseReqBody(map)), consumer)
    }

    /**
     * 10. 用户持仓信息 ：
     *
     * 合约id不填查询的为仓位列表
     *  5s刷新
     */
    fun getPosition4Contract(contractId: String = "",
                             consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPosition4Contract1(getBaseReqBody(map)), consumer)
    }


    /**
     * 11. 用户未平仓合约 ：
     * 页面20s请求
     */
    fun holdContractList4Contract(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).holdContractList4Contract1(getBaseReqBody()), consumer)
    }


    /**
     * 12. 资金划转 ：
     *
     * @param fromType 转出账户type
     * @param toType   转入账户type
     * @param amount   划转金额
     * @param bond     保证金币种
     *
     */
    fun capitalTransfer4Contract(fromType: String,
                                 toType: String,
                                 amount: String,
                                 bond: String?,
                                 consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps()
        map["fromType"] = fromType
        map["toType"] = toType
        map["amount"] = amount
        if (null != bond) {
            map["bond"] = bond!!

        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).capitalTransfer4Contract1(getBaseReqBody(map)), consumer)
    }


    /**
     * 13. 账户余额 ：
     */
    fun getAccountBalance4Contract(
            consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps()
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getAccountBalance4Contract1(getBaseReqBody(map)), consumer)
    }

    /**
     * 15.风险评估(need登录)
     */
    fun getRiskLiquidationRate(contractId: String = "",
                               consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps()
        map["contractId"] = contractId
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getRiskLiquidationRate1(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取历史委托(合约)
     * @param symbol 合约系列
     * @param contractType 合约类型（0 永续合约、1 周合约、2 次周合约、3 月合约、4 季度合约）
     * @param pageSize default 5
     * @param page  default 1
     * @param side 委托方向，BUY买入 SELL卖出 ,不传全部
     * @param isShowCanceled 是否展示已取消的订单，0表示不展示，1表示展示，默认1
     * @param startTime 年月日，禁止输入时分秒：2019-04-22
     * @param endTime 年月日，禁止输入时分秒：2019-04-22
     * @param action 开平仓动作
     */
    fun getHistoryEntrust4Contract(symbol: String,
                                   contractType: String,
                                   pageSize: String = "5",
                                   page: String = "1",
                                   isShowCanceled: String = "1",
                                   side: String = "",
                                   orderType: String = "",
                                   startTime: String = "",
                                   endTime: String = "",
                                   action: String = "",
                                   consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps()
        map["symbol"] = symbol
        map["pageSize"] = pageSize
        map["page"] = page
        map["isShowCanceled"] = isShowCanceled

        if (!TextUtils.isEmpty(side)) {
            map["side"] = side
        }

        if (!TextUtils.isEmpty(orderType)) {
            map["orderType"] = orderType
        }

        map["contractType"] = contractType

        if (!TextUtils.isEmpty(startTime)) {
            map["startTime"] = startTime
        }

        if (!TextUtils.isEmpty(endTime)) {
            map["endTime"] = endTime
        }
        if (!TextUtils.isEmpty(action)) {
            map["action"] = action
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getHistoryEntrust4Contract1(getBaseReqBody(map)), consumer)
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
    fun getBusinessTransferList(item: String = "", childItem: String = "", startTime: String = "", endTime: String = "", page: Int = 1, pageSize: Int = 1000,
                                consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps()
        map["item"] = item
        map["childItem"] = childItem
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["page"] = page.toString()
        map["pageSize"] = pageSize.toString()
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getBusinessTransferList1(getBaseReqBody(map)), consumer)
    }

//    fun getPublicInfo(consumer: DisposableObserver<ResponseBody>): Disposable? {
//        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPublicInfo(getBaseReqBody()), consumer)
//    }


    fun getUserConfig(contractId: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getUserConfig(getBaseReqBody(map)), consumer)
    }


    fun modifyMarginModel(contractId: String = "", marginModel: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["marginModel"] = marginModel
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).modifyMarginModel(getBaseReqBody(map)), consumer)
    }


    fun modifyLevel(contractId: String = "", nowLevel: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["nowLevel"] = nowLevel
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).modifyLevel(getBaseReqBody(map)), consumer)
    }


    fun modifyTransactionLike(contractId: String, positionModel: String, pcSecondConfirm: String, coUnit: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["positionModel"] = positionModel
            this["pcSecondConfirm"] = pcSecondConfirm
            this["coUnit"] = coUnit
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).modifyTransactionLike(getBaseReqBody(map)), consumer)
    }

    /**
     * 开通合约交易
     * @param mobileNumber 交易所登陆用户手机号(脱敏)
     * @param email 交易所邮箱(脱敏)
     * @param uid 交易所用户ID
     */
    fun createContract(consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            if (!TextUtils.isEmpty(UserDataService.getInstance().mobileNumber)) {
                this["mobileNumber"] = UserDataService.getInstance().mobileNumber
            }
            if (!TextUtils.isEmpty(UserDataService.getInstance().email)) {
                this["email"] = UserDataService.getInstance().email
            }
            this["uid"] = UserDataService.getInstance().userInfo4UserId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).createContract(getBaseReqBody(map)), consumer)
    }

    /**
     * 前台公共实时信息
     * @param symbol 合约币对名称, 例如: BTC-USDT
     * @param contractId 合约ID
     */
    fun getMarkertInfo(symbol: String, contractId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getMarkertInfo(getBaseReqBody(map)), consumer)
    }

    /**
     * 提交委托
     * @param symbol 合约币对名称, 例如: BTC-USDT
     * @param contractId 合约ID
     * @param positionType  持仓类型(1 全仓，2 仓逐)
     * @param open  开平仓方向(OPEN 开仓，CLOSE 平仓)
     * @param side  买卖方向（BUY 买入，SELL 卖出）
     * @param type  订单类型(1 limit， 2 market，3 IOC，4 FOK，5 POST_ONLY)
     * @param leverageLevel  杠杆倍数
     * @param price  下单价格(市价单传0)
     * @param volume 下单数量(开仓市价单：金额)
     * @param isConditionOrder 是否是条件单
     * @param triggerPrice 触发价格
     */
    fun createOrder(contractId: Int, positionType: String, open: String, side: String, type: Int, leverageLevel: Int, price: String, volume: String, isConditionOrder: Boolean, triggerPrice: String, expireTime: Int, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMapsV2().apply {
            this["contractId"] = contractId
            this["positionType"] = positionType
            this["open"] = open
            this["side"] = side
            this["type"] = type
            this["leverageLevel"] = leverageLevel
            this["price"] = price
            this["volume"] = volume
            this["isConditionOrder"] = isConditionOrder
            this["triggerPrice"] = triggerPrice
            this["expireTime"] = expireTime
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).createOrder(getBaseReqBodyV1(map)), consumer)
    }

    /**
     * 提交委托(止盈/止损)
     * @param contractId 合约ID
     * @param positionType  持仓类型(1 全仓，2 仓逐)
     * @param side  买卖方向（BUY 买入，SELL 卖出）
     * @param leverageLevel  杠杆倍数
     * @param orderList  下单列表
     *          |triggerType 止盈止损订单类型(3止损， 4 止盈)固定枚举
     *          |price 下单价格(市价单传0)
     *          |volume 下单数量(开仓市价单：金额)
     *          |triggerPrice 触发价格
     *          |type  订单类型(1 limit， 2 market)
     */
    fun createTpslOrder(contractId: Int, positionType: String, side: String, leverageLevel: Int, mTpslOrderList: List<ClTpslOrderBean>, consumer: DisposableObserver<ResponseBody>): Disposable? {
        var sideBuff = ""
        if (side.equals("BUY")) {
            sideBuff = "SELL"
        } else {
            sideBuff = "BUY"
        }
        val map = getBaseMapsV2().apply {
            this["contractId"] = contractId
            this["positionType"] = positionType
            this["side"] = sideBuff
            this["leverageLevel"] = leverageLevel
            this["orderListStr"] = Gson().toJson(mTpslOrderList)
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).createTpslOrder(getBaseReqBodyV1(map)), consumer)
    }

    /**
     * 撤单
     * @param contractId 合约ID
     * @param orderId 订单ID
     */
    fun orderCancel(contractId: String, orderId: String, isConditionOrder: Boolean, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["isConditionOrder"] = isConditionOrder.toString()
            if (!TextUtils.isEmpty(orderId)) {
                this["orderId"] = orderId
            }
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).orderCancel(getBaseReqBody(map)), consumer)
    }

    /**
     * 调节逐仓仓位保证金
     * @param type 调整类型; 1 增加保证金, 2 减少保证金
     * @param contractId 合约ID
     * @param amount 调整金额
     */
    fun modifyPositionMargin(contractId: String, positionId: String, type: String, amount: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["type"] = type
            this["amount"] = amount
            this["positionId"] = positionId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).modifyPositionMargin(getBaseReqBody(map)), consumer)
    }

    /**
     * 持仓列表
     * @param contractId 合约ID
     */
    fun getPositionList(contractId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPositionList(getBaseReqBody(map)), consumer)
    }

    /**
     * 当前委托
     * @param contractId 合约ID
     * @param status 订单状态：0 init，1 new，2 filled，3 part_filled，4 canceled，5 pending_cancel，6 expired (不传默认查询 0, 1, 3, 5 状态)
     */
    fun getCurrentOrderList(contractId: String, status: Int, page: Int, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            if (status != 0) this["type"] = status.toString()
            this["page"] = page.toString()
            this["limit"] = "20"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getCurrentOrderList(getBaseReqBody(map)), consumer)
    }

    /**
     * 当前计划委托
     * @param contractId 合约ID
     * @param status 订单状态：0 init，1 new，2 filled，3 part_filled，4 canceled，5 pending_cancel，6 expired (不传默认查询 0, 1, 3, 5 状态)
     */
    fun getCurrentPlanOrderList(contractId: String, status: Int, page: Int, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            if (status != 0) this["type"] = status.toString()
            this["page"] = page.toString()
            this["limit"] = "20"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getCurrentPlanOrderList(getBaseReqBody(map)), consumer)
    }


    /**
     * 历史委托
     * @param contractId 合约ID
     * @param status 订单状态：0 init，1 new，2 filled，3 part_filled，4 canceled，5 pending_cancel，6 expired (不传默认查询 2 和 4 类型)
     */
    fun getHistoryOrderList(contractId: String, status: Int, page: Int, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            if (status != 0) this["type"] = status.toString()
            this["page"] = page.toString()
            this["limit"] = "20"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getHistoryOrderList(getBaseReqBody(map)), consumer)
    }

    /**
     * 历史计划委托
     * @param contractId 合约ID
     * @param status 订单状态：0 init，1 new，2 filled，3 part_filled，4 canceled，5 pending_cancel，6 expired (不传默认查询 2 和 4 类型)
     */
    fun getHistoryPlanOrderList(contractId: String, status: Int, page: Int, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            if (status != 0) this["type"] = status.toString()
            this["page"] = page.toString()
            this["limit"] = "20"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getHistoryPlanOrderList(getBaseReqBody(map)), consumer)
    }


    /**
     * 获取持仓列表以及资产列表
     * @param marginCoin 保证金币种, 不传查询全部币种
     * @param onlyAccount 1 只返回资产信息, 0 返回仓位和资产
     */
    fun getPositionAssetsList(consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["onlyAccount"] = "0"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPositionAssetsList(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取持仓列表以及资产列表
     * @param contractId contractId
     */
    fun getLadderInfo(contractId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getLadderInfo(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取合约资金流水
     * @param symbol   查询币种
     * @param type 流水类型 1 转入,2 转出,5 资金费用 ,8 分摊
     * @param page 页码
     */
    fun getTransactionList(symbol: String, type: String, page: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["symbol"] = symbol
            if (!type.equals("0")){
                this["type"] = type
            }
            this["page"] = page
            this["limit"] = "20"
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getTransactionList(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取资产详情
     * @param marginCoin   保证金币种
     */
    fun getAccountBalanceByMarginCoin(marginCoin: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["marginCoin"] = marginCoin
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPositionAssetsList(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取止盈止损列表
     * @param contractId   合约ID
     * @param orderSide   持仓方向 BUY 多仓, SELL 空仓(固定枚举)
     */
    fun getTakeProfitStopLoss(contractId: String, orderSide: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["orderSide"] = orderSide
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getTakeProfitStopLoss(getBaseReqBody(map)), consumer)
    }

    /**
     * 取消订单--止盈止损
     * @param contractId   合约ID
     * @param orderIds   订单ID, 多个英文半角逗号分割
     */
    fun cancelOrderTpsl(contractId: String, orderIds: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["orderIds"] = orderIds
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).cancelOrderTpsl(getBaseReqBody(map)), consumer)
    }

    /**
     * 币币划转到合约
     * @param uid   用户id
     * @param coinSymbol   划转币种, 如USDT
     * @param amount   划转金额
     */
//    fun coTransferEx(uid: String, coinSymbol: String, amount: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
//        val map = getBaseMaps().apply {
//            this["coinSymbol"] = coinSymbol
//            this["amount"] = amount
//        }
//        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).coTransferEx(getBaseReqBody(map)), consumer)
//    }

    /**
     * 获取合约币对资金流水
     * @param coinSymbol   币对
     * @param startTime   开始时间
     * @param endTime   结束时间
     * @param page   页码
     * @param pageSize   条数
     * @param transactionScene   划转类型
     */
//    fun getTransferRecord(coinSymbol: String, startTime: String, endTime: String, page: String, pageSize: String, transactionScene: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
//        val map = getBaseMaps().apply {
//            this["coinSymbol"] = coinSymbol
//            this["startTime"] = startTime
//            this["endTime"] = endTime
//            this["page"] = page
//            this["pageSize"] = pageSize
//            this["transactionScene"] = transactionScene
//        }
//        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getTransferRecord(getBaseReqBody(map)), consumer)
//    }

    /**
     * 获取委托订单的历史成交记录
     * @param orderId   订单ID
     * @param contractId   合约ID
     */
    fun getHistoryTradeList(contractId: String, orderId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["orderId"] = orderId
        }
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getHisTradeList(getBaseReqBody(map)), consumer)
    }


}