package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpTimeFormatUtils
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpNewDialogUtils
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.bean.CpCurrentOrderBean
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_detail.*
import org.json.JSONObject
import java.math.BigDecimal

/**
 * 委托详情
 */
class CpContractEntrustDetailActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_contract_entrust_detail
    }

    private var contractOrder: CpCurrentOrderBean? = null

    private var mContractTransactionRecordAdapter: ContractTransactionRecordAdapter? = null
    private val mList = ArrayList<JSONObject>()

    var mPricePrecision = 0
    var mMultiplierCoin = ""
    var mMarginCoin = "--"
    var mMarginCoinPrecision = 0
    var mMultiplier = "0"
    var mMultiplierPrecision = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
    }

    override fun loadData() {
        contractOrder = intent.extras?.getSerializable("order") as CpCurrentOrderBean?
        if (contractOrder == null) {
            finish()
        }

        mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractOrder?.contractId!!.toInt())

        mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(this, contractOrder?.contractId!!.toInt())

        mMarginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(this, contractOrder?.contractId!!.toInt())

        mMarginCoin = CpClLogicContractSetting.getContractMarginCoinById(this, contractOrder?.contractId!!.toInt())

        mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(this, contractOrder?.contractId!!.toInt())

        mMultiplier = CpClLogicContractSetting.getContractMultiplierById(this, contractOrder?.contractId!!.toInt())

        //成交数量
//        tv_volume.setText(getString(R.string.cp_extra_text8) + if (CpClLogicContractSetting.getContractUint(this) == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")
        tv_trades_volume_key.setText(getString(R.string.cp_extra_text8) + if (CpClLogicContractSetting.getContractUint(this) == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")

        //成交均价
//        tv_deal_price.setText(getString(R.string.cl_average_price_str) + "(" + contractOrder?.quote + ")")
        tv_deal_price_key.setText(getString(R.string.cp_extra_text31) + "(" + contractOrder?.quote + ")")

        //手续费
//        tv_fee.setText(getString(R.string.cp_position_text2) + "(" + mMarginCoin + ")")
        tv_fee_key.setText(getString(R.string.cp_position_text2) + "(" + mMarginCoin + ")")

        tv_pl_price_key.setText(getString(R.string.cp_order_text8) + "(" + mMarginCoin + ")")
    }

    override fun initView() {

        mContractTransactionRecordAdapter = ContractTransactionRecordAdapter(mList)
        ll_layout.layoutManager = LinearLayoutManager(this)
        ll_layout.adapter = mContractTransactionRecordAdapter
        mContractTransactionRecordAdapter?.setEmptyView(CpEmptyForAdapterView(this))
        ll_layout.adapter = mContractTransactionRecordAdapter

        initAutoStringView()
        contractOrder?.let {
            //合约名称
            val symbol = CpClLogicContractSetting.getContractShowNameById(mActivity, it.contractId.toInt())
            tv_contract_name.text = symbol
            //方向
            when (it.side) {
                "BUY" -> {
                    if (it.open.equals("OPEN")) {
                        tv_type.setText(R.string.cp_overview_text13)
                        tv_type.setTextColor(resources.getColor(R.color.main_green))
//                        collapsing_toolbar.title = symbol
                    } else {
                        tv_type.setText(R.string.cp_extra_text4)
                        tv_type.setTextColor(resources.getColor(R.color.main_green))
//                        collapsing_toolbar.title = symbol
                    }
                }
                "SELL" -> {
                    if (it.open.equals("OPEN")) {
                        tv_type.setText(R.string.cp_overview_text14)
                        tv_type.setTextColor(resources.getColor(R.color.main_red))
//                        collapsing_toolbar.title = symbol
                    } else {
                        tv_type.setText(R.string.cp_extra_text5)
                        tv_type.setTextColor(resources.getColor(R.color.main_red))
//                        collapsing_toolbar.title = symbol
                    }
                }
            }
            ll_stop_profit.visibility = if (it.otoOrder == null) View.GONE else View.VISIBLE
            ll_stop_loss.visibility = if (it.otoOrder == null) View.GONE else View.VISIBLE
            ll_stop_profit_title.visibility = if (it.otoOrder == null) View.GONE else View.VISIBLE

            if (it.otoOrder != null) {
                tv_stop_profit_trigger_price_value.setText(if (it.otoOrder.takerProfitTrigger.toString().equals("0")) getString(R.string.cp_overview_text53) else it.otoOrder.takerProfitTrigger)
                tv_stop_profit_entrust_price_value.setText(if (it.otoOrder.takerProfitPrice.toString().equals("0")) getString(R.string.cp_overview_text53) else it.otoOrder.takerProfitPrice)
                if (it.otoOrder.takerProfitPrice.toString().equals("null")) {
                    tv_stop_profit_state_value.setText("--")
                    ll_stop_profit.visibility = View.GONE
                } else {
                    tv_stop_profit_state_value.setText(if (it.otoOrder.takerProfitStatus) getString(R.string.cp_order_text88) else getString(R.string.cp_extra_text72))
                }

                tv_stop_loss_trigger_price_value.setText(if (it.otoOrder.stopLossTrigger.toString().equals("0")) getString(R.string.cp_overview_text53) else it.otoOrder.stopLossTrigger)
                tv_loss_profit_entrust_price_value.setText(if (it.otoOrder.stopLossPrice.toString().equals("0")) getString(R.string.cp_overview_text53) else it.otoOrder.stopLossPrice)

                if (it.otoOrder.stopLossPrice.toString().equals("null")) {
                    tv_loss_profit_state_value.setText("--")
                    ll_stop_loss.visibility = View.GONE
                } else {
                    tv_loss_profit_state_value.setText(if (it.otoOrder.stopLossStatus) getString(R.string.cp_order_text88) else getString(R.string.cp_extra_text72))
                }
            }




            tv_entrust_price.text =if (it.type.equals("2")) getString(R.string.cp_overview_text53) else  CpBigDecimalUtils.showSNormal(it.price, it.pricePrecision)

            tv_entrust_amount.text = it.volume
            tv_pl_price.text = CpBigDecimalUtils.showSNormal(it.realizedAmount, mMarginCoinPrecision)
            tv_order_type.text = when (it.type) {
                "1" -> getString(R.string.cp_overview_text3)
                "2" -> getString(R.string.cp_overview_text4)
                "3" -> "IOC"
                "4" -> "FOK"
                "5" -> "Post Only"
                "6" -> getString(R.string.cp_extra_text6)
                "7" -> getString(R.string.cp_extra_text7)
                else -> "error"
            }
            tv_order_type.isEnabled=false

            tv_date.text = CpTimeFormatUtils.timeStampToDate(it.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss")
            tv_status.text = when (it.status) {
                "2" -> getString(R.string.cp_extra_text1)//完全成交
                "3" -> getString(R.string.cp_order_text55)//"部分成交"
                "4" -> getString(R.string.cp_status_text2)//"已撤销"
                "5" -> getString(R.string.cp_status_text4)//"待撤销"
                "6" -> getString(R.string.cp_status_text3)//"异常订单"
                else -> "error"
            }
        }
        tv_order_type.setOnClickListener {
            var tip = contractOrder?.liqPositionMsg.toString()
            if (TextUtils.isEmpty(tip)){
                tip=""
            }
            if (tip.contains("\\n")){
                tip = tip.replace("\\n", "<br />")
            }
            CpNewDialogUtils.showDialog(mActivity!!, tip, true, null, getString(R.string.cp_extra_text80), getString(R.string.cp_extra_text28))
        }

        tv_order_tips.setText(when (contractOrder?.memo) {
            1 -> getString(R.string.cp_extra_text17)
            2 -> getString(R.string.cp_extra_text73)
            3 -> getString(R.string.cp_extra_text74)
            4 -> getString(R.string.cp_extra_text75)
            5 -> getString(R.string.cp_extra_text76)
            6 -> getString(R.string.cp_extra_text77)
            7 -> getString(R.string.cp_extra_text78)
            8 -> getString(R.string.cp_extra_text79)
            else -> ""
        })
        ll_list_tips.visibility = if (contractOrder?.status.equals("4")) View.VISIBLE else View.GONE
//        ll_list_title.visibility = if (!contractOrder?.status.equals("4")) View.VISIBLE else View.GONE


        val mContractUint = CpClLogicContractSetting.getContractUint(this@CpContractEntrustDetailActivity)
        contractOrder?.let {

            val mMarginCoin = CpClLogicContractSetting.getContractMarginCoinById(mActivity, it.contractId.toInt())
            var contractSide = CpClLogicContractSetting.getContractSideById(this@CpContractEntrustDetailActivity, it.contractId.toInt())

            //成交均价
            tv_deal_price_value.text = CpBigDecimalUtils.showSNormalNew(it.avgPrice, CpClLogicContractSetting.getContractSymbolPricePrecisionById(this,it.contractId.toInt()))
            //成交量
            tv_volume_value.text = if (mContractUint == 0) {
                it.dealVolume
            } else {
                CpBigDecimalUtils.mulStr(it.dealVolume, mMultiplier, mMultiplierPrecision)
            }
            //手续费
            if (it.open.equals("OPEN") && it.type.equals("2")) {
                tv_entrust_amount_key.setText(getString(R.string.cp_order_text66) + "(" + (if (contractSide == 1) it.quote else it.base) + ")")
                tv_entrust_amount.setText(it.volume)
            } else {
                tv_entrust_amount_key.setText(getString(R.string.cp_order_text66) + if (mContractUint == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")
                tv_entrust_amount.setText(if (mContractUint == 0) it.volume else CpBigDecimalUtils.mulStr(it.volume, mMultiplier, mMultiplierPrecision))
            }

            if (it.type.equals("6")) {
                val nav_up = getResources().getDrawable(R.drawable.cp_contract_prompt);
                nav_up.setBounds(5, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
                tv_order_type.setCompoundDrawables(null, null, nav_up, null);
                tv_order_type.isEnabled=true

                tv_deal_price_value.setText("--")
                tv_volume_value.setText("--")
            }

        }
        tv_volume_key.setText(getString(R.string.cp_extra_text8) + if (mContractUint == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")

        getHistoryTradeList()
    }

    private fun getHistoryTradeList() {
        mList.clear()
        addDisposable(getContractModel().getHistoryTradeList(contractOrder?.contractId!!.toString(), contractOrder?.id!!,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("tradeList")) {
                                val mTradeList = optJSONArray("tradeList")
                                var buffFee = BigDecimal.ZERO;
                                var feeCoinPrecision = 0
                                var pricePrecision = 0
                                if (mTradeList.length() != 0) {
                                    for (i in 0..(mTradeList.length() - 1)) {
                                        var obj: JSONObject = mTradeList.get(i) as JSONObject
                                        buffFee = buffFee.add(BigDecimal(obj.optString("fee")))
                                        feeCoinPrecision = obj.optInt("feeCoinPrecision")
                                        pricePrecision = obj.optInt("pricePrecision")
                                        obj.putOpt("mMultiplier", mMultiplier)
                                        obj.putOpt("mMultiplierPrecision", mMultiplierPrecision)
                                        obj.putOpt("mContractUint", CpClLogicContractSetting.getContractUint(this@CpContractEntrustDetailActivity))
                                        mList.add(obj)
                                    }
                                }
                                val mContractUint = CpClLogicContractSetting.getContractUint(this@CpContractEntrustDetailActivity)
                                contractOrder?.let {

                                    val mMarginCoin = CpClLogicContractSetting.getContractMarginCoinById(mActivity, it.contractId.toInt())
                                    var contractSide = CpClLogicContractSetting.getContractSideById(this@CpContractEntrustDetailActivity, it.contractId.toInt())

                                    //成交均价
                                    tv_deal_price_value.text = CpBigDecimalUtils.showSNormalNew(it.avgPrice, pricePrecision)
                                    //成交量
                                    tv_volume_value.text = if (mContractUint == 0) {
                                        it.dealVolume
                                    } else {
                                        CpBigDecimalUtils.mulStr(it.dealVolume, mMultiplier, mMultiplierPrecision)
                                    }
                                    //手续费
                                    tv_fee_value.text = buffFee.setScale(feeCoinPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                                    if (it.open.equals("OPEN") && it.type.equals("2")) {
                                        tv_entrust_amount_key.setText(getString(R.string.cp_order_text66) + "(" + (if (contractSide == 1) it.quote else it.base) + ")")
                                        tv_entrust_amount.setText(it.volume)
                                    } else {
                                        tv_entrust_amount_key.setText(getString(R.string.cp_order_text66) + if (mContractUint == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")
                                        tv_entrust_amount.setText(if (mContractUint == 0) it.volume else CpBigDecimalUtils.mulStr(it.volume, mMultiplier, mMultiplierPrecision))
                                    }
                                    if (it.type.equals("6")){
                                        tv_deal_price_value.setText("--")
                                        tv_volume_value.setText("--")
                                    }
                                }
                                tv_volume_key.setText(getString(R.string.cp_extra_text8) + if (mContractUint == 0) "(" + getString(R.string.cp_overview_text9) + ")" else "(" + mMultiplierCoin + ")")
                                mContractTransactionRecordAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }))
    }

    private fun initAutoStringView() {
        img_back?.setOnClickListener {
            finish()
        }
    }


    companion object {
        fun show(activity: Activity, order: CpCurrentOrderBean) {
            val intent = Intent(activity, CpContractEntrustDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("order", order)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    class ContractTransactionRecordAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cp_item_deal_order, data) {
        override fun convert(helper: BaseViewHolder, item: JSONObject) {
            helper?.run {
                //成交数量（张）
                setText(R.id.tv_type_value, if (item.optInt("mContractUint") == 0) {
                    item.optString("volume")
                } else {
                    CpBigDecimalUtils.mulStr(item.optString("volume"), item.optString("mMultiplier"), item.optInt("mMultiplierPrecision"))
                })
                //成交价格
                setText(R.id.tv_time_value, CpBigDecimalUtils.showSNormal(item.optString("price"), item.optInt("pricePrecision")))
                //手续费
                setText(R.id.tv_amount_value, CpBigDecimalUtils.showSNormal(item.optString("fee"), item.optInt("feeCoinPrecision")))
            }
        }
    }
}

