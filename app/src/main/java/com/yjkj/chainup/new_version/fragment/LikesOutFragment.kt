package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.chainup.contract.utils.setSafeListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.new_version.adapter.PageAdapter
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_likes_out.*
import kotlinx.android.synthetic.main.fragment_market_type.*
import org.json.JSONObject
import java.util.HashMap

class LikesOutFragment : NBaseFragment(), WsAgentManager.WsResultCallback {
    val fragments = arrayListOf<Fragment>()

    override fun setContentView(): Int {
        return R.layout.fragment_likes_out;
    }

    override fun initView() {
        fragments.add(LikesFragment())
        fragments.add(LikesHeYueFragment())
        val titles = arrayListOf<String>()
        titles.add("")
        titles.add("")
        tv_bibi.setSafeListener {
            clickTab(0)
            vp_like.currentItem = 0
        }
        tv_heyue.setSafeListener {
            clickTab(1)
            vp_like.currentItem = 1
        }
        vp_like.adapter = PageAdapter(childFragmentManager, titles, fragments);
        vp_like.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                clickTab(position)
            }

        })
        iv_edit?.setOnClickListener {
            var fragment = fragments[vp_like.currentItem]
            when (fragment) {
                is LikesFragment -> {
                    ArouterUtil.greenChannel(RoutePath.LikeEditActivity, Bundle())
                }
                is LikesHeYueFragment -> {
                    Toast.makeText(activity, "合约编辑", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun clickTab(position: Int) {
        if (position == 0) {
            tv_bibi.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            tv_bibi.setBackgroundResource(com.chainup.contract.R.drawable.coins_exchange_buy_blue)
            tv_heyue.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
            tv_heyue.setBackgroundResource(com.chainup.contract.R.drawable.coins_exchange_sell_grey)
        } else if (position == 1) {
            tv_bibi.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
            tv_bibi.setBackgroundResource(com.chainup.contract.R.drawable.coins_exchange_buy_grey)
            tv_heyue.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            tv_heyue.setBackgroundResource(com.chainup.contract.R.drawable.coins_exchange_sell_blue)
        }
        startInit()
    }

    override fun onWsMessage(json: String) {
    }

    fun startInit() {
        var fragment = fragments[vp_like.currentItem]
        when (fragment) {
            is LikesFragment -> {
                fragment.startInit()
            }
            is LikesHeYueFragment -> {
                fragment.startInit()
            }
        }
    }

    fun handleData(data: String) {
        var fragment = fragments[vp_like.currentItem]
        when (fragment) {
            is LikesFragment -> {
                fragment.handleData(data)
            }
            is LikesHeYueFragment -> {
                fragment.handleData(data)
            }
        }
    }

    fun handleData(items: HashMap<String, JSONObject>) {
        var fragment = fragments[vp_like.currentItem]
        when (fragment) {
            is LikesFragment -> {
                fragment.handleData(items)
            }
            is LikesHeYueFragment -> {
                fragment.handleData(items)
            }
        }
    }

    fun getCoins(): ArrayList<Any> {
        val items = arrayListOf<Any>()
        /*if (adapter != null && adapter?.data != null) {
            items.addAll(adapter?.data!!)
        }*/
        return items
    }
}