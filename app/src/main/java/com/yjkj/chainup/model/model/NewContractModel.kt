package com.yjkj.chainup.model.model

import android.text.TextUtils
import com.google.gson.Gson
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.model.api.ContractApiService
import com.yjkj.chainup.model.datamanager.BaseDataManager
import com.yjkj.chainup.new_contract.bean.ClTpslOrderBean
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.ResponseBody

/**
 * @Author: Bertking
 * @Date：2019-09-04-11:27
 * @Description: 合约具体请求
 */
class NewContractModel : BaseDataManager() {

    fun getPublicInfo(consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(httpHelper.getContractUrlService(ContractApiService::class.java).getPublicInfo(getBaseReqBody()), consumer)
    }

    /**
     * 获取合约用户配置信息（保证金模式/杠杆/交易喜好）
     * @param contractId 合约ID
     */
    fun getUserConfig(contractId: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getUserConfig(getBaseReqBody(map)), consumer)
    }

    /**
     * 修改保证金模式
     * @param contractId 合约ID
     * @param marginModel 当前保证金模式 1全仓, 2逐仓
     */
    fun modifyMarginModel(contractId: String = "", marginModel: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["marginModel"] = marginModel
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).modifyMarginModel(getBaseReqBody(map)), consumer)
    }

    /**
     * 修改杠杆
     * @param contractId 合约ID
     * @param nowLevel 当前杠杆倍数
     */
    fun modifyLevel(contractId: String = "", nowLevel: String = "", consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["nowLevel"] = nowLevel
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).modifyLevel(getBaseReqBody(map)), consumer)
    }

    /**
     * 修改交易喜好
     * @param contractId 合约ID
     * @param positionModel 持仓类型 1持仓, 2双向持仓
     * @param pcSecondConfirm PC页面下单前弹窗确认开关, 0使用，1停用
     * @param coUnit 合约单位 1标的货币, 2张
     */
    fun modifyTransactionLike(contractId: String, positionModel: String, pcSecondConfirm: String, coUnit: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["positionModel"] = positionModel
            this["pcSecondConfirm"] = pcSecondConfirm
            this["coUnit"] = coUnit
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).modifyTransactionLike(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).createContract(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getMarkertInfo(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).createOrder(getBaseReqBodyV1(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).createTpslOrder(getBaseReqBodyV1(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).orderCancel(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).modifyPositionMargin(getBaseReqBody(map)), consumer)
    }

    /**
     * 持仓列表
     * @param contractId 合约ID
     */
    fun getPositionList(contractId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getPositionList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getCurrentOrderList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getCurrentPlanOrderList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getHistoryOrderList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getHistoryPlanOrderList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getPositionAssetsList(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取持仓列表以及资产列表
     * @param contractId contractId
     */
    fun getLadderInfo(contractId: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getLadderInfo(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getTransactionList(getBaseReqBody(map)), consumer)
    }

    /**
     * 获取资产详情
     * @param marginCoin   保证金币种
     */
    fun getAccountBalanceByMarginCoin(marginCoin: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["marginCoin"] = marginCoin
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getPositionAssetsList(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getTakeProfitStopLoss(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).cancelOrderTpsl(getBaseReqBody(map)), consumer)
    }

    /**
     * 币币划转到合约
     * @param uid   用户id
     * @param coinSymbol   划转币种, 如USDT
     * @param amount   划转金额
     */
//    fun coTransferEx(uid: String, coinSymbol: String, amount: String, transferType: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
//        val map = getBaseMaps().apply {
//            this["coinSymbol"] = coinSymbol
//            this["amount"] = amount
//            this["transferType"] = transferType
//        }
//        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).coTransferEx(getBaseReqBody(map)), consumer)
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
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getHisTradeList(getBaseReqBody(map)), consumer)
    }

    /**
     * 领取模拟合约体验金
     */
    fun receiveCoupon( consumer: DisposableObserver<ResponseBody>): Disposable? {
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).receiveCoupon(getBaseReqBody()), consumer)
    }


    /**
     * 获取持仓/盈亏记录
     * @param page  页码
     * @param contractId   合约ID
     */
    fun getHistoryPositionList(contractId: String, page: String, side: String, consumer: DisposableObserver<ResponseBody>): Disposable? {
        val map = getBaseMaps().apply {
            this["contractId"] = contractId
            this["page"] = page
            this["limit"] = "20"
            if (!TextUtils.isEmpty(side)){
                this["side"] =side
            }
        }
        return changeIOToMainThread(httpHelper.getContractNewUrlService(ContractApiService::class.java).getHistoryPositionList(getBaseReqBody(map)), consumer)
    }



}