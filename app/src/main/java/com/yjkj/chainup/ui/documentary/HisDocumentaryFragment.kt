package com.yjkj.chainup.ui.documentary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.new_version.adapter.ClContractHistoricalPositionAdapter
import com.yjkj.chainup.ui.documentary.vm.NowDocumentViewModel
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_now_documentary.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class HisDocumentaryFragment : BaseMVFragment<NowDocumentViewModel?, FragmentNowDocumentaryBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(status: Int, uid: String): HisDocumentaryFragment {
            val fg = HisDocumentaryFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            bundle.putString(ParamConstant.MARKET_NAME, uid)
            fg.arguments = bundle
            return fg
        }

    }

    private var adapter: ClContractHistoricalPositionAdapter? = null
    private var mList = ArrayList<JSONObject>()


    override fun setContentView(): Int = R.layout.fragment_now_documentary
    override fun initView() {
        mViewModel?.activity?.value = mActivity
        mViewModel?.status?.value = arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.uid?.value = arguments?.getString(ParamConstant.MARKET_NAME)

        adapter = ClContractHistoricalPositionAdapter(mContext!!, mList)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            if (mViewModel?.uid?.value.isNullOrEmpty()) {
                val item = adapter.data[position] as JSONObject
                ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity)
                    .withSerializable("json", item.toString())
                    .withString("id", item.optString("id") )
                    .withInt("status", mViewModel?.status?.value!!)
                    .navigation()
            }
        }

    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            getList()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getList() {
        mList.clear()
        if (mViewModel?.status?.value == 1) {
            mViewModel?.startTask(mViewModel?.contractApiService!!.findHisSingleList(), Consumer {
                val jsonObject = JSONUtil.parse(it, true)
                jsonObject.optJSONObject("data").run {
                    val mPositionList = optJSONArray("positionList")
                    if (mPositionList!=null&&mPositionList.length() != 0) {
                        tv_em.visibility = View.GONE
                        for (i in 0 until mPositionList.length()) {
                            var obj: JSONObject = mPositionList.get(i) as JSONObject
                            mList.add(obj)
                        }
                        adapter?.notifyDataSetChanged()
                    } else {
                        tv_em.visibility = View.VISIBLE
                    }


                }

            })
        } else {
            val map = HashMap<String, Any>()

            if (mViewModel?.uid?.value.isNullOrEmpty()) {
                map["traderUid"] = UserDataService.getInstance().userInfo4UserId
            } else {
                map["traderUid"] = mViewModel?.uid?.value.toString()
            }

            mViewModel?.startTask(mViewModel?.contractApiService!!.traderPositionList1(map), Consumer {
                val jsonObject = JSONUtil.parse(it, true)
                jsonObject.optJSONObject("data").run {
                    val mPositionList = optJSONArray("positionList")
                    if (mPositionList!=null&&mPositionList.length() != 0) {
                        tv_em.visibility = View.GONE
                        for (i in 0 until mPositionList.length()) {
                            var obj: JSONObject = mPositionList.get(i) as JSONObject
                            mList.add(obj)
                        }
                        adapter?.notifyDataSetChanged()
                    } else {
                        tv_em.visibility = View.VISIBLE
                    }


                }

            })
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {

            MessageEvent.refresh_MyInviteCodesActivity -> {
                getList()
            }
        }
    }


}




