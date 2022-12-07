package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.chainup.contract.ui.activity.CpContractAssetRecordActivity
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.ClContractAssetAdapter
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.NewAssetTopView
import com.yjkj.chainup.util.ContextUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.accet_header_view.view.*
import kotlinx.android.synthetic.main.sl_fragment_contract_asset.*

import org.json.JSONObject

/**
 * 合约资产类
 */
class ClContractAssetFragment : NBaseFragment() {
    override fun setContentView(): Int {
        return R.layout.sl_fragment_contract_asset
    }

    var adapterHoldContract: ClContractAssetAdapter? = null
    val mList = ArrayList<JSONObject>()

    /**
     * 隐藏小额资产
     */
    private var isLittleAssetsShow = false
    private var openContract = 0
    var assetHeadView: NewAssetTopView? = null

    //是否展示合约购买对话框
    var isShowContractBuyDialog = false
    var isScrollStatus = false

    var fragments = ArrayList<Fragment>()

    val showTitles = arrayListOf<String>()
    override fun initView() {
        isShowContractBuyDialog = PreferenceManager.getBoolean(mActivity, "isShowContractBuyDialog", true)
        assetHeadView = NewAssetTopView(activity!!, null, 0)
        assetHeadView?.initNorMalView(ParamConstant.CONTRACT_INDEX)
        initHoldContractAdapter()
        NLiveDataUtil.observeData(this, androidx.lifecycle.Observer {
            if (MessageEvent.refresh_trans_type == it?.msg_type) {
                isLittleAssetsShow = !isLittleAssetsShow
                UserDataService.getInstance().saveAssetState(isLittleAssetsShow)
                assetHeadView?.setAssetOrderHide(isLittleAssetsShow)
            } else if (MessageEvent.refresh_local_contract_type == it?.msg_type) {
                LogUtil.d("DEBUG", "刷新合约资产列表2")
                setRefreshAdapter()
            }
        })
        //划转
        assetHeadView?.ll_transfer_layout?.setOnClickListener {
            if (openContract == 0) {
                showOpenContractDialog()
            } else {
                ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                    putString(ParamConstant.TRANSFERSTATUS, ParamConstant.TRANSFER_CONTRACT)
                    putString(ParamConstant.TRANSFERSYMBOL, "USDT")
                })
            }
        }
        //资金明细，这是合约下面的合约账单
        assetHeadView?.ll_transfer_layout2?.setOnClickListener {
            if (openContract == 0) {
                showOpenContractDialog()
            } else {
                CpContractAssetRecordActivity.show(context as Activity)
            }
        }

//        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
//        /**
//         * 此处刷新
//         */
//        swipe_refresh?.setOnRefreshListener {
//            /**
//             * 刷新数据操作
//             */
//            getPositionList()
//        }
        showTitles.add(LanguageUtil.getString(context, "cp_order_text1"))
        showTitles.add(LanguageUtil.getString(context, "cp_order_text111"))
        fragments.add( ContractHoldFragment())
        fragments.add(ClContractAssetFragmentChild())
        val marketPageAdapter = NVPagerAdapter(childFragmentManager, showTitles.toMutableList(), fragments)
        vp_otc_asset?.adapter = marketPageAdapter
        vp_otc_asset?.offscreenPageLimit = showTitles.size
        vp_otc_asset?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
            }
        })
        try {
            stl_assets_type.setViewPager(vp_otc_asset, showTitles.toTypedArray())
        }catch(e :Exception){

        }
    }

    override fun loadData() {
        getPositionList()
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            loadContractUserConfig()
            getTotalAccountBalance()



        }
    }

    private fun getPositionList() {
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        mList.clear()
        addDisposable(getContractModel().getPositionAssetsList(
                consumer = object : NDisposableObserver(isScrollStatus) {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data")?.run {
                            if (!isNull("accountList")) {
                                val mAccountListJson = optJSONArray("accountList")
                                mList.clear()
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
                            adapterHoldContract?.notifyDataSetChanged()
                        }
//                        swipe_refresh?.isRefreshing =false
                    }
                }))
    }

    private fun getTotalAccountBalance() {
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(getMainModel().contractTotalAccountBalanceV2(
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data")?.run {
                            assetHeadView?.setContractHeadData(this)
                        }
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


    /**
     * 开通合约对话框
     */
    private fun showOpenContractDialog() {
        SlDialogHelper.showSimpleCreateContractDialog(mActivity!!, OnBindViewListener { viewHolder ->
            viewHolder?.let {
                it.getView<TextView>(R.id.tv_cancel_btn).onLineText("common_text_btnCancel")
                it.setImageResource(R.id.iv_logo, R.drawable.sl_create_contract)
                it.setText(R.id.tv_text, getLineText("sl_str_open_contract_tips"))
                it.setText(R.id.tv_confirm_btn, getLineText("sl_str_to_open"))
            }

        }, object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {

                var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
                EventBusUtil.post(messageEvent)
            }

        })
    }

    fun setRefreshAdapter() {
        assetHeadView?.setRefreshAdapter()
        refreshViewData()
        getPositionList()
    }


    private fun refreshViewData() {

        assetHeadView?.setRefreshViewData()
    }


    /**
     * 合约未平仓合约
     */
    private fun initHoldContractAdapter() {
        fl_c.addView(assetHeadView!!)
//        adapterHoldContract = ClContractAssetAdapter(context!!, mList)
//        if (assetHeadView?.parent != null) {
//            (assetHeadView?.parent as ViewGroup).removeAllViews()
//        }
//        adapterHoldContract?.setHeaderView(assetHeadView!!)
//
//        rc_contract?.layoutManager = LinearLayoutManager(context)
//        rc_contract.adapter = adapterHoldContract
//        adapterHoldContract?.setEmptyView(R.layout.item_new_empty_assets)
//        adapterHoldContract?.headerWithEmptyEnable = true
//        rc_contract?.adapter = adapterHoldContract
    }

}


