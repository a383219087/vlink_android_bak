package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.OTCOrderBean
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.new_version.adapter.OTCOrderAdapter
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mine_order_activity.*

/**
 * @Author lianshangljl
 * @Date 2019/4/24-10:03 AM
 * @Email buptjinlong@163.com
 * @description 我的订单
 */
class MineOrderActivity : NewBaseActivity() {

    var status = ""

    companion object {
        const val ORDER_STATUS = "ORDER_STATUS"
        fun enter2(context: Context, status: String) {
            var intent = Intent()
            intent.setClass(context, MineOrderActivity::class.java)
            intent.putExtra(ORDER_STATUS, status)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mine_order_activity)
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }
        }
        setTextContent()
    }
    fun setTextContent(){
        title_layout?.setContentTitle(LanguageUtil.getString(this,"otc_text_myOrder"))
    }

    fun initView(t: ArrayList<OTCOrderBean.Order>) {

        var adapter = OTCOrderAdapter(t)
        if (rv_order_otc!= null) {
            rv_order_otc.layoutManager = LinearLayoutManager(this)
            adapter.setEmptyView(EmptyForAdapterView(context ?: return))
            rv_order_otc.adapter = adapter
            adapter.setOnItemClickListener { adapter, view, position ->

                var item = t[position]

                /**
                 * 根据订单状态
                 * status：订单状态 待支付1 已支付2 交易成功3 取消 4 申诉 5 打币中6 异常订单7 申诉处理结束8
                 */
                when (item.status) {
                    3 -> {
                        if (item.side == "BUY") {
                            NewVersionBuyOrderActivity.enter2(this, item.sequence)
                        } else {
                            NewVersionSellOrderActivity.enter2(this, orderId = item.sequence)
                        }
                    }


                    4 -> {
                        if (item.side == "BUY") {
                            NewVersionBuyOrderActivity.enter2(this, orderId = item.sequence)
                        } else {
                            NewVersionSellOrderActivity.enter2(this, orderId = item.sequence)
                        }
                    }

                    else -> {
                        if (item.side == "BUY") {
                            /**
                             * 买
                             */
                            NewVersionBuyOrderActivity.enter2(this, orderId = item.sequence)
                        } else {
                            /**
                             * 卖
                             */
                            NewVersionSellOrderActivity.enter2(this, orderId = item.sequence)

                        }
                    }
                }


            }
        }

    }

    fun getData() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        HttpClient.instance.byStatus4OTC(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCOrderBean>() {
                    override fun onHandleSuccess(t: OTCOrderBean?) {
                        if (t != null) {
                            initView(t.orderList)
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }
}