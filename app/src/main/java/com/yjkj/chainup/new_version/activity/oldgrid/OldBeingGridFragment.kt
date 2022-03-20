package com.yjkj.chainup.new_version.activity.oldgrid

import android.os.Bundle
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.util.ColorUtil
import kotlinx.android.synthetic.main.fragment_being_performed.*
import kotlinx.android.synthetic.main.item_grid_buy.view.*
import kotlinx.android.synthetic.main.item_grid_sell.view.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2021/2/3-6:13 PM
 * @Email buptjinlong@163.com
 * @description
 */
class OldBeingGridFragment : NBaseFragment() {


    companion object {
        @JvmStatic
        fun newInstance(strategyId: String, coin: String) =
                OldBeingGridFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.GIRD_ID, strategyId)
                        putString(ParamConstant.GIRD_COIN, coin)
                    }
                }
    }

    var strategyId = ""
    override fun initView() {
        arguments?.let {
            strategyId = it.getString(ParamConstant.GIRD_ID, "")
        }

        getOrderingGridList(strategyId)
    }

    private var riseColor = ColorUtil.getMainColorType(isRise = true)
    private var fallColor = ColorUtil.getMainColorType(isRise = false)

    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()

    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    var sellList: JSONArray = JSONArray()
    var buyList: JSONArray = JSONArray()

    override fun setContentView() = R.layout.fragment_being_performed


    fun getOrderingGridList(strategyId: String) {
        addDisposable(getMainModel().getOrderingGridList(strategyId, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                sellList = json.optJSONArray("SELL")
                buyList = json.optJSONArray("BUY")
                var count = sellList.length() + buyList.length()
                var acy = activity
                if (acy != null && acy is OldGridExecutionDetailsActivity) {
                    acy.updateTitleOrder(count)
                }
                initDepthView()
            }
        }))
    }

    /**
     * 买卖盘
     * 初始化交易详情记录view
     */
    private fun initDepthView() {
        if ((sellList == null || sellList.length() == 0) && (buyList == null || buyList.length() == 0)) {
            item_new_empty?.visibility = View.VISIBLE
            ll_depth?.visibility = View.GONE
        } else {
            item_new_empty?.visibility = View.GONE
            ll_depth?.visibility = View.VISIBLE
        }


        for (i in 0 until sellList.length()) {
            /**
             * 卖盘
             */
            val view: View = layoutInflater.inflate(R.layout.item_grid_sell, null)
            view.tv_price_item_for_depth_sell?.setTextColor(fallColor)
            view.tv_price_item_for_depth_sell?.text = sellList.optString(i)
            view.tv_quantity_item_for_depth_sell?.text = "${i + 1}"
            ll_sell?.addView(view)
            sellViewList.add(view)
            /***********/
        }

        for (i in 0 until buyList.length()) {
            /**
             * 买盘
             */
            val view1: View = layoutInflater.inflate(R.layout.item_grid_buy, null)
            view1.tv_price_item_for_depth_buy?.setTextColor(riseColor)
            view1.tv_price_item_for_depth_buy?.text = buyList.optString(i)
            view1.tv_quantity_item_for_depth_buy?.text = "${i + 1}"
            ll_buy?.addView(view1)
            buyViewList.add(view1)
        }

    }

}