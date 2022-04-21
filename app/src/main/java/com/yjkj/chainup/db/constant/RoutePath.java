package com.yjkj.chainup.db.constant;

/**
 * @Description: Aroute路由路径配置，格式：/模块名称/类名
 * @Author: wanghao
 * @CreateDate: 2019-10-22 20:39
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-10-22 20:39
 * @UpdateRemark: 更新说明
 */
public class RoutePath {

    /*
     * 首页  NewMainActivity
     */
    public static final String NewMainActivity = "/main/NewMainActivity";


    /*
     *  OTC模块
     */
    public static final String PaymentMethodActivity = "/otc/PaymentMethodActivity";
    public static final String NewAdvertisingManagementActivity = "/otc/newadvertisingmanagementactivity";
    public static final String NewAdvertisingDetailActivity = "/main/otc/NewAdvertisingDetailActivity";
    public static final String NewReleaseAdvertisingActivity = "/main/otc/NewReleaseAdvertisingActivity";


    /*
     * 个人中心模块
     */
    public static final String PersonalCenterActivity = "/personCenter/PersonalCenterActivity";
    public static final String PersonalInfoActivity = "/personCenter/PersonalInfoActivity";
    public static final String RealNameCertificationActivity = "/personCenter/RealNameCertificationActivity";
    public static final String RealNameCertificaionSuccessActivity = "/personCenter/RealNameCertificaionSuccessActivity";
    public static final String SetOrModifyPwdActivity = "/personCenter/SetOrModifyPwdActivity";
    public static final String SafetySettingActivity = "/personCenter/SafetySettingActivity";
    public static final String NewVerifyActivity = "/personalCenter/NewVerifyActivity";
    public static final String WebviewActivity = "/personalCenter/WebviewActivity";
    public static final String UdeskWebViewActivity = "/personalCenter/UdeskWebViewActivity";
    public static final String ContractAgentActivity = "/personalCenter/ContractAgentActivity1";
    public static final String MyInviteCodesActivity = "/personalCenter/MyInviteCodesActivity";
    public static final String MyInviteActivity = "/personalCenter/MyInviteActivity";
    public static final String FriendsFragment = "/personalCenter/FriendsFragment";
    public static final String CommissionFragment = "/personalCenter/CommissionFragment";

    public static final String DocumentaryActivity = "/documentary/DocumentaryActivity";
    public static final String DocumentaryDetailActivity = "/documentary/DocumentaryDetailActivity";
    public static final String TradersActivity = "/documentary/TradersActivity";
    public static final String ApplyTradersActivity = "/documentary/ApplyTradersActivity";
    public static final String FirstFragment = "/documentary/FirstFragment";
    public static final String MineFragment = "/documentary/MineFragment";
    public static final String NowDocumentaryFragment = "/documentary/NowDocumentaryFragment";
    public static final String MyTradersFragment = "/documentary/MyTradersFragment";
    public static final String HistoryDocumentaryFragment = "/documentary/HistoryDocumentaryFragment";

    public static final String BinaryActivity = "/binary/BinaryActivity";
    public static final String OptionsActivity = "/binary/OptionsActivity";
    public static final String CurrentFragment = "/binary/CurrentFragment";
    public static final String MydealFragment = "/binary/MydealFragment";
    public static final String RankingFragment = "/binary/RankingFragment";
    public static final String ResultsFragment = "/binary/ResultsFragment";


    public static final String FinancialActivity = "/financial/FinancialActivity";
    public static final String ProductFragment = "/financial/ProductFragment";
    public static final String AutomaticDepositFragment = "/financial/AutomaticDepositFragment";
    public static final String HoldFragment = "/financial/HoldFragment";

    /*
     * 登录注册模块
     */
    public static final String NewVersionLoginActivity = "/login/NewVersionLoginActivity";


    /*
     * 活动模块 activity
     */
    public static final String InvitFirendsActivity = "/activity/InvitFirendsActivity";
    public static final String CreateRedPackageActivity = "/activity/CreateRedPackageActivity";
    /*
     网格
     */
    public static final String HistoryGridActivity = "/grid/HistoryGridActivity";
    public static final String GridExecutionDetailsActivity = "/grid/GridExecutionDetailsActivity";

    /*
      委托详情
    *  */

    public static final String CurrentEntrustActivity = "/main/CurrentEntrustFragment";
    public static final String EntrustActivity = "/main/EntrustActivity";
    public static final String EntrustDetialsActivity = "/main/EntrustDetialsActivity";
    public static final String HistoryEntrustActivity = "/main/HistoryEntrustFragment";
    public static final String MarketDetail4Activity = "/main/MarketDetail4Activity";
    public static final String HorizonMarketDetailActivity = "/main/HorizonMarketDetailActivity";

    /* 委托详情 */
    public static final String EntrustDetailActivity = "/main/EntrustDetailActivity";

    /*
     * 合约模块
     */
    public static final String B2CCashFlowActivity = "/main/B2CCashFlowActivity";

    public static final String B2CCashFlowDetailActivity = "/main/B2CCashFlowDetailActivity";

    public static final String B2CRechargeActivity = "/main/B2CRechargeActivity";

    public static final String B2CWithdrawAccountListActivity = "/main/B2CWithdrawAccountListActivity";

    /*
     * 资产模块
     */
    public static final String NewVersionMyAssetActivity = "/asset/NewVersionMyAssetActivity";
    public static final String CurrencyLendingRecordsActivity = "/asset/CurrencyLendingRecordsActivity";
    public static final String GiveBackActivity = "/asset/GiveBackActivity";
    public static final String B2CWithdrawActivity = "/asset/B2CWithdrawActivity";
    public static final String B2CWithdrawAccountActivity = "/asset/B2CWithdrawAccountActivity";
    public static final String B2CBankListActivity = "/asset/B2CBankListActivity";
    public static final String B2CRecordsActivity = "/asset/B2CRecordsActivity";
    public static final String NewVersionBorrowingActivity = "/asset/NewVersionBorrowingActivity";
    public static final String NewVersionTransferActivity = "/asset/NewVersionTransferActivity";
    public static final String WithdrawActivity = "/asset/WithdrawActivity";
    public static final String SelectCoinActivity = "/asset/SelectCoinActivity";
    public static final String WithdrawSelectCoinActivity = "/asset/WithdrawSelectCoinActivity";
    public static final String IdentityAuthenticationResultActivity = "/asset/IdentityAuthenticationResultActivity";
    public static final String IdentityAuthenticationActivity = "/asset/IdentityAuthenticationActivity";
    public static final String NewVersionOTCActivity = "/home/NewVersionOtcActivity";
    /*
     * web页面
     */
    public static final String ItemDetailActivity = "/web/ItemDetailActivity";

    /*
     * 搜索模块
     */
    public static final String CoinMapActivity = "/search/CoinMapActivity";
    public static final String NewCoinMapActivity = "/search/NewCoinMapActivity";

    /**
     * 杠杆
     */
    public static final String HistoryLoanActivity = "/lever/HistoryLoanActivity";
    public static final String CurrentLoanActivity = "/lever/CurrentLoanActivity";
    public static final String BorrowRecordsActivity = "/lever/BorrowRecordsActivity";
    public static final String LeverDrawRecordActivity = "/lever/LeverDrawRecordActivity";
    public static final String LeverTransferRecordActivity = "/lever/LeverTransferRecordActivity";


    /*
     * 订单
     */
    public static final String NewOTCOrdersActivity = "/order/NewOTCOrdersActivity";

    public static final String NewVersionCodeActivity = "/login/codeVerification";

    public static final String LikeEditActivity = "/search/EditActivity";

    public static final String LeverActivity = "/lever/LeverActivity";
    public static final String TradeETFQuestionActivity = "/lever/TradeETFQuestionActivity";



    /**
     * freeStaking
     */
    public static final String FreeStakingActivity = "/freestaking/FreeStakingActivity";
    public static final String IncomeDetailActivity = "/freestaking/IncomeDetailActivity";
    public static final String PosDetailsActivity = "/freestaking/PosDetailsActivity";
    public static final String PositionRecordActivity = "/freestaking/PositionRecordActivity";
    public static final String ProjectDescriptionActivity = "/freestaking/ProjectDescriptionActivity";

    public static final String DirectlyWithdrawActivity = "/asset/DirectlyWithdrawActivity";


}
