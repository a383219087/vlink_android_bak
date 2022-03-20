package com.chainup.contract.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpMathHelper
import com.chainup.contract.utils.CpNumberUtil
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import kotlinx.android.synthetic.main.fragment_cp_contract_para.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


private const val ARG_PARAM1 = "contractId"

class CpContractParaFragment : CpNBaseFragment() {
    private var contractId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractId = it.getInt(ARG_PARAM1)
        }
    }

    override fun initView() {
        val obj = CpClLogicContractSetting.getContractJsonStrById(mActivity, contractId)
        val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(mActivity, contractId)
        tv_contract_name.setText(CpClLogicContractSetting.getContractShowNameById(mActivity,contractId))
        tv_contract_subject_matter.setText(CpClLogicContractSetting.getContractNameById(mActivity,contractId)+getString(R.string.cp_contract_info_text13))

        //deliveryKind : 0 永续, 1 周, 2 次周, 3 月, 4 季度
        if (obj.optInt("deliveryKind") == 0) {
            tv_contract_type.setText(getString(R.string.cp_extra_text121))
        } else if (obj.optInt("deliveryKind") == 1) {
            tv_contract_type.setText(getString(R.string.cp_extra_text122))
        } else if (obj.optInt("deliveryKind") == 2) {
            tv_contract_type.setText(getString(R.string.cp_extra_text123))
        } else if (obj.optInt("deliveryKind") == 3) {
            tv_contract_type.setText(getString(R.string.cp_extra_text124))
        } else {
            tv_contract_type.setText(getString(R.string.cp_extra_text125))
        }



        tv_margin_coin.setText(obj.optString("marginCoin"))
        tv_par_value.setText(CpBigDecimalUtils.showSNormal(obj.optString("multiplier"), mMultiplierPrecision) + obj.optString("multiplierCoin"))
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
        getLadderInfo()
    }

    override fun setContentView(): Int {
      return R.layout.fragment_cp_contract_para
    }

    private fun getLadderInfo() {
        addDisposable(getContractModel().getLadderInfo(contractId.toString(),
                consumer = object : CpNDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            try {
                                val ladderList = optJSONObject("ladderList").optJSONArray("ladderList")
                                var minMarginRate = ladderList.getJSONObject(0).getString("minMarginRate")
                                val rate: Double = CpMathHelper.mul(minMarginRate, "100")
                                tv_keep_margin_ratio.setText(CpNumberUtil().getDecimal(2).format(rate).toString() + "%")
                            } catch (e: Exception) {

                            }
                        }
                    }
                }))
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                CpContractParaFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM1, param1)
                    }
                }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_calc_switch_contract_event -> {
                contractId = event.msg_content as Int
                initView()
            }
        }
    }
}