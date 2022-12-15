package com.yjkj.chainup.model.api

import com.yjkj.chainup.bean.*
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.bean.kline.DepthItem
import com.yjkj.chainup.net.api.HttpResult
import com.yjkj.chainup.treaty.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * @Author: Bertking
 * @Date：2019-09-03-10:45
 * @Description: 合约接口
 */
interface ContractApiService {

    /**
     * 跟单
     */
    @POST("position/find_current_single_list")
    fun findCurrentSingleList(): Observable<HttpResult<TraderPositionInfo>>
    /**
     * 历史跟单
     */
    @POST("position/find_his_single_list")
    fun findHisSingleList(): Observable<ResponseBody>
    /**
     * 带单列表
     */
    @POST("position/trader_position_list")
    @FormUrlEncoded
    fun traderPositionList(@FieldMap map: HashMap<String, Any>): Observable<HttpResult<TraderPositionInfo>>

    @POST("position/trader_position_list")
    @FormUrlEncoded
    fun traderPositionList1(@FieldMap map: HashMap<String, Any>): Observable<ResponseBody>

    /**
     * 带单收益明细
     */
    @POST("assets/traderTransaction")
    @FormUrlEncoded
    fun traderTransaction(@FieldMap map: HashMap<String, Any>): Observable<HttpResult<TraderTransactionInfo>>

    /**
     * 带单收益统计
     */
    @POST("assets/traderTransactionDaySum")
    @FormUrlEncoded
    fun traderTransactionDay(@FieldMap map: HashMap<String, Any>): Observable<HttpResult<List<TraderTransactionBean>>>

    /**
     * 跟单收益统计
     */
    @POST("follower/statistics")
    fun statistics(): Observable<HttpResult<GetAssetsTotalBean>>
    /**
     * 校验是否开通了合约账户
     */
    @POST("trader/check_futures_user")
    @FormUrlEncoded
    fun checkFuturesUser(@FieldMap map: HashMap<String, Any>): Observable<HttpResult<GetAssetsTotalBean>>
    /**
     * 利润分成
     */
    @POST("trader/traderBonusRate")
    fun traderBonusRate(): Observable<HttpResult<String>>
    /**
     * 仓位交易记录
     */
    @POST("position/position_op_log")
    @FormUrlEncoded
    fun positionLog(@FieldMap map: HashMap<String, Any>): Observable<HttpResult<List<FollowerStatisticsBean>>>
    /**
     * 易员历史总收益
     */
    @POST("trader/trader_total_profit")
    fun traderTotalProfit(): Observable<HttpResult<String>>

    /**
     * 8. 修改杠杆倍数
     */
    @POST("change_level")
    fun changeLevel4Contract(@Body requestBody: RequestBody): Observable<HttpResult<Any>>

    /**
     *17合约流水
     */
    @POST("business_transaction_list_v2")
    fun getBusinessTransferList(@Body requestBody: RequestBody): Observable<HttpResult<ContractCashFlowBean>>


    /**********************************合约改版接口**************************************************/



    /**
     * 2. 获取创建订单初始化信息
     */
    @POST("init_take_order")
    fun getInitTakeOrderInfo4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     * 3. 创建订单(any)
     */
    @POST("take_order")
    fun takeOrder4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     * 4. 取消订单(any)
     */
    @POST("cancel_order")
    fun cancelOrder4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     * 5. 订单列表(合约当前委托)
     */
    @POST("order_list_new")
    fun getOrderList4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     * 7. 标记价格
     */
    @POST("tag_price")
    fun getTagPrice4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     * 9. 追加保证金(any)
     */
    @POST("transfer_margin")
    fun transferMargin4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     * 10. 用户持仓信息(any)
     */
    @POST("user_position")
    fun getPosition4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     * 11. 用户未平仓合约 ：
     */
    @POST("hold_contract_list")
    fun holdContractList4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     * 13. 账户余额信息 ：
     */
    @POST("account_balance")
    fun getAccountBalance4Contract1(@Body requestBody: RequestBody):Observable<ResponseBody>


    /**
     * 15.风险评估
     */
    @POST("get_liquidation_rate")
    fun getRiskLiquidationRate1(@Body requestBody: RequestBody):Observable<ResponseBody>


    /**
     * 16 获取历史委托(合约)
     */
    @POST("order_list_history")
    fun getHistoryEntrust4Contract1(@Body requestBody: RequestBody): Observable<ResponseBody>


    //-------------------------------------------------------------------------------------//
    //                                      新版本合约接口start
    //-------------------------------------------------------------------------------------//
    /**
     *  获取前台用户配置(保证金模式/杠杆/交易喜好)
     */
    @POST("user/get_user_config")
    fun getUserConfig(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  开通合约交易
     */
    @POST("user/create_co_id")
    fun createContract(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取合约公共信息
     */
    @POST("common/public_info")
    fun getPublicInfo(@Body requestBody: RequestBody): Observable<ResponseBody>




    /**
     *  获取合约公共实时信息    返回 标记价格 、资金费率 、指数价格
     */
    @POST("common/public_market_info")
    fun getMarkertInfo(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  提交委托（下单）
     */
    @POST("order/order_create")
    fun createOrder(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  历史计划委托订单
     */
    @POST("order/history_trigger_order_list")
    fun getHistoryPlanOrderList(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  持仓列表
     */
    @POST("position/get_position_list")
    fun getPositionList(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  调节逐仓仓位保证金
     */
    @POST("position/change_position_margin")
    fun modifyPositionMargin(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  获取持仓列表以及资产列表
     */
    @POST("position/get_assets_list")
    fun getPositionAssetsList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取资金流水
     */
    @POST("record/get_transaction_list")
    fun getTransactionList(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  获取止盈止损订单列表
     */
    @POST("order/take_profit_stop_loss")
    fun getTakeProfitStopLoss(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取委托订单的成交记录
     */
    @POST("order/get_trade_info")
    fun getHisTradeList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  合约划转到币币
     */
    @POST("assets/saas_trans/co_to_ex")
    fun coTransferEx(@Body requestBody: RequestBody): Observable<HttpResult<Any>>

    /**
     *  资金流水
     */
    @POST("record/get_transfer_record")
    fun getTransferRecord(@Body requestBody: RequestBody): Observable<HttpResult<CashFlowBean>>

    /**
     * 获取深度图
     */
    @POST("common/depth_map")
    fun getCoinDepth(@Body requestBody: RequestBody): Observable<HttpResult<DepthItem>>

}