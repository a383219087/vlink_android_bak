package com.yjkj.chainup.new_version.home

import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.chainup.contract.adapter.CpPageAdapter
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpJsonUtils
import com.chainup.contract.ws.CpWsContractAgentManager
import com.chainup.contract.ws.CpWsContractAgentManager.Companion.instance
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.CpLanguageUtil.getString
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.fragment.CpCoinSearchItemFragment2.Companion.newInstance2
import kotlinx.android.synthetic.main.fragment_home_contract.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @time : 2022/12/5
 * @author :liaoshudong
 * @Details :
 */
class NewHomeContractFragment : CpNBaseFragment(), CpWsContractAgentManager.WsResultCallback{

    private var mContractList: JSONArray? = null
    private var contractListJson: String? = null
    private val ContractCodeList = ArrayList<String>()
    private val showTitles = ArrayList<String>()
    private val fragments = ArrayList<Fragment>()

    override fun initView() {
        addDisposable(getContractModel().getPublicInfo(
            consumer = object : CpNDisposableObserver(mActivity, true) {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    saveContractPublicInfo(jsonObject)
                }
            })
        )
    }



    override fun loadData() {

    }

    private fun saveContractPublicInfo(jsonObject: JSONObject) {
        jsonObject.optJSONObject("data").run {
            var contractList = optJSONArray("contractList")
            CpClLogicContractSetting.setContractJsonListStr(context, contractList.toString())
            showData()
        }
    }

    private fun showData(){
        val mContractListData = CpClLogicContractSetting.getContractJsonListStr(activity)
        instance.addWsCallback(this)
        if (!TextUtils.isEmpty(mContractListData)) {
            contractListJson = mContractListData
            try {
                mContractList = JSONArray(contractListJson)
                initTab()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun initTab() {
        showTitles.clear()
        fragments.clear()
        var isHasU = false //正向合约
        var isHasH = false //混合合约
        var isHasM = false //模拟合约
        val arrays = arrayOfNulls<String>(mContractList!!.length())
        ContractCodeList.clear()
        if (mContractList!!.length() == 0) {
            return
        }
        for (i in 0 until mContractList!!.length()) {
            var contractType: String? = "E"
            try {
                // (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
                contractType = mContractList!!.getJSONObject(i).getString("contractType")
                when (contractType) {
                    "E" -> isHasU = true
                    "H" -> isHasM = true
                    "S" -> isHasH = true
                }
                val obj = mContractList!![i] as JSONObject
                val currentSymbolBuff =
                    (obj.getString("contractType") + "_" + obj.getString("symbol")
                        .replace("-", "")).lowercase(
                        Locale.getDefault()
                    )
                ContractCodeList.add(currentSymbolBuff)
                arrays[i] = currentSymbolBuff
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val rmap = HashMap<String, Any>()
        rmap["bind"] = true
        rmap["symbols"] = CpJsonUtils.gson.toJson(arrays)
        instance.sendMessage(rmap, this)
        //        WsContractAgentManager.instance.sendMessage(hashMapOf("bind" to true, "symbols" to JsonUtils.gson.toJson(arrays)), this)

        //USDT
        if (isHasU) {
            showTitles.add(getString(context, "cp_contract_data_text13"))
            fragments.add(newInstance2(1, contractListJson!!))
        }
        //
        //混合
        if (isHasH) {
            showTitles.add(getString(context, "cp_contract_data_text12"))
            fragments.add(newInstance2(2, contractListJson!!))
        }
        //模拟
        if (isHasM) {
            showTitles.add(getString(context, "cp_contract_data_text11"))
            fragments.add(newInstance2(3, contractListJson!!))
        }
        val vp_market_aa: ViewPager = vp_market_aa
        val marketPageAdapter = CpPageAdapter(childFragmentManager, showTitles, fragments)
        vp_market_aa.adapter = marketPageAdapter
        vp_market_aa.offscreenPageLimit = fragments.size
        val showTitlesArray = arrayOfNulls<String>(showTitles.size)
        for (j in showTitles.indices) {
            showTitlesArray[j] = showTitles[j]
        }
    }

    override fun setContentView(): Int {
        return R.layout.fragment_home_contract;
    }

    override fun onWsMessage(json: String) {
        try {
            val jsonObject = JSONObject(json)
            val messageEvent = CpMessageEvent(CpMessageEvent.sl_contract_sidebar_market_event)
            messageEvent.msg_content = jsonObject
            CpEventBusUtil.post(messageEvent)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val arrays = arrayOfNulls<String>(ContractCodeList.size)
        ContractCodeList.toArray(arrays)
        val rmap = HashMap<String, Any>()
        rmap["bind"] = false
        rmap["symbols"] = CpJsonUtils.gson.toJson(arrays)
    }
}