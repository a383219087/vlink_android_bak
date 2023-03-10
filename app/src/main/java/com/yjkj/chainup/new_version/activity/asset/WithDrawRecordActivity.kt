package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.chainup.contract.adapter.CpCoinSelectLeftAdapter
import com.chainup.contract.adapter.CpCoinSelectRightAdapter
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.setSafeListener
import com.chainup.contract.view.CpNewDialogUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.CashFlowDetailActivity
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.activity.CoinActivity.Companion.SELECTED_TYPE
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.adapter.Cash4ContractAdapter
import com.yjkj.chainup.new_version.adapter.CashFlow4Adapter
import com.yjkj.chainup.new_version.adapter.CashFlowAdapter
import com.yjkj.chainup.new_version.adapter.WithDrawAdapter
import com.yjkj.chainup.new_version.view.*
import com.yjkj.chainup.treaty.bean.ContractCashFlowBean
import com.yjkj.chainup.util.DisplayUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_withdraw_record.*
import org.json.JSONArray


/**
 * @Author lianshangljl
 * @Date 2019/5/17-12:02 PM
 * @Email buptjinlong@163.com
 * @description ????????????
 */
class WithDrawRecordActivity : NewBaseActivity() {
    var selectedCoin: String = ""
    var symbol: String = ""
    var transactionScene: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var adapter: CashFlowAdapter? = null
    var adapter4Deposit: CashFlow4Adapter? = null
    var adapter4Contract: Cash4ContractAdapter? = null
    var adapterContract: CashFlowAdapter? = null
    var page: Int = 1
    var isScrollstatus = true
    var isContract = false
    var recordType = 0

    private var isShowContractSelect: Boolean = false
    private var depthDialog: TDialog? = null
    var typeList = ArrayList<CpTabInfo>()
    private var selectType: CpTabInfo? = null
    private var mCurrentSelectExchange: CpTabInfo? = null

    companion object {
        const val RECORDTYPE = "RecordType"
        const val CONTRACTTYPE = "contractType"


        /**
         * @param selectedCoin ???????????????
         * @param transactionScene  ?????????????????????????????? key
         * @param type  ??????????????????????????????
         */
        fun enter2(context: Context, selectedCoin: String, transactionScene: String, type: Int, symbol: String = "") {
            var intent = Intent()
            intent.setClass(context, WithDrawRecordActivity::class.java)
            intent.putExtra(CoinActivity.SELECTED_COIN, selectedCoin)
            intent.putExtra(SELECTED_TYPE, transactionScene)
            intent.putExtra(ParamConstant.symbol, symbol)
            intent.putExtra(CONTRACTTYPE, false)
            intent.putExtra(RECORDTYPE, type)
            context.startActivity(intent)
        }

        fun enter4Contract(context: Context, selectedCoin: String) {
            var intent = Intent()
            intent.setClass(context, WithDrawRecordActivity::class.java)
            intent.putExtra(CoinActivity.SELECTED_COIN, selectedCoin)
            intent.putExtra(SELECTED_TYPE, "TRANSFER")
            intent.putExtra(CONTRACTTYPE, true)
            intent.putExtra(RECORDTYPE, TRANSFER)
            context.startActivity(intent)
        }

        fun enterContractTransfer(context: Context, selectedCoin: String) {
            var intent = Intent()
            intent.setClass(context, WithDrawRecordActivity::class.java)
            intent.putExtra(CoinActivity.SELECTED_COIN, selectedCoin)
            intent.putExtra(SELECTED_TYPE, ParamConstant.CONTRACT_TRANSFER_RECORD)
            intent.putExtra(CONTRACTTYPE, true)
            intent.putExtra(RECORDTYPE, CONTRACT_TRANSFER_HISTORY)
            context.startActivity(intent)
        }

    }

    var isfrist = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_record)
        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (spw_layout?.visibility == View.VISIBLE) {
                    spw_layout?.visibility = View.GONE
                } else {
                    spw_layout?.visibility = View.VISIBLE
                    if (isfrist) {
                        isfrist = false
//                        spw_layout?.setMage()
                    }
                }
            }

            override fun onclickName() {

            }

        }

        tv_date?.text = LanguageUtil.getString(this, "charge_text_date")
        tv_number_title?.text = LanguageUtil.getString(this, "charge_text_volume")
        tv_state?.text = LanguageUtil.getString(this, "charge_text_state")
        getData()
        initRecordView()
        initRefresh()
        initTypeList()
        initEvent()
    }

    private fun initTypeList() {
        typeList.clear()
        val element = CpTabInfo(CpLanguageUtil.getString(this, "cl_coflowingwater_all"), 0)
        selectType = element
        typeList.add(element)
        typeList.add(CpTabInfo(CpLanguageUtil.getString(this,"cl_coflowingwater_transferin"),1))
        typeList.add(CpTabInfo(CpLanguageUtil.getString(this,"cl_coflowingwater_transferout"),2))
    }

    private fun initEvent() {
        ll_sel_coins.setOnClickListener {
            setCoinData()
            if (!isShowContractSelect) {
                ll_coin_select.visibility = View.VISIBLE
                img_coins_name_arrow.animate().setDuration(200).rotation(180f).start()
            } else {
                ll_coin_select.visibility = View.GONE
                img_coins_name_arrow.animate().setDuration(200).rotation(0f).start()
            }
            isShowContractSelect = !isShowContractSelect
        }
        ll_contract_type?.setSafeListener{
            if (typeList.size == 0) return@setSafeListener
            img_order_type_arrow.animate().setDuration(200).rotation(180f).start()
            depthDialog = CpNewDialogUtils.showNewBottomListDialog(
                context,
                typeList,
                selectType!!.index,
                object : CpNewDialogUtils.DialogOnItemClickListener {
                    override fun clickItem(index: Int) {
                        if (selectType != typeList[index]) {
                            selectType = typeList[index]
                            tv_order_type.setText(selectType?.name)
                        }
                        depthDialog?.dismiss()
                        depthDialog = null
                    }
                },
                object:DialogInterface.OnDismissListener{
                    override fun onDismiss(p0: DialogInterface?) {
                        img_order_type_arrow.animate().setDuration(200).rotation(0f).start()
                    }

                }
            )
        }
    }

    private fun setCoinData() {
        val sideList = ArrayList<CpTabInfo>()
        val sideListBuff = ArrayList<CpTabInfo>()
        val sideListU = ArrayList<CpTabInfo>()
        val sideListB = ArrayList<CpTabInfo>()
        val sideListH = ArrayList<CpTabInfo>()
        val sideListM = ArrayList<CpTabInfo>()

        var isHasU = false //????????????
        val isHasB = false //?????????
        var isHasH = false //????????????
        var isHasM = false //????????????
        val mContractList = JSONArray(CpClLogicContractSetting.getContractJsonListStr(this))
        var positionLeft = 0
        for (i in 0 until mContractList.length()) {
            val obj = mContractList.getJSONObject(i)
            val contractType = obj.getString("contractType")
            val id = obj.getInt("id")

            //classification 1,USDT?????? 2,??????????????? 3,???????????? 4,????????????
            when (contractType) {
                "E" -> {
                    isHasU = true
                    sideListU.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                "S" -> {
                    isHasM = true
                    sideListM.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                else -> {
                    isHasH = true
                    sideListH.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
            }
            if (CpContractEntrustNewActivity.mContractId == id) {
                positionLeft = when (contractType) {
                    "E" -> {
                        0
                    }
                    "S" -> {
                        3
                    }
                    else -> {
                        2
                    }
                }
            }
        }
        if (isHasU) {
            sideList.add(CpTabInfo(getString(com.chainup.contract.R.string.cp_contract_data_text13), 0, if (positionLeft == 0) 0 else 1))
        }
        if (isHasB) {
            sideList.add(CpTabInfo(getString(com.chainup.contract.R.string.cp_contract_data_text10), 1, if (positionLeft == 1) 0 else 1))
        }
        if (isHasH) {
            sideList.add(CpTabInfo(getString(com.chainup.contract.R.string.cp_contract_data_text12), 2, if (positionLeft == 2) 0 else 1))
        }
        if (isHasM) {
            sideList.add(CpTabInfo(getString(com.chainup.contract.R.string.cp_contract_data_text11), 3, if (positionLeft == 3) 0 else 1))
        }
        when(positionLeft) {
            0 -> {
                sideListBuff.addAll(sideListU)
            }
            1 -> {
                sideListBuff.addAll(sideListB)
            }
            3 -> {
                sideListBuff.addAll(sideListM)
            }
            else -> {
                sideListBuff.addAll(sideListH)
            }
        }
        var index = -1
        for (i in 0 until sideListBuff.size){
            if(sideListBuff[i].name==symbol){
                index = i
                break
            }
        }
        val mRightAdapter = CpCoinSelectRightAdapter(sideListBuff, index)
        rv_right?.layoutManager = LinearLayoutManager(this)
        rv_right?.adapter = mRightAdapter
        rv_right?.setHasFixedSize(true)
        mRightAdapter.setOnItemClickListener { adapter, view, position ->
            mCurrentSelectExchange = CpTabInfo(sideListBuff[position].name, position)
            tv_coins_name.text = sideListBuff[position].name
            ll_coin_select.visibility = View.GONE
            img_coins_name_arrow.animate().setDuration(200).rotation(0f).start()
        }
        val adapter = CpCoinSelectLeftAdapter(sideList, positionLeft)
        rv_left?.layoutManager = LinearLayoutManager(this)
        rv_left?.adapter = adapter
        rv_left?.setHasFixedSize(true)
        adapter.setOnItemClickListener { adapter, view, position ->
            sideListBuff.clear()
            when (sideList[position].index) {
                0 -> {
                    sideListBuff.addAll(sideListU)
                }
                1 -> {
                    sideListBuff.addAll(sideListB)
                }
                2 -> {
                    sideListBuff.addAll(sideListH)
                }
                else -> {
                    sideListBuff.addAll(sideListM)
                }
            }
            mRightAdapter.notifyDataSetChanged()

            for (buff in sideList) {
                buff.extrasNum = if (buff.index == sideList[position].index) 0 else 1
            }
            adapter.notifyDataSetChanged()
        }
    }

    fun getData() {
        if (intent != null) {
            selectedCoin = intent.getStringExtra(CoinActivity.SELECTED_COIN) ?: ""
            isContract = intent.getBooleanExtra(CONTRACTTYPE, false)
            transactionScene = intent.getStringExtra(SELECTED_TYPE) ?: ""
            symbol = intent.getStringExtra(ParamConstant.symbol) ?: ""
            recordType = intent.getIntExtra(RECORDTYPE, 0)
            if (TextUtils.isEmpty(symbol)) {
                symbol = selectedCoin
            }
        }
    }

    fun initRecordView() {
        when (recordType) {
            /**
             * ????????????
             */
            ASSETTOPUP -> {
                title_layout?.setContentTitle(LanguageUtil.getString(this, "title_history"))
                title_layout?.setRightVisible(false)

                ll_label_layout?.visibility = View.GONE
                tv_number_title?.text = LanguageUtil.getString(context, "charge_text_volume")
                tv_state?.text = LanguageUtil.getString(context, "charge_text_state")
                spw_layout?.assetTopUpListener = object : ScreeningPopupWindowView.AssetTopUpListener {
                    override fun returnAssetTopUpTime(startTime: String, end: String) {
                        getOtherRecord(symbol, ParamConstant.TRANSFER_DEPOSIT_RECORD, startTime, end, true)
                        spw_layout?.visibility = View.GONE
                    }
                }
            }
            /**
             * ????????????
             */
            WITHDRAWTYPE -> {
                ll_sel_ctrl?.visibility = View.VISIBLE
                ll_label_layout?.visibility = View.GONE
                spw_layout?.setInitView(WITHDRAWTYPE)
//                collapsing_toolbar?.title = NCoinManager.getShowMarket(selectedCoin) + " " + LanguageUtil.getString(context, "withdraw_action_withdrawHistory")
//                title_layout?.setContentTitle(LanguageUtil.getString(this, "title_history"))
                title_layout?.setTvTitle(false)
                title_layout?.setRightVisible(false)
                tv_number_title?.text = LanguageUtil.getString(context, "charge_text_volume")
                tv_state?.text = LanguageUtil.getString(context, "charge_text_state")
                spw_layout?.widthDrawListener = object : ScreeningPopupWindowView.WithDrawListener {
                    override fun returnWithDrawTime(startTime: String, end: String, type: Int) {
                        withdrawTypeMain = type
                        getOtherRecord(symbol, ParamConstant.TRANSFER_WITHDRAW_RECORD, startTime, end, true)
                        spw_layout?.visibility = View.GONE
                    }
                }
            }
            /**
             * ????????????
             */
            TRANSFER -> {
                ll_label_layout?.visibility = View.GONE
                title_layout?.setContentTitle(NCoinManager.getShowMarket(selectedCoin) + " " + LanguageUtil.getString(context, "transfer_text_record"))
                tv_number_title?.text = LanguageUtil.getString(context, "charge_text_volume")
                tv_state?.text = LanguageUtil.getString(context, "contract_text_type")
                if (isContract) {
                    title_layout?.setRightVisible(false)
                } else {
                    spw_layout?.setInitView(TRANSFER)
                    spw_layout?.setShowTransfer(true)
                    spw_layout?.transferListener = object : ScreeningPopupWindowView.TransferListener {
                        override fun returnTransfer(transferType: Int, begin: String, end: String) {
                            transferTypeMain = transferType
                            getOtherRecord(symbol, ParamConstant.OTC_TRANSFER_RECORD, begin, end, true)
                            spw_layout?.visibility = View.GONE
                        }
                    }
                }
            }
            /**
             * ????????????
             */
            CONTRACT_TRANSFER_HISTORY -> {
                title_layout?.setContentTitle(NCoinManager.getShowMarket(selectedCoin) + " " + LanguageUtil.getString(context, "transfer_text_record"))
                tv_number_title?.text = LanguageUtil.getString(context, "charge_text_volume")
                tv_state?.text = LanguageUtil.getString(context, "contract_text_type")
                spw_layout?.setInitView(CONTRACT_TRANSFER_HISTORY)
                spw_layout?.setShowTransfer(true)
                spw_layout?.transferListener = object : ScreeningPopupWindowView.TransferListener {
                    override fun returnTransfer(transferType: Int, begin: String, end: String) {
                        transferTypeMain = transferType
                        getOtherRecord(selectedCoin, ParamConstant.CONTRACT_TRANSFER_RECORD, begin, end, true)
                        spw_layout?.visibility = View.GONE
                    }
                }
            }

            /**
             * ????????????
             */
            TRANSFER_RECORD -> {
                title_layout?.setContentTitle("??????????????????")
                tv_number_title?.text = LanguageUtil.getString(context, "charge_text_volume")
                spw_layout?.setInitView(TRANSFER)
                spw_layout?.setShowTransfer(true)
                spw_layout?.transferListener = object : ScreeningPopupWindowView.TransferListener {
                    override fun returnTransfer(transferType: Int, begin: String, end: String) {
                        transferTypeMain = transferType
                        getOtherRecord(symbol, ParamConstant.DIRECTLY_WITHDRAW_TYPE, begin, end, true)
                        spw_layout?.visibility = View.GONE
                    }
                }


            }
        }
    }

    var transferTypeMain = 0
    var withdrawTypeMain = 0

    override fun onResume() {
        super.onResume()
        if (recordType == TRANSFER && isContract) {
            getBusinessTransferList(startTime, endTime, true)
        } else {
            getOtherRecord(symbol, transactionScene, startTime, endTime, true)
        }
    }

    fun initRefresh() {
        spw_layout?.assetTopUpListener = object : ScreeningPopupWindowView.AssetTopUpListener {

            override fun returnAssetTopUpTime(startTime: String, end: String) {
                endTime = end
                this@WithDrawRecordActivity.startTime = startTime
                page = 1

                if (recordType == TRANSFER && isContract) {
                    getBusinessTransferList(startTime, end, true)
                } else {
                    getOtherRecord(symbol, transactionScene, startTime, end, true)
                }

            }


        }

        /**
         * ?????????????????????
         */
        swipe_refresh?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                page = 1
                isScrollstatus = true
                getOtherRecord(symbol, transactionScene, startTime, endTime, true)
            }
        })


        recycler_view?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter?.itemCount && isScrollstatus) {
                    page += 1
                    if (recordType == TRANSFER && isContract) {
                        getBusinessTransferList(startTime, endTime, false)
                    } else {
                        getOtherRecord(symbol, transactionScene, startTime, endTime, false)
                    }

                }
            }

        })
    }

    var list: ArrayList<CashFlowBean.Finance> = arrayListOf()
    var contractList: ArrayList<ContractCashFlowBean.Transactions> = arrayListOf()

    fun refreshView(bean: ArrayList<CashFlowBean.Finance>) {
        if (bean.isEmpty()) {
            return
        }
        list.addAll(bean)
        adapter?.notifyDataSetChanged()
        adapter4Deposit?.notifyDataSetChanged()
    }

    fun refreshView4Contract(contractList: ArrayList<ContractCashFlowBean.Transactions>) {
        if (contractList.isEmpty()) {
            return
        }
        contractList.addAll(contractList)
        adapter4Contract?.notifyDataSetChanged()
    }


    fun initView4Contract() {
        if (recycler_view != null) {
            recycler_view.layoutManager = LinearLayoutManager(this@WithDrawRecordActivity)
            adapter4Contract = Cash4ContractAdapter(contractList)
            adapter4Contract?.setEmptyView(EmptyForAdapterView(this))
            recycler_view.adapter = adapter4Contract
        }
    }

    fun initView() {
        if (recycler_view != null) {
            recycler_view.layoutManager = LinearLayoutManager(this@WithDrawRecordActivity)
            when (recordType) {
                /**
                 * ????????????
                 */
                ASSETTOPUP -> {
                    adapter4Deposit = CashFlow4Adapter(LanguageUtil.getString(this, "assets_action_chargeCoin"))
                    adapter4Deposit?.setEmptyView(EmptyForAdapterView(this))
                    recycler_view.adapter = adapter4Deposit
                    adapter4Deposit?.setOnItemClickListener { adapter, view, position ->
                        CashFlowDetailActivity.liveData4CashFlowBean.postValue(list[position])
                        CashFlowDetailActivity.enter2(this@WithDrawRecordActivity, ParamConstant.TRANSFER_DEPOSIT_RECORD)
                    }
                    adapter4Deposit?.setList(list)
                }
                /**
                 * ????????????
                 */
                WITHDRAWTYPE -> {
                    adapter4Deposit = WithDrawAdapter()
                    adapter4Deposit?.setEmptyView(EmptyForAdapterView(this))
                    recycler_view.adapter = adapter4Deposit
                    adapter4Deposit?.setOnItemClickListener { adapter, view, position ->
                        CashFlowDetailActivity.liveData4CashFlowBean.postValue(list[position])
                        CashFlowDetailActivity.enter2(this@WithDrawRecordActivity, ParamConstant.TRANSFER_WITHDRAW_RECORD)
                    }
                    adapter4Deposit?.setList(list)
                }
                /**
                 * ????????????
                 */
                TRANSFER -> {

                    adapter4Deposit = CashFlow4Adapter(LanguageUtil.getString(this, "assets_action_transfer"))
                    adapter4Deposit?.setEmptyView(EmptyForAdapterView(this))
                    recycler_view.adapter = adapter4Deposit
                    adapter4Deposit?.setOnItemClickListener { adapter, view, position ->
                        CashFlowDetailActivity.liveData4CashFlowBean.postValue(list[position])
                        CashFlowDetailActivity.enter2(this@WithDrawRecordActivity, ParamConstant.OTC_TRANSFER_RECORD)
                    }
                    adapter4Deposit?.setList(list)
                }
                /**
                 * ??????????????????
                 */
                CONTRACT_TRANSFER_HISTORY -> {

                    adapterContract = CashFlowAdapter(list)
                    adapterContract?.index = OTHER_INDEX
                    adapterContract?.setEmptyView(EmptyForAdapterView(this))
                    recycler_view.adapter = adapterContract
//                    adapter4Deposit?.setOnItemClickListener { adapter, view, position ->
//                        CashFlowDetailActivity.liveData4CashFlowBean.postValue(list[position])
//                        CashFlowDetailActivity.enter2(this@WithDrawRecordActivity,ParamConstant.OTC_TRANSFER_RECORD)
//                    }
                    adapterContract?.setList(list)
                }

                /**
                 * ??????????????????
                 */
                TRANSFER_RECORD -> {
                    adapterContract = CashFlowAdapter(list)
                    adapterContract?.setEmptyView(EmptyForAdapterView(this))
                    recycler_view.adapter = adapterContract
                    adapterContract?.setOnItemClickListener { adapter, view, position ->
                        CashFlowDetailActivity.liveData4CashFlowBean.postValue(list[position])
                        CashFlowDetailActivity.enter2(this@WithDrawRecordActivity, ParamConstant.DIRECTLY_WITHDRAW_TYPE)
                    }
                    adapterContract?.setList(list)
                }
            }
        }
    }

    /**
     * ??????????????????
     */
    fun getOtherRecord(symbol: String, transactionScene: String, startTime: String, endTime: String, refresh: Boolean) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        if ("contract_transfer".equals(transactionScene)) {
            HttpClient.instance.getTransferRecord(symbol, transactionScene, startTime, endTime, page = page.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : NetObserver<CashFlowBean>() {
                        override fun onHandleSuccess(t: CashFlowBean?) {
                            t ?: return
                            if (t?.financeList.isEmpty()) {
                                swipe_refresh?.isRefreshing = false
                            }
                            if (t?.financeList != null) {
                                if (t.financeList.size < 20) {
                                    isScrollstatus = false
                                }

                                var beans: ArrayList<CashFlowBean.Finance> = arrayListOf()
                                if (refresh) {
                                    when (transactionScene) {
                                        /**
                                         * ????????????
                                         */
                                        ParamConstant.CONTRACT_TRANSFER_RECORD -> {
                                            when (transferTypeMain) {
                                                0 -> {
                                                    setListData(t.financeList as ArrayList<CashFlowBean.Finance>)
                                                    initView()
                                                }
                                                1 -> {
                                                    t.financeList.forEach {
                                                        if (it.status == 1) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()

                                                }
                                                2 -> {
                                                    t.financeList.forEach {
                                                        if (it.status == 2) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    refreshView(t.financeList as ArrayList<CashFlowBean.Finance>)
                                }
                            }
                            swipe_refresh?.isRefreshing = false
                        }

                        override fun onHandleError(code: Int, msg: String?) {
                            super.onHandleError(code, msg)
                            DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                            swipe_refresh?.isRefreshing = false
                        }
                    })
        } else {
            HttpClient.instance.otherTransList4V2(symbol, transactionScene, startTime, endTime, page = page.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : NetObserver<CashFlowBean>() {
                        override fun onHandleSuccess(t: CashFlowBean?) {
                            t ?: return
                            if (t?.financeList.isEmpty()) {
                                swipe_refresh?.isRefreshing = false
                            }
                            if (t?.financeList != null) {
                                if (t.financeList.size < 20) {
                                    isScrollstatus = false
                                }

                                var beans: ArrayList<CashFlowBean.Finance> = arrayListOf()
                                if (refresh) {
                                    when (transactionScene) {
                                        /**
                                         * ??????
                                         */
                                        ParamConstant.OTC_TRANSFER_RECORD , ParamConstant.DIRECTLY_WITHDRAW_TYPE  -> {
                                            setListData(t.financeList as ArrayList<CashFlowBean.Finance>)
                                            initView()
                                        }
                                        /**
                                         * ??????
                                         */
                                        ParamConstant.TRANSFER_WITHDRAW_RECORD -> {
                                            when (withdrawTypeMain) {
                                                0 -> {
                                                    setListData(t.financeList as ArrayList<CashFlowBean.Finance>)
                                                    initView()
                                                }
                                                1 -> {
                                                    t.financeList.forEach {
                                                        if (it.status == 0) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()

                                                }
                                                2 -> {
                                                    t.financeList.forEach {
                                                        if (it.status != 0) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()
                                                }
                                            }
                                        }
                                        /**
                                         * ??????
                                         */
                                        ParamConstant.TRANSFER_DEPOSIT_RECORD -> {
                                            setListData(t.financeList as ArrayList<CashFlowBean.Finance>)
                                            initView()
                                        }
                                        /**
                                         * ????????????
                                         */
                                        ParamConstant.CONTRACT_TRANSFER_RECORD -> {
                                            when (transferTypeMain) {
                                                0 -> {
                                                    setListData(t.financeList as ArrayList<CashFlowBean.Finance>)
                                                    initView()
                                                }
                                                1 -> {
                                                    t.financeList.forEach {
                                                        if (it.status == 1) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()

                                                }
                                                2 -> {
                                                    t.financeList.forEach {
                                                        if (it.status == 2) {
                                                            beans.add(it)
                                                        }
                                                    }
                                                    setListData(beans)
                                                    initView()
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    refreshView(t.financeList as ArrayList<CashFlowBean.Finance>)
                                }
                            }
                            swipe_refresh?.isRefreshing = false
                        }

                        override fun onHandleError(code: Int, msg: String?) {
                            super.onHandleError(code, msg)
                            DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                            swipe_refresh?.isRefreshing = false
                        }
                    })
        }

    }

    /**
     * ????????????
     */
    fun setListData(financeList: ArrayList<CashFlowBean.Finance>) {
        list.clear()
        list.addAll(financeList)
    }


    fun getBusinessTransferList(startTime: String = "", endTime: String = "", refresh: Boolean) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        HttpClient.instance.getBusinessTransferList("TRANSFER", "", "", "", page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ContractCashFlowBean>() {
                    override fun onHandleSuccess(t: ContractCashFlowBean?) {
                        t ?: return
                        contractList = t?.transactionsList as ArrayList<ContractCashFlowBean.Transactions>?
                                ?: return
                        if (refresh) {
                            initView4Contract()
                        } else {
                            refreshView4Contract(t?.transactionsList as ArrayList<ContractCashFlowBean.Transactions>)
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }


}