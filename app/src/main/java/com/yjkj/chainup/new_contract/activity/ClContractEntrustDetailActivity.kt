package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractSDKAgent
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import kotlinx.android.synthetic.main.cl_activity_contract_entrust_detail.*
import org.json.JSONObject
import java.math.BigDecimal

/**
 * 委托详情
 */
class ClContractEntrustDetailActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_contract_entrust_detail
    }

    private var contractOrder: ClCurrentOrderBean? = null

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
        contractOrder = intent.extras?.getSerializable("order") as ClCurrentOrderBean?
        if (contractOrder == null) {
            finish()
        }

        mMultiplierCoin = LogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractOrder?.contractId!!.toInt())

        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(this, contractOrder?.contractId!!.toInt())

        mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionById(this, contractOrder?.contractId!!.toInt())

        mMarginCoin = LogicContractSetting.getContractMarginCoinById(this, contractOrder?.contractId!!.toInt())

        mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(this, contractOrder?.contractId!!.toInt())

        mMultiplier = LogicContractSetting.getContractMultiplierById(this, contractOrder?.contractId!!.toInt())

        //成交数量
        tv_volume.setText(getString(R.string.cl_trades_volume_str) + if (LogicContractSetting.getContractUint(this) == 0) "(" + getString(R.string.sl_str_contracts_unit) + ")" else "(" + mMultiplierCoin + ")")
        tv_trades_volume_key.setText(getString(R.string.cl_trades_volume_str) + if (LogicContractSetting.getContractUint(this) == 0) "(" + getString(R.string.sl_str_contracts_unit) + ")" else "(" + mMultiplierCoin + ")")

        //成交均价
        tv_deal_price.setText(getString(R.string.cl_average_price_str) + "(" + contractOrder?.quote + ")")
        tv_deal_price_key.setText(getString(R.string.sl_str_deal_price) + "(" + contractOrder?.quote + ")")

        //手续费
        tv_fee.setText(getString(R.string.cl_commission_str) + "(" + mMarginCoin + ")")
        tv_fee_key.setText(getString(R.string.cl_commission_str) + "(" + mMarginCoin + ")")
    }

    override fun initView() {

        mContractTransactionRecordAdapter = ContractTransactionRecordAdapter(mList)
        ll_layout.layoutManager = LinearLayoutManager(this)
        ll_layout.adapter = mContractTransactionRecordAdapter
        mContractTransactionRecordAdapter?.setEmptyView(EmptyForAdapterView(this))
        ll_layout.adapter = mContractTransactionRecordAdapter

        initAutoStringView()
        contractOrder?.let {
            //合约名称
            val symbol = LogicContractSetting.getContractShowNameById(mActivity, it.contractId.toInt())
            tv_contract_name.text = symbol
            //方向
            when (it.side) {
                "BUY" -> {
                    if (it.open.equals("OPEN")) {
                        tv_type.setText(R.string.sl_str_buy_open)
                        tv_type.setTextColor(resources.getColor(R.color.main_green))
                        collapsing_toolbar.title = symbol
                    } else {
                        tv_type.setText(R.string.sl_str_buy_close)
                        tv_type.setTextColor(resources.getColor(R.color.main_green))
                        collapsing_toolbar.title = symbol
                    }
                }
                "SELL" -> {
                    if (it.open.equals("OPEN")) {
                        tv_type.setText(R.string.sl_str_sell_open)
                        tv_type.setTextColor(resources.getColor(R.color.main_red))
                        collapsing_toolbar.title = symbol
                    } else {
                        tv_type.setText(R.string.sl_str_sell_close)
                        tv_type.setTextColor(resources.getColor(R.color.main_red))
                        collapsing_toolbar.title = symbol
                    }
                }
            }
        }

        tv_order_tips.setText(when (contractOrder?.memo) {
            1 -> getString(R.string.cl_contract_add_text8)
            2 -> getString(R.string.cl_contract_add_text9)
            3 -> getString(R.string.cl_contract_add_text10)
            4 -> getString(R.string.cl_contract_add_text11)
            5 -> getString(R.string.cl_contract_add_text12)
            6 -> getString(R.string.cl_contract_add_text13)
            7 -> getString(R.string.cl_contract_add_text14)
            8 -> "仓位不存在，平仓委托被系统撤销"
            else -> ""
        })
        ll_list_tips.visibility=if (contractOrder?.status.equals("4")) View.VISIBLE else View.GONE
        ll_list_title.visibility=if (!contractOrder?.status.equals("4")) View.VISIBLE else View.GONE
        getHistoryTradeList()
    }

    private fun getHistoryTradeList() {
        mList.clear()
        addDisposable(getContractModel().getHistoryTradeList(contractOrder?.contractId!!.toString(), contractOrder?.id!!,
                consumer = object : NDisposableObserver(mActivity, true) {
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
                                        obj.putOpt("mContractUint", LogicContractSetting.getContractUint(this@ClContractEntrustDetailActivity))
                                        mList.add(obj)
                                    }
                                }
                                contractOrder?.let {

                                    val mMarginCoin = LogicContractSetting.getContractMarginCoinById(mActivity, it.contractId.toInt())

                                    //成交均价
                                    tv_deal_price_value.text = BigDecimalUtils.showSNormalNew(it.avgPrice, pricePrecision)
                                    //成交量
                                    tv_volume_value.text = if (LogicContractSetting.getContractUint(this@ClContractEntrustDetailActivity) == 0) {
                                        it.dealVolume
                                    } else {
                                        BigDecimalUtils.mulStr(it.dealVolume, mMultiplier, mMultiplierPrecision)
                                    }
                                    //手续费
                                    tv_fee_value.text = buffFee.setScale(feeCoinPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                                }

                                mContractTransactionRecordAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }))
    }

    private fun initAutoStringView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.let {
            it.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            it.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            it.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            it.expandedTitleGravity = Gravity.TOP
        }
    }


    companion object {
        fun show(activity: Activity, order: ClCurrentOrderBean) {
            val intent = Intent(activity, ClContractEntrustDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("order", order)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    class ContractTransactionRecordAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cl_item_deal_order, data) {
        override fun convert(helper: BaseViewHolder, item: JSONObject) {
            helper?.run {
                //成交数量（张）
                setText(R.id.tv_type_value, if (item.optInt("mContractUint") == 0) {
                    item.optString("volume")
                } else {
                    BigDecimalUtils.mulStr(item.optString("volume"), item.optString("mMultiplier"), item.optInt("mMultiplierPrecision"))
                })
                //成交价格
                setText(R.id.tv_time_value, BigDecimalUtils.showSNormal(item.optString("price"), item.optInt("pricePrecision")))
                //手续费
                setText(R.id.tv_amount_value, BigDecimalUtils.showSNormal(item.optString("fee"), item.optInt("feeCoinPrecision")))
            }
        }
    }
}

