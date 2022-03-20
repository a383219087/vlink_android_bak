package com.yjkj.chainup.new_version.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.R;
import com.yjkj.chainup.manager.NCoinManager;

import org.json.JSONObject;

/**
 * @author Bertking
 * @date 2018/5/28
 * <p>
 * 参考链接：https://blog.csdn.net/csdnzouqi/article/details/53642130
 */
public class CoinMapAdapter extends BaseQuickAdapter<JSONObject, BaseViewHolder> {
    public static final String TAG = CoinMapAdapter.class.getSimpleName();

    boolean isSearch;
    boolean leverStatus;

    public CoinMapAdapter() {
        super(R.layout.item_coin_map);
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    public void setLeverStatus(boolean status) {
        leverStatus = status;
    }

    @Override
    protected void convert(BaseViewHolder helper, JSONObject item) {
        if (item == null) {
            return;
        }
        if (leverStatus) {
            helper.setGone(R.id.ib_add, true);
            helper.setGone(R.id.tv_market, true);
            helper.setText(R.id.tv_coin, NCoinManager.getShowMarketName(item.optString("name")));
        } else {
            String name = NCoinManager.showAnoterName(item);
            if (null == name || !name.contains("/"))
                return;


            String[] split = name.split("/");
            helper.setText(R.id.tv_coin, split[0]);
            helper.setText(R.id.tv_market, "/" + split[1]);

            if (isSearch) {
                helper.getView(R.id.ib_add).setVisibility(View.GONE);
            } else {
                helper.getView(R.id.ib_add).setVisibility(View.VISIBLE);
            }

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
    }

    public int getCurrentPosition(int position) {
        return getHeaderLayoutCount() + position;
    }

}
