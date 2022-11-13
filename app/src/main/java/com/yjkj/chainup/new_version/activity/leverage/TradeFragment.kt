package com.yjkj.chainup.new_version.activity.leverage


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.grid.NGridFragment
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.fragment.NCVCTradeFragment
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.getVisible
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_trade.*
import org.jetbrains.anko.textColor


/**
 * A simple [Fragment] subclass.
 * Use the [TradeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TradeFragment : NBaseFragment() {
    override fun setContentView() = R.layout.fragment_trade

    private var currentFragment = Fragment()

    companion object {
        var currentIndex = ParamConstant.CVC_INDEX_TAB
        var liveData4DepthData = MutableLiveData<MessageEvent>()
    }

    override fun initView() {
        btn_bb?.text = LanguageUtil.getString(context, "mainTab_text_transaction")
        btn_lever?.text = LanguageUtil.getString(context, "contract_action_lever")
        btn_otc?.text = LanguageUtil.getString(context, "mainTab_text_otc")
        btn_grid?.text = LanguageUtil.getString(context, "quant_grid_title")

        NLiveDataUtil.observeData(this, Observer<MessageEvent> {
            if (null != it) {
                if (MessageEvent.home_event_page_market_type == it.msg_type) {
                    LogUtil.v(TAG, "MessageEvent 处理market ${it}")
                    if (!cvcTradeFragment.isInitSymbol()) {
                        initViewData()
                        cvcTradeFragment.changeInitData()
                        val isLever = PublicInfoDataService.getInstance().isLeverOpen(null)
                        if (isLever) {
                            leverFragment.changeInitData()
                        }
                    }
                    setGrdiView()
                }
                if (MessageEvent.grid_data_update_type == it.msg_type) {
                    setGrdiView()
                }
            }
        })

        initViewData()

        if (PublicInfoDataService.getInstance().isGridTradSwitch(null)) {
            WsAgentManager.instance.addWsCallback(gridFragment)
        }
        // 币币
        btn_bb?.setOnClickListener {
            tabSelected(ParamConstant.CVC_INDEX_TAB)
        }
        //网格
        btn_grid?.setOnClickListener {
            tabSelected(ParamConstant.GRID_INDEX_TAB)
        }
        // otc
        btn_otc?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
        }
        // 杠杆
        btn_lever?.setOnClickListener {
            tabSelected(ParamConstant.LEVER_INDEX_TAB)
            if (!PublicInfoDataService.getInstance().hasShownLeverStatusDialog()) {
                NewDialogUtils.showLeverDialog(context
                        ?: return@setOnClickListener,
                        listener = object : NewDialogUtils.DialogTransferBottomListener {
                            override fun sendConfirm() {

                            }

                            override fun showCancel() {
                                tabSelected(ParamConstant.CVC_INDEX_TAB)
                            }

                        })
            }
        }

    }

    private fun setGrdiView() {
        if (PublicInfoDataService.getInstance().isGridTradSwitch(null)) {
            var gridList = NCoinManager.getGridCroupList(null)
            if (gridList == null || gridList.size == 0) {
                btn_grid.visibility = View.GONE
                switchFragment(cvcTradeFragment)
                cvcTradeFragment.sendAgentData()
                gridFragment.unbindAgentData()
                action4Selected(btn_bb)
                action4Selected(btn_lever, false)
                action4Selected(btn_grid, false)
            } else {
                WsAgentManager.instance.addWsCallback(gridFragment)
                btn_grid.visibility = View.VISIBLE
            }
        } else {
            switchFragment(cvcTradeFragment)
            cvcTradeFragment.sendAgentData()
            gridFragment.unbindAgentData()
            action4Selected(btn_bb)
            action4Selected(btn_lever, false)
            action4Selected(btn_grid, false)
            btn_grid.visibility = View.GONE
        }
    }


    private fun tabSelected(tabIndex: Int = ParamConstant.CVC_INDEX_TAB) {
        currentIndex = tabIndex
        val isGrid = tabIndex == ParamConstant.GRID_INDEX_TAB
        NLiveDataUtil.postValue(MessageEvent(MessageEvent.TAB_TYPE, null, tabIndex == ParamConstant.LEVER_INDEX_TAB,isGrid))
        action4Selected(btn_bb, ParamConstant.CVC_INDEX_TAB == tabIndex)
        action4Selected(btn_lever, ParamConstant.LEVER_INDEX_TAB == tabIndex)
        action4Selected(btn_grid, ParamConstant.GRID_INDEX_TAB == tabIndex)

        when (tabIndex) {
            ParamConstant.CVC_INDEX_TAB -> {
                switchFragment(cvcTradeFragment)
                cvcTradeFragment.sendAgentData()
                gridFragment.unbindAgentData()
            }
            ParamConstant.LEVER_INDEX_TAB -> {
                switchFragment(leverFragment)
                cvcTradeFragment.unbindAgentData()
                gridFragment.unbindAgentData()
            }
            else -> {
                switchFragment(gridFragment)
                cvcTradeFragment.unbindAgentData()
                gridFragment.sendAgentData()
            }
        }
    }


    private fun action4Selected(view: SuperTextView?, isSelected: Boolean = true) {
        view?.run {
            solid = ColorUtil.getColor(if (isSelected) R.color.transparent else R.color.transparent)
            strokeColor = ColorUtil.getColor(if (isSelected) R.color.trade_main_blue else R.color.transparent)
            textColor = ColorUtil.getColor(if (isSelected) R.color.trade_main_blue else R.color.normal_text_color)
        }
    }

     //币币
    private val cvcTradeFragment = NCVCTradeFragment()
    //杠杆
    private val leverFragment = NLeverFragment.newInstance("", "")
    private val gridFragment = NGridFragment()
    private fun switchFragment(targetFragment: Fragment) {
        val transaction = childFragmentManager
                .beginTransaction()
        if (!targetFragment.isAdded) {
            transaction
                    .hide(currentFragment)
                    .add(R.id.fragment_container, targetFragment)
        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment)
        }
        currentFragment = targetFragment
        transaction.commitAllowingStateLoss()
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        LogUtil.d(TAG, "fragmentVisible==TradeFragment==isVisible is $isVisible  isVisibleToUser $isVisibleToUser")
        if (isVisibleToUser) {
            when (currentIndex) {
                ParamConstant.LEVER_INDEX_TAB -> {
                    cvcTradeFragment.fragmentVisibile(false)
                    gridFragment.fragmentVisibile(false)
                    leverFragment.fragmentVisibile(true)
                }
                ParamConstant.CVC_INDEX_TAB -> {
                    cvcTradeFragment.fragmentVisibile(true)
                    leverFragment.fragmentVisibile(false)
                    gridFragment.fragmentVisibile(false)
                }
                ParamConstant.GRID_INDEX_TAB -> {
                    gridFragment.fragmentVisibile(true)
                    cvcTradeFragment.fragmentVisibile(false)
                    leverFragment.fragmentVisibile(false)
                }
            }
        } else {
            cvcTradeFragment.fragmentVisibile(false)
            leverFragment.fragmentVisibile(false)
            gridFragment.fragmentVisibile(false)
        }
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.coinTrade_tab_type == event.msg_type) {
            val msgContent = event.msg_content
            if (null != msgContent && msgContent is Bundle) {
                currentIndex = msgContent.getInt(ParamConstant.COIN_TRADE_TAB_INDEX)
                var msgEvent: MessageEvent? = null
                when (currentIndex) {
                    ParamConstant.LEVER_INDEX_TAB -> {
                        switchFragment(leverFragment)
                        action4Selected(btn_bb, false)
                        action4Selected(btn_grid, false)
                        action4Selected(btn_lever)
                        msgEvent = MessageEvent(MessageEvent.leverTrade_topTab_type)
                        msgEvent.isLever = true
                    }
                    ParamConstant.CVC_INDEX_TAB -> {
                        switchFragment(cvcTradeFragment)
                        action4Selected(btn_bb)
                        action4Selected(btn_lever, false)
                        action4Selected(btn_grid, false)
                        msgEvent = MessageEvent(MessageEvent.coinTrade_topTab_type)
                        msgEvent.isLever = false
                    }
                    else -> {
                        switchFragment(gridFragment)
                        action4Selected(btn_bb, false)
                        action4Selected(btn_lever, false)
                        action4Selected(btn_grid)
                        msgEvent = MessageEvent(MessageEvent.grid_topTab_type)
                        msgEvent.isLever = false
                    }
                }
                LogUtil.d(TAG, "observeData==TradeFragment==币币交易==currentIndex is $currentIndex,msg_content is $msgContent")
                msgEvent.msg_content = msgContent as Bundle
                EventBusUtil.post(msgEvent)
            }
        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        LogUtil.e(TAG, "onVisibleChanged==TradeFragment() $isVisible ")
        if (isVisible) {
            if (currentFragment is NCVCTradeFragment) {
                cvcTradeFragment.sendAgentData()
            }
            if (currentFragment is NGridFragment) {
                gridFragment.sendAgentData()
            }
        } else {
            if (currentFragment is NCVCTradeFragment) {
                cvcTradeFragment.unbindAgentData()
            }
            if (currentFragment is NGridFragment) {
                gridFragment.unbindAgentData()
            }
        }
    }

    override fun background() {
        super.background()

    }

    override fun foreground() {
        super.foreground()
    }

    private fun initViewData() {
        if (NCoinManager.getMarketIsHeader()) {
            rl_header?.visibility = View.VISIBLE
        } else {
            rl_header?.visibility = View.GONE
        }
        val otcOpen = PublicInfoDataService.getInstance().otcOpen(null)
        btn_otc.visibility = otcOpen.getVisible()
        btn_lever.visibility = NCoinManager.getMarketLeverOpen().getVisible()
        if (PublicInfoDataService.getInstance().isGridTradSwitch(null)) {
            var gridList = NCoinManager.getGridCroupList(null)
            if (gridList == null || gridList.size == 0) {
                btn_grid.visibility = View.GONE
            } else {
                btn_grid.visibility = View.VISIBLE
            }
        } else {
            btn_grid.visibility = View.GONE
        }
        if (currentIndex == ParamConstant.CVC_INDEX_TAB) {
            switchFragment(cvcTradeFragment)
            action4Selected(btn_bb)
            action4Selected(btn_lever, false)
            action4Selected(btn_grid, false)
        } else if (currentIndex == ParamConstant.LEVER_INDEX_TAB) {
            switchFragment(leverFragment)
            action4Selected(btn_bb, false)
            action4Selected(btn_grid, false)
            action4Selected(btn_lever)
        } else if (currentIndex == ParamConstant.GRID_INDEX_TAB) {
            switchFragment(gridFragment)
            action4Selected(btn_bb, false)
            action4Selected(btn_lever, false)
            action4Selected(btn_grid)
        }
        action4Selected(btn_otc, false)
    }

}
