package com.yjkj.chainup.new_version.redpackage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.redpackage.adapter.ReceiveAdapter
import com.yjkj.chainup.new_version.redpackage.bean.CreatePackageBean
import com.yjkj.chainup.new_version.redpackage.bean.ReceiveRedPackageBean
import com.yjkj.chainup.new_version.redpackage.bean.RedPackageDetailBean
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_red_package_detail.*
import org.jetbrains.anko.textColor
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.math.abs


/**
 * @Author: Bertking
 * @Date：2019/7/3-14:26 AM
 * @Description: 红包详情
 */
class RedPackageDetailActivity : NewBaseActivity() {

    var isGrant = true

    var menu: Menu? = null

    var adapter = ReceiveAdapter(arrayListOf(), true)

    var url4redPackage: String = ""

    //红包状态 1.领取中 2.已领完 3.已过期
    var redPackageStatus: Int = 0

    var createPackageBean: CreatePackageBean? = null

    companion object {
        const val SIGN = "sign"
        private const val IS_GRANT = "is_grant"
        fun enter2(context: Context, sign: String, isGrant: Boolean = true) {
            val intent = Intent(context, RedPackageDetailActivity::class.java)
            intent.putExtra(SIGN, sign)
            intent.putExtra(IS_GRANT, isGrant)
            context.startActivity(intent)
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_red_package_detail)
        StatusBarUtil.setColor(this, ColorUtil.getColor(R.color.red_package_red))
//        StatusBarUtil.transparentStatusBar(this)
        val sign = intent?.getStringExtra(SIGN) ?: ""
        isGrant = intent?.getBooleanExtra(IS_GRANT, true) ?: true

        if (isGrant) {
            copy_red_package_link?.visibility = View.VISIBLE
        } else {
            copy_red_package_link?.visibility = View.GONE
        }

        rv_all_list?.layoutManager = LinearLayoutManager(context)
        rv_all_list?.adapter = adapter
        adapter.setEmptyView(EmptyForAdapterView(this))

        getRedPackageDetail(sign)

        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        toolbar?.setNavigationOnClickListener {
            finish()
        }




        scrollView?.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            Log.d(TAG, "====Y:====$scrollY=======")

            if (abs(scrollY) > 100) {
                supportActionBar?.title = LanguageUtil.getString(context, "redpacket_received_detail")
                tv_title?.text = ""
            } else {
                supportActionBar?.title = ""
                tv_title?.text = LanguageUtil.getString(context, "redpacket_received_detail")
            }
        }
        copy_red_package_link?.setContent(LanguageUtil.getString(this, "redpacket_sendout_copyRedPacket"))
        copy_red_package_link?.clicked = true
        copy_red_package_link?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (TextUtils.isEmpty(url4redPackage)) {
                    showSnackBar(LanguageUtil.getString(context, "copy_failed"), false)
                } else {
                    //红包状态 1.领取中 2.已领完 3.已过期
                    if (redPackageStatus == 1) {
                        Utils.copyString(url4redPackage)
                        showSnackBar(LanguageUtil.getString(context, "common_tip_copySuccess"), true)
                    } else {
                        showSnackBar(LanguageUtil.getString(context, "redpacket_sendout_cannotReplicated"), false)
                    }

                }
            }
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menu?.getItem(0)?.isVisible = isGrant
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_share -> {
                NewDialogUtils.share4RedPackage(context, createPackageBean, isDirectShare = true)
            }
        }
        return true
    }


    /**
     * 红包的信息
     */
    private fun getRedPackageDetail(packetSn: String) {
        HttpClient.instance
                .getRedPackageDetail(packetSn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<RedPackageDetailBean>() {
                    override fun onHandleSuccess(bean: RedPackageDetailBean?) {
                        printLogcat("红包详情:${bean?.toString()}")

                        createPackageBean = CreatePackageBean(background = bean?.background, nickName = bean?.nickName, coinSymbol = bean?.coinSymbol, shareUrl = bean?.url)
                        if (bean?.mapList?.size != 0) {
                            adapter.setList(bean?.mapList as ArrayList<ReceiveRedPackageBean>)
                        }
                        //红包状态 1.领取中 2.已领完 3.已过期
                        if (isGrant) {
                            if (bean?.status == 1) {
                                copy_red_package_link?.visibility = View.VISIBLE
                                menu?.getItem(0)?.isVisible = true
                            } else {
                                copy_red_package_link?.visibility = View.GONE
                                menu?.getItem(0)?.isVisible = false
                            }
                        } else {
                            menu?.getItem(0)?.isVisible = false
                            copy_red_package_link?.visibility = View.GONE
                        }

                        url4redPackage = bean?.url ?: ""
                        redPackageStatus = bean?.status ?: 0

                        tv_red_package_owner?.text = LanguageUtil.getString(context, "redpacket_send_from").format(bean?.nickName)
                        if (isGrant) {
                            /**
                             * 发出的红包详情
                             */
                            when (bean?.status) {
                                //红包状态 1.领取中 2.已领完 3.已过期
                                1 -> {
                                    tv_red_package_status?.text = LanguageUtil.getString(context, "redpacket_sendout_receive").format("${bean?.getCount}", "${bean?.count}", "${BigDecimal(bean?.getAmount?.toString()).toPlainString()}", "${BigDecimal(bean?.amount?.toString()).toPlainString()}", "${NCoinManager.getShowMarket(bean?.coinSymbol)?.toUpperCase().toString()}")
                                }

                                2 -> {
                                    tv_red_package_status?.text = LanguageUtil.getString(context, "redpacket_sendout_goneDetail").format(bean?.count, "${BigDecimal(bean?.amount?.toString()).toPlainString()}", "${NCoinManager.getShowMarket(bean?.coinSymbol)?.toUpperCase().toString()}")
                                }

                                3 -> {
                                    tv_red_package_status?.text = LanguageUtil.getString(context, "redpacket_sendout_overdue").format(bean?.getAmount, bean?.count, BigDecimal(bean?.getAmount?.toString()).toPlainString(), BigDecimal(bean?.amount?.toString()).toPlainString(), NCoinManager.getShowMarket(bean?.coinSymbol)?.toUpperCase().toString())
                                }
                            }
                            ll_receive?.visibility = View.GONE
                            tv_tips?.textColor = ColorUtil.getColor(R.color.normal_text_color)
                            tv_tips?.text = bean?.tip
                        } else {
                            /**
                             * 收到的红包详情
                             */
                            when (bean?.status) {
                                // 领取中
                                1 -> {
                                    tv_red_package_status?.text = LanguageUtil.getString(context, "redpacket_received_opened").format(bean?.getCount, bean?.count)
                                }
                                2 -> {
                                    tv_red_package_status?.text = LanguageUtil.getString(context, "redpacket_received_gone").format(bean?.count)
                                }
                                else -> {
                                    ""
                                }
                            }
                            ll_receive?.visibility = View.VISIBLE
                            tv_receive_amount?.text = BigDecimal(bean?.myAmount.toString()).toPlainString()
                            tv_receive_coin?.text = NCoinManager.getShowMarket(bean?.coinSymbol).toString()
                            tv_tips?.textColor = ColorUtil.getColor(R.color.main_color)
                            tv_tips?.text = LanguageUtil.getString(context, "redpacket_received_withdraw")
                            tv_tips?.setOnClickListener {
                                if (PublicInfoDataService.getInstance().contractOpen(null) && PublicInfoDataService.getInstance().otcOpen(null)) {
                                    ArouterUtil.navigation(RoutePath.NewVersionMyAssetActivity, null)
                                } else {
                                    forwardHomeTab()
                                    finish()
                                }
                            }

                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@RedPackageDetailActivity,false, msg)
                    }

                })
    }

    private fun forwardHomeTab() {
        var messageEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        bundle.putInt(ParamConstant.homeTabType, 0)
        messageEvent.msg_content = bundle
        EventBusUtil.post(messageEvent)
    }

}
