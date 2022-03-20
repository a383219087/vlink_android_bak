package com.yjkj.chainup.new_contract.fragment

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.common.sdk.impl.IResponse
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.*
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.listener.SLDoListener
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.contract.widget.MyLinearLayoutManager
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.contract.widget.bubble.BubbleSeekBar
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.activity.ClAdjustMarginActivity
import com.yjkj.chainup.new_contract.activity.ClContractStopRateLossActivity
import com.yjkj.chainup.new_contract.activity.ClHoldShareActivity
import com.yjkj.chainup.new_contract.adapter.ClHoldContractAdapter
import com.yjkj.chainup.new_contract.bean.ClContractPositionBean
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.dialog.NewDialogUtils.DialogBottomListener
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cl_activity_select_leverage.*
import kotlinx.android.synthetic.main.fragment_sl_contract_hold.*
import kotlinx.android.synthetic.main.sl_item_close_position_dialog.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.EOFException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 合约持仓
 */
class ClContractHoldFragment : NBaseFragment() {
    private val sTAG = "SlContractHoldFragment"
    private var adapter: ClHoldContractAdapter? = null
    private var mList = ArrayList<ClContractPositionBean>()
    private var marginTDialog: TDialog? = null
    private var closePositionTDialog: TDialog? = null
    private var contract: Contract? = null
    private var coinCode: String = ""
    private var mContractId = 0
    private var openContract = 0//是否开通了合约交易 1已开通, 0未开通
    var coUnit = 0

    //是否是币种持仓
    private var isCoinHold = false

    /**
     * 刷新监听
     */
    var refreshListener: SLDoListener? = null

    override fun setContentView(): Int {
        return R.layout.fragment_cl_contract_hold
    }

    override fun initView() {
        adapter = ClHoldContractAdapter(mList)
        rv_hold_contract.layoutManager = MyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(EmptyForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(R.id.tv_settled_profit_loss_key,R.id.tv_adjust_margins, R.id.tv_adjust_margins, R.id.tv_close_position, R.id.iv_share, R.id.tv_stop_profit_loss, R.id.tv_forced_close_price, R.id.tv_tag_price)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            try {
                when (view.id) {
                    R.id.tv_adjust_margins -> {
                        mActivity?.let { ClAdjustMarginActivity.show(it, mList[position]) }
                    }
                    R.id.tv_close_position -> {
                        showClosePositionDialog(mList[position])
                    }
                    R.id.iv_share -> {
                        mActivity?.let { ClHoldShareActivity.show(it, mList[position]) }
                    }
                    R.id.tv_stop_profit_loss -> {
                        mActivity?.let { ClContractStopRateLossActivity.show(it, mList[position]) }
                    }
                    R.id.tv_forced_close_price -> {
                        NewDialogUtils.showDialog(context!!, getLineText("cl_contract_add_text24", true), true, null, getLineText("cl_calculator_text11"), getLineText("alert_common_i_understand"))
                    }
                    R.id.tv_tag_price -> {
                        NewDialogUtils.showDialogNew(context!!, getLineText("cl_contract_add_text23", true), true, null, getLineText("cl_margin_rate_str"), getLineText("alert_common_i_understand"))
                    }
                    R.id.tv_settled_profit_loss_key -> {
                        val obj: JSONObject= JSONObject()
                        obj.put("profitRealizedAmount", mList[position].profitRealizedAmount)
                        obj.put("tradeFee", mList[position].tradeFee)
                        obj.put("capitalFee", mList[position].capitalFee)
                        obj.put("closeProfit", mList[position].closeProfit)
                        obj.put("shareAmount", mList[position].shareAmount)
                        obj.put("marginCoin", LogicContractSetting.getContractMarginCoinById(context, mList[position].contractId))
                        obj.put("marginCoinPrecision",LogicContractSetting.getContractMarginCoinPrecisionById(context, mList[position].contractId))
                        SlDialogHelper.showProfitLossDetailsDialog(context!!, obj)
                    }
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "点击持仓报错" + e.message)
            }
        }

        rv_hold_contract.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (rv_hold_contract.canScrollVertically(-1)) {
                    refreshListener?.doThing(false)
                } else {
                    refreshListener?.doThing(true)
                }
            }
        })
    }


    private fun getPositionList() {
        coUnit = LogicContractSetting.getContractUint(context)
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(getContractModel().getPositionAssetsList(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClContractPositionBean>()
                        jsonObject.optJSONObject("data")?.run {
                            if (!isNull("positionList")) {
                                val mOrderListJson = optJSONArray("positionList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    mListBuffer.add(Gson().fromJson<ClContractPositionBean>(obj, ClContractPositionBean::class.java))
                                }
                                val msgEvent = MessageEvent(MessageEvent.sl_contract_position_num_event)
                                msgEvent.msg_content = mOrderListJson.length()
                                EventBusUtil.post(msgEvent)
                            }
                            adapter?.setList(mListBuffer)
                        }
                    }
                }))
    }

    override fun onDestroy() {
        super.onDestroy()

        if (marginTDialog != null) {
            marginTDialog?.dismiss()
            marginTDialog = null
        }

        if (closePositionTDialog != null) {
            closePositionTDialog?.dismiss()
            closePositionTDialog = null
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadDataFromNet()
        }
    }

    private fun loadDataFromNet() {
        if (isHidden || !isAdded || !UserDataService.getInstance().isLogined) {
            mList.clear()
            adapter?.notifyDataSetChanged()
            return
        }
        val coin = if (isCoinHold) coinCode else contract?.margin_coin ?: return
        ContractUserDataAgent.loadContractPosition(coin, 1, 0, 0, null)
    }


    /**
     * 弹出平仓对话框
     */
    private fun showClosePositionDialog(info: ClContractPositionBean) {
        coUnit = LogicContractSetting.getContractUint(context)
        var mContractJson = LogicContractSetting.getContractJsonStrById(mActivity!!, info.contractId)
        closePositionTDialog = SlDialogHelper.showClosePositionDialog(mActivity!!, OnBindViewListener {

            val tvBaseSymbol = it.getView<TextView>(R.id.tv_base_symbol)
            val etPrice = it.getView<EditText>(R.id.et_price)
            val etVolume = it.getView<EditText>(R.id.et_volume)
            val btnClosePositionMarket = it.getView<TextView>(R.id.btn_close_position_market)
            val btnClosePosition = it.getView<CommonlyUsedButton>(R.id.btn_close_position)
//
            it.getView<TextView>(R.id.tv_title).onLineText("cl_tradeform_text21")
            it.getView<TextView>(R.id.tv_cancel).onLineText("common_text_btnCancel")
            it.getView<TextView>(R.id.tv_price_label).onLineText("contract_text_price")
            etPrice.hint = getLineText("contract_text_price")
            it.getView<TextView>(R.id.tv_position_label).onLineText("cl_coflowingwater_amount")
            etVolume.hint = getLineText("cl_coflowingwater_amount")
            btnClosePositionMarket.onLineText("cl_positionlis_text3")
            btnClosePosition.textContent = getLineText("cl_positionlis_text2")
            //面值精度
            var multiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(context, mContractJson.optInt("id"))
            //面值
            var multiplier = mContractJson.optString("multiplier")
            //滑块
            val seekBar = it.getView<BubbleSeekBar>(R.id.seek_layout)
            //价格单位
            it.getView<TextView>(R.id.tv_price_unit).text = mContractJson.optString("quote")
            //设置价格精度
            etPrice.numberFilter(mContractJson.optJSONObject("coinResultVo").optInt("symbolPricePrecision"))
            //仓位输入的精度
            val etVolumePrecision = if (coUnit == 0) 0 else multiplierPrecision
            etVolume.numberFilter(etVolumePrecision)
            //价格 默认显示标记价格
            etPrice.setText(BigDecimalUtils.showSNormal(info.indexPrice, mContractJson.getJSONObject("coinResultVo").optInt("symbolPricePrecision")))
            //数量 默认展示最大可平数
//            var allQty = MathHelper.round(info.canCloseVolume, 0)
            var allQty = info.canCloseVolume
            if (coUnit == 0) {
                it.getView<TextView>(R.id.tv_base_symbol).onLineText("sl_str_contracts_unit")
                etVolume.setText("${allQty.toInt()}")
            } else {
                it.getView<TextView>(R.id.tv_base_symbol).text = mContractJson.optString("multiplierCoin")
                allQty = BigDecimalUtils.mulStr(allQty, mContractJson.optString("multiplier"), etVolumePrecision)
                etVolume.setText("${allQty}")
            }
            seekBar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
                override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                    var vol = BigDecimalUtils.mulStr(allQty, BigDecimalUtils.divStr(progress.toString(), "100", 2), etVolumePrecision)
                    if (coUnit == 0) {
                        etVolume.setText(vol)
                    } else {
                        if (BigDecimalUtils.compareTo(vol, allQty) == 1) {
                            vol = allQty
                        }
                        etVolume.setText(vol)
                        etVolume.setSelection(vol.length)
                    }

                }

                override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                }

                override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                }
            }
            etVolume.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    var vol = etVolume.text.toString()
                    if (TextUtils.isEmpty(vol)) {
                        seekBar.setProgress(0.0f)
                    } else {
                        if (BigDecimalUtils.compareTo(vol, allQty) == 1) {
                            if (coUnit == 0) {
                                vol = allQty.toInt().toString()
                            }
                            etVolume.setText(vol)
                            etVolume.setSelection(vol.length)
                        } else {
                            seekBar.setProgress(min((vol.toFloat() / allQty.toFloat() * 100), 100.0f))
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
            //平仓
            btnClosePosition.isEnable(true)
            btnClosePosition.listener = object : CommonlyUsedButton.OnBottonListener {
                override fun bottonOnClick() {
                    val vol = etVolume.text.toString()
                    val price = etPrice.text.toString()
                    LogUtil.e(TAG, preVerifyClosePositionInputV2(vol, price).toString())
                    if (!preVerifyClosePositionInputV2(vol, price)) {
                        return
                    }
                    val unit = if (coUnit == 0) {
                        getLineText("sl_str_contracts_unit")
                    } else {
                        mContractJson.optString("base")
                    }
                    if (preVerifyClosePositionInputV2(vol, price)) {
                        closePositionTDialog?.dismiss()
                        closePositionTDialog = null
                        //弹出确认对话框 限价7300 USDT买入100 张 BTCUSDT合约
                        val type = if (info.orderSide.equals("BUY")) getLineText("sl_str_sell_close") else getLineText("sl_str_buy_close")
                        val tips = getLineText("sl_str_limit_price_tips")
//                        val content = "$tips<font color=\"#FF9E12\"> $price </font> ${mContractJson.optString("quote")}$type<font color=\"#FF9E12\">$vol</font> " + unit + " "+" ${mContractJson.optString("symbol") +" "+ getString(R.string.cl_stoporder_columns1)}"
                        val content = tips + " " + price + mContractJson.optString("quote") + " " + type + " " + vol + unit + " " + LogicContractSetting.getContractShowNameById(context, mContractJson.optInt("id"))
                        NewDialogUtils.showNormalDialog(mActivity!!, content, object : DialogBottomListener {
                            override fun sendConfirm() {
                                var contractId = mContractJson.optInt("id")
                                var positionType = info.positionType.toString()
                                var open = "CLOSE"
                                var side = if (info.orderSide.equals("BUY")) "SELL" else "BUY"
                                var type = 1
                                var leverageLevel = info.leverageLevel
                                var price = price
                                var volume = BigDecimalUtils.getOrderLossNum(vol, multiplier)
                                var isConditionOrder = false
                                var triggerPrice = ""

                                var expireTime = LogicContractSetting.getStrategyEffectTimeStr(mActivity)
                                addDisposable(getContractModel().createOrder(contractId, positionType, open, side, type, leverageLevel, price, volume, isConditionOrder, triggerPrice, expireTime,
                                        consumer = object : NDisposableObserver(mActivity, true) {
                                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                                Observable.timer(500, TimeUnit.MILLISECONDS)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                                            getPositionList()
                                                        }
                                            }
                                        }))
                            }
                        }, getLineText("contract_text_limitPositions"))

                    }
                }
            }
//
//            //市价全平
            btnClosePositionMarket.setOnClickListener {
                val vol = etVolume.text.toString()
                val price = etPrice.text.toString()
                LogUtil.e(TAG, vol + "||" + price + preVerifyClosePositionInputV2(vol, price).toString())
                if (!preVerifyClosePositionInputV2(vol, price)) {
                    return@setOnClickListener
                }
                val unit = if (coUnit == 0) {
                    getLineText("sl_str_contracts_unit")
                } else {
                    mContractJson.optString("base")
                }
                closePositionTDialog?.dismiss()
                closePositionTDialog = null
                //弹出确认对话框   市价买入4000张 BTCUSDT合约
                val type = if (info.orderSide.equals("BUY")) getLineText("sl_str_sell_close") else getLineText("sl_str_buy_close")
                val tips = getLineText("contract_action_marketPrice")

//                val amountCanbeLiquidated: Double = MathHelper.sub(info.cur_qty, info.freeze_qty)
//                val dfVol: DecimalFormat = NumberUtil.getDecimal(contract.vol_index)
//                val content = "$tips$type<font color=\"#FF9E12\"> ${vol} </font>  ${ContractUtils.getHoldVolUnit(contract)} ${contract.getDisplayName(mActivity)}"
//                val content = "$tips<font color=\"#FF9E12\">$type<font color=\"#FF9E12\">$vol</font> " + unit + " ${mContractJson.optString("symbol") + getString(R.string.cl_stoporder_columns1)}"
                val content = tips+" "+type+vol+unit+" "+LogicContractSetting.getContractShowNameById(context, mContractJson.optInt("id"))
                NewDialogUtils.showNormalDialog(mActivity!!, content, object : DialogBottomListener {
                    override fun sendConfirm() {
                        var contractId = mContractJson.optInt("id")
                        var positionType = info.positionType.toString()
                        var open = "CLOSE"
                        var side = if (info.orderSide.equals("BUY")) "SELL" else "BUY"
                        var type = 2
                        var leverageLevel = info.leverageLevel
                        var price = "0"
                        var volume = BigDecimalUtils.getOrderLossNum(vol, multiplier)
                        var isConditionOrder = false
                        var triggerPrice = ""
                        var expireTime = LogicContractSetting.getStrategyEffectTimeStr(mActivity)
                        addDisposable(getContractModel().createOrder(contractId, positionType, open, side, type, leverageLevel, price, volume, isConditionOrder, triggerPrice, expireTime,
                                consumer = object : NDisposableObserver(mActivity, true) {
                                    override fun onResponseSuccess(jsonObject: JSONObject) {
                                        Observable.timer(500, TimeUnit.MILLISECONDS)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                                    getPositionList()
                                                }
                                    }
                                }))
                    }
                }, getLineText("contract_action_marketClosing"))
            }
        }
        )
    }
    /**
     * 校验平仓输入
     */
    private fun preVerifyClosePositionInputV2(vol: String, price: String): Boolean {
        if (BigDecimalUtils.compareTo(vol, "0") != 1) {
            NToastUtil.showTopToastNet(this.mActivity,false, getLineText("sl_str_volume_too_low"))
            return false
        }
        if (BigDecimalUtils.compareTo(price, "0") != 1) {
            NToastUtil.showTopToastNet(this.mActivity,false, getLineText("sl_str_price_too_low"))
            return false
        }
        return true
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_contract_refresh_position_list_event -> {
                getPositionList()
            }
            MessageEvent.sl_contract_change_unit_event -> {
                getPositionList()
            }
            MessageEvent.sl_contract_modify_leverage_event -> {
                getPositionList()
            }
            MessageEvent.sl_contract_user_config_event -> {
                val userConfigObj = event.msg_content as JSONObject
                openContract = userConfigObj.optInt("openContract")
            }
            MessageEvent.sl_contract_modify_position_margin_event -> {
                getPositionList()
            }
            MessageEvent.sl_contract_change_position_list_event -> {
                val mListBuffer = ArrayList<ClContractPositionBean>()
                val mOrderListJson = event.msg_content as JSONArray
                for (i in 0..(mOrderListJson.length() - 1)) {
                    var obj = mOrderListJson.getString(i)
                    mListBuffer.add(Gson().fromJson<ClContractPositionBean>(obj, ClContractPositionBean::class.java))
                }
                LogUtil.e(TAG,"更新持仓列表")
                adapter?.setList(mListBuffer)
            }
            MessageEvent.sl_contract_logout_event -> {
                mList.clear()
                adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        if (isVisible) {
            loopStart()
        } else {
            loopStop()
        }
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        LogUtil.e(TAG,"持仓显示2"+isVisibleToUser)
        if (!isVisibleToUser) {
            loopStop()
        }else{
            loopStart()
        }
    }
    var loopSubscribe: Disposable? = null
    private fun loopStart() {
        loopStop()
        loopSubscribe = Observable.interval(0L, CommonConstant.coinLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getPositionList()
                }
    }

    private fun loopStop() {
        if (loopSubscribe != null) {
            loopSubscribe?.dispose()
        }
    }

}