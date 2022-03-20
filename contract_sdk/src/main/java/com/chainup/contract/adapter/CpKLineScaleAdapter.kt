package com.chainup.contract.adapter

import android.util.Log
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpKLineUtil
import com.chainup.contract.view.CpCustomCheckBoxView

/**
 * @Author: Bertking
 * @Date：2019/3/20-7:25 PM
 * @Description: 竖版的KLine刻度
 */
class CpKLineScaleAdapter(scales: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.cp_item_kline_scale, scales) {
    val TAG = CpKLineScaleAdapter::class.java.simpleName
    override fun convert(helper: BaseViewHolder, item: String) {
        Log.d(TAG, "======&&&&==+item:$item")

        val boxView = helper?.getView<CpCustomCheckBoxView>(R.id.cbtn_view)
        if(helper?.adapterPosition == 0){
            boxView?.setMiddle("line")
        }else{
            boxView?.setMiddle(item!!)
        }
        boxView?.setCenterColor(CpColorUtil.getColor(R.color.normal_text_color))
        boxView?.setCenterSize(12f)
        boxView?.setIsNeedDraw(false)
        boxView?.isChecked = false
        if (CpKLineUtil.getCurTime4Index() == helper?.adapterPosition) {
            boxView?.isChecked = true
            boxView?.setIsNeedDraw(true)
            boxView?.setCenterColor(CpColorUtil.getColor(R.color.text_color))
        } else {
            boxView?.isChecked = false
            boxView?.setCenterColor(CpColorUtil.getColor(R.color.normal_text_color))
            boxView?.setIsNeedDraw(false)
        }
    }
}