package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpEmptyForAdapterView
import com.yjkj.chainup.new_contract.adapter.CpContractSearchAdapter
import kotlinx.android.synthetic.main.cp_activity_contract_coin_search.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 合约搜索
 */
class CpContractSearchActivity : CpNBaseActivity(){
    override fun setContentView(): Int {
        return R.layout.cp_activity_contract_coin_search
    }

    private val mList = ArrayList<JSONObject>()
    private val spitList = ArrayList<JSONObject>()
    private var adapter: CpContractSearchAdapter?=null

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        loadData()
        initView()
    }

    override fun loadData() {
        super.loadData()
        val contractJsonListStr = CpClLogicContractSetting.getContractJsonListStr(mActivity)
        val jsonArray = JSONArray(contractJsonListStr)
        for (i in 0 until jsonArray.length()) {
            val mJSONObject = jsonArray[i] as JSONObject
            mList.add(mJSONObject)
        }
        tv_cancel.setOnClickListener {
            finish()
        }
    }

    override fun initView() {
        super.initView()
        adapter = CpContractSearchAdapter(mList)
        lv_layout.layoutManager = LinearLayoutManager(mActivity)
        lv_layout.adapter=adapter
        adapter?.setEmptyView(CpEmptyForAdapterView(this ?: return))
        lv_layout.adapter = adapter
        adapter?.setOnItemClickListener { adapter, _, position ->
            val item = adapter?.getItem(position) as JSONObject
            val intent = Intent()
            intent.putExtra("contractId",item.optInt("id"))
            setResult(Activity.RESULT_OK,intent)
            finish()
          }
        tv_cancel.setText(getString(R.string.cp_overview_text56))
        et_search.setHint( getString(R.string.cp_extra_text81))
        et_search.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if(TextUtils.isEmpty(text)){
                    adapter?.setNewData(mList)
                    adapter?.notifyDataSetChanged()
                }else{
                    spitList.clear()
                    for (i in mList.indices){
                         val item = mList[i]
                        if(item.optString("symbol").contains(text.toUpperCase())){
                            spitList.add(item)
                        }
                    }
                    adapter?.setNewData(spitList)
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    companion object{
        fun show(activity:Activity){
           val intent = Intent(activity,CpContractSearchActivity::class.java)
            activity.startActivityForResult(intent,1001)
        }
    }

}