package com.yjkj.chainup.new_version.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.R;

import org.json.JSONObject;

/**
 * @author Bertking
 * @date 2018/5/28
 * <p>
 * 参考链接：https://blog.csdn.net/csdnzouqi/article/details/53642130
 */
public class HeYueMapAdapter extends BaseQuickAdapter<JSONObject, BaseViewHolder> {
    public static final String TAG = HeYueMapAdapter.class.getSimpleName();


    public HeYueMapAdapter() {
        super(R.layout.item_heyue_map);
    }


    @Override
    protected void convert(BaseViewHolder helper, JSONObject item) {
//        {"id":1,"contractName":"E-BTC-USDT","symbol":"BTC-USDT","contractType":"E","coType":"E","contractShowType":"永续合约",
//                "deliveryKind":"0","contractSide":1,"multiplier":1.0E-4,"multiplierCoin":"BTC","marginCoin":"USDT","marginRate":1,
//                "capitalStartTime":0,"capitalFrequency":1,"settlementFrequency":1,"brokerId":1,"base":"BTC","quote":"USDT","coinResultVo":
//            {"symbolPricePrecision":2,"depth":["2","1","0"],"minOrderVolume":1,"minOrderMoney":1,"maxMarketVolume":5000000,"maxMarketMoney":
//                10000000,"maxLimitVolume":10000000,"maxLimitMoney":10000000,"priceRange":1,"marginCoinPrecision":6,"fundsInStatus":1,
//                    "fundsOutStatus":1},"sort":1,"maxLever":125,"minLever":1}
        helper.setText(R.id.tv_coin, item.optString("symbol"));


        /**
         * 初始化状态
         */
        boolean isAdd = item.optBoolean("isAdd");
        if (isAdd) {
            ((ImageView) helper.getView(R.id.ib_add)).setImageResource(R.drawable.quotes_optional_selected);
        } else {
            ((ImageView) helper.getView(R.id.ib_add)).setImageResource(R.drawable.quotes_optional_default);
        }
        /**
         * 添加操作
         *
         */


}

    public int getCurrentPosition(int position) {
        return getHeaderLayoutCount() + position;
    }

}
