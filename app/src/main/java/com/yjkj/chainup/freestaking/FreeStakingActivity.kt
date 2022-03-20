package com.yjkj.chainup.freestaking

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.freestaking.adapter.FreeStakingFragmentAdapter
import com.yjkj.chainup.freestaking.bean.FreeStakingBean
import com.yjkj.chainup.freestaking.bean.NotificationRefreshBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_free_staking.*
import kotlinx.android.synthetic.main.what_is_freestaking.view.*
import org.greenrobot.eventbus.EventBus


/**
 * FreeStaking首页
 */
@Route(path = RoutePath.FreeStakingActivity)
class FreeStakingActivity : NewBaseActivity() {
    private var tabList = ArrayList<String>()
    private var fragmentList = ArrayList<Fragment>()
    private var url: String = ""
    private var faqUrl: String = ""
    lateinit var adapter: FreeStakingFragmentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_staking)
        getFreeStakingData()
        initClick()


    }

    private fun initClick() {
        toolbarFreeStaking.setOnLeftImageClickListener {
            finish()
        }
        toolbarFreeStaking.setOnRightImageClickListener {
            var bundle = Bundle()
            bundle.putString(ParamConstant.web_url, url)
            bundle.putString(ParamConstant.head_title, "")
            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
        }
        toolbarFreeStaking.setOnRightTextClickListener {
            //            if (!LoginManager.checkLogin(this, true)) {
//                return@setOnRightTextClickListener
//            }
//            val intent = Intent(this, PositionRecordActivity::class.java)
//            intent.putExtra(PROJECT_TYPE, 3)
//            startActivity(intent)
            if (LoginManager.checkLogin(this, true)) {
                val bundle = Bundle()
                bundle.putInt(PROJECT_TYPE, 3)
                ArouterUtil.greenChannel(RoutePath.PositionRecordActivity, bundle)
            }
        }


        /**
         * FAQ点击事件
         */
        ll_what_is_freestaking.tv_problem.setOnClickListener {
            var bundle = Bundle()
            bundle.putString(ParamConstant.URL_4_SERVICE, faqUrl)
            ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
        }


    }


    private fun initView(freeStakingBean: FreeStakingBean?) {
        toolbarFreeStaking.setRightText(freeStakingBean?.tipMine)
        ll_what_is_freestaking.tv_what_is_freestaking.text = freeStakingBean?.footTitle
        ll_what_is_freestaking.tv_content.text = freeStakingBean?.detail
        ll_what_is_freestaking.tv_contactUs.text = freeStakingBean?.contact
        url = freeStakingBean?.url.toString()
        faqUrl = freeStakingBean?.faqUrl.toString()
        //顶部banner图片
        if (isDestroyed) {
            return
        }
        Glide.with(this).load(freeStakingBean?.banner).into(iv_head)
        tabList.clear()
        fragmentList.clear()
        tabList.add(getString(R.string.pos_string_all))
        for (i in freeStakingBean?.typeConfig!!) {
            tabList.add(i.typeName.toString())

        }
        for (i in 0 until tabList.size) {
            if (i == 0) {
                fragmentList.add(FreeStakingFragment.newInstance(vp_container, "", i))
            } else {
                fragmentList.add(FreeStakingFragment.newInstance(vp_container, freeStakingBean.typeConfig!![i - 1].typeSn!!, i))

            }


        }
        adapter = FreeStakingFragmentAdapter(supportFragmentManager, tabList, fragmentList)
        vp_container.adapter = adapter
        stl_kind.setViewPager(vp_container)
        vp_container.resetHeight(0)
        try {
            vp_container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    /**
                     * 0 - 无变化
                     * 1 - 正在滚动
                     * 2 - 滑动完毕
                     */
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    /**
                     * position 当前页面，及你点击滑动的页面；
                     * positionOffset:当前页面偏移的百分比；
                     * positionOffsetPixels:当前页面偏移的像素位置
                     */
                }

                override fun onPageSelected(position: Int) {
                    vp_container.currentItem = position
                    vp_container.resetHeight(position)
                    //这样处理是为了造成一种假象，造成每次都是重新加载的最新的数据,因为每次切换的时候都要恢复到最初的状态
                    EventBus.getDefault().post(NotificationRefreshBean("refresh"))
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 获取FreeStaking首页数据
     */
    private fun getFreeStakingData() {
        HttpClient.instance.getFreeStakingData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<FreeStakingBean>() {
                    override fun onHandleSuccess(freeStakingBean: FreeStakingBean?) {
                        freeStakingBean?.let {
                            initView(it)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                    }

                })

    }

}
