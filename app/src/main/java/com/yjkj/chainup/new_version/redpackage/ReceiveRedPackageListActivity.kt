package com.yjkj.chainup.new_version.redpackage

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.redpackage.adapter.ReceiveAdapter
import com.yjkj.chainup.new_version.redpackage.bean.ReceiveRedPackageBean
import com.yjkj.chainup.new_version.redpackage.bean.ReceiveRedPackageInfoBean
import com.yjkj.chainup.new_version.redpackage.bean.ReceiveRedPackageListBean
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_receive_red_package_list.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_receive_red_package_list.rv_all_list
import kotlinx.android.synthetic.main.activity_receive_red_package_list.swipe_refresh
import kotlinx.android.synthetic.main.activity_receive_red_package_list.toolbar
import kotlinx.android.synthetic.main.header_receive_red_package.*
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019/6/29-16:26 AM
 * @Description: 收到红包的列表界面
 *
 */
class ReceiveRedPackageListActivity : NewBaseActivity() {

    var redPackageDialog: TDialog? = null
    var subTitleList = ArrayList<String>()

    var adapter = ReceiveAdapter(arrayListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_red_package_list)

        initRecycleView()

        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(context, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(context, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        collapsing_toolbar?.title = LanguageUtil.getString(this, "redpacket_received_received")
        subTitleList.add(LanguageUtil.getString(context, "redpacket_sendout_sendPackets"))
        subTitleList.add(LanguageUtil.getString(context, "redpacket_received_received"))

        receiveRedPackageList()
        getReceiveRedPackageInfo()

    }

    private fun initRecycleView() {
        rv_all_list?.layoutManager = LinearLayoutManager(context)
        rv_all_list?.adapter = adapter
        adapter.setEmptyView(EmptyForAdapterView(this))
        adapter.setHeaderView(layoutInflater.inflate(R.layout.header_receive_red_package, null))

        /**
         * 进入红包详情
         */
        adapter.setOnItemClickListener { adapter, view, position ->
            val redPacket = adapter.data[position] as ReceiveRedPackageBean
            RedPackageDetailActivity.enter2(context, redPacket.packetSn.toString(), false)
        }
        swipe_refresh?.setOnRefreshListener {
            page = 1
            adapter.loadMoreModule.isEnableLoadMore = false
            receiveRedPackageList(false)
        }
        adapter.loadMoreModule.setOnLoadMoreListener {
            LogUtil.e("LogUtils", "loadMore()")
            receiveRedPackageList(true)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_red_package, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu -> {
                redPackageDialog = NewDialogUtils.showBottomListDialog(context, subTitleList, 1, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        redPackageDialog?.dismissAllowingStateLoss()
                        when (item) {
                            0 -> {
                                startActivity(Intent(context, GrantRedPackageListActivity::class.java))
                                finish()
                            }

                            1 -> {
                                /* startActivity(Intent(context, ReceiveRedPackageListActivity::class.java))
                                 finish()*/
                            }
                        }
                    }
                })
            }
        }
        return true
    }

    var page = 1
    val pageSize = 20

    /**
     * 收到红包的信息
     */
    private fun getReceiveRedPackageInfo() {
        HttpClient.instance
                .getReceiveRedPackageInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ReceiveRedPackageInfoBean>() {
                    override fun onHandleSuccess(bean: ReceiveRedPackageInfoBean?) {
                        printLogcat("收到的红包信息:" + bean?.toString())

                        tv_total_amount?.text = BigDecimal(bean?.allAmount.toString()).toPlainString()

                        tv_coin?.text = NCoinManager.getShowMarket(bean?.rateSymbol)

                        tv_package_mount?.text = LanguageUtil.getString(context, "redpacket_received_num").format(bean?.count)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@ReceiveRedPackageListActivity,false, msg)
                    }
                })
    }

    /**
     * 收到红包的列表
     */
    private fun receiveRedPackageList(isMore: Boolean = false, pageSize: Int = 1000) {
        HttpClient.instance
                .receiveRedPackageList(pageNum = page, pageSize = pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ReceiveRedPackageListBean>() {
                    override fun onHandleSuccess(bean: ReceiveRedPackageListBean?) {
                        printLogcat("收到的红包列表: " + bean?.toString())
                        initData(isMore, bean)
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@ReceiveRedPackageListActivity,false, msg)
                        if (!isMore) {
                            getReceiveRedPackageInfo()
                        }
                    }
                })
    }

    fun initData(isMoreRef: Boolean, bean: ReceiveRedPackageListBean?) {
        if (!isMoreRef) {
            getReceiveRedPackageInfo()
        }
        val list = bean?.mapList
        if (list != null) {
            if (!isMoreRef) {
                adapter.setList(list as java.util.ArrayList<ReceiveRedPackageBean>)
            } else {
                adapter.addData(list as java.util.ArrayList<ReceiveRedPackageBean>)
            }
            val isMore = list.size == pageSize
            if (isMore) page++
            adapter.loadMoreModule.isEnableLoadMore = true
            if (!isMore) {
                adapter.loadMoreModule.loadMoreEnd(!isMoreRef)
            } else {
                adapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            if (!isMoreRef) adapter.setEmptyView(EmptyForAdapterView(context))
            adapter.loadMoreModule.loadMoreEnd(isMoreRef)
        }
        swipe_refresh?.isRefreshing = false

    }
}
