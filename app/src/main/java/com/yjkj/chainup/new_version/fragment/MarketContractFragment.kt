package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.chainup.contract.adapter.CpPageAdapter
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpJsonUtils
import com.chainup.contract.ws.CpWsContractAgentManager

import com.flyco.tablayout.listener.OnTabSelectListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.manager.CpLanguageUtil.getString
import kotlinx.android.synthetic.main.fragment_market_contract.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * @Author lianshangljl
 * @Date 2019/3/15-4:10 PM
 * @Email buptjinlong@163.com
 * @description  新版本行情页面
 */
class MarketContractFragment : NBaseFragment() , CpWsContractAgentManager.WsResultCallback{

    private var mContractList: JSONArray? = null
    private var contractListJson: String? = null
    private val ContractCodeList = ArrayList<String>()
    private val showTitles = ArrayList<String>()
    //0是首页1是合约
    private var type:Int=0
    private val fragments = ArrayList<Fragment>()
    override fun setContentView(): Int {
        return R.layout.fragment_market_contract
    }


    companion object {
        @JvmStatic
        fun newInstance(index: Int): MarketContractFragment {
            val fg = MarketContractFragment()
            val bundle = Bundle()
            bundle.putInt(CpParamConstant.CUR_INDEX, index)
            fg.arguments = bundle
            return fg
        }
    }
    override fun initView() {
        type = arguments!!.getInt(CpParamConstant.CUR_INDEX)
        contractListJson = CpClLogicContractSetting.getContractJsonListStr(activity)
        try {
            mContractList = JSONArray(contractListJson)
        }catch (e:Exception){

        }
         if (type==0){
             tv_u.visibility=View.GONE
             ll_item_titles.visibility=View.VISIBLE
         }else{
             tv_u.visibility=View.VISIBLE
             ll_item_titles.visibility=View.GONE
         }



        initTab()
    }

    override fun loadData() {
        super.loadData()
        CpWsContractAgentManager.instance.addWsCallback(this)




    }


private fun initTab() {
    showTitles.clear()
    fragments.clear()
    var isHasU = false //正向合约
    var isHasH = false //混合合约
    var isHasM = false //模拟合约
    val arrays = arrayOfNulls<String>(mContractList?.length() ?: 0)
    ContractCodeList.clear()
    if (mContractList==null||mContractList?.length() == 0) {
        return
    }
    for (i in 0 until mContractList!!.length()) {
        var contractType = "E"
        try {
            // (反向：0，1：正向 , 2 : 混合 , 3 : 模拟)
            val contractSide: Int = mContractList!!.getJSONObject(i).getInt("contractSide")
            contractType = mContractList!!.getJSONObject(i).getString("contractType")
            when (contractType) {
                "E" -> isHasU = true
                "H" -> isHasM = true
                "S" -> isHasH = true
            }
            val obj = mContractList!!.get(i) as JSONObject
            val currentSymbolBuff =
                (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).lowercase(
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
    CpWsContractAgentManager.instance.sendMessage(rmap, this)

    //USDT
    if (isHasU) {
        showTitles.add(getString(context, "cp_contract_data_text13"))
        fragments.add(MarketContracthItemFragment.newInstance(1,type, contractListJson.toString()))
    }
    //
    //混合
    if (isHasH) {
        showTitles.add(getString(context, "cp_contract_data_text12"))
        fragments.add(MarketContracthItemFragment.newInstance(2,type, contractListJson.toString()))
    }
    //模拟
    if (isHasM) {
        showTitles.add(getString(context, "cp_contract_data_text11"))
        fragments.add(MarketContracthItemFragment.newInstance(3,type, contractListJson.toString()))
    }
    val marketPageAdapter = CpPageAdapter(childFragmentManager, showTitles, fragments)
    vp_market_contract.adapter = marketPageAdapter
    vp_market_contract.offscreenPageLimit = fragments.size
    val showTitlesArray = arrayOfNulls<String>(showTitles.size)
    for (j in showTitles.indices) {
        showTitlesArray[j] = showTitles[j]
    }
    tl_market_aa.setViewPager(vp_market_contract, showTitlesArray)
    tl_market_aa.setOnTabSelectListener(object : OnTabSelectListener {
        override fun onTabSelect(position: Int) {
            if (showTitles[position] == getString(context, "cl_market_text7")) {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_receive_coupon))
            }
        }

        override fun onTabReselect(position: Int) {}
    })
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

}