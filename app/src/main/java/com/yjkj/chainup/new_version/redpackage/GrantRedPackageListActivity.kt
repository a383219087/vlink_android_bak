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
import com.yjkj.chainup.new_version.redpackage.adapter.GrantAdapter
import com.yjkj.chainup.new_version.redpackage.bean.GrantRedPackageInfo
import com.yjkj.chainup.new_version.redpackage.bean.GrantRedPackageListBean
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_grant_red_package_list.*
import kotlinx.android.synthetic.main.header_grant_red_package.*
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019/7/1-14:26 AM
 * @Description: 发出红包的列表
 *
 * item_grant_red_package
 * GrantAdapter
 */
class GrantRedPackageListActivity : NewBaseActivity() {
    var redPackageDialog: TDialog? = null
    var subTitleList = ArrayList<String>()

    val grantAdapter = GrantAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grant_red_package_list)

        initRecyclerView()

        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(context, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(context, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        collapsing_toolbar?.title = LanguageUtil.getString(this, "redpacket_sendout_sendPackets")
        subTitleList.add(LanguageUtil.getString(context, "redpacket_sendout_sendPackets"))
        subTitleList.add(LanguageUtil.getString(context, "redpacket_received_received"))

        grantRedPackageList()

        getGrantRedPackageInfo()
    }

    private fun initRecyclerView() {
        rv_all_list?.layoutManager = LinearLayoutManager(context)
        rv_all_list?.adapter = grantAdapter
        grantAdapter.setEmptyView(EmptyForAdapterView(this))
        grantAdapter.setHeaderView(layoutInflater.inflate(R.layout.header_grant_red_package, null))
        /**
         * 进入红包详情
         */
        grantAdapter.setOnItemClickListener { adapter, view, position ->
            val redPacket = adapter.data[position] as GrantRedPackageListBean.RedPacket
            printLogcat(redPacket.redPacketSn.toString())
            RedPackageDetailActivity.enter2(context, redPacket.redPacketSn.toString())
        }
        swipe_refresh?.setOnRefreshListener {
            page = 1
            grantAdapter.loadMoreModule.isEnableLoadMore = false
            grantRedPackageList(false)
        }
        grantAdapter.loadMoreModule.setOnLoadMoreListener({
            LogUtil.e("LogUtils", "loadMore()")
            grantRedPackageList(true)
        })
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
                redPackageDialog = NewDialogUtils.showBottomListDialog(context, subTitleList, 0, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        redPackageDialog?.dismissAllowingStateLoss()
                        when (item) {
                            0 -> {
                                /* startActivity(Intent(context, GrantRedPackageListActivity::class.java))
                                 finish()*/
                            }

                            1 -> {
                                startActivity(Intent(context, ReceiveRedPackageListActivity::class.java))
                                finish()
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
     * 发出红包的信息
     */
    private fun getGrantRedPackageInfo() {
        HttpClient.instance
                .getGrantRedPackageInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<GrantRedPackageInfo>() {
                    override fun onHandleSuccess(bean: GrantRedPackageInfo?) {
                        printLogcat("发出的红包信息:" + bean?.toString())
                        Log.d(TAG, "=========${tv_total_amount == null}========")

                        tv_total_amount?.text = BigDecimal(bean?.allAmount.toString()).toPlainString()

                        tv_coin?.text = NCoinManager.getShowMarket(bean?.rateSymbol)

                        tv_reg?.text = bean?.newCount.toString() + LanguageUtil.getString(context, "redpacket_sendout_people")

                        tv_package_mount?.text = LanguageUtil.getString(context, "redpacket_sendout_total").format(bean?.allCount)

                        tv_get?.text = LanguageUtil.getString(context, "redpacket_sendout_totalPeople").format(bean?.getCount)

                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@GrantRedPackageListActivity,false, msg)
                    }
                })
    }


    /**
     * 发出红包的列表
     */
    private fun grantRedPackageList(isMore: Boolean = false, pageSize: Int = 10) {
        HttpClient.instance
                .grantRedPackageList(pageNum = page, pageSize = pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<GrantRedPackageListBean>() {
                    override fun onHandleSuccess(bean: GrantRedPackageListBean?) {
                        printLogcat("发出的红包列表:" + bean?.toString())
                        initData(isMore, bean)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@GrantRedPackageListActivity,false, msg)
                        if (!isMore) {
                            getGrantRedPackageInfo()
                        }

                    }
                })
    }

    fun initData(isMoreRef: Boolean, bean: GrantRedPackageListBean?) {
        if (!isMoreRef) {
            getGrantRedPackageInfo()
        }
        val list = bean?.redPacketList
        if (list != null) {
            if (!isMoreRef) {
                grantAdapter.setList(list as java.util.ArrayList<GrantRedPackageListBean.RedPacket>)
            } else {
                grantAdapter.addData(list as java.util.ArrayList<GrantRedPackageListBean.RedPacket>)
            }
            val isMore = list.size == pageSize
            if (isMore) page++
            grantAdapter.loadMoreModule.isEnableLoadMore = true
            if (!isMore) {
                grantAdapter.loadMoreModule.loadMoreEnd(!isMoreRef)
            } else {
                grantAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            if (!isMoreRef) grantAdapter.setEmptyView(EmptyForAdapterView(context))
            grantAdapter.loadMoreModule.loadMoreEnd(isMoreRef)
        }
        swipe_refresh?.isRefreshing = false

    }

}
