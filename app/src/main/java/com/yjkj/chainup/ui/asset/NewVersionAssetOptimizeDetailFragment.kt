package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SPUtils
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.bean.AssetScreenBean
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.HomeTabMap
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.*
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.asset.AssetsPieChartFragment
import com.yjkj.chainup.new_version.activity.asset.DepositActivity
import com.yjkj.chainup.new_version.activity.asset.WithdrawActivity
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.adapter.OTCAssetAdapter
import com.yjkj.chainup.new_version.adapter.OTCFundAdapter
import com.yjkj.chainup.new_version.contract.ContractFragment
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.home.homeAssetPieChart
import com.yjkj.chainup.new_version.view.NewAssetTopView
import com.yjkj.chainup.treaty.adapter.HoldContractAssterAdapter
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_asset.*
import kotlinx.android.synthetic.main.fragment_bibi_asset.*
import kotlinx.android.synthetic.main.fragment_bibi_asset.swipe_refresh
import org.json.JSONObject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_INDEX = "param_index"

/**
 * @Author lianshangljl
 * @Date 2019/6/9-11:25 AM
 * @Email buptjinlong@163.com
 * @description
 */
class NewVersionAssetOptimizeDetailFragment : NBaseFragment() {



    override fun setContentView() = R.layout.fragment_bibi_asset

    private var param1: String? = null

    /**
     * bibi ?????????
     * bibao ?????????
     * fabi ???otc
     */
    private var param_index: String? = null


    /**
     * ??????????????????
     */
    private var isLittleAssetsShow = false
    var assetHeadView: NewAssetTopView? = null

    private var total_balance: String = ""
    private var totalBalance: String = ""

    var fragments = ArrayList<Fragment>(2)
    var bibiDialogList = arrayListOf<String>()
    var otcDialogList = arrayListOf<String>()
    var contractDialogList = arrayListOf<String>()
    var isScrollStatus = false
    var tDialog: TDialog? = null

    var adapterHoldContract: HoldContractAssterAdapter? = null
    var nolittleBalanceList4OTC = arrayListOf<JSONObject>()
    var balanceList4OTC = arrayListOf<JSONObject>()
    var nolittleBalanceList = arrayListOf<JSONObject>()
    var balancelist = arrayListOf<JSONObject>()
    var list4OTC = arrayListOf<JSONObject>()
    var listFund = arrayListOf<JSONObject>()


    //??????
    var adapter4Asset: OTCAssetAdapter? = null
    //??????
    var adapter4Fund: OTCFundAdapter? =null
    var buffJson: JSONObject? = null
    companion object {
        var liveDataFilterForEditText: MutableLiveData<AssetScreenBean> = MutableLiveData()
        var liveDataCleanForEditText: MutableLiveData<String> = MutableLiveData()
        @JvmStatic
        fun newInstance(param1: String, index: String) =
            NewVersionAssetOptimizeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_INDEX, index)
                }
            }
    }

    override fun initView() {
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param_index = it.getString(ARG_INDEX)
        }

        when (param_index) {
            ParamConstant.BIBI_INDEX -> {
                bibiSHouyi()
            }
            ParamConstant.FABI_INDEX -> {
                otcDialogList.addAll(
                    arrayListOf(
                        LanguageUtil.getString(context, "assets_action_transfer"),
                        LanguageUtil.getString(context, "assets_action_transaction")
                    )
                )
            }
            ParamConstant.B2C_INDEX -> {
                otcDialogList.addAll(
                    arrayListOf(
                        LanguageUtil.getString(context, "assets_action_chargeCoin"),
                        LanguageUtil.getString(context, "assets_action_withdraw")
                    )
                )
            }
            ParamConstant.LEVER_INDEX -> {
                otcDialogList.addAll(
                    arrayListOf(
                        LanguageUtil.getString(context, "leverage_borrow"),
                        LanguageUtil.getString(context, "assets_action_transfer")
                    )
                )
            }
        }
        bibiDialogList.addAll(
            arrayListOf(
                LanguageUtil.getString(context, "assets_action_chargeCoin"),
                LanguageUtil.getString(context, "assets_action_transfer"),
                LanguageUtil.getString(context, "assets_action_transaction"),
                LanguageUtil.getString(context, "assets_action_withdraw")
            )
        )

        contractDialogList.add(LanguageUtil.getString(context, "assets_action_transaction"))
        assetHeadView = NewAssetTopView(activity!!, null, 0)

        assetHeadView?.initNorMalView(param_index)
        dataProcessing()
        initViewData()
        NLiveDataUtil.observeData(this, Observer {
            if (MessageEvent.refresh_trans_type == it?.msg_type) {
                isLittleAssetsShow = !isLittleAssetsShow
                UserDataService.getInstance().saveAssetState(isLittleAssetsShow)
                hideLittleAssets()
                assetHeadView?.setAssetOrderHide(isLittleAssetsShow)

            }
        })

        rc_contract.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val mLayoutManager = recyclerView.layoutManager
                val mCurrentposition = mLayoutManager?.getPosition(mLayoutManager?.getChildAt(0)!!)

                val mView = mLayoutManager?.findViewByPosition(1)
                if (mView != null) {
                    LogUtil.e("mView.top", mView.top.toString())

                }
            }
        })
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        /**
         * ??????????????????
         */
        swipe_refresh?.setOnRefreshListener {
            var messageEvent = MessageEvent(MessageEvent.refresh_local_coin_trans_type)
            messageEvent.setMsg_content(arguments)
            EventBusUtil.post(messageEvent)
            swipe_refresh?.isRefreshing = false
        }
    }
    /**
     * ????????????
     */
    private fun bibiSHouyi(){
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(
            getMainModel().accountStats(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                          var  rate = opt("rate").toString()
                            var usdt = opt("usdt").toString()
                            assetHeadView?.accountStats(rate,usdt)


                        }
                    }
                })
        )
    }



    var isFrist = true


    fun clearViewing() {
        adapter4Asset?.filter?.filter("")
        adapter4Fund?.filter?.filter("")
        assetHeadView?.clearEdittext()
        clearData()
    }



    private fun dataProcessing() {

        /**
         * ????????????
         */
        liveDataCleanForEditText.observe(this, Observer<String> {
            if (it == param_index) {
                adapter4Asset?.filter?.filter("")
                adapter4Fund?.filter?.filter("")
                clearData()
            }
        })

        /**
         * ??????
         */
        liveDataFilterForEditText.observe(this, Observer<AssetScreenBean> {
            when (it?.index4Asset) {
                ParamConstant.BIBI_INDEX -> {
                    adapter4Fund?.filter?.filter(it?.textContent)
                }
                ParamConstant.FABI_INDEX -> {
                    adapter4Asset?.filter?.filter(it?.textContent)
                }
                ParamConstant.B2C_INDEX -> {
                    adapter4Asset?.filter?.filter(it?.textContent)
                }
                ParamConstant.LEVER_INDEX -> {
                    adapter4Asset?.filter?.filter(it?.textContent)
                }
            }
        })


        /**
         * ??????????????????
         */

        NLiveDataUtil.observeData(this, Observer {
            if (MessageEvent.refresh_local_trans_type == it?.msg_type && param_index == ParamConstant.FABI_INDEX) {
                if (null != it?.msg_content) {
                    var jsonObject = it.msg_content as JSONObject
                    val allCoinMap = jsonObject?.optJSONArray("allCoinMap")
                    balanceList4OTC.clear()
                    nolittleBalanceList4OTC.clear()

                    for (item in 0 until (allCoinMap?.length() ?: 0)) {
                        var json = allCoinMap?.optJSONObject(item)
                        if (json != null) {
                            balanceList4OTC.add(json)
                            if (json.optString("btcValuation") != null && json.optDouble("btcValuation") > 0.0001) {
                                nolittleBalanceList4OTC.add(json)
                            }
                        }

                    }

                    list4OTC.clear()
                    if (isLittleAssetsShow) {
                        list4OTC.addAll(nolittleBalanceList4OTC)
                    } else {
                        list4OTC.addAll(balanceList4OTC)
                    }
                    if (isFrist) {
                        initOTCView()
                    } else {
                        adapter4Asset?.setList(list4OTC)
                    }
                    total_balance = RateManager.getCNYByCoinName(
                        jsonObject?.optString("totalBalanceSymbol"),
                        jsonObject?.optString("totalBalance"),
                        isOnlyResult = true
                    )
                    totalBalance = jsonObject?.optString("totalBalance")
                    buffJson = jsonObject
                    assetHeadView?.setHeadData(jsonObject)
                }
            }
        })




        /**
         * ???????????? ????????????
         */
        NLiveDataUtil.observeData(this, Observer { it ->
            if (MessageEvent.refresh_local_coin_trans_type == it?.msg_type && param_index == ParamConstant.BIBI_INDEX) {
                if (null != it?.msg_content) {
                    var jsonObject = it.msg_content as JSONObject
                    var json = jsonObject?.optJSONObject("allCoinMap")
                    var keys: Iterator<String> = json?.keys() as Iterator<String>
                    balancelist.clear()
                    nolittleBalanceList.clear()


                    while (keys.hasNext()) {
                        var coinMap = json?.optJSONObject(keys?.next())
                        balancelist.add(coinMap ?: JSONObject())
                        if (null != coinMap?.optString("allBtcValuatin")) {
                            if (coinMap?.optDouble("allBtcValuatin") > 0.0001) {
                                nolittleBalanceList.add(coinMap ?: JSONObject())
                            }
                        }
                    }
                    balancelist = DecimalUtil.sortByMultiOptions(balancelist, option2 = "coinName")
                    nolittleBalanceList = DecimalUtil.sortByMultiOptions(nolittleBalanceList, option2 = "coinName")

                    listFund.clear()
                    if (isLittleAssetsShow) {
                        listFund.addAll(nolittleBalanceList)
                    } else {
                        listFund.addAll(balancelist)
                    }
                    listFund.sortByDescending {
                        it?.optString("allBtcValuatin").toString().toDouble()
                    }
                    LogUtil.e(TAG, "initBiBiView() ?????? ${listFund}")
                    adapter4Fund?.setList(listFund)
                    total_balance = RateManager.getCNYByCoinName(
                        jsonObject?.optString("totalBalanceSymbol"),
                        jsonObject?.optString("totalBalance"),
                        isOnlyResult = true
                    )
                    totalBalance = jsonObject?.optString("totalBalance")
                    buffJson = jsonObject
                    assetHeadView?.setHeadData(jsonObject)
                }
            }
        })
        intoRefreshTransferView()
    }


    private fun intoRefreshTransferView() {
        assetHeadView?.listener = object : NewAssetTopView.selecetTransferListener {

            override fun selectWithdrawal(temp: String) {
                /**
                 * ????????????
                 */
                if (temp == param_index) {
                    balancelist.forEach { it ->
                        if (it?.optInt("withdrawOpen") == 1) {
                            if (phoneCertification()) return
                            if (PublicInfoDataService.getInstance().withdrawKycOpen && UserDataService.getInstance().authLevel != 1) {
                                NewDialogUtils.KycSecurityDialog(context!!,
                                    context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                        ?: "",
                                    object : NewDialogUtils.DialogBottomListener {
                                        override fun sendConfirm() {
                                            when (UserDataService.getInstance().authLevel) {
                                                0 -> {
                                                    NToastUtil.showTopToastNet(
                                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                                        false,
                                                        context?.getString(R.string.noun_login_pending)
                                                    )
                                                }

                                                2, 3 -> {
                                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                                }
                                            }
                                        }
                                    })
                                return
                            }

                            ArouterUtil.navigation(RoutePath.WithdrawSelectCoinActivity, Bundle().apply {
                                putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
                                putBoolean(ParamConstant.COIN_FROM, true)
                            })
                            return
                        }
                    }
                    NToastUtil.showTopToastNet(
                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                        false,
                        LanguageUtil.getString(context, "withdraw_tip_notavailable")
                    )
                }
            }

            override fun selectRedEnvelope(temp: String) {
                /**
                 * ????????????
                 */
                if (param_index == temp) {
                    if (PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)) {
                        if (UserDataService.getInstance().authLevel != 1 || UserDataService.getInstance().googleStatus != 1) {
                            NewDialogUtils.redPackageCondition(context ?: return)
                        }
                    } else {
                        if (UserDataService.getInstance().authLevel != 1 || (UserDataService.getInstance().googleStatus != 1 && UserDataService.getInstance().isOpenMobileCheck != 1)) {
                            NewDialogUtils.redPackageCondition(context ?: return)
                            return
                        }
                    }
                    ArouterUtil.navigation(RoutePath.CreateRedPackageActivity, null)
                }
            }

            override fun isShowAssets() {
                if (buffJson != null) {
                    assetHeadView?.setHeadData(buffJson!!)
                }
            }

            override fun clickAssetsPieChart() {
                if (nolittleBalanceList.size == 0) {
                    NewDialogUtils.showDialog(
                        context!!,
                        LanguageUtil.getString(context, "assets_balance_zero_tips"),
                        true,
                        object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                            }
                        },
                        "",
                        LanguageUtil.getString(context, "alert_common_i_understand"),
                        ""
                    )
                    return
                }
                AssetsPieChartFragment.instance.apply {
                    arguments = Bundle().apply {
                        putString("totalBalance", totalBalance)
                    }
                }.show(childFragmentManager, JsonUtils.gson.toJson(nolittleBalanceList))
            }

            override fun leverageFilter(temp: String) {
                LogUtil.e(TAG, "leverageFilter ${temp}")
                adapter4Asset?.filter?.filter(temp)
            }

            override fun fiatFilter(temp: String) {
                adapter4Asset?.filter?.filter(temp)
            }

            override fun bibiFilter(temp: String) {
                adapter4Fund?.filter?.filter(temp)
            }

            override fun b2cFilter(temp: String) {
                adapter4Asset?.filter?.filter(temp)
            }

            override fun selectTransfer(param: String) {
                if (param == param_index) {
                    when (param) {
                        ParamConstant.BIBI_INDEX -> {
                            if (PublicInfoDataService.getInstance().otcOpen(null)) {
                                var list = DataManager.getCoinsFromDB(true)
                                if (list.size == 0) {
                                    NToastUtil.showTopToastNet(
                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                        false,
                                        LanguageUtil.getString(context, "otc_not_open_transfer")
                                    )
                                    return
                                }
                                list.sortBy { it.sort }
                                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, list[0].name)

                            } else if (PublicInfoDataService.getInstance().contractOpen(null)) {
                                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_CONTRACT, "USDT")
                                return
                            }
                        }
                        ParamConstant.FABI_INDEX -> {
                            if (balanceList4OTC.size <= 0) {
                                return
                            }
                            ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_OTC, balanceList4OTC[0].optString("coinSymbol"))

                        }
                        ParamConstant.CONTRACT_INDEX -> {
                            ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_CONTRACT, "USDT")
                        }
                        ParamConstant.LEVER_INDEX -> {
                            ArouterUtil.navigation(RoutePath.CoinMapActivity, Bundle().apply {
                                putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER, true)
                                putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER_UNREFRESH, true)
                                putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER_INTO_TRANSFER, true)
                            })
                        }
                    }
                }
            }

        }
    }


    var symbol = ""


    fun clearData() {
        when (param_index) {
            ParamConstant.BIBI_INDEX -> {
                if (isLittleAssetsShow) {
                    listFund.clear()
                    listFund.addAll(nolittleBalanceList)
                    adapter4Fund?.notifyDataSetChanged()
                } else {
                    listFund.clear()
                    listFund.addAll(balancelist)
                    adapter4Fund?.notifyDataSetChanged()
                }
            }
            ParamConstant.FABI_INDEX, ParamConstant.B2C_INDEX, ParamConstant.LEVER_INDEX -> {
                if (isLittleAssetsShow) {
                    list4OTC.clear()
                    list4OTC.addAll(nolittleBalanceList4OTC)
                    adapter4Asset?.notifyDataSetChanged()
                } else {
                    list4OTC.clear()
                    list4OTC.addAll(balanceList4OTC)
                    adapter4Asset?.notifyDataSetChanged()
                }
            }

        }
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            clearViewing()
            if (buffJson != null) {
                assetHeadView?.setHeadData(buffJson!!)
                setRefreshAdapter()
            }

        }
    }




    override fun onResume() {
        super.onResume()
        assetHeadView?.clearEdittext()
    }

    private fun setRefreshAdapter() {
        assetHeadView?.setRefreshViewData()
        buffJson?.let { assetHeadView?.setHeadData(it) }
        //??????????????????/????????????   ???????????????????????????
        refreshViewData()
    }

    fun hideLittleAssets() {
        LogUtil.e(TAG, "hideLittleAssets $isLittleAssetsShow  type $param_index  ")
        if (isLittleAssetsShow) {
            //????????????????????????

            LogUtil.e(TAG, "hideLittleAssets  ${nolittleBalanceList.size}  ${nolittleBalanceList4OTC.size} ")
            //????????????????????????
            if (param_index == ParamConstant.BIBI_INDEX) {
                listFund.clear()
                listFund.addAll(nolittleBalanceList)
                adapter4Fund?.setList(listFund)
            } else {
                if (param_index == ParamConstant.LEVER_INDEX || param_index == ParamConstant.FABI_INDEX) {
                    list4OTC.clear()
                    list4OTC.addAll(nolittleBalanceList4OTC)
//                    adapter4Asset?.notifyDataSetChanged()
                    adapter4Asset?.setList(list4OTC)
                }
            }
        } else {
            if (param_index == ParamConstant.BIBI_INDEX) {
                listFund.clear()
                listFund.addAll(balancelist)
                adapter4Fund?.setList(listFund)
                LogUtil.e(TAG, "hideLittleAssets  ${balancelist.size}  ${balanceList4OTC.size} ")
            } else {
                if (param_index == ParamConstant.LEVER_INDEX || param_index == ParamConstant.FABI_INDEX) {
                    list4OTC.clear()
                    list4OTC.addAll(balanceList4OTC)
//                    adapter4Asset?.notifyDataSetChanged()
                    adapter4Asset?.setList(list4OTC)
                    LogUtil.e(TAG, "hideLittleAssets  ${balancelist.size}  ${balanceList4OTC.size} ")
                }
            }
        }

        adapter4Fund?.setListener(object : OTCFundAdapter.FilterListener {
            override fun getFilterData(list: List<JSONObject>) {
                listFund.clear()
                listFund.addAll(list)
                adapter4Fund!!.notifyDataSetChanged()

            }
        })
        adapter4Asset?.setListener(object : OTCAssetAdapter.FilterListener {
            override fun getFilterData(list: ArrayList<JSONObject>) {
                list4OTC.clear()
                list4OTC.addAll(list)
                adapter4Asset!!.notifyDataSetChanged()
            }
        })

    }


    @SuppressLint("NotifyDataSetChanged")
    fun refreshViewData() {
        isLittleAssetsShow = UserDataService.getInstance().assetState
        adapter4Asset?.notifyDataSetChanged()
        adapter4Fund?.notifyDataSetChanged()
        adapterHoldContract?.notifyDataSetChanged()
        when (param_index) {
            ParamConstant.CONTRACT_INDEX -> {
                getContractAccount()
                holdContractList4Contract()
            }
        }
    }


    private fun initViewData() {
        isLittleAssetsShow = UserDataService.getInstance().getAssetState()
        nolittleBalanceList4OTC.clear()
        balanceList4OTC.clear()
        nolittleBalanceList.clear()
        balancelist.clear()
        list4OTC.clear()
        listFund.clear()
        if (UserDataService.getInstance().isLogined) {
            when (param_index) {
                ParamConstant.BIBI_INDEX -> {
                    initBiBiView()
                }
                ParamConstant.FABI_INDEX -> {
                    initOTCView()
                }
                ParamConstant.CONTRACT_INDEX -> {
                    getContractAccount()
                    holdContractList4Contract()
                }

            }
        }





    }





    /**
     * ????????????adapter
     */
    private fun initOTCView() {
        if (activity?.isFinishing ?: return || activity?.isDestroyed ?: return || !isAdded) {
            return
        }

        isFrist = false
        if (isLittleAssetsShow) {
            list4OTC.addAll(nolittleBalanceList4OTC)
        } else {
            list4OTC.addAll(balanceList4OTC)
        }

        val parent = assetHeadView?.parent
        if (null != parent) {
            (parent as ViewGroup).removeAllViews()
        }
        adapter4Asset= OTCAssetAdapter(list4OTC)
        adapter4Asset!!.setType(ParamConstant.FABI_INDEX)
        adapter4Asset!!.setHeaderView(assetHeadView!!)
        adapter4Asset!!.setHasStableIds(true)
        if (rc_contract == null) return
        rc_contract?.layoutManager = LinearLayoutManager(context)
        adapter4Asset!!.setEmptyView(R.layout.item_new_empty_assets)
        adapter4Asset!!.headerWithEmptyEnable = true
        rc_contract?.adapter = adapter4Asset
        rc_contract?.itemAnimator = null

        adapter4Asset!!.setOnItemClickListener { _, _, position ->
            tDialog =
                NewDialogUtils.showBottomListDialog(context!!, otcDialogList, 0, object : NewDialogUtils.DialogOnclickListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        //??????
                        if (position >= list4OTC.size) {
                            tDialog?.dismiss()
                            adapter4Asset!!.notifyDataSetChanged()
                            return
                        }
                        when (item) {
                            /**
                             * ??????
                             */
                            0 -> {

                                var aa: String? = ""
                                aa = NCoinManager.getShowMarket(list4OTC[position].optString("coinSymbol"))
                                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_OTC, aa)

                            }
                            /**
                             * ??????
                             */
                            1 -> {
                                if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                                    ToastUtils.showToast(context?.getString(R.string.important_hint1))
                                    return
                                }
                                ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
                                assetsActivityFinish()
                            }
                        }
                        tDialog?.dismiss()
                    }
                })
        }

        adapter4Asset!!.setListener(object : OTCAssetAdapter.FilterListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun getFilterData(list: ArrayList<JSONObject>) {
                list4OTC.clear()
                list4OTC.addAll(list)
                adapter4Asset!!.notifyDataSetChanged()
            }
        })
    }






    fun setEvent(position: Int, content: String) {
        val message = MessageEvent(MessageEvent.coin_payment)
        val jsonObject = JSONObject()
        jsonObject.put("position", position)
        jsonObject.put("content", content)
        message.msg_content = jsonObject
        EventBusUtil.post(message)

    }

    /**
     * ???????????? item
     */
    private fun initBiBiView() {
        rc_contract.removeAllViews()
        if (isLittleAssetsShow) {
            listFund.addAll(nolittleBalanceList)
        } else {
            listFund.addAll(balancelist)
        }
        LogUtil.e(TAG, "initBiBiView() ${listFund}")
        var parent = assetHeadView?.parent
        if (null != parent) {
            (parent as ViewGroup).removeAllViews()
        }
        adapter4Fund=  OTCFundAdapter(listFund)
        adapter4Fund!!.setHeaderView(assetHeadView!!)
        rc_contract?.layoutManager = LinearLayoutManager(context)
        adapter4Fund!!.setHasStableIds(true)
        adapter4Fund!!.setEmptyView(R.layout.item_new_empty_assets)
        adapter4Fund!!.headerWithEmptyEnable = true
        rc_contract?.adapter = adapter4Fund
        rc_contract?.itemAnimator = null

        adapter4Fund!!.setOnItemClickListener { adapter, view, position ->
            bibiDialogList = arrayListOf(
                LanguageUtil.getString(context, "assets_action_chargeCoin"),
                LanguageUtil.getString(context, "assets_action_transfer"),
                LanguageUtil.getString(context, "assets_action_transaction"),
                LanguageUtil.getString(context, "otc_withdraw")
            )

            val coin = PublicInfoDataService.getInstance().getCoinByName(listFund[position].optString("coinName", ""))
            var existMarket: String? = null
            existMarket = NCoinManager.returnExistMarket(listFund[position].optString("exchange_symbol", ""))


            bibiDialogList.clear()
            if (listFund[position].optInt("withdrawOpen") == 1) {
                bibiDialogList.add(LanguageUtil.getString(context, "assets_action_withdraw"))
                if (listFund[position].optInt("innerTransferOpen") == 1) {
                    bibiDialogList.add(LanguageUtil.getString(context, "assets_action_internalTransfer"))
                }
            }
            if (coin?.optInt("otcOpen") == 1) {
                bibiDialogList.add(LanguageUtil.getString(context, "assets_action_transfer"))
            }
            if (null != existMarket && existMarket.isNotEmpty()) {
                bibiDialogList.add(LanguageUtil.getString(context, "assets_action_transaction"))
            }
            if (listFund[position].optInt("depositOpen") == 1) {
                bibiDialogList.add(LanguageUtil.getString(context, "assets_action_chargeCoin"))
            }
            if (bibiDialogList.size == 0) {
                return@setOnItemClickListener
            }

            tDialog =
                NewDialogUtils.showBottomListDialog(context!!, bibiDialogList, 0, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        if (position >= listFund.size) {
                            tDialog?.dismiss()
                            adapter4Fund?.notifyDataSetChanged()
                            return
                        }
                        when (data[item]) {
                            /**
                             * ??????
                             */
                            LanguageUtil.getString(context, "assets_action_chargeCoin") -> {
                                if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                                    ToastUtils.showToast(context?.getString(R.string.important_hint1))
                                    return
                                }
                                if (listFund[position]?.optInt("depositOpen") == 1) {
                                    if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
                                        NewDialogUtils.KycSecurityDialog(context!!,
                                            context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                                ?: "",
                                            object : NewDialogUtils.DialogBottomListener {
                                                override fun sendConfirm() {
                                                    when (UserDataService.getInstance().authLevel) {
                                                        0 -> {
                                                            NToastUtil.showTopToastNet(
                                                                this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                                                false,
                                                                context?.getString(R.string.noun_login_pending)
                                                            )
                                                        }

                                                        2, 3 -> {
                                                            ArouterUtil.greenChannel(
                                                                RoutePath.RealNameCertificationActivity,
                                                                null
                                                            )
                                                        }
                                                    }
                                                }
                                            })
                                        return
                                    }
                                    DepositActivity.enter2(context!!, listFund[position].optString("coinName", ""))
                                } else {
                                    //DisplayUtil.showSnackBar(activity?.window?.decorView, LanguageUtil.getString(context,warn_no_support_recharge), isSuc = false)
                                    NToastUtil.showTopToastNet(
                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                        false,
                                        LanguageUtil.getString(context, "warn_no_support_recharge")
                                    )
                                }
                            }
                            /**
                             * ??????
                             */
                            LanguageUtil.getString(context, "assets_action_withdraw") -> {
                                if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                                    ToastUtils.showToast(context?.getString(R.string.important_hint1))
                                    return
                                }
                                if (listFund[position].optInt("withdrawOpen") == 1) {
                                    if (phoneCertification()) return
                                    if (PublicInfoDataService.getInstance().withdrawKycOpen && UserDataService.getInstance().authLevel != 1) {
                                        NewDialogUtils.KycSecurityDialog(context!!,
                                            context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                                ?: "",
                                            object : NewDialogUtils.DialogBottomListener {
                                                override fun sendConfirm() {
                                                    when (UserDataService.getInstance().authLevel) {
                                                        0 -> {
                                                            NToastUtil.showTopToastNet(
                                                                this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                                                false,
                                                                context?.getString(R.string.noun_login_pending)
                                                            )
                                                        }

                                                        2, 3 -> {
                                                            ArouterUtil.greenChannel(
                                                                RoutePath.RealNameCertificationActivity,
                                                                null
                                                            )
                                                        }
                                                    }
                                                }
                                            })
                                        return
                                    }
                                    WithdrawActivity.enter2(context!!, listFund[position].toString())
                                } else {
                                    //DisplayUtil.showSnackBar(activity?.window?.decorView, LanguageUtil.getString(context,warn_no_support_withdraw), isSuc = false)
                                    NToastUtil.showTopToastNet(
                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                        false,
                                        LanguageUtil.getString(context, "warn_no_support_withdraw")
                                    )
                                }
                            }
                            /**
                             * ??????
                             */
                            LanguageUtil.getString(context, "assets_action_transfer") -> {
                                if (PublicInfoDataService.getInstance().otcOpen(null)) {
                                    var coin = PublicInfoDataService.getInstance()
                                        .getCoinByName(listFund[position].optString("coinName", ""))
                                    if (coin != null && coin.optInt("otcOpen") == 1) {
                                        ArouterUtil.forwardTransfer(
                                            ParamConstant.TRANSFER_BIBI,
                                            listFund[position].optString("coinName", "")
                                        )

                                    } else {
                                        NToastUtil.showTopToastNet(
                                            this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                            false,
                                            LanguageUtil.getString(context, "otc_not_open_transfer")
                                        )

                                    }
                                } else if (PublicInfoDataService.getInstance().contractOpen(null)) {
                                    ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_CONTRACT, "BTC")

                                } else {
                                    NToastUtil.showTopToastNet(
                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                        false,
                                        LanguageUtil.getString(context, "otc_not_open_transfer")
                                    )
                                }


                            }
                            /**
                             * ??????
                             */
                            LanguageUtil.getString(context, "assets_action_transaction") -> {
                                if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                                    ToastUtils.showToast(context?.getString(R.string.important_hint1))
                                    return
                                }
                                var existMarket: String? = ""
                                if (null != listFund[position].optString("exchange_symbol")) {
                                    existMarket =
                                        NCoinManager.returnExistMarket(listFund[position].optString("exchange_symbol", "")!!)

                                }

                                if (!StringUtil.checkStr(existMarket)) {
                                    NToastUtil.showTopToastNet(
                                        this@NewVersionAssetOptimizeDetailFragment.mActivity,
                                        false,
                                        LanguageUtil.getString(context, "warn_no_support_trade")
                                    )
                                    return
                                }

                                setEvent(ParamConstant.TYPE_COIN, "TradingBean")
                                SymbolManager.instance.saveTradeSymbol(existMarket, 0)


                                var messageEvent = MessageEvent(MessageEvent.symbol_switch_type)
                                messageEvent.msg_content = existMarket
                                messageEvent.isLever = false
                                EventBusUtil.post(messageEvent)


                                forwardCoinTradeTab(existMarket)
                                assetsActivityFinish()
                            }
                            /**
                             * ????????????
                             */
                            LanguageUtil.getString(context, "assets_action_internalTransfer") -> {
                                if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                                    ToastUtils.showToast(context?.getString(R.string.important_hint1))
                                    return
                                }
                                if (phoneCertification()) return
                                ArouterUtil.navigation(RoutePath.DirectlyWithdrawActivity, Bundle().apply {
                                    putString(ParamConstant.JSON_BEAN, listFund[position].toString())
                                })

                            }
                        }
                        tDialog?.dismiss()
                    }
                })

        }
        adapter4Fund!!.setOnItemChildClickListener { adapter, view, position ->
            val coinName = listFund[position].optString("coinName", "")
            val isDeposit = (listFund[position].optInt("depositOpen") == 1)
            val isWithdraw = (listFund[position].optInt("withdrawOpen") == 1)
            showSuspendRechargeWithdrawalDialog(coinName, isDeposit, isWithdraw)

        }
        adapter4Fund!!.setListener(object : OTCFundAdapter.FilterListener {
            override fun getFilterData(list: List<JSONObject>) {
                if (list == null) return
                listFund.clear()
                listFund.addAll(list)
                adapter4Fund?.notifyDataSetChanged()
            }
        })

    }

    /*
     * ???????????????tab
     */
    private fun forwardCoinTradeTab(symbol: String?) {
        var messageEvent = MessageEvent(MessageEvent.hometab_switch_type)
        val bundle = Bundle()
        val homeTabType = HomeTabMap.maps.get(HomeTabMap.coinTradeTab) ?: 2
        bundle.putInt(ParamConstant.homeTabType, homeTabType)//???????????????????????????
        bundle.putInt(ParamConstant.transferType, ParamConstant.TYPE_BUY)
        bundle.putString(ParamConstant.symbol, symbol)
        messageEvent.msg_content = bundle
        EventBusUtil.post(messageEvent)
    }


    var contractList: ArrayList<JSONObject> = arrayListOf()
    var isFristContract = true

    /**
     * ?????????????????????
     */
    fun initHoldContractAdapter(list: ArrayList<JSONObject>) {
        contractList.clear()
        contractList.addAll(list)
        adapterHoldContract = HoldContractAssterAdapter(contractList)
        if (assetHeadView?.parent != null) {
            (assetHeadView?.parent as ViewGroup)?.removeAllViews()
        }
        adapterHoldContract?.setHeaderView(assetHeadView!!)

        rc_contract?.layoutManager = LinearLayoutManager(context)

        rc_contract?.adapter = adapterHoldContract
        adapterHoldContract?.setOnItemClickListener { adapter, view, position ->

            tDialog = NewDialogUtils.showBottomListDialog(
                context!!,
                contractDialogList,
                0,
                object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        /*NewMainActivity.liveData4Position.postValue(
                                if (PublicInfoDataService.getInstance().otcOpen(null)) {
                                    4
                                } else {
                                    3
                                }
                        )*/
                        hometab_switch()

                        Contract2PublicInfoManager.currentContractId(contractList[position]?.optInt("contractId"), true)
                        ContractFragment.liveData4Contract.postValue(Contract2PublicInfoManager.currentContract())

                        tDialog?.dismiss()
                    }
                })
        }
    }

    private fun hometab_switch() {
        /*var messageEvent = MessageEvent(MessageEvent.hometab_switch_type)
        if (PublicInfoDataService.getInstance().otcOpen(null)) {
            messageEvent.msg_content = 4
        } else {
            messageEvent.msg_content = 3
        }
        NLiveDataUtil.postValue(messageEvent)*/

        var messageEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        val homeTabType = HomeTabMap.maps.get(HomeTabMap.contractTab) ?: 3
        bundle.putInt(ParamConstant.homeTabType, homeTabType)
        //NLiveDataUtil.postValue(messageEvent)
        messageEvent.msg_content = bundle
        EventBusUtil.post(messageEvent)

    }

    fun refreshContractAdapter(list: ArrayList<JSONObject>) {
        contractList.clear()
        contractList.addAll(list)
        adapterHoldContract?.notifyDataSetChanged()
    }


    /**
     * ??????????????????  ??????
     */
    private fun getContractAccount() {

        addDisposable(getMainModel().getAccountBalance4Contract(object : NDisposableObserver(activity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var jsonarray = jsonObject.optJSONArray("data")
                assetHeadView?.initAdapterView(jsonarray)
            }

        }))
    }



    var isFristRequest = true

    /**
     * ???????????????????????????  ??????
     */
    private fun holdContractList4Contract() {
        if (isFristRequest) {
            showLoadingDialog()
        }
        addDisposable(getContractModelOld().holdContractList4Contract(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (isFristRequest) {
                    isFristRequest = false
                    closeLoadingDialog()
                }
                val json = jsonObject.optJSONArray("data")
                if (null == json || json.length() == 0) {
                    initHoldContractAdapter(arrayListOf())
                    return
                }
                val obj: ArrayList<JSONObject> = ArrayList()
                for (num in 0 until json.length()) {
                    obj.add(json.optJSONObject(num))
                }

                if (isFristContract) {
                    initHoldContractAdapter(obj)
                    isFristContract = false
                } else {
                    refreshContractAdapter(obj)
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                if (isFristRequest) {
                    isFristRequest = false
                    closeLoadingDialog()
                }
            }
        }))
    }


    fun phoneCertification(type: Int = 2): Boolean {
        if (PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)) {
            if (UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingMustPermissionsDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //???????????? 0???????????????1????????????2????????????  3?????????
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }
                }, type = type, title = LanguageUtil.getString(context, "withdraw_tip_bindGoogleFirst"))
                return true
            }
        } else {
            if (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingPermissionsDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //???????????? 0???????????????1????????????2????????????  3?????????
                            //.enter2(context!!)
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }
                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }

                }, type = 2, title = LanguageUtil.getString(context, "otcSafeAlert_action_bindphoneOrGoogle"))
                return true
            }
        }

        return false
    }

    private fun showSuspendRechargeWithdrawalDialog(coinName: String, isDeposit: Boolean, isWithdraw: Boolean) {
        SlDialogHelper.showSuspensionChargingDialog(mActivity!!, OnBindViewListener { viewHolder ->
            viewHolder?.let {
                it.getView<TextView>(R.id.tv_cancel_btn).visibility = View.GONE
                var operationType = ""
                if (!isDeposit && !isWithdraw) {
                    operationType = LanguageUtil.getString(context, "assets_suspend_deposit_Withdraw")
                } else if (!isDeposit) {
                    operationType = LanguageUtil.getString(context, "assets_suspend_deposit")
                } else {
                    operationType = LanguageUtil.getString(context, "assets_suspend_withdraw")
                }
                it.setText(R.id.tv_text, coinName + " " + operationType)
                it.setText(R.id.tv_confirm_btn, getLineText("alert_common_i_understand"))
            }

        }, object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {

            }

        })
    }

    /*
      * ??????????????????finish
      */
    private fun assetsActivityFinish() {
        var msgEvent = MessageEvent(MessageEvent.assets_activity_finish_event)
        EventBusUtil.post(msgEvent)
    }


}