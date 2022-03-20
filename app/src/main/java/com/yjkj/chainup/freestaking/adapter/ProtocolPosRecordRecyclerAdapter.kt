package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.freestaking.bean.MyPosRecordBean
import com.yjkj.chainup.freestaking.formatAmount
import com.yjkj.chainup.manager.NCoinManager

class ProtocolPosRecordRecyclerAdapter(data: ArrayList<MyPosRecordBean.PosListBean>)
    :BaseQuickAdapter<MyPosRecordBean.PosListBean, BaseViewHolder>(R.layout.item_protocolpos_record,data) {
    override fun convert(helper: BaseViewHolder, item: MyPosRecordBean.PosListBean) {
        helper?.setText(R.id.tv_name, item?.baseCoin)
        when(item?.projectStatus){
            "0"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_start))

            }
            "1"->{
                helper?.setText(R.id.tv_status,context.getString(R.string.pos_state_buying))

            }
            "2"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_waitInterest))
            }
            "3"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_InterestIng))
            }
            "4"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_InterestEnd))
            }
            "5"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_release))

            }
            "6"->{
                helper?.setText(R.id.tv_status, context.getString(R.string.pos_state_fulled))

            }
        }
        helper?.setText(R.id.tv_stime, item?.ltime)
        helper?.setText(R.id.tv_number, item?.totalAmount?.formatAmount(NCoinManager.getCoinShowPrecision(item?.baseCoin.toString()))?.toPlainString())
        helper?.setText(R.id.tv_income, item?.gainRate+"%")
        helper?.setText(R.id.tv_current_income, item?.totalUserGainAmount?.toPlainString())
//        helper?.addOnClickListener(R.id.tv_current_income)
    }
}