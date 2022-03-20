package com.yjkj.chainup.new_version.activity;

import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.R;
import com.yjkj.chainup.bean.coin.CoinBean;
import com.yjkj.chainup.manager.NCoinManager;
import com.yjkj.chainup.util.GlideUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bertking
 * @description 搜索币种
 * @date 2018/6/7
 */
public class SelectCoinAdapter extends BaseQuickAdapter<CoinBean, BaseViewHolder> implements Filterable {
    private MyFilter filter = null;
    private FilterListener listener = null;
    private List<CoinBean> list = new ArrayList<>();
    private List<CoinBean> beanList = new ArrayList<>();
    private int position;


    public SelectCoinAdapter(@Nullable List<CoinBean> data, int position) {
        super(R.layout.item_select_coin, data);
        this.list = data;
        this.beanList = data;
        this.position = position;
    }


    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, CoinBean item) {
        if (item == null) return;
        GlideUtils.loadCoinIcon(getContext(), item.getName(), helper.getView(R.id.iv_coin));
        helper.setText(R.id.tv_coin, NCoinManager.getShowMarket(item.getName()));
//        helper.setVisible(R.id.iv_selected, item.isSelected());
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new MyFilter(list);
        }
        return filter;
    }


    public interface FilterListener {
        void getFilterData(List<CoinBean> list);//获取过滤后的数据
    }


    /**
     * 创建内部类，实现数据的过滤
     */
    class MyFilter extends Filter {
        private List<CoinBean> originalData = new ArrayList<>();

        public MyFilter(List<CoinBean> originalData) {
            this.originalData = originalData;
        }

        /**
         * 该方法返回搜索过滤后的数据
         *
         * @param constraint
         * @return
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
/**
 * 没有搜索内容的话就还是给results赋值原始数据的值和大小
 * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
 *
 */
            if (TextUtils.isEmpty(constraint)) {
                results.values = beanList;
                results.count = beanList.size();
            } else {
                // 创建集合保存过滤后的数据
                List<CoinBean> filteredList = new ArrayList<CoinBean>();
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (CoinBean s : originalData) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    if (NCoinManager.getShowMarket(s.getName()).toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
                        // 规则匹配的话就往集合中添加该数据
                        filteredList.add(s);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }

            // 返回FilterResults对象
            return results;
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            // 获取过滤后的数据
            list = (List<CoinBean>) results.values;
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            if (listener != null) {
                listener.getFilterData(list);
            }
            // 刷新数据源显示
            notifyDataSetChanged();
            notifyItemRangeChanged(0, list.size());
        }
    }


}
