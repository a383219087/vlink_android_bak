package com.yjkj.chainup.new_version.fragment


import android.graphics.Typeface
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.fragment_new_version_otc_trading.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/4/2-5:40 PM
 * @Email buptjinlong@163.com
 * @description  新版法币交易 otc
 */
class NewVersionOTCTradingFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_new_version_otc_trading

    override fun loadData() {
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            getUserInfo()
        }
    }


    override fun initView() {
        titles.add(LanguageUtil.getString(context, "otc_action_buy"))
        titles.add(LanguageUtil.getString(context, "otc_action_sell"))

        val title = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(context, "otc_text_desc_forotc")
        } else {
            LanguageUtil.getString(context, "otc_text_desc")
        }
        tv_market_title?.text = title

        tv_buy?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        tv_buy?.setTextColor(ColorUtil.getColor(R.color.text_color))
        tv_sell?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        tv_buy?.setTextColor(ColorUtil.getColor(R.color.normal_icon_color))

        var array = PublicInfoDataService.getInstance().coinArray
        if (null != array && array.size > 0) {
            coinType = array[0]
        }

        var dataForPublic = OTCPublicInfoDataService.getInstance().getData()
        if (null != dataForPublic) {
            var defaultCoin = OTCPublicInfoDataService.getInstance().getDefaultCoin()
            if (array.isNotEmpty()) {
                for (num in 0 until array.size) {
                    if (defaultCoin == array[num]) {
                        coinType = defaultCoin
                        break
                    }
                }
            } else {
                coinType = defaultCoin
            }
            initV(dataForPublic)
        } else {
            addDisposable(getOTCModel().getOTCPublicInfo(object : NDisposableObserver(activity, false) {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    var data = jsonObject.optJSONObject("data")
                    if (null != data) {
                        OTCPublicInfoDataService.getInstance().saveData(data)
                        var defaultCoin = data.optString("defaultCoin")
                        coinType = defaultCoin
                    }
                    initV(data)
                }

                override fun onResponseFailure(code: Int, msg: String?) {
                    super.onResponseFailure(code, msg)
                }
            }))
        }
        setOnclick()
        setTextContent()
        //获取上个也页面需要跳转的tag 0-购买,1-为出售
        var tag = arguments?.getInt("tag",0)?:0
        setTopBar(tag)

    }

    fun setTextContent() {
        tv_buy?.text = LanguageUtil.getString(context, "otc_action_buy")
        tv_sell?.text = LanguageUtil.getString(context, "otc_action_sell")
        tv_advertising?.text = LanguageUtil.getString(context, "otc_text_ad")
    }

    private fun initV(data: JSONObject?) {
        spw_layout.setDataForService(data)
        LogUtil.e(TAG, "initV otc  coinType =  ${coinType}")
        fragments.add(FragmentNewOtcTradingDetail.newInstance(coinType, 0, 0, isBlockTrade, payCoin, payment))
        fragments.add(FragmentNewOtcTradingDetail.newInstance(coinType, 1, 1, isBlockTrade, payCoin, payment))
        initTab()

    }


    private val fragments = arrayListOf<Fragment>()
    private var isBlockTrade: String = ""
    private var payCoin: String = "CNY"
    private var payment: String = ""
    private var titles: ArrayList<String> = arrayListOf()

    var isfrist = true
    fun setOnclick() {
        /**
         * 我的订单
         */
        tv_screening?.setOnClickListener {
            if (LoginManager.checkLogin(activity, true)) {
                ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
            }
        }
        /**
         * 筛选按钮
         */
        iv_search?.setOnClickListener {
            if (spw_layout?.visibility == View.VISIBLE) {
                spw_layout?.visibility = View.GONE
            } else {
                spw_layout?.visibility = View.VISIBLE
            }

        }
        iv_back?.setOnClickListener {
            mActivity?.finish()
        }
        /**
         * 点击 买
         */
        tv_buy?.setOnClickListener {
            setTopBar(0)
        }

        /**
         * 点击卖
         */
        tv_sell?.setOnClickListener {
            setTopBar(1)
        }


        /**
         * 点击广告
         */
        tv_advertising?.setOnClickListener {
            setTopBar(2)
        }
        /**
         * 筛选点击
         */
        spw_layout?.tradingListener = object : ScreeningPopupWindowView.TradingListener {
            override fun returnTradingType(trading: Int, amount: String, fiatType: String, paymentType: String, countries: String) {
                refreshScreening(trading.toString(), amount, fiatType, paymentType, countries)
            }
        }
    }


    /**
     * 筛选
     */
    fun refreshScreening(trading: String, amount: String, fiatType: String, paymentType: String, countries: String) {
        if (fragments.size <= currentItem) return
        var fragment: FragmentNewOtcTradingDetail = fragments[currentItem] as FragmentNewOtcTradingDetail
        fragment.refreshForOTHHome(if (currentItem == 0) TRADING_TYPE_BUY else TRADING_TYPE_SELL, trading, amount, fiatType, paymentType, countries)

    }

    fun setTopBar(buyOrSell: Int) {

        when (buyOrSell) {
            0 -> {
                tv_buy?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                tv_sell?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                tv_buy?.setTextColor(ContextCompat.getColor(context!!, R.color.text_color))
                tv_sell?.setTextColor(ContextCompat.getColor(context!!, R.color.normal_text_color))
                view_buy_bg?.visibility = View.VISIBLE
                view_sell_bg?.visibility = View.GONE
                currentItem = 0
                viewPager?.currentItem = 0
            }
            1 -> {
                tv_buy?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                tv_sell?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                tv_buy?.setTextColor(ContextCompat.getColor(context!!, R.color.normal_text_color))
                tv_sell?.setTextColor(ContextCompat.getColor(context!!, R.color.text_color))
                view_buy_bg?.visibility = View.GONE
                view_sell_bg?.visibility = View.VISIBLE
                currentItem = 1
                viewPager?.currentItem = 1
            }
            2 -> {
                ArouterUtil.navigation(RoutePath.NewAdvertisingManagementActivity, null)
            }
        }
    }


    /**
     * 选择的币对
     */
    var coinType = ""

    var currentItem = 0

    fun initTab() {
        val otcHomePageAdapter = NVPagerAdapter(childFragmentManager, titles, fragments)
        viewPager?.adapter = otcHomePageAdapter
        viewPager?.offscreenPageLimit = 0
    }


    /**
     * 获取 eventBus 从我的资产页面过来跳转 对应币对
     */
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.fait_trading) {
            if (null != event.msg_content) {
                var jsonObject = event.msg_content as JSONObject
                if (jsonObject.optBoolean("buyorsell")) {
                    tv_buy?.setTextColor(ColorUtil.getColor(R.color.text_color))
                    tv_sell?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                    tv_buy?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_sell?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)

                    view_buy_bg?.visibility = View.VISIBLE
                    view_sell_bg?.visibility = View.GONE
                    currentItem = 0
                    viewPager.currentItem = 0
                } else {
                    tv_buy?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    tv_sell?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    tv_buy?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                    tv_sell?.setTextColor(ColorUtil.getColor(R.color.text_color))
                    view_buy_bg?.visibility = View.GONE
                    view_sell_bg?.visibility = View.VISIBLE
                    currentItem = 1
                    viewPager.currentItem = 1
                }
            }
        }
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        tv_advertising?.visibility = View.GONE
        view_dvertising_bg?.visibility = View.GONE
        if (!UserDataService.getInstance().isLogined)
            return

        addDisposable(getMainModel().getUserInfo(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                UserDataService.getInstance().saveData(data)
                var otcCompanyInfo = data?.optJSONObject("otcCompanyInfo")
                LogUtil.d(TAG, "getUserInfo==data is $data")

                var status = otcCompanyInfo?.optString("status") ?: ""
                if (status == "0") {
                    tv_advertising?.visibility = View.VISIBLE
                } else {
                    var userCompanyInfo = data?.optJSONObject("userCompanyInfo")

                    var status = userCompanyInfo?.optString("status") ?: ""
                    if ("1" == status || "3" == status) {
                        tv_advertising?.visibility = View.VISIBLE
                    } else {
                        tv_advertising?.visibility = View.GONE
                    }
                }
            }

        }))
    }

}