package com.yjkj.chainup.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.gcssloop.widget.PagerGridLayoutManager
import com.gcssloop.widget.PagerGridSnapHelper
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNewVersionHomepageBinding
import com.yjkj.chainup.db.constant.*
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.db.service.v5.CommonService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.ui.NewMainActivity
import com.yjkj.chainup.new_version.activity.personalCenter.NoticeActivity
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.adapter.NewHomePageServiceAdapter
import com.yjkj.chainup.new_version.adapter.NewhomepageTradeListAdapter
import com.yjkj.chainup.new_version.fragment.MarketContractFragment
import com.yjkj.chainup.new_version.home.*
import com.yjkj.chainup.new_version.home.adapter.ImageNetAdapter
import com.yjkj.chainup.ui.home.vm.NewVersionHomePageViewModel
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.HomePageItemDecoration
import com.yjkj.chainup.wedegit.VerticalTextview4ChainUp
import com.yjkj.chainup.ws.WsAgentManager
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.RectangleIndicator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2019/5/5-2:54 PM
 * @Email buptjinlong@163.com
 * @description 首页
 */
class NewVersionHomepageFragment :
  BaseMVFragment<NewVersionHomePageViewModel?, FragmentNewVersionHomepageBinding>(),
  WsAgentManager.WsResultCallback {

  val getTopDataReqType = 1 // 首页顶部行情数据请求
  val homepageReqType = 2 // 首页数据请求
  val homeData = 11 //总账户资产请求

  /**
   * 是否开启场外
   */
  private var otcOpen = false

  private var leverOpen = false

  /**
   * 是否开启合约
   */
  private var contractOpen = false

  /**
   * 功能服务
   */
  private var serviceAdapter: NewHomePageServiceAdapter? = null

  var defaultBanner = 0


  /*
   *  是否已经登录
   */
  var isLogined = false
  var subscribeCoin: Disposable? = null//保存订阅者
  var isScrollStatus = false

  override fun setContentView() = R.layout.fragment_new_version_homepage

  override fun initView() {
    mViewModel?.mActivity?.value = mActivity
    otcOpen = PublicInfoDataService.getInstance().otcOpen(null)
    leverOpen = PublicInfoDataService.getInstance().isLeverOpen(null)
    contractOpen = PublicInfoDataService.getInstance().contractOpen(null)
    WsAgentManager.instance.addWsCallback(this)
    observeData()

    initTop24HourView()

    setOnClick()
    mViewModel?.getPublicInfo(context!!)
    defaultBanner = R.drawable.banner_king
    val data = CommonService.instance.getHomeData()
    showHomepageData(data, true)

    mBinding?.swipeRefresh?.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))


  }



  /**
   * 顶部 24小时涨幅榜(推荐币种)
   */
  private var topSymbolAdapter: NewhomepageTradeListAdapter? = null
  var scrollX = 0
  private fun initTop24HourView() {

    mBinding?.recyclerTop24?.layoutManager =
      LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
    mBinding?.recyclerTop24?.addItemDecoration(HomePageItemDecoration(activity!!))
    topSymbolAdapter = NewhomepageTradeListAdapter()
    mBinding?.recyclerTop24?.adapter = topSymbolAdapter
    topSymbolAdapter?.setOnItemClickListener { adapter, view, position ->
      val dataList = topSymbolAdapter!!.data
      if(dataList.size > 0) {
        val symbol = dataList[position].optString("symbol")
        ArouterUtil.forwardKLine(symbol)
      }
    }
    mBinding?.recyclerTop24?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        scrollX += dx
        val contentItem = recyclerView.computeHorizontalScrollOffset()
        if(contentItem == 0) {
          return
        }
        val sum =
          recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
        val count = BigDecimalUtils.div(scrollX.toString(), sum.toString())
        mBinding?.layoutItem?.setRate(count.toFloat())
      }
    })

  }

  @SuppressLint("NotifyDataSetChanged")
  private fun observeData() {
    NLiveDataUtil.observeData(this, Observer {
      if(null != it) {
        if(MessageEvent.color_rise_fall_type == it.msg_type) {
          topSymbolAdapter?.notifyDataSetChanged()
        }
      }
    })
    GlobalScope.launch {
      delay(3000L)
    }
  }

  inner class MyNDisposableObserver(type: Int) : NDisposableObserver() {

    var req_type = type
    override fun onResponseSuccess(jsonObject: JSONObject) {
      when {
        getTopDataReqType == req_type -> {
          mBinding?.recyclerTop24?.visibility = View.VISIBLE
          showTopSymbolsData(jsonObject.optJSONArray("data"))
        }
        homepageReqType == req_type -> {
          showHomepageData(jsonObject.optJSONObject("data"))
        }
        homeData == req_type -> {
          closeLoadingDialog()
          showHomepageData(jsonObject.optJSONObject("data"))
          advertTime()
        }
      }
    }

    override fun onResponseFailure(code: Int, msg: String?) {
      super.onResponseFailure(code, msg)
      if(getTopDataReqType == req_type) {
        mBinding?.recyclerTop24?.visibility = View.GONE
      }
      if(req_type == homeData) {
        initSocket()
        advertTime(true)
      }
      closeLoadingDialog()
    }
  }


  var homepageData: JSONObject? = null


  /*
   * 首页数据展示
   */
  private fun showHomepageData(data: JSONObject?, isCache: Boolean = false) {
    if(null == data)
      return
    if(!isCache) {
      homepageData = data
      val jsonObjects = JSONObject(data.toString())
      CommonService.instance.saveHomeData(jsonObjects)
      val arrayGuide = arrayOf(
        mBinding!!.homeHeader.layoutSearch,
        mBinding.ivNationMore,
        mBinding.layoutTop24,
        mBinding.recyclerCenterServiceLayout
      )
//      showGuideHomepage(mActivity, arrayGuide, data)
    }
    val noticeInfoList = data.optJSONArray("noticeInfoList")
    val cmsAppAdvertList = data.optJSONArray("cmsAppAdvertList")
    val cmsAppDataList = data.optJSONArray("cmsAppDataList")
    val cmsSymbolList = data.optJSONArray("header_symbol")
    val homeRecommendList = data.optJSONArray("home_recommend_list") ?: JSONArray()

    showTopSymbolsData(cmsSymbolList)
    /*
     *涨幅榜等数据 显示
     */
    showBottomVp(homeRecommendList)

    if(noticeInfoList != null) {
      newNoticeInfoList = noticeInfoList
    }
    showGuanggao(noticeInfoList)
    showBannerData(cmsAppAdvertList)
    setServiceData(cmsAppDataList)

  }


  var newNoticeInfoList = JSONArray()

  /*
   * 展示顶部轮播图
   */
  var bannerImgUrls = arrayListOf<String>()
  var bannerNoteUrls: ArrayList<String> = arrayListOf()

  private fun showBannerData(cmsAppAdvertList: JSONArray?) {

    if(null == cmsAppAdvertList || cmsAppAdvertList.length() <= 0)
      return

    bannerImgUrls.clear()
    mViewModel?.bannerImgUrls?.clear()
    for(i in 0 until cmsAppAdvertList.length()) {
      var obj = cmsAppAdvertList.optJSONObject(i)
      var imageUrl = obj.optString("imageUrl")
      if(StringUtil.isHttpUrl(imageUrl)) {
        bannerImgUrls.add(imageUrl)
        mViewModel?.bannerImgUrls?.add(imageUrl)
      }
    }

    mBinding?.bannerLooper?.apply {
      //设置图片集合
      val mAdapter = ImageNetAdapter(bannerImgUrls)
      adapter = mAdapter
      //设置轮播时间
      setLoopTime(3000)
      indicator = RectangleIndicator(context)
      //设置指示器位置（当banner模式中有指示器时）
      setIndicatorGravity(IndicatorConfig.Direction.CENTER)
    }
    mBinding?.bannerLooper?.setOnBannerListener { data, position ->
      var obj = cmsAppAdvertList.optJSONObject(position)
      var httpUrl = obj?.optString("httpUrl") ?: ""
      var nativeUrl = obj?.optString("nativeUrl") ?: ""

      //TODO 需要一个标题
      if(TextUtils.isEmpty(httpUrl)) {
        if(StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
          enter2Activity(nativeUrl.split("?"))
        }
      } else {
        forwardWeb(obj)
      }
    }
    // banner设置方法全部调用完毕时最后调用
    mBinding?.bannerLooper?.start()

  }


  private fun showAdvertising(isShow: Boolean) {
    if(null != mBinding?.vtcAdvertising?.textList && mBinding?.vtcAdvertising?.textList!!.size > 0) {
      if(isShow) {
        mBinding?.vtcAdvertising?.startAutoScroll()
      } else {
        mBinding?.vtcAdvertising?.stopAutoScroll()
      }
    }
  }

  private fun showBanner(isShow: Boolean) {
    if(bannerImgUrls.size > 0) {
      if(isShow) {
        mBinding?.bannerLooper?.start()
      } else {
        mBinding?.bannerLooper?.stop()
      }
    }


  }


  private fun getAllAccounts() {
    isLogined = UserDataService.getInstance().isLogined

    if(null == homepageData) {
      getHomeData()
    }
  }


  /*
   * 资产tab跳转
   */
  private fun homeAssetstab_switch(type: Int) {
    var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
    var bundle = Bundle()
    var homeTabType = HomeTabMap.maps.get(HomeTabMap.assetsTab)
    bundle.putInt(ParamConstant.homeTabType, homeTabType ?: 4)
    bundle.putInt(ParamConstant.assetTabType, type)
    msgEvent.msg_content = bundle
    EventBusUtil.post(msgEvent)
  }

  /*
   * 首页底部tab跳转的处理
   */
  private fun homeTabSwitch(tabType: Int?, symbol: String? = "") {
    var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
    var bundle = Bundle()
    bundle.putInt(ParamConstant.homeTabType, tabType ?: 0)
    if(symbol != null) {
      bundle.putString(ParamConstant.symbol, symbol)
    }
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
    mBinding?.ivNationMore?.setOnClickListener {
      startActivity(Intent(mActivity, NoticeActivity::class.java))
    }



    /**
     * 此处刷新
     */
    mBinding?.swipeRefresh?.setOnRefreshListener {
      isScrollStatus = true
      /**
       * 刷新数据操作
       */
      if(homepageData == null || fragments.size == 0 || selectTopSymbol == null) {
        getHomeData()
      } else {
        getVPTab()
      }
      mBinding?.swipeRefresh?.isRefreshing = false
    }
  }



  /*
   * 首页顶部symbol 24小时行情展示
   */
  var selectTopSymbol: ArrayList<JSONObject>? = null
  private fun showTopSymbolsData(topSymbol: JSONArray?) {
    selectTopSymbol = NCoinManager.getSymbols(topSymbol)
    if(null == selectTopSymbol || selectTopSymbol?.size!! <= 0) {
      setTopViewVisible(false)
      return
    }
    setTopViewVisible(true)
    topSymbolAdapter?.setList(selectTopSymbol)
  }


  private fun initSocket() {
    if(selectTopSymbol == null) {
      return
    }
    val arrays = arrayListOf<String>()
    for(item in selectTopSymbol!!) {
      arrays.add(item.getString("symbol"))
    }
    homeCoins.clear()
    val json = if(bottomCoins.isNotEmpty()) {
      val temp = bottomCoins union arrays
      homeCoins.addAll(temp)
      JsonUtils.gson.toJson(temp)
    } else {
      homeCoins.addAll(arrays)
      JsonUtils.gson.toJson(arrays)
    }
    WsAgentManager.instance.sendMessage(hashMapOf("bind" to true, "symbols" to json), this)
  }

  override fun fragmentVisibile(isVisible: Boolean) {
    super.fragmentVisibile(isVisible)
    val mainActivity = activity
    if(mainActivity != null) {
      if(mainActivity is NewMainActivity) {
        if(isVisible && mainActivity.curPosition == 0) {
          getAllAccounts()
          showAdvertising(true)
          showBanner(true)
        } else {
          showAdvertising(false)
          showBanner(false)
        }

      }
    }

  }


  /**
   * 是否显示24小时行情
   */
  private fun setTopViewVisible(isShow: Boolean) {
    if(isShow) {

      mBinding?.recyclerTop24?.visibility = View.VISIBLE
      if(selectTopSymbol != null && selectTopSymbol!!.size > 3) {
        mBinding?.layoutItem?.visibility = View.VISIBLE
      } else {
        mBinding?.layoutItem?.visibility = View.GONE
      }
    } else {
      mBinding?.recyclerTop24?.visibility = View.GONE
      mBinding?.layoutItem?.visibility = View.GONE
    }
  }

  /**
   * 获取顶部symbol 24小时行情
   */
  private fun getHomeData() {
    showLoadingDialog()
    val type = "1"
    klineTime = System.currentTimeMillis()
    val disposable = getMainModel().getHomeData(type, MyNDisposableObserver(homeData))
    addDisposable(disposable)

  }

  override fun refreshOkhttp(position: Int) {
    if(position == 0) {
      getTopData()
    }
  }

  val fragments = arrayListOf<Fragment>()
  var selectPostion = 0


  private fun showBottomVp(data: JSONArray) {
    val titles = arrayListOf<String>()
    val serviceDatas = JSONUtil.arrayToList(data)
    if(serviceDatas == null) {
      if(!NetUtil.isNetConnected(mActivity!!)) {
        mBinding?.bottomVpLinearlayout?.visibility = View.GONE
      }
      return
    }
    mBinding?.bottomVpLinearlayout?.visibility = View.VISIBLE
    for(item in serviceDatas) {
      LogUtil.e("首页tab数据", serviceDatas.toString())
    }
    titles.add(getString(R.string.trade_contract_title))
    titles.add(getString(R.string.trade_bb_titile))
    fragments.clear()

    fragments.add(MarketContractFragment.newInstance(0))
    fragments.add(
      NewHomeDetailFragmentItem.newInstance(
        titles[1],
        0,
        "rasing",
        mBinding?.fragmentMarket!!,
        serviceDatas[0].getJSONArray("list").toString()
      )
    )

    val marketPageAdapter = NVPagerAdapter(childFragmentManager, titles, fragments)
    mBinding?.fragmentMarket?.adapter = marketPageAdapter
    mBinding?.fragmentMarket?.offscreenPageLimit = fragments.size
    mBinding?.fragmentMarket?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
      }

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        selectPostion = position
        WsAgentManager.instance.unbind(this@NewVersionHomepageFragment)
        loopData()
        mBinding?.fragmentMarket?.resetHeight(selectPostion)
      }
    })
    loopData()
    try {
      mBinding?.stlHomepageList?.setViewPager(mBinding?.fragmentMarket, titles.toTypedArray())
    } catch(e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * 获取滚动的广告
   */
  private fun getNoticeInfoList(notcieList: JSONArray): ArrayList<String> {
    val noticeList4String: ArrayList<String> = arrayListOf()
    for(i in 0 until notcieList.length()) {
      val obj = notcieList.optJSONObject(i)
      val title = obj.optString("title")
      noticeList4String.add(title)
    }
    return noticeList4String
  }

  /**
   * 广告根据数据加载
   * 有数据显示，无数据隐藏
   */
  private fun showGuanggao(noticeInfoList: JSONArray?) {
    if(null != noticeInfoList && noticeInfoList.length() > 0) {

      mBinding?.llAdvertisingLayout?.visibility = View.VISIBLE
      if(null ==   mBinding?.vtcAdvertising?.textList ||   mBinding?.vtcAdvertising?.textList!!.size == 0) {
        mBinding?.vtcAdvertising?.setText(
          12f,
          0,
          ContextCompat.getColor(context!!, R.color.text_color),
          true
        )
        mBinding?.vtcAdvertising?.setTextStillTime(4000)//设置停留时长间隔
        mBinding?.vtcAdvertising?.setAnimTime(400)//设置进入和退出的时间间隔
        mBinding?.vtcAdvertising?.setOnItemClickListener { pos ->
          val obj = newNoticeInfoList.optJSONObject(pos)
          forwardWeb(obj)
        }
      }
      mBinding?.vtcAdvertising?.setTextList(getNoticeInfoList(noticeInfoList))
      mBinding?.vtcAdvertising?.startAutoScroll()
    } else {
      mBinding?.llAdvertisingLayout?.visibility = View.GONE
    }
  }

  private fun forwardWeb(jsonObject: JSONObject?) {
    val id = jsonObject?.optString("id")
    val title = jsonObject?.optString("title")
    val httpUrl = jsonObject?.optString("httpUrl")

    val bundle = Bundle()
    bundle.putString(ParamConstant.head_title, title)
    if(StringUtil.isHttpUrl(httpUrl)) {
      bundle.putString(ParamConstant.web_url, httpUrl)
    } else {
      bundle.putString(ParamConstant.web_url, id)
      bundle.putInt(ParamConstant.web_type, WebTypeEnum.Notice.value)
    }
    ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
  }


  private fun forUdeskWebView() {
    val bundle = Bundle()
    bundle.putString(
      ParamConstant.URL_4_SERVICE,
      PublicInfoDataService.getInstance().getOnlineService(null)
    )
    ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
  }

  /**
   * 如果服务器没有返回服务数据
   * 服务功能这里整体GONE
   */
  private fun setServiceView() {
    mBinding?.recyclerCenterServiceLayout?.visibility = View.GONE
  }

  private fun setServiceShowView() {
    mBinding?.recyclerCenterServiceLayout?.visibility = View.VISIBLE
  }

  private var servicePageSize = 0

  /**
   *
   *  从服务器获取功能服务数据后填充
   */

  private fun setServiceData(appData: JSONArray?, viewType: Int = 0) {
    if(null == appData || appData.length() <= 0) {
      setServiceView()
      return
    }
    setServiceShowView()
    val serviceDatas = JSONUtil.arrayToList(appData)
    val mardBottom = if(bannerNoteUrls.size != 0) DisplayUtil.dip2px(2) else DisplayUtil.dip2px(12)
    val linearParams = mBinding?.recyclerCenterServiceLayout?.layoutParams as LinearLayout.LayoutParams
    linearParams.setMargins(0, 0, 0, mardBottom)
    mBinding?.recyclerCenterServiceLayout?.layoutParams = linearParams
    val linearParams2 = mBinding?.recyclerCenterService?.layoutParams
    linearParams2?.width = ViewGroup.LayoutParams.MATCH_PARENT
    linearParams2?.height = SizeUtils.dp2px(if(serviceDatas.size > 4) 128f else 64f)
    mBinding?.recyclerCenterServiceLayout?.layoutParams = linearParams2

    val mLayoutManager = PagerGridLayoutManager(
      if(serviceDatas.size > 4) 2 else 1,
      4,
      PagerGridLayoutManager.HORIZONTAL
    )
    mLayoutManager.setPageListener(object : PagerGridLayoutManager.PageListener {
      override fun onPageSelect(pageIndex: Int) {
        //todo  这里是第几页 +1
        if(servicePageSize <= 1) {
          return
        }
        when(pageIndex) {
          0 -> {
            mBinding?.ivFrist?.setBackgroundResource(R.drawable.item_bg_4_homepage_select)
            mBinding?.ivSecond?.setBackgroundResource(R.drawable.item_bg_4_homepage_unselect)

          }
          1 -> {
            mBinding?.ivFrist?.setBackgroundResource(R.drawable.item_bg_4_homepage_unselect)
            mBinding?.ivSecond?.setBackgroundResource(R.drawable.item_bg_4_homepage_select)
          }
        }
      }

      override fun onPageSizeChanged(pageSize: Int) {
        servicePageSize = pageSize

      }
    })
    if(serviceDatas.size > 8) {

      mBinding?.rlController?.visibility = View.VISIBLE
    } else {
      mBinding?.rlController?.visibility = View.INVISIBLE
    }
    val snapHelper = PagerGridSnapHelper()
    if(mBinding?.recyclerCenterService?.onFlingListener == null) {
      snapHelper.attachToRecyclerView(mBinding?.recyclerCenterService?: return)
    }
    mBinding?.recyclerCenterService?.layoutManager = mLayoutManager


    serviceAdapter = NewHomePageServiceAdapter(serviceDatas)
    serviceAdapter?.setOnItemClickListener { adapter, view, position ->

      val obj = serviceDatas.get(position)
      val httpUrl = obj.optString("httpUrl")
      val nativeUrl = obj.optString("nativeUrl")

      LogUtil.d(TAG, "httpUrl is $httpUrl , nativeUrl is $nativeUrl")
      if(TextUtils.isEmpty(httpUrl)) {
        if(StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
          enter2Activity(nativeUrl.split("?"))
        }
      } else {
        if(httpUrl == PublicInfoDataService.getInstance().getOnlineService(null)) {
          forUdeskWebView()
        } else {
          forwardWeb(obj)
        }
      }
    }

    mBinding?.recyclerCenterService?.adapter = serviceAdapter
  }


  /**
   * 对应的服务
   */
  private fun enter2Activity(temp: List<String>?) {

    if(null == temp || temp.size <= 0)
      return

    when(temp[0]) {
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
        var tabType = HomeTabMap.maps[HomeTabMap.coinTradeTab]
        homeTabSwitch(tabType, temp[1])
      }
      "coinmap_details" -> {
        /**
         * 币对详情页
         * MarketDetailActivity
         */
        if(!TextUtils.isEmpty(temp[1])) {
          ArouterUtil.forwardKLine(temp[1])
        } else {
          NToastUtil.showTopToastNet(
            this.mActivity,
            false,
            LanguageUtil.getString(context, "common_tip_hasNoCoinPair")
          )
        }
      }
      "otc_buy" -> {
        /**
         *场外交易-购买
         */
        /*if (LoginManager.checkLogin(activity, true)) {
        }*/
        if(otcOpen) {
          var bundle = Bundle()
          bundle.putInt("tag", 0)
          ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, bundle)
        } else {
          NToastUtil.showTopToastNet(
            this.mActivity,
            false,
            LanguageUtil.getString(context, "common_tip_notSupportOTC")
          )
        }
      }
      "otc_sell" -> {
        /**
         * 场外交易-出售
         */
        if(otcOpen) {
          var bundle = Bundle()
          bundle.putInt("tag", 1)
          ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, bundle)
        } else {
          NToastUtil.showTopToastNet(
            this.mActivity,
            false,
            LanguageUtil.getString(context, "common_tip_notSupportOTC")
          )
        }
      }

      "order_record" -> {
        /**
         *订单记录
         */

        if(LoginManager.checkLogin(activity, true)) {
          if(otcOpen) {
            ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
          } else {
            NToastUtil.showTopToastNet(
              this.mActivity,
              false,
              LanguageUtil.getString(context, "common_tip_notSupportOTC")
            )
          }
        }
      }
      "account_transfer" -> {
        /**
         * 账户划转
         */
        if(LoginManager.checkLogin(activity, true)) {
          ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, "BTC")
        }
      }
      "otc_account" -> {
        /**
         *资产-场外账户
         */
        if(LoginManager.checkLogin(activity, true)) {

          if(otcOpen) {
            if(contractOpen) {
              forwardAssetsActivity(1)
            } else {
              homeAssetstab_switch(1)
            }

          } else {
            NToastUtil.showTopToastNet(
              this.mActivity,
              false,
              LanguageUtil.getString(context, "common_tip_notSupportOTC")
            )
          }
        }
      }
      "contract_follow_order" -> {
        /**
         * 跟单页面
         */

      }

      "coin_account" -> {
        /**
         * 资产-币币账户
         */
        if(LoginManager.checkLogin(activity, true)) {
          if(contractOpen && otcOpen) {
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
        if(LoginManager.checkLogin(activity, true)) {
          ArouterUtil.navigation(RoutePath.SafetySettingActivity, null)
        }
      }
      "safe_money" -> {
        /**
         * 安全设置-资金密码
         */
        if(LoginManager.checkLogin(activity, true)) {
          if(UserDataService.getInstance()?.authLevel != 1) {
            NToastUtil.showTopToastNet(
              this.mActivity,
              false,
              LanguageUtil.getString(context, "otc_please_cert")
            )
            return
          }
          if(UserDataService.getInstance().isCapitalPwordSet == 0) {
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
        if(LoginManager.checkLogin(activity, true)) {
          ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
        }

      }
      "personal_invitation" -> {
        /**
         *个人资料-邀请码
         */
        if(LoginManager.checkLogin(activity, true)) {
          ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
        }

      }
      "collection_way" -> {
        /**
         *收款方式
         */
        if(LoginManager.checkLogin(activity, true)) {
          ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
        }
      }
      "real_name" -> {
        /**
         *实名认证
         */
        if(LoginManager.checkLogin(activity, true)) {
          when(UserDataService.getInstance().authLevel) {
            0 -> {
              NToastUtil.showTopToastNet(
                this.mActivity,
                false,
                LanguageUtil.getString(context, "noun_login_pending")
              )
            }
            1 -> {
              ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
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
        if(!LoginManager.checkLogin(context, true)) {
          return
        }
        ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
      }

      "account_freeStaking" -> {
        if(!LoginManager.checkLogin(context, true)) {
          return
        }
        /**
         * FreeStaking首页
         */
        ArouterUtil.greenChannel(RoutePath.FreeStakingActivity, null)
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


  override fun onWsMessage(json: String) {
    handleData(json)
  }

  fun handleData(data: String) {
    try {
      val json = JSONObject(data)
      if(!json.isNull("tick")) {
          val channel = json.optString("channel")
          val temp = homeCoins.filter {
            channel.contains(it)
          }
          if(temp.isNotEmpty()) {
            val dataDiff = callDataDiff(json)
            if(dataDiff != null) {
              val items = dataDiff.second
              if(bottomCoins.size != homeCoins.size) {
                showWsData(items)
              }

              val fragment = fragments[selectPostion]
              if(fragment is NewHomeDetailFragmentItem) {
                val tempMap = HashMap<String, JSONObject>()
                for(item in items) {
                  val channelNew = item.value.optString("channel").split("_")[1]
                  val tempBottom = bottomCoins.filter {
                    channelNew.contains(it)
                  }
                  if(tempBottom.isNotEmpty()) {
                    tempMap[item.key] = item.value
                  }
                }
                if(tempMap.isEmpty()) {
                  return
                }
                fragment.dropListsAdapter(tempMap)
              }
              wsArrayTempList.clear()
              wsArrayMap.clear()
            }
        }
      }
    } catch(e: Exception) {
      e.printStackTrace()
    }
  }

  private fun showWsData(items: HashMap<String, JSONObject>) {
    if(0 == selectTopSymbol?.size)
      return
    val dates = selectTopSymbol
    var isLoad = 0
    for(item in items) {
      val channel = item.value.optString("channel").split("_")[1]
      val temp = dates?.filter {
        channel.contains(it.optString("symbol"))
      }
      if(temp != null && temp.isNotEmpty()) {
        val jsonObject = temp[0]
        val data = item.value
        val tick = data.optJSONObject("tick")
        tick?.apply {
          jsonObject.put("rose", this.optString("rose"))
          jsonObject.put("close", this.optString("close"))
          jsonObject.put("vol", this.optString("vol"))
          val index = dates.indexOf(jsonObject)
          dates[index] = jsonObject
          isLoad++
        }
      }
    }
    if(isLoad != 0) {
      activity?.runOnUiThread {
        topSymbolAdapter?.setList(dates)
      }
    }

  }

  override fun onVisibleChanged(isVisible: Boolean) {
    super.onVisibleChanged(isVisible)
    if(isVisible) {
      Handler().postDelayed({
        isRoseHttp()
        initSocket()
        if(fragments.size == 0) {
          return@postDelayed
        }
        val fragment = fragments[selectPostion]
        if(fragment is NewHomeDetailFragmentItem) {
          fragment.startInit()
        }
      }, 100)
    } else {
      if(selectTopSymbol != null) {
        WsAgentManager.instance.unbind(this)
      }
      clearToolHttp()
    }
  }

  private var bottomCoins = arrayListOf<String>()
  private var homeCoins = arrayListOf<String>()

  @Subscribe(threadMode = ThreadMode.POSTING)
  override fun onMessageEvent(event: MessageEvent) {
    super.onMessageEvent(event)
    if(MessageEvent.home_event_page_symbol_type == event.msg_type) {
      val mainActivity = activity
      if(mainActivity is NewMainActivity) {
        if(mainActivity.curPosition != 0) {
          return
        }
      }
      val map = event.msg_content as HashMap<String, Array<String>>
      val curIndex = map.get("curIndex") as Int
      if(selectPostion != curIndex) {
        return
      }
      bottomCoins.clear()
      val array = map.get("symbols") as Array<String>
      for(item in array) {
        bottomCoins.add(item)
      }
      initSocket()
    }

  }

  private var isRose = true
  private fun loopData() {
    LogUtil.e(TAG, "tradeList value loopData  ${mIsVisibleToUser} ")
    if(!mIsVisibleToUser)
      return
    clearToolHttp()
    if(subscribeCoin == null || (subscribeCoin != null && subscribeCoin?.isDisposed != null && subscribeCoin?.isDisposed!!)) {
      subscribeCoin = Observable.interval(
        10L,
        CommonConstant.homeLoopTime,
        TimeUnit.SECONDS
      )//按时间间隔发送整数的Observable
        .observeOn(AndroidSchedulers.mainThread())//切换到主线程修改UI
        .subscribe {
          getVPTab()
        }
    }
  }

  override fun onResume() {
    super.onResume()
    LogUtil.e(TAG, "onResume() ")
  }

  private fun isRoseHttp() {
    if(!isRose) {
      return
    }
    isRose = false
    loopData()
  }

  /**
   * 获取数据
   */
  private fun getVPTab() {

    var disposable =
      getMainModel().trade_list_v4("rasing", object : NDisposableObserver(null, false, "rasing") {
        override fun onResponseSuccess(jsonObject: JSONObject) {
          isRose = true
          val fragment = fragments[1]
          if(fragment is NewHomeDetailFragmentItem) {
              fragment.initV(jsonObject.optJSONArray("data"))
          }
          this.mapParams
        }

        override fun onResponseFailure(code: Int, msg: String?) {
          super.onResponseFailure(code, msg)
          isRose = true
        }
      })
    addDisposable(disposable)
  }

  private fun clearToolHttp() {
    if(subscribeCoin != null) {
      subscribeCoin?.dispose()
    }
  }

  override fun appBackGroundChange(isVisible: Boolean) {
    super.appBackGroundChange(isVisible)
    mIsVisibleToUser = isVisible
    if(isVisible) {
      isRoseHttp()
    } else {
      clearToolHttp()
    }
  }




  /**
   * 获取顶部symbol 24小时行情
   */
  fun getTopData() {
    var disposable = getMainModel().header_symbol(MyNDisposableObserver(getTopDataReqType))
    addDisposable(disposable)
  }

  var klineTime = 0L
  private fun advertTime(isError: Boolean = false) {
    klineTime = System.currentTimeMillis() - klineTime
    val temp = if(isError) {
      4
    } else {
      val market = PublicInfoDataService.getInstance().getMarket(null)
      if(market == null) {
        5
      } else {
        if(fragments.isNotEmpty()) {
          0
        } else {
          3
        }

      }

    }
    sendWsHomepage(
      mIsVisibleToUser,
      temp,
      NetworkDataService.KEY_PAGE_HOME,
      NetworkDataService.KEY_HTTP_HOME,
      klineTime
    )
  }


  private val wsArrayTempList: ArrayList<JSONObject> = arrayListOf()
  private val wsArrayMap = hashMapOf<String, JSONObject>()
  private var wsTimeFirst: Long = 0L

  @Synchronized
  private fun callDataDiff(jsonObject: JSONObject): Pair<ArrayList<JSONObject>, HashMap<String, JSONObject>>? {
    if(System.currentTimeMillis() - wsTimeFirst >= 1000L && wsTimeFirst != 0L) {
      // 大于一秒
      wsTimeFirst = 0L
      if(wsArrayMap.size != 0) {
        return Pair(wsArrayTempList, wsArrayMap)
      }
    } else {
      if(wsTimeFirst == 0L) {
        wsTimeFirst = System.currentTimeMillis()
      }
      wsArrayTempList.add(jsonObject)
      wsArrayMap.put(jsonObject.optString("channel", ""), jsonObject)
    }
    return null
  }


}
