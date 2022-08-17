package com.yjkj.chainup.new_version.activity.asset

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.impl.ContractAccountListener
import com.google.android.material.appbar.AppBarLayout
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.fragment.asset.SlContractAssetFragment
import com.yjkj.chainup.contract.utils.ContractUtils
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_contract.fragment.ClContractAssetFragment
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.adapter.OTCMyAssetHeatAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.fragment_new_version_my_asset.*
import org.json.JSONObject

private const val ARG_PARAM1 = "param1"
/**
 * @Author lianshangljl
 * @Date 2019/5/14-7:49 PM
 * @Email buptjinlong@163.com
 * @description 我的资产
 *
 * NOTE:币币、法币（B2C）、场外（OTC）、合约、杠杆
 */
open class NewVersionMyAssetFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_new_version_my_asset

    val assetlist = ArrayList<JSONObject>()
    var fragments = ArrayList<Fragment>()
    var tabTitles = arrayListOf<String>()


    val showTitles = arrayListOf<String>()


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var totalBalance: String? = null
    private var legalCurrency: String? = null

    var contractAssetFragment: ClContractAssetFragment? = null

    var mSlContractAssetFragment: SlContractAssetFragment? = null


    /**
     * 场外
     */
    var otcOpen = false

    /**
     * 合约
     */
    var contractOpen = false
    var chooseIndex = 0

    /**
     * b2c
     */
    var b2cOpen = false

    /**
     * 杠杆
     */
    var leverOpen = false

    /**
     * 头部页面
     */
    var adapter4Heat: OTCMyAssetHeatAdapter? = null

    var indexList = ArrayList<String>()

    /*
     *  是否已经登录
     */
    var isLogined = false

    /**
     * 是否第一次接受合约数据
     */
    var isFristContract = true

    var contractTotal: Double = 0.0

    val ARG_INDEX = "param_index"

    private var isFromAssetsActivity = false
    open fun setFromAssetsActivity(isFromAssetsActivity: Boolean) {
        this.isFromAssetsActivity = isFromAssetsActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            chooseIndex = it.getInt(ParamConstant.CHOOSE_INDEX, 0)
        }

        ContractUserDataAgent.registerContractAccountWsListener(this, object : ContractAccountListener() {
            /**
             * 合约账户ws有更新，
             */
            override fun onWsContractAccount(contractAccount: ContractAccount?) {
                if (PublicInfoDataService.getInstance().contractOpen(null)) {
                    if (isFristContract) {
                        contractTotal = ContractUtils.calculateTotalBalance("BTC")
                        updateAsset(true)
                        isFristContract = false
                    }

                    updateContractAccount()
                }
            }

        })
        NLiveDataUtil.observeData(this, Observer {
            if (MessageEvent.hide_safety_advice == it?.msg_type) {
                rl_safety_advice.visibility = if (PreferenceManager.getBoolean(mActivity, "isShowSafetyAdviceDialog", true)) View.VISIBLE else View.GONE
//                rl_safety_advice.visibility = View.GONE
            }
        })


    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            isLogined = UserDataService.getInstance().isLogined
            if (isLogined) {
                adapter4Heat?.notifyDataSetChanged()
                setAssetViewVisible()
                getAccountBalance()
                for (fragment in fragments) {
                    if(fragment is NewVersionAssetOptimizeDetailFragment){
                        fragment.clearViewing()
                    }
                }
            }
        }
    }

    var versionAssetStatus = false
    fun activityRefresh(status: Boolean) {
        versionAssetStatus = status
        var message = MessageEvent(MessageEvent.into_my_asset_activity, versionAssetStatus)
        NLiveDataUtil.postValue(message)
    }


    fun refresh4Homepage() {
        adapter4Heat?.notifyDataSetChanged()
        if (UserDataService.getInstance().isLogined) {
            getAccountBalance()
        }
    }

    var isShowAssets = true
    private fun setAssetViewVisible() {
        isShowAssets = UserDataService.getInstance().isShowAssets
        Utils.showAssetsNewSwitch(isShowAssets, iv_hide_asset)
    }

    fun setSelectClick() {
        /**
         * 点击隐藏或者显示资金
         */
        iv_hide_asset.setOnClickListener {
            isShowAssets = !isShowAssets
            UserDataService.getInstance().setShowAssetStatus(isShowAssets)
            setAssetViewVisible()

            adapter4Heat?.notifyDataSetChanged()
            for (fragment in fragments) {
                if (fragment is ClContractAssetFragment) {
                    if (AppConstant.IS_NEW_CONTRACT) {
                        contractAssetFragment?.setRefreshAdapter()
                    } else {
                        mSlContractAssetFragment?.setRefreshAdapter()
                    }
                } else {
                    (fragment as NewVersionAssetOptimizeDetailFragment).setRefreshAdapter()
                }
            }
            refresh()
        }

        rl_safety_advice.setOnClickListener {
            SlDialogHelper.showSimpleSafetyAdviceDialog(context!!, OnBindViewListener { viewHolder ->
                viewHolder?.let {
                    it.getView<TextView>(R.id.tv_cancel_btn).onLineText("common_text_btnCancel")
                    it.setImageResource(R.id.iv_logo, R.drawable.sl_create_contract)
                    it.setText(R.id.tv_text, LanguageUtil.getString(context, "assets_security_advice_tips"))
                    it.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "alert_common_i_understand"))
                }

            }, object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    var messageEvent = MessageEvent(MessageEvent.hide_safety_advice)
                    NLiveDataUtil.postValue(messageEvent)
                }
            })
        }

        rl_safety_advice.visibility = if (PreferenceManager.getBoolean(mActivity, "isShowSafetyAdviceDialog", true)) View.VISIBLE else View.GONE

    }

    fun refresh() {
        adapter4Heat?.notifyDataSetChanged()
        if (null == totalBalance) return
        Utils.assetsHideShowJrLongData(UserDataService.getInstance().isShowAssets, tv_assets_btc_balance, totalBalance, legalCurrency)
//        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_btc_balance, totalBalance)
//        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_legal_currency_balance, legalCurrency)
    }

    override fun initView() {
        setSelectClick()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
            collapsingToolbarLayout.setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }

        otcOpen = PublicInfoDataService.getInstance().otcOpen(null)
        contractOpen = PublicInfoDataService.getInstance().contractOpen(null)
        b2cOpen = PublicInfoDataService.getInstance().getB2CSwitchOpen(null)
        leverOpen = PublicInfoDataService.getInstance().isLeverOpen(null)

        var jsonObject = JSONObject()
        jsonObject.put("title", LanguageUtil.getString(context, "otc_bibi_account"))
        jsonObject.put("totalBalanceSymbol", "BTC")
        jsonObject.put("totalBalance", "0")
        jsonObject.put("balanceType", ParamConstant.BIBI_INDEX)
        assetlist.add(jsonObject)
        val otcText = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(context, "assets_text_otc_forotc")
        } else {
            LanguageUtil.getString(context, "assets_text_otc")
        }

            if (leverOpen) {
                var jsonObject = JSONObject()
                jsonObject.put("title", LanguageUtil.getString(context, "leverage_asset"))
                jsonObject.put("totalBalanceSymbol", "BTC")
                jsonObject.put("totalBalance", "0")
                jsonObject.put("balanceType", ParamConstant.LEVER_INDEX)
                assetlist.add(jsonObject)
            }

            if (b2cOpen) {
                var jsonObject = JSONObject()
                jsonObject.put("title", LanguageUtil.getString(context, "assets_text_otc"))
                jsonObject.put("totalBalanceSymbol", "BTC")
                jsonObject.put("totalBalance", "0")
                jsonObject.put("balanceType", ParamConstant.B2C_INDEX)

                assetlist.add(jsonObject)
            }

        if (otcOpen) {
            var jsonObject = JSONObject()
            jsonObject.put("title", otcText)
            jsonObject.put("totalBalanceSymbol", "BTC")
            jsonObject.put("totalBalance", "0")
            jsonObject.put("balanceType", ParamConstant.FABI_INDEX)
            assetlist.add(jsonObject)
        }
        if (contractOpen) {
            var jsonObject = JSONObject()
            jsonObject.put("title", LanguageUtil.getString(context, "assets_text_contract"))
            jsonObject.put("totalBalanceSymbol", "USDT")
            jsonObject.put("totalBalance", "0")
            jsonObject.put("balanceType", ParamConstant.CONTRACT_INDEX)
            assetlist.add(jsonObject)
        }


        if (titleStatus) {
            rl_title_layout?.visibility = View.GONE
        }


            tabTitles.add(LanguageUtil.getString(context, "assets_text_exchange"))
            indexList.add(ParamConstant.BIBI_INDEX)
            showTitles.add(LanguageUtil.getString(context, "trade_bb_titile"))

            if (contractOpen) {
                tabTitles.add(LanguageUtil.getString(context, "assets_text_contract"))
                indexList.add(ParamConstant.CONTRACT_INDEX)
                showTitles.add(LanguageUtil.getString(context, "mainTab_text_contract"))
            }

            if (otcOpen) {
                tabTitles.add(otcText)
                indexList.add(ParamConstant.FABI_INDEX)
                showTitles.add(LanguageUtil.getString(context, "mainTab_text_otc"))
            }

            if (leverOpen) {
                tabTitles.add(LanguageUtil.getString(context, "leverage_asset"))
                indexList.add(ParamConstant.LEVER_INDEX)
                showTitles.add(LanguageUtil.getString(context, "contract_action_lever"))

            }
            if (b2cOpen) {
                tabTitles.add(LanguageUtil.getString(context, "assets_text_otc"))
                indexList.add(ParamConstant.B2C_INDEX)
                showTitles.add(LanguageUtil.getString(context, "mainTab_text_otc"))
            }



        tv_title?.text = tabTitles[0]
        for (i in 0 until tabTitles.size) {
            if (indexList[i] == ParamConstant.CONTRACT_INDEX) {
                contractAssetFragment = ClContractAssetFragment()
                mSlContractAssetFragment = SlContractAssetFragment()
                if (AppConstant.IS_NEW_CONTRACT) {
                    fragments.add(contractAssetFragment!!)
                } else {
                    fragments.add(mSlContractAssetFragment!!)
                }
                updateContractAccount()
            } else {
                fragments.add(NewVersionAssetOptimizeDetailFragment.newInstance(tabTitles[i], i, indexList[i]))
            }
        }
        adapter4Heat = OTCMyAssetHeatAdapter(assetlist)
        activity_my_asset_rv?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        activity_my_asset_rv?.adapter = adapter4Heat

        activity_my_asset_rv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var adapterNowPos = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var l: LinearLayoutManager = activity_my_asset_rv.layoutManager as LinearLayoutManager
                adapterNowPos = l.findFirstCompletelyVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                /**new State 一共有三种状态
                 * SCROLL_STATE_IDLE：目前RecyclerView不是滚动，也就是静止
                 * SCROLL_STATE_DRAGGING：RecyclerView目前被外部输入如用户触摸输入。
                 * SCROLL_STATE_SETTLING：RecyclerView目前动画虽然不是在最后一个位置外部控制。
                //这里进行加载更多数据的操作 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    vp_otc_asset?.currentItem = adapterNowPos
                }
            }

        })

        val marketPageAdapter = NVPagerAdapter(childFragmentManager, tabTitles.toMutableList(), fragments)
        vp_otc_asset?.adapter = marketPageAdapter
        vp_otc_asset?.offscreenPageLimit = tabTitles.size
        vp_otc_asset?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewpagePosotion = position
                tv_title?.text = tabTitles[position]
                activity_my_asset_rv?.smoothScrollToPosition(position)
            }
        })
        try {
            stl_assets_type.setViewPager(vp_otc_asset, showTitles.toTypedArray())
        }catch(e :Exception){

        }



        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange;
                }

                if (-verticalOffset >= 150) {
                    tv_title_top.setTextColor(Color.argb(255, 255, 255, 255))
                } else {
                    tv_title_top.setTextColor(Color.argb(-verticalOffset, 255, 255, 255))
                }

            }

        })
        appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                if (state == State.COLLAPSED) {
                    img_line.visibility = View.VISIBLE
                } else {
                    img_line.visibility = View.GONE
                }
            }
        })
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.assetsTab_type == event.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                val vpPos = msg_content.getInt(ParamConstant.assetTabType)
                setViewPagePosition(vpPos)
            }
        }
        if (MessageEvent.refresh_local_coin_trans_type == event.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                val content = msg_content.getString(ARG_INDEX)
                when (content) {
                    //币币
                    ParamConstant.BIBI_INDEX -> {
                        getAccountBalance()
                    }
                    //法币
                    ParamConstant.FABI_INDEX -> {
                        getAccountBalance4OTC()
                    }
                    //杠杆
                    ParamConstant.LEVER_INDEX -> {
                        getLeverData()
                    }
                    //B2C
                    ParamConstant.B2C_INDEX-> {
                        getB2CAccount()
                    }
                }
            }
        }
    }


    var accountBean: JSONObject = JSONObject()

    /**
     * 获取账户信息  法币
     */
    private fun getAccountBalance4OTC() {
        addDisposable(getMainModel().otc_account_list(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var t = jsonObject.optJSONObject("data")
                if (leverOpen && b2cOpen) {
                    if (assetlist.size > 3) {
                        assetlist[3].put("totalBalance", t.optString("totalBalance") ?: "")
                        assetlist[3].put("totalBalanceSymbol", t.optString("totalBalanceSymbol")
                                ?: "")
                    }
                } else if (leverOpen || b2cOpen) {
                    assetlist[2].put("totalBalance", t.optString("totalBalance") ?: "")
                    assetlist[2].put("totalBalanceSymbol", t.optString("totalBalanceSymbol")
                            ?: "")
                } else {
                    assetlist[1].put("totalBalance", t.optString("totalBalance") ?: "")
                    assetlist[1].put("totalBalanceSymbol", t.optString("totalBalanceSymbol")
                            ?: "")
                }


                if (contractOpen) {
                    getContractAccount()
                }
                refresh()

                var message = MessageEvent(MessageEvent.refresh_local_trans_type)
                message.msg_content = t
                NLiveDataUtil.postValue(message)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
            }
        }))

    }

    var isFristRequest = true

    /**
     * 获取账户信息 bibi
     */
    private fun getAccountBalance() {
        var loadingActivity = activity
        if (!isFristRequest) {
            loadingActivity = null
        }
        addDisposable(getMainModel().accountBalance(object : NDisposableObserver(loadingActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()
                isFristRequest = false

                var json = jsonObject.optJSONObject("data")
                vp_otc_asset ?: return
                activity_my_asset_rv ?: return
                accountBean = json
                assetlist.get(0).put("totalBalance", json.optString("totalBalance") ?: "")
                assetlist.get(0).put("totalBalanceSymbol", json.optString("totalBalanceSymbol")
                        ?: "")
                vp_otc_asset?.currentItem = viewpagePosotion
                activity_my_asset_rv?.smoothScrollToPosition(viewpagePosotion)
                    when {
                        leverOpen -> {
                            getLeverData()
                        }
                        b2cOpen -> {
                            getB2CAccount()
                        }
                        otcOpen -> {
                            getAccountBalance4OTC()
                        }
                        contractOpen -> {
                            getContractAccount()
                        }
                    }
                getTotalAssets()

                refresh()

                var message = MessageEvent(MessageEvent.refresh_local_coin_trans_type)
                message.msg_content = json
                NLiveDataUtil.postValue(message)

            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                isFristRequest = false
            }


        }))
    }

    private fun getContractAccount() {
        try {
            ContractUserDataAgent.getContractAccounts(true)
        } catch (e: Exception) {
        }
    }

    var titleStatus = false
    var viewpagePosotion = 0

    /**
     * 更新账户信息  合约
     */
    private fun updateContractAccount() {
        val totalBalanceSymbol = "BTC"
        val totalBalance = ContractUtils.calculateTotalBalance(totalBalanceSymbol)

            if (leverOpen && b2cOpen && otcOpen) {
                if (assetlist.size > 4) {
                    assetlist[4].put("totalBalance", totalBalance)
                    assetlist[4].put("totalBalanceSymbol", totalBalanceSymbol)
                }
            } else if ((b2cOpen && otcOpen) || (leverOpen && otcOpen) || (leverOpen && b2cOpen)) {
                if (assetlist.size > 3) {
                    assetlist[3].put("totalBalance", totalBalance)
                    assetlist[3].put("totalBalanceSymbol", totalBalanceSymbol)
                }
            } else if ((!leverOpen && !b2cOpen && otcOpen) || (!leverOpen && b2cOpen && !otcOpen) || (leverOpen && !b2cOpen && !b2cOpen)) {
                assetlist[2].put("totalBalance", totalBalance)
                assetlist[2].put("totalBalanceSymbol", totalBalanceSymbol)
            } else {
                assetlist[1].put("totalBalance", totalBalance)
                assetlist[1].put("totalBalanceSymbol", totalBalanceSymbol)
            }


        //刷新header
        refresh()
        //通知列表刷新
        if (AppConstant.IS_NEW_CONTRACT) {
            contractAssetFragment?.setRefreshAdapter()
        } else {
            mSlContractAssetFragment?.setRefreshAdapter()
        }
    }

    /**
     * 获取B2C的资产列表
     */
    private fun getB2CAccount() {
        addDisposable(getMainModel().fiatBalance(symbol = "",
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val data = jsonObject.optJSONObject("data")
                        if (leverOpen) {
                            assetlist.get(2).put("totalBalance", data.optString("totalBtcValue")
                                    ?: "")
                            assetlist.get(2).put("totalBalanceSymbol", data.optString("totalBalanceSymbol")
                                    ?: "")
                        } else {
                            assetlist.get(1).put("totalBalance", data.optString("totalBtcValue")
                                    ?: "")
                            assetlist.get(1).put("totalBalanceSymbol", data.optString("totalBalanceSymbol")
                                    ?: "")
                        }
                        val allCoinMap = data?.optJSONArray("allCoinMap")
                        if (allCoinMap != null && allCoinMap.length() > 0) {
                            val json = allCoinMap.optJSONObject(0)
                            PublicInfoDataService.getInstance().saveCoinInfo4B2C(json?.optString("symbol"))
                        }
                        if (otcOpen) {
                            getAccountBalance4OTC()
                        } else if (contractOpen) {
                            ContractUserDataAgent.getContractAccounts(true)
                        }
                        refresh()

                        var message = MessageEvent(MessageEvent.refresh_local_b2c_coin_trans_type)
                        message.msg_content = data
                        NLiveDataUtil.postValue(message)

                    }

                }))
    }

    /**
     * 获取总资产
     */
    private fun getTotalAssets() {
        addDisposable(getMainModel().getTotalAsset(
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val data = jsonObject.optJSONObject("data")
                        totalBalance = if (null == totalBalance) {
                            data.optString("totalBalance")
                        } else {
                            BigDecimalUtil.add(data.optString("totalBalance"), contractTotal.toString(), 8).toPlainString()
                        }
//                        总资产折合计算： 币币总资产+ 法币总资产 +合约总资产估值 +杠杆净资产（总资产 - 借贷资产）

                        updateAsset(false)
                    }
                }))
    }


    fun updateAsset(status: Boolean) {
        if (status) {
            totalBalance = BigDecimalUtil.add(totalBalance, contractTotal.toString(), 8).toPlainString()
        } else {
            totalBalance = BigDecimalUtil.add(totalBalance, "0", 8).toPlainString()
        }
        legalCurrency = RateManager.getCNYByCoinName("BTC", totalBalance)
        Utils.assetsHideShowJrLongData(UserDataService.getInstance().isShowAssets,tv_assets_btc_balance,totalBalance,legalCurrency)
//        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_btc_balance, totalBalance)
//        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_legal_currency_balance, legalCurrency)
    }


    fun hideTitle(status: Boolean) {
        titleStatus = status
        rl_title_layout?.visibility = View.GONE
    }

    fun setViewPagePosition(position: Int) {
        chooseIndex = position
        viewpagePosotion = position
        vp_otc_asset?.currentItem = viewpagePosotion
    }

    /**
     * 杠杆列表
     */

    fun getLeverData() {
        addDisposable(getMainModel().getBalanceList(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json: JSONObject? = jsonObject.optJSONObject("data") ?: return
                var jsonLeverMap: JSONObject? = json?.optJSONObject("leverMap") ?: return
                assetlist.get(1).put("totalBalance", json.optString("totalBalance")
                        ?: "")
                assetlist.get(1).put("totalBalanceSymbol", json.optString("totalBalanceSymbol")
                        ?: "")
                when {
                    b2cOpen -> {
                        getB2CAccount()
                    }
                    otcOpen -> {
                        getAccountBalance4OTC()
                    }
                    contractOpen -> {
                        getContractAccount()
                    }
                }
                refresh()
                var message = MessageEvent(MessageEvent.refresh_local_lever_type)
                message.msg_content = json
                NLiveDataUtil.postValue(message)
            }
        }))
    }

}
private const val ARG_PARAM2 = "param2"

