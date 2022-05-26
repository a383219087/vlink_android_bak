package com.yjkj.chainup.new_version.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yjkj.chainup.util.DateUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.db.service.UserDataService;
import com.yjkj.chainup.new_version.bean.OTCIMDetailsProblemBean;
import com.yjkj.chainup.util.GlideUtils;
import com.yjkj.chainup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lianshangljl
 * @Date 2018/10/23-上午11:27
 * @Email buptjinlong@163.com
 * @description
 */
public class OTCIMDetailsProblemBeanAdapter extends BaseAdapter {
    private Context context;
    private List<OTCIMDetailsProblemBean.RqReplyList> mList = new ArrayList<>();
    private int userId = 0;
    private String status = "0", time = "";

    public OTCIMDetailsProblemBeanAdapter(Context context, List<OTCIMDetailsProblemBean.RqReplyList> mList, String status, String time) {
        this.context = context;
        this.mList = mList;
        if (UserDataService.getInstance().getUserData() != null) {
            userId = Integer.parseInt(UserDataService.getInstance().getUserInfo4UserId());
        }
        this.status = status;
        this.time = time;
    }

    public void setmList(List<OTCIMDetailsProblemBean.RqReplyList> mList) {
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
        OTCIMDetailsProblemBean.RqReplyList message = (OTCIMDetailsProblemBean.RqReplyList) getItem(position);

        int type = getItemViewType(position);
        return getItemView(convertView, message, type);
    }

    private View getItemView(View convertView, OTCIMDetailsProblemBean.RqReplyList entity, int type) {
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

    private View getMineView(View convertView, OTCIMDetailsProblemBean.RqReplyList entity) {
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
        if ("1".equals(entity.getContentType())) {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.ivMineLoading.setVisibility(View.GONE);
            holder.tvContent.setText(entity.getReplayContent());
        } else {
            holder.tvContent.setVisibility(View.GONE);
            holder.ivMineLoading.setVisibility(View.VISIBLE);
            GlideUtils.loadImage(context, entity.getReplayContent(), holder.ivMineLoading);
        }
        holder.tvTime.setText(DateUtil.INSTANCE.longToString("HH:mm:ss", entity.getCtime()));
        return holder.mainView;
    }

    private View getOtherView(View convertView, OTCIMDetailsProblemBean.RqReplyList entity) {
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
        if ("1".equals(entity.getContentType())) {
            holder.mViewContextText.setVisibility(View.VISIBLE);
            holder.ivOtherLoading.setVisibility(View.GONE);
            holder.mViewContextText.setText(entity.getReplayContent());
        } else {
            holder.mViewContextText.setVisibility(View.GONE);
            holder.ivOtherLoading.setVisibility(View.VISIBLE);
            GlideUtils.loadImage(context, entity.getReplayContent(), holder.ivOtherLoading);
        }
        holder.tvTime.setText(DateUtil.INSTANCE.longToString("HH:mm:ss", entity.getCtime()));
        return holder.mainView;
    }


    private View getEmpty(View convertView, OTCIMDetailsProblemBean.RqReplyList entity) {
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

    class EmViewHolder {
        View mainView;

        EmViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_null_layout, null);
        }
    }

    class OtherViewHolder {
        View mainView;
        TextView tvTime;
        TextView mViewContextText;
        TextView imOtherhead;
        ImageView ivOtherLoading;


        OtherViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_chat_other, null);
            tvTime = mainView.findViewById(R.id.item_otc_chat_other_time);
            mViewContextText = mainView.findViewById(R.id.item_otc_chat_other_content);
            imOtherhead = mainView.findViewById(R.id.item_otc_chat_other_url);
            ivOtherLoading = mainView.findViewById(R.id.item_otc_chat_other_image);
        }

    }

    class MineViewHolder {
        View mainView;
        TextView tvContent;
        TextView imhead;
        TextView tvTime;
        ImageView ivMineLoading;

        MineViewHolder() {
            mainView = View.inflate(context, R.layout.item_otc_chat_mine, null);
            tvTime = mainView.findViewById(R.id.item_otc_chat_mine_time);
            tvContent = mainView.findViewById(R.id.item_otc_chat_mine_content);
            imhead = mainView.findViewById(R.id.item_otc_chat_mine_url);
            ivMineLoading = mainView.findViewById(R.id.item_otc_chat_mine_image);
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
        OTCIMDetailsProblemBean.RqReplyList message = (OTCIMDetailsProblemBean.RqReplyList) getItem(position);
        if ("2".equals(message.getUserType())) {
            return TYPE_MINE;
        } else if (TextUtils.isEmpty(message.getReplayContent())) {
            return TYPE_EMPTY;
        } else {
            return TYPE_OTHER;
        }
    }
}
