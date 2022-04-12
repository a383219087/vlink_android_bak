package com.yjkj.chainup.new_version.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.core.widget.NestedScrollView
import android.text.TextUtils
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.*
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.*
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.personalCenter.MailActivity
import com.yjkj.chainup.new_version.activity.personalCenter.NoticeActivity
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.home.adapter.ImageNetAdapter
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.VerticalTextview4ChainUp
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.listener.OnBannerListener
import kotlinx.android.synthetic.main.activity_home_title.*
import kotlinx.android.synthetic.main.fragment_new_home_asset_login_japan.*
import kotlinx.android.synthetic.main.fragment_new_home_asset_nologin_japan.*
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.*
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.banner_looper
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.fragment_market
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.iv_close_red_envelope
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.iv_nation_more
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.ll_advertising_layout
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.rl_red_envelope_entranc_layout
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.rl_red_envelope_entrance
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.stl_homepage_list
import kotlinx.android.synthetic.main.fragment_new_home_page_japan.vtc_advertising
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-01-15-15:21
 * @Email buptjinlong@163.com
 * @description
 */
class NewVersionJapanHomepageFragment : NBaseFragment(), MarketWsData.RefreshWSListener {
    override fun onRefreshWS(pos: Int) {

    }

    val homepageReqType = 2 // 首页数据请求
    val accountBalanceReqType = 5 //币币请求

    /**
     * 是否开启场外
     */
    private var otcOpen = false

    private var leverOpen = false

    /**
     * 是否开启合约
     */
    private var contractOpen = false

    private var isAssetsShow = true


    /*
     *  是否已经登录
     */
    var isLogined = false

    override fun setContentView() = R.layout.fragment_new_home_page_japan

    override fun initView() {

        otcOpen = PublicInfoDataService.getInstance().otcOpen(null)
        leverOpen = PublicInfoDataService.getInstance().isLeverOpen(null)
        contractOpen = PublicInfoDataService.getInstance().contractOpen(null)

        setTopBar()
        initRedPacketView()
        setOnClick()

        LogUtil.d(TAG, "切换语言==NewVersionHomepageFragment==")
    }

    /*
     * 初始化红包view
     */
    private fun initRedPacketView() {
        val isRedPacketOpen = PublicInfoDataService.getInstance().isRedPacketOpen(null)
        showRedPacket(isRedPacketOpen)
    }


    inner class MyNDisposableObserver(type: Int) : NDisposableObserver() {

        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            closeLoadingDialog()
            if (homepageReqType == req_type) {
                showHomepageData(jsonObject.optJSONObject("data"))
            } else if (accountBalanceReqType == req_type) {
                showAccountBalance(jsonObject.optJSONObject("data"))
            }
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            closeLoadingDialog()
        }
    }


    var homepageData: JSONObject? = null

    /*
     * 首页数据展示
     */
    private fun showHomepageData(data: JSONObject?) {
        LogUtil.d("NewVersionHomepageFragment", "showHomepageData==data is $data")
        if (null == data)
            return
        homepageData = data
        var noticeInfoList = data.optJSONArray("noticeInfoList")
        var cmsAppAdvertList = data.optJSONArray("cmsAppAdvertList")

        LogUtil.d("NewVersionHomepageFragment", "showHomepageData==cmsAppAdvertList is $noticeInfoList")
        showGuanggao(noticeInfoList)
        showBannerData(cmsAppAdvertList)
        showBottomVp(data)

    }

    /*
     * 展示顶部轮播图
     */
    var bannerImgUrls: ArrayList<String> = ArrayList<String>()

    private fun showBannerData(cmsAppAdvertList: JSONArray?) {


        bannerImgUrls = ArrayList<String>()
        if (null != cmsAppAdvertList && cmsAppAdvertList.length() != 0) {
            for (i in 0 until cmsAppAdvertList.length()) {
                var obj = cmsAppAdvertList.optJSONObject(i)
                var imageUrl = obj.optString("imageUrl")
                if (StringUtil.isHttpUrl(imageUrl)) {
                    bannerImgUrls.add(imageUrl)
                }
            }
        }

        //设置图片集合
        banner_looper?.apply {
            //设置图片集合
            val mAdapter = ImageNetAdapter(bannerImgUrls)
            adapter = mAdapter
            //设置轮播时间
            setLoopTime(3000)
            //设置指示器位置（当banner模式中有指示器时）
            setIndicatorGravity(IndicatorConfig.Direction.CENTER)
        }
        banner_looper?.setOnBannerListener(object : OnBannerListener<String> {
            override fun OnBannerClick(data: String?, position: Int) {
                if (null == cmsAppAdvertList || cmsAppAdvertList.length() == 0) return
                var obj = cmsAppAdvertList.optJSONObject(position)
                var httpUrl = obj?.optString("httpUrl") ?: ""
                var nativeUrl = obj?.optString("nativeUrl") ?: ""

                //TODO 需要一个标题
                if (TextUtils.isEmpty(httpUrl)) {
                    if (StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
                        enter2Activity(nativeUrl?.split("?"))
                    }
                } else {
                    forwardWeb(obj)
                }
            }
        })
        //banner设置方法全部调用完毕时最后调用
        banner_looper?.start()

    }

    /*
     * 首页账户币币数据展示
     */
    private fun showAccountBalance(data: JSONObject?) {
        if (null == data)
            return

        var totalBalance = data.optString("totalBalance", "")
        var totalBalanceSymbol = "BTC"
        tv_total_amount_title?.text = LanguageUtil.getString(context, "home_text_assets")
        accountFlat = RateManager.getCNYByCoinName(totalBalanceSymbol, totalBalance)
        accountBalance = "0"
        if (StringUtil.checkStr(totalBalance)) {
            accountBalance = BigDecimalUtils.divForDown(totalBalance, NCoinManager.getCoinShowPrecision(totalBalanceSymbol)).toPlainString()
        }

        setAssetViewVisible()

    }

    private fun showAdvertising(isShow: Boolean) {
        if (null != vtc_advertising?.textList && vtc_advertising?.textList!!.size > 0) {
            if (isShow) {
                vtc_advertising?.startAutoScroll()
            } else {
                vtc_advertising?.stopAutoScroll()
            }
        }
    }

    private fun showBanner(isShow: Boolean) {
        if (null != bannerImgUrls && bannerImgUrls!!.size > 0) {
            if (isShow) {
                banner_looper?.start()
            } else {
                banner_looper?.stop()
            }
        }
    }


    private fun getAllAccounts() {
        isLogined = UserDataService.getInstance().isLogined

        if (null == homepageData) {
            getHomepageData()
        }
        setAssetViewVisible()

        if (isLogined) {
            fragment_new_home_asset_unlogin_japan?.visibility = View.GONE
            fragment_new_home_asset_login_japan?.visibility = View.VISIBLE
            getAccountBalance()
        } else {
            fragment_new_home_asset_unlogin_japan?.visibility = View.VISIBLE
            fragment_new_home_asset_login_japan?.visibility = View.GONE
        }
    }


    fun setTopBar() {
        ns_layout?.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distance = resources.getDimension(R.dimen.dp_64)
            if ((1 - scrollY / distance) < (0.0001)) {
                item_view_market_line.visibility = View.VISIBLE
            } else {
                item_view_market_line.visibility = View.INVISIBLE
            }
        }
    }

    /*
     * 资产tab跳转
     */
    private fun homeAssetstab_switch(type: Int) {
        var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        var homeTabType = HomeTabMap.maps.get(HomeTabMap.assetsTab) ?: 4
        bundle.putInt(ParamConstant.homeTabType, homeTabType)
        bundle.putInt(ParamConstant.assetTabType, type)
        msgEvent.msg_content = bundle
        EventBusUtil.post(msgEvent)
    }

    /*
     * 首页底部tab跳转的处理
     */
    private fun homeTabSwitch(tabType: Int?) {
        var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        bundle.putInt(ParamConstant.homeTabType, tabType ?: 0)
        msgEvent.msg_content = bundle
        EventBusUtil.post(msgEvent)
    }

    /*
     * 跳转至 NewVersionMyAssetActivity
     */
    private fun forwardAssetsActivity(type: Int) {
        var bundle = Bundle()
        bundle.putInt(ParamConstant.assetTabType, type)//跳转到币币交易页面
        ArouterUtil.navigation(RoutePath.NewVersionMyAssetActivity, bundle)
    }

    fun setOnClick() {

        /**
         * 隐藏或者显示资产
         */
        iv_hide_asset_japan?.setOnClickListener {
            isAssetsShow = !isAssetsShow
            UserDataService.getInstance().setShowAssetStatus(isAssetsShow)
            setAssetViewVisible()
        }

        /**
         * 个人中心
         */
        iv_personal_logo?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.PersonalCenterActivity, null)
        }

        layout_search?.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.CoinMapActivity, Bundle().apply {
                putString("type", ParamConstant.ADD_COIN_MAP)
            })
        }
        iv_market_msg?.setOnClickListener {
            if (LoginManager.checkLogin(mActivity, true)) {
                startActivity(Intent(mActivity, MailActivity::class.java))
            }
        }

        iv_nation_more?.setOnClickListener {
//            if (LoginManager.checkLogin(mActivity, true)) {
                startActivity(Intent(mActivity, NoticeActivity::class.java))
//            }
        }

        /**
         * 登录
         */
        btn_login?.setOnClickListener {
            LoginManager.checkLogin(activity, true)
        }

        /**
         * 币币账户
         */
        rl_japan_asset_layout?.setOnClickListener {
            if (contractOpen && otcOpen) {
                forwardAssetsActivity(0)
            } else {
                homeAssetstab_switch(AssetsEnum.COIN_ACCOUNT.value)
            }
        }



        if (SystemUtils.isZh()) {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope)
        } else {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope_english)
        }

        /**
         * 点击红包 跳转
         */
        rl_red_envelope_entrance?.setOnClickListener {
            if (!LoginManager.checkLogin(activity, true)) {
                return@setOnClickListener
            }

            var isEnforceGoogleAuth = PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)

            var authLevel = UserDataService.getInstance().authLevel
            var googleStatus = UserDataService.getInstance().googleStatus
            var isOpenMobileCheck = UserDataService.getInstance().isOpenMobileCheck
            if (isEnforceGoogleAuth) {
                if (authLevel != 1 || (googleStatus != 1 && isOpenMobileCheck != 1)) {
                    NewDialogUtils.redPackageCondition(context ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            } else {
                if (authLevel != 1 || isOpenMobileCheck != 1) {
                    NewDialogUtils.redPackageCondition(context ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            }
            ArouterUtil.navigation(RoutePath.CreateRedPackageActivity, null)
        }

        /**
         * 点击关闭红包
         */
        iv_close_red_envelope?.setOnClickListener {
            showRedPacket(false)
        }
    }


    private fun showRedPacket(isVisibile: Boolean) {
        if (isVisibile) {
            rl_red_envelope_entranc_layout?.visibility = View.VISIBLE
        } else {
            rl_red_envelope_entranc_layout?.visibility = View.GONE
        }
    }


    override fun fragmentVisibile(isVisible: Boolean) {
        super.fragmentVisibile(isVisible)
        LogUtil.d(TAG, "切换语言==NewVersionHomepageFragment==")
        LogUtil.d(TAG, "fragmentVisibile==NewVersionHomepageFragment==isVisible is $isVisible")
        if (isVisible) {
            getAllAccounts()
            setAssetViewVisible()
        }
        showAdvertising(isVisible)
        showBanner(isVisible)
    }


    fun showBottomVp(data: JSONObject) {

        var chooseType = arrayListOf<String>()
        val fragments = arrayListOf<Fragment>()
        var titles = arrayListOf<String>()

        var risingListIsOpen = data.optString("risingListIsOpen", "")
        var fallingListIsOpen = data.optString("fallingListIsOpen", "")
        var dealListIsOpen = data.optString("dealListIsOpen", "")

        if ("1" == risingListIsOpen) {
            titles.add(LanguageUtil.getString(context, "home_text_upRanking"))
            chooseType.add("rasing")
        }
        if ("1" == fallingListIsOpen) {
            titles.add(LanguageUtil.getString(context, "home_text_downRanking"))
            chooseType.add("falling")

        }
        if ("1" == dealListIsOpen) {
            titles.add(LanguageUtil.getString(context, "home_text_dealRanking"))
            chooseType.add("deal")
        }

        if (titles.isEmpty())
            return

        for (i in 0 until titles.size) {
            fragments.add(NewHomeJapanDetailFragment.newInstance(titles[i], i, chooseType[i], fragment_market))
        }

        var marketPageAdapter = NVPagerAdapter(childFragmentManager, titles, fragments)

        fragment_market?.adapter = marketPageAdapter
        fragment_market?.offscreenPageLimit = fragments.size
        fragment_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                fragment_market?.resetHeight(position)
            }
        })
        stl_homepage_list?.setViewPager(fragment_market, titles.toTypedArray())
    }

    /**
     * 设置资金显示
     */

    fun setAssetViewVisible() {
        if (!isLogined) {
            return
        }
        isAssetsShow = UserDataService.getInstance().isShowAssets

        Utils.showAssetsNewSwitch(isAssetsShow, iv_hide_asset_japan)
        //币币
        Utils.assetsHideShow(isAssetsShow, tv_assets_btc_balance, accountBalance)
        Utils.assetsHideShow(isAssetsShow, tv_assets_legal_currency_balance, accountFlat)


    }

    /**
     * 获取滚动的广告
     */
    fun getNoticeInfoList(notcieList: JSONArray): ArrayList<String> {
        var noticeList4String: ArrayList<String> = arrayListOf()
        for (i in 0 until notcieList.length()) {
            var obj = notcieList.optJSONObject(i)
            var title = obj.optString("title")
            if (null != title) {
                noticeList4String.add(title)
            }
        }
        return noticeList4String
    }

    /**
     * 广告根据数据加载
     * 有数据显示，无数据隐藏
     */
    private fun showGuanggao(noticeInfoList: JSONArray?) {
        if (null != noticeInfoList && noticeInfoList.length() > 0) {
            if (null == vtc_advertising?.textList || vtc_advertising?.textList!!.size == 0) {
                vtc_advertising?.setTextList(getNoticeInfoList(noticeInfoList))
                vtc_advertising?.setText(12f, 0, ContextCompat.getColor(context!!, R.color.text_color),true)
                vtc_advertising?.setTextStillTime(4000)//设置停留时长间隔
                vtc_advertising?.setAnimTime(200)//设置进入和退出的时间间隔
                vtc_advertising?.setOnItemClickListener(object : VerticalTextview4ChainUp.OnItemClickListener {
                    override fun onItemClick(pos: Int) {
                        var obj = noticeInfoList.optJSONObject(pos)
                        forwardWeb(obj)
                    }
                })
                vtc_advertising?.startAutoScroll()
            }
        } else {
            ll_advertising_layout?.visibility = View.GONE
            ll_advertising_line?.visibility = View.GONE
        }
    }

    private fun forwardWeb(jsonObject: JSONObject?) {
        var id = jsonObject?.optString("id")
        var title = jsonObject?.optString("title")
        var httpUrl = jsonObject?.optString("httpUrl")

        var bundle = Bundle()
        bundle.putString(ParamConstant.head_title, title)
        if (StringUtil.isHttpUrl(httpUrl)) {
            bundle.putString(ParamConstant.web_url, httpUrl)
        } else {
            bundle.putString(ParamConstant.web_url, id)
            bundle.putInt(ParamConstant.web_type, WebTypeEnum.Notice.value)
        }
        ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
    }

    /**
     * 获取 首页数据
     */
    private fun getHomepageData() {
        showLoadingDialog()
        var disposable = getMainModel().common_index(MyNDisposableObserver(homepageReqType))
        addDisposable(disposable!!)
    }

    /**
     * 对应的服务
     */
    fun enter2Activity(temp: List<String>?) {

        if (null == temp || temp.size <= 0)
            return

        when (temp[0]) {
            "coinmap_market" -> {
                /**
                 * 行情
                 */
                var tabType = HomeTabMap.maps[HomeTabMap.marketTab]
                homeTabSwitch(tabType)
            }
            "coinmap_trading" -> {
                /**
                 * 币对交易页
                 */
                SymbolManager.instance.saveTradeSymbol(temp[1])
                var tabType = HomeTabMap.maps[HomeTabMap.coinTradeTab]
                homeTabSwitch(tabType)
            }
            "coinmap_details" -> {
                /**
                 * 币对详情页
                 * MarketDetailActivity
                 */
                if (!TextUtils.isEmpty(temp[1])) {
                    ArouterUtil.forwardKLine(temp[1])
                } else {
                    NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "common_tip_hasNoCoinPair"))
                }
            }
            "otc_buy" -> {
                /**
                 *场外交易-购买
                 */
                /*if (LoginManager.checkLogin(activity, true)) {
                }*/
                if (otcOpen) {
                    ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
                } else {
                    NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))
                }
            }
            "otc_sell" -> {
                /**
                 * 场外交易-出售
                 */
                if (otcOpen) {
                    ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
                } else {
                    NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))
                }
            }

            "order_record" -> {
                /**
                 *订单记录
                 */

                if (LoginManager.checkLogin(activity, true)) {
                    if (otcOpen) {
                        ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                    } else {
                        NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))
                    }
                }
            }
            "account_transfer" -> {
                /**
                 * 账户划转
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, "BTC")
                }
            }
            "otc_account" -> {
                /**
                 *资产-场外账户
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (otcOpen) {
                        if (contractOpen) {
                            forwardAssetsActivity(1)
                        } else {
                            homeAssetstab_switch(1)
                        }
                    } else {
                        NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))

                    }
                }
            }
            "coin_account" -> {
                /**
                 * 资产-币币账户
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (contractOpen && otcOpen) {
                        forwardAssetsActivity(0)
                    } else {
                        homeAssetstab_switch(0)
                    }

                }

            }
            "safe_set" -> {
                /**
                 *安全设置
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.navigation(RoutePath.SafetySettingActivity, null)
                }
            }
            "safe_money" -> {
                /**
                 * 安全设置-资金密码
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (UserDataService.getInstance()?.authLevel != 1) {
                        NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "otc_please_cert"))
                        return
                    }
                    if (UserDataService.getInstance().isCapitalPwordSet == 0) {
                        ArouterUtil.forwardModifyPwdPage(ParamConstant.SET_PWD, ParamConstant.FROM_OTC)
                    } else {
                        ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_OTC)
                    }
                }
            }
            "personal_information" -> {
                /**
                 *个人资料
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                }

            }
            "personal_invitation" -> {
                /**
                 *个人资料-邀请码
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
                }

            }
            "collection_way" -> {
                /**
                 *收款方式
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (UserDataService.getInstance().authLevel != 1) {
                        NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "otc_please_cert"))
                        return
                    }

                    val capitalPwordSet = UserDataService.getInstance().isCapitalPwordSet
                    if (capitalPwordSet == 0) {
                        NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "otc_please_set_pwd"))
                        return
                    }

                    ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                }
            }
            "real_name" -> {
                /**
                 *实名认证
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            NToastUtil.showTopToastNet(this.mActivity,false, LanguageUtil.getString(context, "noun_login_pending"))
                        }
                        1 -> {
                            NToastUtil.showTopToastNet(this.mActivity,true, LanguageUtil.getString(context, "personal_text_verified"))
                        }
                        /**
                         * 审核未通过
                         */
                        2 -> {
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                        }

                        3 -> {
                            ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }
            }
            "contract_transaction" -> {
                /**
                 * 去合约交易页面
                 */
                forwardContractTab()
            }

            "market_etf" -> {
                /**
                 * ETF列表
                 */
                forwardMarketTab("ETF")
            }
            /**
             * 合约经纪人
             * TODO 这里需要确定key
             */
            "config_contract_agent_key" -> {
                if (!LoginManager.checkLogin(context, true)) {
                    return
                }
                ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
            }
        }
    }

    private fun forwardContractTab() {
        var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
        EventBusUtil.post(messageEvent)
    }

    private fun forwardMarketTab(coin: String) {
        var messageEvent = MessageEvent(MessageEvent.market_switch_type)
        messageEvent.msg_content = coin
        EventBusUtil.post(messageEvent)
    }

    /**
     * 获取账户信息
     */
    var accountBalance = ""
    var accountFlat = ""
    private fun getAccountBalance() {
        var disposable = getMainModel().getTotalAsset(MyNDisposableObserver(accountBalanceReqType))
        addDisposable(disposable)
    }
}