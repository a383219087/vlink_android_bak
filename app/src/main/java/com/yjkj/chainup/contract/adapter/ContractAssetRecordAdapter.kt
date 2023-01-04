package com.yjkj.chainup.contract.adapter

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.utils.CpColorUtil
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.common.sdk.utlis.TimeFormatUtils
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractCashBook
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.contract.widget.pswkeyboard.widget.PopEnterPassword
import com.yjkj.chainup.util.ToastUtils
import java.text.DecimalFormat

/**
 * 合约资产记录
 */
class ContractAssetRecordAdapter(ctx: Context, data: ArrayList<ContractCashBook>) : BaseQuickAdapter<ContractCashBook, BaseViewHolder>(R.layout.sl_item_asset_record, data), LoadMoreModule {

    var journalAccount_text_amount = ""
    var sl_str_fee = ""
    var sl_str_balance = ""
    var sl_str_buy_open = ""
    var sl_str_buy_close = ""
    var sl_str_sell_close = ""
    var sl_str_sell_open = ""
    var sl_str_transfer_bb2contract = ""
    var sl_str_transfer_contract2bb = ""
    var sl_str_transferim_position2contract = ""
    var sl_str_transferim_contract2position = ""
    var contract_fee_share = ""
    var sl_str_air_drop = ""
    var sl_str_contract_bonus_Issue = ""
    var sl_str_contract_bonus_recycle = ""
    var contract_cash_book_action_system_fee_in = ""

    init {
        journalAccount_text_amount = ctx.getLineText("journalAccount_text_amount")
        sl_str_fee = ctx.getLineText("sl_str_fee")
        sl_str_balance = ctx.getLineText("sl_str_balance")
        sl_str_buy_open = ctx.getLineText("sl_str_buy_open")
        sl_str_buy_close = ctx.getLineText("sl_str_buy_close")
        sl_str_sell_close = ctx.getLineText("sl_str_sell_close")
        sl_str_sell_open = ctx.getLineText("sl_str_sell_open")
        sl_str_transfer_bb2contract = ctx.getLineText("sl_str_transfer_bb2contract")
        sl_str_transfer_contract2bb = ctx.getLineText("sl_str_transfer_contract2bb")
        sl_str_transferim_position2contract = ctx.getLineText("sl_str_transferim_position2contract")
        sl_str_transferim_contract2position = ctx.getLineText("sl_str_transferim_contract2position")
        contract_fee_share = ctx.getLineText("contract_fee_share")
        sl_str_air_drop = ctx.getLineText("sl_str_air_drop")
        sl_str_contract_bonus_Issue = ctx.getLineText("contract_bonus_Issue")
        sl_str_contract_bonus_recycle = ctx.getLineText("contract_bonus_recycle")
        contract_cash_book_action_system_fee_in = ctx.getLineText("contract_cash_book_action_system_fee_in")
    }

    override fun convert(helper: BaseViewHolder, item: ContractCashBook) {
        helper?.run {
            val contract = ContractPublicDataAgent.getContract(item.instrument_id)
            val volIndex: Int = contract?.value_index ?: 6
            val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)

            //金额
            setText(R.id.tv_amount_value, dfDefault.format(MathHelper.round(item.deal_count, volIndex)))
            getView<TextView>(R.id.tv_amount_label).text = journalAccount_text_amount
            //手续费
            setText(R.id.tv_fee_value, dfDefault.format(MathHelper.round(item.fee, volIndex)))
            getView<TextView>(R.id.tv_fee_label).text = sl_str_fee
            //余额
            setText(R.id.tv_balance_value, dfDefault.format(MathHelper.round(item.last_assets, volIndex)))
            getView<TextView>(R.id.tv_balance_label).text = sl_str_balance
            //时间
            setText(R.id.tv_time_value, TimeFormatUtils.convertZTime(item.created_at, TimeFormatUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND))
            //类型
            val action: Int = item.action
            val tvTypeValue = getView<TextView>(R.id.tv_type_value)
            var typeColor = CpColorUtil.getMainColorType(true)
            var typeText = "--"
            when (action) {
                1 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = sl_str_buy_open
                }
                2 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = sl_str_buy_close
                }
                3 -> {
                    typeColor = CpColorUtil.getMainColorType(false)
                    typeText = sl_str_sell_close
                }
                4 -> {
                    typeColor = CpColorUtil.getMainColorType(false)
                    typeText = sl_str_sell_open
                }
                5, 7, 22 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = sl_str_transfer_bb2contract
                }
                6, 8, 23 -> {
                    typeColor = CpColorUtil.getMainColorType(false)
                    typeText = sl_str_transfer_contract2bb
                }
                9 -> {
                    typeColor = CpColorUtil.getMainColorType(false)
                    typeText = sl_str_transferim_position2contract
                }
                10 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = sl_str_transferim_contract2position
                }
                11 -> {
                    typeColor = CpColorUtil.getMainColorType(false)
                    typeText = sl_str_transferim_contract2position
                }
                12 -> {
                    //赠金发放 contract_bonus_Issue
                    typeText = sl_str_contract_bonus_Issue
                    typeColor = CpColorUtil.getMainColorType(true)
                }
                13 -> {
                    //赠金回收 contract_bonus_recycle
                    typeText = sl_str_contract_bonus_recycle
                    typeColor = CpColorUtil.getMainColorType(false)
                }
                19 -> {
                    typeText = contract_cash_book_action_system_fee_in
                    typeColor = CpColorUtil.getMainColorType(false)
                }
                20 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = contract_fee_share
                }
                21 -> {
                    typeColor = CpColorUtil.getMainColorType(true)
                    typeText = sl_str_air_drop
                }
            }
            tvTypeValue.run {
                text = typeText
                setTextColor(typeColor)
            }
        }
    }

    /**
     * CONTRACT_CASH_BOOK_ACTION_UNKOWN                   CONTRACT_CASH_BOOK_ACTION = iota // value --> 0
    CONTRACT_CASH_BOOK_ACTION_TRADE_BUY_OL                                              // 1 开多买
    CONTRACT_CASH_BOOK_ACTION_TRADE_BUY_CS                                              // 2 平空买
    CONTRACT_CASH_BOOK_ACTION_TRADE_SELL_CL                                             // 3 平多卖
    CONTRACT_CASH_BOOK_ACTION_TRADE_SELL_OS                                             // 4 开空卖
    CONTRACT_CASH_BOOK_ACTION_IN_FROM_SPOT_ACCOUNT                                      // 5 从现货账号转入
    CONTRACT_CASH_BOOK_ACTION_OUT_TO_SPOT_ACCOUNT                                       // 6 转出到现货账号
    CONTRACT_CASH_BOOK_ACTION_IN_FROM_CONTRACT_ACCOUNT                                  // 7 从合约账号转入
    CONTRACT_CASH_BOOK_ACTION_OUT_TO_CONTRACT_ACCOUNT                                   // 8 转出到合约账号
    CONTRACT_CASH_BOOK_ACTION_IN_FROM_MARGIN                                            // 9 减少保证金,将保证金从仓位转移到合约账户
    CONTRACT_CASH_BOOK_ACTION_OUT_TO_MARGIN                                             // 10 增加保证金,将合约账户的资产转移到仓位保证金
    CONTRACT_CASH_BOOK_ACTION_POSITION_FEE                                              // 11 仓位的资金费用
    CONTRACT_CASH_BOOK_ACTION_BONUS_IN                                                  // 12 转入赠金
    CONTRACT_CASH_BOOK_ACTION_BONUS_OUT                                                 // 13 转出赠金
    CONTRACT_CASH_BOOK_ACTION_BONUS_OUTALL                                              // 14 转出所有赠金
    CONTRACT_CASH_BOOK_ACTION_RISK_IN                                                   // 15 增加系统风险储备金
    CONTRACT_CASH_BOOK_ACTION_RISK_OUT                                                  // 16 减少系统风险储备金
    CONTRACT_CASH_BOOK_ACTION_LIABILITY_ADL                                             // 17 ADL增加系统负债
    CONTRACT_CASH_BOOK_ACTION_LIABILITY_SETTLE                                          // 18 资金费率结算增加系统负债
    CONTRACT_CASH_BOOK_ACTION_SYSTEM_FEE_IN                                             // 19 系统手续费结算
    CONTRACT_CASH_BOOK_ACTION_SHARE_FEE_IN                                              // 20 商户手续费结算
    CONTRACT_CASH_BOOK_ACTION_AIRDROP                                                   // 21 空投
    CONTRACT_CASH_BOOK_ACTION_CLOUD_IN                                                  // 22 合约云账号转入资金
    CONTRACT_CASH_BOOK_ACTION_CLOUD_OUT                                                 // 23 合约云账号划出资金
     */


}