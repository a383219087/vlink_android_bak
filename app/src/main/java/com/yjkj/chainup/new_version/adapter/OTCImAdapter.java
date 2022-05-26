package com.yjkj.chainup.new_version.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yjkj.chainup.util.DateUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.db.service.UserDataService;
import com.yjkj.chainup.new_version.bean.OTCIMMessageBean;
import com.yjkj.chainup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lianshangljl
 * @Date 2018/10/22-上午10:26
 * @Email buptjinlong@163.com
 * @description
 */
public class OTCImAdapter extends BaseAdapter {

    private Context context;
    private List<OTCIMMessageBean> mList = new ArrayList<>();
    private int userId = 0;
    private String status, time, url;

    public OTCImAdapter(Context context, List<OTCIMMessageBean> mList, String status, String time, String url) {
        this.context = context;
        this.mList = mList;
        if (UserDataService.getInstance().getUserData() != null) {
            userId = Integer.parseInt(UserDataService.getInstance().getUserInfo4UserId());
        }
        this.status = status;
        this.time = time;
        this.url = url;
    }

    public void setmList(List<OTCIMMessageBean> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }


    private static Integer TYPE_MINE = 0;
    private static Integer TYPE_OTHER = 1;
    private static Integer TYPE_EMPTY = 2;
    private static Integer TYPE_SYSTEM = 3;


    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null || mList.size() <= position ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OTCIMMessageBean message = (OTCIMMessageBean) getItem(position);

        int type = getItemViewType(position);
        return getItemView(convertView, message, type);
    }

    private View getItemView(View convertView, OTCIMMessageBean entity, int type) {
        if (entity == null)
            return convertView;

        if (type == TYPE_MINE) {
            return getMineView(convertView, entity);
        } else if (type == TYPE_OTHER) {
            return getOtherView(convertView, entity);
        } else if (type == TYPE_EMPTY) {
            return getEmpty(convertView, entity);
        } else {
            return getSystem(convertView);
        }
    }

    private View getMineView(View convertView, OTCIMMessageBean entity) {
        if (entity == null)
            return convertView;
        MineViewHolder holder;
        if (convertView == null || convertView.getTag(R.id.otc_chat_item_tag_type_mine) != TYPE_MINE) {
            holder = new MineViewHolder();
            holder.mainView.setTag(R.id.otc_chat_item_tag_type_mine, TYPE_MINE);
            holder.mainView.setTag(R.id.otc_chat_item_tag_holder_mine, holder);
        } else {
            holder = (MineViewHolder) convertView.getTag(R.id.otc_chat_item_tag_holder_mine);
        }
        holder.tvContent.setText(entity.getContent());

        try {
            holder.imhead.setText(entity.getFromName().substring(0, 1));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imhead.setText("");
        }

        try {
            holder.tvTime.setText(DateUtil.INSTANCE.longToString("HH:mm:ss", Long.parseLong(entity.getCtime())));
        } catch (Exception e) {
            e.printStackTrace();
            holder.tvTime.setText(entity.getCtime());
        }

        return holder.mainView;
    }

    private View getEmpty(View convertView, OTCIMMessageBean entity) {
        if (entity == null)
            return convertView;
        EmViewHolder holder;
        if (convertView == null || convertView.getTag(R.id.otc_chat_item_tag_type_mine) != TYPE_EMPTY) {
            holder = new EmViewHolder();
            holder.mainView.setTag(R.id.otc_chat_item_tag_type_mine, TYPE_EMPTY);
            holder.mainView.setTag(R.id.otc_chat_item_tag_holder_mine, holder);
        } else {
            holder = (EmViewHolder) convertView.getTag(R.id.otc_chat_item_tag_holder_mine);
        }
        return holder.mainView;
    }

    private View getSystem(View convertView) {
        SystemHolder holder;

        if (convertView == null || convertView.getTag(R.id.otc_chat_type_system) != TYPE_SYSTEM) {
            holder = new SystemHolder();
            holder.mainView.setTag(R.id.otc_chat_type_system, TYPE_SYSTEM);
            holder.mainView.setTag(R.id.otc_chat_holder_system, holder);
        } else {
            holder = (SystemHolder) convertView.getTag(R.id.otc_chat_holder_system);
        }
        holder.tvTime.setText(time);
        holder.tvStatus.setText(Utils.getOrderType(context, Integer.parseInt(status)));
        return holder.mainView;
    }

    private View getOtherView(View convertView, OTCIMMessageBean entity) {
        if (entity == null)
            return convertView;
        OtherViewHolder holder;
        if (convertView == null || convertView.getTag(R.id.otc_chat_item_tag_type_other) != TYPE_MINE) {
            holder = new OtherViewHolder();
            holder.mainView.setTag(R.id.otc_chat_item_tag_type_other, TYPE_MINE);
            holder.mainView.setTag(R.id.otc_chat_item_tag_holder_oter, holder);
        } else {
            holder = (OtherViewHolder) convertView.getTag(R.id.otc_chat_item_tag_holder_oter);
        }

        try {
            holder.imOtherhead.setText(entity.getFromName().substring(0, 1));
        } catch (Exception e) {
            e.printStackTrace();
            holder.imOtherhead.setText("");
        }


        holder.mViewContextText.setText(entity.getContent());
        try {
            holder.tvTime.setText(DateUtil.INSTANCE.longToString("HH:mm:ss", Long.parseLong(entity.getCtime())));
        } catch (Exception e) {
            e.printStackTrace();
            holder.tvTime.setText(entity.getCtime());
        }
        return holder.mainView;
    }


    class OtherViewHolder {
        View mainView;
        TextView tvTime;
        TextView mViewContextText;
        TextView imOtherhead;


        OtherViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_chat_other, null);
            tvTime = mainView.findViewById(R.id.item_otc_chat_other_time);
            mViewContextText = mainView.findViewById(R.id.item_otc_chat_other_content);
            imOtherhead = mainView.findViewById(R.id.item_otc_chat_other_url);
        }

    }

    class MineViewHolder {
        View mainView;
        TextView tvContent;
        TextView imhead;
        TextView tvTime;

        MineViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_chat_mine, null);
            tvTime = mainView.findViewById(R.id.item_otc_chat_mine_time);
            tvContent = mainView.findViewById(R.id.item_otc_chat_mine_content);
            imhead = mainView.findViewById(R.id.item_otc_chat_mine_url);
        }
    }

    class EmViewHolder {
        View mainView;

        EmViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_null_layout, null);
        }
    }

    class SystemHolder {
        View mainView;
        TextView tvTime;
        TextView tvStatus;

        SystemHolder() {
            mainView = View.inflate(context, R.layout.item_otc_system_layout, null);
            tvTime = mainView.findViewById(R.id.tv_time);
            tvStatus = mainView.findViewById(R.id.tv_status);
        }
    }

    @Override
    public int getItemViewType(int position) {
        OTCIMMessageBean message = (OTCIMMessageBean) getItem(position);
        if (message != null && message.getFromId() != null && message.getFromId().equals(String.valueOf(userId))) {
            return TYPE_MINE;
        } else if (TextUtils.isEmpty(message.getContent())) {
            return TYPE_EMPTY;
        } else {
            return TYPE_OTHER;
        }
    }


}
