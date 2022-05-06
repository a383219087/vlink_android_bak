package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.util.BigDecimalUtils
import kotlinx.android.synthetic.main.cl_activity_contract_detail.*
import kotlinx.android.synthetic.main.cl_activity_contract_detail.title_layout
import org.json.JSONObject
import java.lang.Exception


/**
 * 合约详情
 */
class ClContractDetailActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_contract_detail
    }

    private var contractId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
        getLadderInfo()
    }


    override fun initView() {
        super.initView()
        initAutoTextView()

    }

    private fun initAutoTextView() {
        title_layout.setContentTitle(getLineText("sl_str_tab_contract_info"))
    }

    override fun loadData() {
        super.loadData()
        contractId = intent.getIntExtra("contractId", -1)
        val obj = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
        val mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        tv_contract_name.setText(LogicContractSetting.getContractShowNameById(mActivity,contractId))
        tv_contract_subject_matter.setText(LogicContractSetting.getContractShowNameById(mActivity,contractId)+getString(R.string.cl_assets_text18))

        //deliveryKind : 0 永续, 1 周, 2 次周, 3 月, 4 季度
        if (obj.optInt("deliveryKind") == 0) {
            tv_contract_type.setText(getString(R.string.cl_assets_text24))
        } else if (obj.optInt("deliveryKind") == 1) {
            tv_contract_type.setText(getString(R.string.cl_assets_text25))
        } else if (obj.optInt("deliveryKind") == 2) {
            tv_contract_type.setText(getString(R.string.cl_assets_text26))
        } else if (obj.optInt("deliveryKind") == 3) {
            tv_contract_type.setText(getString(R.string.cl_assets_text27))
        } else {
            tv_contract_type.setText(getString(R.string.cl_assets_text28))
        }



        tv_margin_coin.setText(obj.optString("marginCoin"))
        tv_par_value.setText(BigDecimalUtils.showSNormal(obj.optString("multiplier"), mMultiplierPrecision) + obj.optString("multiplierCoin"))
        tv_settlement_period.setText(obj.optString("settlementFrequency") + "Min")

//        tv_keep_margin_ratio.setText(obj.optString("settlementFrequency") + "Min")

        val symbolPricePrecision = obj.optJSONObject("coinResultVo").optInt("symbolPricePrecision")
        if (symbolPricePrecision == 0) {
            tv_minimum_price.setText("1" + obj.optString("quote"))
        } else {
            var buff = StringBuffer("0")
            buff.append(".")
            for (x in 0 until symbolPricePrecision - 1) {
                buff.append("0")
            }
            buff.append("1")
            tv_minimum_price.setText(buff.toString() + obj.optString("quote"))
        }

    }

    private fun getLadderInfo() {
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            try {
                                val ladderList = optJSONObject("ladderList").optJSONArray("ladderList")
                                var minMarginRate = ladderList.getJSONObject(0).getString("minMarginRate")
                                val rate: Double = MathHelper.mul(minMarginRate, "100")
                                tv_keep_margin_ratio.setText(NumberUtil.getDecimal(2).format(rate).toString() + "%")
                            } catch (e: Exception) {

                            }
                        }
                    }
                }))
    }

    companion object {
        fun show(activity: Activity, contractId: Int = 0) {
            val intent = Intent(activity, ClContractDetailActivity::class.java)
            intent.putExtra("contractId", contractId)
            activity.startActivity(intent)
        }
    }

}