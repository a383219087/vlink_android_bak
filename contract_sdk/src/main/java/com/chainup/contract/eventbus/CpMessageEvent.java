package com.chainup.contract.eventbus;

import org.json.JSONObject;

public class CpMessageEvent {

    public static final int data_req_error = 1;   //数据请求失败，可用作全局处理

    public static final int collect_data_type = 2;   //自选或收藏数据事件

    public static final int hometab_switch_type = 3;   //首页tab切换事件

    public static final int color_rise_fall_type = 4;   //涨跌幅颜色

    public static final int symbol_switch_type = 5;   //切换币种事件

    public static final int TRANSFER_TYPE = 6;   //买or卖事件类型

    public static final int assetTabType = 7;   //资产页面tab切换

    public static final int coinSearchType = 8;   //侧边栏币对搜索

    public static final int closeLeftCoinSearchType = 9;   //关闭侧边栏事件

    public static final int login_operation_type = 10;   //登录操作事件

    public static final int coin_payment = 11; //币币交易或者法币交易交易

    public static final int fait_trading = 12; //去交易

    public static final int left_coin_contract_type = 13; //合约侧边栏

    public static final int refresh_trans_type = 14; // 资产页面刷新

    public static final int refresh_local_trans_type = 15; // 资产页面局部刷新

    public static final int refresh_local_b2c_trans_type = 16; // 资产页面局部刷新

    public static final int refresh_local_coin_trans_type = 17; // 资产页面局部刷新

    public static final int refresh_local_b2c_coin_trans_type = 18; // 资产页面局部刷新


    public static final int refresh_local_contract_type = 19; // 资产页面合约局部刷新

    public static final int refresh_local_lever_type = 20; // 资产页面 杠杆

    public static final int DEPTH_LEVEL_TYPE = 21; //深度刻度
    public static final int DEPTH_DATA_TYPE = 22; //深度图的数据
    public static final int DEPTH_CONTRACT_DATA_TYPE = 220; //合约深度图的数据

    public static final int CREATE_ORDER_TYPE = 23; //下单通知

    // 币币 or lever
    public static final int TAB_TYPE = 24;

    public static final int into_transfer_activity = 25; //进入划转页面

    public static final int into_my_asset_activity = 26; //是否全开

    //public static final int live_contract_asset_beanList = 28; //首页跳转


    public static final int coinTrade_tab_type = 29; //币币交易页面tab

    public static final int coinTrade_topTab_type = 30; //币币交易页面顶部币币tab

    public static final int leverTrade_topTab_type = 31; //币币交易页面顶部杠杆tab

    public static final int assetsTab_type = 32; //币币交易页面顶部杠杆tab

    public static final int assets_activity_finish_event = 33; //首页跳转

    public static final int contract_switch_type = 37;   //合约tab切换事件
    public static final int market_switch_type = 38;   //合约tab切换事件
    public static final int login_bind_type = 40;   //登录操作事件
    public static final int market_switch_curTime = 399;   //合约tab切换事件

    public static final int sl_contract_select_leverage_event = 400;   //合约切换杠杆
    public static final int sl_contract_left_coin_type = 401;//切换合约币种
    public static final int sl_contract_switch_time_type = 402;//切换K线区间


    public static final int sl_contract_modify_margin_event = 50;//修改保证金
    public static final int sl_contract_modify_leverage_event = 52;//修改杠杆
    public static final int sl_contract_change_tagPrice_event = 53;//标记价格改变、指数价格改变
    public static final int sl_contract_modify_position_margin_event = 60;//调整保证金
    public static final int sl_contract_refresh_position_list_event = 61;//刷新持仓列表
    public static final int sl_contract_create_account_event = 62;//弹出开通合约对话框
    public static final int sl_contract_calc_switch_contract_event = 63;//合约计算器切换合约通知
    public static final int sl_contract_sidebar_market_event = 64;//侧边栏行情
    public static final int sl_contract_first_show_info_event = 65;//首次加载合约信息
    public static final int sl_contract_change_position_model_event = 67;//改变持仓模式
    public static final int sl_contract_change_unit_event = 68;//改变仓位展示单位
    public static final int sl_contract_login_status_event = 69;//未登录状态改变合约按钮
    public static final int sl_contract_first_input_last_price_event = 70;//切换合约后第一次填入当前价格
    public static final int sl_contract_logout_event = 71;//切换合约后检测到未登录清空原有页面显示的数据
    public static final int sl_contract_new_status_event = 72;//新合约
    public static final int sl_contract_page_hide_event = 73;//合约tab页面隐藏通知
    public static final int sl_contract_depth_level_event = 74;//合约深度切换
    public static final int sl_contract_force_event = 75;//强制使用新合约
    public static final int sl_contract_receive_coupon = 76;//领取模拟合约体验金
    public static final int sl_contract_go_login_page = 77;//跳转登录
    public static final int sl_contract_go_fundsTransfer_page = 78;//跳转资金流水
    public static final int sl_contract_refresh_current_entrust_list_event = 79;//刷新当前委托列表
    public static final int sl_contract_refresh_plan_entrust_list_event = 80;//刷新计划委托列表
    public static final int sl_contract_record_switch_contract_event = 81;
    public static final int sl_contract_record_switch_entrust_type_event = 82;
    public static final int sl_contract_record_switch_order_type_event = 83;
    public static final int sl_contract_record_switch_tab_event = 84;
    public static final int sl_contract_open_contract_event = 85;
    public static final int sl_contract_switch_lever_event = 86;
    public static final int sl_contract_create_order_event = 87;
    public static final int sl_contract_req_current_entrust_list_event = 88;
    public static final int sl_contract_req_plan_entrust_list_event = 89;
    public static final int sl_contract_req_position_list_event = 90;
    public static final int sl_contract_req_modify_leverage_event = 91;
    public static final int sl_contract_record_switch_contract_side_event = 92;
    public static final int sl_contract_modify_depth_event = 93;
    public static final int sl_contract_go_kyc_page = 94;
    public static final int sl_contract_calc_switch_margin_coin_event = 95;//合约保证金切换
    public static final int sl_contract_calc_switch_contract_id = 96;//选中合约的通知

    private CpMessageEvent() {
    }

    private Object msg_content;//事件内容
    private Object msg_content_data;//事件内容
    private int msg_type;//事件类型
    private boolean isLever;//是否是杠杆

    public CpMessageEvent(int msg_type) {
        this.msg_type = msg_type;
    }


    public CpMessageEvent(int msg_type, Object msg_content) {
        this.msg_content = msg_content;
        this.msg_type = msg_type;
    }

    public CpMessageEvent(boolean isLever) {
        this.isLever = isLever;
    }

    public CpMessageEvent(int msg_type, Object msg_content, boolean isLever) {
        this.msg_type = msg_type;
        this.msg_content = msg_content;
        this.isLever = isLever;
    }

    public CpMessageEvent setMsg_content(Object msg_content) {
        this.msg_content = msg_content;
        return this;
    }

    public Object getMsg_content() {
        return msg_content;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public boolean isLever() {
        return isLever;
    }

    public void setLever(boolean lever) {
        isLever = lever;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "msg_content=" + msg_content +
                ", msg_type=" + msg_type +
                ", isLever=" + isLever +
                '}';
    }

    public Object getMsg_content_data() {
        return msg_content_data;
    }

    public void setMsg_content_data(Object msg_content_data) {
        this.msg_content_data = msg_content_data;
    }

    public boolean dataIsNotNull() {
        return msg_content_data != null;
    }

    public JSONObject getDataJson() {
        return (JSONObject) msg_content_data;
    }
}