package com.yjkj.chainup.treaty.dialog

/**
 * @Author lianshangljl
 * @Date 2019/1/29-8:00 PM
 * @Email buptjinlong@163.com
 * @description
 */

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.yjkj.chainup.R


class ContractDialog {
    interface ConfirmListener {
        fun onClick()
    }

    companion object {
        /**
         * NSF的弹窗
         * @param context
         * @param title
         * @param isShowImage 是否显示图片
         * @param listener
         */
        private fun showDialog(context: Context,
                               title: String,
                               content: String) {
            TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.item_contract_dialog)
                    .setScreenWidthAspect(context, 0.8f)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(false)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.setText(R.id.tv_title, title)
                        viewHolder?.setText(R.id.tv_content, content)

                    }
                    .addOnClickListener(R.id.tv_cancel)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.tv_cancel -> {
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
        }

        /**
         * 强平价格
         */
        fun showDialog4FlatPricer(context: Context) {
            showDialog(context, context.getString(R.string.contract_text_liqPrice), context.getString(R.string.contract_flat_price_content))
        }


        /**
         * 风险评估
         */
        fun showDialog4Risk(context: Context) {
            showDialog(context, context.getString(R.string.cost), context.getString(R.string.risk_show_tips))
        }


        /**
         * 成本
         */
        fun showDialog4TheCostOf(context: Context) {
            showDialog(context, context.getString(R.string.cost), context.getString(R.string.contract_cost_content))
        }

        /**
         * 标记价格
         */
        fun showDialog4PriceTag(context: Context) {
            showDialog(context, context.getString(R.string.contract_price_tag), context.getString(R.string.contract_price_tag_content))
        }

        /**
         * 保证金
         */
        fun showDialog4Margin(context: Context) {
            showDialog(context, context.getString(R.string.contract_margin), context.getString(R.string.contract_margin_content))
        }


        /**
         * 价格
         */
        fun showDialog4ThePrice(context: Context) {
            showDialog(context, context.getString(R.string.contract_price_title), context.getString(R.string.index_mark_price))
        }

        /**
         *账户余额
         */
        fun showDialog4AccountBalance(context: Context) {
            showDialog(context, context.getString(R.string.contract_wallet_balance_title), context.getString(R.string.contract_wallet_balance_content))
        }

        /**
         * 市价平仓
         */
        fun showDialog4MarketPrice(context: Context) {
            showDialog(context, context.getString(R.string.contract_action_marketPrice), context.getString(R.string.contract_market_content))
        }

        /**
         * 账户权益
         */
        fun showDialog4AccountRights(context: Context) {
            showDialog(context, context.getString(R.string.contract_assets_account_equity), context.getString(R.string.contracnt_margin_balance_content))
        }

        /**
         * 可用余额
         */
        fun showDialog4AvailableBalance(context: Context) {
            showDialog(context, context.getString(R.string.withdraw_text_available), context.getString(R.string.contract_available_balance_content))
        }

        /**
         * 未实现盈亏
         */
        fun showDialog4UnrealisedPNL(context: Context) {
            showDialog(context, context.getString(R.string.contract_unrealised_pnl_title), context.getString(R.string.contract_unrealised_pnl_content))
        }

        /**
         * 仓位保证金
         */
        fun showDialog4PositionMarginL(context: Context) {
            showDialog(context, context.getString(R.string.contract_text_positionMargin), context.getString(R.string.contract_position_margin_content))
        }

        /**
         * 委托保证金
         */
        fun showDialog4OrderMargin(context: Context) {
            showDialog(context, context.getString(R.string.contract_text_orderMargin), context.getString(R.string.contract_order_margin_content))
        }


    }
}