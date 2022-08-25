package com.chainup.contract.api


import com.chainup.contract.bean.CpCashFlowBean
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

/**
 * @Author: Bertking
 * @Date：2019-09-03-10:45
 * @Description: 合约接口
 */
interface CpContractApiService {

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
     *  修改保证金模式
     */
    @POST("user/margin_model_edit")
    fun modifyMarginModel(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  修改交易喜好设置
     */
    @POST("user/edit_user_page_config")
    fun modifyTransactionLike(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  修改杠杆
     */
    @POST("user/level_edit")
    fun modifyLevel(@Body requestBody: RequestBody): Observable<ResponseBody>

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
     *  提交委托（止盈/止损）
     */
    @POST("order/condition_create")
    fun createTpslOrder(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  撤单
     */
    @POST("order/order_cancel")
    fun orderCancel(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  历史委托订单
     */
    @POST("order/history_order_list")
    fun getHistoryOrderList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  历史计划委托订单
     */
    @POST("order/history_trigger_order_list")
    fun getHistoryPlanOrderList(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  当前委托订单
     */
    @POST("order/current_order_list")
    fun getCurrentOrderList(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  当前计划订单
     */
    @POST("order/trigger_order_list")
    fun getCurrentPlanOrderList(@Body requestBody: RequestBody): Observable<ResponseBody>


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
     *  获取杠杆阶梯配置
     */
    @POST("common/get_ladder_info")
    fun getLadderInfo(@Body requestBody: RequestBody): Observable<ResponseBody>


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
     *  取消订单--止盈止损
     */
    @POST("order/order_tpsl_cancel")
    fun cancelOrderTpsl(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取委托订单的成交记录
     */
    @POST("order/get_trade_info")
    fun getHisTradeList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  合约划转到币币
     */
    @POST("assets/saas_trans/co_to_ex")
    fun coTransferEx(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  持仓/盈亏记录
     */
    @POST("position/history_position_list")
    fun getHistoryPositionList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  资金流水
     */
    @POST("record/get_transfer_record")
    fun getTransferRecord(@Body requestBody: RequestBody): Observable<CpHttpResult<CpCashFlowBean>>

    /**
     * 获取深度图
     */
    @POST("common/depth_map")
    fun getCoinDepth(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  领取模拟合约体验金
     */
    @POST("user/receive_coupon")
    fun receiveCoupon(@Body requestBody: RequestBody): Observable<ResponseBody>


    /**
     *  闪电平仓
     */
    @POST("order/light_close")
    fun lightClose(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取合约保险基金余额折线图/历史记录
     */
    @POST("common/risk_balance_list")
    fun riskBalanceList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取合约资金费率折线图/历史记录
     */
    @POST("common/funding_rate_list")
    fun fundingRateList(@Body requestBody: RequestBody): Observable<ResponseBody>

    /**
     *  获取保险基金余额
     */
    @POST("common/get_risk_account")
    fun getRiskAccount(@Body requestBody: RequestBody): Observable<ResponseBody>

}