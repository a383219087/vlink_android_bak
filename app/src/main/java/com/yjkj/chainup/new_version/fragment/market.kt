package com.yjkj.chainup.new_version.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.chainup.contract.utils.CpDisplayUtils
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.util.ColorUtil
import org.jetbrains.anko.textColor

class MarketLayout @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var mTitles: Array<String>? = null
    var mCurrentTab = 0
    var onTabSelectListener: OnTabSelectListener? = null
    fun setTabData(titles: Array<String>) {
        mTitles = titles
        this.orientation = HORIZONTAL
        for (index in titles) {
            val tabView = View.inflate(context, R.layout.item_tab_market, null)
            tabView.tag = titles.indexOf(index)
            val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            params.marginStart = 16
            addView(tabView, params)
            var item: SuperTextView = tabView.findViewById(R.id.btn_item)
            if (titles.indexOf(index) == mCurrentTab) {
                action4Selected(item, true)
            } else {
                action4Selected(item, false)
            }
            item.text = index
            tabView.setOnClickListener {
                val position = it.tag as Int
                if (mCurrentTab != position) {
                    action4Selected(tabView.findViewById(R.id.btn_item), true)
                    action4Selected(this.getChildAt(mCurrentTab).findViewById(R.id.btn_item), false)
                    mCurrentTab = position
                    onTabSelectListener?.onTabSelect(position)
                } else {
                    onTabSelectListener?.onTabReselect(position)
                }
            }
        }
    }

    fun currentTab(position: Int) {
        if (position != mCurrentTab) {
            action4Selected(this.getChildAt(position).findViewById(R.id.btn_item), true)
            action4Selected(this.getChildAt(mCurrentTab).findViewById(R.id.btn_item), false)
            mCurrentTab = position
        }
    }

    private fun action4Selected(view: SuperTextView?, isSelected: Boolean = true) {
        view?.run {
            solid = ColorUtil.getColor(if (isSelected) R.color.transparent else R.color.transparent)
            //strokeColor = ColorUtil.getColor(if (isSelected) R.color.trade_main_blue else R.color.transparent)
            strokeColor = ColorUtil.getColor(if (isSelected) R.color.transparent else R.color.transparent)
            textColor = ColorUtil.getColor(if (isSelected) R.color.main_blue else R.color.normal_text_color)
        }
    }


}

interface OnTabSelectListener {
    fun onTabSelect(position: Int)
    fun onTabReselect(position: Int)
}