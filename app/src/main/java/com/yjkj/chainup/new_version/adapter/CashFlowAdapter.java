package com.yjkj.chainup.new_version.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.util.DateUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.app.AppConstant;
import com.yjkj.chainup.bean.fund.CashFlowBean;
import com.yjkj.chainup.util.LanguageUtil;
import com.yjkj.chainup.manager.NCoinManager;
import com.yjkj.chainup.util.BigDecimalUtils;

import java.util.List;

import static com.yjkj.chainup.new_version.activity.asset.ChargeSymbolRecordActivityKt.OTC_TRANSFER;
import static com.yjkj.chainup.new_version.activity.asset.ChargeSymbolRecordActivityKt.OTHER_INDEX;
import static com.yjkj.chainup.new_version.activity.asset.ChargeSymbolRecordActivityKt.RECHARGE_INDEX;
import static com.yjkj.chainup.new_version.activity.asset.ChargeSymbolRecordActivityKt.WITHDRAW_INDEX;

/**
 * @author Bertking
 * @date 2018/5/23
 * <p>
 */
public class CashFlowAdapter extends BaseQuickAdapter<CashFlowBean.Finance, BaseViewHolder> {

    int index = 0;


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public CashFlowAdapter(@Nullable List<CashFlowBean.Finance> data) {
        super(R.layout.item_cashflow_com, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CashFlowBean.Finance item) {
        if (item == null) {
            return;
        }

        switch (index) {
            case RECHARGE_INDEX:

                //日期
                helper.setText(R.id.tv_date, item.getTranCreateTime());

                // 划转数量
                helper.setText(R.id.tv_count, BigDecimalUtils.divForDown(item.getAmount(), NCoinManager.getCoinShowPrecision(item.getCoinSymbol())).toPlainString());

                // 状态//'充值状态: 0 未确认，1 已完成，2 异常'
                helper.setText(R.id.tv_status, item.getStatusText());


                break;
            case WITHDRAW_INDEX:

                //日期
                helper.setText(R.id.tv_date, item.getTranCreateTime());


                // 划转数量
                helper.setText(R.id.tv_count, BigDecimalUtils.divForDown(item.getAmount(), NCoinManager.getCoinShowPrecision(item.getCoinSymbol())).toPlainString());

                // 状态/提现状态: 0 未审核，1 已审核，2 审核拒绝，3 支付中，4 支付失败，5 已完成，6 已撤销
                helper.setText(R.id.tv_status, item.getStatusText());


                break;
            case OTHER_INDEX:

                if (AppConstant.Companion.getIS_NEW_CONTRACT()) {
                    //日期
                    helper.setText(R.id.tv_date, DateUtil.INSTANCE.longToString("yyyy-MM-dd HH:mm:ss", item.getCreatedAtTime()));
                    // 划转数量
                    helper.setText(R.id.tv_count, BigDecimalUtils.divForDown(item.getAmount(), NCoinManager.getCoinShowPrecision(item.getCoinSymbol())).toPlainString());
                    // 状态//"status": 1 转入  2 转出
                    String statusStr = "";
                    if (item.getStatus() == 1) {
                        statusStr = LanguageUtil.getString(getContext(),"contract_assets_record_transfer_from_swap_account");
                    } else {
                        statusStr = LanguageUtil.getString(getContext(),"contract_assets_record_transfer_to_swap_account");
                    }
                    helper.setText(R.id.tv_status, statusStr);
                } else {
                    //日期
                    helper.setText(R.id.tv_date, item.getTranCreateTime());
                    // 划转数量
                    helper.setText(R.id.tv_count, BigDecimalUtils.divForDown(item.getAmount(), NCoinManager.getCoinShowPrecision(item.getCoinSymbol())).toPlainString());
                    // 状态//'充值状态: 0 未确认，1 已完成，2 异常'
                    helper.setText(R.id.tv_status, item.getStatusText());
                }


                break;
            case OTC_TRANSFER:
                //日期
                helper.setText(R.id.tv_date, item.getCreateTime());


                // 划转数量
                helper.setText(R.id.tv_count, BigDecimalUtils.divForDown(item.getAmount(), NCoinManager.getCoinShowPrecision(item.getCoinSymbol())).toPlainString());

                // 状态//'充值状态: 0 未确认，1 已完成，2 异常'
                helper.setText(R.id.tv_status, item.getTransactionType_text());

                break;
            default:
                break;
        }


    }
}
