package com.yjkj.chainup.new_version.activity.like

import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.loopeer.itemtouchhelperextension.ItemTouchHelperExtension
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.HeYueLikeDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyMarketForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.getSymbols
import com.yjkj.chainup.util.getVisible
import com.yjkj.chainup.wedegit.item.SpacesItemDecoration
import kotlinx.android.synthetic.main.activity_edit_like_heyue.*
import kotlinx.android.synthetic.main.include_edit_coin_bottom.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject

@Route(path = RoutePath.LikeEditHeYueActivity)
class LikeHeYueEditActivity : NBaseActivity(), EditHeYueDragListener {
    var isLogin = false
    var operationType = 0
    var adapter: MarketHeYueEditAdapter? = null
    private var normalTickList = arrayListOf<JSONObject>()
    override fun setContentView(): Int {
        return R.layout.activity_edit_like_heyue
    }


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initRecylerView()
        initAdapter()
        isLogin = UserDataService.getInstance().isLogined
        iv_edit.onClick {
            finish()
        }
        iv_search.onClick {
            ArouterUtil.greenChannel(RoutePath.HeYueMapActivity, Bundle())
        }
        ll_item_all.onClick {
            adapter?.apply {
                selectAllCoins()
                initSelectTools()
            }
        }
        ll_item_delete.onClick {
            adapter?.apply {
                if (isSelectSymbol()) {
                    NewDialogUtils.showNormalDialog(this@LikeHeYueEditActivity,
                            LanguageUtil.getString(this@LikeHeYueEditActivity, "new_confrim_likes"),
                            object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    upload(true)
                                }
                            })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val items = getCollecData()
        normalTickList.clear()
        if (items != null) {
            normalTickList.addAll(items)
        }
        adapter?.setList(normalTickList)
        initSelectTools()

    }

    private fun initRecylerView() {
        rv_market_detail?.apply {
            layoutManager = LinearLayoutManager(this@LikeHeYueEditActivity)
            addItemDecoration(SpacesItemDecoration())
        }

    }

    private fun initAdapter() {
        adapter = MarketHeYueEditAdapter(normalTickList)
        adapter?.addChildClickViewIds(R.id.layout_check_item)
        adapter?.editDragListener = this
        rv_market_detail?.adapter = adapter
        rv_market_detail?.setHasFixedSize(true)
        val emptyForAdapterView = EmptyMarketForAdapterView(this)
        adapter?.setEmptyView(emptyForAdapterView)
        adapter?.emptyLayout?.findViewById<LinearLayout>(R.id.layout_add_like)?.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.HeYueMapActivity, Bundle())
        }
        adapter?.setOnItemChildClickListener { mAdapter, view, position ->
            adapter?.apply {
                selectCurrent(position)
                initSelectTools()
            }
        }
        (rv_market_detail.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        val itemTouchCallback = CoinsManageHeYueTouchHelperCallback(adapter!!)
        val itemTouchHelper = ItemTouchHelperExtension(itemTouchCallback)
        adapter?.itemTouchHelperExtension = itemTouchHelper
        itemTouchHelper.attachToRecyclerView(rv_market_detail)
    }



    private fun getCollecData(): ArrayList<JSONObject>? {
        return HeYueLikeDataService.getInstance().getCollecData(false)
    }

    override fun onDragListener1() {
        LogUtil.e(TAG, "onDrag()")
        HeYueLikeDataService.getInstance().apply {
            clearAllCollect()
            // ??????????????????
            saveCollecData(normalTickList)
            upload()
        }
    }

    private fun initSelectTools() {
        adapter?.apply {
            check_select.isChecked = isSelectAllSymbol()
            tv_delete.isEnabled = isSelectSymbol()
            type_sort.visibility = (data.size != 0).getVisible()
            iv_delete.imageResource = if (isSelectSymbol()) R.mipmap.favorites_delete_highlight
            else R.mipmap.favorites_delete
        }
    }

    private fun upload(isDelete: Boolean = false) {
        if (!LoginManager.isLogin(this)) {
            delete(isDelete, false)
            return
        }
        val symbols = when (isDelete) {
            true -> adapter?.getSelectSymbolsInvert()!!
            else -> normalTickList.getSymbols()
        }
        delete(isDelete)
        showLoadingDialog()
        MainModel().likesCoinsUpload(symbols, "BTC-USDT", object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()
                delete(isDelete)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                closeLoadingDialog()
            }
        })
    }

    private fun delete(isDelete: Boolean, isLogin: Boolean = true) {
        if (isDelete) {
            // ????????????
            adapter?.apply {
                val newAll = getNewSymbolsInvert()
                replaceData(newAll)
                resetSelect()
                HeYueLikeDataService.getInstance().clearAllCollect()
                if (newAll.size != 0) {
                    HeYueLikeDataService.getInstance().saveCollecData(newAll)
                }

            }
            initSelectTools()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.symbol_switch_type == event.msg_type) {
            finish()
        }
    }

}


