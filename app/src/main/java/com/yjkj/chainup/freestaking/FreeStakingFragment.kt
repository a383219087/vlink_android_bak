package com.yjkj.chainup.freestaking


import android.os.Bundle
import com.timmy.tdialog.TDialog

import com.yjkj.chainup.R
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.wedegit.WrapContentViewPager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.freestaking.adapter.FreeStakingRecyclerAdapter
import com.yjkj.chainup.freestaking.bean.CurrencyBean
import com.yjkj.chainup.freestaking.bean.NotificationRefreshBean
import com.yjkj.chainup.util.DisplayUtil
import kotlinx.android.synthetic.main.fragment_free_staking.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 * FreeStakingActivity中的Fragment
 */
const val CUR_INDEX = "cur_index"
const val CONFIG_TYPE = "config_type"
const val ITEM_ID = "item_id"
const val PROJECT_TYPE = "project_type"

class FreeStakingFragment : BaseFragment(), NewDialogUtils.DialogOnclickListener {
    /**
     * 标志位，标志已经初始化完
     */
    private var isPrepared: Boolean = false
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private var mHasLoadedOnce: Boolean = false

    private var viewPager: WrapContentViewPager? = null
    private var curIndex: Int = 0
    private var configType: String = ""
    lateinit var adapter: FreeStakingRecyclerAdapter
    private var activityStatusDialog: TDialog? = null
    var symbolList = ArrayList<CurrencyBean>()
    private var tvStatus: TextView? = null
    var allList = ArrayList<CurrencyBean>()
    private var statusList = ArrayList<String>()
    private var showList = listOf<CurrencyBean>()
    private var selectItem = 3
    private var statusName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_free_staking, container, false)
        arguments?.let {
            this.curIndex = it.getInt(CUR_INDEX)
            this.configType = it.getString(CONFIG_TYPE) ?: ""
        }
        isPrepared = true
        lazyLoad()
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerView = View.inflate(context, R.layout.freestaking_recycler_head, null)
        tvStatus = headerView.findViewById(R.id.tv_status)
        statusName = getString(R.string.pos_string_all)
        tvStatus?.text = statusName
        statusList.clear()
        statusList.add(getString(R.string.pos_state_end))
        statusList.add(getString(R.string.pos_state_processing))
        statusList.add(getString(R.string.pos_state_start))
        statusList.add(getString(R.string.pos_string_all))
        if (viewPager != null) {
            viewPager?.setObjectForPosition(view, curIndex)
        }
        rv_freeStakingList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = FreeStakingRecyclerAdapter(symbolList)
        adapter.addHeaderView(headerView)
        rv_freeStakingList.adapter = adapter
//        adapter.bindToRecyclerView(rv_freeStakingList)
        val divider = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        divider.setDrawable(context?.let { getDrawable(it, R.drawable.item_decoration_shape) }!!)
        rv_freeStakingList.addItemDecoration(divider)
        adapter.notifyDataSetChanged()
        val emptyView = View.inflate(context, R.layout.item_new_empty, null)
        emptyView.minimumHeight = DisplayUtil.dip2px(404)
        adapter.setEmptyView( emptyView)
        adapter.headerWithEmptyEnable=true
        adapter.setOnItemClickListener { adapter, _, position ->
            //跳转
            ArouterUtil.greenChannel(RoutePath.PosDetailsActivity, Bundle().apply {
                putInt(ITEM_ID, (adapter?.getItem(position) as CurrencyBean).id)
                putInt(PROJECT_TYPE, (adapter?.getItem(position) as CurrencyBean).projectType)
            })
        }

        tvStatus?.setOnClickListener {
            activityStatusDialog = NewDialogUtils.showBottomListDialog(context!!, statusList, selectItem, this)

        }

    }

    /**
     * 重写方法去加载数据
     */
    override fun lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return
        }
        getFreeStakingList()

    }

    /**
     * 不可见的时候让筛选回到全部上面
     */
    override fun onInvisible() {
        if (tvStatus != null) {
            tvStatus?.text = getString(R.string.pos_string_all)
            selectItem = 3
        }
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        arguments?.let {
            curIndex = it.getInt(CUR_INDEX)
        }

    }

    @Subscribe
    fun refreshRecyclerList(bean: NotificationRefreshBean) {
        if ("refresh".equals(bean.event)) {
            clickItem(statusList, 3)

        } else if ("refreshStatus".equals(bean.event)) {
            //从详情回来以后刷新列表，因为此时Fragment没有重建，但是币的状态可能已经变了
            getFreeStakingList()

        }

    }

    override fun clickItem(data: ArrayList<String>, item: Int) {
        selectItem = item
        statusName = data[selectItem]
        tvStatus?.text = statusName
        when (selectItem) {
            0 -> {
                showList = symbolList.filter { (it.projectType == 1 && it.status == 3) || (it.projectType == 3 && it.status in arrayOf(2, 3, 4, 5, 6)) }
            }

            1 -> {
                showList = symbolList.filter { (it.projectType == 1 && it.status == 2) || (it.projectType == 3 && it.status in arrayOf(1)) }

            }

            2 -> {

                showList = symbolList.filter { (it.projectType == 1 && it.status == 1) || (it.projectType == 3 && it.status in arrayOf(0)) }
            }

            3 -> {

                showList = symbolList
            }

        }
        //重新设置数据
        adapter.setList(showList)
        activityStatusDialog?.dismiss()
    }


    companion object {
        @JvmStatic
        fun newInstance(viewPager: WrapContentViewPager, configType: String, position: Int) = FreeStakingFragment().apply {
            this.viewPager = viewPager
            arguments = Bundle().apply {
                putString(CONFIG_TYPE, configType)
                putInt(CUR_INDEX, position)

            }
        }


    }


    private fun getFreeStakingList() {
        HttpClient.instance.getFreeStakingList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ArrayList<CurrencyBean>>() {
                    override fun onHandleSuccess(arrayList: ArrayList<CurrencyBean>?) {
                        arrayList ?: return
                        if (curIndex == 0) {
                            symbolList.clear()
                            symbolList.addAll(arrayList)
                            adapter.notifyDataSetChanged()
                        } else {
                            allList.clear()
                            for (currencyBean in arrayList) {
                                if (currencyBean.configTypes?.indexOf(configType) != -1) {
                                    allList.add(currencyBean)
                                }
                            }
                            symbolList.clear()
                            symbolList.addAll(allList)
                            adapter.notifyDataSetChanged()

                        }

                    }

                })


    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


}
