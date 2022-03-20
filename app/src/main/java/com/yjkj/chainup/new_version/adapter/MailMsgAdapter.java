package com.yjkj.chainup.new_version.adapter;

import androidx.annotation.Nullable;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fengniao.news.util.DateUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.bean.Message;
import com.yjkj.chainup.bean.dev.MessageBean;
import com.yjkj.chainup.manager.LanguageUtil;

import java.util.List;

/**
 * @author Bertking
 * @date 2018/5/24
 * @description 站内信的Item Adapter
 */
public class MailMsgAdapter extends BaseQuickAdapter<MessageBean.UserMessage, BaseViewHolder> {
    public static final String TAG = MailMsgAdapter.class.getSimpleName();

    private List<MessageBean.Type> typeList;

    public MailMsgAdapter(@Nullable List<MessageBean.UserMessage> data, List<MessageBean.Type> typeList) {
        super(R.layout.item_msg_mail, data);
        this.typeList = typeList;
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageBean.UserMessage item) {
        Log.d(TAG, "===item===" + item.toString());

        /**
         * 0-未读，1-已读
         */
        helper.getView(R.id.iv_msg_status).setBackgroundResource(item.getStatus() == 0 ? R.drawable.ic_reddot : R.drawable.ic_dot);

        for (MessageBean.Type type : typeList) {
            if (type.getTid() == item.getMessageType()) {
                helper.setText(R.id.tv_type_title, type.getTitle());
            }
        }
        helper.setText(R.id.tv_ctime, DateUtil.INSTANCE.longToString("yyyy-MM-dd HH:mm:ss", item.getCtime()));
        helper.setText(R.id.tv_content, item.getMessageContent());

    }
}
