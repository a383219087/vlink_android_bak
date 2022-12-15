package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.ClContractAssetAdapter
import com.yjkj.chainup.util.ContextUtil
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.accet_header_view.view.*
import kotlinx.android.synthetic.main.fragment_asset.*
import kotlinx.android.synthetic.main.sl_fragment_contract_asset_child.*
import org.json.JSONObject

/**
 * 合约资产类
 */
class ClContractAssetFragmentChild : NBaseFragment() {
    override fun setContentView(): Int {
        return R.layout.sl_fragment_contract_asset_child
    }

    var adapterHoldContract: ClContractAssetAdapter? = null
    val mList = ArrayList<JSONObject>()

    /**
     * 隐藏小额资产
     */
    private var isLittleAssetsShow = false
    private var openContract = 0

    //是否展示合约购买对话框
    var isShowContractBuyDialog = false
    var isScrollStatus = false

    var fragments = ArrayList<Fragment>()

    val showTitles = arrayListOf<String>()
    override fun initView() {
        isShowContractBuyDialog = PreferenceManager.getBoolean(mActivity, "isShowContractBuyDialog", true)

        initHoldContractAdapter()
        NLiveDataUtil.observeData(this, androidx.lifecycle.Observer {
            if (MessageEvent.refresh_trans_type == it?.msg_type) {
                isLittleAssetsShow = !isLittleAssetsShow
                UserDataService.getInstance().saveAssetState(isLittleAssetsShow)
            } else if (MessageEvent.refresh_local_contract_type == it?.msg_type) {
                LogUtil.d("DEBUG", "刷新合约资产列表2")
                setRefreshAdapter()
            }
        })

        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        /**
         * 此处刷新
         */
        swipe_refresh?.setOnRefreshListener {
            /**
             * 刷新数据操作
             */
            getPositionList()
        }

    }

    override fun loadData() {
        getPositionList()
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            loadContractUserConfig()
        }
    }

    private fun getPositionList() {
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        mList.clear()
        addDisposable(getContractModel().getPositionAssetsList(
                consumer = object : NDisposableObserver(isScrollStatus) {
                    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data")?.run {
                            if (!isNull("accountList")) {
                                val mAccountListJson = optJSONArray("accountList")
                                mList.clear()
                                if (mAccountListJson != null) {
                                    for (i in 0 until mAccountListJson.length()) {
                                        val data: JSONObject=mAccountListJson.get(i) as JSONObject
                                        if (data.optString("totalAmount").toDouble()>0){
                                            mList.add(0,data)

                                        }else{
                                            mList.add(data)
                                        }


                                        LogUtil.e(TAG, "------------------------------------")
                                    }
                                }
                            }
                            adapterHoldContract?.notifyDataSetChanged()
                        }
                        swipe_refresh?.isRefreshing =false
                    }
                }))
    }


    private fun loadContractUserConfig() {
        //如果合约ID传0则获取默认的数据，此处主要就是获取是否开通合约
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(getContractModel().getUserConfig("0",
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            //  1已开通, 0未开通
                            openContract = optInt("openContract")
                            getPositionList()
                        }
                    }
                }))
    }



    fun setRefreshAdapter() {
        getPositionList()
    }




    /**
     * 合约未平仓合约
     */
    private fun initHoldContractAdapter() {
        adapterHoldContract = ClContractAssetAdapter(context!!, mList)
        rc_contract?.layoutManager = LinearLayoutManager(context)
        rc_contract.adapter = adapterHoldContract
        adapterHoldContract?.setEmptyView(R.layout.item_new_empty_assets)
        adapterHoldContract?.headerWithEmptyEnable = true
        rc_contract?.adapter = adapterHoldContract
    }

}


