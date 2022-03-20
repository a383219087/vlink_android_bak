package com.yjkj.chainup.new_version.adapter;

import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.R;
import com.yjkj.chainup.bean.address.AddressBean;

import java.util.ArrayList;

/**
 * @author Bertking
 * @date 2018/6/2
 * @description 提现地址
 * <p>
 * @Bean的内容 {
 * "id": 522,
 * "uid": 10609,
 * "symbol": "BTC",
 * "address": "1LJkiVMaXBW6F55g8MysUhdGgsrbTe92Zz",
 * "label": "BTC",
 * "status": 1,
 * "ctime": 1529653630000
 * }
 */
public class WithdrawAddressAdapter extends BaseQuickAdapter<AddressBean.Address, BaseViewHolder> {
    private int position = 0;

    public WithdrawAddressAdapter(@Nullable ArrayList<AddressBean.Address> data) {
        super(R.layout.item_withdraw_address, data);
    }

    private String selectAddress;

    public void setAddress(String address) {
        selectAddress = address;
    }

    @Override
    protected void convert(BaseViewHolder helper, AddressBean.Address item) {
        if (item == null) {
            return;
        }
        if (!TextUtils.isEmpty(item.getAddress())) {
            String[] split = item.getAddress().split("_");
            if (split != null && split.length > 1) {
                helper.setGone(R.id.tv_address_tag, false);

                /**
                 * 地址
                 */
                helper.setText(R.id.tv_address, split[0]);
                helper.setText(R.id.tv_address_tag, split[1]);
            } else {
                /**
                 * 地址
                 */
                helper.setText(R.id.tv_address, item.getAddress());
                helper.setGone(R.id.tv_address_tag, true);
            }
        }
        if (selectAddress != null) {
            if (selectAddress.equals(item.getAddress())) {
                helper.setGone(R.id.iv_address_image, false);
            } else {
                helper.setGone(R.id.iv_address_image, true);
            }
        }

        if (item.getTrustType() == 1) {
            helper.setGone(R.id.ll_certification, false);
        } else {
            helper.setGone(R.id.ll_certification, true);
        }

        /**
         * 地址名称
         */
        helper.setText(R.id.tv_address_name, item.getLabel());
    }

    public void setSelectAddr(int position) {
        this.position = position;
        notifyDataSetChanged();
    }
}
