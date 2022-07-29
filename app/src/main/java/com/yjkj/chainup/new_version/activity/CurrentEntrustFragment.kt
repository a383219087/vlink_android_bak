package com.yjkj.chainup.new_version.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NCurrentEntrustAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.HistoryScreeningControl
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_contract_current_entrust.*
import kotlinx.android.synthetic.main.activity_current_entrust.*
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/4-10:26 AM
 * @Description: 所有当前委托(币币)
 */

class CurrentEntrustFragment : NBaseFragment(), HistoryScreeningListener {


    var isShowCanceled = "0"
    var side = ""
    var type = ""
    var startTime = ""
    var endTime = ""
    val pageSize = 100
    var page = 1
    var isScrollStatus = true

    override fun ConfirmationScreen(status: Boolean, statusType: Int, symbolCoin: String, symbolAndUnit: String, tradingType: Int, priceType: Int, begin: String, end: String) {
        var activity = activity

        if (activity != null && !activity.isFinishing) {
            if (activity is EntrustActivity) {

                if (!TextUtils.isEmpty(symbolCoin) || !TextUtils.isEmpty(symbolAndUnit)) {
                    ll_tip_layout?.visibility = View.GONE
                } else {
                    ll_tip_layout?.visibility = View.VISIBLE
                }
                var trading = ""
                page = 1
                isScrollStatus = true
                when (tradingType) {
                    1 -> {
                        trading = "BUY"
                    }
                    2 -> {
                        trading = "SELL"

                    }
                }

                isShowCanceled = if (status) "0" else "1"
                side = trading
                if (symbolCoin.isNotEmpty()){
                    symbol = NCoinManager.setShowNameGetName(symbolCoin) + NCoinManager.setShowNameGetName(symbolAndUnit)
                }
                type = if (priceType == 0) {
                    ""
                } else {
                    priceType.toString()
                }
                startTime = begin
                endTime = end
                if (activity.currentItem == 0) {
//                    if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
//                        getNewHistoryEntrust(true)
//                    } else {
//                        getCurrentEntrust()
//                    }
                    getCurrentEntrust()
                }
            }
        }
    }

    fun initData() {
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            ll_tip_layout?.visibility = View.VISIBLE
            getNewHistoryEntrust(true)
        } else {
            ll_tip_layout?.visibility = View.GONE
            symbol = if (orderType == ParamConstant.LEVER_INDEX) {
                PublicInfoDataService.getInstance().currentSymbol4Lever
            } else {
                PublicInfoDataService.getInstance().currentSymbol
            }
            getCurrentEntrust()
        }
    }


    var list = ArrayList<JSONObject>()
    var curEntrustAdapter = NCurrentEntrustAdapter(list)

    var symbol = ""

    companion object {
        @JvmStatic
        fun newInstance(orderType: String) =
                CurrentEntrustFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.TYPE, orderType)
                    }
                }
    }

    override fun loadData() {
        arguments.let {
            orderType = it?.getString(ParamConstant.TYPE, ParamConstant.BIBI_INDEX)
                    ?: ParamConstant.BIBI_INDEX
        }
    }

    var orderType = ParamConstant.BIBI_INDEX

    override fun setContentView() = R.layout.activity_current_entrust


    override fun initView() {
        HistoryScreeningControl.getInstance().addListener(this)
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            ll_tip_layout?.visibility = View.VISIBLE
        } else {
            ll_tip_layout?.visibility = View.GONE
        }
        tv_tip_title?.text = LanguageUtil.getString(mActivity, "common_text_tip")
        tv_tip_content?.text = LanguageUtil.getString(mActivity, "common_text_entrustListLimit")
        tv_title?.text = LanguageUtil.getString(context, "contract_text_currentEntrust")
        tv_sub_title?.text = LanguageUtil.getString(context, "contract_text_currentEntrust")
        tv_history_order?.text = LanguageUtil.getString(context, "contract_text_historyCommision")

        rv_all_entrust?.layoutManager = LinearLayoutManager(mActivity)
        rv_all_entrust?.adapter = curEntrustAdapter
        curEntrustAdapter.addChildClickViewIds(R.id.tv_status)
        curEntrustAdapter.setOnItemChildClickListener { adapter, _, position ->
            if (adapter.data.isNotEmpty()) {
                try {
                    (adapter.data[position] as JSONObject?)?.run {
                        var source = optString("source")
                        if (source == "QUANT-GRID") {
                            NewDialogUtils.showNewsingleDialog2(context!!, getString(R.string.stop_grid_operate_again), object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {

                                }
                            }, cancelTitle = LanguageUtil.getString(context, "alert_common_i_understand"))
                        }else{
                            val status = this.optString("status")
                            val id = this.optString("id")
                            val baseCoin = optString("baseCoin").toLowerCase()
                            val countCoin = optString("countCoin").toLowerCase()

                            when (status) {
                                "0", "1", "3" -> {
                                    deleteOrder(id, baseCoin + countCoin, position)
                                }
                            }
                        }



                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        initData()
    }

    /**
     * 获取当前委托
     */
    private fun getCurrentEntrust() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }

//        val symbol = if (orderType == ParamConstant.LEVER_INDEX) {
//            PublicInfoDataService.getInstance().currentSymbol4Lever
//        } else {
//            PublicInfoDataService.getInstance().currentSymbol
//        }

        addDisposable(getMainModel().getNewEntrust(symbol = symbol,type = type,side = side, isLever = (orderType == ParamConstant.LEVER_INDEX), isOnly20 = false, consumer = object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                try {
                    val activity = activity
                    if (activity != null && !activity.isFinishing) {
                        if (activity is EntrustActivity) {
                            if (activity.currentItem == 0) {
                                list.clear()
                                closeLoadingDialog()
                                jsonObject.optJSONObject("data")?.run {
                                    optJSONArray("orderList")?.run {
                                        if (length() != 0) {
                                            for (i in 0 until length()) {
                                                list.add(this.optJSONObject(i))
                                            }
                                            curEntrustAdapter.setList(list)
                                        } else {
                                            curEntrustAdapter.setList(list)
                                            curEntrustAdapter.setEmptyView(R.layout.item_new_empty)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }))

    }


    /**
     * 新版本委托接口
     * @param entrust 1:当前委托、2：历史委托
     * @param side  BUY：买、SELL：卖fFfFUuPp
     * @param symbol 币对
     * @param orderType 订单类型1:常规订单，2 杠杆订单
     * @param status  订单状态：1 新订单，2 已完成，3 部分成交，4 已取消，5 待撤销，6 异常单
     * @param isShowCanceled 0:不展示已撤单、其余默认展示已撤单
     * @param quote 所在交易区（usdt。。。）
     * @param page 分页
     * @param pageSize 页面大小
     */
    private fun getNewHistoryEntrust(refresh: Boolean) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }

        addDisposable(getMainModel().getNewCurrentEntrustSearch(side, symbol, isShowCanceled, type, page.toString(), pageSize.toString(), (orderType == ParamConstant.LEVER_INDEX), ParamConstant.CURRENT_ENTURST, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                try {
                    var activity = activity

                    if (activity != null && !activity.isFinishing) {
                        if (activity is EntrustActivity) {
                            if (activity.currentItem == 0) {
                                list.clear()
                                closeLoadingDialog()
                                var entrustActivity = activity
                                jsonObject.optJSONObject("data")?.run {
                                    optJSONArray("orders")?.run {
                                        if (length() != 0) {
                                            for (i in 0 until length()) {
                                                list.add(this.optJSONObject(i))
                                            }
                                            curEntrustAdapter.setList(list)
                                        } else {
                                            curEntrustAdapter.setList(list)
                                            curEntrustAdapter.setEmptyView(R.layout.item_new_empty)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }))

    }


    /**
     * 撤销订单
     */
    private fun deleteOrder(order_id: String, symbol: String, pos: Int) {
        addDisposable(getMainModel().cancelOrder(order_id = order_id, symbol = symbol, isLever = (orderType == ParamConstant.LEVER_INDEX), consumer = object : NDisposableObserver(mActivity) {
            override fun onResponseSuccess(data: JSONObject) {
                if (pos in (0 until list.size)) {
                    curEntrustAdapter?.remove(pos)
                    curEntrustAdapter?.notifyItemRemoved(pos)
                    NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(context, "common_tip_cancelSuccess"))
                }
            }
        }))
    }
}
